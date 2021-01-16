package at.bitfire.notesx5.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.R
import at.bitfire.notesx5.convertLongToDateString
import at.bitfire.notesx5.convertLongToTimeString
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.relations.ICalEntity
import at.bitfire.notesx5.databinding.FragmentVjournalItemBinding
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_vjournal_edit_comment.view.*
import kotlinx.android.synthetic.main.fragment_vjournal_item_relatedto.view.*


class VJournalItemFragment : Fragment() {

    lateinit var binding: FragmentVjournalItemBinding
    lateinit var application: Application
    lateinit var inflater: LayoutInflater
    lateinit var dataSource: ICalDatabaseDao
    lateinit var viewModelFactory: VJournalItemViewModelFactory
    lateinit var vJournalItemViewModel: VJournalItemViewModel


    val allContactsWithName: MutableList<String> = mutableListOf()
    val allContactsWithNameAndMail: MutableList<String> = mutableListOf()
    val allContactsAsAttendee: MutableList<Attendee> = mutableListOf()




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentVjournalItemBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

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
                        VJournalItemFragmentDirections.actionVjournalItemFragmentToVJournalItemEditFragment(vJournalItemViewModel.vJournal.value!!)
                )
            }
        })

        vJournalItemViewModel.vJournal.observe(viewLifecycleOwner, {

            if (it?.vJournal != null) {

                val statusArray = resources.getStringArray(R.array.ical_status)
                binding.statusChip.text = statusArray[vJournalItemViewModel.vJournal.value!!.vJournal.status]

                val classificationArray = resources.getStringArray(R.array.ical_classification)
                binding.classificationChip.text = classificationArray[vJournalItemViewModel.vJournal.value!!.vJournal.classification]


                binding.commentsLinearlayout.removeAllViews()
                vJournalItemViewModel.vJournal.value!!.comment?.forEach { comment ->
                    val commentView = inflater.inflate(R.layout.fragment_vjournal_edit_comment, container, false);
                    commentView.comment_textview.text = comment.text
                    binding.commentsLinearlayout.addView(commentView)
                }
            }
        })



        vJournalItemViewModel.relatedICalObjects.observe(viewLifecycleOwner, {

            if (it?.size != 0)
            {
                binding.feedbackLinearlayout.removeAllViews()
                it.forEach { relatedICalObject ->
                    val relatedView = inflater.inflate(R.layout.fragment_vjournal_item_relatedto, container, false);
                    relatedView.related_textview.text = relatedICalObject?.summary
                    binding.feedbackLinearlayout.addView(relatedView)
                }
            }
        })


            /*

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


             */

        vJournalItemViewModel.categories.observe(viewLifecycleOwner, {
            binding.categoriesChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.forEach { category ->
                addCategoryChip(category)
            }
        })

        vJournalItemViewModel.attendees.observe(viewLifecycleOwner, {
            binding.attendeeChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.forEach { attendee ->
                addAttendeeChip(attendee)
            }
        })


        binding.addFeedback.setOnClickListener {

            val newNote = TextInputEditText(context!!)
            newNote.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            newNote.isSingleLine = false;
            newNote.maxLines = 8

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add feedback / note")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(newNote)

            builder.setPositiveButton("Save") { _, _ ->
                vJournalItemViewModel.insertRelatedNote(ICalObject(component = "NOTE", summary = newNote.text.toString()))
            }

            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
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



    private fun addCategoryChip(category: Category) {

        if (category.text.isBlank())     // don't add empty categories
            return

        val categoryChip = inflater.inflate(R.layout.fragment_vjournal_item_categories_chip, binding.categoriesChipgroup, false) as Chip
        categoryChip.text = category.text
        binding.categoriesChipgroup.addView(categoryChip)

        categoryChip.setOnClickListener {
            val selectedCategoryArray = arrayOf(category.text)     // convert to array
            // Responds to chip click
            this.findNavController().navigate(
                    VJournalItemFragmentDirections.actionVjournalItemFragmentToVjournalListFragmentList().setCategory2filter(selectedCategoryArray)
            )
        }


    }



    private fun addAttendeeChip(attendee: Attendee) {

        val attendeeChip = inflater.inflate(R.layout.fragment_vjournal_item_attendees_chip, binding.attendeeChipgroup, false) as Chip
        attendeeChip.text = attendee.caladdress
        when (attendee.roleparam) {
            0 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_chair, null)
            1 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
            2 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_optparticipant, null)
            3 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_nonparticipant, null)
            else -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
        }
        binding.attendeeChipgroup.addView(attendeeChip)

        attendeeChip.setOnClickListener {
            // Responds to chip click
        }

        attendeeChip.setOnCloseIconClickListener { chip ->
            //vJournalEditViewModel.categoryDeleted.add(attendee)  // add the category to the list for categories to be deleted
        }

        attendeeChip.setOnCheckedChangeListener { chip, isChecked ->
            // Responds to chip checked/unchecked
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
}

