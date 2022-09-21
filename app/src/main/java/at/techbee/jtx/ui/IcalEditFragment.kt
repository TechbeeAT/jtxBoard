/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


class IcalEditFragment {

/*

    viewModelScope.launch {

        progressIndicatorVisible = Transformations.map(icalEntity) { item ->
            return@map item?.property == null     // show progress indicator as long as item.property is null
        }
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

        /*
        if(binding.editFragmentTabGeneral.editCategoriesAddAutocomplete.text.isNotEmpty())
            validationError += resources.getString(R.string.edit_validation_errors_category_not_confirmed) + "\n"
        if(binding.editFragmentTabCar.editAttendeesAddAutocomplete.text.isNotEmpty())
            validationError += resources.getString(R.string.edit_validation_errors_attendee_not_confirmed) + "\n"
        if(binding.editFragmentTabCar.editResourcesAddAutocomplete.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_resource_not_confirmed) + "\n"
        if(binding.editFragmentTabUlc.editCommentAddEdittext.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_comment_not_confirmed) + "\n"

         */
        /*
        if(binding.editFragmentTabSubtasks.editSubtasksAddEdittext.text?.isNotEmpty() == true)
            validationError += resources.getString(R.string.edit_validation_errors_subtask_not_confirmed) + "\n"

         */
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
 */

}
