/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionsHelper {

    companion object {

        /**
         * Checks if the RECORD_AUDIO permission was already granted and requests it if it was not granted yet
         * @return true if the permission was already granted, false if the permission was missing and is requested now
         */
        fun checkPermissionRecordAudio(activity: Activity): Boolean {

            // Check if the permission to record audio is already granted, otherwise make a dialog to ask for permission
            if (ContextCompat.checkSelfPermission(activity.applicationContext, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

                MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.view_fragment_audio_permission))
                    .setMessage(activity.getString(R.string.view_fragment_audio_permission_message))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .show()

                return false
            }
            else
                return true
        }
    }
}