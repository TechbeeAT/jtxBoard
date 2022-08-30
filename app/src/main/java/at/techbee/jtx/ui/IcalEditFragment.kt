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
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.database.*
import at.techbee.jtx.database.Component
import at.techbee.jtx.database.properties.Alarm
import at.techbee.jtx.database.properties.AlarmRelativeTo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.properties.Comment
import at.techbee.jtx.databinding.FragmentIcalEditBinding
import at.techbee.jtx.databinding.FragmentIcalEditColorpickerDialogBinding
import at.techbee.jtx.databinding.FragmentIcalEditCommentBinding
import at.techbee.jtx.databinding.FragmentIcalEditSubtaskBinding
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.JtxReviewManager
import at.techbee.jtx.flavored.MapManager
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_END_AFTER
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_END_NEVER
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_END_ON
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_MODE_DAY
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_MODE_MONTH
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_MODE_UNSUPPORTED
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_MODE_WEEK
import at.techbee.jtx.ui.IcalEditViewModel.Companion.RECURRENCE_MODE_YEAR
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_ALARMS
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_ATTACHMENTS
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_GENERAL
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_LOC_COMMENTS
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_PEOPLE_RES
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_RECURRING
import at.techbee.jtx.ui.IcalEditViewModel.Companion.TAB_SUBTASKS
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateString
import at.techbee.jtx.util.DateTimeUtils.convertLongToFullDateTimeString
import at.techbee.jtx.util.DateTimeUtils.getDateWithoutTime
import at.techbee.jtx.util.DateTimeUtils.getLocalizedWeekdays
import at.techbee.jtx.util.DateTimeUtils.getLongListfromCSVString
import at.techbee.jtx.util.DateTimeUtils.isLocalizedWeekstartMonday
import at.techbee.jtx.util.DateTimeUtils.requireTzId
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import io.noties.markwon.Markwon
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import net.fortuna.ical4j.model.*
import java.time.*
import java.time.temporal.ChronoUnit


class IcalEditFragment : Fragment() {

    private var _binding: FragmentIcalEditBinding? = null
    val binding get() = _binding!!

    private lateinit var application: Application
    private lateinit var dataSource: ICalDatabaseDao
    lateinit var icalEditViewModel: IcalEditViewModel
    private lateinit var inflater: LayoutInflater
    private var container: ViewGroup? = null
    private var menu: Menu? = null

    private var rruleUntil: Long = System.currentTimeMillis()

    private var toastNoDtstart: Toast? = null


    companion object {
        const val TAG_PICKER_DTSTART = "dtstart"
        const val TAG_PICKER_DUE = "due"
        const val TAG_PICKER_COMPLETED = "completed"
        const val TAG_PICKER_RECUR_UNTIL = "recurUntil"

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


        //Don't show the recurring tab for Notes
        if(icalEditViewModel.iCalEntity.property.module == Module.NOTE.name && binding.icalEditTabs.tabCount >= TAB_RECURRING)
            binding.icalEditTabs.getTabAt(TAB_RECURRING)?.view?.visibility = View.GONE

        // VJOURNALs are not allowed to have Alarms!
        if(icalEditViewModel.iCalEntity.property.component == Component.VJOURNAL.name)
            binding.icalEditTabs.getTabAt(TAB_ALARMS)?.view?.visibility = View.GONE

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

        binding.editFragmentIcalEditAlarm.editAlarmsButtonCustom.setOnClickListener {
            var zonedTimestamp = ZonedDateTime.now()

            // Build constraints.
            // Create a custom date validator to only enable dates that are in the list
            val onlyFutureDatesValidator = object : CalendarConstraints.DateValidator {
                override fun writeToParcel(p0: Parcel, p1: Int) { }
                override fun describeContents(): Int { return 0 }
                override fun isValid(date: Long): Boolean = date >= DateTimeUtils.getTodayAsLong()
            }


            val constraints = CalendarConstraints.Builder().apply {
                setStart(zonedTimestamp.toInstant().toEpochMilli())
                setValidator(onlyFutureDatesValidator)
            }.build()
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.edit_datepicker_dialog_select_date)
                    .setSelection(zonedTimestamp.toInstant().toEpochMilli())
                    .setCalendarConstraints(constraints)
                    .build()

            val clockFormat = if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H



            val timePicker =
                MaterialTimePicker.Builder()
                    .setTitleText(R.string.edit_datepicker_dialog_select_time)
                    .setTimeFormat(clockFormat)
                    .build()

            timePicker.addOnPositiveButtonClickListener {
                zonedTimestamp = zonedTimestamp
                    .withHour(timePicker.hour)
                    .withMinute(timePicker.minute)
                val newAlarm = Alarm.createDisplayAlarm(zonedTimestamp.toInstant().toEpochMilli(), null)
                if(!icalEditViewModel.alarmUpdated.contains(newAlarm)) {
                    icalEditViewModel.alarmUpdated.add(newAlarm)
                    addAlarmView(newAlarm)
                }
            }

            datePicker.addOnPositiveButtonClickListener {
                val selectedUtcDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC"))
                zonedTimestamp = ZonedDateTime.of(selectedUtcDateTime.year, selectedUtcDateTime.monthValue, selectedUtcDateTime.dayOfMonth, 0, 0, 0, 0, ZoneId.systemDefault())
                timePicker.show(parentFragmentManager, tag)
            }
            datePicker.show(parentFragmentManager, tag)
        }

        binding.editFragmentIcalEditAlarm.editAlarmsButtonAddRelative.setOnClickListener {

            val alarm = Alarm.createDisplayAlarm()

            val number = try {
                Integer.parseInt(binding.editFragmentIcalEditAlarm.editAlarmsNumber.text.toString()).toLong()
            } catch (e: NumberFormatException) {
                Log.w("AlarmDuration", "Trigger Duration of Alarm could not be parsed as Integer \n$e")
                return@setOnClickListener
            }

            val dur = when (binding.editFragmentIcalEditAlarm.editAlarmsMinutesHoursDaysSpinner.selectedItem) {
                getString(R.string.alarms_minutes) -> Duration.of(number, ChronoUnit.MINUTES)
                getString(R.string.alarms_hours) -> Duration.of(number, ChronoUnit.HOURS)
                getString(R.string.alarms_days) -> Duration.of(number, ChronoUnit.DAYS)
                else -> return@setOnClickListener
            }

            when(binding.editFragmentIcalEditAlarm.editAlarmsBeforeAfterStartDueSpinner.selectedItem) {
                getString(R.string.alarms_before_start) -> {
                    alarm.triggerRelativeDuration = dur.negated().toString()
                    alarm.triggerRelativeTo = AlarmRelativeTo.START.name
                }
                getString(R.string.alarms_after_start) -> {
                    alarm.triggerRelativeDuration = dur.toString()
                    alarm.triggerRelativeTo = AlarmRelativeTo.START.name
                }
                getString(R.string.alarms_before_due) -> {
                    alarm.triggerRelativeDuration = dur.negated().toString()
                    alarm.triggerRelativeTo = AlarmRelativeTo.END.name
                }
                getString(R.string.alarms_after_due) -> {
                    alarm.triggerRelativeDuration = dur.toString()
                    alarm.triggerRelativeTo = AlarmRelativeTo.END.name
                }
            }
            if(!icalEditViewModel.alarmUpdated.contains(alarm)) {
                icalEditViewModel.alarmUpdated.add(alarm)
                addAlarmView(alarm)
            }
        }

        binding.editFragmentIcalEditAlarm.editAlarmsButtonOnstart.setOnClickListener {
            val alarm = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.START)
            if(!icalEditViewModel.alarmUpdated.contains(alarm)) {
                icalEditViewModel.alarmUpdated.add(alarm)
                addAlarmView(alarm)
            }
        }

        binding.editFragmentIcalEditAlarm.editAlarmsButtonOndue.setOnClickListener {
            val alarm = Alarm.createDisplayAlarm(Duration.ZERO, AlarmRelativeTo.END)
            if(!icalEditViewModel.alarmUpdated.contains(alarm)) {
                icalEditViewModel.alarmUpdated.add(alarm)
                addAlarmView(alarm)
            }
        }

        // notify the user if a duration was detected (currently not supported)
        if(icalEditViewModel.iCalEntity.property.duration?.isNotEmpty() == true) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.edit_fragment_recur_unsupported_duration_dialog_title))
                .setMessage(getString(R.string.edit_fragment_recur_unsupported_duration_dialog_message))
                .setPositiveButton(R.string.ok) { _, _ ->  }
                .show()
        }


        val weekdays = getLocalizedWeekdays()
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.text = weekdays[0]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.text = weekdays[1]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.text = weekdays[2]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.text = weekdays[3]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.text = weekdays[4]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.text = weekdays[5]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.text = weekdays[6]
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.setOnCheckedChangeListener { _, _ -> updateRRule() }
        binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.setOnCheckedChangeListener { _, _ -> updateRRule() }

        binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.wrapSelectorWheel = false
        binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.minValue = 1
        binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.maxValue = 31
        binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.setOnValueChangedListener { _, _, _ -> updateRRule() }

        binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.wrapSelectorWheel = false
        binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.minValue = 1
        binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.maxValue = ICalObject.DEFAULT_MAX_RECUR_INSTANCES
        binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.setOnValueChangedListener { _, _, _ -> updateRRule() }

        binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.wrapSelectorWheel = false
        binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.minValue = 1
        binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.maxValue = 31
        binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.setOnValueChangedListener { _, _, _ -> updateRRule() }

        binding.editFragmentIcalEditRecur.editRecurDaysMonthsSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when(position) {
                        0 -> icalEditViewModel.recurrenceMode.value = RECURRENCE_MODE_DAY
                        1 -> icalEditViewModel.recurrenceMode.value = RECURRENCE_MODE_WEEK
                        2 -> icalEditViewModel.recurrenceMode.value = RECURRENCE_MODE_MONTH
                        3 -> icalEditViewModel.recurrenceMode.value = RECURRENCE_MODE_YEAR
                    }
                    icalEditViewModel.updateVisibility()
                    updateRRule()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}    // nothing to do
            }
        binding.editFragmentIcalEditRecur.editRecurDaysMonthsSpinner.setSelection(
            when(icalEditViewModel.recurrenceMode.value) {
                RECURRENCE_MODE_DAY -> 0
                RECURRENCE_MODE_WEEK -> 1
                RECURRENCE_MODE_MONTH -> 2
                RECURRENCE_MODE_YEAR -> 3
                else -> 0
            })

        rruleUntil = icalEditViewModel.iCalEntity.property.rrule?.let { Recur(it).until?.time } ?: icalEditViewModel.iCalEntity.property.dtstart ?: System.currentTimeMillis()
        binding.editFragmentIcalEditRecur.editRecurEndsOnDateText.text = convertLongToFullDateTimeString(rruleUntil, icalEditViewModel.iCalEntity.property.dtstartTimezone)

        binding.editFragmentIcalEditRecur.editRecurEndSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    icalEditViewModel.recurrenceEnd.value = when(position) {
                        0 -> RECURRENCE_END_AFTER
                        1 -> RECURRENCE_END_ON
                        2 -> RECURRENCE_END_NEVER
                        else -> RECURRENCE_END_AFTER
                    }
                    icalEditViewModel.updateVisibility()
                    updateRRule()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}    // nothing to do
            }
        binding.editFragmentIcalEditRecur.editRecurEndSpinner.setSelection(
            when(icalEditViewModel.recurrenceEnd.value) {
                RECURRENCE_END_AFTER -> 0
                RECURRENCE_END_ON -> 1
                RECURRENCE_END_NEVER -> 2
                else -> 0
            })


        //pre-set rules if rrule is present
        if(icalEditViewModel.iCalEntity.property.rrule!= null) {

            try {

                val recur = Recur(icalEditViewModel.iCalEntity.property.rrule)

                if(icalEditViewModel.recurrenceMode.value == RECURRENCE_MODE_UNSUPPORTED)
                    throw Exception("Unsupported recurrence mode detected")

                if(recur.experimentalValues.isNotEmpty() || recur.hourList.isNotEmpty() || recur.minuteList.isNotEmpty() || recur.monthList.isNotEmpty() || recur.secondList.isNotEmpty() || recur.setPosList.isNotEmpty() || recur.skip != null || recur.weekNoList.isNotEmpty() || recur.weekStartDay != null || recur.yearDayList.isNotEmpty())
                    throw Exception("Unsupported values detected")

                binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.value =
                    icalEditViewModel.iCalEntity.property.retrieveCount()

                binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.value =
                    if(recur.interval <1) 1 else recur.interval

                //pre-check the weekday-chips according to the rrule
                if (icalEditViewModel.recurrenceMode.value == RECURRENCE_MODE_WEEK) {
                    if(recur.dayList.size < 1)
                        throw Exception("Recurrence mode Weekly but no weekdays were set")
                    recur.dayList.forEach {
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.MO) || (!isLocalizedWeekstartMonday() && it == WeekDay.SU))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.TU) || (!isLocalizedWeekstartMonday() && it == WeekDay.MO))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.WE) || (!isLocalizedWeekstartMonday() && it == WeekDay.TU))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.TH) || (!isLocalizedWeekstartMonday() && it == WeekDay.WE))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.FR) || (!isLocalizedWeekstartMonday() && it == WeekDay.TH))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.SA) || (!isLocalizedWeekstartMonday() && it == WeekDay.FR))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isChecked = true
                        if ((isLocalizedWeekstartMonday() && it == WeekDay.SU) || (!isLocalizedWeekstartMonday() && it == WeekDay.SA))
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isChecked = true
                    }
                }

                //pre-select the day of the month according to the rrule
                if (icalEditViewModel.recurrenceMode.value == RECURRENCE_MODE_MONTH) {
                    if(recur.monthDayList.size != 1)
                        throw Exception("Recurrence mode Monthly but no day or multiple days were set")
                    val selectedMonth = Recur(icalEditViewModel.iCalEntity.property.rrule).monthDayList[0]
                    binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.value = selectedMonth
                }
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

        binding.editFragmentTabGeneral.editColorItem.setOnClickListener {

            val colorPickerBinding = FragmentIcalEditColorpickerDialogBinding.inflate(inflater)
            icalEditViewModel.iCalObjectUpdated.value?.color?.let{ colorPickerBinding.colorPicker.color = it }
            colorPickerBinding.colorPicker.showOldCenterColor = false

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.color)
                .setView(colorPickerBinding.root)
                .setIcon(R.drawable.ic_color)
                .setPositiveButton(R.string.ok)  { _, _ ->
                    icalEditViewModel.iCalObjectUpdated.value?.color = colorPickerBinding.colorPicker.color
                    icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
                }
                .setNeutralButton(R.string.cancel)  { _, _ -> return@setNeutralButton  /* nothing to do */  }
                .setNegativeButton(R.string.reset) { _, _ ->
                    icalEditViewModel.iCalObjectUpdated.value?.color = null
                    icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
                }
                .show()
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

        icalEditViewModel.deleteClicked.observe(viewLifecycleOwner) {
            if (it == true) {

                if (icalEditViewModel.iCalObjectUpdated.value?.id == 0L)
                    showDiscardMessage()
                else
                    showDeleteMessage()
            }
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

            // Set the default value of the Classification Chip
            binding.editFragmentTabGeneral.editClassificationChip.text =
                Classification.getStringResource(requireContext(), it.classification)
                    ?: it.classification       // if unsupported just show whatever is there

            updateRRule()
            icalEditViewModel.updateVisibility()

            // update color for item if possible
            ICalObject.applyColorOrHide(binding.editFragmentTabGeneral.editColorbarItem, it.color)

            // update quick alarm options depending if dates are set
            if(it.dtstart == null)
                binding.editFragmentIcalEditAlarm.editAlarmsButtonOnstart.visibility = View.GONE
            else
                binding.editFragmentIcalEditAlarm.editAlarmsButtonOnstart.visibility = View.VISIBLE
            if(it.due == null)
                binding.editFragmentIcalEditAlarm.editAlarmsButtonOndue.visibility = View.GONE
            else
                binding.editFragmentIcalEditAlarm.editAlarmsButtonOndue.visibility = View.VISIBLE

            if(it.dtstart == null && it.due == null)
                binding.editFragmentIcalEditAlarm.editAlarmsRelativeAlarmBlock.visibility = View.GONE
            else
                binding.editFragmentIcalEditAlarm.editAlarmsRelativeAlarmBlock.visibility = View.VISIBLE

            val adapterMinutesHoursDays = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                arrayOf(getString(R.string.alarms_minutes), getString(R.string.alarms_hours), getString(R.string.alarms_days))
            )
            adapterMinutesHoursDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.editFragmentIcalEditAlarm.editAlarmsMinutesHoursDaysSpinner.adapter = adapterMinutesHoursDays

            val adapterBeforeAfterOptions = arrayListOf<String>()
            if(it.dtstart != null)
                adapterBeforeAfterOptions.addAll(arrayListOf(getString(R.string.alarms_before_start), getString(R.string.alarms_after_start)))
            if(it.due != null)
                adapterBeforeAfterOptions.addAll(arrayListOf(getString(R.string.alarms_before_due), getString(R.string.alarms_after_due)))
            val adapterBeforeAfter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                adapterBeforeAfterOptions
            )
            adapterBeforeAfter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.editFragmentIcalEditAlarm.editAlarmsBeforeAfterStartDueSpinner.adapter = adapterBeforeAfter

            // recreate alarm views (as start or due date might have been updated)
            binding.editFragmentIcalEditAlarm.editAlarmsLinearlayout.removeAllViews()
            icalEditViewModel.alarmUpdated.forEach { singleAlarm ->
                addAlarmView(singleAlarm)
            }

            menu?.findItem(R.id.menu_edit_clear_dates)?.isVisible =
                (it.component == Component.VTODO.name && (it.dtstart != null || it.due != null || it.completed != null))   // don't show clear dates if no dates are set anyway
        }


        icalEditViewModel.addTimeChecked.observe(viewLifecycleOwner) { addTime ->

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            val oldDtstart = icalEditViewModel.iCalObjectUpdated.value?.dtstart?.let{ ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone)) }
            val oldDue = icalEditViewModel.iCalObjectUpdated.value?.due?.let{ ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dueTimezone)) }
            val oldCompleted = icalEditViewModel.iCalObjectUpdated.value?.completed?.let{ ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.completedTimezone)) }

            if (addTime) {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = null
            } else {
                icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone = ICalObject.TZ_ALLDAY
                icalEditViewModel.iCalObjectUpdated.value?.dueTimezone = ICalObject.TZ_ALLDAY
                icalEditViewModel.iCalObjectUpdated.value?.completedTimezone = ICalObject.TZ_ALLDAY
            }

            // make sure that the day stays the same when we add time
            icalEditViewModel.iCalObjectUpdated.value?.dtstart = oldDtstart?.let {
                ZonedDateTime.of(it.year, it.monthValue, it.dayOfMonth, 0, 0, 0, 0, requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone)).toInstant().toEpochMilli()
            }
            icalEditViewModel.iCalObjectUpdated.value?.due = oldDue?.let {
                ZonedDateTime.of(it.year, it.monthValue, it.dayOfMonth, 0, 0, 0, 0, requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dueTimezone)).toInstant().toEpochMilli()
            }
            icalEditViewModel.iCalObjectUpdated.value?.completed = oldCompleted?.let {
                ZonedDateTime.of(it.year, it.monthValue, it.dayOfMonth, 0, 0, 0, 0, requireTzId(icalEditViewModel.iCalObjectUpdated.value?.completedTimezone)).toInstant().toEpochMilli()
            }

            // post itself to update UI
            icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)

            // is this even necessary when the iCalObjectUpadted is posted anyway?
            icalEditViewModel.updateVisibility()                 // Update visibility of Elements on Change of showAll
        }

        icalEditViewModel.addTimezoneJournalChecked.observe(viewLifecycleOwner) { addJournalTimezone ->

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if(addJournalTimezone) {
                if(icalEditViewModel.iCalObjectUpdated.value?.dtstart != null)
                    showTimezonePicker(TAG_PICKER_DTSTART)
            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = null
                icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
            }
        }


        icalEditViewModel.addTimezoneTodoChecked.observe(viewLifecycleOwner) { addTodoTimezone ->

            if (icalEditViewModel.iCalObjectUpdated.value == null)     // don't do anything if the object was not initialized yet
                return@observe

            if(addTodoTimezone) {
                when {
                    icalEditViewModel.iCalObjectUpdated.value?.dtstart != null -> showTimezonePicker(TAG_PICKER_DTSTART)
                    icalEditViewModel.iCalObjectUpdated.value?.due != null -> showTimezonePicker(TAG_PICKER_DUE)
                    icalEditViewModel.iCalObjectUpdated.value?.completed != null -> showTimezonePicker(TAG_PICKER_COMPLETED)
                }
            } else {
                icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.dueTimezone = null
                icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = null
                icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
            }
        }

        icalEditViewModel.relatedSubtasks.observe(viewLifecycleOwner) {

            if (icalEditViewModel.savingClicked.value == true)    // don't do anything if saving was clicked, saving could interfere here!
                return@observe

            if(it.isNullOrEmpty())
                return@observe

            it.sortedBy { item -> item.sortIndex }.forEach { singleSubtask ->
                addSubtasksView(singleSubtask)
            }
            icalEditViewModel.relatedSubtasks.removeObservers(viewLifecycleOwner)
        }

        icalEditViewModel.recurrenceChecked.observe(viewLifecycleOwner) {
            updateRRule()
            icalEditViewModel.updateVisibility()
            binding.editAppbarlayoutTabs.setExpanded(true)
        }


        //TODO: Check if the Sequence was updated in the meantime and notify user!


        icalEditViewModel.iCalEntity.comments?.forEach { singleComment ->
            if(!icalEditViewModel.commentUpdated.contains(singleComment)) {
                icalEditViewModel.commentUpdated.add(singleComment)
                addCommentView(singleComment)
            }
        }


        icalEditViewModel.iCalEntity.alarms?.forEach { singleAlarm ->
            if(!icalEditViewModel.alarmUpdated.contains(singleAlarm)) {
                icalEditViewModel.alarmUpdated.add(singleAlarm)
                addAlarmView(singleAlarm)
            }
        }


        // Set up items to suggest for categories
        icalEditViewModel.allCategories.observe(viewLifecycleOwner) {
            // Create the adapter and set it to the AutoCompleteTextView
            if (icalEditViewModel.allCategories.value != null) {
                val arrayAdapter = ArrayAdapter(
                    application.applicationContext,
                    android.R.layout.simple_list_item_1,
                    icalEditViewModel.allCategories.value!!
                )
                binding.editFragmentTabGeneral.editCategoriesAddAutocomplete.setAdapter(arrayAdapter)
            }
        }

        // Set up items to suggest for resources
        icalEditViewModel.allResources.observe(viewLifecycleOwner) {
            // Create the adapter and set it to the AutoCompleteTextView
            if (icalEditViewModel.allResources.value != null) {
                val arrayAdapter = ArrayAdapter(
                    application.applicationContext,
                    android.R.layout.simple_list_item_1,
                    icalEditViewModel.allResources.value!!
                )
                binding.editFragmentTabCar.editResourcesAddAutocomplete.setAdapter(arrayAdapter)
            }
        }

        // initialize allRelatedto
        icalEditViewModel.isChild.observe(viewLifecycleOwner) {

            // if the current item is a child, changing the collection is not allowed; also making it recurring is not allowed
            if (it) {
                binding.editFragmentTabGeneral.editCollectionSpinner.isEnabled = false
                binding.editFragmentIcalEditRecur.editRecurSwitch.isEnabled = false
            }
        }

        binding.editFragmentTabGeneral.editDtstartCard.setOnClickListener {
            showDatePicker(
                icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: DateTimeUtils.getTodayAsLong(),
                icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone,
                TAG_PICKER_DTSTART
            )
        }

        binding.editFragmentTabGeneral.editTaskDatesFragment.editTaskDueCard.setOnClickListener {
            showDatePicker(
                icalEditViewModel.iCalObjectUpdated.value?.due ?: DateTimeUtils.getTodayAsLong(),
                icalEditViewModel.iCalObjectUpdated.value?.dueTimezone,
                TAG_PICKER_DUE
            )
        }

        binding.editFragmentTabGeneral.editTaskDatesFragment.editTaskCompletedCard.setOnClickListener {
            showDatePicker(
                icalEditViewModel.iCalObjectUpdated.value?.completed ?: DateTimeUtils.getTodayAsLong(),
                icalEditViewModel.iCalObjectUpdated.value?.completedTimezone,
                TAG_PICKER_COMPLETED
            )
        }

        binding.editFragmentTabGeneral.editTaskDatesFragment.editTaskStartedCard.setOnClickListener {
            showDatePicker(
                icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: DateTimeUtils.getTodayAsLong(),
                icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone,
                TAG_PICKER_DTSTART
            )
        }

        binding.editFragmentIcalEditRecur.editRecurEndsOnDateCard.setOnClickListener {
            if(icalEditViewModel.iCalObjectUpdated.value?.dtstart != null)
                showDatePicker(
                    Recur(icalEditViewModel.iCalObjectUpdated.value?.rrule).until.time,
                    icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone,
                    TAG_PICKER_RECUR_UNTIL
                )
            else
                toastNoDtstart?.show()
        }

        var restoreProgress = icalEditViewModel.iCalObjectUpdated.value?.percent ?: 0

        binding.editFragmentTabGeneral.editProgressSlider.addOnChangeListener { _, value, _ ->
            icalEditViewModel.iCalObjectUpdated.value?.percent = value.toInt()
            binding.editFragmentTabGeneral.editProgressCheckbox.isChecked = value == 100F
            binding.editFragmentTabGeneral.editProgressPercent.text = String.format("%.0f%%", value)   // takes care of localized representation of percentages (with 0 positions after the comma)
            if (value != 100F)
                restoreProgress = value.toInt()

            val statusBefore = icalEditViewModel.iCalObjectUpdated.value?.status

            // if the status was not set initially (=null), then we don't update it
            if(icalEditViewModel.iCalObjectUpdated.value?.status.isNullOrEmpty())
                return@addOnChangeListener

            when (value.toInt()) {
                100 -> icalEditViewModel.iCalObjectUpdated.value!!.status =
                    StatusTodo.COMPLETED.name
                in 1..99 -> icalEditViewModel.iCalObjectUpdated.value!!.status =
                    StatusTodo.`IN-PROCESS`.name
                0 -> icalEditViewModel.iCalObjectUpdated.value!!.status =
                    StatusTodo.`NEEDS-ACTION`.name
            }

            // update the status only if it was actually changed, otherwise the performance sucks
            if (icalEditViewModel.iCalObjectUpdated.value!!.status != statusBefore) {
                when (icalEditViewModel.iCalObjectUpdated.value!!.component) {
                    Component.VTODO.name -> binding.editFragmentTabGeneral.editStatusChip.text =
                        StatusTodo.getStringResource(
                            requireContext(),
                            icalEditViewModel.iCalObjectUpdated.value!!.status
                        ) ?: icalEditViewModel.iCalObjectUpdated.value!!.status
                    Component.VJOURNAL.name -> binding.editFragmentTabGeneral.editStatusChip.text =
                        StatusJournal.getStringResource(
                            requireContext(),
                            icalEditViewModel.iCalObjectUpdated.value!!.status
                        ) ?: icalEditViewModel.iCalObjectUpdated.value!!.status
                    else -> binding.editFragmentTabGeneral.editStatusChip.text =
                        icalEditViewModel.iCalObjectUpdated.value!!.status
                }       // if unsupported just show whatever is there
            }
        }

        binding.editFragmentTabGeneral.editProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            val newProgress: Int = if (checked) 100
            else restoreProgress

            binding.editFragmentTabGeneral.editProgressSlider.value =
                newProgress.toFloat()    // This will also trigger saving through the listener!
        }


        binding.editFragmentTabSubtasks.editSubtasksAdd.setEndIconOnClickListener {
            // Respond to end icon presses
            if(binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text.toString().isNotBlank()) {
                val newSubtask =
                    ICalObject.createTask(summary = binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text.toString())
                icalEditViewModel.subtaskUpdated.add(newSubtask)
                addSubtasksView(newSubtask)
                binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text?.clear()  // clear the field
            }
        }

        // Transform the comment input into a view when the Done button in the keyboard is clicked
        binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if(binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text.toString().isNotBlank()) {
                        val newSubtask =
                            ICalObject.createTask(summary = binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text.toString())
                        icalEditViewModel.subtaskUpdated.add(newSubtask)    // store the comment for saving
                        addSubtasksView(newSubtask)      // add the new comment
                        binding.editFragmentTabSubtasks.editSubtasksAdd.editText?.text?.clear()  // clear the field
                    }
                    true
                }
                else -> false
            }
        }


        binding.editBottomBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.menu_edit_bottom_delete -> icalEditViewModel.deleteClicked()
            }
            true
        }


        return binding.root
    }

    override fun onResume() {
        icalEditViewModel.updateVisibility()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun showDatePicker(presetTime: Long, timezone: String?, tag: String) {

        val tzId = requireTzId(timezone)
        val presetValueUTC = ZonedDateTime.ofInstant(Instant.ofEpochMilli(presetTime), tzId)

        // make sure preset is according to constraints (due is after start)
        val preset = when {
            tag == TAG_PICKER_DUE && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && presetValueUTC.toInstant()
                .toEpochMilli() < (icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: 0L) -> icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: icalEditViewModel.iCalObjectUpdated.value?.due?: System.currentTimeMillis()
            tag == TAG_PICKER_DTSTART && icalEditViewModel.iCalObjectUpdated.value?.due != null && presetValueUTC.toInstant()
                .toEpochMilli() > (icalEditViewModel.iCalObjectUpdated.value?.due ?: 0L) -> icalEditViewModel.iCalObjectUpdated.value?.due ?: icalEditViewModel.iCalObjectUpdated.value?.dtstart?: System.currentTimeMillis()
            else -> presetValueUTC.toInstant().toEpochMilli()
        }

        // Build constraints.
        val constraints =
            CalendarConstraints.Builder().apply {
                if(tag == TAG_PICKER_DUE && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null )
                    setStart(icalEditViewModel.iCalObjectUpdated.value!!.dtstart!!)
                if(tag == TAG_PICKER_DTSTART && icalEditViewModel.iCalObjectUpdated.value?.due != null )
                    setStart(icalEditViewModel.iCalObjectUpdated.value!!.due!!)
                if(tag == TAG_PICKER_RECUR_UNTIL && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null )
                    setStart(icalEditViewModel.iCalObjectUpdated.value!!.dtstart!!)

                // Create a custom date validator to only enable dates that are in the list
                val customDateValidator = object : CalendarConstraints.DateValidator {
                    override fun describeContents(): Int { return 0 }
                    override fun writeToParcel(p0: Parcel, p1: Int) {   }
                    override fun isValid(date: Long): Boolean {
                        if(tag == TAG_PICKER_DUE && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && date < getDateWithoutTime(icalEditViewModel.iCalObjectUpdated.value!!.dtstart!!, icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone))
                            return false
                        if(tag == TAG_PICKER_DTSTART && icalEditViewModel.iCalObjectUpdated.value?.due != null && date > getDateWithoutTime(icalEditViewModel.iCalObjectUpdated.value!!.due!!, icalEditViewModel.iCalObjectUpdated.value?.dueTimezone))
                            return false
                        if(tag == TAG_PICKER_RECUR_UNTIL && icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && date < getDateWithoutTime(icalEditViewModel.iCalObjectUpdated.value!!.dtstart!!, icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone))
                            return false
                        return true
                    }
                }
                setValidator(customDateValidator)
            }.build()

        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.edit_datepicker_dialog_select_date)
                .setSelection(preset)
                .setCalendarConstraints(constraints)
                .build()

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            val selectedUtcDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC"))
            val zonedTimestamp = presetValueUTC.withYear(selectedUtcDateTime.year).withMonth(selectedUtcDateTime.monthValue).withDayOfMonth(selectedUtcDateTime.dayOfMonth).toInstant().toEpochMilli()

            when (tag) {
                TAG_PICKER_DTSTART -> icalEditViewModel.iCalObjectUpdated.value!!.dtstart = zonedTimestamp
                TAG_PICKER_DUE -> icalEditViewModel.iCalObjectUpdated.value!!.due = zonedTimestamp
                TAG_PICKER_COMPLETED -> icalEditViewModel.iCalObjectUpdated.value!!.completed = zonedTimestamp
                TAG_PICKER_RECUR_UNTIL -> {
                    rruleUntil = zonedTimestamp
                    binding.editFragmentIcalEditRecur.editRecurEndsOnDateText.text = convertLongToFullDateString(rruleUntil, icalEditViewModel.iCalEntity.property.dtstartTimezone)
                }
            }

            if(tag == TAG_PICKER_DTSTART && rruleUntil < (icalEditViewModel.iCalObjectUpdated.value!!.dtstart ?: System.currentTimeMillis())) {
                rruleUntil = icalEditViewModel.iCalObjectUpdated.value!!.dtstart ?: System.currentTimeMillis()
                binding.editFragmentIcalEditRecur.editRecurEndsOnDateText.text = convertLongToFullDateTimeString(rruleUntil, icalEditViewModel.iCalEntity.property.dtstartTimezone)
            }

            // if DTSTART was changed, we additionally update the RRULE
            if(tag == TAG_PICKER_DTSTART || tag == TAG_PICKER_RECUR_UNTIL) {
                updateRRule()
            }

            if (tag != TAG_PICKER_RECUR_UNTIL && icalEditViewModel.addTimeChecked.value == true)    // let the user set the time only if the time is desired!
                showTimePicker(zonedTimestamp, tzId.id, tag)

            // post itself to update the UI
            icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
        }

        datePicker.show(parentFragmentManager, tag)
    }


    private fun showTimePicker(presetValueUTC: Long, timezone: String?, tag: String) {

        val tzId = requireTzId(timezone)
        val presetUtcDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(presetValueUTC), tzId)
        val clockFormat = if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timePicker =
            MaterialTimePicker.Builder()
                .setHour(presetUtcDateTime.hour)
                .setMinute(presetUtcDateTime.minute)
                .setTitleText(R.string.edit_datepicker_dialog_select_time)
                .setTimeFormat(clockFormat)
                .build()

        timePicker.addOnPositiveButtonClickListener {

            val zonedTimestamp = presetUtcDateTime.withHour(timePicker.hour).withMinute(timePicker.minute).withSecond(0).withNano(0).toInstant().toEpochMilli()

            when (tag) {
                TAG_PICKER_DTSTART -> icalEditViewModel.iCalObjectUpdated.value!!.dtstart = zonedTimestamp
                TAG_PICKER_DUE -> icalEditViewModel.iCalObjectUpdated.value!!.due = zonedTimestamp
                TAG_PICKER_COMPLETED -> icalEditViewModel.iCalObjectUpdated.value!!.completed = zonedTimestamp
            }

            // if DTSTART was changed, we additionally update the RRULE
            if(tag == TAG_PICKER_DTSTART)
                updateRRule()

            // post itself to update the UI
            icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)

            if (icalEditViewModel.addTimezoneJournalChecked.value == true || icalEditViewModel.addTimezoneTodoChecked.value == true)
                showTimezonePicker(tag)
        }

        timePicker.show(parentFragmentManager, tag)
    }


    private fun showTimezonePicker(tag: String) {

        val spinner = Spinner(requireContext())

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            icalEditViewModel.possibleTimezones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        when (tag) {
            TAG_PICKER_DUE -> spinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(icalEditViewModel.iCalObjectUpdated.value?.dueTimezone))
            TAG_PICKER_COMPLETED -> spinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(icalEditViewModel.iCalObjectUpdated.value?.completedTimezone))
            TAG_PICKER_DTSTART -> spinner.setSelection(icalEditViewModel.possibleTimezones.indexOf(icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone))
        }


        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set timezone")
        builder.setIcon(R.drawable.ic_timezone)
        builder.setView(spinner)
        builder.setPositiveButton(R.string.save) { _, _ ->

            val selectedTimezone = icalEditViewModel.possibleTimezones[spinner.selectedItemPosition]

            when (tag) {
                TAG_PICKER_DUE -> {
                    val oldUtcDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(icalEditViewModel.iCalObjectUpdated.value?.due ?: System.currentTimeMillis()), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dueTimezone))
                    icalEditViewModel.iCalObjectUpdated.value?.dueTimezone = selectedTimezone
                    icalEditViewModel.iCalObjectUpdated.value?.due = oldUtcDateTime.withZoneSameLocal(requireTzId(selectedTimezone)).toInstant().toEpochMilli()
                }
                TAG_PICKER_COMPLETED -> {
                    val oldUtcDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(icalEditViewModel.iCalObjectUpdated.value?.completed ?: System.currentTimeMillis()), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.completedTimezone))
                    icalEditViewModel.iCalObjectUpdated.value!!.completedTimezone = selectedTimezone
                    icalEditViewModel.iCalObjectUpdated.value?.completed = oldUtcDateTime.withZoneSameLocal(requireTzId(selectedTimezone)).toInstant().toEpochMilli()
                }
                TAG_PICKER_DTSTART -> {
                    val oldUtcDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: System.currentTimeMillis()), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone))
                    icalEditViewModel.iCalObjectUpdated.value!!.dtstartTimezone = selectedTimezone
                    icalEditViewModel.iCalObjectUpdated.value?.dtstart = oldUtcDateTime.withZoneSameLocal(requireTzId(selectedTimezone)).toInstant().toEpochMilli()
                }
            }

            // if times are set but no timezone is set, we set the timezone also for the other date-times
            /*
            if(icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone == null)
                icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone = selectedTimezone
            if(icalEditViewModel.iCalObjectUpdated.value?.due != null && icalEditViewModel.iCalObjectUpdated.value?.dueTimezone == null)
                icalEditViewModel.iCalObjectUpdated.value?.dueTimezone = selectedTimezone
            if(icalEditViewModel.iCalObjectUpdated.value?.completed != null && icalEditViewModel.iCalObjectUpdated.value?.completedTimezone == null)
                icalEditViewModel.iCalObjectUpdated.value?.completedTimezone = selectedTimezone

             */



            // post itself to update the UI
            icalEditViewModel.iCalObjectUpdated.postValue(icalEditViewModel.iCalObjectUpdated.value)
        }

        builder.setNegativeButton(R.string.cancel) { _, _ ->
            // Do nothing, just close the message
        }

        builder.show()
    }

    private fun showDiscardMessage() {

        // show Alert Dialog before the item gets really deleted
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.edit_dialog_sure_to_discard_title))
        builder.setMessage(getString(R.string.edit_dialog_sure_to_discard_message))
        builder.setPositiveButton(R.string.discard) { _, _ ->

            //hideKeyboard()
            context?.let { context -> Attachment.scheduleCleanupJob(context) }

            val direction = IcalEditFragmentDirections.actionIcalEditFragmentToIcalListFragment()
            direction.module2show = icalEditViewModel.iCalObjectUpdated.value!!.module
            this.findNavController().navigate(direction)
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->  }   // Do nothing, just close the message
        builder.show()

    }

    private fun showDeleteMessage() {

        // show Alert Dialog before the item gets really deleted
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.edit_dialog_sure_to_delete_title, icalEditViewModel.iCalObjectUpdated.value?.summary))
        builder.setMessage(getString(R.string.edit_dialog_sure_to_delete_message, icalEditViewModel.iCalObjectUpdated.value?.summary))
        builder.setPositiveButton(R.string.delete) { _, _ ->
            //hideKeyboard()
            icalEditViewModel.delete()
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->  }   // Do nothing, just close the message
        builder.show()
    }



    private fun addCommentView(comment: Comment) {

        val bindingComment = FragmentIcalEditCommentBinding.inflate(inflater, container, false)
        bindingComment.editCommentTextview.text = comment.text
        //commentView.edit_comment_textview.text = comment.text
        binding.editFragmentTabUlc.editCommentsLinearlayout.addView(bindingComment.root)

        // set on Click Listener to open a dialog to update the comment
        bindingComment.root.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedText = TextInputEditText(requireContext())
            updatedText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            updatedText.setText(comment.text)
            updatedText.isSingleLine = false
            updatedText.maxLines = 8
            updatedText.contentDescription = getString(R.string.edit_comment_add_dialog_hint)

            // set up the builder for the AlertDialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.edit_comment_add_dialog_hint)
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(updatedText)
            builder.setPositiveButton(R.string.save) { _, _ ->
                // update the comment
                val updatedComment = comment.copy()
                updatedComment.text = updatedText.text.toString()
                icalEditViewModel.commentUpdated.add(updatedComment)
                bindingComment.editCommentTextview.text = updatedComment.text
            }
            builder.setNegativeButton(R.string.cancel) { _, _ -> /* Do nothing, just close the message */ }
            builder.setNeutralButton(R.string.delete) { _, _ ->
                icalEditViewModel.commentUpdated.remove(comment)
                bindingComment.root.visibility = View.GONE
            }
            builder.show()
        }
    }

    private fun addAlarmView(alarm: Alarm) {

        // we don't add alarm of which the DateTime is not set or cannot be determined
        if(alarm.triggerTime == null && alarm.triggerRelativeDuration == null)
            return

        val bindingAlarm = when {
            alarm.triggerTime != null ->
                alarm.getAlarmCardBinding(inflater, binding.editFragmentIcalEditAlarm.editAlarmsLinearlayout, null, null )
            alarm.triggerRelativeDuration?.isNotEmpty() == true -> {

                val referenceDate = if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name)
                    icalEditViewModel.iCalObjectUpdated.value?.due ?: icalEditViewModel.iCalEntity.property.due ?: return
                else
                    icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: icalEditViewModel.iCalEntity.property.dtstart ?: return

                val referenceTZ = if(alarm.triggerRelativeTo == AlarmRelativeTo.END.name)
                    icalEditViewModel.iCalObjectUpdated.value?.dueTimezone ?: icalEditViewModel.iCalEntity.property.dueTimezone
                else
                    icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone ?: icalEditViewModel.iCalEntity.property.dtstartTimezone

                alarm.getAlarmCardBinding(inflater, binding.editFragmentIcalEditAlarm.editAlarmsLinearlayout, referenceDate, referenceTZ )
            }
            else -> return
        }

        bindingAlarm?.cardAlarmDelete?.setOnClickListener {
            icalEditViewModel.alarmUpdated.remove(alarm)
            binding.editFragmentIcalEditAlarm.editAlarmsLinearlayout.removeView(bindingAlarm.root)
        }
        binding.editFragmentIcalEditAlarm.editAlarmsLinearlayout.addView(bindingAlarm?.root)
    }


    private fun addSubtasksView(subtask: ICalObject) {

        val bindingSubtask = FragmentIcalEditSubtaskBinding.inflate(inflater, container, false)
        bindingSubtask.editSubtaskTextview.text = subtask.summary
        bindingSubtask.editSubtaskProgressSlider.value = subtask.percent?.toFloat() ?: 0F
        bindingSubtask.editSubtaskProgressPercent.text = String.format("%.0f%%", subtask.percent?.toFloat() ?: 0F)   // takes care of localized representation of percentages (with 0 positions after the comma)


        bindingSubtask.editSubtaskProgressCheckbox.isChecked = subtask.percent == 100

        var restoreProgress = subtask.percent

        bindingSubtask.editSubtaskProgressSlider.addOnChangeListener { _, value, _ ->
            //Update the progress in the updated list: try to find the matching uid (the only unique element for now) and then assign the percent
            //Attention, the new subtask must have been inserted before in the list!
            if (icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid } == null) {
                val changedItem = subtask.copy()
                changedItem.percent = value.toInt()
                icalEditViewModel.subtaskUpdated.add(changedItem)
            } else {
                icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid }?.percent =
                    value.toInt()
            }

            bindingSubtask.editSubtaskProgressCheckbox.isChecked = value == 100F
            bindingSubtask.editSubtaskProgressPercent.text = String.format("%.0f%%", value)
            if (value != 100F)
                restoreProgress = value.toInt()
        }

        bindingSubtask.editSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->
            val newProgress: Int = if (checked) 100
            else restoreProgress ?: 0

            bindingSubtask.editSubtaskProgressSlider.value =
                newProgress.toFloat()    // This will also trigger saving through the listener!
        }


        binding.editFragmentTabSubtasks.editSubtasksLinearlayout.addView(bindingSubtask.root)

        // set on Click Listener to open a dialog to update the comment
        bindingSubtask.root.setOnClickListener {

            // set up the values for the TextInputEditText
            val updatedSummary = TextInputEditText(requireContext())
            updatedSummary.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            updatedSummary.setText(subtask.summary)
            updatedSummary.isSingleLine = false
            updatedSummary.maxLines = 2

            // set up the builder for the AlertDialog
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Edit subtask")
            builder.setIcon(R.drawable.ic_comment_add)
            builder.setView(updatedSummary)


            builder.setPositiveButton(R.string.save) { _, _ ->

                if (icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid } == null) {
                    val changedItem = subtask.copy()
                    changedItem.summary = updatedSummary.text.toString()
                    icalEditViewModel.subtaskUpdated.add(changedItem)
                } else {
                    icalEditViewModel.subtaskUpdated.find { it.uid == subtask.uid }?.summary =
                        updatedSummary.text.toString()
                }
                bindingSubtask.editSubtaskTextview.text = updatedSummary.text.toString()

            }
            builder.setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, just close the message
            }

            builder.setNeutralButton(R.string.delete) { _, _ ->
                icalEditViewModel.subtaskDeleted.add(subtask)
                bindingSubtask.root.visibility = View.GONE
            }
            builder.show()
        }
    }



    private fun updateRRule() {

        // the RRule might need to be deleted if the switch was deactivated
        if(icalEditViewModel.recurrenceChecked.value == null || icalEditViewModel.recurrenceChecked.value == false) {
            icalEditViewModel.iCalObjectUpdated.value?.rrule = null
            return
        }

        if(icalEditViewModel.iCalEntity.property.dtstart == null) {
            toastNoDtstart?.cancel()   // cancel if already shown and how again, otherwise the new Toast gets enqueued and will be shown too long
            toastNoDtstart = Toast.makeText(requireContext(), R.string.edit_recur_toast_requires_start_date,Toast.LENGTH_LONG)
            toastNoDtstart?.show()
            return
        }

        val recurBuilder = Recur.Builder()
        when( binding.editFragmentIcalEditRecur.editRecurDaysMonthsSpinner.selectedItemPosition) {
            RECURRENCE_MODE_DAY ->  {
                recurBuilder.frequency(Recur.Frequency.DAILY)
            }
            RECURRENCE_MODE_WEEK -> {
                recurBuilder.frequency(Recur.Frequency.WEEKLY)
                val dayList = WeekDayList()
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isChecked))
                    dayList.add(WeekDay.MO)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isChecked))
                    dayList.add(WeekDay.TU)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isChecked))
                    dayList.add(WeekDay.WE)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isChecked))
                    dayList.add(WeekDay.TH)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isChecked))
                    dayList.add(WeekDay.FR)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isChecked))
                    dayList.add(WeekDay.SA)
                if((isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isChecked) ||
                    (!isLocalizedWeekstartMonday() && binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isChecked))
                    dayList.add(WeekDay.SU)

                // the day of dtstart must be checked and should not be unchecked!
                val zonedDtstart = ZonedDateTime.ofInstant(Instant.ofEpochMilli(icalEditViewModel.iCalObjectUpdated.value?.dtstart ?: System.currentTimeMillis()), requireTzId(icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone))

                //re-enable all before disabling the current weekday again as it might have changed
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isEnabled = true
                binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isEnabled = true


                when (zonedDtstart.dayOfWeek) {
                    DayOfWeek.MONDAY -> {
                        dayList.add(WeekDay.MO)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isEnabled = false
                        }
                    }
                    DayOfWeek.TUESDAY -> {
                        dayList.add(WeekDay.TU)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip1.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isEnabled = false
                        }
                    }
                    DayOfWeek.WEDNESDAY -> {
                        dayList.add(WeekDay.WE)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip2.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isEnabled = false
                        }
                    }
                    DayOfWeek.THURSDAY -> {
                        dayList.add(WeekDay.TH)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip3.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isEnabled = false
                        }
                    }
                    DayOfWeek.FRIDAY -> {
                        dayList.add(WeekDay.FR)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip4.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isEnabled = false
                        }
                    }
                    DayOfWeek.SATURDAY -> {
                        dayList.add(WeekDay.SA)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip5.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isEnabled = false
                        }
                    }
                    DayOfWeek.SUNDAY -> {
                        dayList.add(WeekDay.SU)
                        if(isLocalizedWeekstartMonday()) {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip6.isEnabled = false
                        } else {
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isChecked = true
                            binding.editFragmentIcalEditRecur.editRecurWeekdayChip0.isEnabled = false
                        }
                    }
                    else -> { }
                }

                if(dayList.isNotEmpty())
                    recurBuilder.dayList(dayList)
            }
            RECURRENCE_MODE_MONTH -> {
                recurBuilder.frequency(Recur.Frequency.MONTHLY)
                val monthDayList = NumberList()
                monthDayList.add(binding.editFragmentIcalEditRecur.editRecurOnTheXDayOfMonthNumberPicker.value)
                recurBuilder.monthDayList(monthDayList)
            }
            RECURRENCE_MODE_YEAR -> {
                recurBuilder.frequency(Recur.Frequency.YEARLY)
            }
            else -> return
        }
        when (icalEditViewModel.recurrenceEnd.value) {
            RECURRENCE_END_AFTER -> recurBuilder.count(binding.editFragmentIcalEditRecur.editRecurUntilXOccurencesPicker.value)
            RECURRENCE_END_ON -> recurBuilder.until(Date(rruleUntil))
            // RECURRENCE_END_NEVER -> nothing to do
        }
        recurBuilder.interval(binding.editFragmentIcalEditRecur.editRecurEveryXNumberPicker.value)
        val recur = recurBuilder.build()

        Log.d("recur", recur.toString())

        //store calculated rRule
        if(icalEditViewModel.recurrenceChecked.value == true)
            icalEditViewModel.iCalObjectUpdated.value?.rrule = recur.toString()
        else
            icalEditViewModel.iCalObjectUpdated.value?.rrule = null


        // update list
        icalEditViewModel.recurrenceList.clear()

        //UpdateUI
        icalEditViewModel.recurrenceList.addAll(icalEditViewModel.iCalEntity.property.getInstancesFromRrule())

        val lastOccurrenceString = convertLongToFullDateTimeString(icalEditViewModel.recurrenceList.lastOrNull(), icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone)

        var allOccurrencesString = ""
        icalEditViewModel.recurrenceList.forEach {
            allOccurrencesString += convertLongToFullDateTimeString(it, icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone) + "\n"
        }

        var allExceptionsString = ""
        getLongListfromCSVString(icalEditViewModel.iCalObjectUpdated.value?.exdate).forEach {
            allExceptionsString += convertLongToFullDateTimeString(it, icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone) + "\n"
        }

        var allAdditionsString = ""
        getLongListfromCSVString(icalEditViewModel.iCalObjectUpdated.value?.rdate).forEach {
            allAdditionsString += convertLongToFullDateTimeString(it, icalEditViewModel.iCalObjectUpdated.value?.dtstartTimezone) + "\n"
        }

        binding.editFragmentIcalEditRecur.editRecurLastOccurenceItem.text = lastOccurrenceString
        binding.editFragmentIcalEditRecur.editRecurAllOccurencesItems.text = allOccurrencesString
        binding.editFragmentIcalEditRecur.editRecurExceptionItems.text = allExceptionsString
        binding.editFragmentIcalEditRecur.editRecurAdditionsItems.text = allAdditionsString
    }


    private fun isDataValid(): Boolean {

        var isValid = true
        var validationError = ""

        if(icalEditViewModel.iCalObjectUpdated.value?.summary.isNullOrBlank() && icalEditViewModel.iCalObjectUpdated.value?.description.isNullOrBlank())
            validationError += resources.getString(R.string.edit_validation_errors_summary_or_description_necessary) + "\n"
        if(icalEditViewModel.iCalObjectUpdated.value?.dtstart != null && icalEditViewModel.iCalObjectUpdated.value?.due != null && icalEditViewModel.iCalObjectUpdated.value?.due!! < icalEditViewModel.iCalObjectUpdated.value?.dtstart!!)
            validationError += resources.getString(R.string.edit_validation_errors_dialog_due_date_before_dtstart) + "\n"
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
