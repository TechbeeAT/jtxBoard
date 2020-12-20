package com.example.android.vjournalcalendar.ui

import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.android.vjournalcalendar.*
import com.example.android.vjournalcalendar.database.VJournalDatabase
import com.example.android.vjournalcalendar.database.VJournalDatabaseDao
import com.example.android.vjournalcalendar.database.vJournalItem
import com.example.android.vjournalcalendar.databinding.FragmentVjournalItemEditBinding
import com.google.android.material.chip.Chip
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
                              savedInstanceState: Bundle?): View? {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentVjournalItemEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemEditFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)


        this.viewModelFactory = VJournalItemEditViewModelFactory(arguments.vJournalItemEditId, dataSource, application)
        vJournalItemEditViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemEditViewModel::class.java)

        binding.vJournalItemEditViewModel = vJournalItemEditViewModel
        binding.lifecycleOwner = this



        vJournalItemEditViewModel.savingClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                vJournalItemEditViewModel.summaryChanged = binding.summaryEdit.text.toString()
                vJournalItemEditViewModel.descriptionChanged = binding.descriptionEdit.text.toString()
                vJournalItemEditViewModel.urlChanged = binding.urlEdit.editText?.text.toString()
                vJournalItemEditViewModel.attendeeChanged = binding.urlEdit.editText?.text.toString()
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
                builder.setNegativeButton("Cancel") {_, _ ->
                    // Do nothing, just close the message
                }

                builder.setNeutralButton("Mark as cancelled") { _, _ ->
                    vJournalItemEditViewModel.statusChanged = "CANCELLED"
                    vJournalItemEditViewModel.savingClicked()

                    var summary = vJournalItemEditViewModel.vJournalItem.value!!.summary
                    Toast.makeText(context, "\"$summary\" marked as Cancelled.", Toast.LENGTH_LONG).show()

                }

                builder.show()
            }
        })

        vJournalItemEditViewModel.returnVJournalItemId.observe(viewLifecycleOwner, Observer {
            if (it != 0L) {
                this.findNavController().navigate(VJournalItemEditFragmentDirections.actionVJournalItemEditFragmentToVjournalListFragmentList().setVJournalItemId(it))
                //this.findNavController().popBackStack(R.id.VJournalItemEditFragment, true)
            }
            vJournalItemEditViewModel.savingClicked.value = false
        })


        vJournalItemEditViewModel.vJournalItem.observe(viewLifecycleOwner, {

            // Add the chips for existing categories
            if (vJournalItemEditViewModel.vJournalItem.value != null)
                addChips(convertCategoriesCSVtoList(vJournalItemEditViewModel.vJournalItem.value!!.categories))

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

        })

        // Set up items to suggest for categories
        vJournalItemEditViewModel.allCategories.observe(viewLifecycleOwner, {
            // Create the adapter and set it to the AutoCompleteTextView
            if(vJournalItemEditViewModel.allCategories.value != null) {
                val allCategoriesCSV = convertCategoriesListtoCSVString(vJournalItemEditViewModel.allCategories.value!!.toMutableList())
                val allCategoriesList = convertCategoriesCSVtoList(allCategoriesCSV).distinct()
                val arrayAdapter = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allCategoriesList)
                binding.categoriesAdd.setAdapter(arrayAdapter)
            }

        })


        binding.dtstartTime.setOnClickListener {
            showDatepicker()
            showTimepicker()
        }

        binding.dtstartYear.setOnClickListener {
            showDatepicker()
            showTimepicker()
        }

        binding.dtstartMonth.setOnClickListener {
            showDatepicker()
            showTimepicker()
        }

        binding.dtstartDay.setOnClickListener {
            showDatepicker()
            showTimepicker()
        }

        // Transform the category input into a chip when the Add-Button is clicked
        // If the user entered multiple categories separated by comma, the values will be split in multiple categories
        binding.categoriesAddIcon.setOnClickListener{

            val addedCategories: List<String> = convertCategoriesCSVtoList(binding.categoriesAdd.text.toString())
            addChips(addedCategories)
            binding.categoriesAdd.text.clear()
        }

        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.categoriesAdd.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val addedCategories: List<String> = convertCategoriesCSVtoList(binding.categoriesAdd.text.toString())
                    addChips(addedCategories)
                    binding.categoriesAdd.text.clear()
                    true
                }
                else -> false
            }
        }


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

