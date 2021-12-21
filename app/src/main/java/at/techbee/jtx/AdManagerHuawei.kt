/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.LinearLayout
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


class AdManagerHuawei: Application() {

    companion object {

        private var activity: MainActivity? = null
        var isPersonalizedAdsAllowed = false

        private val HW_UNIT_ID_BANNER_TEST = "testw6vs28auh3"
        val HW_UNIT_ID_BANNER_VIEW = "s29u6so07k"
        val HW_UNIT_ID_BANNER_LIST_JOURNAL = "ca-app-pub-5573084047491645/9205580258"
        val HW_UNIT_ID_BANNER_LIST_NOTE = "ca-app-pub-5573084047491645/2684613362"
        val HW_UNIT_ID_BANNER_LIST_TODO = "ca-app-pub-5573084047491645/8866878338"


        fun initialize(activity: MainActivity) {
            this.activity = activity
            HwAds.init(activity)
            checkConsentStatus()
        }

        /**
         * Loads a banner ad with an optional given unitId and adds it to the LinearLayout
         * [linearLayout] to which the banner should be added
         * [context] calling context
         * [unitId] the UnitId, preferrable as defined in AdManager e.g. ADMOB_UNIT_ID_BANNER_LIST_JOURNAL
         */
        fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) {
            val bannerView = BannerView(context)
            bannerView.adId = if(BuildConfig.DEBUG)
                HW_UNIT_ID_BANNER_TEST
            else
                unitId ?: HW_UNIT_ID_BANNER_VIEW
            bannerView.bannerAdSize = BannerAdSize.BANNER_SIZE_360_57

            // Create an ad request to load an ad.
            val adParam = AdParam.Builder().build()
            bannerView.loadAd(adParam)

            linearLayout.removeAllViews()
            linearLayout.addView(bannerView)
            linearLayout.visibility = View.VISIBLE
        }

        private fun checkConsentStatus() {

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
            requestOptions = requestOptions?.toBuilder()?.setNonPersonalizedAd(ALLOW_NON_PERSONALIZED)?.build()
            HwAds.setRequestOptions(requestOptions)
            isPersonalizedAdsAllowed = allowPersonalized
        }
    }
}
