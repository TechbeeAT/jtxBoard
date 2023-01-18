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
import android.widget.Toast
import androidx.lifecycle.LiveData
import at.techbee.jtx.R

interface BillingManagerDefinition {

    val isProPurchased: LiveData<Boolean>
    val proPrice: LiveData<String?>
    val proPurchaseDate: LiveData<String?>
    val proOrderId: LiveData<String?>

    /**
     * Initialises the billing client (if not initialised yet)
     * and makes the variable billingClient available for further use.
     * The initialisiation also calls querySkuDetails().
     */
    fun initialise(context: Context)

    /**
     * This function launches the billing flow from Google Play.
     * It shows a bar on the bototm of the page where the user can buy the item.
     * The passed skuDetails are currently BillingManager.proProductDetails.
     */
    fun launchBillingFlow(activity: Activity)

    fun getErrorToast(context: Context): Toast = Toast.makeText(context, context.getString(R.string.buypro_purchase_init_error_message), Toast.LENGTH_LONG)
}