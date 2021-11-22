/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import at.techbee.jtx.BuildConfig.FLAVOR
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.*
import java.lang.ClassCastException

class AdManager {

    companion object {

        private const val ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/5354046379"
        private const val ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_PROD = "ca-app-pub-5573084047491645/3240430994"

        private const val ONE_WEEK_IN_MILLIS = 604800000L  // = one week
        private const val FIVE_MIN_IN_MILLIS = 300000L     // = 5 min

        private val ADMOB_UNIT_ID_REWARDED_INTERSTITIAL = if(BuildConfig.DEBUG)
            ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_TEST
        else
            ADMOB_UNIT_ID_REWARDED_INTERSTITIAL_PROD

        var consentInformation: ConsentInformation? = null
        private var consentForm: ConsentForm? = null
        //var mInterstitialAd: InterstitialAd? = null
        var rewardedInterstitialAd: RewardedInterstitialAd? = null

        private var adPrefs: SharedPreferences? = null

        private var nextAdShowtime: Long = 0L
        private var adsAccepted: Boolean = false

        private const val PREFS_ADS = "sharedPreferencesAds"
        private const val PREFS_ADS_NEXT_AD = "prefsNextAd"
        private const val PREFS_ADS_ACCEPTED = "adsAccepted"
        //private const val ONE_WEEK_IN_MILLIS = 604800000L
        private val TIME_TO_NEXT_AD = if(BuildConfig.DEBUG)
            FIVE_MIN_IN_MILLIS
        else
            ONE_WEEK_IN_MILLIS



        fun isAdShowtime(activity: Activity): Boolean {

            if (adPrefs == null)
                adPrefs = activity.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)

            adPrefs?.let {

                nextAdShowtime = it.getLong(PREFS_ADS_NEXT_AD, 0L)
                if (nextAdShowtime == 0L)               // initially set the shared preferences to today + one week
                    it.edit()?.putLong(
                        PREFS_ADS_NEXT_AD,
                        System.currentTimeMillis() + TIME_TO_NEXT_AD
                    )?.apply()

                return System.currentTimeMillis() > nextAdShowtime
            }
            return false
        }

        fun showAd(activity: Activity) {


            try {
                val mainActivity = activity as MainActivity
                if (isAdShowtime(activity) && rewardedInterstitialAd != null) {
                    rewardedInterstitialAd?.show(mainActivity, mainActivity)
                } else {
                    Log.d("AdLoader", "The interstitial ad wasn't ready yet.")
                }
            } catch (e: ClassCastException) {
                Log.w("AdLoader", "Class Cast from Activity to MainActivity failed! \n$e")
            }

        }

        fun processAdReward(activity: Activity) {

            if (adPrefs == null)
                adPrefs = activity.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)

            adPrefs?.let {
                nextAdShowtime = System.currentTimeMillis() + TIME_TO_NEXT_AD
                it.edit()?.putLong(PREFS_ADS_NEXT_AD, nextAdShowtime)?.apply()
            }
        }


        private fun setUpAds(context: Context) {

            MobileAds.initialize(context) {}

            RewardedInterstitialAd.load(context,
                ADMOB_UNIT_ID_REWARDED_INTERSTITIAL,
                AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialAd = ad
                        rewardedInterstitialAd!!.fullScreenContentCallback = object :
                            FullScreenContentCallback() {
                            /** Called when the ad failed to show full screen content.  */
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.i("onAdFailedToShow", adError.toString())
                            }

                            /** Called when ad showed the full screen content.  */
                            override fun onAdShowedFullScreenContent() {
                                Log.i("onAdShowed", "onAdShowedFullScreenContent")
                            }

                            /** Called when full screen content is dismissed.  */
                            override fun onAdDismissedFullScreenContent() {
                                Log.i("onAdDismissed", "onAdDismissedFullScreenContent")
                            }
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e("onAdFailedToLoad", loadAdError.toString())
                    }
                })
        }


        fun initializeUserConsent(activity: Activity, context: Context) {

            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                //.addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .addTestDeviceHashedId("C4E10B8B06DB3B7287C2097746D070C4")
                .build()

            // Set tag for underage of consent. false means users are not underage. Admob will decide which ads to show and should take care to not show personalized ads to children.
            val consentParams = ConsentRequestParameters.Builder().apply {
                this.setTagForUnderAgeOfConsent(false)
                if(BuildConfig.DEBUG)
                    this.setConsentDebugSettings(debugSettings)    // set Geography to EEA for DEBUG builds
            }.build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(context)
            consentInformation.requestConsentInfoUpdate(
                activity,
                consentParams,
                {
                    // The consent information state was updated.
                    // You are now ready to check if a form is available.
                    Log.d("ConsentInformation", consentInformation.consentStatus.toString())
                    loadForm(activity, context, consentInformation)
                },
                {
                    // Handle the error.
                })

            Companion.consentInformation = consentInformation

        }

        private fun loadForm(activity: Activity, context: Context, consentInformation: ConsentInformation) {

            // don't continue if the flavor is adfree (ose)
            if(FLAVOR == MainActivity.BUILD_FLAVOR_OSE)
                return

            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInformation.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {

                UserMessagingPlatform.loadConsentForm(
                    context,
                    { consentForm ->

                        consentForm.show(activity) {
                            //Handle dismissal by reloading form
                            loadForm(activity, context, consentInformation)
                        }
                        Companion.consentForm = consentForm
                        setUpAds(context)
                    }
                ) {
                    // Handle the error
                    //Log.d("consentForm", "Failed loading consent form")
                    Log.d("consentForm", it.message)
                }
            } else {
                setUpAds(context)
            }
        }


        fun resetUserConsent(activity: Activity, context: Context) {

            // TODO: Not sure if this is correct here. This should be reviewed at again, but for now it is working.
            consentInformation?.reset()
            initializeUserConsent(activity, context)
        }



        fun isAdsAccepted(activity: Activity): Boolean {

            if (adPrefs == null)
                adPrefs = activity.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)

            adPrefs?.let {
                adsAccepted = it.getBoolean(PREFS_ADS_ACCEPTED, false)
            }
            return adsAccepted
        }

        fun setAdsAccepted(isAccepted: Boolean, activity: Activity) {

            if (adPrefs == null)
                adPrefs = activity.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)

            adPrefs?.edit()?.putBoolean(PREFS_ADS_ACCEPTED, isAccepted)?.apply()
        }
    }
}