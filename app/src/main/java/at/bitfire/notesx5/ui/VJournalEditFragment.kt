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
import at.bitfire.notesx5.database.VCategory
import at.bitfire.notesx5.database.VJournalDatabase
import at.bitfire.notesx5.database.VJournalDatabaseDao
import at.bitfire.notesx5.databinding.FragmentVjournalItemEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_vjournal_item_categories_chip.view.*
import java.util.*


class VJournalEditFragment : Fragment(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    lateinit var binding: FragmentVjournalItemEditBinding
    lateinit var application: Application
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory:  VJournalEditViewModelFactory
    lateinit var vJournalEditViewModel: VJournalEditViewModel
    lateinit var inflater: LayoutInflater

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentVjournalItemEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalEditFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)


        this.viewModelFactory = VJournalEditViewModelFactory(arguments.item2edit, dataSource, application)
        vJournalEditViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalEditViewModel::class.java)

        binding.model = vJournalEditViewModel
        binding.lifecycleOwner = this

/*
        if(arguments.item2edit == 0L) {
            when (arguments.component4new) {
                "JOURNAL" -> vJournalEditViewModel.vJournalUpdated.value!!.component = "JOURNAL"
                "NOTE" -> vJournalEditViewModel.vJournalUpdated.value!!.component = "NOTE"
            }
        }
*/

        val statusItems = resources.getStringArray(R.array.vjournal_status)
        binding.statusChip.text = statusItems[1]   // Set default of status Chip to 1 (=FINAL), might be overwritten by observer, but sets the default for new items
        val classificationItems = resources.getStringArray(R.array.vjournal_classification)
        binding.classificationChip.text = classificationItems[0]   // Set default of classification Chip to 0 (=PUBLIC), might be overwritten by observer, but sets the default for new items




        vJournalEditViewModel.savingClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                vJournalEditViewModel.vJournalUpdated.value!!.summary = binding.summaryEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.description = binding.descriptionEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.collection = binding.collection.selectedItem.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.organizer = binding.organizerEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.url = binding.urlEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.attendee = binding.attendeeEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.contact = binding.contactEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.related = binding.relatedtoEdit.editText?.text.toString()

                vJournalEditViewModel.update()
            }
        })

        vJournalEditViewModel.deleteClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {

                // show Alert Dialog before the item gets really deleted
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Delete \"${vJournalEditViewModel.vJournalItem.value!!.vJournal.summary}\"")
                builder.setMessage("Are you sure you want to delete \"${vJournalEditViewModel.vJournalItem.value!!.vJournal.summary}\"?")
                builder.setPositiveButton("Delete") { _, _ ->
                    val direction = VJournalEditFragmentDirections.actionVJournalItemEditFragmentToVjournalListFragmentList()
                    direction.component2show = vJournalEditViewModel.vJournalItem.value!!.vJournal.component

                    val summary = vJournalEditViewModel.vJournalItem.value!!.vJournal.summary
                    vJournalEditViewModel.delete()
                    Toast.makeText(context, "\"$summary\" successfully deleted.", Toast.LENGTH_LONG).show()

                    this.findNavController().navigate(direction)
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    // Do nothing, just close the message
                }

                builder.setNeutralButton("Mark as cancelled") { _, _ ->
                    vJournalEditViewModel.vJournalUpdated.value!!.status = 2    // 2 = CANCELLED
                    vJournalEditViewModel.savingClicked()

                    val summary = vJournalEditViewModel.vJournalItem.value!!.vJournal.summary
                    Toast.makeText(context, "\"$summary\" marked as Cancelled.", Toast.LENGTH_LONG).show()

                }

                builder.show()
            }
        })

        vJournalEditViewModel.returnVJournalItemId.observe(viewLifecycleOwner, Observer {
            if (it != 0L) {
                val direction = VJournalEditFragmentDirections.actionVJournalItemEditFragmentToVjournalListFragmentList()
                direction.component2show = vJournalEditViewModel.vJournalItem.value!!.vJournal.component
                direction.item2focus = it
                this.findNavController().navigate(direction)
            }
            vJournalEditViewModel.savingClicked.value = false
        })



        vJournalEditViewModel.vJournalItem.observe(viewLifecycleOwner, {

            if(it?.vJournal == null || it.vCategory == null)
                return@observe

            vJournalEditViewModel.vJournalUpdated.postValue(it.vJournal)
            vJournalEditViewModel.vCategoryUpdated.addAll(it.vCategory!!)


            // Add the chips for existing categories
            addChips(vJournalEditViewModel.vCategoryUpdated)

            // Set the default value of the Status Chip
            if (vJournalEditViewModel.vJournalItem.value?.vJournal?.status == -1)      // if unsupported don't show the status
                binding.statusChip.visibility = View.GONE
            else
                binding.statusChip.text = statusItems[vJournalEditViewModel.vJournalItem.value!!.vJournal.status]   // if supported show the status according to the String Array

            // Set the default value of the Classification Chip
            if (vJournalEditViewModel.vJournalItem.value?.vJournal?.classification == -1)      // if unsupported don't show the classification
                binding.classificationChip.visibility = View.GONE
            else
                binding.classificationChip.text = classificationItems[vJournalEditViewModel.vJournalItem.value!!.vJournal.classification]  // if supported show the classification according to the String Array

            // set the default selection for the spinner. The same snippet exists for the allOrganizers observer
            if(vJournalEditViewModel.allCollections.value != null) {
                val selectedCollectionPos = vJournalEditViewModel.allCollections.value?.indexOf(vJournalEditViewModel.vJournalItem.value?.vJournal?.collection)
                if (selectedCollectionPos != null)
                    binding.collection.setSelection(selectedCollectionPos)
            }
        })



        // Set up items to suggest for categories
        vJournalEditViewModel.allCategories.observe(viewLifecycleOwner, {
            // Create the adapter and set it to the AutoCompleteTextView
            if (vJournalEditViewModel.allCategories.value != null) {
                val arrayAdapter = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, vJournalEditViewModel.allCategories.value!!)
                binding.categoriesAddAutocomplete.setAdapter(arrayAdapter)
            }
        })

        vJournalEditViewModel.allCollections.observe(viewLifecycleOwner, {

            // set up the adapter for the organizer spinner
            val spinner: Spinner = binding.collection
            val adapter = ArrayAdapter<Any?>(context!!, android.R.layout.simple_spinner_item, vJournalEditViewModel.allCollections.value!!)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.setAdapter(adapter)

            // set the default selection for the spinner. The same snippet exists for the vJournalItem observer
            if(vJournalEditViewModel.allCollections.value != null) {
                val selectedCollectionPos = vJournalEditViewModel.allCollections.value?.indexOf(vJournalEditViewModel.vJournalItem.value?.vJournal?.collection)
                if (selectedCollectionPos != null)
                        spinner.setSelection(selectedCollectionPos)
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
            vJournalEditViewModel.vCategoryUpdated.add(VCategory(text=binding.categoriesAdd.editText?.text.toString()))
            addChips(listOf(VCategory(text = binding.categoriesAdd.editText?.text.toString())))
            binding.categoriesAdd.editText?.text?.clear()


            // TODO: SAVE added categories!!!
            //vJournalItemEditViewModel.vJournalItemUpdated.value!!.vCategory?.plus(category)
            // .add(category))
        }



        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.categoriesAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    vJournalEditViewModel.vCategoryUpdated.add(VCategory(text=binding.categoriesAdd.editText?.text.toString()))
                    addChips(listOf(VCategory(text = binding.categoriesAdd.editText?.text.toString())))
                    binding.categoriesAdd.editText?.text?.clear()


                    // TODO: SAVE added categories!!!
                    //vJournalItemEditViewModel.vJournalItemUpdated.value!!.vCategory?.plus(category)
                    // .add(category))

                    true
                }
                else -> false
            }
        }



        binding.statusChip.setOnClickListener {

            val statusItems = resources.getStringArray(R.array.vjournal_status)
            val checkedStatus = vJournalEditViewModel.vJournalItem.value!!.vJournal.status

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set status")
                    .setItems(statusItems) { dialog, which ->
                        // Respond to item chosen
                        vJournalEditViewModel.vJournalUpdated.value!!.status = which
                        binding.statusChip.text = statusItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_status)
                    .show()

            /*
            MaterialAlertDialogBuilder(context!!)
                    //.setTitle(resources.getString(R.string.title))
                    .setTitle("Set status")
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        // Respond to neutral button press
                        vJournalItemEditViewModel.vJournalUpdated.value!!.status = vJournalItemEditViewModel.vJournalItem.value!!.vJournal.status  // Reset to previous status
                        binding.statusChip.text = statusItems[checkedStatus]   // don't forget to update the UI
                    }
                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                        // Respond to positive button press, ATTENTION: "which" returns here -1 as this is the INT-value of the positive button!
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(statusItems, checkedStatus) { dialog, which ->
                        // Respond to item chosen
                        vJournalItemEditViewModel.vJournalUpdated.value!!.status = which
                        binding.statusChip.text = statusItems[which]     // don't forget to update the UI
                    }
                    .show()

             */
        }




        binding.classificationChip.setOnClickListener {

            val classificationItems = resources.getStringArray(R.array.vjournal_classification)
            val checkedClassification = vJournalEditViewModel.vJournalItem.value!!.vJournal.classification



            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set classification")
                    .setItems(classificationItems) { dialog, which ->
                        // Respond to item chosen
                        vJournalEditViewModel.vJournalUpdated.value!!.classification = which
                        binding.classificationChip.text = classificationItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_classification)
                    .show()

            /*

            MaterialAlertDialogBuilder(context!!)
                    //.setTitle(resources.getString(R.string.title))
                    .setTitle("Set classification")
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        // Respond to neutral button press
                        vJournalItemEditViewModel.vJournalUpdated.value!!.classification = vJournalItemEditViewModel.vJournalItem.value!!.vJournal.classification  // Reset to previous classification
                        binding.classificationChip.text = classificationItems[checkedClassification]   // don't forget to update the UI
                    }
                    .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                        // Respond to positive button press
                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(classificationItems, checkedClassification) { dialog, which ->
                        // Respond to item chosen
                        vJournalItemEditViewModel.vJournalUpdated.value!!.classification = which
                        binding.classificationChip.text = classificationItems[which]     // don't forget to update the UI
                    }
                    .show()

             */
        }




        binding.urlEdit.editText?.setOnFocusChangeListener { view, hasFocus ->
                if((!binding.urlEdit.editText?.text.isNullOrEmpty() && !isValidURL(binding.urlEdit.editText?.text.toString())))
                    vJournalEditViewModel.urlError.value = "Please enter a valid URL"

        }



        return binding.root
    }


    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        val c = Calendar.getInstance()
        c.timeInMillis = vJournalEditViewModel.vJournalUpdated.value?.dtstart!!

        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month)
        c.set(Calendar.DAY_OF_MONTH, day)
        //var formattedDate = convertLongToDateString(c.timeInMillis)
        //Log.println(Log.INFO, "OnTimeSet", "Here are the values: $formattedDate")    }

        binding.dtstartYear.text = convertLongToYearString(c.timeInMillis)
        binding.dtstartMonth.text = convertLongToMonthString(c.timeInMillis)
        binding.dtstartDay.text = convertLongToDayString(c.timeInMillis)

        vJournalEditViewModel.vJournalUpdated.value!!.dtstart = c.timeInMillis

        showTimepicker()

    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val c = Calendar.getInstance()
        c.timeInMillis = vJournalEditViewModel.vJournalUpdated.value?.dtstart!!
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)

        //var formattedTime = convertLongToTimeString(c.timeInMillis)
        //Log.println(Log.INFO, "OnTimeSet", "Here are the values: $formattedTime")

        binding.dtstartTime.text = convertLongToTimeString(c.timeInMillis)

        vJournalEditViewModel.vJournalUpdated.value!!.dtstart = c.timeInMillis

    }




    fun showDatepicker() {
        val c = Calendar.getInstance()
        c.timeInMillis = vJournalEditViewModel.vJournalUpdated.value?.dtstart!!

        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(activity!!, this, year, month, day).show()
    }

    fun showTimepicker() {
        val c = Calendar.getInstance()
        c.timeInMillis = vJournalEditViewModel.vJournalUpdated.value?.dtstart!!

        val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        TimePickerDialog(activity, this, hourOfDay, minute, is24HourFormat(activity)).show()
    }


    private fun addChips(categories: List<VCategory>?) {

        categories?.forEach() { category ->

            if (category.text.isBlank())
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_edit_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category.text
            binding.categoriesChipgroup.addView(categoryChip)

            categoryChip.setOnClickListener {
                // Responds to chip click
            }

            categoryChip.setOnCloseIconClickListener { chip ->
                // Responds to chip's close icon click if one is present
                // Delete by re-assigning an edited, mutable category list
                // TODO: Delete the category from the list!!!
                val currentCategories = vJournalEditViewModel.vCategoryUpdated.removeIf { it.text == category.text}
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
            vJournalEditViewModel.deleteClicked()
        }
        return super.onOptionsItemSelected(item)
    }
}

