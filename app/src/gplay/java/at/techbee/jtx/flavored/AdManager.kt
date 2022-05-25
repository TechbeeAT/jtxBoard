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
import at.techbee.jtx.MainActivity


class AdManager: AdManagerDefinition {

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

    override val unitIdInterstitialTest = "ca-app-pub-3940256099942544/1033173712"      // Test Admob Unit ID for rewarded interstitials
    override val unitIdInterstitial = "ca-app-pub-5573084047491645/5522012773"      // Prod Admob Unit ID for interstitials

    override val unitIdBannerTest = "ca-app-pub-3940256099942544/6300978111"
    override val unitIdBannerView = "ca-app-pub-5573084047491645/9263174599"
    override val unitIdBannerListJournal = "ca-app-pub-5573084047491645/9205580258"
    override val unitIdBannerListNote = "ca-app-pub-5573084047491645/2684613362"
    override val unitIdBannerListTodo = "ca-app-pub-5573084047491645/8866878338"

    override fun isAdFlavor() = false
    override fun isConsentRequired() = false
    override fun checkOrRequestConsentAndLoadAds(activity: MainActivity, context: Context) { /* nothing to do for ose */  }
    override fun resetUserConsent(activity: MainActivity, context: Context) { /* nothing to do for ose */  }
    override fun showInterstitialAd(activity: Activity) { /* nothing to do for ose */  }
    override fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) { /* nothing to do for ose */  }
    override fun pauseAds() { /* nothing to do for ose */  }
    override fun resumeAds() { /* nothing to do for ose */  }
}