/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5.ui

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.bitfire.notesx5.*
import at.bitfire.notesx5.database.*
import at.bitfire.notesx5.database.properties.Attachment
import at.bitfire.notesx5.database.properties.Attendee
import at.bitfire.notesx5.database.properties.Category
import at.bitfire.notesx5.database.properties.Role
import at.bitfire.notesx5.databinding.*
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class IcalViewFragment : Fragment() {

    lateinit var binding: FragmentIcalViewBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: IcalViewViewModelFactory
    lateinit var icalViewViewModel: IcalViewViewModel

    private var fileName: Uri? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var recording: Boolean = false
    private var playing: Boolean = false

    private lateinit var settings: SharedPreferences

    // set default audio format (might be overwritten by settings)
    private var audioFileExtension = "mp4"
    private var audioOutputFormat = MediaRecorder.OutputFormat.MPEG_4
    private var audioEncoder = MediaRecorder.AudioEncoder.AAC


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

        settings = PreferenceManager.getDefaultSharedPreferences(context)
        val settingMimetype = settings.getString("setting_audio_format", Attachment.FMTTYPE_AUDIO_MP4_AAC)!!
        if(settingMimetype == Attachment.FMTTYPE_AUDIO_MP4_AAC) {
            audioFileExtension = "aac"
            audioOutputFormat = MediaRecorder.OutputFormat.MPEG_4
            audioEncoder = MediaRecorder.AudioEncoder.AAC
        } else if (settingMimetype == Attachment.FMTTYPE_AUDIO_3GPP) {
            audioFileExtension = "3gp"
            audioOutputFormat = MediaRecorder.OutputFormat.THREE_GPP
            audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
        } else if (settingMimetype == Attachment.FMTTYPE_AUDIO_OGG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioFileExtension = "ogg"
            audioOutputFormat = MediaRecorder.OutputFormat.OGG
            audioEncoder = MediaRecorder.AudioEncoder.OPUS
        }


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

                binding.viewAttachmentsLinearlayout.removeAllViews()
                icalViewViewModel.icalEntity.value!!.attachment?.forEach { attachment ->
                    val attachmentBinding = FragmentIcalViewAttachmentBinding.inflate(inflater, container, false)

                    //open the attachment on click
                    attachmentBinding.viewAttachmentCardview.setOnClickListener {

                        try {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.setDataAndType(Uri.parse(attachment.uri), attachment.fmttype)
                            intent.flags = FLAG_GRANT_READ_URI_PERMISSION
                            startActivity(intent)

                        } catch (e: IOException) {
                            Log.i("fileprovider", "Failed to retrieve file\n$e")
                            Toast.makeText(requireContext(), "Failed to retrieve file.", Toast.LENGTH_LONG).show()
                        } catch (e: ActivityNotFoundException) {
                            Log.i("ActivityNotFound", "No activity found to open file\n$e")
                            Toast.makeText(requireContext(), "No app was found to open this file.", Toast.LENGTH_LONG).show()
                        }

                    }
                    if (attachment.filename!!.isNotEmpty())
                        attachmentBinding.viewAttachmentTextview.text = attachment.filename
                    else
                        attachmentBinding.viewAttachmentTextview.text = attachment.fmttype

                    if (attachment.filesize == null)
                        attachmentBinding.viewAttachmentFilesize.visibility = View.GONE
                    else
                        attachmentBinding.viewAttachmentFilesize.text = getAttachmentSizeString(attachment.filesize?:0L)


                    // load thumbnail if possible
                    try {
                        val thumbSize = Size(50, 50)
                        val thumbUri = Uri.parse(attachment.uri)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val thumbBitmap = context?.contentResolver!!.loadThumbnail(thumbUri, thumbSize, null)
                            attachmentBinding.viewAttachmentPictureThumbnail.setImageBitmap(thumbBitmap)
                            attachmentBinding.viewAttachmentPictureThumbnail.visibility = View.VISIBLE
                        }
                    } catch (e: FileNotFoundException) {
                        Log.d("FileNotFound", "File with uri ${attachment.uri} not found.\n$e")
                    }

                    binding.viewAttachmentsLinearlayout.addView(attachmentBinding.root)
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
                it.forEach { relatedNote ->
                    if(relatedNote == null)
                        return@forEach

                    val commentBinding = FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                    commentBinding.viewCommentTextview.text = relatedNote.summary
                    if(relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_MP4_AAC || relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_3GPP || relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_OGG) {
                        commentBinding.viewCommentPlaybutton.visibility = View.VISIBLE

                        // TODO TRY Catch
                        // Maybe there is a better solution here anyway

                        //playback on click
                        commentBinding.viewCommentPlaybutton.setOnClickListener {

                            //stop playing if playback is on
                            if(playing) {
                                stopPlaying()
                                commentBinding.viewCommentPlaybutton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                                playing = false
                            } else {
                                // write the base64 decoded Bytestream in a file and use it as an input for the player
                                //val fileBytestream = Base64.decode(relatedNote.attachmentValue, Base64.DEFAULT)
                                fileName = Uri.parse(relatedNote.attachmentUri)

                                startPlaying()
                                commentBinding.viewCommentPlaybutton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop))
                                playing = true

                                // make sure to set the icon back to the play icon when the player reached the end
                                player?.setOnCompletionListener {
                                    commentBinding.viewCommentPlaybutton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                                    playing = false
                                }
                            }
                        }

                    }
                    commentBinding.root.setOnClickListener { view ->
                        view.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(relatedNote.id))
                    }
                    binding.viewFeedbackLinearlayout.addView(commentBinding.root)
                }
            }
        })

        icalViewViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            binding.viewSubtasksLinearlayout.removeAllViews()
            it.forEach { singleSubtask ->
                addSubtasksView(singleSubtask, container)
            }
        }



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
                icalViewViewModel.insertRelatedNote(newNote.text.toString())
            }

            builder.setNegativeButton("Cancel") { _, _ ->
                // Do nothing, just close the message
            }

            builder.show()

        }


        // handling audio recording
        binding.viewAddAudioNote.setOnClickListener {

            // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
            if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

                val audioDialogBinding = FragmentIcalViewAudioDialogBinding.inflate(inflater, container, false)

                audioDialogBinding.viewAudioDialogStartrecordingFab.setOnClickListener {

                    if(!recording) {
                        fileName = Uri.parse("${requireContext().cacheDir}/recorded.$audioFileExtension")
                        audioDialogBinding.viewAudioDialogStartrecordingFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop))
                        startRecording()
                        audioDialogBinding.viewAudioDialogStartplayingFab.isEnabled = false
                        recording = true
                    } else {
                        stopRecording()
                        audioDialogBinding.viewAudioDialogStartrecordingFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_microphone))
                        audioDialogBinding.viewAudioDialogStartplayingFab.isEnabled = true
                        recording = false

                        player?.duration?.let { audioDialogBinding.viewAudioDialogProgressbar.max = it }
                        player?.currentPosition?.let { audioDialogBinding.viewAudioDialogProgressbar.progress = it }

                    }
                }

                audioDialogBinding.viewAudioDialogStartplayingFab.setOnClickListener {
                    if(!playing) {
                        startPlaying()
                        initialiseSeekBar(audioDialogBinding.viewAudioDialogProgressbar)
                        audioDialogBinding.viewAudioDialogStartplayingFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop))
                        playing = true

                        player?.setOnCompletionListener {
                            audioDialogBinding.viewAudioDialogStartplayingFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                            playing = false
                        }
                    }
                    else {
                        stopPlaying()
                        audioDialogBinding.viewAudioDialogStartplayingFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                        playing = false
                    }
                }


                audioDialogBinding.viewAudioDialogProgressbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser)
                            player?.seekTo(progress)
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {   }
                    override fun onStopTrackingTouch(seekBar: SeekBar?)  {   }
                })





                //Open dialog to record audio
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.view_fragment_audio_dialog_add_audio_note))
                        //.setMessage(getString(R.string.view_fragment_audio_permission_message))
                        .setView(audioDialogBinding.root)
                        .setPositiveButton("Save") { _, _ ->
                            stopRecording()
                            stopPlaying()

                            if(fileName != null) {

                                try {
                                    val cachedFile = File(fileName.toString())
                                    val newFilename = "${System.currentTimeMillis()}.$audioFileExtension"
                                    val newFile = File(
                                        Attachment.getAttachmentDirectory(requireContext()),
                                        newFilename
                                    )
                                    newFile.createNewFile()
                                    newFile.writeBytes(cachedFile.readBytes())

                                    val newAttachment = Attachment(
                                        fmttype = Attachment.FMTTYPE_AUDIO_MP4_AAC,
                                        uri = getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, newFile).toString(),
                                        filename = newFilename,
                                        extension = ".mp4",
                                        filesize = newFile.length()
                                    )
                                    icalViewViewModel.insertRelatedAudioNote(newAttachment)


                                } catch (e: IOException) {
                                    Log.e("IOException", "Failed to process file\n$e")
                                }
                            }
                        }
                        .setNegativeButton("Discard") { _, _ ->
                            stopRecording()
                            stopPlaying()
                        }
                        .show()
            } else {
                //request for permission to load contacts
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.view_fragment_audio_permission))
                    .setMessage(getString(R.string.view_fragment_audio_permission_message))
                    .setPositiveButton("Ok") { _, _ ->
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            }

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
        when (item.itemId) {
            R.id.menu_view_share_text -> {

                var shareText = "${convertLongToDateString(icalViewViewModel.icalEntity.value!!.property.dtstart)} ${convertLongToTimeString(icalViewViewModel.icalEntity.value!!.property.dtstart)}\n"
                shareText += "${icalViewViewModel.icalEntity.value!!.property.summary}\n\n"
                shareText += "${icalViewViewModel.icalEntity.value!!.property.description}\n\n"
                //shareText += icalViewViewModel.icalEntity.value!!.getICalString()

                val categories: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value!!.category?.forEach { categories.add(it.text) }
                shareText += "Categories/Labels: ${categories.joinToString(separator=", ")}"

                val attendees: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value!!.attendee?.forEach { attendees.add(it.caladdress) }

                // prepare file attachment, the file is stored in the externalCacheDir and then provided through a FileProvider
                var uri: Uri? = null
                try {
                    val icsFileName = "${requireContext().externalCacheDir}/ics_file.ics"
                    val icsFile = File(icsFileName).apply {
                        writeText(icalViewViewModel.icalEntity.value!!.getICalString())
                        createNewFile()
                    }
                    uri = getUriForFile(requireContext(),
                        AUTHORITY_FILEPROVIDER, icsFile)
                } catch (e: Exception) {
                    Log.i("fileprovider", "Failed to attach ICS File")
                    Toast.makeText(requireContext(), "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
                }

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, icalViewViewModel.icalEntity.value!!.property.summary)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_EMAIL, attendees.toTypedArray())

                }

                Log.d("shareIntent", shareText)
                startActivity(Intent(shareIntent))
            }
            R.id.menu_view_share_ics -> {

                val shareText = icalViewViewModel.icalEntity.value!!.getICalString()

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/calendar"
                    putExtra(Intent.EXTRA_STREAM, shareText)
                }

                Log.d("shareIntent", shareText)
                startActivity(Intent(shareIntent))
            }
            R.id.menu_view_copy_as_journal -> {
                val icalEntityCopy = icalViewViewModel.icalEntity.value!!
                icalEntityCopy.property.id = 0L
                icalEntityCopy.property.component = Component.VJOURNAL.name
                icalEntityCopy.property.module = Module.JOURNAL.name
                icalEntityCopy.property.completed = null
                icalEntityCopy.property.completedTimezone = null
                if(icalEntityCopy.property.dtstart == null)
                    icalEntityCopy.property.dtstart = System.currentTimeMillis()
                icalEntityCopy.property.dtstamp = System.currentTimeMillis()
                icalEntityCopy.property.dtend = null
                icalEntityCopy.property.dtendTimezone = null
                icalEntityCopy.property.due = null
                icalEntityCopy.property.dueTimezone = null
                icalEntityCopy.property.duration = null
                icalEntityCopy.property.priority = null

                icalEntityCopy.attachment?.forEach { it.attachmentId = 0L }
                icalEntityCopy.attendee?.forEach { it.attendeeId = 0L }
                icalEntityCopy.category?.forEach { it.categoryId = 0L }
                icalEntityCopy.comment?.forEach { it.commentId = 0L }
                icalEntityCopy.organizer?.organizerId = 0L
                icalEntityCopy.relatedto?.forEach { it.relatedtoId = 0L }
                icalEntityCopy.resource?.forEach { it.resourceId = 0L }

                this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalEntityCopy)
                )
            }
            R.id.menu_view_copy_as_note -> {
                val icalEntityCopy = icalViewViewModel.icalEntity.value!!
                icalEntityCopy.property.id = 0L
                icalEntityCopy.property.component = Component.VJOURNAL.name
                icalEntityCopy.property.module = Module.NOTE.name
                icalEntityCopy.property.completed = null
                icalEntityCopy.property.completedTimezone = null
                icalEntityCopy.property.dtstart = null
                icalEntityCopy.property.dtstartTimezone = null
                icalEntityCopy.property.dtstamp = System.currentTimeMillis()
                icalEntityCopy.property.dtend = null
                icalEntityCopy.property.dtendTimezone = null
                icalEntityCopy.property.due = null
                icalEntityCopy.property.dueTimezone = null
                icalEntityCopy.property.duration = null
                icalEntityCopy.property.priority = null

                icalEntityCopy.attachment?.forEach { it.attachmentId = 0L }
                icalEntityCopy.attendee?.forEach { it.attendeeId = 0L }
                icalEntityCopy.category?.forEach { it.categoryId = 0L }
                icalEntityCopy.comment?.forEach { it.commentId = 0L }
                icalEntityCopy.organizer?.organizerId = 0L
                icalEntityCopy.relatedto?.forEach { it.relatedtoId = 0L }
                icalEntityCopy.resource?.forEach { it.resourceId = 0L }

                this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalEntityCopy)
                )
            }
            R.id.menu_view_copy_as_todo -> {
                val icalEntityCopy = icalViewViewModel.icalEntity.value!!
                icalEntityCopy.property.id = 0L
                icalEntityCopy.property.component = Component.VTODO.name
                icalEntityCopy.property.module = Module.TODO.name
                icalEntityCopy.property.dtstamp = System.currentTimeMillis()

                icalEntityCopy.attachment?.forEach { it.attachmentId = 0L }
                icalEntityCopy.attendee?.forEach { it.attendeeId = 0L }
                icalEntityCopy.category?.forEach { it.categoryId = 0L }
                icalEntityCopy.comment?.forEach { it.commentId = 0L }
                icalEntityCopy.organizer?.organizerId = 0L
                icalEntityCopy.relatedto?.forEach { it.relatedtoId = 0L }
                icalEntityCopy.resource?.forEach { it.resourceId = 0L }

                this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalEntityCopy)
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(audioOutputFormat)
            setAudioEncoder(audioEncoder)
            setOutputFile(fileName.toString())
            setMaxDuration(60000)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("startRecording()", "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        // initialise the player
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName.toString())
                prepare()
            } catch (e: IOException) {
                Log.e("preparePlaying()", "prepare() failed")
            }
        }
    }

    private fun startPlaying() {
        // initialise the player
        if(fileName != null) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(requireContext(), fileName!!)
                    //setDataSource(fileName)
                    prepare()
                } catch (e: IOException) {
                    Log.e("preparePlaying()", "prepare() failed: \n$e")
                }
            }
            player?.start()
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun initialiseSeekBar(seekbar: SeekBar) {

        seekbar.max = player!!.duration

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object: Runnable {
            override fun run() {
                try {
                    seekbar.progress = player!!.currentPosition
                    handler.postDelayed(this, 10)
                } catch (e: Exception) {
                    seekbar.progress = 0
                }
            }
        }, 0)
    }
}

