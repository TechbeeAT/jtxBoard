/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.databinding.FragmentIcalEditBinding
import at.techbee.jtx.databinding.FragmentIcalEditColorpickerDialogBinding
import at.techbee.jtx.databinding.FragmentIcalEditCommentBinding
import at.techbee.jtx.databinding.FragmentIcalEditSubtaskBinding
import at.techbee.jtx.flavored.JtxReviewManager
import at.techbee.jtx.flavored.MapManager
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_ALARMS
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_RECURRING
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_SUBTASKS
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import net.fortuna.ical4j.model.Recur


class IcalEditFragment : Fragment() {

    private var _binding: FragmentIcalEditBinding? = null
    val binding get() = _binding!!

    private lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    lateinit var icalEditViewModel: IcalEditViewModel
    private lateinit var inflater: LayoutInflater
    private var container: ViewGroup? = null
    private var menu: Menu? = null


    companion object {

        const val PREFS_EDIT_VIEW = "sharedPreferencesEditView"
        const val PREFS_LAST_COLLECTION = "lastUsedCollection"
        const val PREFS_CONTACTS_PERMISSION_SHOWN = "contactsPermissionShown"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.

        this.inflater = inflater
        this._binding = FragmentIcalEditBinding.inflate(inflater, container, false)
        this.container = container
        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalEditFragmentArgs.fromBundle((requireArguments()))
        val prefs = activity?.getSharedPreferences(PREFS_EDIT_VIEW, Context.MODE_PRIVATE)!!

        // add menu
        setHasOptionsMenu(true)

        // add markwon to description edittext
        val markwon = Markwon.create(requireContext())
        val markwonEditor = MarkwonEditor.create(markwon)
        binding.editFragmentTabGeneral.editDescriptionEdittext.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(markwonEditor))


        val model: IcalEditViewModel by viewModels { IcalEditViewModelFactory(application, arguments.icalentity) }
        icalEditViewModel = model
        binding.model = icalEditViewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.editFragmentTabGeneral.editCollectionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                    icalEditViewModel.selectedCollectionId = icalEditViewModel.allCollections.value?.get(pos)?.collectionId ?: return
                    icalEditViewModel.iCalObjectUpdated.value?.collectionId = icalEditViewModel.selectedCollectionId ?: icalEditViewModel.allCollections.value?.first()?.collectionId ?: return

                    //Don't show the subtasks tab if the collection doesn't support VTODO
                    val currentCollection = icalEditViewModel.allCollections.value?.find { col -> col.collectionId == icalEditViewModel.iCalObjectUpdated.value?.collectionId }
                    if(currentCollection?.supportsVTODO != true)
                        binding.icalEditTabs.getTabAt(TAB_SUBTASKS)?.view?.visibility = View.GONE
                    else
                        binding.icalEditTabs.getTabAt(TAB_SUBTASKS)?.view?.visibility = View.VISIBLE

                    icalEditViewModel.allCollections.removeObservers(viewLifecycleOwner)     // make sure the selection doesn't change anymore by any sync happening that affects the oberser/collection-lsit
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }



        //pre-set rules if rrule is present
        if(icalEditViewModel.iCalEntity.property.rrule!= null) {

            try {

                val recur = Recur(icalEditViewModel.iCalEntity.property.rrule)

                //if(icalEditViewModel.recurrenceMode.value == RECURRENCE_MODE_UNSUPPORTED)
                //    throw Exception("Unsupported recurrence mode detected")

                if(recur.experimentalValues.isNotEmpty() || recur.hourList.isNotEmpty() || recur.minuteList.isNotEmpty() || recur.monthList.isNotEmpty() || recur.secondList.isNotEmpty() || recur.setPosList.isNotEmpty() || recur.skip != null || recur.weekNoList.isNotEmpty() || recur.weekStartDay != null || recur.yearDayList.isNotEmpty())
                    throw Exception("Unsupported values detected")

                binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.value =
                    icalEditViewModel.iCalEntity.property.retrieveCount()

                binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.value =
                    if(recur.interval <1) 1 else recur.interval


                //pre-select the day of the month according to the rrule
                /*
                if (icalEditViewModel.recurrenceMode.value == RECURRENCE_MODE_MONTH) {
                    if(recur.monthDayList.size != 1)
                        throw Exception("Recurrence mode Monthly but no day or multiple days were set")
                    val selectedMonth = Recur(icalEditViewModel.iCalEntity.property.rrule).monthDayList[0]
                    binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.value = selectedMonth
                }

                 */
            } catch (e: Exception) {
                Log.w("LoadRRule", "Failed to preset UI according to provided RRule\n$e")

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.edit_fragment_recur_unknown_rrule_dialog_title))
                    .setMessage(getString(R.string.edit_fragment_recur_unknown_rrule_dialog_message))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        icalEditViewModel.iCalObjectUpdated.value?.rrule = null
                        icalEditViewModel.iCalObjectUpdated.value?.rdate = null
                        icalEditViewModel.iCalObjectUpdated.value?.exdate = null
                        binding.editFragmentIcalEditRecur.editRecurSwitch.isChecked = false
                    }
                    .show()
            }
        }


        if(BuildConfig.FLAVOR == MainActivity.BUILD_FLAVOR_GOOGLEPLAY) {
            binding.editFragmentTabUlc.editLocationEdit.setEndIconOnClickListener {
                MapManager(requireContext()).showLocationPickerDialog(
                    inflater,
                    icalEditViewModel.iCalObjectUpdated
                )
            }
        } else {
            binding.editFragmentTabUlc.editLocationEdit.isEndIconVisible = false
        }

        icalEditViewModel.savingClicked.observe(viewLifecycleOwner) {
            if (it == true) {

                // do some validation first
                if (!isDataValid())
                    return@observe

                icalEditViewModel.iCalObjectUpdated.value!!.percent =
                    binding.editFragmentTabGeneral.editProgressSlider.value.toInt()
                prefs.edit().putLong(
                    PREFS_LAST_COLLECTION,
                    icalEditViewModel.selectedCollectionId ?: icalEditViewModel.iCalObjectUpdated.value!!.collectionId
                ).apply()

                icalEditViewModel.update()
            }
        }

        icalEditViewModel.collectionNotFoundError.observe(viewLifecycleOwner) { error ->

            if (!error)
                return@observe

            // show a dialog to inform the user
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.edit_dialog_collection_not_found_error_title))
            builder.setMessage(getString(R.string.edit_dialog_collection_not_found_error_message))
            builder.setIcon(R.drawable.ic_error)
            builder.setPositiveButton(R.string.ok) { _, _ -> }
            builder.show()
        }


        icalEditViewModel.returnIcalObjectId.observe(viewLifecycleOwner) {

            icalEditViewModel.savingClicked.value = false

            if (it != 0L) {
                // saving is done now
                //hideKeyboard()
                SyncUtil.notifyContentObservers(context)

                // ask for a review (if applicable)
                JtxReviewManager(requireActivity()).launch()

                // return to list view
                val direction =
                    IcalEditFragmentDirections.actionIcalEditFragmentToIcalListFragment()
                direction.module2show = icalEditViewModel.iCalObjectUpdated.value!!.module
                direction.item2focus = it

                /*  // ALTERNATVE return to view fragment
                val direction = IcalEditFragmentDirections.actionIcalEditFragmentToIcalViewFragment()
                direction.item2show = it
                 */
                this.findNavController().navigate(direction)
            }
        }

        icalEditViewModel.entryDeleted.observe(viewLifecycleOwner) {

            if (it) {
                // saving is done now
                //hideKeyboard()
                SyncUtil.notifyContentObservers(context)
                icalEditViewModel.entryDeleted.value = false

                val summary = icalEditViewModel.iCalObjectUpdated.value?.summary
                Toast.makeText(context, getString(R.string.edit_toast_deleted_successfully, summary), Toast.LENGTH_LONG).show()

                context?.let { context -> Attachment.scheduleCleanupJob(context) }

                // return to list view
                val direction = IcalEditFragmentDirections.actionIcalEditFragmentToIcalListFragment()
                direction.module2show = icalEditViewModel.iCalObjectUpdated.value!!.module
                this.findNavController().navigate(direction)
            }
        }


        icalEditViewModel.iCalObjectUpdated.observe(viewLifecycleOwner) {

            binding.editFragmentTabGeneral.editProgressPercent.text = String.format("%.0f%%", it.percent?.toFloat() ?: 0F)


            // show the reset dates menu item if it is a to-do
            if(it.module == Module.TODO.name)
                menu?.findItem(R.id.menu_edit_clear_dates)?.isVisible = true

            // if the item has an original Id, the user chose to unlink the recurring instance from the original, the recurring values need to be deleted
            if(it.isRecurLinkedInstance) {
                it.rrule = null
                it.exdate = null
                it.rdate = null
                it.isRecurLinkedInstance = false    // remove the link
            }
        }


        //TODO: Check if the Sequence was updated in the meantime and notify user!


        // initialize allRelatedto
        icalEditViewModel.isChild.observe(viewLifecycleOwner) {

            // if the current item is a child, changing the collection is not allowed; also making it recurring is not allowed
            if (it) {
                binding.editFragmentTabGeneral.editCollectionSpinner.isEnabled = false
                binding.editFragmentIcalEditRecur.editRecurSwitch.isEnabled = false
            }
        }

        return binding.root
    }


    private fun isDataValid(): Boolean {

        var isValid = true
        var validationError = ""

        if(icalEditViewModel.iCalObjectUpdated.value?.summary.isNullOrBlank() && icalEditViewModel.iCalObjectUpdated.value?.description.isNullOrBlank())
            validationError += resources.getString(R.string.edit_validation_errors_summary_or_description_necessary) + "\n"
        if(icalEditViewModel.iCalObjectUpdated.value?.module == Module.TODO.name && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && icalEditViewModel.iCalObjectUpdated.value?.due != null &&
        ((icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone?.isNotEmpty() == true && icalEditViewModel.iCalObjectUpdated.value?.dueTimezone.isNullOrEmpty())
            || (icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone.isNullOrEmpty() && icalEditViewModel.iCalObjectUpdated.value?.dueTimezone?.isNotEmpty() == true)))
                validationError += resources.getString(R.string.edit_validation_errors_start_due_timezone_check) + "\n"

        if(binding.editFragmentTabGeneral.editCategoriesAddAutocomplete.text.isNotEmpty())
            validationError += resources.getString(R.string.edit_validation_errors_category_not_confirmed) + "\n"
        if(binding.editFragmentTabCar.editAttendeesAddAutocomplete.text.isNotEmpty())
            validationError += resources.getString(R.string.edit_validation_errors_attendee_not_confirmed) + "\n"
        if(binding.editFragmentTabCar.editResourcesAddAutocomplete.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_resource_not_confirmed) + "\n"
        if(binding.editFragmentTabUlc.editCommentAddEdittext.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_comment_not_confirmed) + "\n"
        if(binding.editFragmentTabSubtasks.editSubtasksAddEdittext.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_subtask_not_confirmed) + "\n"
/*
        if(binding.editTaskDatesFragment.editDueDateEdittext.text?.isNotEmpty() == true && binding.editTaskDatesFragment.editDueTimeEdittext.text.isNullOrBlank() && binding.editTaskDatesFragment.editTaskAddStartedAndDueTimeSwitch.isActivated)
            validationError += resources.getString(R.string.edit_validation_errors_due_time_not_set) + "\n"
        if(binding.editTaskDatesFragment.editStartedDateEdittext.text?.isNotEmpty() == true && binding.editTaskDatesFragment.editStartedTimeEdittext.text.isNullOrBlank() && binding.editTaskDatesFragment.editTaskAddStartedAndDueTimeSwitch.isActivated)
            validationError += resources.getString(R.string.edit_validation_errors_start_time_not_set) + "\n"

 */
/*        if(binding.editCompletedTimeEdittext?.text.isNullOrBlank() && binding.editCompletedAddtimeSwitch?.isActivated == false)
            validationError += resources.getString(R.string.edit_validation_errors_completed_time_not_set) + "\n"
 */

        if(validationError.isNotEmpty()) {
            isValid = false

            validationError = resources.getString(R.string.edit_validation_errors_detected) + "\n\n" + validationError

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_validation_errors_dialog_header)
                .setMessage(validationError)
                .setPositiveButton(R.string.ok) { _, _ ->   }
                .show()
        }

        return isValid

    }

}
