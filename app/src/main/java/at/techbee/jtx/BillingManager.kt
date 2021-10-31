package at.techbee.jtx

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.*
import kotlinx.coroutines.*

class BillingManager :
    LifecycleObserver {

    companion object {

        private var billingClient: BillingClient? = null
        var adfreeSkuDetails: SkuDetails? = null
        var adfreeSubSkuDetails: SkuDetails? = null
        private const val IN_APP_PRODUCT_ADFREE = "adfree"
        private const val IN_APP_PRODUCT_ADFREE_SUBSCRIPTION = "adfreesub"


        private var billingPrefs: SharedPreferences? = null
        private const val PREFS_BILLING = "sharedPreferencesBilling"
        private const val PREFS_BILLING_ADFREE_PURCHASE_STATE = "prefsBillingAdfreePurchaseState"
        private const val PREFS_BILLING_ADFREESUB_PURCHASE_STATE = "prefsBillingAdfreeSubPurchaseState"


        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases?.forEach { purchase ->
                        CoroutineScope(Dispatchers.IO).launch {
                            handlePurchase(purchase)
                        }
                    }
                }
            /*
                else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                } else {
                    // Handle any other error codes.
                }
            */
            }


        /**
         * Initialises the billing client (if not initialised yet)
         * and makes the variable billingClient available for further use.
         * The initialisiation also calls querySkuDetails().
         */
        fun initialise(activity: Activity) {

            // initialisation is done already, just return and do nothing
            if(billingClient != null && adfreeSkuDetails != null && adfreeSubSkuDetails != null) {
                // if everything is initialised we doublecheck if we missed a purchase and update it if necessary
                updatePurchases()
                return
            }

            if (billingPrefs == null)
                billingPrefs = activity.getSharedPreferences(PREFS_BILLING, Context.MODE_PRIVATE)


            billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()


            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        Log.d("Billing Client", "Connection OK")

                        // The BillingClient is ready. You can query purchases here.
                        CoroutineScope(Dispatchers.IO).launch {
                            querySkuDetails()
                        }

                        // once everything is initialised we doublecheck if we missed a purchase and update it if necessary
                        updatePurchases()
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

            //query one-time payments
            val skuList = ArrayList<String>()
            skuList.add(IN_APP_PRODUCT_ADFREE)
            val params = SkuDetailsParams.newBuilder().apply {
                this.setSkusList(arrayListOf(IN_APP_PRODUCT_ADFREE))
                this.setType(BillingClient.SkuType.INAPP)
            }.build()

            withContext(Dispatchers.IO) {
                Log.d("Billing Client", "Querying products")
                val queryResult = billingClient?.querySkuDetails(params)
                queryResult?.skuDetailsList?.forEach {
                    if(it.sku == IN_APP_PRODUCT_ADFREE)
                        adfreeSkuDetails = it
                }
            }

            // now query subscriptions
            val skuListSub = ArrayList<String>()
            skuListSub.add(IN_APP_PRODUCT_ADFREE)
            val paramsSub = SkuDetailsParams.newBuilder().apply {
                this.setSkusList(arrayListOf(IN_APP_PRODUCT_ADFREE_SUBSCRIPTION))
                this.setType(BillingClient.SkuType.SUBS)
            }.build()

            withContext(Dispatchers.IO) {
                Log.d("Billing Client", "Querying subscriptions")
                val queryResult = billingClient?.querySkuDetails(paramsSub)
                queryResult?.skuDetailsList?.forEach {
                    if(it.sku == IN_APP_PRODUCT_ADFREE_SUBSCRIPTION)
                        adfreeSubSkuDetails = it
                }
            }


            // Process the result.
        }


        /**
         * This function launches the billing flow from Google Play.
         * It shows a bar on the bototm of the page where the user can buy the item.
         * The passed skuDetails are currently either [BillingManager.adfreeSkuDetails]
         * or [BillingManager.adfreeSubSkuDetails].
         */
        fun launchBillingFlow(activity: Activity, skuDetails: SkuDetails?) {

            if(billingClient != null && skuDetails != null) {
                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
                val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
            } else {
                Toast.makeText(activity, "Ooops, something went wrong there. Please check your internet connection or try again later!", Toast.LENGTH_LONG).show()
                this.initialise(activity)
            }
        }

        /**
         * This method queries the purchases and updates the purchase status through queryPurchasesAsync.
         * This is necessary as the listener might have missed an update.
         */
        private fun updatePurchases() {

            CoroutineScope(Dispatchers.IO).launch {
                val purchases = billingClient?.queryPurchasesAsync(BillingClient.SkuType.INAPP)
                purchases?.purchasesList?.forEach { purchase ->
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
                    val ackPurchaseResult = withContext(Dispatchers.IO) {
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
                    }
                }
            }

            purchase.skus.forEach {
                when (it) {
                    IN_APP_PRODUCT_ADFREE -> billingPrefs?.edit()?.putString(PREFS_BILLING_ADFREE_PURCHASE_STATE, purchase.purchaseState.toString())?.apply()
                    IN_APP_PRODUCT_ADFREE_SUBSCRIPTION -> billingPrefs?.edit()?.putString(PREFS_BILLING_ADFREESUB_PURCHASE_STATE, purchase.purchaseState.toString())?.apply()
                }
            }
        }

        /**
         * @return true if either the one-time item got the status PURCHASED or the subscription got the status PURCHASED, otherwise false
         */
        fun isPurchased(): Boolean = isAdfreePurchased() || isSubscriptionPurchased()
        fun isAdfreePurchased(): Boolean = billingPrefs?.getString(PREFS_BILLING_ADFREE_PURCHASE_STATE, Purchase.PurchaseState.UNSPECIFIED_STATE.toString()) == Purchase.PurchaseState.PURCHASED.toString()
        fun isSubscriptionPurchased(): Boolean = billingPrefs?.getString(PREFS_BILLING_ADFREESUB_PURCHASE_STATE, Purchase.PurchaseState.UNSPECIFIED_STATE.toString()) == Purchase.PurchaseState.PURCHASED.toString()
    }
}