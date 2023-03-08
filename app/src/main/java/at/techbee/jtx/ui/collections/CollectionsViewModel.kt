/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.collections


import android.accounts.Account
import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    val database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao
    val collections = database.getAllCollectionsView()
    val app = application

    val collectionsICS = MutableLiveData<List<Pair<String, String>>?>(null)
    val isProcessing = MutableLiveData(false)
    val resultInsertedFromICS = MutableLiveData<Pair<Int, Int>?>(null)


    /**
     * saves the given collection with the new values or inserts a new collection if collectionId == 0L
     * @param [collection] to be saved
     */
    fun saveCollection(collection: ICalCollection) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {

            if(collection.collectionId == 0L)
                database.insertCollectionSync(collection)
            else
                database.updateCollection(collection)

            isProcessing.postValue(false)
        }
    }

    fun deleteCollection(collection: ICalCollection) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteICalCollection(collection)
            isProcessing.postValue(false)
        }
    }

    fun moveCollectionItems(oldCollectionId: Long, newCollectionId: Long) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            val objectsToMove = database.getICalObjectIdsToMove(oldCollectionId)
            objectsToMove.forEach {
                val newId = ICalObject.updateCollectionWithChildren(it, null, newCollectionId, database, getApplication()) ?: return@forEach
                database.getICalObjectByIdSync(newId)?.recreateRecurring(app)
            }
            objectsToMove.forEach {
                ICalObject.deleteItemWithChildren(it, database)
            }
            SyncUtil.notifyContentObservers(getApplication())
            isProcessing.postValue(false)
        }
    }


    fun requestICSForExport(collections: List<CollectionsView>) {
        isProcessing.postValue(true)
        val icsList: MutableList<Pair<String, String>> = mutableListOf()   // first of pair is filename/collectionname, second is ics

        viewModelScope.launch(Dispatchers.IO)  {
            collections.forEach { collection ->
                val ics = (Ical4androidUtil.getICSFormatForCollectionFromProvider(Account(collection.accountName, collection.accountType), getApplication(), collection.collectionId))
                ics?.let { icsList.add(Pair(collection.displayName?:collection.collectionId.toString(), it)) }
            }
            collectionsICS.postValue(icsList)
            isProcessing.postValue(false)
        }
    }

    fun exportICSasZIP(resultExportFilepath: Uri?,context: Context) {

        if(resultExportFilepath == null || collectionsICS.value == null)
            return

        isProcessing.postValue(true)
        try {
            val output: OutputStream? = context.contentResolver?.openOutputStream(resultExportFilepath)
            val bos = BufferedOutputStream(output)
            ZipOutputStream(bos).use { zos ->
                collectionsICS.value?.forEach { ics ->
                    // not available on BufferedOutputStream
                    zos.putNextEntry(ZipEntry("${ics.first}.ics"))
                    zos.write(ics.second.toByteArray())
                    zos.closeEntry()
                }
            }
            output?.flush()
            output?.close()
            Toast.makeText(context, R.string.collections_toast_export_all_ics_success, Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, R.string.collections_toast_export_all_ics_error, Toast.LENGTH_LONG).show()
        } finally {
            collectionsICS.value = null
            isProcessing.postValue(false)
        }
    }

    fun exportICS(resultExportFilepath: Uri?, context: Context) {

        if(resultExportFilepath == null || collectionsICS.value == null)
            return

        isProcessing.postValue(true)
        try {
            val output: OutputStream? =
                context.contentResolver?.openOutputStream(resultExportFilepath)
            output?.write(collectionsICS.value?.first()?.second?.toByteArray())
            output?.flush()
            output?.close()
            Toast.makeText(context, R.string.collections_toast_export_ics_success, Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, R.string.collections_toast_export_ics_error, Toast.LENGTH_LONG).show()
        } finally {
            collectionsICS.value = null
            isProcessing.postValue(false)
        }
    }


    fun insertICSFromReader(collection: ICalCollection, ics: String) {

        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO)  {
            val resultPair = Ical4androidUtil.insertFromReader(collection.getAccount(), getApplication(), collection.collectionId, ics.reader())
            if(collection.accountType != LOCAL_ACCOUNT_TYPE)
                SyncUtil.notifyContentObservers(getApplication())
            resultInsertedFromICS.postValue(resultPair)
            isProcessing.postValue(false)
        }
    }


    /**
     * This function removes an account with all its collections from jtx Board.
     */
    fun removeAccount(account: Account) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteAccount(account.name, account.type)
            isProcessing.postValue(false)
        }
    }
}

