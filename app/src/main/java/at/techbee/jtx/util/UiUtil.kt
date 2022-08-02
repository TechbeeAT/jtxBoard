/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.util

import androidx.core.util.PatternsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

object UiUtil {

    fun isValidURL(urlString: String?): Boolean {
        return PatternsCompat.WEB_URL.matcher(urlString.toString()).matches()
    }
}