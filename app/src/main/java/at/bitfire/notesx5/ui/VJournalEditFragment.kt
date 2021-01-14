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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.ICalDatabase
import at.bitfire.notesx5.database.ICalDatabaseDao
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Comment
import at.bitfire.notesx5.databinding.FragmentVjournalEditBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_vjournal_edit_comment.view.*
import kotlinx.android.synthetic.main.fragment_vjournal_item.*
import kotlinx.android.synthetic.main.fragment_vjournal_item_categories_chip.view.*
import java.util.*


class VJournalEditFragment : Fragment(),
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    lateinit var binding: FragmentVjournalEditBinding
    lateinit var application: Application
    lateinit var dataSource: ICalDatabaseDao
    lateinit var viewModelFactory: VJournalEditViewModelFactory
    lateinit var vJournalEditViewModel: VJournalEditViewModel
    lateinit var inflater: LayoutInflater

    val allContactsMail: MutableList<String> = mutableListOf()
    val allContactsNameAndMail: MutableList<String> = mutableListOf()

    var displayedCategoryChips = mutableListOf<Category>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this.binding = FragmentVjournalEditBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = VJournalEditFragmentArgs.fromBundle((arguments!!))

        val statusItems = resources.getStringArray(R.array.ical_status)
        val classificationItems = resources.getStringArray(R.array.ical_classification)


        // add menu
        setHasOptionsMenu(true)

        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            //request for permission to load contacts
            MaterialAlertDialogBuilder(context!!)
                    .setTitle("App Permission")
                    .setMessage("NOTESx5 can propose Attendee, Contact and Organizer data as input values for your entries. Read Permissions on your contacts is needed to enable this feature.")
                    .setPositiveButton("Ok") { dialog, which ->
                        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_CONTACTS), CONTACT_READ_PERMISSION_CODE)
                    }
                    .setNegativeButton("Cancel") { dialog, which -> }
                    .show()
        }


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


        binding.statusChip.text = statusItems[1]   // Set default of status Chip to 1 (=FINAL), might be overwritten by observer, but sets the default for new items
        binding.classificationChip.text = classificationItems[0]   // Set default of classification Chip to 0 (=PUBLIC), might be overwritten by observer, but sets the default for new items


        vJournalEditViewModel.savingClicked.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                vJournalEditViewModel.vJournalUpdated.value!!.summary = binding.summaryEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.description = binding.descriptionEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.collection = binding.collection.selectedItem.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.url = binding.urlEdit.editText?.text.toString()
                //vJournalEditViewModel.vJournalUpdated.value!!.attendee = binding.attendeeEdit.editText?.text.toString()
                vJournalEditViewModel.vJournalUpdated.value!!.contact = binding.contactEdit.editText?.text.toString()
                //vJournalEditViewModel.vJournalUpdated.value!!.related = binding.relatedtoEdit.editText?.text.toString()

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

            //TODO: Check if the Sequence was updated in the meantime and notify user!

            if (it?.vJournal == null || it.category == null)
                return@observe

            vJournalEditViewModel.vJournalUpdated.postValue(it.vJournal)

            binding.commentsLinearlayout.removeAllViews()
            vJournalEditViewModel.vJournalItem.value?.comment?.forEach { singleComment ->
                addCommentView(singleComment, container)
            }

            binding.categoriesChipgroup.removeAllViews()
            vJournalEditViewModel.vJournalItem.value?.category?.forEach { singleCategory ->
                addCategoryChip(singleCategory)
            }

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
            if (vJournalEditViewModel.allCollections.value != null) {
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
            if (vJournalEditViewModel.allCollections.value != null) {
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
            vJournalEditViewModel.categoryUpdated.add(Category(text = binding.categoriesAdd.editText?.text.toString()))
            addCategoryChip(Category(text = binding.categoriesAdd.editText?.text.toString()))
            binding.categoriesAdd.editText?.text?.clear()

        }


        // Transform the category input into a chip when the Done button in the keyboard is clicked
        binding.categoriesAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    vJournalEditViewModel.categoryUpdated.add(Category(text = binding.categoriesAdd.editText?.text.toString()))
                    addCategoryChip(Category(text = binding.categoriesAdd.editText?.text.toString()))
                    binding.categoriesAdd.editText?.text?.clear()

                    true
                }
                else -> false
            }
        }

        binding.attendeesAddAutocomplete.setOnItemClickListener { adapterView, view, i, l ->
            //TODO
            val newAttendee = binding.attendeesAddAutocomplete.adapter.getItem(i).toString()
            addAttendeeChip(Attendee(caladdress = newAttendee))
            binding.attendeesAddAutocomplete.text.clear()
        }





        binding.commentAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            val newComment = Comment(text = binding.commentAdd.editText?.text.toString())
            vJournalEditViewModel.commentUpdated.add(newComment)    // store the comment for saving
            addCommentView(newComment, container)      // add the new comment
            binding.commentAdd.editText?.text?.clear()  // clear the field

        }


        // Transform the comment input into a view when the Done button in the keyboard is clicked
        binding.commentAdd.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val newComment = Comment(text = binding.commentAdd.editText?.text.toString())
                    vJournalEditViewModel.commentUpdated.add(newComment)    // store the comment for saving
                    addCommentView(newComment, container)      // add the new comment
                    binding.commentAdd.editText?.text?.clear()  // clear the field
                    true
                }
                else -> false
            }
        }


        binding.statusChip.setOnClickListener {

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set status")
                    .setItems(statusItems) { dialog, which ->
                        // Respond to item chosen
                        vJournalEditViewModel.vJournalUpdated.value!!.status = which
                        binding.statusChip.text = statusItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_status)
                    .show()
        }


        binding.classificationChip.setOnClickListener {

            val classificationItems = resources.getStringArray(R.array.ical_classification)

            MaterialAlertDialogBuilder(context!!)
                    .setTitle("Set classification")
                    .setItems(classificationItems) { dialog, which ->
                        // Respond to item chosen
                        vJournalEditViewModel.vJournalUpdated.value!!.classification = which
                        binding.classificationChip.text = classificationItems[which]     // don't forget to update the UI
                    }
                    .setIcon(R.drawable.ic_classification)
                    .show()
        }


        binding.urlEdit.editText?.setOnFocusChangeListener { view, hasFocus ->
            if ((!binding.urlEdit.editText?.text.isNullOrEmpty() && !isValidURL(binding.urlEdit.editText?.text.toString())))
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


    private fun addCategoryChip(category: Category) {

            if (category.text.isNullOrBlank())
                return

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_edit_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category.text
            binding.categoriesChipgroup.addView(categoryChip)
            displayedCategoryChips.add(category)

            categoryChip.setOnClickListener {
                // Responds to chip click
            }

            categoryChip.setOnCloseIconClickListener { chip ->
                vJournalEditViewModel.categoryDeleted.add(category)  // add the category to the list for categories to be deleted
                chip.visibility = View.GONE
            }

            categoryChip.setOnCheckedChangeListener { chip, isChecked ->
                // Responds to chip checked/unchecked
            }
    }


    private fun addAttendeeChip(attendee: Attendee) {

        val attendeeChip = inflater.inflate(R.layout.fragment_vjournal_edit_attendees_chip, binding.attendeesChipgroup, false) as Chip
        attendeeChip.text = attendee.caladdress
        binding.attendeesChipgroup.addView(attendeeChip)

        attendeeChip.setOnClickListener {
            // Responds to chip click
        }

        attendeeChip.setOnCloseIconClickListener { chip ->
            //vJournalEditViewModel.categoryDeleted.add(attendee)  // add the category to the list for categories to be deleted
            chip.visibility = View.GONE
        }

        attendeeChip.setOnCheckedChangeListener { chip, isChecked ->
            // Responds to chip checked/unchecked
        }
    }


    private fun addCommentView(comment: Comment, container: ViewGroup?) {

            val commentView = inflater.inflate(R.layout.fragment_vjournal_edit_comment, container, false);
            commentView.comment_textview.text = comment.text
            binding.commentsLinearlayout.addView(commentView)

            // set on Click Listener to open a dialog to update the comment
            commentView.setOnClickListener {

                // set up the values for the TextInputEditText
                val updatedText: TextInputEditText = TextInputEditText(context!!)
                updatedText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                updatedText.setText(comment.text)
                updatedText.isSingleLine = false;
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
                    vJournalEditViewModel.commentUpdated.add(updatedComment)
                    it.comment_textview.text = updatedComment.text
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    // Do nothing, just close the message
                }

                builder.setNeutralButton("Delete") { _, _ ->
                    vJournalEditViewModel.commentDeleted.add(comment)
                    it.visibility = View.GONE
                }
                builder.show()
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

        binding.contactAddAutocomplete.setAdapter(arrayAdapterNameAndMail)
        binding.attendeesAddAutocomplete.setAdapter(arrayAdapterNameAndMail)


    }
}
