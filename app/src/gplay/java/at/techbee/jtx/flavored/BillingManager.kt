/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager :
    LifecycleObserver, BillingManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: BillingManager? = null

        private const val IN_APP_PRODUCT_PRO = "adfree"

        private const val PREFS_BILLING = "sharedPreferencesBilling"
        private const val PREFS_BILLING_PURCHASE_STATE = "prefsBillingPurchaseState"

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
    private var proProductDetails: MutableLiveData<ProductDetails?> =
        MutableLiveData<ProductDetails?>(null)
    private var proPurchase: MutableLiveData<Purchase?> =
        MutableLiveData<Purchase?>(null)


    override lateinit var isProPurchased: LiveData<Boolean>
    override var isProPurchasedLoaded = MutableLiveData(false)

    override val proPrice = Transformations.map(proProductDetails) {
        it?.oneTimePurchaseOfferDetails?.formattedPrice
    }
    override val proPurchaseDate = Transformations.map(proPurchase) {
        DateTimeUtils.convertLongToFullDateTimeString(it?.purchaseTime, null)
    }
    override val proOrderId = Transformations.map(proPurchase) {
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

        if (billingPrefs == null)
            billingPrefs = activity.getSharedPreferences(PREFS_BILLING, Context.MODE_PRIVATE)

        // initialisation is done already, just return and do nothing
        //if(billingClient != null && adfreeOneTimeSkuDetails.value != null && adfreeSubscriptionSkuDetails.value != null) {
        if (billingClient != null && proProductDetails.value != null) {
            // if everything is initialised we doublecheck if we missed a purchase and update it if necessary
            updatePurchases()
            return
        }


        /*
         * Returns true if a purchase status is set for the subscription in the billingPrefs
         * The status itself doesn't matter here, if a status is set, then the user has an active subscription.
         * If the user has no subscription or it expired, the item would not be returned in the purchase list.
         * See also https://developer.android.com/google/play/billing/subscriptions#lifecycle
         */
        isProPurchased =
            Transformations.map(proPurchase) { purchase ->
                purchase?.purchaseState == Purchase.PurchaseState.PURCHASED
                    || billingPrefs?.getString(PREFS_BILLING_PURCHASE_STATE, null) == Purchase.PurchaseState.PURCHASED.toString()
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
                    queryProductDetails()
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
     * and sets the variable proProductDetails that contains the details of
     * the product
     */
    private fun queryProductDetails() {

        val productList =
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(IN_APP_PRODUCT_PRO)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

        billingClient?.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK)
                return@queryProductDetailsAsync

            productDetailsList.forEach { productDetails ->
                if(productDetails.productId == IN_APP_PRODUCT_PRO)
                    proProductDetails.postValue(productDetails)
            }
            updatePurchases()
            // Process the result
        }
    }


    /**
     * This function launches the billing flow from Google Play.
     * It shows a bar on the bottom of the page where the user can buy the item.
     * The passed skuDetails are currently [BillingManager.proProductDetails].
     */
    override fun launchBillingFlow(activity: Activity) {

        if (billingClient != null && proProductDetails.value != null) {

            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams
                        .newBuilder()
                        .setProductDetails(proProductDetails.value!!)
                        .build()
                )
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            // Launch the billing flow
            billingClient?.launchBillingFlow(activity, billingFlowParams)
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

        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchaseList ->
            // Process the result
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK)
                return@queryPurchasesAsync

            if (purchaseList.isEmpty()) {
                billingPrefs?.edit()?.remove(PREFS_BILLING_PURCHASE_STATE)?.apply()
                proPurchase.postValue(null)
                isProPurchasedLoaded.postValue(true)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    purchaseList.forEach { purchase ->
                        handlePurchase(purchase)
                    }
                    isProPurchasedLoaded.postValue(true)
                }
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
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
            }
        }

        if (purchase.products.contains(IN_APP_PRODUCT_PRO)) {
            if (proPurchase.value?.purchaseState != purchase.purchaseState)         // avoid updating live data for nothing
                proPurchase.postValue(purchase)
            billingPrefs?.edit()?.putString(PREFS_BILLING_PURCHASE_STATE, purchase.purchaseState.toString())?.apply()
        } else {
            billingPrefs?.edit()?.remove(PREFS_BILLING_PURCHASE_STATE)?.apply()
            if (proPurchase.value?.purchaseState != purchase.purchaseState)         // avoid updating live data for nothing
                proPurchase.postValue(purchase)
        }
    }
}