/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import at.techbee.jtx.MainActivity2


class AdManager : AdManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: AdManager? = null

        fun getInstance(): AdManager? {

            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                val instance = INSTANCE
                // If instance is `null` assign a new instance.
                if (instance == null) {
                    INSTANCE = AdManager()
                }
                return INSTANCE
            }
        }
    }

    override val unitIdInterstitialTest: String? = null
    override val unitIdInterstitial: String? = null
    override val unitIdBannerTest: String? = null
    override val unitIdBannerView: String? = null
    override val unitIdBannerListJournal: String? = null
    override val unitIdBannerListNote: String? = null
    override val unitIdBannerListTodo: String? = null

    override fun isAdFlavor() = false
    override fun isConsentRequired() = false
    override fun checkOrRequestConsentAndLoadAds(activity: MainActivity2, context: Context) { /* nothing to do for ose */  }
    override fun resetUserConsent(activity: MainActivity2, context: Context) { /* nothing to do for ose */  }
    override fun showInterstitialAd(activity: Activity) { /* nothing to do for ose */  }
    override fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) { /* nothing to do for ose */  }
    override fun pauseAds() { /* nothing to do for ose */  }
    override fun resumeAds() { /* nothing to do for ose */  }
}
