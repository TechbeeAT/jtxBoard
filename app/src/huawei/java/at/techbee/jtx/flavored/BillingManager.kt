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
import android.content.IntentSender
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.ProductInfoReq
import com.huawei.hms.iap.entity.PurchaseIntentReq
import com.huawei.hms.support.api.client.Status


class BillingManager :
    LifecycleObserver, BillingManagerDefinition {

    companion object {

        @Volatile
        private lateinit var INSTANCE: BillingManager

        private const val IN_APP_PRODUCT_PRO = "pro"
        private const val PREFS_BILLING = "sharedPreferencesBilling"
        private const val PREFS_BILLING_PURCHASED = "prefsBillingPurchased"

        fun getInstance(): BillingManager {
            synchronized(this) {
                // Copy the current value of INSTANCE to a local variable so Kotlin can smart cast.
                // Smart cast is only available to local variables.
                //val instance = INSTANCE
                // If instance is `null` assign a new instance.
                if(!this::INSTANCE.isInitialized) {
                    INSTANCE = BillingManager()
                }
                return INSTANCE
            }
        }
    }

    override val isProPurchased = MutableLiveData(true)
    override val proPrice = MutableLiveData("-")
    override val proPurchaseDate = MutableLiveData("-")
    override val proOrderId = MutableLiveData("-")

    private var billingPrefs: SharedPreferences? = null

    override fun initialise(context: Context) {

        val req = ProductInfoReq().apply {
            priceType = 1 // priceType: 0: consumable; 1: non-consumable; 2: subscription
            productIds = arrayListOf(IN_APP_PRODUCT_PRO)
        }
        val task = Iap.getIapClient(context).obtainProductInfo(req)
        task.addOnSuccessListener { result ->
            // Obtain the product details returned upon a successful API call.
            val product = result.productInfoList.firstOrNull() ?: return@addOnSuccessListener
            proPrice.postValue(product.price)
        }.addOnFailureListener { e ->
            Log.w("BillingManager", e.stackTraceToString())
        }

    }

    override fun launchBillingFlow(activity: Activity) {

        val req = PurchaseIntentReq()   // Construct a PurchaseIntentReq object.
        req.productId = "pro"  // Only those products already configured in AppGallery Connect can be purchased through the createPurchaseIntent API.
        req.priceType = 1 // priceType: 0: consumable; 1: non-consumable; 2: subscription
        req.developerPayload = "test"

// Call the createPurchaseIntent API to create a managed product order.
        val task = Iap.getIapClient(activity).createPurchaseIntent(req)
        task.addOnSuccessListener { result ->
            // Obtain the order creation result.
            val status: Status = result.status
            if (status.hasResolution()) {
                try {
                    status.startResolutionForResult(activity, 1)
                } catch (e: IntentSender.SendIntentException) {
                    Log.w("BillingManager", e.stackTraceToString())
                }
            }
        }.addOnFailureListener { e ->
            if (e is IapApiException) {
                Log.w("BillingManager", "${e.statusCode} - ${e.status}")
            } else {
                Log.w("BillingManager", e.stackTraceToString())
            }
        }
    }

}