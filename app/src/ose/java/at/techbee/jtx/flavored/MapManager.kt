/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.database.ICalObject

class MapManager(val context: Context): MapManagerDefinition {

    override fun showLocationPickerDialog(inflater: LayoutInflater, iCalObject: MutableLiveData<ICalObject>) {  /* do nothing */   }
    override fun addMap(layout: LinearLayout, lat: Double, lng: Double, label: String?) {  /* do nothing */   }

}