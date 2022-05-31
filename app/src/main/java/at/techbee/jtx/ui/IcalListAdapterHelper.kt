/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.databinding.FragmentIcalListItemAttachmentBinding
import at.techbee.jtx.databinding.FragmentIcalListItemSubtaskBinding
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.util.DateTimeUtils.getAttachmentSizeString
import com.google.android.material.slider.Slider
import java.io.FileNotFoundException
import java.lang.NullPointerException
/*
class IcalListAdapterHelper {


            subtasks.forEach { subtask ->

                // if there is a search for statusTodo given, then the subtask is only taken if it is in the given status
                if(model.searchStatusTodo.isNotEmpty() && !model.searchStatusTodo.contains(StatusTodo.getFromString(subtask.status)))
                    return@forEach

                if(model.isExcludeDone && subtask.percent == 100)          // if done tasks are excluded, we must also exclude the subtask and just skip here
                    return@forEach




 */
