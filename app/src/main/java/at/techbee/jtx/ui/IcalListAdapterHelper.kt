/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.*
import at.techbee.jtx.database.StatusTodo
import at.techbee.jtx.database.properties.Attachment
import at.techbee.jtx.database.views.ICal4List
import at.techbee.jtx.databinding.FragmentIcalListItemAttachmentBinding
import at.techbee.jtx.databinding.FragmentIcalListItemSubtaskBinding
import at.techbee.jtx.util.DateTimeUtils.getAttachmentSizeString
import com.google.android.material.slider.Slider
import java.io.FileNotFoundException

class IcalListAdapterHelper {

    companion object {

        private lateinit var settings: SharedPreferences
        private var settingShowSubtasks = true
        private var settingShowAttachments = true
        private var settingShowProgressSubtasks = true
        private var settingShowProgressMaintasks = false
        //private var iCal4List: LiveData<List<ICal4ListWithRelatedto>> = model.iCal4List
        //private var allSubtasks: LiveData<List<ICal4List?>> = model.allSubtasks


        fun addSubtasksView(model: IcalListViewModel, subtasks: List<ICal4List?>, subtasksLinearLayout: LinearLayout, context: Context, parent: ViewGroup) {

            subtasksLinearLayout.removeAllViews()

            if (subtasks.isEmpty() || !settingShowSubtasks)
                return

            setSettings(context)

            subtasks.forEach { subtask ->

                if(subtask == null)
                    return@forEach

                // if there is a search for statusTodo given, then the subtask is only taken if it is in the given status
                if(model.searchStatusTodo.isNotEmpty() && !model.searchStatusTodo.contains(StatusTodo.getFromString(subtask.status)))
                    return@forEach

                var resetProgress = subtask.percent ?: 0             // remember progress to be reset if the checkbox is unchecked

                val subtaskBinding = FragmentIcalListItemSubtaskBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )

                var subtaskSummary = subtask.summary
                //val subtaskCount = model.subtasksCountList.value?.find { subtask.id == it.icalobjectId}?.count
                /*if (subtaskCount != null)
                    subtaskSummary += " (+${subtaskCount})" */
                if (subtask.numSubtasks > 0)
                    subtaskSummary += " (+${subtask.numSubtasks})"

                subtaskBinding.listItemSubtaskTextview.text = subtaskSummary
                subtaskBinding.listItemSubtaskProgressSlider.value = subtask.percent?.toFloat() ?: 0F
                subtaskBinding.listItemSubtaskProgressPercent.text = String.format("%.0f%%", subtask.percent?.toFloat() ?: 0F)
                subtaskBinding.listItemSubtaskProgressCheckbox.isChecked = subtask.percent == 100

                // Instead of implementing here
                //        subtaskView.subtask_progress_slider.addOnChangeListener { slider, value, fromUser ->  vJournalItemViewModel.updateProgress(subtask, value.toInt())    }
                //   the approach here is to update only onStopTrackingTouch. The OnCangeListener would update on several times on sliding causing lags and unnecessary updates  */


                subtaskBinding.listItemSubtaskProgressSlider.addOnSliderTouchListener(object :
                    Slider.OnSliderTouchListener {

                    override fun onStartTrackingTouch(slider: Slider) {   /* Nothing to do */
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        if (subtaskBinding.listItemSubtaskProgressSlider.value < 100)
                            resetProgress = subtask.percent ?: 0

                        model.updateProgress(
                            subtask.id,
                            subtaskBinding.listItemSubtaskProgressSlider.value.toInt(),
                            subtask.isLinkedRecurringInstance
                        )
                    }
                })

                subtaskBinding.listItemSubtaskProgressCheckbox.setOnCheckedChangeListener { _, checked ->

                    if (checked)
                        subtaskBinding.listItemSubtaskProgressSlider.value = 100F
                    else
                        subtaskBinding.listItemSubtaskProgressSlider.value = resetProgress.toFloat()

                    model.updateProgress(
                        subtask.id,
                        subtaskBinding.listItemSubtaskProgressSlider.value.toInt(),
                        subtask.isLinkedRecurringInstance
                    )
                }

                subtaskBinding.root.setOnClickListener {
                    subtasksLinearLayout.findNavController().navigate(
                        IcalListFragmentDirections.actionIcalListFragmentToIcalViewFragment()
                            .setItem2show(subtask.id)
                    )
                }

                // on long click we notify the model to get the entity, so that the observer can forward the user to the edit fragment
                subtaskBinding.root.setOnLongClickListener {
                    // the observer in the fragment will make sure that the edit fragment is opened for the loaded entity
                    model.postDirectEditEntity(subtask.id)
                    true
                }



                if(settingShowProgressSubtasks) {
                    subtaskBinding.listItemSubtaskProgressSlider.visibility = View.VISIBLE
                    subtaskBinding.listItemSubtaskProgressPercent.visibility = View.VISIBLE
                } else {
                    subtaskBinding.listItemSubtaskProgressSlider.visibility = View.GONE
                    subtaskBinding.listItemSubtaskProgressPercent.visibility = View.GONE
                }

                subtasksLinearLayout.addView(subtaskBinding.root)
            }
        }



        fun addAttachmentView(attachments: List<Attachment>?, attachmentsLinearLayout: LinearLayout, context: Context, parent: ViewGroup) {

            setSettings(context)

            attachmentsLinearLayout.removeAllViews()

            if(!settingShowAttachments)
                return

            attachments?.forEach { attachment ->

                val attachmentBinding = FragmentIcalListItemAttachmentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                //open the attachment on click
                attachmentBinding.listItemAttachmentCardview.setOnClickListener {
                    attachment.openFile(context)
                }


                when {
                    attachment.filename?.isNotEmpty() == true -> attachmentBinding.listItemAttachmentTextview.text = attachment.filename
                    attachment.fmttype?.isNotEmpty() == true -> attachmentBinding.listItemAttachmentTextview.text = attachment.fmttype
                    else -> attachmentBinding.listItemAttachmentTextview.text = ""
                }

                if (attachment.filesize == null)
                    attachmentBinding.listItemAttachmentFilesize.visibility = View.GONE
                else
                    attachmentBinding.listItemAttachmentFilesize.text = getAttachmentSizeString(attachment.filesize?:0L)

                // load thumbnail if possible

                try {
                    val thumbSize = Size(50, 50)
                    val thumbUri = Uri.parse(attachment.uri)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val thumbBitmap =
                            context.contentResolver!!.loadThumbnail(thumbUri, thumbSize, null)
                        attachmentBinding.listItemAttachmentPictureThumbnail.setImageBitmap(
                            thumbBitmap
                        )
                        attachmentBinding.listItemAttachmentPictureThumbnail.visibility =
                            View.VISIBLE
                    }
                } catch (e: FileNotFoundException) {
                    Log.d("FileNotFound", "File with uri ${attachment.uri} not found.\n$e")
                }


                attachmentsLinearLayout.addView(attachmentBinding.root)
            }
        }

        private fun setSettings(context: Context) {

            //load settings
            settings = PreferenceManager.getDefaultSharedPreferences(context)
            settingShowSubtasks = settings.getBoolean(SettingsFragment.SHOW_SUBTASKS_IN_LIST, true)
            settingShowAttachments = settings.getBoolean(SettingsFragment.SHOW_ATTACHMENTS_IN_LIST, true)
            settingShowProgressMaintasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_MAINTASKS_IN_LIST, false)
            settingShowProgressSubtasks = settings.getBoolean(SettingsFragment.SHOW_PROGRESS_FOR_SUBTASKS_IN_LIST, true)
        }
    }







}


