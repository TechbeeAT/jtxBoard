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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.relations.ICal4ListWithRelatedto
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.monetization.AdManager
import at.techbee.jtx.monetization.BillingManager
import com.google.android.material.card.MaterialCardView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin

class IcalListAdapterNote(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapterNote.NoteItemHolder>() {

    lateinit var parent: ViewGroup
    private lateinit var settings: SharedPreferences
    private var settingShowSubtasks = true
    private var settingShowAttachments = true
    private var settingShowProgressSubtasks = true
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
    private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks
    private var markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteItemHolder {

        this.parent = parent
        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        settingShowSubtasks = settings.getBoolean(SettingsFragment.SHOW_SUBTASKS_IN_LIST, true)
        settingShowAttachments = settings.getBoolean(SettingsFragment.SHOW_ATTACHMENTS_IN_LIST, true)
        settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true)

        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_ical_list_item_note, parent, false)
        return NoteItemHolder(itemHolder)

    }

    override fun getItemCount() = iCal4List.value?.size ?: 0

    override fun onBindViewHolder(holder: NoteItemHolder, position: Int) {


        if (iCal4List.value?.size == 0)    // only continue if there are items in the list
            return

        val subtasksVisibility = if (settingShowSubtasks)  View.VISIBLE else View.GONE

        holder.subtasksLinearLayout.visibility = subtasksVisibility

        val iCal4ListItem = iCal4List.value?.get(position)

        if (iCal4ListItem != null) {

            holder.summary.text = iCal4ListItem.property.summary
            if (iCal4ListItem.property.description.isNullOrEmpty())
                holder.description.visibility = View.GONE
            else {
                iCal4ListItem.property.description?.let {
                    val descMarkwon = markwon.toMarkdown(it)
                    holder.description.text = descMarkwon
                }
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

            // applying the color
            ICalObject.applyColorOrHide(holder.colorBar, iCal4ListItem.property.color)


            holder.status.text = StatusJournal.getStringResource(context, iCal4ListItem.property.status)
                        ?: iCal4ListItem.property.status       // if unsupported just show whatever is there

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


            // show upload pending symbol if applicable
            if(iCal4ListItem.property.uploadPending)
                holder.uploadPendingIcon.visibility = View.VISIBLE
            else
                holder.uploadPendingIcon.visibility = View.GONE


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

            val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id } != null } ?: emptyList()
            IcalListAdapterHelper.addSubtasksView(model, itemSubtasks.distinct(), holder.subtasksLinearLayout, context, parent)

            IcalListAdapterHelper.addAttachmentView(iCal4ListItem.attachment, holder.attachmentsLinearLayout, context, parent)
        }


        // show ads only for AdFlavors and if the subscription was not purchased (gplay flavor only), show only on every 5th position
        if(position%3 == 2 && AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isAdFreeSubscriptionPurchased?.value == false)
            AdManager.getInstance()?.addAdViewToContainerViewFragment(holder.adContainer, context, AdManager.getInstance()?.unitIdBannerListNote)
        else
            holder.adContainer.visibility = View.GONE


        //scrolling is much smoother when isRecyclable is set to false
        holder.setIsRecyclable(false)
    }


    class NoteItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val listItemBinding = FragmentIcalListItemBinding.inflate(LayoutInflater.from(itemView.context), itemView as ViewGroup, false)
        var listItemCardView: MaterialCardView = itemView.findViewById(R.id.list_item_note_card_view)

        var summary: TextView = itemView.findViewById(R.id.list_item_note_summary)
        var description: TextView = itemView.findViewById(R.id.list_item_note_description)


        var categories: TextView = itemView.findViewById(R.id.list_item_note_categories)
        var collection: TextView = itemView.findViewById(R.id.list_item_note_collection)

        var status: TextView = itemView.findViewById(R.id.list_item_note_status)
        var statusIcon: ImageView = itemView.findViewById(R.id.list_item_note_status_icon)
        var classification: TextView = itemView.findViewById(R.id.list_item_note_classification)
        var classificationIcon: ImageView = itemView.findViewById(R.id.list_item_note_classification_icon)

        var colorBar: ImageView = itemView.findViewById(R.id.list_item_note_colorbar)

        var subtasksLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_note_subtasks_linearlayout)

        var attachmentsLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_note_attachments)

        var numAttendeesIcon: ImageView = itemView.findViewById(R.id.list_item_note_num_attendees_icon)
        var numAttachmentsIcon: ImageView = itemView.findViewById(R.id.list_item_note_num_attachments_icon)
        var numCommentsIcon: ImageView = itemView.findViewById(R.id.list_item_note_num_comments_icon)
        var numAttendeesText: TextView = itemView.findViewById(R.id.list_item_note_num_attendees_text)
        var numAttachmentsText: TextView = itemView.findViewById(R.id.list_item_note_num_attachments_text)
        var numCommentsText: TextView = itemView.findViewById(R.id.list_item_note_num_comments_text)
        var uploadPendingIcon: ImageView = itemView.findViewById(R.id.list_item_note_upload_pending_icon)

        var adContainer: LinearLayout = itemView.findViewById(R.id.list_item_note_adContainer)
    }
}


