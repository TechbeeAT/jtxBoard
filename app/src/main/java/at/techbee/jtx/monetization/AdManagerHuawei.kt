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
import com.huawei.hms.ads.NonPersonalizedAd.ALLOW_ALL
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.NonPersonalizedAd.ALLOW_NON_PERSONALIZED
import com.huawei.hms.ads.RequestOptions
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.inter.Consent
import com.huawei.hms.ads.consent.inter.ConsentUpdateListener


class AdManagerHuawei : AdManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: AdManagerHuawei? = null

        fun getInstance(): AdManagerHuawei? {

            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                val instance = INSTANCE
                // If instance is `null` assign a new instance.
                if (instance == null) {
                    INSTANCE = AdManagerHuawei()
                }
                return INSTANCE
            }
        }
    }

    override val unitIdRewardedInterstitialTest: String? = null
    override val unitIdRewardedInterstitial: String? = null
    override val unitIdBannerTest = "testw6vs28auh3"
    override val unitIdBannerView = "s29u6so07k"
    override val unitIdBannerListJournal = "o1zwzcs28c"
    override val unitIdBannerListNote = "o02sczyyg9"
    override val unitIdBannerListTodo = "d1cbvsvy7j"

    override fun isAdFlavor() = true
    override fun isConsentRequired() = false  // consent is not required for huawei as personalized ads get deactivated if required

    override fun checkOrRequestConsentAndLoadAds(activity: MainActivity, context: Context) {
        HwAds.init(activity)
        checkConsentStatus(activity)
    }

    override fun resetUserConsent(activity: MainActivity, context: Context) { /* nothing to do for huawei */ }
    override fun showInterstitialAd() { /* nothing to do for huawei */ }
    override fun processAdReward(context: Context) { /* nothing to do for huawei */ }



    /**
     * Loads a banner ad with an optional given unitId and adds it to the LinearLayout
     * [linearLayout] to which the banner should be added
     * [context] calling context
     * [unitId] the UnitId, preferrable as defined in AdManager e.g. ADMOB_UNIT_ID_BANNER_LIST_JOURNAL
     */
    override fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) {
        val bannerView = BannerView(context)
        bannerView.adId = if(BuildConfig.DEBUG)
            unitIdBannerTest
        else
            unitId ?: unitIdBannerView
        bannerView.bannerAdSize = BannerAdSize.BANNER_SIZE_360_57

        // Create an ad request to load an ad.
        val adParam = AdParam.Builder().build()
        bannerView.loadAd(adParam)

        linearLayout.removeAllViews()
        linearLayout.addView(bannerView)
        linearLayout.visibility = View.VISIBLE
    }

    private fun checkConsentStatus(activity: MainActivity) {

        allowPersonalizedAds(false)    //default false until we checked

        val consentInfo = Consent.getInstance(activity)
        consentInfo.requestConsentUpdate(object : ConsentUpdateListener {
            override fun onSuccess(consentStatus: ConsentStatus, isNeedConsent: Boolean, adProviders: List<AdProvider>) {
                // The parameter indicating whether the consent is required is returned.
                if (!isNeedConsent)
                    allowPersonalizedAds(true)
            }
            override fun onFail(errorDescription: String) {
                // Failed to update user consent status.
                // Nothing to do, personalized Ads are already deactivated
            }
        })
    }

    /**
     * For the HUAWEI Flavor no proper implementation of a GDPR user consent could be found. That's why we set
     * the parameter in setNonPersonalizedAd to ALLOW_NON_PERSONALIZED to request only non-personalized ads
     * if a user consent would be required.
     * TODO: Review options again to provide a GDPR message and personalized ads for HUAWEI
     * see also https://developer.huawei.com/consumer/fr/doc/development/HMSCore-Guides/publisher-service-consent-settings-0000001075342977
     * @param [allowPersonalized] true if personalized ads are allowed
     */
    private fun allowPersonalizedAds(allowPersonalized: Boolean) {
        var requestOptions: RequestOptions? = HwAds.getRequestOptions()
        requestOptions = if(allowPersonalized)
            requestOptions?.toBuilder()?.setNonPersonalizedAd(ALLOW_ALL)?.build()
        else
            requestOptions?.toBuilder()?.setNonPersonalizedAd(ALLOW_NON_PERSONALIZED)?.build()
        HwAds.setRequestOptions(requestOptions)
    }
}
