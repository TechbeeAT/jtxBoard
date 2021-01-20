package at.bitfire.notesx5.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import java.util.*

class VJournalListAdapter(var context: Context, var vJournalList: LiveData<List<ICalEntityWithCategory>>):
        RecyclerView.Adapter<VJournalListAdapter.VJournalItemHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VJournalItemHolder {

        val itemHolder = LayoutInflater.from(parent.context).inflate(R.layout.fragment_vjournal_list_item, parent, false)
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

            holder.summary.text = vJournalItem.vJournal.summary
            if(vJournalItem.vJournal.description.isNullOrEmpty())
                holder.description.visibility = View.GONE
            else
               holder.description.text = vJournalItem.vJournal.description

            if (vJournalItem.category?.isNotEmpty() == true) {
                val categoriesList = mutableListOf<String>()
                vJournalItem.category!!.forEach { categoriesList.add(it.text)  }
                holder.categories.text = categoriesList.joinToString(separator=", ")
            } else {
                holder.categories.visibility = View.GONE
                //holder.categoriesIcon.visibility = View.GONE
            }

            if(vJournalItem.vJournal.component == "JOURNAL") {
                holder.dtstartDay.text = convertLongToDayString(vJournalItem.vJournal.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(vJournalItem.vJournal.dtstart)
                holder.dtstartYear.text = convertLongToYearString(vJournalItem.vJournal.dtstart)
                holder.dtstartDay.visibility = View.VISIBLE
                holder.dtstartMonth.visibility = View.VISIBLE
                holder.dtstartYear.visibility = View.VISIBLE
                holder.status.visibility = View.VISIBLE
                holder.statusIcon.visibility = View.VISIBLE
                holder.classification.visibility = View.VISIBLE
                holder.classificationIcon.visibility = View.VISIBLE
                holder.progressLabel.visibility = View.GONE
                holder.progressSlider.visibility = View.GONE
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE

                if (vJournalItem.vJournal.dtstartTimezone == "ALLDAY") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(vJournalItem.vJournal.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else if(vJournalItem.vJournal.component == "NOTE") {
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
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE

            } else if(vJournalItem.vJournal.component == "TODO") {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
                holder.status.visibility = View.VISIBLE
                holder.statusIcon.visibility = View.VISIBLE
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
                holder.progressLabel.visibility = View.VISIBLE
                holder.progressSlider.value = vJournalItem.vJournal.percent?.toFloat()?:0F
                holder.progressSlider.visibility = View.VISIBLE
                holder.priorityIcon.visibility = View.VISIBLE
                holder.priority.visibility = View.VISIBLE
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
                holder.priorityIcon.visibility = View.GONE
                holder.priority.visibility = View.GONE
            }

            val statusArray = if (vJournalItem.vJournal.component == "TODO")
                context.resources.getStringArray(R.array.vtodo_status)
            else
                context.resources.getStringArray(R.array.vjournal_status)

            if (vJournalItem.vJournal.status in 0..3 || (vJournalItem.vJournal.component == "TODO" && vJournalItem.vJournal.status in 0..3))
                holder.status.text = statusArray[vJournalItem.vJournal.status]

            val classificationArray = context.resources.getStringArray(R.array.ical_classification)
            if(vJournalItem.vJournal.classification in 0..2)
                holder.classification.text = classificationArray[vJournalItem.vJournal.classification]

            val priorityArray = context.resources.getStringArray(R.array.priority)
            if(vJournalItem.vJournal.priority != null && vJournalItem.vJournal.priority in 0..9)
                holder.priority.text = priorityArray[vJournalItem.vJournal.priority!!]


            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemFragment().setItem2show(vJournalItem.vJournal.id))
            }

        }
    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView = itemView.findViewById<MaterialCardView>(R.id.list_item_card_view)

        var summary = itemView.findViewById<TextView>(R.id.summary)
        var description = itemView.findViewById<TextView>(R.id.description)


        var categories: TextView = itemView.findViewById<TextView>(R.id.categories)
        //var categoriesIcon = itemView.findViewById<ImageView>(R.id.categories_icon)
        var status: TextView = itemView.findViewById<TextView>(R.id.status)
        var statusIcon: ImageView = itemView.findViewById<ImageView>(R.id.status_icon)
        var classification: TextView = itemView.findViewById<TextView>(R.id.classification)
        var classificationIcon: ImageView = itemView.findViewById<ImageView>(R.id.classification_icon)
        var priority: TextView = itemView.findViewById<TextView>(R.id.priority)
        var priorityIcon: ImageView = itemView.findViewById<ImageView>(R.id.priority_icon)

        var progressLabel: TextView = itemView.findViewById<TextView>(R.id.progress_label)
        var progressSlider: Slider = itemView.findViewById<Slider>(R.id.progress_slider)





        var dtstartDay: TextView = itemView.findViewById<TextView>(R.id.dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById<TextView>(R.id.dtstart_month)
        var dtstartYear: TextView = itemView.findViewById<TextView>(R.id.dtstart_year)
        var dtstartTime: TextView = itemView.findViewById<TextView>(R.id.dtstart_time)


    }

}