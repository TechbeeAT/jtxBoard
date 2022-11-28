package at.techbee.jtx.widgets


import at.techbee.jtx.database.views.ICal4List


@kotlinx.serialization.Serializable
data class ICal4ListWidget(

    var id: Long,
    var module: String,
    var summary: String?,
    var description: String?,

    var dtstart: Long?,
    var dtstartTimezone: String?,
     var percent: Int?,

    var due: Long?,
    var dueTimezone: String?,
    var uid: String?,

    var isChildOfJournal: Boolean,
    var isChildOfNote: Boolean,
    var isChildOfTodo: Boolean,

    var vtodoUidOfParent: String?,
    var vjournalUidOfParent: String?,
    var isReadOnly: Boolean,
) {

    companion object {

        fun fromICal4List(iCal4List: ICal4List): ICal4ListWidget {
            return ICal4ListWidget(
                id = iCal4List.id,
                module = iCal4List.module,
                summary = iCal4List.summary?.let { if(it.length > 160) it.substring(0,160) else it },
                description = iCal4List.description?.let { if(it.length > 160) it.substring(0,160) else it },
                dtstart = iCal4List.dtstart,
                dtstartTimezone = iCal4List.dtstartTimezone,
                percent = iCal4List.percent,
                due = iCal4List.due,
                dueTimezone = iCal4List.dueTimezone,
                uid = iCal4List.uid,
                isChildOfJournal = iCal4List.isChildOfJournal,
                isChildOfNote = iCal4List.isChildOfNote,
                isChildOfTodo = iCal4List.isChildOfTodo,
                vtodoUidOfParent = iCal4List.vtodoUidOfParent,
                vjournalUidOfParent = iCal4List.vjournalUidOfParent,
                isReadOnly = iCal4List.isReadOnly
            )
        }
    }
}