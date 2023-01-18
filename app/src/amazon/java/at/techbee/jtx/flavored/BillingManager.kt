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
import android.widget.Toast
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import at.techbee.jtx.util.DateTimeUtils
import at.techbee.jtx.util.getPackageInfoCompat
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse


class BillingManager :
    LifecycleObserver, BillingManagerDefinition {

    companion object {

        @Volatile
        private lateinit var INSTANCE: BillingManager

        private const val IN_APP_PRODUCT_PRO = "jtx_board_pro"
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


    /**
     * Initialises the billing client (if not initialised yet)
     * and makes the variable billingClient available for further use.
     * The initialisiation also calls querySkuDetails().
     */
    override fun initialise(context: Context) {

        val firstInstall = context.packageManager?.getPackageInfoCompat(context.packageName, 0)?.firstInstallTime ?: System.currentTimeMillis()
        if(firstInstall < 1674514800000L)
            return


        if (billingPrefs == null)
            billingPrefs = context.getSharedPreferences(PREFS_BILLING, Context.MODE_PRIVATE)
        isProPurchased.value = billingPrefs?.getBoolean(PREFS_BILLING_PURCHASED, true) ?: true

        PurchasingService.registerListener(context, object : PurchasingListener {
            override fun onUserDataResponse(p0: UserDataResponse?) {
                // nothing to do
            }

            override fun onProductDataResponse(response: ProductDataResponse?) {
                proPrice.value = response?.productData?.get(IN_APP_PRODUCT_PRO)?.price ?: ""
            }

            override fun onPurchaseResponse(response: PurchaseResponse?) {
                when (response?.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {
                        val receipt = response.receipt
                        proPurchaseDate.postValue(DateTimeUtils.convertLongToFullDateTimeString(receipt.purchaseDate.toInstant().toEpochMilli(), null))
                        proOrderId.postValue(receipt.receiptId)
                        isProPurchased.postValue(true)
                        billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, true)?.apply()
                    }
                    PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> {
                        val receipt = response.receipt
                        proPurchaseDate.postValue(DateTimeUtils.convertLongToFullDateTimeString(receipt.purchaseDate.toInstant().toEpochMilli(), null))
                        proOrderId.postValue(receipt.receiptId)
                        isProPurchased.postValue(true)
                        billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, true)?.apply()
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "Ooops, something went wrong there. Please check your internet connection or try again later!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse?) {
                when (response?.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        if (response.receipts.isEmpty() || response.receipts.all { it.isCanceled }) {
                            billingPrefs?.edit()?.remove(PREFS_BILLING_PURCHASED)?.apply()
                            isProPurchased.postValue(false)
                        } else {
                            val receipt = response.receipts.last { !it.isCanceled }
                            proPurchaseDate.postValue(DateTimeUtils.convertLongToFullDateTimeString(receipt.purchaseDate.toInstant().toEpochMilli(), null))
                            proOrderId.postValue(receipt.receiptId)
                            isProPurchased.postValue(true)
                            billingPrefs?.edit()?.putBoolean(PREFS_BILLING_PURCHASED, true)?.apply()
                        }
                    }
                    else -> {}
                }
            }

        })

        PurchasingService.getUserData()
        PurchasingService.getProductData(setOf(IN_APP_PRODUCT_PRO)) // Triggers PurchasingListener.onProductDataResponse()
        PurchasingService.getPurchaseUpdates(true)

    }


    /**
     * This function launches the billing flow from Amazon appstore.
     * It shows a bar on the bottom of the page where the user can buy the item.
     */
    override fun launchBillingFlow(activity: Activity) {
        PurchasingService.purchase(IN_APP_PRODUCT_PRO)
    }
}