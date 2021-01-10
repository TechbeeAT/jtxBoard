package at.bitfire.notesx5.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.R
import at.bitfire.notesx5.convertLongToDateString
import at.bitfire.notesx5.convertLongToTimeString
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_vjournal_item_comment.view.*
import java.util.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var inflater: LayoutInflater
    lateinit var dataSource: VJournalDatabaseDao
    lateinit var viewModelFactory: VJournalItemViewModelFactory
    lateinit var vJournalItemViewModel: VJournalItemViewModel


    var displayedCategoryChips = mutableListOf<VCategory>()

    val allContactsWithName: MutableList<String> = mutableListOf()
    val allContactsWithNameAndMail: MutableList<String> = mutableListOf()
    val allContactsAsAttendee: MutableList<VAttendee> = mutableListOf()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentVjournalItemBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = VJournalDatabase.getInstance(application).vJournalDatabaseDao

        val arguments = VJournalItemFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)




        // set up view model
        viewModelFactory = VJournalItemViewModelFactory(arguments.item2show, dataSource, application)
        vJournalItemViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(VJournalItemViewModel::class.java)

        binding.model = vJournalItemViewModel
        binding.lifecycleOwner = this




        // set up observers
        vJournalItemViewModel.editingClicked.observe(viewLifecycleOwner, Observer {
            if (it) {
                vJournalItemViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment().setItem2edit(vJournalItemViewModel.vJournal.value!!.vJournal.id))
            }
        })

        vJournalItemViewModel.vJournal.observe(viewLifecycleOwner, {

            if (it?.vJournal != null) {

                val statusArray = resources.getStringArray(R.array.vjournal_status)
                binding.statusChip.text = statusArray[vJournalItemViewModel.vJournal.value!!.vJournal.status]

                val classificationArray = resources.getStringArray(R.array.vjournal_classification)
                binding.classificationChip.text = classificationArray[vJournalItemViewModel.vJournal.value!!.vJournal.classification]


                binding.commentsLinearlayout.removeAllViews()
                vJournalItemViewModel.vJournal.value!!.vComment?.forEach { comment ->
                    val commentView = inflater.inflate(R.layout.fragment_vjournal_item_comment, container, false);
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
                            vJournalItemViewModel.upsertComment(updatedComment)
                        }
                        builder.setNegativeButton("Cancel") { _, _ ->
                            // Do nothing, just close the message
                        }

                        builder.setNeutralButton("Delete") { _, _ ->
                            vJournalItemViewModel.deleteComment(comment)
                            Snackbar.make(this.view!!, "Comment deleted: ${comment.text}", Snackbar.LENGTH_LONG).show()
                        }

                        builder.show()
                    }

                }


            }
        })

        vJournalItemViewModel.vCategory.observe(viewLifecycleOwner, {
            if (it != null)
                addChips(vJournalItemViewModel.vJournal.value!!.vCategory!!)
        })


        binding.addComment.setOnClickListener {

            val newComment: TextInputEditText = TextInputEditText(context!!)
            newComment.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            newComment.isSingleLine = false;
            newComment.maxLines = 8

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add comment")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(newComment)

            builder.setPositiveButton("Save") { _, _ ->
               vJournalItemViewModel.upsertComment(VComment(journalLinkId = vJournalItemViewModel.vJournal.value!!.vJournal.id, text = newComment.text.toString()))
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.show()

        }


        binding.urlAddButton.setOnClickListener{

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Set URL")
            builder.setIcon(R.drawable.ic_url_add)

           // val dialog = R.layout.fragment_vjournal_item_dialog_url
            val dialog = inflater.inflate(R.layout.fragment_vjournal_item_dialog_url, null)
            val editText = dialog.findViewById<EditText>(R.id.url_dialog_edittext)

            editText.setText(vJournalItemViewModel.vJournal.value?.vJournal?.url)

            builder.setView(dialog)


            builder.setPositiveButton("Save") { _, _ ->
                vJournalItemViewModel.updateUrl(editText.text.toString())
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            if (!vJournalItemViewModel.vJournal.value!!.vJournal.url.isNullOrBlank())
            {
                builder.setNeutralButton("Delete") { _, _ ->
                    vJournalItemViewModel.updateUrl("")
                }
            }

            builder.show()
        }

        binding.contactAddButton.setOnClickListener{

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Set Contact")
            builder.setIcon(R.drawable.ic_contact)

            val dialog = inflater.inflate(R.layout.fragment_vjournal_item_dialog_contact, null)
            val editText = dialog.findViewById<EditText>(R.id.contact_dialog_edittext)
            editText.setText(vJournalItemViewModel.vJournal.value?.vJournal?.contact)

            builder.setView(dialog)


            builder.setPositiveButton("Save") { _, _ ->
                vJournalItemViewModel.updateContact(editText.text.toString())
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            if (!vJournalItemViewModel.vJournal.value!!.vJournal.contact.isNullOrBlank())
            {
                builder.setNeutralButton("Delete") { _, _ ->
                    vJournalItemViewModel.updateContact("")
                }
            }

            builder.show()
        }


/*
        binding.attendeeAddButton.setOnClickListener{

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Set Attendees")
            builder.setIcon(R.drawable.ic_attendee)

            val dialog = inflater.inflate(R.layout.fragment_vjournal_item_dialog_attendee, null)
            val editText = dialog.findViewById<AutoCompleteTextView>(R.id.attendee_add_autocompletetextview)
            val attendeesAddChipgroup = dialog.findViewById<ChipGroup>(R.id.attendees_add_chipgroup)
            editText.setAdapter(loadContacts())

            builder.setView(dialog)


            builder.setPositiveButton("Save") { _, _ ->
                //vJournalItemViewModel.upsertComment(VComment(journalLinkId = vJournalItemViewModel.vJournal.value!!.vAttendee., text = editText.text.toString()))
            }
            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            if (!vJournalItemViewModel.vJournal.value!!.vAttendee.isNullOrEmpty())
            {
                builder.setNeutralButton("Delete") { _, _ ->
                    // Do nothing, just close the message
                }
            }

            builder.show()


            editText.setOnItemClickListener(AdapterView.OnItemClickListener { parent, arg1, pos, id ->
                val item: String = parent.getItemAtPosition(pos) as String
                Log.println(Log.INFO, "AutoCompleteValues", "Position: $pos, Id: $id, String: $item")


                val attendeeChip = inflater.inflate(R.layout.fragment_vjournal_item_chip_person, attendeesAddChipgroup, false) as Chip
                attendeeChip.text = editText.text
                attendeesAddChipgroup.addView(attendeeChip)


                attendeeChip.setOnCloseIconClickListener { chip ->
                    // Responds to chip's close icon click if one is present
                    // Delete by re-assigning an edited, mutable category list
                    // TODO: Delete the category from the list!!!
                    //val currentCategories = vJournalEditViewModel.vCategoryUpdated.removeIf { it.text == category.text}
                    chip.visibility = View.GONE
                }

                editText.text.clear()
            })


            // Transform the category input into a chip when the Done button in the keyboard is clicked
            editText.setOnEditorActionListener { v, actionId, event ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        //vJournalEditViewModel.vCategoryUpdated.add(VCategory(text = binding.categoriesAdd.editText?.text.toString()))
                        //addChips(listOf(VCategory(text = binding.categoriesAdd.editText?.text.toString())))

                        val attendeeChip = inflater.inflate(R.layout.fragment_vjournal_item_chip_person, attendeesAddChipgroup, false) as Chip
                        attendeeChip.text = editText.text
                        attendeesAddChipgroup.addView(attendeeChip)


                        attendeeChip.setOnCloseIconClickListener { chip ->
                            // Responds to chip's close icon click if one is present
                            // Delete by re-assigning an edited, mutable category list
                            // TODO: Delete the category from the list!!!
                            //val currentCategories = vJournalEditViewModel.vCategoryUpdated.removeIf { it.text == category.text}
                            chip.visibility = View.GONE
                        }



                        editText.text.clear()


                        // TODO: SAVE added categories!!!
                        //vJournalItemEditViewModel.vJournalItemUpdated.value!!.vCategory?.plus(category)
                        // .add(category))

                        true
                    }
                    else -> false
                }
            }



 */

/*

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

 */



        return binding.root
    }

    // adds Chips to the categoriesChipgroup based on the categories List
    private fun addChips(categories: List<VCategory>) {

        categories.forEach() { category ->

            if (category.text.isBlank())     // don't add empty categories
                return@forEach

            if(displayedCategoryChips.indexOf(category) != -1)    // only show categories that are not there yet
                return@forEach

            val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
            categoryChip.text = category.text
            binding.categoriesChipgroup.addView(categoryChip)
            displayedCategoryChips.add(category)

            categoryChip.setOnClickListener {

                val selectedCategoryArray = arrayOf(category.text)     // convert to array
                // Responds to chip click
                this.findNavController().navigate(
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVjournalListFragmentList().setCategory2filter(selectedCategoryArray)
                )
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vjournal_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.vjournal_item_share) {

            var shareText: String = "${convertLongToDateString(vJournalItemViewModel.vJournal.value!!.vJournal.dtstart)} ${convertLongToTimeString(vJournalItemViewModel.vJournal.value!!.vJournal.dtstart)}\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournal.summary}\n\n"
            shareText += "${vJournalItemViewModel.vJournal.value!!.vJournal.description}\n\n"
            //todo add category again
            //shareText += "Categories/Labels: ${vJournalItemViewModel.vJournal.value!!.vCategory}"

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, vJournalItemViewModel.vJournal.value!!.vJournal.summary)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent(shareIntent))


        }
        return super.onOptionsItemSelected(item)
    }




    /*

    fun loadContacts():  ArrayAdapter<String> {


        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_CONTACTS), 1)


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
                Log.println(Log.INFO, "cursor: ", "$name: $emlAddr")
                allContactsWithNameAndMail.add("$name ($emlAddr)")
                allContactsAsAttendee.add(VAttendee(cnparam = name, attendee = emlAddr))

            }
            cur.close()

        }

        val arrayAdapter = ArrayAdapter<String>(application.applicationContext, android.R.layout.simple_list_item_1, allContactsWithNameAndMail)
       // binding.organizerAddAutocomplete.setAdapter(arrayAdapter)
        return arrayAdapter

    }


     */
}

