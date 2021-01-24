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
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.databinding.FragmentIcalViewBinding
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_ical_view_comment.view.*
import kotlinx.android.synthetic.main.fragment_ical_view_relatedto.view.*
import kotlinx.android.synthetic.main.fragment_ical_view_subtask.view.*


class IcalViewFragment : Fragment() {

    lateinit var binding: FragmentIcalViewBinding
    lateinit var application: Application
    lateinit var inflater: LayoutInflater
    lateinit var dataSource: ICalDatabaseDao
    lateinit var viewModelFactory: IcalViewViewModelFactory
    lateinit var icalViewViewModel: IcalViewViewModel


    /*
    val allContactsWithName: MutableList<String> = mutableListOf()
    val allContactsWithNameAndMail: MutableList<String> = mutableListOf()
    val allContactsAsAttendee: MutableList<Attendee> = mutableListOf()


     */



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentIcalViewBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalViewFragmentArgs.fromBundle((arguments!!))

        // add menu
        setHasOptionsMenu(true)




        // set up view model
        viewModelFactory = IcalViewViewModelFactory(arguments.item2show, dataSource, application)
        icalViewViewModel =
                ViewModelProvider(
                        this, viewModelFactory).get(IcalViewViewModel::class.java)

        binding.model = icalViewViewModel
        binding.lifecycleOwner = this




        // set up observers
        icalViewViewModel.editingClicked.observe(viewLifecycleOwner,  {
            if (it) {
                icalViewViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.vJournal.value!!)
                )
            }
        })

        icalViewViewModel.vJournal.observe(viewLifecycleOwner, {

            if (it?.property != null) {

                val statusArray = if (icalViewViewModel.vJournal.value!!.property.component == "TODO")
                    resources.getStringArray(R.array.vtodo_status)
                else
                    resources.getStringArray(R.array.vjournal_status)

                binding.viewStatusChip.text = statusArray[icalViewViewModel.vJournal.value!!.property.status]

                val classificationArray = resources.getStringArray(R.array.ical_classification)
                binding.viewClassificationChip.text = classificationArray[icalViewViewModel.vJournal.value!!.property.classification]

                val priorityArray = resources.getStringArray(R.array.priority)
                if (icalViewViewModel.vJournal.value?.property?.priority != null && icalViewViewModel.vJournal.value!!.property.priority in 0..9)
                    binding.viewPriorityChip.text = priorityArray[icalViewViewModel.vJournal.value!!.property.priority!!]


                binding.viewCommentsLinearlayout.removeAllViews()
                icalViewViewModel.vJournal.value!!.comment?.forEach { comment ->
                    val commentView = inflater.inflate(R.layout.fragment_ical_view_comment, container, false)
                    commentView.view_comment_textview.text = comment.text
                    binding.viewCommentsLinearlayout.addView(commentView)
                }
            }
        })



        icalViewViewModel.relatedNotes.observe(viewLifecycleOwner, {

            if (it?.size != 0)
            {
                binding.viewFeedbackLinearlayout.removeAllViews()
                it.forEach { relatedICalObject ->
                    val relatedView = inflater.inflate(R.layout.fragment_ical_view_relatedto, container, false)
                    relatedView.view_related_textview.text = relatedICalObject?.summary
                    binding.viewFeedbackLinearlayout.addView(relatedView)
                }
            }
        })

        icalViewViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            binding.viewSubtasksLinearlayout.removeAllViews()
            it.forEach {singleSubtask ->
                addSubtasksView(singleSubtask, container)
            }
        }


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

        icalViewViewModel.categories.observe(viewLifecycleOwner, {
            binding.viewCategoriesChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.forEach { category ->
                addCategoryChip(category)
            }
        })

        icalViewViewModel.attendees.observe(viewLifecycleOwner, {
            binding.viewAttendeeChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.forEach { attendee ->
                addAttendeeChip(attendee)
            }
        })


        binding.viewAddNote.setOnClickListener {

            val newNote = TextInputEditText(context!!)
            newNote.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            newNote.isSingleLine = false
            newNote.maxLines = 8

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Add feedback / note")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(newNote)

            builder.setPositiveButton("Save") { _, _ ->
                icalViewViewModel.insertRelatedNote(ICalObject.createNote(summary = newNote.text.toString()))
            }

            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.show()

        }

        binding.viewProgressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */  }

            override fun onStopTrackingTouch(slider: Slider) {
                icalViewViewModel.updateProgress(icalViewViewModel.vJournal.value!!.property, binding.viewProgressSlider.value.toInt())
            }
        })



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

        val categoryChip = inflater.inflate(R.layout.fragment_ical_view_categories_chip, binding.viewCategoriesChipgroup, false) as Chip
        categoryChip.text = category.text
        binding.viewCategoriesChipgroup.addView(categoryChip)

        categoryChip.setOnClickListener {
            val selectedCategoryArray = arrayOf(category.text)     // convert to array
            // Responds to chip click
            this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalListFragment().setCategory2filter(selectedCategoryArray)
            )
        }


    }



    private fun addAttendeeChip(attendee: Attendee) {

        val attendeeChip = inflater.inflate(R.layout.fragment_ical_view_attendees_chip, binding.viewAttendeeChipgroup, false) as Chip
        attendeeChip.text = attendee.caladdress
        when (attendee.roleparam) {
            0 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_chair, null)
            1 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
            2 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_optparticipant, null)
            3 -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_nonparticipant, null)
            else -> attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_attendee_reqparticipant, null)
        }
        binding.viewAttendeeChipgroup.addView(attendeeChip)

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


    private fun addSubtasksView(subtask: ICalObject?, container: ViewGroup?) {

        if (subtask == null)
            return

        var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

        val subtaskView = inflater.inflate(R.layout.fragment_ical_view_subtask, container, false)
        subtaskView.view_subtask_textview.text = subtask.summary
        subtaskView.view_subtask_progress_slider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskView.view_subtask_progress_percent.text = if(subtask.percent?.toFloat() != null) subtask.percent!!.toString() else "0"
        subtaskView.view_subtask_progress_checkbox.isChecked = subtask.percent == 100

        // Instead of implementing here
        //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
        //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */
        subtaskView.view_subtask_progress_slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */  }

            override fun onStopTrackingTouch(slider: Slider) {
                subtaskView.view_subtask_progress_percent.text = subtaskView.view_subtask_progress_slider.value.toInt().toString()
                subtaskView.view_subtask_progress_checkbox.isChecked = subtask.percent == 100
                if (subtaskView.view_subtask_progress_slider.value < 100)
                    resetProgress = subtaskView.view_subtask_progress_slider.value.toInt()
                icalViewViewModel.updateProgress(subtask, subtaskView.view_subtask_progress_slider.value.toInt())


            }
        })

        subtaskView.view_subtask_progress_checkbox.setOnCheckedChangeListener { button, checked ->
            if (checked) {
                subtaskView.view_subtask_progress_percent.text = "100"
                subtaskView.view_subtask_progress_slider.value = 100F
                icalViewViewModel.updateProgress(subtask, 100)
            } else {
                subtaskView.view_subtask_progress_percent.text = resetProgress.toString()
                subtaskView.view_subtask_progress_slider.value = resetProgress.toFloat()
                icalViewViewModel.updateProgress(subtask, resetProgress)
            }

        }
            binding.viewSubtasksLinearlayout.addView(subtaskView)
    }




    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_view_share) {

            var shareText = "${convertLongToDateString(icalViewViewModel.vJournal.value!!.property.dtstart)} ${convertLongToTimeString(icalViewViewModel.vJournal.value!!.property.dtstart)}\n"
            shareText += "${icalViewViewModel.vJournal.value!!.property.summary}\n\n"
            shareText += "${icalViewViewModel.vJournal.value!!.property.description}\n\n"
            //todo add category again
            //shareText += "Categories/Labels: ${vJournalItemViewModel.vJournal.value!!.vCategory}"

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, icalViewViewModel.vJournal.value!!.property.summary)
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            startActivity(Intent(shareIntent))


        }
        return super.onOptionsItemSelected(item)
    }
}

