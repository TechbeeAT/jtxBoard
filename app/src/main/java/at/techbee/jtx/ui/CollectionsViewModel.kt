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
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CollectionsViewModel(val database: ICalDatabaseDao,
                           application: Application) : AndroidViewModel(application) {

    val localCollections = database.getLocalCollections()
    val remoteCollections = database.getRemoteCollections()
    val isDavx5Compatible = MutableLiveData(SyncUtil.isDAVx5CompatibleWithJTX(application))


    /**
     * saves the given collection with the new values or inserts a new collection if collectionId == 0L
     * @param [collection] to be saved
     */
    fun saveCollection(collection: ICalCollection) {
        viewModelScope.launch(Dispatchers.IO) {

            if(collection.collectionId == 0L)
                database.insertCollectionSync(collection)
            else
                database.updateCollection(collection)
        }
    }

    fun deleteCollection(collection: ICalCollection) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteICalCollection(collection)
        }
    }
}

