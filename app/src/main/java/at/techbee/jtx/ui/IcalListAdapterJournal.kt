/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


/*
class IcalListAdapterJournal(var context: Context, var model: IcalListViewModel) :
    RecyclerView.Adapter<IcalListAdapterJournal.JournalItemHolder>() {


    private var markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .build()

    override fun onBindViewHolder(holder: JournalItemHolder, position: Int) {


            // strikethrough the summary if the item is cancelled
            if(iCal4ListItem.property.status == StatusJournal.CANCELLED.name || iCal4ListItem.property.status == StatusTodo.CANCELLED.name) {
                holder.summary.paintFlags = holder.summary.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                holder.summary.paintFlags = holder.summary.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }




            //set the timezone (if applicable)
            if (iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY || iCal4ListItem.property.dtstartTimezone.isNullOrEmpty() || TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT) == null) {
                holder.dtstartTimeZone.visibility = View.GONE
            } else {
                holder.dtstartTimeZone.text = TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT)
                holder.dtstartTimeZone.visibility = View.VISIBLE
            }


        }

    }
}

 */


