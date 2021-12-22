/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.monetization

import android.app.Activity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class BillingManager :
    LifecycleObserver, BillingManagerDefinition {

    companion object {

        @Volatile
        private var INSTANCE: BillingManager? = null

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

    override var isAdFreeSubscriptionPurchased: LiveData<Boolean> = MutableLiveData(false)   // always false for global flavor
    override val adFreeSubscriptionPrice = MutableLiveData<String?>(null)
    override val adFreeSubscriptionPurchaseDate = MutableLiveData<String?>(null)
    override val adFreeSubscriptionOrderId = MutableLiveData<String?>(null)

    override fun initialise(activity: Activity) { /* nothing to do for this flavor */ }
    override fun launchSubscriptionBillingFlow(activity: Activity) { /* nothing to do for this flavor */ }
}