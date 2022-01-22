/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.accounts.Account
import android.app.AlertDialog
import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ImageDecoder
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.*
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.*
import at.techbee.jtx.databinding.*
import at.techbee.jtx.monetization.AdManager
import at.techbee.jtx.monetization.BillingManager
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateTimeString
import at.techbee.jtx.util.DateTimeUtils.getAttachmentSizeString
import at.techbee.jtx.util.DateTimeUtils.getLongListfromCSVString
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import java.io.*
import java.lang.ClassCastException
import java.lang.NullPointerException


class IcalViewFragment : Fragment() {

    lateinit var binding: FragmentIcalViewBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var viewModelFactory: IcalViewViewModelFactory
    lateinit var icalViewViewModel: IcalViewViewModel
    private var optionsMenu: Menu? = null

    private var fileName: Uri? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private val seekbarHandler = Handler(Looper.getMainLooper())

    private var recording: Boolean = false
    private var playing: Boolean = false

    private lateinit var settings: SharedPreferences

    // set default audio format (might be overwritten by settings)
    private var audioFileExtension = "3gp"
    private var audioOutputFormat = MediaRecorder.OutputFormat.MPEG_4
    private var audioEncoder = MediaRecorder.AudioEncoder.AAC
    private var audioMimetype: String = Attachment.FMTTYPE_AUDIO_3GPP


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentIcalViewBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalViewFragmentArgs.fromBundle((requireArguments()))

        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .build()

        // add menu
        setHasOptionsMenu(true)

        settings = PreferenceManager.getDefaultSharedPreferences(context)
        val settingMimetype = settings.getString("setting_audio_format", Attachment.FMTTYPE_AUDIO_3GPP) ?: Attachment.FMTTYPE_AUDIO_3GPP
        if(settingMimetype == Attachment.FMTTYPE_AUDIO_MP4_AAC) {
            audioFileExtension = "aac"
            audioOutputFormat = MediaRecorder.OutputFormat.MPEG_4
            audioEncoder = MediaRecorder.AudioEncoder.AAC
        } else if (settingMimetype == Attachment.FMTTYPE_AUDIO_OGG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioFileExtension = "ogg"
            audioOutputFormat = MediaRecorder.OutputFormat.OGG
            audioEncoder = MediaRecorder.AudioEncoder.OPUS
        } else {  // settingMimetype == Attachment.FMTTYPE_AUDIO_3GPP is also the default format
            audioFileExtension = "3gp"
            audioOutputFormat = MediaRecorder.OutputFormat.THREE_GPP
            audioEncoder = MediaRecorder.AudioEncoder.AMR_NB
        }


        // set up view model
        viewModelFactory = IcalViewViewModelFactory(arguments.item2show, dataSource, application)
        icalViewViewModel =
            ViewModelProvider(
                this, viewModelFactory)[IcalViewViewModel::class.java]

        binding.model = icalViewViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE or ContentResolver.SYNC_OBSERVER_TYPE_PENDING) {
            icalViewViewModel.showSyncProgressIndicator.postValue(
                SyncUtil.isJtxSyncRunningForAccount(
                    Account(icalViewViewModel.icalEntity.value?.ICalCollection?.accountName, icalViewViewModel.icalEntity.value?.ICalCollection?.accountType)
                ))
        }

        // set up observers
        icalViewViewModel.editingClicked.observe(viewLifecycleOwner, {
            if (it) {
                icalViewViewModel.editingClicked.value = false

                // if the item is an instance of a recurring entry, make sure that the user is aware of this
                val originalId = icalViewViewModel.icalEntity.value?.property?.recurOriginalIcalObjectId
                if(originalId != null && icalViewViewModel.icalEntity.value?.property?.isRecurLinkedInstance == true) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.view_recurrence_note_to_original_dialog_header))
                        .setMessage(getString(R.string.view_recurrence_note_to_original))
                        .setPositiveButton("Continue") { _, _ ->
                            icalViewViewModel.icalEntity.value?.let { entity ->
                                this.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(entity)
                            )

                            }
                        }
                        .setNegativeButton("Go to Original") { _, _ ->
                            this.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(originalId)
                            )
                        }
                        .show()
                } else {
                    icalViewViewModel.icalEntity.value?.let { entity ->
                        this.findNavController().navigate(
                            IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(entity)
                        )
                    }
                }
            }
        })

        icalViewViewModel.icalEntity.observe(viewLifecycleOwner, {

            if(it == null) {
                Toast.makeText(context, R.string.view_toast_entry_does_not_exist_anymore, Toast.LENGTH_LONG).show()
                view?.findNavController()?.navigate(IcalViewFragmentDirections.actionIcalViewFragmentToIcalListFragment())
                return@observe   // just make sure that nothing else happens
            }

            if(it.ICalCollection?.readonly == true)
                hideEditingOptions()


            updateToolbarText()

            if(!SyncUtil.isDAVx5CompatibleWithJTX(application) || it.ICalCollection?.accountType == LOCAL_ACCOUNT_TYPE)
                optionsMenu?.findItem(R.id.menu_view_syncnow)?.isVisible = false


            when (it.property.component) {
                Component.VTODO.name -> {
                    binding.viewStatusChip.text =
                        StatusTodo.getStringResource(requireContext(), it.property.status)
                            ?: it.property.status
                }
                Component.VJOURNAL.name -> {
                    binding.viewStatusChip.text =
                        StatusJournal.getStringResource(requireContext(), it.property.status)
                            ?: it.property.status
                }
                else -> {
                    binding.viewStatusChip.text = it.property.status
                }
            }

            binding.viewClassificationChip.text =
                Classification.getStringResource(requireContext(), it.property.classification)
                    ?: it.property.classification

            val priorityArray = resources.getStringArray(R.array.priority)
            if (it.property.priority in 0..9)
                binding.viewPriorityChip.text =
                    priorityArray[icalViewViewModel.icalEntity.value?.property?.priority?:0]

            // don't show the option to add notes if VJOURNAL is not supported (only relevant if the current entry is a VTODO)
            if(it.ICalCollection?.supportsVJOURNAL != true) {
                binding.viewAddNote.visibility = View.GONE
                binding.viewAddAudioNote.visibility = View.GONE
            }

            // setting the description with Markdown
            it.property.description?.let { desc ->
                val descMarkwon = markwon.toMarkdown(desc)
                binding.viewDescription.text = descMarkwon
            }

            binding.viewCommentsLinearlayout.removeAllViews()
            it.comments?.forEach { comment ->
                val commentBinding =
                    FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                commentBinding.viewCommentTextview.text = comment.text
                binding.viewCommentsLinearlayout.addView(commentBinding.root)
            }

            binding.viewAttachmentsLinearlayout.removeAllViews()
            it.attachments?.forEach { attachment ->
                val attachmentBinding =
                    FragmentIcalViewAttachmentBinding.inflate(inflater, container, false)

                //open the attachment on click
                attachmentBinding.viewAttachmentCardview.setOnClickListener {
                    attachment.openFile(requireContext())
                }
                attachmentBinding.viewAttachmentTextview.text = attachment.getFilenameOrLink()

                val filesize = attachment.getFilesize(requireContext())
                if (filesize == 0L)
                    attachmentBinding.viewAttachmentFilesize.visibility = View.GONE
                else
                    attachmentBinding.viewAttachmentFilesize.text =
                        getAttachmentSizeString(filesize)


                // load thumbnail if possible
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        val thumbSize = Size(50, 50)
                        val thumbUri = Uri.parse(attachment.uri)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val thumbBitmap =
                                context?.contentResolver!!.loadThumbnail(thumbUri, thumbSize, null)
                            attachmentBinding.viewAttachmentPictureThumbnail.setImageBitmap(
                                thumbBitmap
                            )
                            attachmentBinding.viewAttachmentPictureThumbnail.visibility =
                                View.VISIBLE
                        }
                    } catch (e: NullPointerException) {
                        Log.i("UriEmpty", "Uri was empty or could not be parsed.")
                    } catch (e: FileNotFoundException) {
                        Log.d("FileNotFound", "File with uri ${attachment.uri} not found.\n$e")
                    } catch (e: ImageDecoder.DecodeException) {
                        Log.i("ImageThumbnail", "Could not retrieve image thumbnail from file ${attachment.uri}")
                    }
                }

                binding.viewAttachmentsLinearlayout.addView(attachmentBinding.root)
            }

            binding.viewAlarmsLinearlayout.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.alarms?.forEach { alarm ->
                addAlarmView(alarm)
            }

            binding.viewCategoriesChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.categories?.forEach { category ->
                addCategoryChip(category)
            }

            binding.viewResourcesChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.resources?.forEach { resource ->
                addResourceChip(resource)
            }

            binding.viewAttendeeChipgroup.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.attendees?.forEach { attendee ->
                addAttendeeChip(attendee)
            }

            // applying the color
            ICalObject.applyColorOrHide(binding.viewColorbarCollection, it.ICalCollection?.color)
            ICalObject.applyColorOrHide(binding.viewColorbarCollectionItem, it.property.color)


            it.property.recurOriginalIcalObjectId?.let { origId ->
                binding.viewRecurrenceGotooriginalButton.setOnClickListener { view ->
                    view.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(origId)
                    )
                }
            }

            var allExceptionsString = ""
            getLongListfromCSVString(it.property.exdate).forEach { exdate ->
                allExceptionsString += convertLongToFullDateTimeString(exdate, it.property.dtstartTimezone) + "\n"
            }
            binding.viewRecurrenceExceptionItems.text = allExceptionsString

            var allAdditionsString = ""
            getLongListfromCSVString(it.property.rdate).forEach { rdate ->
                allAdditionsString += convertLongToFullDateTimeString(rdate, it.property.dtstartTimezone) + "\n"
            }
            binding.viewRecurrenceAdditionsItems.text = allAdditionsString

        })

        icalViewViewModel.subtasksCountList.observe(viewLifecycleOwner, { })

        icalViewViewModel.relatedNotes.observe(viewLifecycleOwner, {

            if(playing)             // don't interrupt if audio is currently played
                return@observe

            if (it?.size != 0) {
                binding.viewFeedbackLinearlayout.removeAllViews()
                it.forEach { relatedNote ->
                    if(relatedNote == null)
                        return@forEach

                    val commentBinding = FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                    if(relatedNote.summary?.isNotEmpty() == true)
                        commentBinding.viewCommentTextview.text = relatedNote.summary
                    else
                        commentBinding.viewCommentTextview.visibility = View.GONE

                    if(relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_MP4_AAC || relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_3GPP || relatedNote.attachmentFmttype == Attachment.FMTTYPE_AUDIO_OGG) {
                        commentBinding.viewCommentPlaybutton.visibility = View.VISIBLE
                        commentBinding.viewCommentProgressbar.visibility = View.VISIBLE

                        //playback on click
                        commentBinding.viewCommentPlaybutton.setOnClickListener {

                            val uri = Uri.parse(relatedNote.attachmentUri)
                            togglePlayback(commentBinding.viewCommentProgressbar, commentBinding.viewCommentPlaybutton, uri)

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
                addSubtasksView(singleSubtask, binding.viewSubtasksLinearlayout)
            }
        }

        icalViewViewModel.recurInstances.observe(viewLifecycleOwner) { instanceList ->
            val recurDates = mutableListOf<String>()
            instanceList.forEach { instance ->
                instance?.let {
                    recurDates.add(convertLongToFullDateTimeString(it.dtstart, it.dtstartTimezone))
                }
            }
            binding.viewRecurrenceItems.text = recurDates.joinToString(separator = "\n")
        }


        binding.viewAddNote.setOnClickListener {

            val addnoteDialogBinding = FragmentIcalViewAddnoteDialogBinding.inflate(inflater)

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.view_dialog_add_note)
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(addnoteDialogBinding.root)

            builder.setPositiveButton(R.string.save) { _, _ ->
                val text = addnoteDialogBinding.viewViewAddnoteDialogEdittext.text.toString()
                if(text.isBlank())
                    Toast.makeText(context, R.string.view_dialog_addnote_toast_no_summary_description, Toast.LENGTH_LONG).show()
                else
                    icalViewViewModel.insertRelated(addnoteDialogBinding.viewViewAddnoteDialogEdittext.text.toString(), null)
            }

            builder.setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, just close the message
            }
            //builder is shown on positiveButton after unlinking or immediately (after the recur-check)

            // if the item is an instance of a recurring entry, make sure that the user is aware of this
            val originalId = icalViewViewModel.icalEntity.value?.property?.recurOriginalIcalObjectId
            if(originalId != null && icalViewViewModel.icalEntity.value?.property?.isRecurLinkedInstance == true) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.view_recurrence_note_to_original_dialog_header))
                    .setMessage(getString(R.string.view_recurrence_note_to_original))
                    .setPositiveButton(R.string.cont) { _, _ ->
                        builder.show()
                    }
                    .setNegativeButton(R.string.view_recurrence_go_to_original_button) { _, _ ->
                        this.findNavController().navigate(
                            IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(
                                originalId
                            )
                        )
                    }
                    .show()
            } else {
                builder.show()
                addnoteDialogBinding.viewViewAddnoteDialogEdittext.requestFocus()
            }
        }


        // handling audio recording
        binding.viewAddAudioNote.setOnClickListener {

            // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
            if (PermissionsHelper.checkPermissionRecordAudio(requireActivity())) {

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
                    togglePlayback(audioDialogBinding.viewAudioDialogProgressbar, audioDialogBinding.viewAudioDialogStartplayingFab, fileName)
                }

                audioDialogBinding.viewAudioDialogProgressbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser)
                            player?.seekTo(progress)
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {   }
                    override fun onStopTrackingTouch(seekBar: SeekBar?)  {   }
                })

                //Prepare the builder to open dialog to record audio
                val audioRecorderAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.view_fragment_audio_dialog_add_audio_note))
                    //.setMessage(getString(R.string.view_fragment_audio_permission_message))
                    .setView(audioDialogBinding.root)
                    .setPositiveButton(R.string.save) { _, _ ->
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
                                    fmttype = audioMimetype,
                                    uri = getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, newFile).toString(),
                                    filename = newFilename,
                                    extension = audioFileExtension,
                                )
                                icalViewViewModel.insertRelated(null, newAttachment)


                            } catch (e: IOException) {
                                Log.e("IOException", "Failed to process file\n$e")
                            }
                        }
                    }
                    .setNegativeButton(R.string.discard) { _, _ ->
                        stopRecording()
                        stopPlaying()
                    }

                // if the item is an instance of a recurring entry, make sure that the user is aware of this
                val originalId = icalViewViewModel.icalEntity.value?.property?.recurOriginalIcalObjectId
                if(originalId != null && icalViewViewModel.icalEntity.value?.property?.isRecurLinkedInstance == true) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.view_recurrence_note_to_original_dialog_header))
                        .setMessage(getString(R.string.view_recurrence_note_to_original))
                        .setPositiveButton(R.string.cont) { _, _ ->
                            audioRecorderAlertDialogBuilder.show()
                        }
                        .setNegativeButton(R.string.view_recurrence_go_to_original_button) { _, _ ->
                            this.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(
                                    originalId
                                )
                            )
                        }
                        .show()
                } else {
                    audioRecorderAlertDialogBuilder.show()
                }
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


        binding.viewBottomBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.menu_view_bottom_copy -> this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.icalEntity.value?.getIcalEntityCopy(icalViewViewModel.icalEntity.value?.property?.module) ?: return@setOnMenuItemClickListener false)
                )
                R.id.menu_view_bottom_delete -> deleteItem()
            }
            false
        }

        // show ads only for AdFlavors and if the subscription was not purchased (gplay flavor only)
        if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isAdFreeSubscriptionPurchased?.value == false)
            AdManager.getInstance()?.addAdViewToContainerViewFragment(binding.viewAdContainer, requireContext(), AdManager.getInstance()?.unitIdBannerView)
        else
            binding.viewAdContainer.visibility = View.GONE


        return binding.root
    }

    override fun onResume() {

        updateToolbarText()
        super.onResume()
    }



    private fun updateToolbarText() {
        try {
            val activity = requireActivity() as MainActivity
            val toolbarText = when(icalViewViewModel.icalEntity.value?.property?.module) {
                Module.JOURNAL.name -> getString(R.string.toolbar_text_view_journal_details)
                Module.NOTE.name -> getString(R.string.toolbar_text_view_note_details)
                Module.TODO.name -> getString(R.string.toolbar_text_view_task_details)
                else -> ""
            }
            activity.setToolbarTitle(toolbarText, icalViewViewModel.icalEntity.value?.property?.summary )
        } catch (e: ClassCastException) {
            Log.d("setToolbarText", "Class cast to MainActivity failed (this is common for tests but doesn't really matter)\n$e")
        }
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

    private fun addResourceChip(resource: Resource) {

        if (resource.text.isNullOrBlank())     // don't add empty categories
            return

        val resourceChip = inflater.inflate(R.layout.fragment_ical_view_resources_chip, binding.viewResourcesChipgroup, false) as Chip
        resourceChip.text = resource.text
        binding.viewResourcesChipgroup.addView(resourceChip)
    }


    private fun addAttendeeChip(attendee: Attendee) {

        val attendeeChip = inflater.inflate(R.layout.fragment_ical_view_attendees_chip, binding.viewAttendeeChipgroup, false) as Chip
        attendeeChip.text = attendee.getDisplayString()
        attendeeChip.chipIcon = ResourcesCompat.getDrawable(resources, Role.getDrawableResourceByName(attendee.role), null)

        binding.viewAttendeeChipgroup.addView(attendeeChip)
    }

    private fun addAlarmView(alarm: Alarm) {

        // we don't add alarm of which the DateTime is not set or cannot be determined
        if(alarm.triggerTime == null && alarm.triggerRelativeDuration == null)
            return

        val bindingAlarm = when {
            alarm.triggerTime != null ->
                alarm.getAlarmCardBinding(inflater, binding.viewAlarmsLinearlayout, null, null )
            alarm.triggerRelativeDuration?.isNotEmpty() == true -> {

                val referenceDate = if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name)
                    icalViewViewModel.icalEntity.value?.property?.due ?: return
                else
                    icalViewViewModel.icalEntity.value?.property?.dtstart ?: return

                val referenceTZ = if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name)
                    icalViewViewModel.icalEntity.value?.property?.dueTimezone
                else
                    icalViewViewModel.icalEntity.value?.property?.dtstartTimezone
                alarm.getAlarmCardBinding(inflater, binding.viewAlarmsLinearlayout, referenceDate, referenceTZ )
            }
            else -> return
        }
        bindingAlarm?.cardAlarmDelete?.visibility = View.GONE
        binding.viewAlarmsLinearlayout.addView(bindingAlarm?.root)
    }


    private fun addSubtasksView(subtask: ICalObject?, linearLayout: LinearLayout) {

        if (subtask == null)
            return

        val subtaskBinding = FragmentIcalViewSubtaskBinding.inflate(inflater, linearLayout, false)

        var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

       var subtaskSummary =subtask.summary
        val subtaskCount = icalViewViewModel.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
        if (subtaskCount != null)
            subtaskSummary += " (+${subtaskCount})"
        subtaskBinding.viewSubtaskTextview.text = subtaskSummary
        subtaskBinding.viewSubtaskProgressSlider.value = subtask.percent?.toFloat() ?: 0F
        subtaskBinding.viewSubtaskProgressPercent.text = String.format("%.0f%%", subtask.percent?.toFloat() ?: 0F)
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

            linearLayout.addView(subtaskBinding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ical_view, menu)
        this.optionsMenu = menu
        if(icalViewViewModel.icalEntity.value?.ICalCollection?.readonly == true)
            hideEditingOptions()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_view_share_text -> {

                var shareText = ""
                icalViewViewModel.icalEntity.value?.property?.dtstart?.let { shareText += "${convertLongToFullDateTimeString(it, icalViewViewModel.icalEntity.value?.property?.dtstartTimezone)}\n" }
                icalViewViewModel.icalEntity.value?.property?.summary?.let { shareText += "${it}\n\n" }
                icalViewViewModel.icalEntity.value?.property?.description?.let { shareText += "${it}\n\n" }

                val categories: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value?.categories?.forEach { categories.add(it.text) }
                shareText += categories.joinToString(separator=", ")

                val attendees: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value?.attendees?.forEach { attendees.add(it.caladdress.removePrefix("mailto:")) }

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    type = "text/plain"
                    icalViewViewModel.icalEntity.value?.property?.summary?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_EMAIL, attendees.toTypedArray())
                }
                val files = ArrayList<Uri>()

                // prepare file attachment, the file is stored in the externalCacheDir and then provided through a FileProvider
                try {
                    val icsFileName = "${requireContext().externalCacheDir}/ics_file.ics"
                    val icsFile = File(icsFileName).apply {
                        val os = ByteArrayOutputStream()
                        icalViewViewModel.icalEntity.value!!.writeIcalOutputStream(os)
                        this.writeBytes(os.toByteArray())
                        createNewFile()
                    }
                    val uri = getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, icsFile)
                    files.add(uri)
                } catch (e: Exception) {
                    Log.i("fileprovider", "Failed to attach ICS File")
                    Toast.makeText(requireContext(), "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
                }

                icalViewViewModel.icalEntity.value?.attachments?.forEach {
                    try {
                        files.add(Uri.parse(it.uri))
                    } catch (e: NullPointerException) {
                        Log.i("Attachment", "Attachment Uri could not be parsed")
                    } catch (e: FileNotFoundException) {
                        Log.i("Attachment", "Attachment-File could not be accessed.")
                    }
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)

                Log.d("shareIntent", shareText)
                startActivity(Intent(shareIntent))
            }
            R.id.menu_view_share_ics -> {

                val shareText = icalViewViewModel.icalEntity.value!!.getIcalFormat().toString()
                Log.d("iCalFileContent", shareText)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/calendar"
                    putExtra(Intent.EXTRA_STREAM, shareText)
                }

                Log.d("shareIntent", shareText)
                startActivity(Intent(shareIntent))
            }
            R.id.menu_view_copy_as_journal -> this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.icalEntity.value?.getIcalEntityCopy(Module.JOURNAL.name) ?: return false)
                )

            R.id.menu_view_copy_as_note -> this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.icalEntity.value?.getIcalEntityCopy(Module.NOTE.name) ?: return false)
                )

            R.id.menu_view_copy_as_todo -> this.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(icalViewViewModel.icalEntity.value?.getIcalEntityCopy(Module.TODO.name) ?: return false)
                )

            R.id.menu_view_delete_item -> deleteItem()
            R.id.menu_view_syncnow -> SyncUtil.syncAccount(Account(icalViewViewModel.icalEntity.value?.ICalCollection?.accountName, icalViewViewModel.icalEntity.value?.ICalCollection?.accountType))
        }
        return super.onOptionsItemSelected(item)
    }


    private fun startRecording() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(requireContext())
        else
            MediaRecorder()
        recorder?.apply {
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
        // make sure the player is not running
        stopPlaying()

        // initialise the player
        fileName?.let {  fname ->
            player = MediaPlayer().apply {
                try {
                    setDataSource(requireContext(), fname)
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

        seekbar.max = player?.duration ?: 0

        seekbarHandler.postDelayed(object: Runnable {
            override fun run() {
                try {
                    if(playing) {
                        seekbar.progress = player?.currentPosition ?: 0
                        seekbarHandler.postDelayed(this, 10)
                    }
                } catch (e: Exception) {
                    seekbar.progress = 0

                }
            }
        }, 0)
    }


    private fun togglePlayback(seekbar: SeekBar, button: FloatingActionButton, fileToPlay: Uri?) {


        //stop playing if playback is on - but only with the current file. If the player is playing another file, then don't react
        if(playing && fileName == fileToPlay) {
            stopPlaying()
            button.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
            playing = false
            seekbar.progress = 0
        } else if (!playing) {
            // write the base64 decoded Bytestream in a file and use it as an input for the player
            //val fileBytestream = Base64.decode(relatedNote.attachmentValue, Base64.DEFAULT)
            fileName = fileToPlay

            startPlaying()
            initialiseSeekBar(seekbar)
            button.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop))
            playing = true

            // make sure to set the icon back to the play icon when the player reached the end
            player?.setOnCompletionListener {
                button.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                playing = false
            }
        }
    }

    /**
     * Shows a Dialog if the user really wants to delete the item.
     * If yes, a Toast is shown and the user is forwarded to the list view
     * If no, the dialog is closed.
     */
    private fun deleteItem() {

        // show Alert Dialog before the item gets really deleted
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.view_dialog_sure_to_delete_title, icalViewViewModel.icalEntity.value?.property?.summary ?: ""))
        builder.setMessage(getString(R.string.view_dialog_sure_to_delete_message, icalViewViewModel.icalEntity.value?.property?.summary ?: ""))
        builder.setPositiveButton(R.string.delete) { _, _ ->

            val summary = icalViewViewModel.icalEntity.value?.property?.summary ?: ""

            val direction = IcalViewFragmentDirections.actionIcalViewFragmentToIcalListFragment()
            direction.module2show = icalViewViewModel.icalEntity.value?.property?.module
            icalViewViewModel.delete(icalViewViewModel.icalEntity.value?.property!!)

            Toast.makeText(context, getString(R.string.view_toast_deleted_successfully, summary), Toast.LENGTH_LONG).show()

            Attachment.scheduleCleanupJob(requireContext())
            this.findNavController().navigate(direction)
        }
        builder.setNeutralButton(R.string.cancel) { _, _ -> }
        builder.show()
    }


    private fun hideEditingOptions() {
        binding.viewBottomBar.menu.findItem(R.id.menu_view_bottom_copy).isVisible = false
        binding.viewBottomBar.menu.findItem(R.id.menu_view_bottom_delete).isVisible = false
        binding.viewFabEdit.visibility = View.GONE
        optionsMenu?.findItem(R.id.menu_view_delete_item)?.isVisible = false
        binding.viewAddNote.visibility = View.GONE
        binding.viewAddAudioNote.visibility = View.GONE
        binding.viewReadyonly.visibility = View.VISIBLE
    }
}

