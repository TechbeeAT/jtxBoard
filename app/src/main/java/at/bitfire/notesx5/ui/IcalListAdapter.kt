/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.relations.ICal4ListWithRelatedto
import at.bitfire.notesx5.database.views.ICal4List
import at.bitfire.notesx5.databinding.FragmentIcalListItemSubtaskBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class IcalListAdapter(var context: Context, var model: IcalListViewModel):
        RecyclerView.Adapter<IcalListAdapter.VJournalItemHolder>() {

    lateinit var parent: ViewGroup
    private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
    private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VJournalItemHolder {

        this.parent = parent
        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.fragment_ical_list_item, parent, false)
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
        val classificationVisibility = if (model.searchModule  == Module.JOURNAL.name) View.VISIBLE else View.GONE
        val progressVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE
        val priorityVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE
        val dueVisibility = if (model.searchModule  == Module.TODO.name) View.VISIBLE else View.GONE
        val subtasksVisibility = if (model.searchModule == Module.TODO.name) View.VISIBLE else View.GONE


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
        holder.expandSubtasks.visibility = subtasksVisibility
        holder.subtasksLinearLayout.visibility = subtasksVisibility


        val iCal4ListItem = iCal4List.value?.get(position)

        if (iCal4ListItem != null ) {

            holder.summary.text = iCal4ListItem.property.summary
            if(iCal4ListItem.property.description.isNullOrEmpty())
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

            if(iCal4ListItem.property.color != null) {
                try {
                    holder.colorBar.setColorFilter(iCal4ListItem.property.color!!)
                } catch (e: IllegalArgumentException) {
                    Log.println(Log.INFO, "Invalid color", "Invalid Color cannot be parsed: ${iCal4ListItem.property.color}")
                    holder.colorBar.visibility = View.GONE
                }
            }
            else
                holder.colorBar.visibility = View.GONE

            if(iCal4ListItem.property.module == Module.JOURNAL.name) {
                holder.dtstartDay.text = convertLongToDayString(iCal4ListItem.property.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(iCal4ListItem.property.dtstart)
                holder.dtstartYear.text = convertLongToYearString(iCal4ListItem.property.dtstart)

                if (iCal4ListItem.property.dtstartTimezone == "ALLDAY") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(iCal4ListItem.property.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else if(iCal4ListItem.property.module == Module.TODO.name) {

                holder.progressSlider.value = iCal4ListItem.property.percent?.toFloat()?:0F
                holder.progressCheckbox.isChecked = iCal4ListItem.property.percent == 100
                if(iCal4ListItem.relatedto.isNullOrEmpty() )     // TODO: also tasks with a subnote would be shown here, they should also be excluded!
                    holder.expandSubtasks.visibility = View.INVISIBLE
                else
                    holder.expandSubtasks.visibility = View.VISIBLE
                holder.subtasksLinearLayout.visibility = View.VISIBLE

                holder.progressPercent.text = "${iCal4ListItem.property.percent} %"

                if(iCal4ListItem.property.priority in 1..9) {           // show priority only if it was set and != 0 (no priority)
                    holder.priorityIcon.visibility = View.VISIBLE
                    holder.priority.visibility = View.VISIBLE
                } else  {
                    holder.priorityIcon.visibility = View.GONE
                    holder.priority.visibility = View.GONE
                }

                if (iCal4ListItem.property.due == null)
                    holder.due.visibility = View.GONE
                else {
                    holder.due.visibility = View.VISIBLE
                    var millisLeft = iCal4ListItem.property.due!! - System.currentTimeMillis()
                    if(iCal4ListItem.property.dueTimezone == "ALLDAY")
                        millisLeft = millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                    when {
                        millisLeft < 0L -> holder.due.text = context.getString(R.string.list_due_overdue)
                        millisLeft >= 0L && daysLeft == 0L && iCal4ListItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_today)
                        millisLeft >= 0L && daysLeft == 1L && iCal4ListItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_tomorrow)
                        millisLeft >= 0L && daysLeft <= 1L && iCal4ListItem.property.dueTimezone != "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_inXhours, hoursLeft)
                        millisLeft >= 0L && daysLeft >= 2L -> holder.due.text = context.getString(R.string.list_due_inXdays, daysLeft)
                        else -> holder.due.visibility = View.GONE      //should not be possible
                    }
                }

                if(iCal4ListItem.property.percent == 100)
                    holder.progressCheckbox.isActivated = true
            }


            if (iCal4ListItem.property.component == Component.VTODO.name && iCal4ListItem.property.status in StatusTodo.paramValues())
                holder.status.text  = context.getString(StatusTodo.getStringResourceByParam(iCal4ListItem.property.status)!!)
            else if (iCal4ListItem.property.component == Component.VJOURNAL.name && iCal4ListItem.property.status in StatusJournal.paramValues())
                holder.status.text  = context.getString(StatusJournal.getStringResourceByParam(iCal4ListItem.property.status)!!)
            else
                holder.status.text = iCal4ListItem.property.status       // if unsupported just show whatever is there

            if (iCal4ListItem.property.classification in Classification.paramValues())
                holder.classification.text = context.getString(Classification.getStringResource(iCal4ListItem.property.classification)!!)
            else
                holder.classification.text = iCal4ListItem.property.classification      // if unsupported just show whatever is there

            val priorityArray = context.resources.getStringArray(R.array.priority)
            if(iCal4ListItem.property.priority != null && iCal4ListItem.property.priority in 0..9)
                holder.priority.text = priorityArray[iCal4ListItem.property.priority!!]


            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment().setItem2show(iCal4ListItem.property.id))
            }

            var resetProgress = iCal4ListItem.property.percent ?: 0

            // take care to update the progress in the DB when the progress is changed
            if(iCal4ListItem.property.module == Module.TODO.name) {
                holder.progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
                    }

                    override fun onStopTrackingTouch(slider: Slider) {

                        model.updateProgress(iCal4ListItem.property.id, holder.progressSlider.value.toInt())

                        if (holder.progressSlider.value.toInt() != 100)
                            resetProgress = holder.progressSlider.value.toInt()
                    }
                })



                holder.progressCheckbox.setOnCheckedChangeListener { _, checked ->
                    if (checked)
                        holder.progressSlider.value = 100F
                     else
                        holder.progressSlider.value = resetProgress.toFloat()

                    model.updateProgress(iCal4ListItem.property.id, holder.progressSlider.value.toInt())

                }

                if(iCal4ListItem.property.component == Component.VTODO.name) {
                    val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id  } != null }
                    itemSubtasks?.forEach {
                        addSubtasksView(it, holder)
                    }
                }


                var toggleSubtasksExpanded = true

                holder.expandSubtasks.setOnClickListener {

                    if(!toggleSubtasksExpanded) {
                        val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id  } != null }
                        itemSubtasks?.forEach {
                            addSubtasksView(it, holder)
                        }
                        toggleSubtasksExpanded = true
                        holder.expandSubtasks.setImageResource(R.drawable.ic_collapse)
                    }
                    else {
                        holder.subtasksLinearLayout.removeAllViews()
                        toggleSubtasksExpanded = false
                        holder.expandSubtasks.setImageResource(R.drawable.ic_expand)
                    }
                }


                holder.progressLabel.setOnClickListener {

                    if(!toggleSubtasksExpanded) {
                        val itemSubtasks = allSubtasks.value?.filter { sub -> iCal4ListItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id  } != null }
                        itemSubtasks?.forEach {
                            addSubtasksView(it, holder)
                        }
                        toggleSubtasksExpanded = true
                        holder.expandSubtasks.setImageResource(R.drawable.ic_collapse)
                    }
                    else {
                        holder.subtasksLinearLayout.removeAllViews()
                        toggleSubtasksExpanded = false
                        holder.expandSubtasks.setImageResource(R.drawable.ic_expand)
                    }
                }

            }
        }

        //TODO: Check the impact of this setting!
        // Trying out if this solves the weird behaviour that sometimes fields in the cardview are just missing
        // Scrolling is actually not so smooth, but it looks like the weird behaviour is not there anymore...
        holder.setIsRecyclable(false)

    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView: MaterialCardView = itemView.findViewById(R.id.list_item_card_view)

        var summary: TextView = itemView.findViewById(R.id.list_item_summary)
        var description: TextView = itemView.findViewById(R.id.list_item_description)

        var categories: TextView = itemView.findViewById(R.id.list_item_categories)
        var status: TextView = itemView.findViewById(R.id.list_item_status)
        var statusIcon: ImageView = itemView.findViewById(R.id.list_item_status_icon)
        var classification: TextView = itemView.findViewById(R.id.list_item_classification)
        var classificationIcon: ImageView = itemView.findViewById(R.id.list_item_classification_icon)
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
        var subtasksLinearLayout: LinearLayout = itemView.findViewById(R.id.list_item_subtasks_linearlayout)

    }




    private fun addSubtasksView(subtask: ICal4List?, holder: VJournalItemHolder) {

        if (subtask == null)
            return

        var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

        val subtaskBinding = FragmentIcalListItemSubtaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        var subtaskSummary = subtask.summary
        //val subtaskCount = model.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
        /*if (subtaskCount != null)
            subtaskSummary += " (+${subtaskCount})" */
        if (subtask.subtasksCount > 0)
            subtaskSummary += " (+${subtask.subtasksCount})"

        subtaskBinding.listItemSubtaskTextview.text = subtaskSummary
        subtaskBinding.listItemSubtaskProgressSlider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskBinding.listItemSubtaskProgressPercent.text = if(subtask.percent?.toFloat() != null) "${subtask.percent!!} %" else "0 %"
        subtaskBinding.listItemSubtaskProgressCheckbox.isChecked = subtask.percent == 100

        // Instead of implementing here
        //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
        //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */


        subtaskBinding.listItemSubtaskProgressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (subtaskBinding.listItemSubtaskProgressSlider.value < 100)
                    resetProgress = subtask.percent?:0

                model.updateProgress(subtask.id, subtaskBinding.listItemSubtaskProgressSlider.value.toInt())
            }
        })


        subtaskBinding.listItemSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->

            if (checked)
                subtaskBinding.listItemSubtaskProgressSlider.value = 100F
            else
                subtaskBinding.listItemSubtaskProgressSlider.value = resetProgress.toFloat()

            model.updateProgress(subtask.id, subtaskBinding.listItemSubtaskProgressSlider.value.toInt())
        }

        subtaskBinding.root.setOnClickListener {
            holder.listItemCardView.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment().setItem2show(subtask.id))
        }

        holder.subtasksLinearLayout.addView(subtaskBinding.root)
    }
}


