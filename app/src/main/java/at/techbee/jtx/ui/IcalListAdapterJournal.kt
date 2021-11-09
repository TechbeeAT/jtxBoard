/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
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
import java.lang.IllegalArgumentException
import java.util.*

class IcalListAdapterJournal(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapterJournal.JournalItemHolder>() {

    lateinit var parent: ViewGroup
    private lateinit var settings: SharedPreferences
    private var settingShowSubtasks = true
    private var settingShowAttachments = true
    private var settingShowProgressSubtasks = true
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
    private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalItemHolder {

        this.parent = parent
        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        settingShowSubtasks = settings.getBoolean(SettingsFragment.SHOW_SUBTASKS_IN_LIST, true)
        settingShowAttachments = settings.getBoolean(SettingsFragment.SHOW_ATTACHMENTS_IN_LIST, true)
        settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true)

        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_ical_list_item_journal, parent, false)
        return JournalItemHolder(itemHolder)

    }

    override fun getItemCount(): Int {

        return if (iCal4List.value == null || iCal4List.value?.size == null)
            0
        else
            iCal4List.value?.size!!
    }

    override fun onBindViewHolder(holder: JournalItemHolder, position: Int) {


        if (iCal4List.value?.size == 0)    // only continue if there are items in the list
            return

        val dtstartVisibility = if (model.searchModule == Module.JOURNAL.name) View.VISIBLE else View.GONE
        val subtasksVisibility = if (settingShowSubtasks)  View.VISIBLE else View.GONE


        holder.dtstartDay.visibility = dtstartVisibility
        holder.dtstartMonth.visibility = dtstartVisibility
        holder.dtstartYear.visibility = dtstartVisibility
        holder.dtstartTime.visibility = dtstartVisibility
        holder.dtstartTimeZone.visibility = dtstartVisibility
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

            //show the status only for Journals and only if it is DRAFT or CANCELLED
            if(iCal4ListItem.property.status == StatusJournal.DRAFT.name || iCal4ListItem.property.status == StatusJournal.CANCELLED.name) {
                holder.status.visibility = View.VISIBLE
                holder.statusIcon.visibility = View.VISIBLE
            } else {
                holder.status.visibility = View.GONE
                holder.statusIcon.visibility = View.GONE
            }

            // strikethrough the summary if the item is cancelled
            if(iCal4ListItem.property.status == StatusJournal.CANCELLED.name || iCal4ListItem.property.status == StatusTodo.CANCELLED.name) {
                holder.summary.paintFlags = holder.summary.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.summary.paintFlags = holder.summary.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            //show the classification  only for Journals and only if it is PRIVATE or CONFIDENTIAL
            if(model.searchModule == Module.JOURNAL.name && (iCal4ListItem.property.classification == Classification.PRIVATE.name || iCal4ListItem.property.classification == Classification.CONFIDENTIAL.name)) {
                holder.classification.visibility = View.VISIBLE
                holder.classificationIcon.visibility = View.VISIBLE
            } else {
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
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
                } catch (e: IllegalArgumentException) {
                    Log.i("Invalid color","Invalid Color cannot be parsed: ${iCal4ListItem.property.color}")
                    holder.colorBar.visibility = View.INVISIBLE
                }
            } else
                holder.colorBar.visibility = View.INVISIBLE


            holder.dtstartDay.text = convertLongToDayString(iCal4ListItem.property.dtstart)
            holder.dtstartMonth.text = convertLongToMonthString(iCal4ListItem.property.dtstart)
            holder.dtstartYear.text = convertLongToYearString(iCal4ListItem.property.dtstart)

            if (iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY) {
                holder.dtstartTime.visibility = View.GONE
            } else {
                holder.dtstartTime.text = convertLongToTimeString(iCal4ListItem.property.dtstart)
                holder.dtstartTime.visibility = View.VISIBLE
            }

            //set the timezone (if applicable)
            if (iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY || iCal4ListItem.property.dtstartTimezone.isNullOrEmpty() || TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT) == null) {
                holder.dtstartTimeZone.visibility = View.GONE
            } else {
                holder.dtstartTimeZone.text = TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT)
                holder.dtstartTimeZone.visibility = View.VISIBLE
            }



            when (iCal4ListItem.property.component) {
                Component.VTODO.name -> holder.status.text =
                    StatusTodo.getStringResource(context, iCal4ListItem.property.status)
                        ?: iCal4ListItem.property.status       // if unsupported just show whatever is there
                Component.VJOURNAL.name -> holder.status.text =
                    StatusJournal.getStringResource(context, iCal4ListItem.property.status)
                        ?: iCal4ListItem.property.status       // if unsupported just show whatever is there
                else -> holder.status.text =
                    iCal4ListItem.property.status
            }       // if unsupported just show whatever is there

            holder.classification.text =
                Classification.getStringResource(context, iCal4ListItem.property.classification)
                    ?: iCal4ListItem.property.classification    // if unsupported just show whatever is there

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


            val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id } != null } ?: emptyList()
            IcalListAdapterHelper.addSubtasksView(model, itemSubtasks, holder.subtasksLinearLayout, context, parent)

            IcalListAdapterHelper.addAttachmentView(iCal4ListItem.attachment, holder.attachmentsLinearLayout, context, parent)

        }

        //scrolling is much smoother when isRecyclable is set to false
        holder.setIsRecyclable(false)

    }


    class JournalItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val listItemBinding = FragmentIcalListItemBinding.inflate(LayoutInflater.from(itemView.context), itemView as ViewGroup, false)
        var listItemCardView: MaterialCardView = itemView.findViewById(R.id.list_item_journal_card_view)

        var summary: TextView = itemView.findViewById(R.id.list_item_journal_summary)
        var description: TextView = itemView.findViewById(R.id.list_item_journal_description)

        var categories: TextView = itemView.findViewById(R.id.list_item_journal_categories)
        var collection: TextView = itemView.findViewById(R.id.list_item_journal_collection)

        var status: TextView = itemView.findViewById(R.id.list_item_journal_status)
        var statusIcon: ImageView = itemView.findViewById(R.id.list_item_journal_status_icon)
        var classification: TextView = itemView.findViewById(R.id.list_item_journal_classification)
        var classificationIcon: ImageView = itemView.findViewById(R.id.list_item_journal_classification_icon)
        var dtstartDay: TextView = itemView.findViewById(R.id.list_item_journal_dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById(R.id.list_item_journal_dtstart_month)
        var dtstartYear: TextView = itemView.findViewById(R.id.list_item_journal_dtstart_year)
        var dtstartTime: TextView = itemView.findViewById(R.id.list_item_journal_dtstart_time)
        var dtstartTimeZone: TextView = itemView.findViewById(R.id.list_item_journal_dtstart_timezone)

        var colorBar: ImageView = itemView.findViewById(R.id.list_item_journal_colorbar)

        var subtasksLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_journal_subtasks_linearlayout)
        var attachmentsLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_journal_attachments)

        var numAttendeesIcon: ImageView = itemView.findViewById(R.id.list_item_journal_num_attendees_icon)
        var numAttachmentsIcon: ImageView = itemView.findViewById(R.id.list_item_journal_num_attachments_icon)
        var numCommentsIcon: ImageView = itemView.findViewById(R.id.list_item_journal_num_comments_icon)
        var numAttendeesText: TextView = itemView.findViewById(R.id.list_item_journal_num_attendees_text)
        var numAttachmentsText: TextView = itemView.findViewById(R.id.list_item_journal_num_attachments_text)
        var numCommentsText: TextView = itemView.findViewById(R.id.list_item_journal_num_comments_text)

        var recurIcon: ImageView = itemView.findViewById(R.id.list_item_journal_recurring_icon)
    }

}


