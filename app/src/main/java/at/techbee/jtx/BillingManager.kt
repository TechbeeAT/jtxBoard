package at.techbee.jtx

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.*
import kotlinx.coroutines.*

class BillingManager :
    LifecycleObserver {

    companion object {

        private var billingClient: BillingClient? = null

        private const val IN_APP_PRODUCT_ADFREE = "adfree"


        var adfreePrice: String? = null
        var adfreeTitle: String? = null
        var adfreeDesc: String? = null


        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }



        fun initialise(activity: MainActivity) {

            // initialisation is done already, just return and do nothing
            if(billingClient != null && adfreePrice != null && adfreeTitle != null && adfreeDesc != null)
                return

            billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()


            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        Log.d("Billing Client", "Connection OK")
                        // The BillingClient is ready. You can query purchases here.

                        //TODO: Really not sure if this will work...
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

        private suspend fun querySkuDetails() {
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
                    adfreePrice = it.price
                    adfreeTitle = it.title
                    adfreeDesc = it.description
                }
            }



            // Process the result.
        }
    }


}