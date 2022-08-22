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
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.NavigationDirections
import at.techbee.jtx.PermissionsHelper
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.databinding.FragmentIcalViewAudioDialogBinding
import at.techbee.jtx.databinding.FragmentIcalViewBinding
import at.techbee.jtx.databinding.FragmentIcalViewCommentBinding
import at.techbee.jtx.databinding.FragmentIcalViewSubtaskBinding
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.MapManager
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateTimeString
import at.techbee.jtx.util.DateTimeUtils.getLongListfromCSVString
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class IcalViewFragment : Fragment() {

    private var _binding: FragmentIcalViewBinding? = null
    val binding get() = _binding!!

    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    lateinit var icalViewViewModel: IcalViewViewModel
    private var optionsMenu: Menu? = null

    private var fileName: Uri? = null
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private val seekbarHandler = Handler(Looper.getMainLooper())

    private var recording: Boolean = false
    private var playing: Boolean = false

    private var summary2delete: String = ""

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
        this._binding = FragmentIcalViewBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalViewFragmentArgs.fromBundle((requireArguments()))

        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .build()

        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())
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
        val model: IcalViewViewModel by viewModels { IcalViewViewModelFactory(application, arguments.item2show) }
        icalViewViewModel = model
        binding.model = icalViewViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            icalViewViewModel.showSyncProgressIndicator.postValue(
                SyncUtil.isJtxSyncRunningForAccount(
                    Account(icalViewViewModel.icalEntity.value?.ICalCollection?.accountName, icalViewViewModel.icalEntity.value?.ICalCollection?.accountType)
                ))
        }

        // set up observers
        icalViewViewModel.entryToEdit.observe(viewLifecycleOwner) {
            if (it != null) {
                icalViewViewModel.entryToEdit.value = null

                // if the item is an instance of a recurring entry, make sure that the user is aware of this
                val originalId =
                    icalViewViewModel.icalEntity.value?.property?.recurOriginalIcalObjectId
                if (originalId != null && icalViewViewModel.icalEntity.value?.property?.isRecurLinkedInstance == true) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.view_recurrence_note_to_original_dialog_header))
                        .setMessage(getString(R.string.view_recurrence_note_to_original))
                        .setPositiveButton("Continue") { _, _ ->
                            icalViewViewModel.icalEntity.value?.let { entity ->
                                this.findNavController().navigate(
                                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(
                                        entity
                                    )
                                )

                            }
                        }
                        .setNegativeButton("Go to Original") { _, _ ->
                            this.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf()
                                    .setItem2show(originalId)
                            )
                        }
                        .show()
                } else {
                    this.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(it)
                    )
                }
            }
        }

        icalViewViewModel.entryDeleted.observe(viewLifecycleOwner) {
            if(it) {
                Attachment.scheduleCleanupJob(requireContext())
                SyncUtil.notifyContentObservers(context)

                Toast.makeText(context, getString(R.string.view_toast_deleted_successfully, summary2delete), Toast.LENGTH_LONG).show()

                val direction = IcalViewFragmentDirections.actionIcalViewFragmentToIcalListFragment()
                direction.module2show = icalViewViewModel.icalEntity.value?.property?.module
                this.findNavController().navigate(direction)
                icalViewViewModel.entryDeleted.value = false
            }

        }

        icalViewViewModel.icalEntity.observe(viewLifecycleOwner) {

            if (it == null) {
                if(summary2delete.isEmpty())
                    Toast.makeText(context, R.string.view_toast_entry_does_not_exist_anymore, Toast.LENGTH_LONG).show()
                findNavController().navigate(NavigationDirections.actionGlobalIcalListFragment())
                return@observe   // just make sure that nothing else happens
            }

            if (it.ICalCollection?.readonly == true)
                hideEditingOptions()

            if(it.ICalCollection?.readonly == false
                && it.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE
                && BillingManager.getInstance()?.isProPurchased?.value == false) {
                hideEditingOptions()
                val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_remote_entries_blocked, Snackbar.LENGTH_INDEFINITE)
                //snackbar.setAction(R.string.more) {
                    //findNavController().navigate(R.id.action_global_buyProFragment)
                //}
                snackbar.show()
            }

            if (!SyncUtil.isDAVx5CompatibleWithJTX(application) || it.ICalCollection?.accountType == LOCAL_ACCOUNT_TYPE)
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
                    priorityArray[icalViewViewModel.icalEntity.value?.property?.priority ?: 0]

            // don't show the option to add notes if VJOURNAL is not supported (only relevant if the current entry is a VTODO)
            if (it.ICalCollection?.supportsVJOURNAL != true) {
                binding.viewAddNoteTextinputlayout.visibility = View.GONE
                binding.viewAddAudioNote.visibility = View.GONE
            }

            // setting the description with Markdown
            it.property.description?.let { desc ->
                val descMarkwon = markwon.toMarkdown(desc)
                binding.viewDescription.text = descMarkwon
            }

            if(it.property.geoLat != null && it.property.geoLong != null)
                MapManager(requireContext()).addMap(binding.viewLocationMap, it.property.geoLat!!, it.property.geoLong!!, it.property.location)

            binding.viewCommentsLinearlayout.removeAllViews()
            it.comments?.forEach { comment ->
                val commentBinding =
                    FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                commentBinding.viewCommentTextview.text = comment.text
                binding.viewCommentsLinearlayout.addView(commentBinding.root)
            }


            binding.viewAlarmsLinearlayout.removeAllViews()      // remove all views if something has changed to rebuild from scratch
            it.alarms?.forEach { alarm ->
                addAlarmView(alarm)
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
                allExceptionsString += convertLongToFullDateTimeString(
                    exdate,
                    it.property.dtstartTimezone
                ) + "\n"
            }
            binding.viewRecurrenceExceptionItems.text = allExceptionsString

            var allAdditionsString = ""
            getLongListfromCSVString(it.property.rdate).forEach { rdate ->
                allAdditionsString += convertLongToFullDateTimeString(
                    rdate,
                    it.property.dtstartTimezone
                ) + "\n"
            }
            binding.viewRecurrenceAdditionsItems.text = allAdditionsString

        }

        icalViewViewModel.relatedNotes.observe(viewLifecycleOwner) {

            if (playing)             // don't interrupt if audio is currently played
                return@observe

            if (it?.size != 0) {
                binding.viewFeedbackLinearlayout.removeAllViews()
                it.sortedBy { item -> item.sortIndex }
                    .forEach { relatedNote ->

                        val commentBinding =
                        FragmentIcalViewCommentBinding.inflate(inflater, container, false)
                    if (relatedNote.summary?.isNotEmpty() == true)
                        commentBinding.viewCommentTextview.text = relatedNote.summary
                    else
                        commentBinding.viewCommentTextview.visibility = View.GONE

                    relatedNote.audioAttachment?.let { audioUri ->
                        commentBinding.viewCommentPlaybutton.visibility = View.VISIBLE
                        commentBinding.viewCommentProgressbar.visibility = View.VISIBLE
                        commentBinding.viewCommentPlaybutton.setOnClickListener {

                            val uri = Uri.parse(audioUri)
                            togglePlayback(
                                commentBinding.viewCommentProgressbar,
                                commentBinding.viewCommentPlaybutton,
                                uri
                            )
                        }
                    }
                    commentBinding.root.setOnClickListener { view ->
                        view.findNavController().navigate(
                            IcalViewFragmentDirections.actionIcalViewFragmentSelf()
                                .setItem2show(relatedNote.id)
                        )
                    }
                    commentBinding.root.setOnLongClickListener {
                        icalViewViewModel.retrieveSubEntryToEdit(relatedNote.id)
                        true
                    }
                    binding.viewFeedbackLinearlayout.addView(commentBinding.root)
                }
            } else if (it.isEmpty() && icalViewViewModel.icalEntity.value?.ICalCollection?.readonly == true) {   // don't show header for subnotes if read only and no subnotes are present
                binding.viewFeedbackHeader.visibility = View.GONE
                binding.viewFeedbackLinearlayout.visibility = View.GONE
            }
        }

        icalViewViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            binding.viewSubtasksLinearlayout.removeAllViews()
            it.sortedBy { item -> item.sortIndex }
                .forEach { singleSubtask ->
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


        binding.viewAddNoteTextinputlayout.setEndIconOnClickListener {
            if(binding.viewAddNoteEdittext.text.toString().isNotEmpty())
                icalViewViewModel.insertRelated(ICalObject.createNote(binding.viewAddNoteEdittext.text.toString()), null)
            binding.viewAddNoteEdittext.text?.clear()
        }

        binding.viewAddNoteTextinputlayout.editText?.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if(binding.viewAddNoteEdittext.text.toString().isNotEmpty()) {
                        icalViewViewModel.insertRelated(ICalObject.createNote(binding.viewAddNoteEdittext.text.toString()),null)
                        binding.viewAddNoteEdittext.text?.clear()
                    }
                    true
                }
                else -> false
            }
        }

        // handling audio recording
        binding.viewAddAudioNote.setOnClickListener {
            openAddAudioNoteDialog()
        }

        binding.viewSubtasksAdd.setEndIconOnClickListener {
            if(binding.viewSubtasksAddEdittext.text.toString().isNotEmpty())
                icalViewViewModel.insertRelated(ICalObject.createTask(binding.viewSubtasksAddEdittext.text.toString()), null)
            binding.viewSubtasksAddEdittext.text?.clear()
        }

        binding.viewSubtasksAdd.editText?.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if(binding.viewSubtasksAddEdittext.text.toString().isNotEmpty()) {
                        icalViewViewModel.insertRelated(ICalObject.createTask(binding.viewSubtasksAddEdittext.text.toString()),null)
                        binding.viewSubtasksAddEdittext.text?.clear()
                    }
                    true
                }
                else -> false
            }
        }

        var resetProgress = icalViewViewModel.icalEntity.value?.property?.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

        binding.viewProgressSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (binding.viewProgressSlider.value.toInt() < 100)
                    resetProgress = binding.viewProgressSlider.value.toInt()
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property.id, binding.viewProgressSlider.value.toInt())
            }
        })

        binding.viewProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property.id, 100)
            } else {
                icalViewViewModel.updateProgress(icalViewViewModel.icalEntity.value!!.property.id, resetProgress)
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
        if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
            AdManager.getInstance()?.addAdViewToContainerViewFragment(binding.viewAdContainer, requireContext(), AdManager.getInstance()?.unitIdBannerView)
        else
            binding.viewAdContainer.visibility = View.GONE


        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


    private fun addSubtasksView(subtask: ICal4List?, linearLayout: LinearLayout) {

        if (subtask == null)
            return

        val subtaskBinding = FragmentIcalViewSubtaskBinding.inflate(inflater, linearLayout, false)

        var subtaskSummary =subtask.summary
        if (subtask.numSubtasks > 0)
            subtaskSummary += " (+${subtask.numSubtasks})"
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
                icalViewViewModel.updateProgress(subtask.id, subtaskBinding.viewSubtaskProgressSlider.value.toInt())
            }
        })

        subtaskBinding.viewSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                icalViewViewModel.updateProgress(subtask.id, 100)
            } else {
                icalViewViewModel.updateProgress(subtask.id, 0)
            }

        }

        subtaskBinding.root.setOnClickListener {
            it.findNavController().navigate(
                    IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(subtask.id))
        }

        subtaskBinding.root.setOnLongClickListener {
            icalViewViewModel.retrieveSubEntryToEdit(subtask.id)
            true
        }

        if(icalViewViewModel.icalEntity.value?.ICalCollection?.readonly == true) {
            subtaskBinding.viewSubtaskProgressCheckbox.isEnabled = false
            subtaskBinding.viewSubtaskProgressSlider.isEnabled = false
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
                icalViewViewModel.icalEntity.value?.property?.dtstart?.let { shareText += getString(R.string.view_started) + ": " + convertLongToFullDateTimeString(it, icalViewViewModel.icalEntity.value?.property?.dtstartTimezone) + System.lineSeparator() + System.lineSeparator()}
                icalViewViewModel.icalEntity.value?.property?.due?.let { shareText += getString(R.string.view_due) + ": " + convertLongToFullDateTimeString(it, icalViewViewModel.icalEntity.value?.property?.dueTimezone) + System.lineSeparator() }
                icalViewViewModel.icalEntity.value?.property?.completed?.let { shareText += getString(R.string.view_completed) + ": " + convertLongToFullDateTimeString(it, icalViewViewModel.icalEntity.value?.property?.completedTimezone) + System.lineSeparator() }
                icalViewViewModel.icalEntity.value?.property?.getRecurInfo(context)?.let { shareText += it }
                icalViewViewModel.icalEntity.value?.property?.summary?.let { shareText += it + System.lineSeparator() }
                icalViewViewModel.icalEntity.value?.property?.description?.let { shareText += it + System.lineSeparator() + System.lineSeparator() }

                val categories: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value?.categories?.forEach { categories.add(it.text) }
                if(categories.isNotEmpty())
                    shareText += getString(R.string.categories) + ": " + categories.joinToString(separator=", ") + System.lineSeparator()

                if(icalViewViewModel.icalEntity.value?.property?.contact?.isNotEmpty() == true)
                    shareText += getString(R.string.contact) + ": " + icalViewViewModel.icalEntity.value?.property?.contact + System.lineSeparator()

                if(icalViewViewModel.icalEntity.value?.property?.location?.isNotEmpty() == true)
                    shareText += getString(R.string.location) + ": " + icalViewViewModel.icalEntity.value?.property?.location + System.lineSeparator()

                if(icalViewViewModel.icalEntity.value?.property?.url?.isNotEmpty() == true)
                    shareText += getString(R.string.url) + ": " + icalViewViewModel.icalEntity.value?.property?.url + System.lineSeparator()

                val resources: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value?.resources?.forEach { resource -> resource.text?.let { resources.add(it) } }
                if(resources.isNotEmpty())
                    shareText += getString(R.string.resources) + ": " + resources.joinToString(separator=", ") + System.lineSeparator()

                val attachments: MutableList<String> = mutableListOf()
                icalViewViewModel.icalEntity.value?.attachments?.forEach { attachment ->
                    if(attachment.uri?.startsWith("http") == true)
                        attachments.add(attachment.uri!!)
                }
                if(attachments.isNotEmpty())
                    shareText += getString(R.string.attachments) + ": " + System.lineSeparator() + attachments.joinToString(separator=System.lineSeparator()) + System.lineSeparator()

                shareText = shareText.trim()

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

                // prepare output stream for the ics attachment, the file is stored in the externalCacheDir and then provided through a FileProvider
                // further processing happens in the observer!
                val os = ByteArrayOutputStream()
                icalViewViewModel.writeICSFile(os)

                icalViewViewModel.icalEntity.value?.attachments?.forEach {
                    try {
                        files.add(Uri.parse(it.uri))
                    } catch (e: NullPointerException) {
                        Log.i("Attachment", "Attachment Uri could not be parsed")
                    } catch (e: FileNotFoundException) {
                        Log.i("Attachment", "Attachment-File could not be accessed.")
                    }
                }

                icalViewViewModel.icsFileWritten.observe(viewLifecycleOwner) {
                    if (it == true) {
                        try {
                            val icsFileName = "${requireContext().externalCacheDir}/ics_file.ics"
                            val icsFile = File(icsFileName).apply {
                                this.writeBytes(os.toByteArray())
                                createNewFile()
                            }
                            val uri = getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, icsFile)
                            files.add(uri)
                        } catch (e: Exception) {
                            Log.i("fileprovider", "Failed to attach ICS File")
                            Toast.makeText(requireContext(), "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
                        }

                        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                        Log.d("shareIntent", shareText)

                        // start the intent when the file is ready
                        startActivity(Intent(shareIntent))
                        icalViewViewModel.icsFileWritten.removeObservers(viewLifecycleOwner)
                        icalViewViewModel.icsFileWritten.postValue(null)
                    }
                }
            }
            R.id.menu_view_share_ics -> {
                icalViewViewModel.retrieveICSFormat()
                icalViewViewModel.icsFormat.observe(viewLifecycleOwner) { ics ->
                    if(ics.isNullOrEmpty())
                        return@observe

                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/calendar"
                        //putExtra(Intent.EXTRA_TEXT, ics)
                        putExtra(Intent.EXTRA_STREAM, ics)
                    }
                    startActivity(Intent(shareIntent))
                    icalViewViewModel.icsFormat.removeObservers(viewLifecycleOwner)
                    icalViewViewModel.icsFormat.postValue(null)
                }
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
            summary2delete = icalViewViewModel.icalEntity.value?.property?.summary ?: ""
            icalViewViewModel.delete(icalViewViewModel.icalEntity.value?.property!!)
        }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.show()
    }


    private fun hideEditingOptions() {
        binding.viewBottomBar.menu.findItem(R.id.menu_view_bottom_copy).isVisible = false
        binding.viewBottomBar.menu.findItem(R.id.menu_view_bottom_delete).isVisible = false
        binding.viewFabEdit.visibility = View.GONE
        optionsMenu?.findItem(R.id.menu_view_delete_item)?.isVisible = false
        binding.viewAddNoteTextinputlayout.visibility = View.GONE

        binding.viewAddAudioNote.visibility = View.GONE
        binding.viewReadyonly.visibility = View.VISIBLE
        binding.viewBottomBar.visibility = View.GONE

        binding.viewSubtasksAdd.isEnabled = false
        binding.viewAddNoteTextinputlayout.isEnabled = false
        binding.viewProgressSlider.isEnabled = false
        binding.viewProgressCheckbox.isEnabled = false
    }


    private fun openAddAudioNoteDialog() {

        // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
        if (PermissionsHelper.checkPermissionRecordAudio(requireActivity())) {

            val audioDialogBinding = FragmentIcalViewAudioDialogBinding.inflate(inflater)

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
                            icalViewViewModel.insertRelated(ICalObject.createNote(), newAttachment)

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
}

