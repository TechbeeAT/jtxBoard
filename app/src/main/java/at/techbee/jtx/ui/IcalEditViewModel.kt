/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IcalEditViewModel(
    application: Application,
    val iCalEntity: ICalEntity
) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    var returnIcalObjectId: MutableLiveData<Long> =
        MutableLiveData<Long>().apply { postValue(0L) }
    var collectionNotFoundError: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(false)

    var iCalObjectUpdated: MutableLiveData<ICalObject> =
        MutableLiveData<ICalObject>().apply { postValue(iCalEntity.property) }


    fun update() {
        var insertedOrUpdatedItemId: Long

        viewModelScope.launch(Dispatchers.IO) {
            // the case that an item gets deleted at the same time the user was already editing this item, is currently not handled.
            // On save the user would not get an error, he would return to the overview with the deleted item missing
            try {
                // we insert or update - if the collection was changed, we still update the current entry and move the item to the new collection at the end
                insertedOrUpdatedItemId = if(iCalObjectUpdated.value!!.id == 0L)
                        database.insertICalObject(iCalObjectUpdated.value!!)
                    else {
                        database.update(iCalObjectUpdated.value!!)
                        iCalObjectUpdated.value!!.id
                    }
                iCalObjectUpdated.value!!.id = insertedOrUpdatedItemId
            } catch (e: SQLiteConstraintException) {
                Log.d("SQLConstraint", e.toString())
                Log.d("SQLConstraint", iCalObjectUpdated.value.toString())
                collectionNotFoundError.postValue(true)
                //savingClicked.postValue(false)
                collectionNotFoundError.postValue(false)
                return@launch
            }

            returnIcalObjectId.postValue(insertedOrUpdatedItemId)
        }
    }
/*
    viewModelScope.launch {

        progressIndicatorVisible = Transformations.map(icalEntity) { item ->
            return@map item?.property == null     // show progress indicator as long as item.property is null
        }
    }

 */
}

