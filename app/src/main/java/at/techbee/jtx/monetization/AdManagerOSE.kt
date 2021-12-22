/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.monetization

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.NonPersonalizedAd.ALLOW_NON_PERSONALIZED
import com.huawei.hms.ads.RequestOptions
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.inter.Consent
import com.huawei.hms.ads.consent.inter.ConsentUpdateListener


class AdManagerOSE : AdManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: AdManagerOSE? = null

        fun getInstance(): AdManagerOSE? {

            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                val instance = INSTANCE
                // If instance is `null` assign a new instance.
                if (instance == null) {
                    INSTANCE = AdManagerOSE()
                }
                return INSTANCE
            }
        }
    }

    override val unitIdRewardedInterstitialTest: String? = null
    override val unitIdRewardedInterstitial: String? = null
    override val unitIdBannerTest: String? = null
    override val unitIdBannerView: String? = null
    override val unitIdBannerListJournal: String? = null
    override val unitIdBannerListNote: String? = null
    override val unitIdBannerListTodo: String? = null

    override fun isAdFlavor() = false
    override fun isConsentRequired() = false
    override fun checkOrRequestConsentAndLoadAds(activity: MainActivity, context: Context) { /* nothing to do for ose */  }
    override fun resetUserConsent(activity: MainActivity, context: Context) { /* nothing to do for ose */  }
    override fun showInterstitialAd() { /* nothing to do for ose */  }
    override fun processAdReward(context: Context) { /* nothing to do for ose */  }
    override fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) { /* nothing to do for ose */  }
}
