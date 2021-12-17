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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.*
import java.lang.ClassCastException

class AdManager {

    companion object {

        private val ADMOB_UNIT_ID_REWARDED_INTERSTITIAL = if(BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5354046379"      // Test Admob Unit ID for rewarded interstitials
        else
            "ca-app-pub-5573084047491645/3240430994"      // Prod Admob Unit ID for rewarded interstitials

        var consentInformation: ConsentInformation? = null
        private var consentForm: ConsentForm? = null
        var rewardedInterstitialAd: RewardedInterstitialAd? = null

        private var adPrefs: SharedPreferences? = null

        private const val PREFS_ADS = "sharedPreferencesAds"
        private const val PREFS_ADS_NEXT_AD = "prefsNextAd"

        private val TIME_TO_NEXT_AD = if(BuildConfig.DEBUG)
            300000L     // = 5 min
        else
            604800000L  // = one week

        private val TIME_TO_FIRST_AD = if(BuildConfig.DEBUG)
            60000L    // = 10 min
        else
            86400000L     // = one day

        private var mainActivity: MainActivity? = null

        /**
         * Initializes the AdManager. This mainly passes the mainActivity to the AdManager to load and provide the AdPreferences
         */
        fun initialize(activity: Activity) {
            try {
                mainActivity = activity as MainActivity
            } catch (e: ClassCastException) {
                Log.w("AdLoader", "Class Cast from Activity to MainActivity failed! \n$e")
            }
            adPrefs = activity.getSharedPreferences(PREFS_ADS, Context.MODE_PRIVATE)
        }

        /**
         * @return true if an ad should be shown (current time < nextAdTime
         */
        fun isAdShowtime(): Boolean {
            val nextAdTime = adPrefs?.getLong(PREFS_ADS_NEXT_AD, 0L) ?: return false      // return false if adPrefs was not correctly initialized
            if (nextAdTime == 0L) {               // initially set the shared preferences to today + one day
                adPrefs?.edit()?.putLong(PREFS_ADS_NEXT_AD, System.currentTimeMillis() + TIME_TO_FIRST_AD)?.apply()
                return true             // initially we return true to show the ad-info dialog, but when the user saves an entry, the setting will be updated in the future and no ad will be shown until the TIME_TO_FIRST_AD is reached
            }
            return System.currentTimeMillis() > nextAdTime
        }

        /**
         * Shows the ad if the ad was loaded and ready
         */
        fun showAd() {
            mainActivity?.let { act ->
                if (isAdShowtime() && rewardedInterstitialAd != null) {
                    rewardedInterstitialAd?.show(act, act)
                } else {
                    Log.d("AdLoader", "The interstitial ad wasn't ready yet.")
                }
            }
        }


        private fun loadAd(context: Context) {

            MobileAds.initialize(context) {

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

        /**
         *  Loads the consent form and takes care of the response. If everything was okay (or the consent was not needed), the ads are set up
         */
        private fun loadForm(activity: Activity, context: Context, consentInformation: ConsentInformation) {

            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInformation.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {

                UserMessagingPlatform.loadConsentForm(
                    context,
                    { consentForm ->
                        consentForm.show(activity) {
                            loadForm(activity, context, consentInformation)    //Handle dismissal by reloading form
                        }
                        Companion.consentForm = consentForm
                        loadAd(context)
                    }
                ) {
                    Log.d("consentForm", it.message)     // Handle the error just with a log message
                }
            } else {
                loadAd(context)
            }
        }


        /**
         * Resets the user consent information and shows the consent form again
         */
        fun resetUserConsent(activity: Activity, context: Context) {
            // TODO: Not sure if this is correct here. This should be reviewed at again, but for now it is working.
            consentInformation?.reset()
            initializeUserConsent(activity, context)
        }

        /**
         * Processes the ad reward by setting the preference when to show the next add to one week from now
         * Additionally sets the rewardedInterstitialAd back to null and calls setUpAds again to load another ad.
         */
        fun processAdReward(context: Context) {
            adPrefs?.edit()?.putLong(PREFS_ADS_NEXT_AD, (System.currentTimeMillis() + TIME_TO_NEXT_AD))?.apply()
            rewardedInterstitialAd = null
            loadAd(context)
        }

    }
}