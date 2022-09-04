/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.accounts.Account
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.AUTHORITY_FILEPROVIDER
import at.techbee.jtx.NavigationDirections
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.databinding.FragmentIcalViewBinding
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.MapManager
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateTimeString
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException


class IcalViewFragment : Fragment() {

    private var _binding: FragmentIcalViewBinding? = null
    val binding get() = _binding!!

    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    lateinit var icalViewViewModel: IcalViewViewModel
    private var optionsMenu: Menu? = null

    private var summary2delete: String = ""

    private lateinit var settings: SharedPreferences




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


            it.property.recurOriginalIcalObjectId?.let { origId ->
                binding.viewRecurrenceGotooriginalButton.setOnClickListener { view ->
                    view.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(origId)
                    )
                }
            }
      }



        // show ads only for AdFlavors and if the subscription was not purchased (gplay flavor only)
        if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
            AdManager.getInstance()?.addAdViewToContainerViewFragment(binding.viewAdContainer, requireContext(), AdManager.getInstance()?.unitIdBannerView)
        else
            binding.viewAdContainer.visibility = View.GONE

        return binding.root
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
                        try {
                            startActivity(Intent(shareIntent))
                        } catch (e: ActivityNotFoundException) {
                            Log.i("ActivityNotFound", "No activity found to send this entry.")
                            Toast.makeText(requireContext(), "No app found to send this entry.", Toast.LENGTH_SHORT).show()
                        } finally {
                            icalViewViewModel.icsFileWritten.removeObservers(viewLifecycleOwner)
                            icalViewViewModel.icsFileWritten.postValue(null)
                        }
                    }
                }
            }
            R.id.menu_view_share_ics -> {
                icalViewViewModel.retrieveICSFormat()
                icalViewViewModel.icsFormat.observe(viewLifecycleOwner) { ics ->
                    if(ics.isNullOrEmpty())
                        return@observe

                    val icsShareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/calendar"
                    }

                    try {
                        val icsFileName = "${requireContext().externalCacheDir}/ics_file.ics"
                        val icsFile = File(icsFileName).apply {
                            this.writeBytes(ics.toByteArray())
                            createNewFile()
                        }
                        val uri = getUriForFile(requireContext(), AUTHORITY_FILEPROVIDER, icsFile)
                        icsShareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                        startActivity(Intent(icsShareIntent))
                    } catch (e: ActivityNotFoundException) {
                        Log.i("ActivityNotFound", "No activity found to open file.")
                        Toast.makeText(requireContext(), "No app found to open this file.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.i("fileprovider", "Failed to attach ICS File")
                        Toast.makeText(requireContext(), "Failed to attach ICS File.", Toast.LENGTH_SHORT).show()
                    } finally {
                        icalViewViewModel.icsFormat.removeObservers(viewLifecycleOwner)
                        icalViewViewModel.icsFormat.postValue(null)
                    }
                }
            }

            R.id.menu_view_syncnow -> SyncUtil.syncAccount(Account(icalViewViewModel.icalEntity.value?.ICalCollection?.accountName, icalViewViewModel.icalEntity.value?.ICalCollection?.accountType))
        }
        return super.onOptionsItemSelected(item)
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
}

