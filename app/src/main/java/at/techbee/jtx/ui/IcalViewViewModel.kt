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
import androidx.lifecycle.*
import at.techbee.jtx.R
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.util.Ical4androidUtil
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(application: Application, private val icalItemId: Long) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var dtstartTimezone: LiveData<String>

    lateinit var progressIndicatorVisible: LiveData<Boolean>
    val showSyncProgressIndicator = MutableLiveData(false)

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var timezoneVisible: LiveData<Boolean>
    lateinit var locationHeaderVisible: LiveData<Boolean>
    lateinit var locationVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>
    lateinit var uploadPendingVisible: LiveData<Boolean>
    lateinit var recurrenceVisible: LiveData<Boolean>
    lateinit var recurrenceItemsVisible: LiveData<Boolean>
    lateinit var recurrenceLinkedVisible: LiveData<Boolean>
    lateinit var recurrenceGoToOriginalVisible: LiveData<Boolean>
    lateinit var recurrenceIsExceptionVisible: LiveData<Boolean>
    lateinit var recurrenceExceptionsVisible: LiveData<Boolean>
    lateinit var recurrenceAdditionsVisible: LiveData<Boolean>

    var icsFormat: MutableLiveData<String?> = MutableLiveData(null)
    var icsFileWritten: MutableLiveData<Boolean?> = MutableLiveData(null)

    var entryDeleted: MutableLiveData<Boolean> = MutableLiveData(false)

    lateinit var collectionText: LiveData<String?>

    var entryToEdit = MutableLiveData<ICalEntity?>().apply { postValue(null) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the item or load the existing one from the DB
            icalEntity = if (icalItemId == 0L)
                MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else
                database.get(icalItemId)


            recurInstances = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { originalId -> database.getRecurInstances(originalId) }
            }

            progressIndicatorVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property == null     // show progress indicator as long as item.property is null
            }
            dateVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && item.property.dtstartTimezone != ICalObject.TZ_ALLDAY          // true if component == JOURNAL and it is not an All Day Event
            }

            timezoneVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == Component.VJOURNAL.name && item.property.dtstart != null && !(item.property.dtstartTimezone == ICalObject.TZ_ALLDAY || item.property.dtstartTimezone.isNullOrEmpty())           // true if component == JOURNAL and it is not an All Day Event
            }

            uploadPendingVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.dirty == true && item.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE
            }

            collectionText = Transformations.map(icalEntity) { item ->
                if (item?.ICalCollection?.accountName?.isNotEmpty() == true)
                    item.ICalCollection?.displayName + " (" + item.ICalCollection?.accountName + ")"
                else
                    item?.ICalCollection?.displayName ?: "-"
            }
        }
    }


    fun retrieveSubEntryToEdit(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.getSync(id)?.let {
                entryToEdit.postValue(it)
            }
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

    fun delete(item: ICalObject) {

        viewModelScope.launch(Dispatchers.IO) {
            ICalObject.deleteItemWithChildren(item.id, database)
            entryDeleted.postValue(true)
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
