/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */


package at.techbee.jtx.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import at.techbee.jtx.database.relations.ICalEntity

class IcalEditViewModelFactory (
        private val application: Application,
        private val iCalEntity: ICalEntity) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IcalEditViewModel::class.java)) {
                return IcalEditViewModel(application, iCalEntity) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}


