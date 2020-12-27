package com.example.android.vjournalcalendar.ui

import android.content.Context
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.android.vjournalcalendar.*
import com.example.android.vjournalcalendar.database.vJournalItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.hours

class VJournalListAdapter(var context: Context, var vJournalList: LiveData<List<vJournalItem>>):
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

        var vJournalItem = vJournalList.value?.get(position)

        if (vJournalItem != null) {

            holder.summary.text = vJournalItem.summary
            holder.description.text = vJournalItem.description

            if (vJournalItem.categories != "") {
                holder.categories.text = vJournalItem.categories
            } else {
                holder.categories.visibility = View.GONE
                //holder.categoriesIcon.visibility = View.GONE
            }

            if(vJournalItem.component == "JOURNAL") {
                holder.dtstartDay.text = convertLongToDayString(vJournalItem.dtstart)
                holder.dtstartMonth.text = convertLongToMonthString(vJournalItem.dtstart)
                holder.dtstartYear.text = convertLongToYearString(vJournalItem.dtstart)
                holder.dtstartDay.visibility = View.VISIBLE
                holder.dtstartMonth.visibility = View.VISIBLE
                holder.dtstartYear.visibility = View.VISIBLE

                val minute_formatter = SimpleDateFormat("mm")
                val hour_formatter = SimpleDateFormat("HH")
                if (minute_formatter.format(Date(vJournalItem.dtstart)).toString() == "00" && hour_formatter.format(Date(vJournalItem.dtstart)).toString() == "00") {
                    holder.dtstartTime.visibility = View.GONE
                } else {
                    holder.dtstartTime.text = convertLongToTimeString(vJournalItem.dtstart)
                    holder.dtstartTime.visibility = View.VISIBLE
                }

            } else {
                holder.dtstartDay.visibility = View.GONE
                holder.dtstartMonth.visibility = View.GONE
                holder.dtstartYear.visibility = View.GONE
                holder.dtstartTime.visibility = View.GONE
            }

            val statusArray = context.resources.getStringArray(R.array.vjournal_status)
            holder.status.text = statusArray[vJournalItem.status]

            val classificationArray = context.resources.getStringArray(R.array.vjournal_classification)
            holder.classification.text = classificationArray[vJournalItem.classification]



            // turn to item view when the card is clicked
            holder.listItemCardView.setOnClickListener {
                it.findNavController().navigate(
                        VJournalListFragmentDirections.actionVjournalListFragmentListToVJournalItemFragment().setItem2show(vJournalItem.id))
            }

        }
    }




    class VJournalItemHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var listItemCardView = itemView.findViewById<CardView>(R.id.list_item_card_view)

        var summary = itemView.findViewById<TextView>(R.id.summary)
        var description = itemView.findViewById<TextView>(R.id.description)


        var categories = itemView.findViewById<TextView>(R.id.categories)
        //var categoriesIcon = itemView.findViewById<ImageView>(R.id.categories_icon)
        var status = itemView.findViewById<TextView>(R.id.status)
        var classification = itemView.findViewById<TextView>(R.id.classification)


        var dtstartDay = itemView.findViewById<TextView>(R.id.dtstart_day)
        var dtstartMonth = itemView.findViewById<TextView>(R.id.dtstart_month)
        var dtstartYear = itemView.findViewById<TextView>(R.id.dtstart_year)
        var dtstartTime = itemView.findViewById<TextView>(R.id.dtstart_time)


    }

}