/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.techbee.jtx.*
import at.techbee.jtx.MainActivity.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.MainActivity.Companion.BUILD_FLAVOR_HUAWEI
import at.techbee.jtx.databinding.FragmentAdinfoBinding
import at.techbee.jtx.util.DateTimeUtils


class AdInfoFragment : Fragment() {

    lateinit var binding: FragmentAdinfoBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater

    companion object {
        private const val MANAGE_SUBSCRIPTIONS_LINK = "https://play.google.com/store/account/subscriptions"
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentAdinfoBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        BillingManager.adfreeSubscriptionSkuDetails.observe(viewLifecycleOwner) {
            updateFragmentContent()
        }

        BillingManager.adfreeSubscriptionPurchase.observe(viewLifecycleOwner){
            updateFragmentContent()
        }

                /*
        BillingManager.adfreeOneTimeSkuDetails.observe(viewLifecycleOwner) {
            updateFragmentContent()
        }

        BillingManager.adfreeOneTimePurchase.observe(viewLifecycleOwner){
            updateFragmentContent()
        }
        */



        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_adinfo), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }

        updateFragmentContent()

        super.onResume()
    }


    /**
     * Updates the visibilities of all elements in the fragment.
     * This is called on create but also when the observer detects an update
     * in BillingManager.adfreeSubscriptionSkuDetails or BillingManager.adfreeOneTimeSkuDetails
     */
    private fun updateFragmentContent() {

        // If the user is in a country, where the consent is not required or the initial user consent is still unknown (it must be chosen before the first ad if necessary!) then don't show the button.
        if (AdManager.getInstance()?.isConsentRequired() == true)
            binding.adinfoButtonUserconsent.setOnClickListener {
                AdManager.getInstance()?.resetUserConsent(requireActivity() as MainActivity, requireContext())
            }
         else
            binding.adinfoButtonUserconsent.visibility = View.GONE

        /*
        if(BillingManager.adfreeOneTimeSkuDetails.value == null)
            binding.adinfoCardPurchase.visibility = View.GONE
        else
            binding.adinfoCardPurchase.visibility = View.VISIBLE


        if(BillingManager.adfreeSubscriptionSkuDetails.value == null && BillingManager.adfreeOneTimeSkuDetails.value == null)
            binding.adinfoAdfreeText.visibility = View.GONE

        if(BillingManager.isOneTimePurchased()) {      // change text if item was already bought
            binding.adinfoCardPurchaseHeader.text = getText(R.string.adinfo_adfree_purchase_header_thankyou)
            binding.adinfoCardPurchaseDescription.text = getText(R.string.adinfo_adfree_purchase_description_thankyou)
            binding.adinfoCardPurchasePrice.visibility = View.GONE
        } else {
            BillingManager.adfreeOneTimeSkuDetails.value?.price?.let { binding.adinfoCardPurchasePrice.text = it }
            binding.adinfoCardPurchase.setOnClickListener {
                BillingManager.launchBillingFlow(requireActivity(), BillingManager.adfreeOneTimeSkuDetails.value)
            }
        }
         */

        if(BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY && BillingManager.adfreeSubscriptionSkuDetails.value != null) {

            if (BillingManager.isSubscriptionPurchased()) {      // change text if item was already bought
                binding.adinfoCardSubscribeSuccess.setOnClickListener { return@setOnClickListener }   // actually we remove the listener
                binding.adinfoCardSubscribeSuccessPurchaseDate.text =
                    getString(R.string.adinfo_adfree_subscribe_purchase_date, DateTimeUtils.convertLongToFullDateTimeString(BillingManager.adfreeSubscriptionPurchase.value?.purchaseTime,null))
                binding.adinfoCardSubscribeSuccessOrderNumber.text = getString(
                    R.string.adinfo_adfree_subscribe_order_id,
                    BillingManager.adfreeSubscriptionPurchase.value?.orderId
                )
                binding.adinfoCardSubscribe.visibility = View.GONE
                binding.adinfoAdfreeText.visibility = View.GONE
                binding.adinfoButtonUserconsent.visibility = View.GONE
                binding.adinfoText.visibility = View.GONE
                binding.adinfoCardSubscribeSuccess.visibility = View.VISIBLE
                binding.adinfoThankyouImage.visibility = View.VISIBLE
                binding.adinfoThankyouText.visibility = View.VISIBLE
                binding.adinfoButtonManageSubscriptions.visibility = View.VISIBLE
                binding.adinfoButtonManageSubscriptions.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MANAGE_SUBSCRIPTIONS_LINK)))
                }
            } else {
                BillingManager.adfreeSubscriptionSkuDetails.value?.price?.let {
                    binding.adinfoCardSubscribePrice.text = it
                }
                binding.adinfoCardSubscribeSuccess.visibility = View.GONE
                binding.adinfoThankyouImage.visibility = View.GONE
                binding.adinfoThankyouText.visibility = View.GONE
                binding.adinfoButtonManageSubscriptions.visibility = View.GONE
                binding.adinfoCardSubscribe.setOnClickListener {
                    BillingManager.launchBillingFlow(requireActivity(), BillingManager.adfreeSubscriptionSkuDetails.value)
                }
                binding.adinfoCardSubscribe.visibility = View.VISIBLE
                binding.adinfoAdfreeText.visibility = View.VISIBLE
                binding.adinfoButtonUserconsent.visibility = View.VISIBLE
                binding.adinfoText.visibility = View.VISIBLE

            }
        } else {
            binding.adinfoCardSubscribe.visibility = View.GONE
            binding.adinfoAdfreeText.visibility = View.GONE
            binding.adinfoCardSubscribeSuccess.visibility = View.GONE
            binding.adinfoThankyouImage.visibility = View.GONE
            binding.adinfoThankyouText.visibility = View.GONE
            binding.adinfoButtonManageSubscriptions.visibility = View.GONE

            // we don't show the button to reset the user consent for the HUAWEI flavor. Currently there is no user consent implemented for HUAWEI.
            // If a user consent would be required, we only show non-personalized ads, additionally we show this as an info.
            // If no consent was required, we hide the button and show no info.
            if(BuildConfig.FLAVOR == BUILD_FLAVOR_HUAWEI) {
                binding.adinfoButtonUserconsent.visibility = View.GONE
                if(!AdManagerHuawei.isPersonalizedAdsAllowed)
                    binding.adinfoHuweiOnlyNonPersonalizedText.visibility = View.VISIBLE
            }
        }
    }
}