package at.bitfire.notesx5.ui

import android.app.Application
import androidx.lifecycle.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Relatedto
import at.bitfire.notesx5.database.properties.Reltype
import at.bitfire.notesx5.database.relations.ICalEntity
import kotlinx.android.synthetic.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*


class IcalViewViewModel(private val icalItemId: Long,
                        val database: ICalDatabaseDao,
                        application: Application) : AndroidViewModel(application) {

    lateinit var icalEntity: LiveData<ICalEntity?>
    lateinit var categories: LiveData<List<Category>>
    lateinit var attendees: LiveData<List<Attendee>>
    lateinit var relatedNotes: LiveData<List<ICalObject?>>
    lateinit var relatedSubtasks: LiveData<List<ICalObject?>>

    lateinit var dtstartFormatted: LiveData<String>
    lateinit var createdFormatted: LiveData<String>
    lateinit var lastModifiedFormatted: LiveData<String>

    lateinit var dateVisible: LiveData<Boolean>
    lateinit var timeVisible: LiveData<Boolean>
    lateinit var urlVisible: LiveData<Boolean>
    lateinit var attendeesVisible: LiveData<Boolean>
    lateinit var organizerVisible: LiveData<Boolean>
    lateinit var contactVisible: LiveData<Boolean>
    lateinit var commentsVisible: LiveData<Boolean>
    lateinit var relatedtoVisible: LiveData<Boolean>
    lateinit var progressVisible: LiveData<Boolean>
    lateinit var priorityVisible: LiveData<Boolean>
    lateinit var subtasksVisible: LiveData<Boolean>

    lateinit var subtasksCountList: LiveData<List<SubtaskCount>>


    var editingClicked: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }


    init {

        viewModelScope.launch {

            // insert a new value to initialize the vJournalItem or load the existing one from the DB
            icalEntity = if (icalItemId == 0L)
                MutableLiveData<ICalEntity?>().apply {
                    postValue(ICalEntity(ICalObject(), null, null, null, null, null))
                }
            else
                database.get(icalItemId)

            subtasksCountList = database.getSubtasksCount()


            categories = Transformations.map(icalEntity) {
                it?.category
            }

            attendees = Transformations.map(icalEntity) {
                it?.attendee
            }


            relatedNotes = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedNotes(parentId) }
            }

            relatedSubtasks = Transformations.switchMap(icalEntity) {
                it?.property?.id?.let { parentId -> database.getRelatedTodos(parentId) }
            }


            dateVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == "JOURNAL"           // true if component == JOURNAL
            }

            timeVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.component == "JOURNAL" && item.property.dtstartTimezone != "ALLDAY"           // true if component == JOURNAL and it is not an All Day Event
            }

            dtstartFormatted = Transformations.map(icalEntity) { item ->
                if (item!!.property.dtstart != null) {
                    val formattedDate = DateFormat.getDateInstance(DateFormat.LONG).format(Date(item.property.dtstart!!))
                    val formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(item.property.dtstart!!))
                    return@map "$formattedDate $formattedTime"
                } else
                    return@map ""

            }

            createdFormatted = Transformations.map(icalEntity) { item ->
                item!!.property.let { Date(it.created).toString() }
            }

            lastModifiedFormatted = Transformations.map(icalEntity) { item ->
                item!!.property.let { Date(it.lastModified).toString() }
            }

            urlVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.url.isNullOrBlank()      // true if url is NOT null or empty
            }
            attendeesVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.attendee.isNullOrEmpty()      // true if attendees is NOT null or empty
            }
            organizerVisible = Transformations.map(icalEntity) { item ->
                return@map item?.organizer != null      // true if organizer is NOT null or empty
            }
            contactVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.property?.contact.isNullOrBlank()      // true if contact is NOT null or empty
            }
            relatedtoVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.relatedto.isNullOrEmpty()      // true if relatedto is NOT null or empty
            }
            subtasksVisible = Transformations.map(relatedSubtasks) { subtasks ->
                return@map subtasks?.isNotEmpty()      // true if relatedto is NOT null or empty
            }
            commentsVisible = Transformations.map(icalEntity) { item ->
                return@map !item?.comment.isNullOrEmpty()      // true if relatedto is NOT null or empty
            }
            progressVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.percent != null && item.property.component == "TODO"     // true if percent (progress) is NOT null
            }
            priorityVisible = Transformations.map(icalEntity) { item ->
                return@map item?.property?.priority != null      // true if priority is NOT null
            }
        }

        viewModelScope.launch {
            subtasksCountList = database.getSubtasksCount()
        }

    }

    fun editingClicked() {
        editingClicked.value = true
    }


    fun insertRelatedNote(note: ICalObject) {
        viewModelScope.launch {
            val newNoteId = database.insertICalObject(note)
            database.insertRelatedto(Relatedto(icalObjectId = icalEntity.value!!.property.id, linkedICalObjectId = newNoteId, reltype = Reltype.CHILD.name, text = note.uid))

        }
    }



    fun updateProgress(itemId: Long, newPercent: Int) {

        val newStatus = when (newPercent) {
            100 -> StatusTodo.COMPLETED.param
            in 1..99 -> StatusTodo.INPROCESS.param
            0 -> StatusTodo.NEEDSACTION.param
            else -> StatusTodo.NEEDSACTION.param      // should never happen!
        }

        viewModelScope.launch() {
            database.updateProgress(itemId, newPercent, newStatus, System.currentTimeMillis())
        }
    }
}



