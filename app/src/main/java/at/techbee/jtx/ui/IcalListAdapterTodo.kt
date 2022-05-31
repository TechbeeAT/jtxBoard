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

class IcalListAdapterTodo(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapterTodo.TodoItemHolder>() {

    lateinit var parent: ViewGroup
    private lateinit var settings: SharedPreferences
    private var settingShowSubtasks = true
    private var settingShowAttachments = true
    private var settingShowProgressSubtasks = true
    private var settingShowProgressMaintasks = false
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4ListTodos
    private var allSubtasks: LiveData<List<ICal4List>> = model.allSubtasks
    private var markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun onBindViewHolder(holder: TodoItemHolder, position: Int) {


            /* START handle subtasks */
            holder.progressSlider.value = iCal4ListItem.property.percent?.toFloat() ?: 0F
            holder.progressCheckbox.isChecked = iCal4ListItem.property.percent == 100
            holder.progressCheckboxTop.isChecked = iCal4ListItem.property.percent == 100

            if (model.searchModule == Module.TODO.name && settingShowSubtasks && settingShowProgressMaintasks && iCal4ListItem.property.numSubtasks > 0)
                holder.expandSubtasks.visibility = View.VISIBLE
            else
                holder.expandSubtasks.visibility = View.INVISIBLE


            //holder.subtasksLinearLayout.visibility = View.VISIBLE
            holder.progressPercent.text = String.format("%.0f%%", iCal4ListItem.property.percent?.toFloat() ?: 0F)

            if (iCal4ListItem.property.priority in 1..9) {           // show priority only if it was set and != 0 (no priority)
                holder.priorityIcon.visibility = View.VISIBLE
                holder.priority.visibility = View.VISIBLE
            } else {
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE
            }

            if (iCal4ListItem.property.due == null)
                holder.due.visibility = View.GONE
            else {
                holder.due.visibility = View.VISIBLE
                val zonedDue = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(iCal4ListItem.property.due!!),
                    DateTimeUtils.requireTzId(iCal4ListItem.property.dueTimezone)).toInstant().toEpochMilli()
                val millisLeft = if(iCal4ListItem.property.dueTimezone == ICalObject.TZ_ALLDAY)
                    zonedDue - DateTimeUtils.getTodayAsLong()
                else
                    zonedDue - System.currentTimeMillis()

                val daysLeft =
                    TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                val hoursLeft =
                    TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                when {
                    millisLeft < 0L -> holder.due.text =
                        context.getString(R.string.list_due_overdue)
                    millisLeft >= 0L && daysLeft == 0L && iCal4ListItem.property.dueTimezone == ICalObject.TZ_ALLDAY -> holder.due.text =
                        context.getString(R.string.list_due_today)
                    millisLeft >= 0L && daysLeft == 1L && iCal4ListItem.property.dueTimezone == ICalObject.TZ_ALLDAY -> holder.due.text =
                        context.getString(R.string.list_due_tomorrow)
                    millisLeft >= 0L && daysLeft <= 1L && iCal4ListItem.property.dueTimezone != ICalObject.TZ_ALLDAY -> holder.due.text =
                        context.getString(R.string.list_due_inXhours, hoursLeft)
                    millisLeft >= 0L && daysLeft >= 2L -> holder.due.text =
                        context.getString(R.string.list_due_inXdays, daysLeft)
                    else -> holder.due.visibility = View.GONE      //should not be possible
                }
            }

            if (iCal4ListItem.property.dtstart == null)
                holder.start.visibility = View.GONE
            else {
                holder.start.visibility = View.VISIBLE

                val zonedStart = ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(iCal4ListItem.property.dtstart!!),
                    DateTimeUtils.requireTzId(iCal4ListItem.property.dtstartTimezone)).toInstant().toEpochMilli()
                val millisLeft = if(iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY)
                    zonedStart - DateTimeUtils.getTodayAsLong()
                else
                    zonedStart - System.currentTimeMillis()

                val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                when {
                    millisLeft < 0L -> holder.start.text =
                        context.getString(R.string.list_start_past)
                    millisLeft >= 0L && daysLeft == 0L && iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY -> holder.start.text =
                        context.getString(R.string.list_start_today)
                    millisLeft >= 0L && daysLeft == 1L && iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY -> holder.start.text =
                        context.getString(R.string.list_start_tomorrow)
                    millisLeft >= 0L && daysLeft <= 1L && iCal4ListItem.property.dtstartTimezone != ICalObject.TZ_ALLDAY -> holder.start.text =
                        context.getString(R.string.list_start_inXhours, hoursLeft)
                    millisLeft >= 0L && daysLeft >= 2L -> holder.start.text =
                        context.getString(R.string.list_start_inXdays, daysLeft)
                    else -> holder.start.visibility = View.GONE      //should not be possible
                }
            }




            val priorityArray = context.resources.getStringArray(R.array.priority)
            if (iCal4ListItem.property.priority != null && iCal4ListItem.property.priority in 0..9)
                holder.priority.text = priorityArray[iCal4ListItem.property.priority!!]



            var resetProgress = iCal4ListItem.property.percent ?: 0

            // take care to update the progress in the DB when the progress is changed
            holder.progressSlider.addOnSliderTouchListener(object :
                Slider.OnSliderTouchListener {

                override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
                }

                override fun onStopTrackingTouch(slider: Slider) {

                    model.updateProgress(
                        iCal4ListItem.property.id,
                        holder.progressSlider.value.toInt(),
                        iCal4ListItem.property.isLinkedRecurringInstance
                    )

                    if (holder.progressSlider.value.toInt() != 100)
                        resetProgress = holder.progressSlider.value.toInt()
                }
            })


            holder.progressCheckbox.setOnCheckedChangeListener { _, checked ->
                if (checked)
                    holder.progressSlider.value = 100F
                else
                    holder.progressSlider.value = resetProgress.toFloat()

                model.updateProgress(
                    iCal4ListItem.property.id,
                    holder.progressSlider.value.toInt(),
                    iCal4ListItem.property.isLinkedRecurringInstance
                )
            }

            holder.progressCheckboxTop.setOnCheckedChangeListener { _, checked ->
                if (checked)
                    holder.progressSlider.value = 100F
                else
                    holder.progressSlider.value = 0F

                model.updateProgress(
                    iCal4ListItem.property.id,
                    holder.progressSlider.value.toInt(),
                    iCal4ListItem.property.isLinkedRecurringInstance
                )
            }

            val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub.id } != null } ?: emptyList()
            IcalListAdapterHelper.addSubtasksView(model, itemSubtasks.distinct(), holder.subtasksLinearLayout, context, parent)

            IcalListAdapterHelper.addAttachmentView(iCal4ListItem.attachment, holder.attachmentsLinearLayout, context, parent)


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

            holder.progressLabel.setOnClickListener {

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

            if(iCal4ListItem.property.isReadOnly) {
                holder.progressCheckboxTop.isEnabled = false
                holder.progressCheckbox.isEnabled = false
                holder.progressSlider.isEnabled = false
            } else {
                holder.progressCheckboxTop.isEnabled = true
                holder.progressCheckbox.isEnabled = true
                holder.progressSlider.isEnabled = true
            }
        }


}

 */
