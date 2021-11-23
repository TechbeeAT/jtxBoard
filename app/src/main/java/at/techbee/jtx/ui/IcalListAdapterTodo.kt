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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class IcalListAdapterTodo(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapterTodo.TodoItemHolder>() {

    lateinit var parent: ViewGroup
    private lateinit var settings: SharedPreferences
    private var settingShowSubtasks = true
    private var settingShowAttachments = true
    private var settingShowProgressSubtasks = true
    private var settingShowProgressMaintasks = false
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
    private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemHolder {

        this.parent = parent
        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        settingShowSubtasks = settings.getBoolean(SettingsFragment.SHOW_SUBTASKS_IN_LIST, true)
        settingShowAttachments = settings.getBoolean(SettingsFragment.SHOW_ATTACHMENTS_IN_LIST, true)
        settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false)
        settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true)

        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_ical_list_item_todo, parent, false)
        return TodoItemHolder(itemHolder)

    }

    override fun getItemCount(): Int {

        //Log.println(Log.INFO, "getItemCount", vJournalListCount.value.toString())
        //Log.println(Log.INFO, "getItemCount", vJournalList.value?.size!!.toString())

        return if (iCal4List.value == null || iCal4List.value?.size == null)
            0
        else
            iCal4List.value?.size!!
    }

    override fun onBindViewHolder(holder: TodoItemHolder, position: Int) {


        if (iCal4List.value?.size == 0)    // only continue if there are items in the list
            return

        val progressVisibility = if (settingShowProgressMaintasks) View.VISIBLE else View.GONE
        val progressTopVisibility = if (!settingShowProgressMaintasks) View.VISIBLE else View.GONE
        val subtasksVisibility = if (settingShowSubtasks)  View.VISIBLE else View.GONE

        holder.progressLabel.visibility = progressVisibility
        holder.progressSlider.visibility = progressVisibility
        holder.progressPercent.visibility = progressVisibility
        holder.progressCheckbox.visibility = progressVisibility
        holder.progressCheckboxTop.visibility = progressTopVisibility
        holder.subtasksLinearLayout.visibility = subtasksVisibility


        val iCal4ListItem = iCal4List.value?.get(position)

        if (iCal4ListItem != null) {

            holder.summary.text = iCal4ListItem.property.summary
            if (iCal4ListItem.property.description.isNullOrEmpty())
                holder.description.visibility = View.GONE
            else {
                holder.description.text = iCal4ListItem.property.description
                holder.description.visibility = View.VISIBLE
            }



            // strikethrough the summary if the item is cancelled
            if(iCal4ListItem.property.status == StatusTodo.CANCELLED.name) {
                holder.summary.paintFlags = holder.summary.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.summary.paintFlags = holder.summary.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }


            if (iCal4ListItem.property.categories.isNullOrEmpty()) {
                holder.categories.visibility = View.GONE
            } else {
                holder.categories.text = iCal4ListItem.property.categories
                holder.categories.visibility = View.VISIBLE
            }

            if (iCal4ListItem.property.collectionDisplayName.isNullOrEmpty()) {
                holder.collection.visibility = View.GONE
            } else {
                holder.collection.text = iCal4ListItem.property.collectionDisplayName
                holder.collection.visibility = View.VISIBLE
            }

            if (iCal4ListItem.property.color != null) {
                try {
                    holder.colorBar.setColorFilter(iCal4ListItem.property.color!!)
                    holder.colorBar.visibility = View.VISIBLE
                } catch (e: IllegalArgumentException) {
                    Log.i("Invalid color","Invalid Color cannot be parsed: ${iCal4ListItem.property.color}")
                    holder.colorBar.visibility = View.INVISIBLE
                }
            } else
                holder.colorBar.visibility = View.INVISIBLE


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
                var millisLeft = iCal4ListItem.property.due!! - System.currentTimeMillis()
                if (iCal4ListItem.property.dueTimezone == ICalObject.TZ_ALLDAY)
                    millisLeft =
                        millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
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
                var millisLeft = iCal4ListItem.property.dtstart!! - System.currentTimeMillis()
                if (iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY)
                    millisLeft =
                        millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
                val daysLeft =
                    TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                val hoursLeft =
                    TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

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

            /*
            if (iCal4ListItem.property.percent == 100)
                holder.progressCheckbox.isActivated = true

             */
            /* END handle subtasks */


            val priorityArray = context.resources.getStringArray(R.array.priority)
            if (iCal4ListItem.property.priority != null && iCal4ListItem.property.priority in 0..9)
                holder.priority.text = priorityArray[iCal4ListItem.property.priority!!]

            // field numAttendees
            if(iCal4ListItem.property.numAttendees == 0) {
                holder.numAttendeesIcon.visibility = View.GONE
                holder.numAttendeesText.visibility = View.GONE
            } else {
                holder.numAttendeesIcon.visibility = View.VISIBLE
                holder.numAttendeesText.visibility = View.VISIBLE
                holder.numAttendeesText.text = iCal4ListItem.property.numAttendees.toString()
            }

            //field numAttachments
            if(iCal4ListItem.property.numAttachments == 0) {
                holder.numAttachmentsIcon.visibility = View.GONE
                holder.numAttachmentsText.visibility = View.GONE
            } else {
                holder.numAttachmentsIcon.visibility = View.VISIBLE
                holder.numAttachmentsText.visibility = View.VISIBLE
                holder.numAttachmentsText.text = iCal4ListItem.property.numAttachments.toString()
            }

            //field numComments
            if(iCal4ListItem.property.numComments == 0) {
                holder.numCommentsIcon.visibility = View.GONE
                holder.numCommentsText.visibility = View.GONE
            } else {
                holder.numCommentsIcon.visibility = View.VISIBLE
                holder.numCommentsText.visibility = View.VISIBLE
                holder.numCommentsText.text = iCal4ListItem.property.numComments.toString()
            }

            //recur icon and text
            if((iCal4ListItem.property.isRecurringOriginal) || (iCal4ListItem.property.isRecurringInstance && iCal4ListItem.property.isLinkedRecurringInstance)) {
                holder.recurIcon.setImageResource(R.drawable.ic_recurring)
                holder.recurIcon.visibility = View.VISIBLE
            } else if (iCal4ListItem.property.isRecurringInstance && !iCal4ListItem.property.isLinkedRecurringInstance) {
                holder.recurIcon.setImageResource(R.drawable.ic_recur_exception)
                holder.recurIcon.visibility = View.VISIBLE
            }
             else
                holder.recurIcon.visibility = View.GONE




                    // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment()
                        .setItem2show(iCal4ListItem.property.id)
                )
            }

            // on long click we notify the model to get the entity, so that the observer can forward the user to the edit fragment
            holder.listItemCardView.setOnLongClickListener {
                // the observer in the fragment will make sure that the edit fragment is opened for the loaded entity
                model.postDirectEditEntity(iCal4ListItem.property.id)
                true
            }


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

            val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id } != null } ?: emptyList()
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

        }

        //scrolling is much smoother when isRecyclable is set to false
        holder.setIsRecyclable(false)

    }


    class TodoItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val listItemBinding = FragmentIcalListItemBinding.inflate(LayoutInflater.from(itemView.context), itemView as ViewGroup, false)
        var listItemCardView: MaterialCardView = itemView.findViewById(R.id.list_item_todo_card_view)

        var summary: TextView = itemView.findViewById(R.id.list_item_todo_summary)
        var description: TextView = itemView.findViewById(R.id.list_item_todo_description)
        var progressCheckboxTop: CheckBox = itemView.findViewById(R.id.list_item_todo_progress_checkbox_top)


        var categories: TextView = itemView.findViewById(R.id.list_item_todo_categories)
        var collection: TextView = itemView.findViewById(R.id.list_item_todo_collection)

        var priority: TextView = itemView.findViewById(R.id.list_item_todo_priority)
        var priorityIcon: ImageView = itemView.findViewById(R.id.list_item_todo_priority_icon)

        var progressLabel: TextView = itemView.findViewById(R.id.list_item_todo_progress_label)
        var progressSlider: Slider = itemView.findViewById(R.id.list_item_todo_progress_slider)
        var progressPercent: TextView = itemView.findViewById(R.id.list_item_todo_progress_percent)
        var progressCheckbox: CheckBox = itemView.findViewById(R.id.list_item_todo_progress_checkbox)

        var due: TextView = itemView.findViewById(R.id.list_item_todo_due)
        var start: TextView = itemView.findViewById(R.id.list_item_todo_start)


        var colorBar: ImageView = itemView.findViewById(R.id.list_item_todo_colorbar)

        var expandSubtasks: ImageView = itemView.findViewById(R.id.list_item_todo_expand)
        var subtasksLinearLayout: LinearLayout =
            itemView.findViewById(R.id.list_item_todo_subtasks_linearlayout)

        var attachmentsLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_todo_attachments)

        var numAttendeesIcon: ImageView = itemView.findViewById(R.id.list_item_todo_num_attendees_icon)
        var numAttachmentsIcon: ImageView = itemView.findViewById(R.id.list_item_todo_num_attachments_icon)
        var numCommentsIcon: ImageView = itemView.findViewById(R.id.list_item_todo_num_comments_icon)
        var numAttendeesText: TextView = itemView.findViewById(R.id.list_item_todo_num_attendees_text)
        var numAttachmentsText: TextView = itemView.findViewById(R.id.list_item_todo_num_attachments_text)
        var numCommentsText: TextView = itemView.findViewById(R.id.list_item_todo_num_comments_text)

        var recurIcon: ImageView = itemView.findViewById(R.id.list_item_todo_recurring_icon)
    }

}


