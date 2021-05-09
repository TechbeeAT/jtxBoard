/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import at.bitfire.notesx5.R
import at.bitfire.notesx5.convertLongToDateString
import at.bitfire.notesx5.convertLongToTimeString
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Role
import at.bitfire.notesx5.databinding.FragmentIcalViewBinding
import at.bitfire.notesx5.databinding.FragmentIcalViewCommentBinding
import at.bitfire.notesx5.databinding.FragmentIcalViewRelatedtoBinding
import at.bitfire.notesx5.databinding.FragmentIcalViewSubtaskBinding
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText


class IcalViewFragment : Fragment() {

    lateinit var binding: FragmentIcalViewBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: IcalViewViewModelFactory
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

        val arguments = IcalViewFragmentArgs.fromBundle((requireArguments()))

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
        icalViewViewModel.editingClicked.observe(viewLifecycleOwner, {
            if (it) {
                icalViewViewModel.editingClicked.value = false
                this.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.icalEntity.value!!)
                )
            }
        })

        icalViewViewModel.icalEntity.observe(viewLifecycleOwner, {

            if (it?.property == null) {
                binding.viewProgressIndicator.visibility = View.VISIBLE
            }
            else {

                binding.viewProgressIndicator.visibility = View.GONE

                when (it.property.component) {
                    Component.VTODO.name -> {
                        binding.viewStatusChip.text = StatusTodo.getStringResource(requireContext(), it.property.status)
                                ?: it.property.status
                    }
                    Component.VJOURNAL.name -> {
                        binding.viewStatusChip.text = StatusJournal.getStringResource(requireContext(), it.property.status)
                                ?: it.property.status
                    }
                    else -> {
                        binding.viewStatusChip.text = it.property.status
                    }
                }

                binding.viewClassificationChip.text = Classification.getStringResource(requireContext(), it.property.classification)
                        ?: it.property.classification

                val priorityArray = resources.getStringArray(R.array.priority)
                if (icalViewViewModel.icalEntity.value?.property?.priority != null && icalViewViewModel.icalEntity.value!!.property.priority in 0..9)
                    binding.viewPriorityChip.text = priorityArray[icalViewViewModel.icalEntity.value!!.property.priority!!]

                binding.viewCommentsLinearlayout.removeAllViews()
                icalViewViewModel.icalEntity.value!!.comment?.forEach { comment ->
                    val commentBinding = FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                    commentBinding.viewCommentTextview.text = comment.text
                    binding.viewCommentsLinearlayout.addView(commentBinding.root)
                }

                if (it.ICalCollection?.color != null) {
                    try {
                        binding.viewColorbar.setColorFilter(it.ICalCollection?.color!!)
                    } catch (e: IllegalArgumentException) {
                        Log.println(Log.INFO, "Invalid color", "Invalid Color cannot be parsed: ${it.ICalCollection?.color}")
                        binding.viewColorbar.visibility = View.GONE
                    }
                } else
                    binding.viewColorbar.visibility = View.GONE
            }
        })

        icalViewViewModel.subtasksCountList.observe(viewLifecycleOwner, { })



        icalViewViewModel.relatedNotes.observe(viewLifecycleOwner, {

            if (it?.size != 0) {
                binding.viewFeedbackLinearlayout.removeAllViews()
                it.forEach { relatedICalObject ->
                    val relatedtoBinding = FragmentIcalViewRelatedtoBinding.inflate(inflater, container, false)
                    relatedtoBinding.viewRelatedTextview.text = relatedICalObject?.summary
                    relatedtoBinding.root.setOnClickListener { view ->
                        view.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(relatedICalObject!!.id))
                    }
                    binding.viewFeedbackLinearlayout.addView(relatedtoBinding.root)
                }
            }
        })

        icalViewViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            binding.viewSubtasksLinearlayout.removeAllViews()
            it.forEach { singleSubtask ->
                addSubtasksView(singleSubtask, container)
            }
        }

       // icalViewViewModel.subtasksCountHashmap.observe(viewLifecycleOwner) { }


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

            val newNote = TextInputEditText(requireContext())
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


        var resetProgress = icalViewViewModel.icalEntity.value?.property?.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

        binding.viewProgressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (binding.viewProgressSlider.value.toInt() < 100)
                    resetProgress = binding.viewProgressSlider.value.toInt()
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, binding.viewProgressSlider.value.toInt())
            }
        })

        binding.viewProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, 100)
            } else {
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property, resetProgress)
            }
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
        attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, Role.getDrawableResourceByName(attendee.role), null)

        binding.viewAttendeeChipgroup.addView(attendeeChip)

    }


    private fun addSubtasksView(subtask: ICalObject?, container: ViewGroup?) {

        if (subtask == null)
            return

        val subtaskBinding = FragmentIcalViewSubtaskBinding.inflate(inflater, container, false)

        var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

       var subtaskSummary =subtask.summary
        val subtaskCount = icalViewViewModel.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
        if (subtaskCount != null)
            subtaskSummary += " (+${subtaskCount})"
        subtaskBinding.viewSubtaskTextview.text = subtaskSummary
        subtaskBinding.viewSubtaskProgressSlider.value = if(subtask.percent?.toFloat() != null) subtask.percent!!.toFloat() else 0F
        subtaskBinding.viewSubtaskProgressPercent.text = if(subtask.percent?.toFloat() != null) "${subtask.percent!!} %" else "0"
        subtaskBinding.viewSubtaskProgressCheckbox.isChecked = subtask.percent == 100

        // Instead of implementing here
        //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
        //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */
        subtaskBinding.viewSubtaskProgressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (subtaskBinding.viewSubtaskProgressSlider.value < 100)
                    resetProgress = subtaskBinding.viewSubtaskProgressSlider.value.toInt()
                icalViewViewModel.updateProgress(subtask, subtaskBinding.viewSubtaskProgressSlider.value.toInt())


            }
        })

        subtaskBinding.viewSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                icalViewViewModel.updateProgress(subtask, 100)
            } else {
                icalViewViewModel.updateProgress(subtask, resetProgress)
            }

        }

        subtaskBinding.root.setOnClickListener {
            it.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(subtask.id))
        }

            binding.viewSubtasksLinearlayout.addView(subtaskBinding.root)
    }





    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_view_share_text) {

            var shareText = "${convertLongToDateString(icalViewViewModel.icalEntity.value!!.property.dtstart)} ${convertLongToTimeString(icalViewViewModel.icalEntity.value!!.property.dtstart)}\n"
            shareText += "${icalViewViewModel.icalEntity.value!!.property.summary}\n\n"
            shareText += "${icalViewViewModel.icalEntity.value!!.property.description}\n\n"
            shareText += icalViewViewModel.icalEntity.value!!.getICalString()
            //todo add category again
            //shareText += "Categories/Labels: ${vJournalItemViewModel.vJournal.value!!.vCategory}"

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, icalViewViewModel.icalEntity.value!!.property.summary)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            Log.d("shareIntent", shareText)
            startActivity(Intent(shareIntent))
        }
        else if (item.itemId == R.id.menu_view_share_ics) {

            val shareText = icalViewViewModel.icalEntity.value!!.getICalString()

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, shareText)
            }

            Log.d("shareIntent", shareText)
            startActivity(Intent(shareIntent))
        }
        return super.onOptionsItemSelected(item)
    }
}

