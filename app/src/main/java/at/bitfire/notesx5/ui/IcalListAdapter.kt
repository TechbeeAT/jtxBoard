/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attachment
import at.bitfire.notesx5.database.relations.ICal4ListWithRelatedto
import at.bitfire.notesx5.database.views.ICal4List
import at.bitfire.notesx5.databinding.FragmentIcalListItemAttachmentBinding
import at.bitfire.notesx5.databinding.FragmentIcalListItemSubtaskBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class IcalListAdapter(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapter.VJournalItemHolder>() {

    lateinit var parent: ViewGroup
    private lateinit var settings: SharedPreferences
    private var settingShowSubtasks = true
    private var settingShowAttachments = true
    private var settingShowProgress = true
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
    private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VJournalItemHolder {

        this.parent = parent
        //load settings
        settings = PreferenceManager.getDefaultSharedPreferences(context)
        settingShowSubtasks = settings.getBoolean(SettingsFragment.SHOW_SUBTASKS_IN_LIST, true)
        settingShowAttachments = settings.getBoolean(SettingsFragment.SHOW_ATTACHMENTS_IN_LIST, true)
        settingShowProgress = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_IN_LIST, true)

        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_ical_list_item, parent, false)
        return VJournalItemHolder(itemHolder)

    }

    override fun getItemCount(): Int {

        //Log.println(Log.INFO, "getItemCount", vJournalListCount.value.toString())
        //Log.println(Log.INFO, "getItemCount", vJournalList.value?.size!!.toString())

        return if (iCal4List.value == null || iCal4List.value?.size == null)
            0
        else
            iCal4List.value?.size!!
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: VJournalItemHolder, position: Int) {


        if (iCal4List.value?.size == 0)    // only continue if there are items in the list
            return

        val dtstartVisibility = if (model.searchModule == Module.JOURNAL.name) View.VISIBLE else View.GONE
        val statusVisibility = if (model.searchModule == Module.JOURNAL.name) View.VISIBLE else View.GONE
        val classificationVisibility = if (model.searchModule == Module.JOURNAL.name) View.VISIBLE else View.GONE
        val progressVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE
        val subtaskExpandVisibility = if (model.searchModule == Module.TODO.name && settingShowSubtasks) View.VISIBLE else View.GONE
        val priorityVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE
        val dueVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE
        val subtasksVisibility = if (settingShowSubtasks)  View.VISIBLE else View.GONE


        holder.dtstartDay.visibility = dtstartVisibility
        holder.dtstartMonth.visibility = dtstartVisibility
        holder.dtstartYear.visibility = dtstartVisibility
        holder.dtstartTime.visibility = dtstartVisibility
        holder.status.visibility = statusVisibility
        holder.statusIcon.visibility = statusVisibility
        holder.classification.visibility = classificationVisibility
        holder.classificationIcon.visibility = classificationVisibility
        holder.progressLabel.visibility = progressVisibility
        holder.progressSlider.visibility = progressVisibility
        holder.progressPercent.visibility = progressVisibility
        holder.progressCheckbox.visibility = progressVisibility
        holder.priorityIcon.visibility = priorityVisibility
        holder.priority.visibility = priorityVisibility
        holder.due.visibility = dueVisibility
        holder.expandSubtasks.visibility = subtaskExpandVisibility
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

            if (iCal4ListItem.property.categories.isNullOrEmpty()) {
                holder.categories.visibility = View.GONE
            } else {
                holder.categories.text = iCal4ListItem.property.categories
                holder.categories.visibility = View.VISIBLE
            }

            if (iCal4ListItem.property.color != null) {
                try {
                    holder.colorBar.setColorFilter(iCal4ListItem.property.color!!)
                } catch (e: IllegalArgumentException) {
                    Log.println(
                        Log.INFO,
                        "Invalid color",
                        "Invalid Color cannot be parsed: ${iCal4ListItem.property.color}"
                    )
                    holder.colorBar.visibility = View.GONE
                }
            } else
                holder.colorBar.visibility = View.GONE

            if (iCal4ListItem.property.module == Module.JOURNAL.name) {
                holder.dtstartDay.text = convertLongToDayString(iCal4ListItem.property.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(iCal4ListItem.property.dtstart)
                holder.dtstartYear.text = convertLongToYearString(iCal4ListItem.property.dtstart)

                if (iCal4ListItem.property.dtstartTimezone == "ALLDAY") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text =
                        convertLongToTimeString(iCal4ListItem.property.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }
            }

            /* START handle subtasks */
            holder.progressSlider.value = iCal4ListItem.property.percent?.toFloat() ?: 0F
            holder.progressCheckbox.isChecked = iCal4ListItem.property.percent == 100
            if (iCal4ListItem.relatedto?.isNotEmpty() == true && iCal4ListItem.property.component == Component.VTODO.name && settingShowSubtasks) {   // TODO: also tasks with a subnote would be shown here, they should also be excluded!
                holder.expandSubtasks.visibility = View.VISIBLE
            } else {
                holder.expandSubtasks.visibility = View.INVISIBLE
            }

            //holder.subtasksLinearLayout.visibility = View.VISIBLE
            holder.progressPercent.text = "${iCal4ListItem.property.percent?:0} %"

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
                if (iCal4ListItem.property.dueTimezone == "ALLDAY")
                    millisLeft =
                        millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
                val daysLeft =
                    TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                val hoursLeft =
                    TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                when {
                    millisLeft < 0L -> holder.due.text =
                        context.getString(R.string.list_due_overdue)
                    millisLeft >= 0L && daysLeft == 0L && iCal4ListItem.property.dueTimezone == "ALLDAY" -> holder.due.text =
                        context.getString(R.string.list_due_today)
                    millisLeft >= 0L && daysLeft == 1L && iCal4ListItem.property.dueTimezone == "ALLDAY" -> holder.due.text =
                        context.getString(R.string.list_due_tomorrow)
                    millisLeft >= 0L && daysLeft <= 1L && iCal4ListItem.property.dueTimezone != "ALLDAY" -> holder.due.text =
                        context.getString(R.string.list_due_inXhours, hoursLeft)
                    millisLeft >= 0L && daysLeft >= 2L -> holder.due.text =
                        context.getString(R.string.list_due_inXdays, daysLeft)
                    else -> holder.due.visibility = View.GONE      //should not be possible
                }
            }

            if (iCal4ListItem.property.percent == 100)
                holder.progressCheckbox.isActivated = true
            /* END handle subtasks */


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


                    // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment()
                        .setItem2show(iCal4ListItem.property.id)
                )
            }

            var resetProgress = iCal4ListItem.property.percent ?: 0

            // take care to update the progress in the DB when the progress is changed
            if (iCal4ListItem.property.module == Module.TODO.name) {
                holder.progressSlider.addOnSliderTouchListener(object :
                    Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
                    }

                    override fun onStopTrackingTouch(slider: Slider) {

                        model.updateProgress(
                            iCal4ListItem.property.id,
                            holder.progressSlider.value.toInt()
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
                        holder.progressSlider.value.toInt()
                    )
                }
            }

            val itemSubtasks =
                allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id } != null }
            itemSubtasks?.forEach {
                addSubtasksView(it, holder)
            }


            if(iCal4ListItem.attachment.isNullOrEmpty()) {
                holder.attachmentsLinearLayout.visibility = View.GONE
            } else {
                holder.attachmentsLinearLayout.visibility = View.VISIBLE
                addAttachmentView(iCal4ListItem.attachment, holder)
            }




            var toggleSubtasksExpanded = true

            holder.expandSubtasks.setOnClickListener {

                if (!toggleSubtasksExpanded) {
                    itemSubtasks?.forEach {
                        addSubtasksView(it, holder)
                    }
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
                    itemSubtasks?.forEach {
                        addSubtasksView(it, holder)
                    }
                    toggleSubtasksExpanded = true
                    holder.expandSubtasks.setImageResource(R.drawable.ic_collapse)
                } else {
                    holder.subtasksLinearLayout.removeAllViews()
                    toggleSubtasksExpanded = false
                    holder.expandSubtasks.setImageResource(R.drawable.ic_expand)
                }
            }

        }

        //TODO: Check the impact of this setting!
        // Trying out if this solves the weird behaviour that sometimes fields in the cardview are just missing
        // Scrolling is actually not so smooth, but it looks like the weird behaviour is not there anymore...
        holder.setIsRecyclable(false)

    }


    class VJournalItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val listItemBinding = FragmentIcalListItemBinding.inflate(LayoutInflater.from(itemView.context), itemView as ViewGroup, false)
        var listItemCardView: MaterialCardView = itemView.findViewById(R.id.list_item_card_view)

        var summary: TextView = itemView.findViewById(R.id.list_item_summary)
        var description: TextView = itemView.findViewById(R.id.list_item_description)

        var categories: TextView = itemView.findViewById(R.id.list_item_categories)
        var status: TextView = itemView.findViewById(R.id.list_item_status)
        var statusIcon: ImageView = itemView.findViewById(R.id.list_item_status_icon)
        var classification: TextView = itemView.findViewById(R.id.list_item_classification)
        var classificationIcon: ImageView =
            itemView.findViewById(R.id.list_item_classification_icon)
        var priority: TextView = itemView.findViewById(R.id.list_item_priority)
        var priorityIcon: ImageView = itemView.findViewById(R.id.list_item_priority_icon)

        var progressLabel: TextView = itemView.findViewById(R.id.list_item_progress_label)
        var progressSlider: Slider = itemView.findViewById(R.id.list_item_progress_slider)
        var progressPercent: TextView = itemView.findViewById(R.id.list_item_progress_percent)
        var progressCheckbox: CheckBox = itemView.findViewById(R.id.list_item_progress_checkbox)

        var due: TextView = itemView.findViewById(R.id.list_item_due)

        var dtstartDay: TextView = itemView.findViewById(R.id.list_item_dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById(R.id.list_item_dtstart_month)
        var dtstartYear: TextView = itemView.findViewById(R.id.list_item_dtstart_year)
        var dtstartTime: TextView = itemView.findViewById(R.id.list_item_dtstart_time)

        var colorBar: ImageView = itemView.findViewById(R.id.list_item_colorbar)

        var expandSubtasks: ImageView = itemView.findViewById(R.id.list_item_expand)
        var subtasksLinearLayout: LinearLayout =
            itemView.findViewById(R.id.list_item_subtasks_linearlayout)

        var attachmentsLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_attachments)

        var numAttendeesIcon: ImageView = itemView.findViewById(R.id.list_item_num_attendees_icon)
        var numAttachmentsIcon: ImageView = itemView.findViewById(R.id.list_item_num_attachments_icon)
        var numCommentsIcon: ImageView = itemView.findViewById(R.id.list_item_num_comments_icon)
        var numAttendeesText: TextView = itemView.findViewById(R.id.list_item_num_attendees_text)
        var numAttachmentsText: TextView = itemView.findViewById(R.id.list_item_num_attachments_text)
        var numCommentsText: TextView = itemView.findViewById(R.id.list_item_num_comments_text)


    }


    private fun addSubtasksView(subtask: ICal4List?, holder: VJournalItemHolder) {

        if (subtask == null)
            return

        var resetProgress = subtask.percent
            ?: 0             // remember progress to be reset if the checkbox is unchecked

        val subtaskBinding = FragmentIcalListItemSubtaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        var subtaskSummary = subtask.summary
        //val subtaskCount = model.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
        /*if (subtaskCount != null)
            subtaskSummary += " (+${subtaskCount})" */
        if (subtask.numSubtasks > 0)
            subtaskSummary += " (+${subtask.numSubtasks})"

        subtaskBinding.listItemSubtaskTextview.text = subtaskSummary
        subtaskBinding.listItemSubtaskProgressSlider.value =
            if (subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskBinding.listItemSubtaskProgressPercent.text =
            if (subtask.percent?.toFloat() != null) "${subtask.percent!!} %" else "0 %"
        subtaskBinding.listItemSubtaskProgressCheckbox.isChecked = subtask.percent == 100

        // Instead of implementing here
        //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
        //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */


        subtaskBinding.listItemSubtaskProgressSlider.addOnSliderTouchListener(object :
            Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (subtaskBinding.listItemSubtaskProgressSlider.value < 100)
                    resetProgress = subtask.percent ?: 0

                model.updateProgress(
                    subtask.id,
                    subtaskBinding.listItemSubtaskProgressSlider.value.toInt()
                )
            }
        })


        subtaskBinding.listItemSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->

            if (checked)
                subtaskBinding.listItemSubtaskProgressSlider.value = 100F
            else
                subtaskBinding.listItemSubtaskProgressSlider.value = resetProgress.toFloat()

            model.updateProgress(
                subtask.id,
                subtaskBinding.listItemSubtaskProgressSlider.value.toInt()
            )
        }

        subtaskBinding.root.setOnClickListener {
            holder.listItemCardView.findNavController().navigate(
                IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment()
                    .setItem2show(subtask.id)
            )
        }

        if(settingShowProgress) {
            subtaskBinding.listItemSubtaskProgressSlider.visibility = View.VISIBLE
            subtaskBinding.listItemSubtaskProgressPercent.visibility = View.VISIBLE
        } else {
            subtaskBinding.listItemSubtaskProgressSlider.visibility = View.GONE
            subtaskBinding.listItemSubtaskProgressPercent.visibility = View.GONE
        }

        holder.subtasksLinearLayout.addView(subtaskBinding.root)
    }



    private fun addAttachmentView(attachments: List<Attachment>?, holder: VJournalItemHolder) {

        holder.attachmentsLinearLayout.removeAllViews()

        if(settingShowAttachments) {

            attachments?.forEach { attachment ->

                val attachmentBinding = FragmentIcalListItemAttachmentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                //open the attachment on click
                attachmentBinding.listItemAttachmentCardview.setOnClickListener {

                    try {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setDataAndType(Uri.parse(attachment.uri), attachment.fmttype)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)

                    } catch (e: IOException) {
                        Log.i("fileprovider", "Failed to retrieve file\n$e")
                        Toast.makeText(context, "Failed to retrieve file.", Toast.LENGTH_LONG)
                            .show()
                    } catch (e: ActivityNotFoundException) {
                        Log.i("ActivityNotFound", "No activity found to open file\n$e")
                        Toast.makeText(
                            context,
                            "No app was found to open this file.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
                if (attachment.filename?.isNotEmpty() == true)
                    attachmentBinding.listItemAttachmentTextview.text = attachment.filename
                else if (attachment.fmttype?.isNotEmpty() == true)
                    attachmentBinding.listItemAttachmentTextview.text = attachment.fmttype
                else
                    attachmentBinding.listItemAttachmentTextview.text = "<Attachment>"

                if (attachment.filesize == null)
                    attachmentBinding.listItemAttachmentFilesize.visibility = View.GONE
                else
                    attachmentBinding.listItemAttachmentFilesize.text = getAttachmentSizeString(attachment.filesize?:0L)

                // load thumbnail if possible
                // deactivated for now as the loading caused some lags in the recycler view!
                /*
                try {
                    val thumbSize = Size(50, 50)
                    val thumbUri = Uri.parse(attachment.uri)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val thumbBitmap =
                            context.contentResolver!!.loadThumbnail(thumbUri, thumbSize, null)
                        attachmentBinding.listItemAttachmentPictureThumbnail.setImageBitmap(
                            thumbBitmap
                        )
                        attachmentBinding.listItemAttachmentPictureThumbnail.visibility =
                            View.VISIBLE
                    }
                } catch (e: FileNotFoundException) {
                    Log.d("FileNotFound", "File with uri ${attachment.uri} not found.\n$e")
                }
                 */

                holder.attachmentsLinearLayout.addView(attachmentBinding.root)
            }
        }
    }
}


