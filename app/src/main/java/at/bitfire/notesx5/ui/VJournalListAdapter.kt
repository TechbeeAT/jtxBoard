package at.bitfire.notesx5.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.relations.ICalEntityWithCategory
import java.text.SimpleDateFormat
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

                val minute_formatter = SimpleDateFormat("mm")
                val hour_formatter = SimpleDateFormat("HH")
                if (minute_formatter.format(Date(vJournalItem.vJournal.dtstart)).toString() == "00" && hour_formatter.format(Date(vJournalItem.vJournal.dtstart)).toString() == "00") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(vJournalItem.vJournal.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
                holder.status.visibility = View.GONE
                holder.statusIcon.visibility = View.GONE
                holder.classification.visibility = View.GONE
                holder.classificationIcon.visibility = View.GONE
            }

            val statusArray = context.resources.getStringArray(R.array.ical_status)
            holder.status.text = statusArray[vJournalItem.vJournal.status]

            val classificationArray = context.resources.getStringArray(R.array.ical_classification)
            holder.classification.text = classificationArray[vJournalItem.vJournal.classification]


            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemFragment().setItem2show(vJournalItem.vJournal.id))
            }

        }
    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView = itemView.findViewById<CardView>(R.id.list_item_card_view)

        var summary = itemView.findViewById<TextView>(R.id.summary)
        var description = itemView.findViewById<TextView>(R.id.description)


        var categories: TextView = itemView.findViewById<TextView>(R.id.categories)
        //var categoriesIcon = itemView.findViewById<ImageView>(R.id.categories_icon)
        var status: TextView = itemView.findViewById<TextView>(R.id.status)
        var classification: TextView = itemView.findViewById<TextView>(R.id.classification)
        var statusIcon: ImageView = itemView.findViewById<ImageView>(R.id.status_icon)
        var classificationIcon: ImageView = itemView.findViewById<ImageView>(R.id.classification_icon)



        var dtstartDay: TextView = itemView.findViewById<TextView>(R.id.dtstart_day)
        var dtstartMonth: TextView = itemView.findViewById<TextView>(R.id.dtstart_month)
        var dtstartYear: TextView = itemView.findViewById<TextView>(R.id.dtstart_year)
        var dtstartTime: TextView = itemView.findViewById<TextView>(R.id.dtstart_time)


    }

}