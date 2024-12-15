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
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.settings.DropdownSettingOption
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.IOException
import java.time.LocalTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class CollectionsExportMimetype(val mimeType: String) { ICS("text/calendar"), ZIP("application/zip") }

class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val _application = application
    val database: ICalDatabaseDao = ICalDatabase.getInstance(_application).iCalDatabaseDao()
    val collections = database.getAllCollectionsView()

    val isProcessing = MutableLiveData(false)
    val toastText = MutableLiveData<String>(null)
    val resultInsertedFromICS = MutableLiveData<Pair<Int, Int>?>(null)
    val collectionsToExport = MutableLiveData<List<CollectionsView>>(emptyList())


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
            database.moveToCollection(objectsToMove, newCollectionId)
            SyncUtil.notifyContentObservers(getApplication())
            isProcessing.postValue(false)
        }
    }

    /**
     * Exports the given collections in collectionsToExport to the given uri
     * @param [resultExportFilepath] where the data should be saved.
     */
    fun writeToFile(resultExportFilepath: Uri, collectionsExportMimetype: CollectionsExportMimetype) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _application.contentResolver?.openOutputStream(resultExportFilepath)?.use { outputStream ->
                    if(collectionsToExport.value.isNullOrEmpty())
                        throw IOException("Collections to export are empty")

                    when(collectionsExportMimetype) {
                        CollectionsExportMimetype.ICS -> {
                            val collection = collectionsToExport.value?.first() ?: throw IOException("Collections to export are empty")
                            Ical4androidUtil.getICSFormatForCollectionFromProvider(collection.toICalCollection().getAccount(), getApplication(), collection.collectionId)?.let { ics ->
                                outputStream.write(ics.toByteArray())
                            }
                        }
                        CollectionsExportMimetype.ZIP -> {
                            val bos = BufferedOutputStream(outputStream)
                            ZipOutputStream(bos).use { zos ->
                                collectionsToExport.value?.forEach { collection ->
                                    Ical4androidUtil.getICSFormatForCollectionFromProvider(collection.toICalCollection().getAccount(), getApplication(), collection.collectionId)
                                        ?.let { ics ->
                                            zos.putNextEntry(ZipEntry("${collection.displayName ?: collection.collectionId.toString()}.ics"))
                                            zos.write(ics.toByteArray())
                                            zos.closeEntry()
                                        }
                                }
                            }
                        }
                    }
                }
                toastText.postValue(_application.getString(R.string.collections_toast_export_success))
            } catch (e: IOException) {
                toastText.postValue(_application.getString(R.string.collections_toast_export_error))
            } finally {
                isProcessing.postValue(false)
                collectionsToExport.postValue(emptyList())
            }
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

    fun insertTxt(
        text: String,
        module: Module,
        collection: ICalCollection,
        defaultJournalDateSettingOption: DropdownSettingOption,
        defaultStartDateSettingOption: DropdownSettingOption,
        defaultStartTime: LocalTime?,
        defaultStartTimezone: String?,
        defaultDueDateSettingOption: DropdownSettingOption,
        defaultDueTime: LocalTime?,
        defaultDueTimezone: String?
    ) {
        isProcessing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            database.insertICalObject(
                ICalObject.fromText(
                    module,
                    collection.collectionId,
                    text,
                    defaultJournalDateSettingOption,
                    defaultStartDateSettingOption,
                    defaultStartTime,
                    defaultStartTimezone,
                    defaultDueDateSettingOption,
                    defaultDueTime,
                    defaultDueTimezone
                )
            )
            if(collection.accountType != LOCAL_ACCOUNT_TYPE)
                SyncUtil.notifyContentObservers(getApplication())
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

