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
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.*


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


    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var interstitialAd: InterstitialAd? = null


    /**
     *  Loads the consent form and takes care of the response. If everything was okay (or the consent was not needed), the ads are set up
     *  If no user consent is necessary, the ads are loaded directly
     */
    private fun loadForm(activity: Activity, context: Context, consentInformation: ConsentInformation) {

        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInformation.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {

            UserMessagingPlatform.loadConsentForm(
                context,
                { consentForm ->
                    consentForm.show(activity) {
                        loadForm(activity, context, consentInformation)    //Handle dismissal by reloading form
                    }
                    this.consentForm = consentForm
                    loadAds(context)
                }
            ) {
                Log.d("consentForm", it.message)     // Handle the error just with a log message
            }
        } else {
            loadAds(context)
        }
    }


    /**
     * Initializes the Admob MobileAds and loads an interstitial Ad
     */
    private fun loadAds(context: Context) {

        if(BuildConfig.DEBUG) {
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(
                listOf(
                    "C4E10B8B06DB3B7287C2097746D070C4",
                    "D69766433D841525603C53DC036422CD"
                )
            ).build()
            MobileAds.setRequestConfiguration(configuration)
        }


        val interstitialUnitId = if(BuildConfig.DEBUG)
            unitIdInterstitialTest
        else
            unitIdInterstitial

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, interstitialUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("InterstitalAd", adError.message)
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("InterstitalAd", "Ad was loaded.")
                interstitialAd = ad
            }
        })


        interstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("InterstitalAd", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d("InterstitalAd", "Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d("InterstitalAd", "Ad showed fullscreen content.")
                interstitialAd = null
                loadAds(context)
            }
        }
    }

    override fun isAdFlavor() = true

    override fun isConsentRequired(): Boolean = consentInformation?.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInformation?.consentStatus == ConsentInformation.ConsentStatus.OBTAINED

    override fun resetUserConsent(activity: MainActivity, context: Context) {
        // TODO: Not sure if this is correct here. This should be reviewed at again, but for now it is working.
        consentInformation?.reset()
        checkOrRequestConsentAndLoadAds(activity, context)
    }


    /**
     * This function initializes the AdManager and loads the adPrefs.
     * It loads the user consent to check if a user consent is needed
     */
    override fun checkOrRequestConsentAndLoadAds(activity: MainActivity, context: Context) {

        val consentParams = ConsentRequestParameters.Builder().apply {
            this.setTagForUnderAgeOfConsent(false)
            if(BuildConfig.DEBUG) {
                val debugSettings = ConsentDebugSettings.Builder(context)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("C4E10B8B06DB3B7287C2097746D070C4")
                    .build()
                this.setConsentDebugSettings(debugSettings)    // set Geography to EEA for DEBUG builds
            }
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

        this.consentInformation = consentInformation

        INSTANCE = this
    }

    override fun showInterstitialAd(activity: Activity) {
        if (interstitialAd != null)
            interstitialAd?.show(activity)
        else
            Log.d("AdLoader", "The interstitial ad wasn't ready yet.")
    }

    override fun addAdViewToContainerViewFragment(linearLayout: LinearLayout, context: Context, unitId: String?) {
        val adView = AdView(context)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = if(BuildConfig.DEBUG)
            unitIdBannerTest
        else
            unitId ?: unitIdBannerView
        adView.loadAd(AdRequest.Builder().build())

        linearLayout.removeAllViews()
        linearLayout.addView(adView)
        linearLayout.visibility = View.VISIBLE
    }
}