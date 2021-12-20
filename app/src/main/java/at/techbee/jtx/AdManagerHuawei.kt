/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.Application
import com.huawei.hms.ads.AdParam
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

        fun initialize(activity: MainActivity) {
            this.activity = activity
            HwAds.init(activity)
            checkConsentStatus()
        }

        fun loadBannerAdForView(hwBannerView: BannerView) {

            // Set the refresh interval to 60 seconds.
            hwBannerView.setBannerRefresh(60)
            // Create an ad request to load an ad.
            val adParam = AdParam.Builder().build()
            hwBannerView.loadAd(adParam)
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
