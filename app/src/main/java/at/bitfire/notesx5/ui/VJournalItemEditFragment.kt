package at.bitfire.notesx5.ui

import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentVjournalItemEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_vjournal_item_categories_chip.view.*
import java.util.*


class VJournalItemEditFragment : Fragment(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    lateinit var binding: FragmentVjournalItemEditBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory:  VJournalItemEditViewModelFactory
    lateinit var vJournalItemEditViewModel: VJournalItemEditViewModel
    lateinit var inflater: LayoutInflater

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentVjournalItemEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemEditFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)


        this.viewModelFactory = VJournalItemEditViewModelFactory(arguments.item2edit, dataSource, application)
        vJournalItemEditViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemEditViewModel::class.java)

        binding.model = vJournalItemEditViewModel
        binding.lifecycleOwner = this



        vJournalItemEditViewModel.savingClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                vJournalItemEditViewModel.summaryChanged = binding.summaryEdit.editText?.text.toString()
                vJournalItemEditViewModel.descriptionChanged = binding.descriptionEdit.editText?.text.toString()
                vJournalItemEditViewModel.organizerChanged = binding.organizer.selectedItem.toString()
                vJournalItemEditViewModel.urlChanged = binding.urlEdit.editText?.text.toString()
                vJournalItemEditViewModel.attendeeChanged = binding.attendeeEdit.editText?.text.toString()
                vJournalItemEditViewModel.contactChanged = binding.contactEdit.editText?.text.toString()
                vJournalItemEditViewModel.relatesChanged = binding.relatedtoEdit.editText?.text.toString()
                vJournalItemEditViewModel.update()
            }
        })

        vJournalItemEditViewModel.deleteClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {

                // show Alert Dialog before the item gets really deleted
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete \"${vJournalItemEditViewModel.vJournalItem.value!!.summary}\"")
                builder.setMessage("Are you sure you want to delete \"${vJournalItemEditViewModel.vJournalItem.value!!.summary}\"?")
                builder.setPositiveButton("Delete") { _, _ ->
                    var summary = vJournalItemEditViewModel.vJournalItem.value!!.summary
                    vJournalItemEditViewModel.delete()
                    Toast.makeText(context, "\"$summary\" successfully deleted.", Toast.LENGTH_LONG).show()
                    this.findNavController().navigate(VJournalItemEditFragmentDirections.actionVJournalItemEditFragmentToVjournalListFragmentList())
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    // Do nothing, just close the message
                }

                builder.setNeutralButton("Mark as cancelled") { _, _ ->
                    vJournalItemEditViewModel.statusChanged = 2    // 2 = CANCELLED
                    vJournalItemEditViewModel.savingClicked()

                    var summary = vJournalItemEditViewModel.vJournalItem.value!!.summary
                    Toast.makeText(context, "\"$summary\" marked as Cancelled.", Toast.LENGTH_LONG).show()

                }

                builder.show()
            }
        })

        vJournalItemEditViewModel.returnVJournalItemId.observe(viewLifecycleOwner, Observer {
            if (it != 0L) {
                this.findNavController().navigate(VJournalItemEditFragmentDirections.actionVJournalItemEditFragmentToVjournalListFragmentList().setItem2focus(it))
            }
            vJournalItemEditViewModel.savingClicked.value = false
        })


        vJournalItemEditViewModel.vJournalItem.observe(viewLifecycleOwner, {

            // Add the chips for existing categories
            if (vJournalItemEditViewModel.vJournalItem.value != null)
                addChips(convertCategoriesCSVtoList(vJournalItemEditViewModel.vJournalItem.value!!.categories))


            // Set the default value of the Status Chip
            val statusItems = resources.getStringArray(R.array.vjournal_status)
            if (vJournalItemEditViewModel.vJournalItem.value?.status == 3)      // if unsupported don't show the status
                binding.statusChip.visibility = View.GONE
            else
                binding.statusChip.text = statusItems[vJournalItemEditViewModel.vJournalItem.value!!.status]   // if supported show the status according to the String Array


            // Set the default value of the Classification Chip
            val classificationItems = resources.getStringArray(R.array.vjournal_classification)
            if (vJournalItemEditViewModel.vJournalItem.value?.classification == 3)      // if unsupported don't show the classification
                binding.classificationChip.visibility = View.GONE
            else
                binding.classificationChip.text = classificationItems[vJournalItemEditViewModel.vJournalItem.value!!.classification]  // if supported show the classification according to the String Array

            // set the default selection for the spinner. The same snippet exists for the allOrganizers observer
            if(vJournalItemEditViewModel.allOrganizers.value != null) {
                var selectedOrganizerPos = vJournalItemEditViewModel.allOrganizers.value?.indexOf(vJournalItemEditViewModel.vJournalItem.value?.organizer)
                if (selectedOrganizerPos != null)
                    binding.organizer.setSelection(selectedOrganizerPos)
            }

            /*

                // Set the default value of the Status Spinner
                when(vJournalItemEditViewModel.vJournalItem.value?.status) {
                    "DRAFT" -> binding.statusSpinner.setSelection(0)
                    "FINAL" -> binding.statusSpinner.setSelection(1)
                    "CANCELLED" -> binding.statusSpinner.setSelection(2)
                    else -> binding.statusSpinner.visibility = View.GONE      // don't show the spinner if the value is not known in order to not mess with external data!
                }

                // Set the default value of the Classification Spinner
                when(vJournalItemEditViewModel.vJournalItem.value?.classification) {
                    "PUBLIC" -> binding.classificationSpinner.setSelection(0)
                    "PRIVATE" -> binding.classificationSpinner.setSelection(1)
                    "CONFIDENTIAL" -> binding.classificationSpinner.setSelection(2)
                    else -> binding.classificationSpinner.visibility = View.GONE      // don't show the spinner if the value is not known in order to not mess with external data!
                }

    */
        })

        // Set up items to suggest for categories
        vJournalItemEditViewModel.allCategories.observe(viewLifecycleOwner, {
            // Create the adapter and set it to the AutoCompleteTextView
            if (vJournalItemEditViewModel.allCategories.value != null) {
                val allCategoriesCSV = convertCategoriesListtoCSVString(vJournalItemEditViewModel.allCategories.value!!.toMutableList())
                val allCategoriesList = convertCategoriesCSVtoList(allCategoriesCSV).distinct()
                val arrayAdapter = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allCategoriesList)
                binding.categoriesAddAutocomplete.setAdapter(arrayAdapter)
            }
        })

        vJournalItemEditViewModel.allOrganizers.observe(viewLifecycleOwner, {

            // set up the adapter for the organizer spinner
            val spinner: Spinner = binding.organizer
            val adapter = ArrayAdapter<Any?>(context!!, android.R.layout.simple_spinner_item, vJournalItemEditViewModel.allOrganizers.value!!)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(adapter)

            // set the default selection for the spinner. The same snippet exists for the vJournalItem observer
            if(vJournalItemEditViewModel.allOrganizers.value != null) {
                var selectedOrganizerPos = vJournalItemEditViewModel.allOrganizers.value?.indexOf(vJournalItemEditViewModel.vJournalItem.value?.organizer)
                if (selectedOrganizerPos != null)
                        spinner.setSelection(selectedOrganizerPos)
            }

        })


        binding.dtstartTime.setOnClickListener {
            showDatepicker()
        }

        binding.dtstartYear.setOnClickListener {
            showDatepicker()
        }

        binding.dtstartMonth.setOnClickListener {
            showDatepicker()
        }

        binding.dtstartDay.setOnClickListener {
            showDatepicker()
        }

        // Transform the category input into a chip when the Add-Button is clicked
        // If the user entered multiple categories separated by comma, the values will be split in multiple categories

        binding.categoriesAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            val addedCategories: List<String> = convertCategoriesCSVtoList(binding.categoriesAdd.editText?.text.toString())
            addChips(addedCategories)
            binding.categoriesAdd.editText?.text?.clear()
        }



        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.categoriesAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val addedCategories: List<String> = convertCategoriesCSVtoList(binding.categoriesAdd.editText?.text.toString())
                    addChips(addedCategories)
                    binding.categoriesAdd.editText?.text?.clear()
                    true
                }
                else -> false
            }
        }



        binding.statusChip.setOnClickListener {

            val statusItems = resources.getStringArray(R.array.vjournal_status)
            val checkedStatus = vJournalItemEditViewModel.vJournalItem.value!!.status

            MaterialAlertDialogBuilder(context!!)
                    //.setTitle(resources.getString(R.string.title))
                    .setTitle("Set status")
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        // Respond to neutral button press
                        vJournalItemEditViewModel.statusChanged = vJournalItemEditViewModel.vJournalItem.value!!.status  // Reset to previous status
                        binding.statusChip.text = statusItems[checkedStatus]   // don't forget to update the UI
                    }
                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                        // Respond to positive button press, ATTENTION: "which" returns here -1 as this is the INT-value of the positive button!
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(statusItems, checkedStatus) { dialog, which ->
                        // Respond to item chosen
                        vJournalItemEditViewModel.statusChanged = which
                        binding.statusChip.text = statusItems[which]     // don't forget to update the UI
                    }
                    .show()
        }




        binding.classificationChip.setOnClickListener {

            val classificationItems = resources.getStringArray(R.array.vjournal_classification)
            val checkedClassification = vJournalItemEditViewModel.vJournalItem.value!!.classification

            MaterialAlertDialogBuilder(context!!)
                    //.setTitle(resources.getString(R.string.title))
                    .setTitle("Set classification")
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        // Respond to neutral button press
                        vJournalItemEditViewModel.classificationChanged = vJournalItemEditViewModel.vJournalItem.value!!.classification  // Reset to previous classification
                        binding.classificationChip.text = classificationItems[checkedClassification]   // don't forget to update the UI
                    }
                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                        // Respond to positive button press
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(classificationItems, checkedClassification) { dialog, which ->
                        // Respond to item chosen
                        vJournalItemEditViewModel.classificationChanged = which
                        binding.classificationChip.text = classificationItems[which]     // don't forget to update the UI
                    }
                    .show()
        }



        /*
        val statusSpinner = binding.statusSpinner
        statusSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                when(pos) {
                    0 -> vJournalItemEditViewModel.statusChanged = "DRAFT"
                    1 -> vJournalItemEditViewModel.statusChanged = "FINAL"
                    2 -> vJournalItemEditViewModel.statusChanged = "CANCELLED"
                    // no else here! if external data is not one of those 3, the spinner is not shown (see observer)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            //do nothing
            }
        }

        val classificationSpinner = binding.classificationSpinner
        classificationSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // An item was selected. You can retrieve the selected item using
                when(pos) {
                    0 -> vJournalItemEditViewModel.classificationChanged = "PUBLIC"
                    1 -> vJournalItemEditViewModel.classificationChanged = "PRIVATE"
                    2 -> vJournalItemEditViewModel.classificationChanged = "CONFIDENTIAL"
                    // no else here! if external data is not one of those 3, the spinner is not shown (see observer)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // do nothing
            }
        }

         */


        binding.urlEdit.editText?.setOnFocusChangeListener { view, hasFocus ->
                if((!binding.urlEdit.editText?.text.isNullOrEmpty() && !isValidURL(binding.urlEdit.editText?.text.toString())))
                    vJournalItemEditViewModel.urlError.value = "Please enter a valid URL"

        }



        return binding.root
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)

        //var formattedTime = convertLongToTimeString(c.timeInMillis)
        //Log.println(Log.INFO, "OnTimeSet", "Here are the values: $formattedTime")

        binding.dtstartTime.text = convertLongToTimeString(c.timeInMillis)

        vJournalItemEditViewModel.dtstartChangedHour = hourOfDay
        vJournalItemEditViewModel.dtstartChangedMinute = minute

    }



    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        //var formattedDate = convertLongToDateString(c.timeInMillis)
        //Log.println(Log.INFO, "OnTimeSet", "Here are the values: $formattedDate")    }

        binding.dtstartYear.text = convertLongToYearString(c.timeInMillis)
        binding.dtstartMonth.text = convertLongToMonthString(c.timeInMillis)
        binding.dtstartDay.text = convertLongToDayString(c.timeInMillis)

        vJournalItemEditViewModel.dtstartChangedYear = year
        vJournalItemEditViewModel.dtstartChangedMonth = month
        vJournalItemEditViewModel.dtstartChangedDay = day

        showTimepicker()

    }

    fun showDatepicker() {
        val c = Calendar.getInstance()
        c.timeInMillis = vJournalItemEditViewModel.vJournalItem.value?.dtstart!!

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(activity!!, this, year, month, day).show()
    }

    fun showTimepicker() {
        val c = Calendar.getInstance()
        c.timeInMillis = vJournalItemEditViewModel.vJournalItem.value?.dtstart!!

        val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        TimePickerDialog(activity, this, hourOfDay, minute, is24HourFormat(activity)).show()
    }


    fun addChips(categories: List<String>) {

        categories.forEach() { category ->

            if (category == "")
                return@forEach

            vJournalItemEditViewModel.categoriesListChanged.add(category)

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_edit_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category
            binding.categoriesChipgroup.addView(categoryChip)

            categoryChip.setOnClickListener {
                // Responds to chip click
            }

            categoryChip.setOnCloseIconClickListener { chip ->
                // Responds to chip's close icon click if one is present
                vJournalItemEditViewModel.categoriesListChanged.remove(category)
                chip.visibility = View.GONE
            }

            categoryChip.setOnCheckedChangeListener { chip, isChecked ->
                // Responds to chip checked/unchecked
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_delete) {
            vJournalItemEditViewModel.deleteClicked()
        }
        return super.onOptionsItemSelected(item)
    }
}

