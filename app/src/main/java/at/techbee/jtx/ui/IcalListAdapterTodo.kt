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
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.database.Module
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.util.DateTimeUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/*



            if (model.searchModule == Module.TODO.name && settingShowSubtasks && settingShowProgressMaintasks && iCal4ListItem.property.numSubtasks > 0)
                holder.expandSubtasks.visibility = View.VISIBLE
            else
                holder.expandSubtasks.visibility = View.INVISIBLE



            var toggleSubtasksExpanded = true

            holder.expandSubtasks.setOnClickListener {

                if (!toggleSubtasksExpanded) {
                    IcalListAdapterHelper.addSubtasksView(model, itemSubtasks.distinct(), holder.subtasksLinearLayout, context, parent)
                    toggleSubtasksExpanded = true
                    holder.expandSubtasks.setImageResource(R.drawable.ic_collapse)
                } else {
                    holder.subtasksLinearLayout.removeAllViews()
                    toggleSubtasksExpanded = false
                    holder.expandSubtasks.setImageResource(R.drawable.ic_expand)
                }
            }


}

 */
