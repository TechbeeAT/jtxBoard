/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Relatedto
import at.techbee.jtx.database.properties.Reltype
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var relatedNotes: LiveData<List<ICal4List>>
    lateinit var relatedSubtasks: LiveData<List<ICal4List>>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var progressIndicatorVisible: LiveData<Boolean>

    var icsFormat: MutableLiveData<String?> = MutableLiveData(null)
    var icsFileWritten: MutableLiveData<Boolean?> = MutableLiveData(null)

    var entryDeleted = mutableStateOf(false)


    init {

        viewModelScope.launch {

            // insert a new value to initialize the item or load the existing one from the DB
            icalEntity = MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }

            relatedNotes = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid -> database.getAllSubnotesOf(parentUid) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.uid?.let { parentUid -> database.getAllSubtasksOf(parentUid) }
            }

            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }

            progressIndicatorVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property == null     // show progress indicator as long as item.property is null
            }
        }
    }

    fun load(icalObjectId: Long) {
        viewModelScope.launch {
            icalEntity = database.get(icalObjectId)
        }

    }


    fun insertRelated(newIcalObject: ICalObject, attachment: Attachment?) {

        this.icalEntity.value?.property?.let {
            makeRecurringExceptionIfNecessary(it)
        }

        viewModelScope.launch {
            newIcalObject.collectionId = icalEntity.value?.ICalCollection?.collectionId ?: 1L
            val newNoteId = database.insertICalObject(newIcalObject)

            // We insert both directions in the database - deprecated, only one direction
            //database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = newIcalObject.uid))
            database.insertRelatedto(Relatedto(icalObjectId = newNoteId, reltype = Reltype.PARENT.name, text = icalEntity.value!!.property.uid))

            if(attachment != null) {
                attachment.icalObjectId = newNoteId
                database.insertAttachment(attachment)
            }

            //database.updateSetDirty(icalItemId, System.currentTimeMillis())
            SyncUtil.notifyContentObservers(getApplication())
        }
    }


    fun updateProgress(id: Long, newPercent: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val item = database.getICalObjectById(id) ?: return@launch
            makeRecurringExceptionIfNecessary(item)
            item.setUpdatedProgress(newPercent)
            database.update(item)
            SyncUtil.notifyContentObservers(getApplication())
        }
    }

    private fun makeRecurringExceptionIfNecessary(item: ICalObject) {

        if(item.isRecurLinkedInstance) {
            viewModelScope.launch(Dispatchers.IO) {
                ICalObject.makeRecurringException(item, database)
                SyncUtil.notifyContentObservers(getApplication())
            }
            Toast.makeText(getApplication(), R.string.toast_item_is_now_recu_exception, Toast.LENGTH_SHORT).show()
        }
    }

    fun delete() {
        // TODO: open dialog
        viewModelScope.launch(Dispatchers.IO) {
            icalEntity.value?.property?.id?.let { id ->
                ICalObject.deleteItemWithChildren(id, database)
                entryDeleted.value = true
            }
        }
    }

    fun retrieveICSFormat() {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch
            val ics = Ical4androidUtil.getICSFormatFromProvider(account, getApplication(), collectionId, iCalObjectId) ?: return@launch
            icsFormat.postValue(ics)
        }
    }

    fun writeICSFile(os: ByteArrayOutputStream) {

        viewModelScope.launch(Dispatchers.IO)  {
            val account = icalEntity.value?.ICalCollection?.getAccount() ?: return@launch
            val collectionId = icalEntity.value?.property?.collectionId ?: return@launch
            val iCalObjectId = icalEntity.value?.property?.id ?: return@launch
            icsFileWritten.postValue(Ical4androidUtil.writeICSFormatFromProviderToOS(account, getApplication(), collectionId, iCalObjectId, os))
        }

    }
}
