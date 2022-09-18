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
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.relations.ICalEntity
import at.techbee.jtx.util.SyncUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IcalViewViewModel(application: Application, private val icalItemId: Long) : AndroidViewModel(application) {

    private var database: ICalDatabaseDao = ICalDatabase.getInstance(application).iCalDatabaseDao

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var recurInstances: LiveData<List<ICalObject?>>

    lateinit var dtstartTimezone: LiveData<String>

    lateinit var progressIndicatorVisible: LiveData<Boolean>
    val showSyncProgressIndicator = MutableLiveData(false)

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

    lateinit var collectionText: LiveData<String?>


    init {

        viewModelScope.launch {

            progressIndicatorVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property == null     // show progress indicator as long as item.property is null
            }
        }
    }
}
