package at.bitfire.notesx5.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class IcalListAdapter(var context: Context, var vJournalList: LiveData<List<ICalEntityWithCategory>>):
        RecyclerView.Adapter<IcalListAdapter.VJournalItemHolder>() {

    var dataSource = ICalDatabase.getInstance(context.applicationContext).iCalDatabaseDao

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VJournalItemHolder {

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

        val vJournalItem = vJournalList.value?.get(position)

        if (vJournalItem != null ) {

            holder.summary.text = vJournalItem.property.summary
            if(vJournalItem.property.description.isNullOrEmpty())
                holder.description.visibility = View.GONE
            else
               holder.description.text = vJournalItem.property.description

            if (vJournalItem.category?.isNotEmpty() == true) {
                val categoriesList = mutableListOf<String>()
                vJournalItem.category!!.forEach { categoriesList.add(it.text)  }
                holder.categories.text = categoriesList.joinToString(separator=", ")
            } else {
                holder.categories.visibility = View.GONE
                //holder.categoriesIcon.visibility = View.GONE
            }

            if(vJournalItem.property.component == "JOURNAL") {
                holder.dtstartDay.text = convertLongToDayString(vJournalItem.property.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(vJournalItem.property.dtstart)
                holder.dtstartYear.text = convertLongToYearString(vJournalItem.property.dtstart)
                holder.dtstartDay.visibility = View.VISIBLE
                holder.dtstartMonth.visibility = View.VISIBLE
                holder.dtstartYear.visibility = View.VISIBLE
                holder.status.visibility = View.VISIBLE
                holder.statusIcon.visibility = View.VISIBLE
                holder.classification.visibility = View.VISIBLE
                holder.classificationIcon.visibility = View.VISIBLE
                holder.progressLabel.visibility = View.GONE
                holder.progressSlider.visibility = View.GONE
                holder.progressPercent.visibility = View.GONE
                holder.progressCheckbox.visibility = View.GONE
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE
                holder.due.visibility = View.GONE

                if (vJournalItem.property.dtstartTimezone == "ALLDAY") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(vJournalItem.property.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else if(vJournalItem.property.component == "NOTE") {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
                holder.status.visibility = View.GONE
                holder.statusIcon.visibility = View.GONE
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
                holder.progressLabel.visibility = View.GONE
                holder.progressSlider.visibility = View.GONE
                holder.progressPercent.visibility = View.GONE
                holder.progressCheckbox.visibility = View.GONE
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE
                holder.due.visibility = View.GONE


            } else if(vJournalItem.property.component == "TODO") {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
                holder.status.visibility = View.GONE
                holder.statusIcon.visibility = View.GONE
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
                holder.progressLabel.visibility = View.VISIBLE
                holder.progressSlider.value = vJournalItem.property.percent?.toFloat()?:0F
                holder.progressSlider.visibility = View.VISIBLE
                holder.progressCheckbox.visibility = View.VISIBLE
                holder.progressCheckbox.isChecked = vJournalItem.property.percent == 100
                holder.progressPercent.visibility = View.VISIBLE
                holder.progressPercent.text = context.getString(R.string.list_progress_percent, vJournalItem.property.percent?.toString() ?: "0")
                if(vJournalItem.property.priority in 1..9) {           // show priority only if it was set and != 0 (no priority)
                    holder.priorityIcon.visibility = View.VISIBLE
                    holder.priority.visibility = View.VISIBLE
                } else  {
                    holder.priorityIcon.visibility = View.GONE
                    holder.priority.visibility = View.GONE
                }

                if (vJournalItem.property.due == null)
                    holder.due.visibility = View.GONE
                else {
                    holder.due.visibility = View.VISIBLE
                    var millisLeft = vJournalItem.property.due!! - System.currentTimeMillis()
                    if(vJournalItem.property.dueTimezone == "ALLDAY")
                        millisLeft = millisLeft + TimeUnit.DAYS.toMillis(1) - 1        // if it's due on the same day, then add 1 day minus 1 millisecond to consider the end of the day
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(millisLeft)     // cannot be negative, would stop at 0!
                    val hoursLeft = TimeUnit.MILLISECONDS.toHours(millisLeft)     // cannot be negative, would stop at 0!

                    when {
                        millisLeft < 0L -> holder.due.text = context.getString(R.string.list_due_overdue)
                        millisLeft >= 0L && daysLeft == 0L && vJournalItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_today)
                        millisLeft >= 0L && daysLeft == 1L && vJournalItem.property.dueTimezone == "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_tomorrow)
                        millisLeft >= 0L && daysLeft <= 1L && vJournalItem.property.dueTimezone != "ALLDAY" -> holder.due.text = context.getString(R.string.list_due_inXhours, hoursLeft)
                        millisLeft >= 0L && daysLeft >= 2L -> holder.due.text = context.getString(R.string.list_due_inXdays, daysLeft)
                        else -> holder.due.visibility = View.GONE      //should not be possible
                    }
                }



                if(vJournalItem.property.percent == 100)
                    holder.progressCheckbox.isActivated = true
                    
            } else {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
                holder.status.visibility = View.GONE
                holder.statusIcon.visibility = View.GONE
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
                holder.progressLabel.visibility = View.GONE
                holder.progressSlider.visibility = View.GONE
                holder.progressPercent.visibility = View.GONE
                holder.progressCheckbox.visibility = View.GONE
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE
                holder.due.visibility = View.GONE
            }

            val statusArray = if (vJournalItem.property.component == "TODO")
                context.resources.getStringArray(R.array.vtodo_status)
            else
                context.resources.getStringArray(R.array.vjournal_status)

            if (vJournalItem.property.status in 0..3 || (vJournalItem.property.component == "TODO" && vJournalItem.property.status in 0..3))
                holder.status.text = statusArray[vJournalItem.property.status]

            val classificationArray = context.resources.getStringArray(R.array.ical_classification)
            if(vJournalItem.property.classification in 0..2)
                holder.classification.text = classificationArray[vJournalItem.property.classification]

            val priorityArray = context.resources.getStringArray(R.array.priority)
            if(vJournalItem.property.priority != null && vJournalItem.property.priority in 0..9)
                holder.priority.text = priorityArray[vJournalItem.property.priority!!]


            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment().setItem2show(vJournalItem.property.id))
            }

            var resetProgress = vJournalItem.property.percent ?: 0

            // take care to update the progress in the DB when the progress is changed
            if(vJournalItem.property.component == "TODO") {
                holder.progressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */  }

                    override fun onStopTrackingTouch(slider: Slider) {

                        updateProgress(holder, vJournalItem.property, holder.progressSlider.value.toInt())

                        if (holder.progressSlider.value.toInt() != 100)
                            resetProgress = holder.progressSlider.value.toInt()
                    }
                })


                holder.progressCheckbox.setOnCheckedChangeListener { button, checked ->
                    if (checked) {
                        updateProgress(holder, vJournalItem.property, 100)
                        holder.progressSlider.value = 100F
                    } else {
                        updateProgress(holder, vJournalItem.property, resetProgress)
                    holder.progressSlider.value = resetProgress.toFloat()
                     }


                }
            }


        }
    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView = itemView.findViewById<MaterialCardView>(R.id.list_item_card_view)

        var summary = itemView.findViewById<TextView>(R.id.view_item_summary)
        var description = itemView.findViewById<TextView>(R.id.view_item_description)


        var categories: TextView = itemView.findViewById<TextView>(R.id.categories)
        //var categoriesIcon = itemView.findViewById<ImageView>(R.id.categories_icon)
        var status: TextView = itemView.findViewById<TextView>(R.id.view_item_status)
        var statusIcon: ImageView = itemView.findViewById<ImageView>(R.id.view_item_status_icon)
        var classification: TextView = itemView.findViewById<TextView>(R.id.classification)
        var classificationIcon: ImageView = itemView.findViewById<ImageView>(R.id.classification_icon)
        var priority: TextView = itemView.findViewById<TextView>(R.id.view_item_priority)
        var priorityIcon: ImageView = itemView.findViewById<ImageView>(R.id.view_item_priority_icon)

        var progressLabel: TextView = itemView.findViewById<TextView>(R.id.item_view_progress_label)
        var progressSlider: Slider = itemView.findViewById<Slider>(R.id.item_view_progress_slider)
        var progressPercent: TextView = itemView.findViewById<TextView>(R.id.item_view_progress_percent)
        var progressCheckbox: CheckBox = itemView.findViewById<CheckBox>(R.id.item_view_progress_checkbox)

        var due: TextView = itemView.findViewById<TextView>(R.id.view_item_due)


        var dtstartDay: TextView = itemView.findViewById<TextView>(R.id.view_item_dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById<TextView>(R.id.view_item_dtstart_month)
        var dtstartYear: TextView = itemView.findViewById<TextView>(R.id.view_item_dtstart_year)
        var dtstartTime: TextView = itemView.findViewById<TextView>(R.id.view_item_dtstart_time)

    }



    private fun updateProgress(holder: VJournalItemHolder, item: ICalObject, progress: Int) {


        val item2update = item
        item2update.percent = progress
        item2update.lastModified = System.currentTimeMillis()
        item2update.sequence++

        when (item2update.percent) {
            100 -> item2update.status = 2
            in 1..99 -> item2update.status = 1
            0 -> item2update.status = 0
        }

        GlobalScope.launch {
            dataSource.update(item2update)
        }


        //update UI
        holder.progressPercent.text = context.getString(R.string.list_progress_percent, progress.toString())

        val statusArray = context.resources.getStringArray(R.array.vtodo_status)
        holder.status.text = statusArray[item2update.status]

        holder.progressCheckbox.isChecked = progress == 100    // isChecked when Progress = 100

    }

}