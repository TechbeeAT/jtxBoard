package at.techbee.jtx

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener

class BillingLoader {

    companion object {

        private var billingClient: BillingClient? = null



        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
            }



        fun initialise(activity: MainActivity) {

            billingClient = BillingClient.newBuilder(activity)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()


            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        Log.d("Billing Client", "Connection OK")
                        // The BillingClient is ready. You can query purchases here.
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.d("Billing Client", "Connection DISCONNECTED")
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })

        }



    }



}