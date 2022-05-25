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
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.MainActivity
import at.techbee.jtx.MainActivity.Companion.BUILD_FLAVOR_GOOGLEPLAY
import at.techbee.jtx.R
import at.techbee.jtx.databinding.FragmentBuyproBinding
import at.techbee.jtx.flavored.BillingManager


class BuyProFragment : Fragment() {

    private var _binding: FragmentBuyproBinding? = null
    private val binding get() = _binding!!
    lateinit var application: Application


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this._binding = FragmentBuyproBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        BillingManager.getInstance()?.isProPurchased?.observe(viewLifecycleOwner) {
            updateFragmentContent()
        }

        BillingManager.getInstance()?.proPrice?.observe(viewLifecycleOwner) {
            binding.buyproCardPurchasePrice.text = it ?: ""
        }

        BillingManager.getInstance()?.proOrderId?.observe(viewLifecycleOwner) {
            binding.buyproCardSuccessOrderNumber.text = getString(R.string.buypro_order_id, it)
        }

        BillingManager.getInstance()?.proPurchaseDate?.observe(viewLifecycleOwner) {
            binding.buyproCardSuccessPurchaseDate.text = getString(R.string.buypro_purchase_date, it)
        }

        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_buypro), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }

        updateFragmentContent()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /**
     * Updates the visibilities of all elements in the fragment.
     * This is called on create but also when the observer detects an update
     * in BillingManager.adfreeSubscriptionSkuDetails or BillingManager.adfreeOneTimeSkuDetails
     */
    private fun updateFragmentContent() {

        if(BuildConfig.FLAVOR == BUILD_FLAVOR_GOOGLEPLAY) {

            if (BillingManager.getInstance()?.isProPurchased?.value == true) {      // change text if item was already bought
                binding.buyproCardSuccess.setOnClickListener { return@setOnClickListener }   // actually we remove the listener
                binding.buyproCardPurchase.visibility = View.GONE
                binding.buyproText.visibility = View.GONE
                binding.buyproCardSuccess.visibility = View.VISIBLE
                binding.buyproThankyouImage.visibility = View.VISIBLE
                binding.buyproThankyouText.visibility = View.VISIBLE
            } else {
                binding.buyproCardSuccess.visibility = View.GONE
                binding.buyproThankyouImage.visibility = View.GONE
                binding.buyproThankyouText.visibility = View.GONE
                binding.buyproCardPurchase.setOnClickListener {
                    BillingManager.getInstance()?.launchBillingFlow(requireActivity())
                }
                binding.buyproCardPurchase.visibility = View.VISIBLE
                binding.buyproText.visibility = View.VISIBLE
            }
        } else {
            binding.buyproCardPurchase.visibility = View.GONE
            binding.buyproText.visibility = View.GONE
            binding.buyproCardSuccess.visibility = View.GONE
            binding.buyproThankyouImage.visibility = View.GONE
            binding.buyproThankyouText.visibility = View.GONE
        }
    }
}