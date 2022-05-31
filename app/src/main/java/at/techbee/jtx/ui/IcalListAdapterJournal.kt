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



            //set the timezone (if applicable)
            if (iCal4ListItem.property.dtstartTimezone == ICalObject.TZ_ALLDAY || iCal4ListItem.property.dtstartTimezone.isNullOrEmpty() || TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT) == null) {
                holder.dtstartTimeZone.visibility = View.GONE
            } else {
                holder.dtstartTimeZone.text = TimeZone.getTimeZone(iCal4ListItem.property.dtstartTimezone).getDisplayName(true, TimeZone.SHORT)
                holder.dtstartTimeZone.visibility = View.VISIBLE
            }




            if (model.searchModule == Module.TODO.name && settingShowSubtasks && settingShowProgressMaintasks && iCal4ListItem.property.numSubtasks > 0)
                holder.expandSubtasks.visibility = View.VISIBLE
            else
                holder.expandSubtasks.visibility = View.INVISIBLE



            var toggleSubtasksExpanded = true

            holder.expandSubtasks.setOnClickListener {

                if (!toggleSubtasksExpanded) {
                    IcalListAdapterHelper.addSubtasksView(model, itemSubtasks.distinct(), holder.subtasksLinearLayout, context, parent)
                    toggleSubtasksExpanded = true
                    holder.expandSubtasks.setImageResource(R.drawable.ic_collapse)
                } else {
                    holder.subtasksLinearLayout.removeAllViews()
                    toggleSubtasksExpanded = false
                    holder.expandSubtasks.setImageResource(R.drawable.ic_expand)
                }
            }


}




        }

    }
}

 */


