/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.monetization

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import at.techbee.jtx.MainActivity


interface AdManagerDefinition {

    val unitIdInterstitialTest: String?      // Test Admob Unit ID for rewarded interstitials
    val unitIdInterstitial: String?      // Prod Admob Unit ID for rewarded interstitials

    val unitIdBannerTest: String?
    val unitIdBannerView: String?
    val unitIdBannerListJournal: String?
    val unitIdBannerListNote: String?
    val unitIdBannerListTodo: String?


    /**
     * @return true if ads should basically be shown for this flavor
     */
    fun isAdFlavor(): Boolean

    /**
     * @return true if a user consent is required
     */
    fun isConsentRequired(): Boolean

    /**
     * Resets the user consent information and shows the consent form again
     */
    fun resetUserConsent(activity: MainActivity, context: Context)

    /**
     * This function initializes the AdManager and loads the adPrefs.
     * It loads the user consent to check if a user consent is needed
     */
    fun checkOrRequestConsentAndLoadAds(activity: MainActivity, context: Context)

    /**
     * Shows the ad if the ad was loaded and ready
     */
    fun showInterstitialAd(activity: Activity)

    /**
     * Loads a banner ad with an optional given unitId and adds it to the LinearLayout
     * [linearLayout] to which the banner should be added
     * [context] calling context
     * [unitId] the UnitId, preferrable as defined in AdManager e.g. ADMOB_UNIT_ID_BANNER_LIST_JOURNAL
     */
    fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?)

    /**
     * Function to pause all banner ads to not make any more network requests to refresh (necessary onPause()!)
     */
    fun pauseAds()

    /**
     * Resumes ad refreshing after pauseAds(), should be done in onResume()
     */
    fun resumeAds()

}
