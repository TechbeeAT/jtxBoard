/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

interface ReviewManagerDefinition {

    companion object {
        const val PREFS_NEXT_REQUEST = "nextReviewOn"
    }

    var nextRequestOn: Long

    /**
     * This function launches the in-app review or requests a donation by the user depending on flavor
     */
    fun showIfApplicable(): Boolean
}