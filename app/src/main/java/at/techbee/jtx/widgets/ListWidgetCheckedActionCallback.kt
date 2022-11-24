/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import at.techbee.jtx.database.ICalDatabase

class ListWidgetCheckedActionCallback: ActionCallback {

    companion object {
        val actionWidgetIcalObjectId = ActionParameters.Key<Long>("iCalObjectId")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val iCalObjectId = parameters[actionWidgetIcalObjectId] ?: return
        val database = ICalDatabase.getInstance(context).iCalDatabaseDao
        val iCalObject = database.getICalObjectByIdSync(iCalObjectId) ?: return
        iCalObject.setUpdatedProgress(if(iCalObject.percent == 100) null else 100)
        database.update(iCalObject)
        ListWidgetReceiver.setOneTimeWork(context)
    }
}