/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
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
import at.techbee.jtx.monetization.AdManager
import at.techbee.jtx.monetization.BillingManager


class AdInfoFragment : Fragment() {

    lateinit var binding: FragmentAdinfoBinding
    lateinit var application: Application


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this.binding = FragmentAdinfoBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        BillingManager.getInstance()?.isAdFreePurchased?.observe(viewLifecycleOwner) {
            updateFragmentContent()
        }

        BillingManager.getInstance()?.adFreePrice?.observe(viewLifecycleOwner) {
            binding.adinfoCardPurchasePrice.text = it ?: ""
        }

        BillingManager.getInstance()?.adFreeOrderId?.observe(viewLifecycleOwner) {
            binding.adinfoCardSuccessOrderNumber.text = getString(R.string.adinfo_adfree_order_id, it)
        }

        BillingManager.getInstance()?.adFreePurchaseDate?.observe(viewLifecycleOwner) {
            binding.adinfoCardSuccessPurchaseDate.text = getString(R.string.adinfo_adfree_purchase_date, it)
        }

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


        if(BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY) {

            if (BillingManager.getInstance()?.isAdFreePurchased?.value == true) {      // change text if item was already bought
                binding.adinfoCardSuccess.setOnClickListener { return@setOnClickListener }   // actually we remove the listener
                binding.adinfoCardPurchase.visibility = View.GONE
                binding.adinfoAdfreeText.visibility = View.GONE
                binding.adinfoButtonUserconsent.visibility = View.GONE
                binding.adinfoText.visibility = View.GONE
                binding.adinfoCardSuccess.visibility = View.VISIBLE
                binding.adinfoThankyouImage.visibility = View.VISIBLE
                binding.adinfoThankyouText.visibility = View.VISIBLE
                /* binding.adinfoButtonManageSubscriptions.visibility = View.VISIBLE
                binding.adinfoButtonManageSubscriptions.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(MANAGE_SUBSCRIPTIONS_LINK)))
                } */
            } else {
                binding.adinfoCardSuccess.visibility = View.GONE
                binding.adinfoThankyouImage.visibility = View.GONE
                binding.adinfoThankyouText.visibility = View.GONE
                /* binding.adinfoButtonManageSubscriptions.visibility = View.GONE
                binding.adinfoCardSubscribe.setOnClickListener {
                    BillingManager.getInstance()?.launchBillingFlow(requireActivity())
                } */
                binding.adinfoCardPurchase.setOnClickListener {
                    BillingManager.getInstance()?.launchBillingFlow(requireActivity())
                }
                binding.adinfoCardPurchase.visibility = View.VISIBLE
                binding.adinfoAdfreeText.visibility = View.VISIBLE
                binding.adinfoText.visibility = View.VISIBLE

                if(AdManager.getInstance()?.isConsentRequired() == true)
                    binding.adinfoButtonUserconsent.visibility = View.VISIBLE
                else
                    binding.adinfoButtonUserconsent.visibility = View.GONE
            }
        } else {
            binding.adinfoCardPurchase.visibility = View.GONE
            binding.adinfoAdfreeText.visibility = View.GONE
            binding.adinfoCardSuccess.visibility = View.GONE
            binding.adinfoThankyouImage.visibility = View.GONE
            binding.adinfoThankyouText.visibility = View.GONE
            //binding.adinfoButtonManageSubscriptions.visibility = View.GONE

            // we don't show the button to reset the user consent for the HUAWEI flavor. Currently there is no user consent implemented for HUAWEI.
            // If a user consent would be required, we only show non-personalized ads, additionally we show this as an info.
            // If no consent was required, we hide the button and show no info.
            if(BuildConfig.FLAVOR == BUILD_FLAVOR_HUAWEI) {
                binding.adinfoButtonUserconsent.visibility = View.GONE
                if(AdManager.getInstance()?.isConsentRequired() == false)
                    binding.adinfoHuweiOnlyNonPersonalizedText.visibility = View.VISIBLE
            }
        }
    }
}