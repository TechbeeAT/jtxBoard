/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.Manifest
import android.annotation.SuppressLint
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
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.*
import at.bitfire.notesx5.databinding.FragmentIcalEditBinding
import at.bitfire.notesx5.databinding.FragmentIcalEditCommentBinding
import at.bitfire.notesx5.databinding.FragmentIcalEditSubtaskBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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

    private val allContactsMail: MutableList<String> = mutableListOf()
    //private val allContactsNameAndMail: MutableList<String> = mutableListOf()

    private var displayedCategoryChips = mutableListOf<Category>()


    private var datetimepickerOrigin: Int? = null


    companion object {
        const val PICKER_ORIGIN_DTSTART = 0
        const val PICKER_ORIGIN_DUE = 1
        const val PICKER_ORIGIN_COMPLETED = 2
        const val PICKER_ORIGIN_STARTED = 3

    }



    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentIcalEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalEditFragmentArgs.fromBundle((requireArguments()))


        // add menu
        setHasOptionsMenu(true)


        // Check if the permission to read local contacts is already granted, otherwise make a dialog to ask for permission
        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            //request for permission to load contacts
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.edit_fragment_app_permission))
                    .setMessage(getString(R.string.edit_fragment_app_permission_message))
                    .setPositiveButton("Ok") { dialog, which ->
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), CONTACT_READ_PERMISSION_CODE)
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

        var classificationItems: Array<String> = arrayOf()
        Classification.values().forEach { classificationItems = classificationItems.plus(getString(it.stringResource))       }

        var statusItems: Array<String> = arrayOf()
        if (icalEditViewModel.iCalEntity.property.component == Component.VTODO.name) {
            StatusTodo.values().forEach { statusItems = statusItems.plus(getString(it.stringResource))       }
        } else {
            StatusJournal.values().forEach { statusItems = statusItems.plus(getString(it.stringResource))       }
        }


        binding.editTimezoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = binding.editTimezoneSpinner.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {        }    // nothing to do
        }
        binding.editStartedtimezoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = binding.editStartedtimezoneSpinner.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {        }    // nothing to do
        }
        binding.editDuetimezoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = binding.editDuetimezoneSpinner.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {        }    // nothing to do
        }
        binding.editCompletedtimezoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = binding.editCompletedtimezoneSpinner.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {        }    // nothing to do
        }



        icalEditViewModel.savingClicked.observe(viewLifecycleOwner, {
            if (it == true) {
                icalEditViewModel.iCalObjectUpdated.value?.collectionId = icalEditViewModel.allCollections.value?.find { it.displayName == binding.editCollection.selectedItem.toString() }!!.collectionId

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
                    direction.module2show = icalEditViewModel.iCalObjectUpdated.value!!.module

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
                    if (icalEditViewModel.iCalObjectUpdated.value!!.component == Component.VTODO.name)
                        icalEditViewModel.iCalObjectUpdated.value!!.status = StatusTodo.CANCELLED.name
                    else
                        icalEditViewModel.iCalObjectUpdated.value!!.status = StatusJournal.CANCELLED.name

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
                direction.module2show = icalEditViewModel.iCalObjectUpdated.value!!.module
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
                binding.editPriorityChip.text = it.priority.toString()

            // Set the default value of the Status Chip
            if (it.component == Component.VTODO.name)
                binding.editStatusChip.text = StatusTodo.getStringResource(requireContext(), it.status) ?: it.status
            else if (it.component == Component.VJOURNAL.name)
                binding.editStatusChip.text = StatusJournal.getStringResource(requireContext(), it.status) ?: it.status
            else
                binding.editStatusChip.text = it.status       // if unsupported just show whatever is there


            // Set the default value of the Classification Chip
            binding.editClassificationChip.text = Classification.getStringResource(requireContext(), it.classification) ?: it.classification       // if unsupported just show whatever is there

            binding.editTimezoneSpinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(it.dtstartTimezone))
            binding.editCompletedtimezoneSpinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(it.completedTimezone))
            binding.editDuetimezoneSpinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(it.dueTimezone))
            binding.editStartedtimezoneSpinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(it.dtstartTimezone))

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

        icalEditViewModel.addDueTimeChecked.observe(viewLifecycleOwner) {

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


        icalEditViewModel.addCompletedTimeChecked.observe(viewLifecycleOwner) {

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if (!it) {
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = "ALLDAY"

                // make sure that the time gets reset to 0
                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.completed ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                icalEditViewModel.iCalObjectUpdated.value!!.completed = c.timeInMillis
                binding.editCompletedTimeEdittext.text?.clear()
                binding.editCompletedtimezoneSpinner.setSelection(0)

            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = ""
            }

            icalEditViewModel.updateVisibility()                 // Update visibility of Elements on Change of showAll
        }

        icalEditViewModel.addStartedTimeChecked.observe(viewLifecycleOwner) {

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if (!it) {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = "ALLDAY"

                // make sure that the time gets reset to 0
                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.set(Calendar.MINUTE, 0)
                icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
                binding.editStartedTimeEdittext.text?.clear()
                binding.editStartedtimezoneSpinner.setSelection(0)

            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = ""
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
            val selectedCollectionPos = icalEditViewModel.allCollections.value?.indexOf(icalEditViewModel.iCalEntity.ICalCollection)
            if (selectedCollectionPos != null)
                binding.editCollection.setSelection(selectedCollectionPos)
        }


        // Set up items to suggest for categories
        icalEditViewModel.allCategories.observe(viewLifecycleOwner, {
            // Create the adapter and set it to the AutoCompleteTextView
            if (icalEditViewModel.allCategories.value != null) {
                val arrayAdapter = ArrayAdapter(application.applicationContext, android.R.layout.simple_list_item_1, icalEditViewModel.allCategories.value!!)
                binding.editCategoriesAddAutocomplete.setAdapter(arrayAdapter)
            }
        })

        // initialize allRelatedto
        icalEditViewModel.allRelatedto.observe(viewLifecycleOwner, {

            // if the current item can be found as linkedICalObjectId and the reltype is CHILD, then it must be a child and changing the collection is not allowed
            if(it.isNotEmpty() && it.find { rel -> rel.linkedICalObjectId == icalEditViewModel.iCalObjectUpdated.value?.id && rel.reltype == Reltype.CHILD.name } != null)
                binding.editCollection.isEnabled = false
        })

        icalEditViewModel.allCollections.observe(viewLifecycleOwner, {

            // set up the adapter for the organizer spinner
            val spinner: Spinner = binding.editCollection
            val allCollectionNames: MutableList<String> = mutableListOf()
            icalEditViewModel.allCollections.value?.forEach { it.displayName?.let { name -> allCollectionNames.add(name) } }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, allCollectionNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(adapter)

            // set the default selection for the spinner. The same snippet exists for the vJournalItem observer
            if (icalEditViewModel.allCollections.value != null) {
                val selectedCollectionPos = icalEditViewModel.allCollections.value?.indexOf(icalEditViewModel.iCalEntity.ICalCollection)
                if (selectedCollectionPos != null)
                    spinner.setSelection(selectedCollectionPos)
            }
        })


        binding.editDtstartTime.setOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DTSTART
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartYear.setOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DTSTART
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartMonth.setOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DTSTART
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDtstartDay.setOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DTSTART
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editDueDate.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DUE
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.due)
        }

        binding.editDueTime.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_DUE
            showTimepicker(icalEditViewModel.iCalObjectUpdated.value?.due)
        }

        binding.editCompletedDate.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_COMPLETED
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.completed)
        }

        binding.editCompletedTime.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_COMPLETED
            showTimepicker(icalEditViewModel.iCalObjectUpdated.value?.completed)
        }

        binding.editStartedDate.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_STARTED
            showDatepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }

        binding.editStartedTime.setEndIconOnClickListener {
            datetimepickerOrigin = PICKER_ORIGIN_STARTED
            showTimepicker(icalEditViewModel.iCalObjectUpdated.value?.dtstart)
        }



        var restoreProgress = icalEditViewModel.iCalObjectUpdated.value?.percent ?: 0

        binding.editProgressSlider.addOnChangeListener { slider, value, fromUser ->
            icalEditViewModel.iCalObjectUpdated.value?.percent = value.toInt()
            binding.editProgressCheckbox.isChecked = value == 100F
            binding.editProgressPercent.text = "${value.toInt()} %"
            if (value != 100F)
                restoreProgress = value.toInt()

            val statusBefore = icalEditViewModel.iCalObjectUpdated.value!!.status

            when (value.toInt()) {
                100 -> icalEditViewModel.iCalObjectUpdated.value!!.status = StatusTodo.COMPLETED.name
                in 1..99 -> icalEditViewModel.iCalObjectUpdated.value!!.status = StatusTodo.`IN-PROCESS`.name
                0 -> icalEditViewModel.iCalObjectUpdated.value!!.status = StatusTodo.`NEEDS-ACTION`.name
            }

            // update the status only if it was actually changed, otherwise the performance sucks
            if (icalEditViewModel.iCalObjectUpdated.value!!.status != statusBefore) {
                if (icalEditViewModel.iCalObjectUpdated.value!!.component == Component.VTODO.name)
                    binding.editStatusChip.text = StatusTodo.getStringResource(requireContext(), icalEditViewModel.iCalObjectUpdated.value!!.status) ?: icalEditViewModel.iCalObjectUpdated.value!!.status
                else if (icalEditViewModel.iCalObjectUpdated.value!!.component == Component.VJOURNAL.name)
                    binding.editStatusChip.text = StatusJournal.getStringResource(requireContext(), icalEditViewModel.iCalObjectUpdated.value!!.status) ?: icalEditViewModel.iCalObjectUpdated.value!!.status
                else
                    binding.editStatusChip.text = icalEditViewModel.iCalObjectUpdated.value!!.status       // if unsupported just show whatever is there
            }
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

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Set status")
                    .setItems(statusItems) { dialog, which ->
                        // Respond to item chosen
                        if (icalEditViewModel.iCalObjectUpdated.value!!.component == Component.VTODO.name) {
                            icalEditViewModel.iCalObjectUpdated.value!!.status = StatusTodo.values().getOrNull(which)!!.name
                            binding.editStatusChip.text = StatusTodo.getStringResource(requireContext(), icalEditViewModel.iCalObjectUpdated.value!!.status)
                        }

                        if (icalEditViewModel.iCalObjectUpdated.value!!.component == Component.VJOURNAL.name) {
                            icalEditViewModel.iCalObjectUpdated.value!!.status = StatusJournal.values().getOrNull(which)!!.name
                            binding.editStatusChip.text = StatusJournal.getStringResource(requireContext(), icalEditViewModel.iCalObjectUpdated.value!!.status)
                        }

                    }
                    .setIcon(R.drawable.ic_status)
                    .show()
        }


        binding.editClassificationChip.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Set classification")
                    .setItems(classificationItems) { dialog, which ->
                        // Respond to item chosen
                        icalEditViewModel.iCalObjectUpdated.value!!.classification = Classification.values().getOrNull(which)!!.name
                        binding.editClassificationChip.text = Classification.getStringResource(requireContext(), icalEditViewModel.iCalObjectUpdated.value!!.classification)    // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_classification)
                    .show()
        }


        binding.editPriorityChip.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
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
            if ((binding.editUrlEdit.editText?.text?.isNotBlank() == true && !isValidURL(binding.editUrlEdit.editText?.text.toString())))
                icalEditViewModel.urlError.value = "Please enter a valid URL"
        }

        binding.editAttendeesAdd.editText?.setOnFocusChangeListener { view, hasFocus ->
            if ((binding.editAttendeesAdd.editText?.text?.isNotBlank() == true && !isValidEmail(binding.editAttendeesAdd.editText?.text.toString())))
                icalEditViewModel.attendeesError.value = "Please enter a valid E-Mail address"
        }


        return binding.root
    }


    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        // depending on the origin of the click the date/time is processed for the dtstart-field (Journal) or for the due-field (Todos) or for the completed-field (Todos)
        when (datetimepickerOrigin) {
            PICKER_ORIGIN_DTSTART -> {

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
            PICKER_ORIGIN_DUE -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.due
                        ?: System.currentTimeMillis()

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, day)

                val dateString = DateFormat.getDateInstance().format(c.time)
                binding.editDueDateEdittext.setText(dateString)
                icalEditViewModel.iCalObjectUpdated.value!!.due = c.timeInMillis
            }
            PICKER_ORIGIN_COMPLETED -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.completed
                        ?: System.currentTimeMillis()

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, day)

                val dateString = DateFormat.getDateInstance().format(c.time)
                binding.editCompletedDateEdittext.setText(dateString)
                icalEditViewModel.iCalObjectUpdated.value!!.completed = c.timeInMillis
            }
            PICKER_ORIGIN_STARTED -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart
                        ?: System.currentTimeMillis()

                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, month)
                c.set(Calendar.DAY_OF_MONTH, day)

                val dateString = DateFormat.getDateInstance().format(c.time)
                binding.editStartedDateEdittext.setText(dateString)
                icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
            }
        }
    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {

        // depending on the component the date/time is processed for the dtstart-field (Journal) or for the due-field (Todos)
        when (datetimepickerOrigin) {
            PICKER_ORIGIN_DTSTART -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)

                binding.editDtstartTime.text = convertLongToTimeString(c.timeInMillis)

                icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
            }
            PICKER_ORIGIN_DUE -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.due
                        ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)

                val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                binding.editDueTimeEdittext.setText(timeString)
                icalEditViewModel.iCalObjectUpdated.value!!.due = c.timeInMillis
            }

            PICKER_ORIGIN_COMPLETED -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.completed
                        ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)

                val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                binding.editCompletedTimeEdittext.setText(timeString)
                icalEditViewModel.iCalObjectUpdated.value!!.completed = c.timeInMillis
            }

            PICKER_ORIGIN_STARTED -> {

                val c = Calendar.getInstance()
                c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart
                        ?: System.currentTimeMillis()
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)

                val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                binding.editStartedTimeEdittext.setText(timeString)
                icalEditViewModel.iCalObjectUpdated.value!!.dtstart = c.timeInMillis
            }

        }

    }


    private fun showDatepicker(selectedDate: Long?) {
        val c = Calendar.getInstance()
        c.timeInMillis = selectedDate ?: System.currentTimeMillis()
        //c.timeInMillis = icalEditViewModel.iCalObjectUpdated.value?.dtstart!!

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(requireActivity(), this, year, month, day).show()
    }

    private fun showTimepicker(selectedTime: Long?) {
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

        var attendeeRoles: Array<String> = arrayOf()
        Role.values().forEach { attendeeRoles = attendeeRoles.plus(getString(it.stringResource))       }

        val attendeeChip = inflater.inflate(R.layout.fragment_ical_edit_attendees_chip, binding.editAttendeesChipgroup, false) as Chip
        attendeeChip.text = attendee.caladdress
        attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, Role.getDrawableResourceByName(attendee.role), null)
        binding.editAttendeesChipgroup.addView(attendeeChip)


        attendeeChip.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Set attendee role")
                    .setItems(attendeeRoles) { dialog, which ->
                        // Respond to item chosen
                        val curIndex = icalEditViewModel.attendeeUpdated.indexOf(attendee)    // find the attendee in the original list
                        if (curIndex == -1)
                            icalEditViewModel.attendeeUpdated.add(attendee)                   // add the attendee to the list of updated items if it was not there yet
                        else
                            icalEditViewModel.attendeeUpdated[curIndex].role = Role.values().getOrNull(which)?.name      // update the roleparam

                        attendee.role = Role.values().getOrNull(which)?.name
                        attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, Role.values().getOrNull(which)?.icon ?: R.drawable.ic_attendee_reqparticipant, null)

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

        val bindingComment = FragmentIcalEditCommentBinding.inflate(inflater, container, false)
        bindingComment.editCommentTextview.text = comment.text
        //commentView.edit_comment_textview.text = comment.text
        binding.editCommentsLinearlayout.addView(bindingComment.root)

        // set on Click Listener to open a dialog to update the comment
        bindingComment.root.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedText = TextInputEditText(requireContext())
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
                bindingComment.editCommentTextview.text = updatedComment.text
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.setNeutralButton("Delete") { _, _ ->
                icalEditViewModel.commentDeleted.add(comment)
                bindingComment.root.visibility = View.GONE
            }
            builder.show()
        }
    }



    @SuppressLint("SetTextI18n")
    private fun addSubtasksView(subtask: ICalObject?, container: ViewGroup?) {

        if (subtask == null)
            return

        val bindingSubtask = FragmentIcalEditSubtaskBinding.inflate(inflater, container, false)
        bindingSubtask.editSubtaskTextview.text = subtask.summary
        bindingSubtask.editSubtaskProgressSlider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        bindingSubtask.editSubtaskProgressPercent.text = if(subtask.percent?.toFloat() != null)
            "${subtask.percent!!.toInt()} %"
        else "0"

        bindingSubtask.editSubtaskProgressCheckbox.isChecked = subtask.percent == 100

        var restoreProgress = subtask.percent

        bindingSubtask.editSubtaskProgressSlider.addOnChangeListener { slider, value, fromUser ->
            //Update the progress in the updated list: try to find the matching uid (the only unique element for now) and then assign the percent
            //Attention, the new subtask must have been inserted before in the list!
            if (icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid } == null) {
                val changedItem = subtask.copy()
                changedItem.percent = value.toInt()
                icalEditViewModel.subtaskUpdated.add(changedItem)
            } else {
                icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid }?.percent = value.toInt()
            }

            bindingSubtask.editSubtaskProgressCheckbox.isChecked = value == 100F
            bindingSubtask.editSubtaskProgressPercent.text = "${value.toInt()} %"
            if (value != 100F)
                restoreProgress = value.toInt()
        }

        bindingSubtask.editSubtaskProgressCheckbox.setOnCheckedChangeListener { button, checked ->
            val newProgress: Int = if (checked)  100
             else restoreProgress ?: 0

            bindingSubtask.editSubtaskProgressSlider.value = newProgress.toFloat()    // This will also trigger saving through the listener!
        }


        binding.editSubtasksLinearlayout.addView(bindingSubtask.root)

        // set on Click Listener to open a dialog to update the comment
        bindingSubtask.root.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedSummary = TextInputEditText(requireContext())
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
                bindingSubtask.editSubtaskTextview.text = updatedSummary.text.toString()

            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.setNeutralButton("Delete") { _, _ ->
                icalEditViewModel.subtaskDeleted.add(subtask)
                bindingSubtask.root.visibility = View.GONE
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


    private fun loadContacts() {

        /*
        Template: https://stackoverflow.com/questions/10117049/get-only-email-address-from-contact-list-android
         */

        val context = activity
        val cr = context!!.contentResolver
        val projection = arrayOf(ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.DATA)
        val order = ContactsContract.Contacts.DISPLAY_NAME
        val filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''"
        val cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, filter, null, order)

        if (cur!!.count > 0) {
            while (cur.moveToNext()) {

                //val name = cur.getString(1)    // according to projection 0 = DISPLAY_NAME, 1 = Email.DATA
                val emlAddr = cur.getString(2)
                //Log.println(Log.INFO, "cursor: ", "$name: $emlAddr")
                allContactsMail.add(emlAddr)
                //allContactsNameAndMail.add("$name ($emlAddr)")
                //allContactsAsAttendee.add(VAttendee(cnparam = name, attendee = emlAddr))

            }
            cur.close()
        }

        // TODO: Here it can be considered that also the cuname in the attendee is filled out based on the contacts entry.
        //val arrayAdapterNameAndMail = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allContactsNameAndMail)
        val arrayAdapterNameAndMail = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allContactsMail)

        binding.editContactAddAutocomplete.setAdapter(arrayAdapterNameAndMail)
        binding.editAttendeesAddAutocomplete.setAdapter(arrayAdapterNameAndMail)


    }
}
