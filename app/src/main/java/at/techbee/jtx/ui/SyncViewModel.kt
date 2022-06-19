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
import at.techbee.jtx.database.*



class SyncViewModel(application: Application) : AndroidViewModel(application) {

    val database = ICalDatabase.getInstance(application).iCalDatabaseDao
    val remoteCollections = database.getAllRemoteCollections()
    val isSyncInProgress = MutableLiveData(false)
    val isDavx5Available = MutableLiveData(false)
}
