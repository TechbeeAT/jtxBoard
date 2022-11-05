/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters


class ListWidgetUpdateWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(
    context,
    workerParameters
) {
    override suspend fun doWork(): Result {
        ListWidgetReceiver.updateListWidgets(context)
        return Result.success()
    }
}