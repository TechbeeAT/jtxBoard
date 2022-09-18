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
import at.techbee.jtx.util.DateTimeUtils.addLongToCSVString
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


            if (iCalObjectUpdated.value?.recurOriginalIcalObjectId != null && iCalObjectUpdated.value?.isRecurLinkedInstance == false) {
                val newExceptionList = addLongToCSVString(
                    database.getRecurExceptions(iCalObjectUpdated.value?.recurOriginalIcalObjectId!!),
                    iCalObjectUpdated.value!!.dtstart
                )
                database.setRecurExceptions(
                    iCalObjectUpdated.value?.recurOriginalIcalObjectId!!,
                    newExceptionList,
                    System.currentTimeMillis()
                )
            }

            /* DONE MIGRATION
            if(iCalEntity.ICalCollection?.collectionId != selectedCollectionId
                && selectedCollectionId != null
                && iCalEntity.property.id != 0L) {
                val newId = ICalObject.updateCollectionWithChildren(iCalEntity.property.id, null, selectedCollectionId!!, getApplication())

                // once the newId is there, the local entries can be deleted (or marked as deleted)
                ICalObject.deleteItemWithChildren(iCalEntity.property.id, database)        // make sure to delete the old item (or marked as deleted - this is already handled in the function)
                insertedOrUpdatedItemId = newId
            }

            iCalObjectUpdated.value!!.id = insertedOrUpdatedItemId
            if (recurrenceList.size > 0 || iCalObjectUpdated.value!!.id != 0L)    // recreateRecurring if the recurrenceList is not empty, but also when it is an update, as the recurrence might have been deactivated and it is necessary to delete instances
                iCalObjectUpdated.value?.recreateRecurring(database, getApplication())

             */

            returnIcalObjectId.postValue(insertedOrUpdatedItemId)
        }
    }
}

