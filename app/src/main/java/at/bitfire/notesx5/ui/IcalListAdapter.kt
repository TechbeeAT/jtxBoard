package at.bitfire.notesx5.ui

import android.content.Context
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
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.fragment_ical_list_item_subtask.view.*
import java.util.concurrent.TimeUnit

class IcalListAdapter(var context: Context, var model: IcalListViewModel):
        RecyclerView.Adapter<IcalListAdapter.VJournalItemHolder>() {

    lateinit var parent: ViewGroup
    var vJournalList: LiveData<List<ICalEntityWithCategory>> = model.vJournalList
    var allSubtasks: LiveData<List<ICalObject?>> = model.allSubtasks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VJournalItemHolder {

        this.parent = parent
        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.fragment_ical_list_item, parent, false)
        return VJournalItemHolder(itemHolder)

    }

    override fun getItemCount(): Int {

        //Log.println(Log.INFO, "getItemCount", vJournalListCount.value.toString())
        //Log.println(Log.INFO, "getItemCount", vJournalList.value?.size!!.toString())

        return if (vJournalList.value == null || vJournalList.value?.size == null)
            0
        else
            vJournalList.value?.size!!
    }

    override fun onBindViewHolder(holder: VJournalItemHolder, position: Int) {


        if (vJournalList.value?.size == 0)    // only continue if there are items in the list
            return

        val dtstartVisibility = if (model.searchComponent == "JOURNAL") View.VISIBLE else View.GONE
        val statusVisibility = if (model.searchComponent == "JOURNAL") View.VISIBLE else View.GONE
        val classificationVisibility = if (model.searchComponent  == "JOURNAL") View.VISIBLE else View.GONE
        val progressVisibility = if (model.searchComponent == "TODO") View.VISIBLE else View.GONE
        val priorityVisibility = if (model.searchComponent == "TODO") View.VISIBLE else View.GONE
        val dueVisibility = if (model.searchComponent  == "TODO") View.VISIBLE else View.GONE
        val subtasksVisibility = if (model.searchComponent == "TODO") View.VISIBLE else View.GONE


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


        val iCalItem = vJournalList.value?.get(position)

        if (iCalItem != null ) {

            holder.summary.text = iCalItem.property.summary
            if(iCalItem.property.description.isNullOrEmpty())
                holder.description.visibility = View.GONE
            else {
                holder.description.text = iCalItem.property.description
                holder.description.visibility = View.VISIBLE
            }

            if (iCalItem.category?.isNotEmpty() == true) {
                val categoriesList = mutableListOf<String>()
                iCalItem.category!!.forEach { categoriesList.add(it.text)  }
                holder.categories.text = categoriesList.joinToString(separator = ", ")
                holder.categories.visibility = View.VISIBLE
            } else {
                holder.categories.visibility = View.GONE
                //holder.categoriesIcon.visibility = View.GONE
            }

            if(iCalItem.property.component == "JOURNAL") {
                holder.dtstartDay.text = convertLongToDayString(iCalItem.property.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(iCalItem.property.dtstart)
                holder.dtstartYear.text = convertLongToYearString(iCalItem.property.dtstart)

                if (iCalItem.property.dtstartTimezone == "ALLDAY") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(iCalItem.property.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else if(iCalItem.property.component == "TODO") {

                holder.progressSlider.value = iCalItem.property.percent?.toFloat()?:0F
                holder.progressCheckbox.isChecked = iCalItem.property.percent == 100
                if(iCalItem.relatedto.isNullOrEmpty() )     // TODO: also tasks with a subnote would be shown here, they should also be excluded!
                    holder.expandSubtasks.visibility = View.INVISIBLE
                else
                    holder.expandSubtasks.visibility = View.VISIBLE
                holder.subtasksLinearLayout.visibility = View.VISIBLE

                holder.progressPercent.text = context.getString(R.string.list_progress_percent, iCalItem.property.percent?.toString()
                        ?: "0")
                if(iCalItem.property.priority in 1..9) {           // show priority only if it was set and != 0 (no priority)
                    holder.priorityIcon.visibility = View.VISIBLE
                    holder.priority.visibility = View.VISIBLE
                } else  {
                    holder.priorityIcon.visibility = View.GONE
                    holder.priority.visibility = View.GONE
                }

                if (iCalItem.property.due == null)
                    holder.due.visibility = View.GONE
                else {
                    holder.due.visibility = View.VISIBLE
                    var millisLeft = iCalItem.property.due!! - System.currentTimeMillis()
                    if(iCalItem.property.dueTimezone == "ALLDAY")
                        millisLeft = millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                    when {
                        millisLeft < 0L -> holder.due.text = context.getString(R.string.list_due_overdue)
                        millisLeft >= 0L && daysLeft == 0L && iCalItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_today)
                        millisLeft >= 0L && daysLeft == 1L && iCalItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_tomorrow)
                        millisLeft >= 0L && daysLeft <= 1L && iCalItem.property.dueTimezone != "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_inXhours, hoursLeft)
                        millisLeft >= 0L && daysLeft >= 2L -> holder.due.text = context.getString(R.string.list_due_inXdays, daysLeft)
                        else -> holder.due.visibility = View.GONE      //should not be possible
                    }
                }

                if(iCalItem.property.percent == 100)
                    holder.progressCheckbox.isActivated = true
            }


            if (iCalItem.property.component == Component.TODO.name && iCalItem.property.status in StatusTodo.paramValues())
                holder.status.text  = context.getString(StatusTodo.getStringResourceByParam(iCalItem.property.status)!!)
            else if ((iCalItem.property.component == Component.JOURNAL.name || iCalItem.property.component == Component.NOTE.name) && iCalItem.property.status in StatusJournal.paramValues())
                holder.status.text  = context.getString(StatusJournal.getStringResourceByParam(iCalItem.property.status)!!)
            else
                holder.status.text = iCalItem.property.status       // if unsupported just show whatever is there

            if (iCalItem.property.classification in Classification.paramValues())
                holder.classification.text = context.getString(Classification.getStringResourceByParam(iCalItem.property.classification)!!)
            else
                holder.classification.text = iCalItem.property.classification      // if unsupported just show whatever is there

            val priorityArray = context.resources.getStringArray(R.array.priority)
            if(iCalItem.property.priority != null && iCalItem.property.priority in 0..9)
                holder.priority.text = priorityArray[iCalItem.property.priority!!]


            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment().setItem2show(iCalItem.property.id))
            }

            var resetProgress = iCalItem.property.percent ?: 0

            // take care to update the progress in the DB when the progress is changed
            if(iCalItem.property.component == "TODO") {
                holder.progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
                    }

                    override fun onStopTrackingTouch(slider: Slider) {

                        model.updateProgress(iCalItem.property, holder.progressSlider.value.toInt())

                        if (holder.progressSlider.value.toInt() != 100)
                            resetProgress = holder.progressSlider.value.toInt()
                    }
                })



                holder.progressCheckbox.setOnCheckedChangeListener { button, checked ->
                    if (checked)
                        holder.progressSlider.value = 100F
                     else
                        holder.progressSlider.value = resetProgress.toFloat()

                    model.updateProgress(iCalItem.property, holder.progressSlider.value.toInt())

                }


                var toggleSubtasksExpanded = false

                holder.expandSubtasks.setOnClickListener {

                    if(!toggleSubtasksExpanded) {
                        val itemSubtasks = allSubtasks.value?.filter { sub -> iCalItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id  } != null }
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
                        val itemSubtasks = allSubtasks.value?.filter { sub -> iCalItem.relatedto?.find { rel -> rel.linkedICalObjectId == sub?.id  } != null }
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
        //holder.setIsRecyclable(false)

    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView = itemView.findViewById<MaterialCardView>(R.id.list_item_card_view)

        var summary = itemView.findViewById<TextView>(R.id.list_item_summary)
        var description = itemView.findViewById<TextView>(R.id.list_item_description)

        var categories: TextView = itemView.findViewById<TextView>(R.id.list_item_categories)
        var status: TextView = itemView.findViewById<TextView>(R.id.list_item_status)
        var statusIcon: ImageView = itemView.findViewById<ImageView>(R.id.list_item_status_icon)
        var classification: TextView = itemView.findViewById<TextView>(R.id.list_item_classification)
        var classificationIcon: ImageView = itemView.findViewById<ImageView>(R.id.list_item_classification_icon)
        var priority: TextView = itemView.findViewById<TextView>(R.id.list_item_priority)
        var priorityIcon: ImageView = itemView.findViewById<ImageView>(R.id.list_item_priority_icon)

        var progressLabel: TextView = itemView.findViewById<TextView>(R.id.list_item_progress_label)
        var progressSlider: Slider = itemView.findViewById<Slider>(R.id.list_item_progress_slider)
        var progressPercent: TextView = itemView.findViewById<TextView>(R.id.list_item_progress_percent)
        var progressCheckbox: CheckBox = itemView.findViewById<CheckBox>(R.id.list_item_progress_checkbox)

        var due: TextView = itemView.findViewById<TextView>(R.id.list_item_due)

        var dtstartDay: TextView = itemView.findViewById<TextView>(R.id.list_item_dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById<TextView>(R.id.list_item_dtstart_month)
        var dtstartYear: TextView = itemView.findViewById<TextView>(R.id.list_item_dtstart_year)
        var dtstartTime: TextView = itemView.findViewById<TextView>(R.id.list_item_dtstart_time)

        var expandSubtasks: ImageView = itemView.findViewById<ImageView>(R.id.list_item_expand)
        var subtasksLinearLayout: LinearLayout = itemView.findViewById<LinearLayout>(R.id.list_item_subtasks_linearlayout)

    }





    private fun addSubtasksView(subtask: ICalObject?, holder: VJournalItemHolder) {

        if (subtask == null)
            return

        var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

        val subtaskView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_ical_list_item_subtask, parent, false)

        var subtaskSummary = subtask.summary
        val subtaskCount = model.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
        if (subtaskCount != null)
            subtaskSummary += " (+${subtaskCount})"
        subtaskView.list_item_subtask_textview.text = subtaskSummary
        subtaskView.list_item_subtask_progress_slider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskView.list_item_subtask_progress_percent.text = if(subtask.percent?.toFloat() != null) subtask.percent!!.toString() else "0"
        subtaskView.list_item_subtask_progress_checkbox.isChecked = subtask.percent == 100

        // Instead of implementing here
        //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
        //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */


        subtaskView.list_item_subtask_progress_slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (subtaskView.list_item_subtask_progress_slider.value < 100)
                    resetProgress = subtaskView.list_item_subtask_progress_slider.value.toInt()
                else
                    subtaskView.list_item_subtask_progress_checkbox.isChecked = true

                model.updateProgress(subtask, subtaskView.list_item_subtask_progress_slider.value.toInt())
            }
        })


        subtaskView.list_item_subtask_progress_checkbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                subtaskView.list_item_subtask_progress_percent.text = 100.toString()
                subtaskView.list_item_subtask_progress_slider.value = 100F
                subtaskView.list_item_subtask_progress_checkbox.isChecked = true
            } else {
                subtaskView.list_item_subtask_progress_percent.text = resetProgress.toString()
                subtaskView.list_item_subtask_progress_slider.value = resetProgress.toFloat()
                subtaskView.list_item_subtask_progress_checkbox.isChecked = false
            }
            model.updateProgress(subtask, subtaskView.list_item_subtask_progress_slider.value.toInt())
        }

        subtaskView.setOnClickListener { view ->
            holder.listItemCardView.findNavController().navigate(
                    IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment().setItem2show(subtask.id))
        }

        holder.subtasksLinearLayout.addView(subtaskView)
    }
}


