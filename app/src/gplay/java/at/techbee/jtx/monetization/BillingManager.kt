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
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import at.techbee.jtx.util.DateTimeUtils
import com.android.billingclient.api.*
import kotlinx.coroutines.*

class BillingManager :
    LifecycleObserver, BillingManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: BillingManager? = null

        private const val IN_APP_PRODUCT_SUBSCRIPTION = "jtx_adfree_sub_quarterly"

        private const val PREFS_BILLING = "sharedPreferencesBilling"
        private const val PREFS_BILLING_SUBSCRIPTION_PURCHASE_STATE =
            "prefsBillingSubscriptionPurchaseState"

        fun getInstance(): BillingManager? {
            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                val instance = INSTANCE
                // If instance is `null` assign a new instance.
                if (instance == null) {
                    INSTANCE = BillingManager()
                }
                return INSTANCE
            }
        }
    }

    private var billingClient: BillingClient? = null
    private var adfreeSubscriptionSkuDetails: MutableLiveData<SkuDetails?> =
        MutableLiveData<SkuDetails?>(null)
    private var adfreeSubscriptionPurchase: MutableLiveData<Purchase?> =
        MutableLiveData<Purchase?>(null)


    override lateinit var isAdFreeSubscriptionPurchased: LiveData<Boolean>

    override val adFreeSubscriptionPrice = Transformations.map(adfreeSubscriptionSkuDetails) {
        it?.price
    }
    override val adFreeSubscriptionPurchaseDate = Transformations.map(adfreeSubscriptionPurchase) {
        DateTimeUtils.convertLongToFullDateTimeString(it?.purchaseTime, null)
    }
    override val adFreeSubscriptionOrderId = Transformations.map(adfreeSubscriptionPurchase) {
        it?.orderId
    }

    private var billingPrefs: SharedPreferences? = null

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchase ->
                    CoroutineScope(Dispatchers.IO).launch {
                        handlePurchase(purchase)
                    }
                }
            }
        }

    /**
     * Initialises the billing client (if not initialised yet)
     * and makes the variable billingClient available for further use.
     * The initialisiation also calls querySkuDetails().
     */
    override fun initialise(activity: Activity) {

        // initialisation is done already, just return and do nothing
        //if(billingClient != null && adfreeOneTimeSkuDetails.value != null && adfreeSubscriptionSkuDetails.value != null) {
        if (billingClient != null && adfreeSubscriptionSkuDetails.value != null) {
            // if everything is initialised we doublecheck if we missed a purchase and update it if necessary
            updatePurchases()
            return
        }

        if (billingPrefs == null)
            billingPrefs = activity.getSharedPreferences(PREFS_BILLING, Context.MODE_PRIVATE)

        /*
         * Returns true if a purchase status is set for the subscription in the billingPrefs
         * The status itself doesn't matter here, if a status is set, then the user has an active subscription.
         * If the user has no subscription or it expired, the item would not be returned in the purchase list.
         * See also https://developer.android.com/google/play/billing/subscriptions#lifecycle
         */
        isAdFreeSubscriptionPurchased =
            Transformations.map(adfreeSubscriptionPurchase) { purchase ->
                purchase?.purchaseState == Purchase.PurchaseState.PURCHASED || billingPrefs?.getString(
                    PREFS_BILLING_SUBSCRIPTION_PURCHASE_STATE,
                    null
                ) == Purchase.PurchaseState.PURCHASED.toString()
            }

        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()


        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("Billing Client", "Connection OK")

                    // The BillingClient is ready. You can query purchases here.
                    CoroutineScope(Dispatchers.IO).launch {
                        querySkuDetails()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("Billing Client", "Connection DISCONNECTED")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }

        })
    }


    /**
     * Queries the available IN-APP products
     * and sets the variable adfreeSkuDetails that contains the details of
     * the ad-free option
     */
    private suspend fun querySkuDetails() {

        // now query subscriptions
        val paramsSub = SkuDetailsParams.newBuilder().apply {
            this.setSkusList(arrayListOf(IN_APP_PRODUCT_SUBSCRIPTION))
            this.setType(BillingClient.SkuType.SUBS)
        }.build()

        withContext(Dispatchers.IO) {
            //Log.d("Billing Client", "Querying subscriptions")
            val queryResult = billingClient?.querySkuDetails(paramsSub)
            queryResult?.skuDetailsList?.forEach {
                if (it.sku == IN_APP_PRODUCT_SUBSCRIPTION)
                    adfreeSubscriptionSkuDetails.postValue(it)
            }
            // once everything is initialised we doublecheck if we missed a purchase and update it if necessary
            updatePurchases()
        }
    }


    /**
     * This function launches the billing flow from Google Play.
     * It shows a bar on the bototm of the page where the user can buy the item.
     * The passed skuDetails are currently [BillingManager.adfreeSubscriptionSkuDetails].
     */
    override fun launchSubscriptionBillingFlow(activity: Activity) {

        if (billingClient != null && adfreeSubscriptionSkuDetails.value != null) {
            // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(adfreeSubscriptionSkuDetails.value!!)
                .build()
            //val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
            billingClient?.launchBillingFlow(activity, flowParams)
        } else {
            Toast.makeText(
                activity,
                "Ooops, something went wrong there. Please check your internet connection or try again later!",
                Toast.LENGTH_LONG
            ).show()
            initialise(activity)
        }
    }

    /**
     * This method queries the purchases and updates the purchase status through queryPurchasesAsync.
     * This is necessary as the listener might have missed an update.
     */
    private fun updatePurchases() {

        CoroutineScope(Dispatchers.IO).launch {
            val subscriptionPurchases =
                billingClient?.queryPurchasesAsync(BillingClient.SkuType.SUBS)
            if (subscriptionPurchases?.purchasesList?.isEmpty() == true) {
                billingPrefs?.edit()?.remove(PREFS_BILLING_SUBSCRIPTION_PURCHASE_STATE)?.apply()
                adfreeSubscriptionPurchase.postValue(null)
            }

            val allPurchases = mutableListOf<Purchase>().apply {
                //addAll(inappPurchases?.purchasesList ?: emptyList())
                addAll(subscriptionPurchases?.purchasesList ?: emptyList())
            }

            allPurchases.forEach { purchase ->
                handlePurchase(purchase)
            }
        }
    }

    /**
     * This method updates the purchase status for each purchase.
     * It stores the purchase status in a shared preference for later usage.
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
            }
        }

        if (purchase.skus.contains(IN_APP_PRODUCT_SUBSCRIPTION)) {
            if (adfreeSubscriptionPurchase.value?.purchaseState != purchase.purchaseState)         // avoid updating live data for nothing
                adfreeSubscriptionPurchase.postValue(purchase)
            billingPrefs?.edit()?.putString(
                PREFS_BILLING_SUBSCRIPTION_PURCHASE_STATE,
                purchase.purchaseState.toString()
            )?.apply()
        } else {
            billingPrefs?.edit()?.remove(PREFS_BILLING_SUBSCRIPTION_PURCHASE_STATE)?.apply()
            if (adfreeSubscriptionPurchase.value?.purchaseState != purchase.purchaseState)         // avoid updating live data for nothing
                adfreeSubscriptionPurchase.postValue(purchase)
        }
    }
}