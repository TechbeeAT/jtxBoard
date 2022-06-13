/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Application
import androidx.lifecycle.*
import at.techbee.jtx.database.ICalCollection
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    val database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao
    val collections = database.getAllCollectionsView()
    val isDavx5Compatible = MutableLiveData(SyncUtil.isDAVx5CompatibleWithJTX(application))
    val app = application

    val collectionICS = MutableLiveData<String>(null)
    val allCollectionICS = MutableLiveData<List<Pair<String, String>>>(null)

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
            val objectsToMove = database.getICalObjectIdsWithinCollection(oldCollectionId)
            objectsToMove.forEach {
                val newId = ICalObject.updateCollectionWithChildren(it, null, newCollectionId, getApplication())
                database.getICalObjectByIdSync(newId)?.recreateRecurring(database, app)
            }
            objectsToMove.forEach {
                ICalObject.deleteItemWithChildren(it, database)
            }
            SyncUtil.notifyContentObservers(getApplication())
            isProcessing.postValue(false)
        }
    }

    fun requestICSForCollection(collection: ICalCollection) {
        isProcessing.postValue(true)

        viewModelScope.launch(Dispatchers.IO)  {
            val account = collection.getAccount()
            val collectionId = collection.collectionId
            collectionICS.postValue(Ical4androidUtil.getICSFormatForCollectionFromProvider(account, getApplication(), collectionId))
            isProcessing.postValue(false)
        }
    }

    fun requestAllForExport(collections: List<ICalCollection>) {
        isProcessing.postValue(true)
        val icsList: MutableList<Pair<String, String>> = mutableListOf()   // first of pair is filename/collectionname, second is ics

        viewModelScope.launch(Dispatchers.IO)  {
            collections.forEach { collection ->
                val account = collection.getAccount()
                val collectionId = collection.collectionId
                val ics = (Ical4androidUtil.getICSFormatForCollectionFromProvider(account, getApplication(), collectionId))
                ics?.let { icsList.add(Pair(collection.displayName?:collection.collectionId.toString(), it)) }
            }
            allCollectionICS.postValue(icsList)
            isProcessing.postValue(false)
        }
    }


    fun insertICSFromReader(collection: ICalCollection, ics: String) {

        viewModelScope.launch(Dispatchers.IO)  {
            val resultPair = Ical4androidUtil.insertFromReader(collection.getAccount(), getApplication(), collection.collectionId, ics.reader())
            if(collection.accountType != LOCAL_ACCOUNT_TYPE)
                SyncUtil.notifyContentObservers(getApplication())
            resultInsertedFromICS.postValue(resultPair)
        }
    }

}

