/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */


package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.database.relations.ICalEntity

class IcalEditViewModelFactory (
        private val iCalEntity: ICalEntity,
        private val dataSource: ICalDatabaseDao,
        private val application: Application) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IcalEditViewModel::class.java)) {
                return IcalEditViewModel(iCalEntity, dataSource, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}



