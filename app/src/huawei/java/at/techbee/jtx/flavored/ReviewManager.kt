/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.app.Activity

class JtxReviewManager(val activity: Activity) : ReviewManagerDefinition {

    override var nextRequestOn: Long = 0L
    override fun showIfApplicable() = false    // no review manager yet for huawei
}