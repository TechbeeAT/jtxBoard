/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.NonPersonalizedAd.ALLOW_NON_PERSONALIZED
import com.huawei.hms.ads.RequestOptions


class AdManagerHuawei {

    companion object {

        private var activity: MainActivity? = null


        fun initialize(activity: MainActivity) {
            this.activity = activity
            HwAds.init(activity)

            // For the HUAWEI Flavor no proper implementation of a GDPR user consent could be found. That's why we set
            // the parameter in setNonPersonalizedAd to ALLOW_NON_PERSONALIZED to request only non-personalized ads.
            // TODO: Review options again to provide a GDPR message and personalized ads for HUAWEI
            var requestOptions: RequestOptions? = HwAds.getRequestOptions()
            requestOptions = requestOptions?.toBuilder()?.setNonPersonalizedAd(ALLOW_NON_PERSONALIZED)?.build()
            HwAds.setRequestOptions(requestOptions)
        }

        fun loadBannerAdForView(hwBannerView: BannerView) {

            // Set the refresh interval to 60 seconds.
            hwBannerView.setBannerRefresh(60)
            // Create an ad request to load an ad.
            val adParam = AdParam.Builder().build()
            hwBannerView.loadAd(adParam)
        }
    }
}
