package at.bitfire.notesx5.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.database.ICalObject
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Comment
import at.bitfire.notesx5.databinding.FragmentIcalEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_ical_edit_comment.view.*
import kotlinx.android.synthetic.main.fragment_ical_edit_subtask.view.*
import java.text.DateFormat
import java.util.*


class IcalEditFragment : Fragment(),
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private lateinit var binding: FragmentIcalEditBinding
    private lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: IcalEditViewModelFactory
    private lateinit var icalEditViewModel: IcalEditViewModel
    private lateinit var inflater: LayoutInflater

    //private val allContactsMail: MutableList<String> = mutableListOf()
    private val allContactsNameAndMail: MutableList<String> = mutableListOf()

    private var displayedCategoryChips = mutableListOf<Category>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentIcalEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalEditFragmentArgs.fromBundle((arguments!!))


        // add menu
        setHasOptionsMenu(true)

        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            //request for permission to load contacts
            MaterialAlertDialogBuilder(context!!)
                    .setTitle(getString(R.string.edit_fragment_app_permission))
                    .setMessage(getString(R.string.edit_fragment_app_permission_message))
                    .setPositiveButton("Ok") { dialog, which ->
                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_CONTACTS), CONTACT_READ_PERMISSION_CODE)
                    }
                    .setNegativeButton("Cancel") { dialog, which -> }
                    .show()
        }


        this.viewModelFactory = IcalEditViewModelFactory(arguments.icalentity, dataSource, application)
        icalEditViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(IcalEditViewModel::class.java)

        binding.model = icalEditViewModel
        binding.lifecycleOwner = this


        val priorityItems = resources.getStringArray(R.array.priority)
        val classificationItems = resources.getStringArray(R.array.ical_classification)
        val statusItems = if (icalEditViewModel.iCalEntity.property.component == "TODO") {
            resources.getStringArray(R.array.vtodo_status)
        } else {
            resources.getStringArray(R.array.vjournal_status)
        }


        icalEditViewModel.savingClicked.observe(viewLifecycleOwner, {
            if (it == true) {
                icalEditViewModel.iCalObjectUpdated.value!!.collection = binding.editCollection.selectedItem.toString()

                icalEditViewModel.iCalObjectUpdated.value!!.percent = binding.editProgressSlider.value.toInt()
                icalEditViewModel.update()
            }
        })

        icalEditViewModel.deleteClicked.observe(viewLifecycleOwner, {
            if (it == true) {

                // show Alert Dialog before the item gets really deleted
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete \"${icalEditViewModel.iCalObjectUpdated.value?.summary}\"")
                builder.setMessage("Are you sure you want to delete \"${icalEditViewModel.iCalObjectUpdated.value?.summary}\"?")
                builder.setPositiveButton("Delete") { _, _ ->
                    val direction = IcalEditFragmentDirections.actionIcalEditFragmentToIcalListFragment()
                    direction.component2show = icalEditViewModel.iCalObjectUpdated.value!!.component

                    val summary = icalEditViewModel.iCalObjectUpdated.value?.summary
                    icalEditViewModel.delete()
                    Toast.makeText(context, "\"$summary\" successfully deleted.", Toast.LENGTH_LONG).show()

                    this.findNavController().navigate(direction)
                }
                /*
                builder.setNegativeButton("Cancel") { _, _ ->
                    // Do nothing, just close the message
                }

                 */

                builder.setNeutralButton("Mark as cancelled") { _, _ ->
                    if (icalEditViewModel.iCalObjectUpdated.value!!.component == "TODO")
                        icalEditViewModel.iCalObjectUpdated.value!!.status = ICalObject.STATUS_TODO_CANCELLED
                    else
                        icalEditViewModel.iCalObjectUpdated.value!!.status = ICalObject.STATUS_JOURNAL_CANCELLED

                    icalEditViewModel.savingClicked()

                    val summary = icalEditViewModel.iCalObjectUpdated.value?.summary
                    Toast.makeText(context, "\"$summary\" marked as Cancelled.", Toast.LENGTH_LONG).show()

                }

                builder.show()
            }
        })

        icalEditViewModel.returnVJournalItemId.observe(viewLifecycleOwner, {
            if (it != 0L) {
                val direction = IcalEditFragmentDirections.actionIcalEditFragmentToIcalListFragment()
                direction.component2show = icalEditViewModel.iCalObjectUpdated.value!!.component
                direction.item2focus = it
                this.findNavController().navigate(direction)
            }
            icalEditViewModel.savingClicked.value = false
        })


        icalEditViewModel.iCalObjectUpdated.observe(viewLifecycleOwner) {
            //TODO: Check if this can be done without observer (with livedata directly)
            binding.editDtstartDay.text = convertLongToDayString(it.dtstart)
            binding.editDtstartMonth.text = convertLongToMonthString(it.dtstart)
            binding.editDtstartYear.text = convertLongToYearString(it.dtstart)
            binding.editDtstartTime.text = convertLongToTimeString(it.dtstart)


            // Set the default value of the priority Chip
            if (it.priority != null && it.priority in 0..9)   // if unsupported don't show the classification
                binding.editPriorityChip.text = priorityItems[it.priority!!]  // if supported show the priority according to the String Array
            else
                binding.editPriorityChip.text = it.priorityX

            // Set the default value of the Status Chip
            if (it.status == -1)      // if unsupported don't show the status
                binding.editStatusChip.text = it.statusX
            else
                binding.editStatusChip.text = statusItems[it.status]   // if supported show the status according to the String Array

            // Set the default value of the Classification Chip
            if (it.classification == -1)      // if unsupported don't show the classification
                binding.editClassificationChip.text = it.classificationX
            else
                binding.editClassificationChip.text = classificationItems[it.classification]  // if supported show the classification according to the String Array

        }

        icalEditViewModel.showAll.observe(viewLifecycleOwner) {
            icalEditViewModel.updateVisibility()                 // Update visibility of Elements on Change of showAll
        }


        icalEditViewModel.allDayChecked.observe(viewLifecycleOwner) {

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if (it) {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = "ALLDAY"

                // make sure that the time gets reset to 0
                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = ""
                binding.editTimezoneSpinner.setSelection(0)
            }

            icalEditViewModel.updateVisibility()                 // Update visibility of Elements on Change of showAll
        }

        icalEditViewModel.addTimeChecked.observe(viewLifecycleOwner) {

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if (!it) {
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = "ALLDAY"

                // make sure that the time gets reset to 0
                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.due ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                icalEditViewModel.iCalObjectUpdated.value!!.due = c.timeInMillis
                binding.editDueTimeEdittext.text?.clear()
                binding.editDuetimezoneSpinner.setSelection(0)

            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = ""
            }

            icalEditViewModel.updateVisibility()                 // Update visibility of Elements on Change of showAll
        }


        icalEditViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            if (icalEditViewModel.savingClicked.value == true)    // don't do anything if saving was clicked, saving could interfere here!
                return@observe

            it.forEach {singleSubtask ->
                addSubtasksView(singleSubtask, container)
            }
        }



        //TODO: Check if the Sequence was updated in the meantime and notify user!


        icalEditViewModel.iCalEntity.comment?.forEach { singleComment ->
            addCommentView(singleComment, container)
        }

        icalEditViewModel.iCalEntity.category?.forEach { singleCategory ->
            addCategoryChip(singleCategory)
        }

        icalEditViewModel.iCalEntity.attendee?.forEach { singleAttendee ->
            addAttendeeChip(singleAttendee)
        }



        // set the default selection for the spinner. The same snippet exists for the allOrganizers observer
        if (icalEditViewModel.allCollections.value != null) {
            val selectedCollectionPos = icalEditViewModel.allCollections.value?.indexOf(icalEditViewModel.iCalEntity.property.collection)
            if (selectedCollectionPos != null)
                binding.editCollection.setSelection(selectedCollectionPos)
        }


        // Set up items to suggest for categories
        icalEditViewModel.allCategories.observe(viewLifecycleOwner, {
            // Create the adapter and set it to the AutoCompleteTextView
            if (icalEditViewModel.allCategories.value != null) {
                val arrayAdapter = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, icalEditViewModel.allCategories.value!!)
                binding.editCategoriesAddAutocomplete.setAdapter(arrayAdapter)
            }
        })

        icalEditViewModel.allCollections.observe(viewLifecycleOwner, {

            // set up the adapter for the organizer spinner
            val spinner: Spinner = binding.editCollection
            val adapter = ArrayAdapter<Any?>(context!!, android.R.layout.simple_spinner_item, icalEditViewModel.allCollections.value!!)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(adapter)

            // set the default selection for the spinner. The same snippet exists for the vJournalItem observer
            if (icalEditViewModel.allCollections.value != null) {
                val selectedCollectionPos = icalEditViewModel.allCollections.value?.indexOf(icalEditViewModel.iCalEntity.property.collection)
                if (selectedCollectionPos != null)
                    spinner.setSelection(selectedCollectionPos)
            }

        })


        binding.editDtstartTime.setOnClickListener {
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartYear.setOnClickListener {
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartMonth.setOnClickListener {
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartDay.setOnClickListener {
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDueDate.setEndIconOnClickListener {
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.due)
        }

        binding.editDueTime.setEndIconOnClickListener {
            showTimepicker(icalEditViewModel.iCalObjectUpdated.value?.due)

        }




        var restoreProgress = icalEditViewModel.iCalObjectUpdated.value?.percent ?: 0

        binding.editProgressSlider.addOnChangeListener { slider, value, fromUser ->
            icalEditViewModel.iCalObjectUpdated.value?.percent = value.toInt()
            binding.editProgressCheckbox.isChecked = value == 100F
            binding.editProgressPercent.text = value.toInt().toString()
            if (value != 100F)
                restoreProgress = value.toInt()

            val statusBefore = icalEditViewModel.iCalObjectUpdated.value!!.status

            when (value.toInt()) {
                100 -> icalEditViewModel.iCalObjectUpdated.value!!.status = ICalObject.STATUS_TODO_COMPLETED
                in 1..99 -> icalEditViewModel.iCalObjectUpdated.value!!.status = ICalObject.STATUS_TODO_INPROCESS
                0 -> icalEditViewModel.iCalObjectUpdated.value!!.status = ICalObject.STATUS_TODO_NEEDSACTION
            }

            // update the status only if it was actually changed, otherwise the performance sucks
            if (icalEditViewModel.iCalObjectUpdated.value!!.status != statusBefore)
                binding.editStatusChip.text = statusItems[icalEditViewModel.iCalObjectUpdated.value!!.status]
        }

        binding.editProgressCheckbox.setOnCheckedChangeListener { button, checked ->
            val newProgress: Int = if (checked)  100
            else restoreProgress

            binding.editProgressSlider.value = newProgress.toFloat()    // This will also trigger saving through the listener!
        }




            // Transform the category input into a chip when the Add-Button is clicked
        // If the user entered multiple categories separated by comma, the values will be split in multiple categories

        binding.editCategoriesAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            icalEditViewModel.categoryUpdated.add(Category(text = binding.editCategoriesAdd.editText?.text.toString()))
            addCategoryChip(Category(text = binding.editCategoriesAdd.editText?.text.toString()))
            binding.editCategoriesAdd.editText?.text?.clear()

        }


        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.editCategoriesAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    icalEditViewModel.categoryUpdated.add(Category(text = binding.editCategoriesAdd.editText?.text.toString()))
                    addCategoryChip(Category(text = binding.editCategoriesAdd.editText?.text.toString()))
                    binding.editCategoriesAdd.editText?.text?.clear()

                    true
                }
                else -> false
            }
        }

        binding.editAttendeesAddAutocomplete.setOnItemClickListener { adapterView, view, i, l ->
            //TODO
            val newAttendee = binding.editAttendeesAddAutocomplete.adapter.getItem(i).toString()
            addAttendeeChip(Attendee(caladdress = newAttendee))
            icalEditViewModel.attendeeUpdated.add(Attendee(caladdress = newAttendee))
            binding.editAttendeesAddAutocomplete.text.clear()
        }

        binding.editAttendeesAdd.setEndIconOnClickListener {

            if ((!binding.editAttendeesAdd.editText?.text.isNullOrEmpty() && !isValidEmail(binding.editAttendeesAdd.editText?.text.toString())))
                icalEditViewModel.attendeesError.value = "Please enter a valid email-address"
            else {
                val newAttendee = binding.editAttendeesAdd.editText?.text.toString()
                addAttendeeChip(Attendee(caladdress = newAttendee))
                icalEditViewModel.attendeeUpdated.add(Attendee(caladdress = newAttendee))
                binding.editAttendeesAddAutocomplete.text.clear()
            }
        }

        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.editAttendeesAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if ((!binding.editAttendeesAdd.editText?.text.isNullOrEmpty() && !isValidEmail(binding.editAttendeesAdd.editText?.text.toString())))
                        icalEditViewModel.attendeesError.value = "Please enter a valid email-address"
                    else {
                        val newAttendee = binding.editAttendeesAdd.editText?.text.toString()
                        addAttendeeChip(Attendee(caladdress = newAttendee))
                        icalEditViewModel.attendeeUpdated.add(Attendee(caladdress = newAttendee))
                        binding.editAttendeesAddAutocomplete.text.clear()
                    }

                    true
                }
                else -> false
            }
        }



        binding.editCommentAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            val newComment = Comment(text = binding.editCommentAdd.editText?.text.toString())
            icalEditViewModel.commentUpdated.add(newComment)    // store the comment for saving
            addCommentView(newComment, container)      // add the new comment
            binding.editCommentAdd.editText?.text?.clear()  // clear the field

        }


        // Transform the comment input into a view when the Done button in the keyboard is clicked
        binding.editCommentAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val newComment = Comment(text = binding.editCommentAdd.editText?.text.toString())
                    icalEditViewModel.commentUpdated.add(newComment)    // store the comment for saving
                    addCommentView(newComment, container)      // add the new comment
                    binding.editCommentAdd.editText?.text?.clear()  // clear the field
                    true
                }
                else -> false
            }
        }

        binding.editSubtasksAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            val newSubtask = ICalObject.createSubtask(summary = binding.editSubtasksAdd.editText?.text.toString())
            icalEditViewModel.subtaskUpdated.add(newSubtask)    // store the comment for saving
            addSubtasksView(newSubtask, container)      // add the new comment
            binding.editSubtasksAdd.editText?.text?.clear()  // clear the field

        }


        // Transform the comment input into a view when the Done button in the keyboard is clicked
        binding.editSubtasksAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val newSubtask = ICalObject.createSubtask(summary = binding.editSubtasksAdd.editText?.text.toString())
                    icalEditViewModel.subtaskUpdated.add(newSubtask)    // store the comment for saving
                    addSubtasksView(newSubtask, container)      // add the new comment
                    binding.editSubtasksAdd.editText?.text?.clear()  // clear the field
                    true
                }
                else -> false
            }
        }


        binding.editStatusChip.setOnClickListener {

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set status")
                    .setItems(statusItems) { dialog, which ->
                        // Respond to item chosen
                        icalEditViewModel.iCalObjectUpdated.value!!.status = which
                        binding.editStatusChip.text = statusItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_status)
                    .show()
        }


        binding.editClassificationChip.setOnClickListener {

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set classification")
                    .setItems(classificationItems) { dialog, which ->
                        // Respond to item chosen
                        icalEditViewModel.iCalObjectUpdated.value!!.classification = which
                        binding.editClassificationChip.text = classificationItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_classification)
                    .show()
        }


        binding.editPriorityChip.setOnClickListener {

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set priority")
                    .setItems(priorityItems) { dialog, which ->
                        // Respond to item chosen
                        icalEditViewModel.iCalObjectUpdated.value!!.priority = which
                        binding.editPriorityChip.text = priorityItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_priority)
                    .show()
        }


        binding.editUrlEdit.editText?.setOnFocusChangeListener { view, hasFocus ->
            if ((!binding.editUrlEdit.editText?.text.isNullOrEmpty() && !isValidURL(binding.editUrlEdit.editText?.text.toString())))
                icalEditViewModel.urlError.value = "Please enter a valid URL"
        }


        return binding.root
    }


    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        // depending on the component the date/time is processed for the dtstart-field (Journal) or for the due-field (Todos)
        if(icalEditViewModel.iCalEntity.property.component == "JOURNAL") {

            val c = Calendar.getInstance()
            c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, day)

            binding.editDtstartYear.text = convertLongToYearString(c.timeInMillis)
            binding.editDtstartMonth.text = convertLongToMonthString(c.timeInMillis)
            binding.editDtstartDay.text = convertLongToDayString(c.timeInMillis)

            icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis

            if (!icalEditViewModel.allDayChecked.value!!)     // let the user set the time only if the allDaySwitch is not set!
                showTimepicker(icalEditViewModel.iCalObjectUpdated.value!!.dtstart)

        }
        else if (icalEditViewModel.iCalEntity.property.component == "TODO") {

            val c = Calendar.getInstance()
            c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.due ?: System.currentTimeMillis()

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, month)
            c.set(Calendar.DAY_OF_MONTH, day)

            val dateString = DateFormat.getDateInstance().format(c.time)
            binding.editDueDateEdittext.setText(dateString)
            icalEditViewModel.iCalObjectUpdated.value!!.due = c.timeInMillis

        }

    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {

        // depending on the component the date/time is processed for the dtstart-field (Journal) or for the due-field (Todos)
        if(icalEditViewModel.iCalEntity.property.component == "JOURNAL") {

            val c = Calendar.getInstance()
            c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!
            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
            c.set(Calendar.MINUTE, minute)

            binding.editDtstartTime.text = convertLongToTimeString(c.timeInMillis)

            icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
        }
        else if (icalEditViewModel.iCalEntity.property.component == "TODO") {

            val c = Calendar.getInstance()
            c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.due ?: System.currentTimeMillis()
            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
            c.set(Calendar.MINUTE, minute)

            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
            binding.editDueTimeEdittext.setText(timeString)
            icalEditViewModel.iCalObjectUpdated.value!!.due = c.timeInMillis
        }

    }


    fun showDatepicker(selectedDate: Long?) {
        val c = Calendar.getInstance()
        c.timeInMillis = selectedDate ?: System.currentTimeMillis()
        //c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(activity!!, this, year, month, day).show()
    }

    fun showTimepicker(selectedTime: Long?) {
        val c = Calendar.getInstance()
        c.timeInMillis = selectedTime ?: System.currentTimeMillis()
        //c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!

        val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        TimePickerDialog(activity, this, hourOfDay, minute, is24HourFormat(activity)).show()
    }




    private fun addCategoryChip(category: Category) {

        if (category.text.isBlank())
            return

        val categoryChip = inflater.inflate(R.layout.fragment_ical_edit_categories_chip, binding.editCategoriesChipgroup, false) as Chip
        categoryChip.text = category.text
        binding.editCategoriesChipgroup.addView(categoryChip)
        displayedCategoryChips.add(category)

        categoryChip.setOnClickListener {
            // Responds to chip click
        }

        categoryChip.setOnCloseIconClickListener { chip ->
            icalEditViewModel.categoryDeleted.add(category)  // add the category to the list for categories to be deleted
            chip.visibility = View.GONE
        }

        categoryChip.setOnCheckedChangeListener { chip, isChecked ->
            // Responds to chip checked/unchecked
        }
    }


    private fun addAttendeeChip(attendee: Attendee) {

        if (attendee.caladdress.isBlank())
            return

        val attendeeRoles = resources.getStringArray(R.array.ical_attendee_roles)

        val attendeeChip = inflater.inflate(R.layout.fragment_ical_edit_attendees_chip, binding.editAttendeesChipgroup, false) as Chip
        attendeeChip.text = attendee.caladdress
        when (attendee.roleparam) {
            0 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_chair, null)
            1 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
            2 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_optparticipant, null)
            3 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_nonparticipant, null)
            else -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
        }
        attendeeChip.chipIcon
        binding.editAttendeesChipgroup.addView(attendeeChip)


        attendeeChip.setOnClickListener {

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set attendee role")
                    .setItems(attendeeRoles) { dialog, which ->
                        // Respond to item chosen
                        val curIndex = icalEditViewModel.attendeeUpdated.indexOf(attendee)    // find the attendee in the original list
                        if (curIndex == -1)
                            icalEditViewModel.attendeeUpdated.add(attendee)                   // add the attendee to the list of updated items if it was not there yet
                        else
                            icalEditViewModel.attendeeUpdated[curIndex].roleparam = which      // update the roleparam
                        attendee.roleparam = which

                        when (which) {
                            0 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_chair, null)
                            1 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
                            2 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_optparticipant, null)
                            3 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_nonparticipant, null)
                            else -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
                        }

                    }
                    .setIcon(R.drawable.ic_attendee)
                    .show()
        }

        attendeeChip.setOnCloseIconClickListener { chip ->
            icalEditViewModel.attendeeDeleted.add(attendee)  // add the category to the list for categories to be deleted
            chip.visibility = View.GONE
        }
    }


    private fun addCommentView(comment: Comment, container: ViewGroup?) {

        val commentView = inflater.inflate(R.layout.fragment_ical_edit_comment, container, false)
        commentView.edit_comment_textview.text = comment.text
        binding.editCommentsLinearlayout.addView(commentView)

        // set on Click Listener to open a dialog to update the comment
        commentView.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedText = TextInputEditText(context!!)
            updatedText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            updatedText.setText(comment.text)
            updatedText.isSingleLine = false
            updatedText.maxLines = 8

            // set up the builder for the AlertDialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Edit comment")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(updatedText)


            builder.setPositiveButton("Save") { _, _ ->
                // update the comment
                val updatedComment = comment.copy()
                updatedComment.text = updatedText.text.toString()
                icalEditViewModel.commentUpdated.add(updatedComment)
                it.edit_comment_textview.text = updatedComment.text
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.setNeutralButton("Delete") { _, _ ->
                icalEditViewModel.commentDeleted.add(comment)
                it.visibility = View.GONE
            }
            builder.show()
        }
    }



    private fun addSubtasksView(subtask: ICalObject?, container: ViewGroup?) {

        if (subtask == null)
            return

        val subtaskView = inflater.inflate(R.layout.fragment_ical_edit_subtask, container, false)
        subtaskView.edit_subtask_textview.text = subtask.summary
        subtaskView.edit_subtask_progress_slider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskView.edit_subtask_progress_percent.text = if(subtask.percent?.toFloat() != null)
            subtask.percent?.toString()
        else "0"

        subtaskView.edit_subtask_progress_checkbox.isChecked = subtask.percent == 100

        var restoreProgress = subtask.percent

        subtaskView.edit_subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->
            //Update the progress in the updated list: try to find the matching uid (the only unique element for now) and then assign the percent
            //Attention, the new subtask must have been inserted before in the list!
            if (icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid } == null) {
                val changedItem = subtask.copy()
                changedItem.percent = value.toInt()
                icalEditViewModel.subtaskUpdated.add(changedItem)
            } else {
                icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid }?.percent = value.toInt()
            }

            subtaskView.edit_subtask_progress_checkbox.isChecked = value == 100F
            subtaskView.edit_subtask_progress_percent.text = value.toInt().toString()
            if (value != 100F)
                restoreProgress = value.toInt()
        }

        subtaskView.edit_subtask_progress_checkbox.setOnCheckedChangeListener { button, checked ->
            val newProgress: Int = if (checked)  100
             else restoreProgress ?: 0

            subtaskView.edit_subtask_progress_slider.value = newProgress.toFloat()    // This will also trigger saving through the listener!
        }


        binding.editSubtasksLinearlayout.addView(subtaskView)

        // set on Click Listener to open a dialog to update the comment
        subtaskView.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedSummary = TextInputEditText(context!!)
            updatedSummary.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            updatedSummary.setText(subtask.summary)
            updatedSummary.isSingleLine = false
            updatedSummary.maxLines = 2

            // set up the builder for the AlertDialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Edit subtask")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(updatedSummary)


            builder.setPositiveButton("Save") { _, _ ->

                if (icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid } == null) {
                    val changedItem = subtask.copy()
                    changedItem.summary = updatedSummary.text.toString()
                    icalEditViewModel.subtaskUpdated.add(changedItem)
                } else {
                    icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid }?.summary = updatedSummary.text.toString()
                }
                it.edit_subtask_textview.text = updatedSummary.text.toString()

            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.setNeutralButton("Delete") { _, _ ->
                icalEditViewModel.subtaskDeleted.add(subtask)
                it.visibility = View.GONE
            }
            builder.show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_edit_delete) {
            icalEditViewModel.deleteClicked()
        }
        return super.onOptionsItemSelected(item)
    }


    fun loadContacts() {

        /*
        Template: https://stackoverflow.com/questions/10117049/get-only-email-address-from-contact-list-android
         */

        val context = activity
        val cr = context!!.contentResolver
        val PROJECTION = arrayOf(ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA)
        val order = ContactsContract.Contacts.DISPLAY_NAME
        val filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''"
        val cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order)

        if (cur!!.count > 0) {
            while (cur.moveToNext()) {

                val name = cur.getString(1)    // according to projection 0 = DISPLAY_NAME, 1 = Email.DATA
                val emlAddr = cur.getString(2)
                //Log.println(Log.INFO, "cursor: ", "$name: $emlAddr")
                allContactsNameAndMail.add("$name ($emlAddr)")
                //allContactsAsAttendee.add(VAttendee(cnparam = name, attendee = emlAddr))

            }
            cur.close()
        }

        val arrayAdapterNameAndMail = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allContactsNameAndMail)

        binding.editContactAddAutocomplete.setAdapter(arrayAdapterNameAndMail)
        binding.editAttendeesAddAutocomplete.setAdapter(arrayAdapterNameAndMail)


    }
}
