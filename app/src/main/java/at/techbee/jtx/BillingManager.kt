package at.techbee.jtx

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import com.android.billingclient.api.*
import kotlinx.coroutines.*

class BillingManager :
    LifecycleObserver {

    companion object {

        private var billingClient: BillingClient? = null

        private const val IN_APP_PRODUCT_ADFREE = "adfree"

        var adfreeSkuDetails: SkuDetails? = null


        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }



        fun initialise(activity: Activity) {

            // initialisation is done already, just return and do nothing
            if(billingClient != null && adfreeSkuDetails != null)
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
                    adfreeSkuDetails = it
                }
            }



            // Process the result.
        }


        fun launchBillingFlow(activity: Activity) {

            if(billingClient != null && adfreeSkuDetails != null) {
                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(adfreeSkuDetails!!)
                    .build()
                val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
            } else {
                Toast.makeText(activity, "Ooops, something went wrong there. Please check your internet connection or try again later!", Toast.LENGTH_LONG).show()
                this.initialise(activity)
            }
        }
    }


}