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
import at.techbee.jtx.util.DateTimeUtils
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.*
import com.huawei.hms.iap.entity.InAppPurchaseData.PurchaseState
import com.huawei.hms.support.api.client.Status
import org.json.JSONException


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

        /*
        val firstInstall = context.packageManager?.getPackageInfoCompat(context.packageName, 0)?.firstInstallTime ?: System.currentTimeMillis()
        if(firstInstall < 1674514800000L)
            return
         */

        if (billingPrefs == null)
            billingPrefs = context.getSharedPreferences(PREFS_BILLING, Context.MODE_PRIVATE)
        isProPurchased.value = billingPrefs?.getBoolean(PREFS_BILLING_PURCHASED, true) ?: true

        val isEnvReadyTask = Iap.getIapClient(context).isEnvReady
        isEnvReadyTask.addOnSuccessListener {
            // Obtain the execution result.
            //val carrierId = result.carrierId
            obtainProduct(context)
            obtainPurchases(context)

        }.addOnFailureListener { e ->
            if (e is IapApiException) {
                if (e.status.statusCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                    getErrorToast(context).show()
                } else if (e.status.statusCode == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
                    // The current country/region does not support HUAWEI IAP.
                    isProPurchased.postValue(true)
                    billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, true)?.apply()
                }
            } else {
                // Other external errors.
            }
        }
    }

    private fun obtainProduct(context: Context) {
        val req = ProductInfoReq().apply {
            priceType = 1 // priceType: 0: consumable; 1: non-consumable; 2: subscription
            productIds = arrayListOf(IN_APP_PRODUCT_PRO)
        }
        val obtainProductInfoTask = Iap.getIapClient(context).obtainProductInfo(req)
        obtainProductInfoTask.addOnSuccessListener { result ->
            // Obtain the product details returned upon a successful API call.
            Log.d("BillingManager",  "productId" + result.productInfoList.firstOrNull()?.productId)
            Log.d("BillingManager",  "price" + result.productInfoList.firstOrNull()?.price)

            val product = result.productInfoList.firstOrNull() ?: return@addOnSuccessListener
            proPrice.postValue(product.price)
        }.addOnFailureListener { e ->
            Log.w("BillingManager", e.stackTraceToString())
        }
    }

    private fun obtainPurchases(context: Context) {
        val ownedPurchasesReq = OwnedPurchasesReq().apply {
            this.priceType = 1   // Set priceType to 1 (non-consumable).
        }
        val obtainOwnedPurchasesTask = Iap.getIapClient(context).obtainOwnedPurchases(ownedPurchasesReq)
        obtainOwnedPurchasesTask.addOnSuccessListener { result ->
            // Obtain the execution result.
            try {
                val inAppPurchaseDataBean = result?.inAppPurchaseDataList?.map { InAppPurchaseData(it) }
                val lastPurchase = inAppPurchaseDataBean?.findLast { it.purchaseState == PurchaseState.PURCHASED || it.purchaseState == PurchaseState.INITIALIZED || it.purchaseState == PurchaseState.PENDING }
                Log.d("BillingManager", "lastPurchase: ${lastPurchase?.purchaseTime} - orderId: ${lastPurchase?.orderID} - purchase size: ${inAppPurchaseDataBean?.size}")
                if (lastPurchase != null) {
                    proPurchaseDate.postValue(DateTimeUtils.convertLongToFullDateTimeString(lastPurchase.purchaseTime, null))
                    proOrderId.postValue(lastPurchase.orderID)
                    isProPurchased.postValue(true)
                    billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, true)?.apply()

                    //ONLY FOR TESTING - consume purchase to start purchase again - this will be called in onResume!
                    val req = ConsumeOwnedPurchaseReq().apply { purchaseToken = lastPurchase.purchaseToken }
                    val task = Iap.getIapClient(context).consumeOwnedPurchase(req)
                    task.addOnSuccessListener { // Consume success
                        Log.i("IAP","consumeOwnedPurchase success")
                    }.addOnFailureListener {  }

                    return@addOnSuccessListener
                }
            } catch (e: JSONException) {
                Log.w("BillingManager", e.stackTraceToString())
            }

            // if no early return, it was not purchased
            billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, false)?.apply()
            isProPurchased.postValue(false)
        }.addOnFailureListener { e ->
            Log.w("BillingManager", e.stackTraceToString())
        }
    }

    override fun launchBillingFlow(activity: Activity) {

        val req = PurchaseIntentReq().apply {    // Construct a PurchaseIntentReq object.
            productId = IN_APP_PRODUCT_PRO  // Only those products already configured in AppGallery Connect can be purchased through the createPurchaseIntent API.
            priceType = 1 // priceType: 0: consumable; 1: non-consumable; 2: subscription
            //developerPayload = "test"
        }

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
            getErrorToast(activity).show()
            if (e is IapApiException) {
                Log.w("BillingManager", "${e.statusCode} - ${e.status}")
            } else {
                Log.w("BillingManager", e.stackTraceToString())
            }
        }
    }

}