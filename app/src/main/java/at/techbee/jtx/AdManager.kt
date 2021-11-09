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

        private val ADMOB_UNIT_ID_REWARDED_INTERSTITIAL = if(BuildConfig.DEBUG)
            "ca-app-pub-3940256099942544/5354046379"  // TEST
        else
            "ca-app-pub-4426141011962540/2723893422"    // PROD

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
            300000L     // = 5 min for debug pursposes
        else
            604800000L  // = one week


/*
        fun isTrialPeriod(activity: Activity): Boolean {
            val firstInstalled: Long = activity.applicationContext.packageManager.getPackageInfo(
                activity.applicationContext.packageName,
                0
            ).firstInstallTime
            // TODO come back here
            //val trialEnd = firstInstalled + TimeUnit.DAYS.toMillis(TRIAL_PERIOD_DAYS)
            val trialEnd = firstInstalled + TimeUnit.MINUTES.toMillis(5L)    // for testing

            return System.currentTimeMillis() <= trialEnd
        }
 */

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

            // TODO: replace adUnitId with production Unit Id
            MobileAds.initialize(context) {}

            /*
            // Section to retrieve the width of the device to set the adSize
            val adWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowManager.currentWindowMetrics.bounds.width() / resources.displayMetrics.density.toInt()
            } else {
                val outMetrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                val display = windowManager.defaultDisplay
                @Suppress("DEPRECATION")
                display.getMetrics(outMetrics)
                (outMetrics.widthPixels.toFloat() / outMetrics.density).toInt()
            }

            val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)

            // now that the adSize is determined we can place the add
            val adView = AdView(this)
            //adView.adSize = AdSize.SMART_BANNER          // adaptive ads replace the smart banner
            adView.adSize = adSize
            adView.adUnitId = ADMOB_UNIT_ID_BANNER_TEST

            val adLinearLayout: LinearLayout =
                findViewById(R.id.main_adlinearlayout)  // add to the linear layout container
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)

            adLinearLayout.removeAllViews()
            adLinearLayout.addView(adView)

             */

            // Interstitial Ad Block
            /*
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(this, ADMOB_UNIT_ID_INTERSTITIAL_TEST, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("onAdFailedToLoad", adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("onAdLoaded", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })

            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("onAdDismissedFull", "Ad was dismissed.")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d("onAdFailedToShow", "Ad failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("onAdShowed", "Ad showed fullscreen content.")
                    mInterstitialAd = null
                }
            }
             */

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

            /*
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                //.addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()
             */


            // Set tag for underage of consent. false means users are not underage.
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                //.setConsentDebugSettings(debugSettings)      // only for testing, TODO: remove for production!
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(context)
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
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

            consentForm?.show(activity) {
                //Handle dismissal by reloading form
                loadForm(activity, context, consentInformation!!)
            }
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