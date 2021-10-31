package at.techbee.jtx.ui

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.AdManager
import at.techbee.jtx.BillingManager
import at.techbee.jtx.databinding.FragmentAdinfoBinding
import com.google.android.ump.ConsentInformation


class AdInfoFragment : Fragment() {

    lateinit var binding: FragmentAdinfoBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentAdinfoBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application


        // TODO: currently the adinfoButtonUserconsent is set to invisible because the GDPR message is not shown on click, this needs further investigation
        if (AdManager.consentInformation?.consentStatus == ConsentInformation.ConsentStatus.NOT_REQUIRED || AdManager.consentInformation?.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {
            // the user is in a country, where the consent is not required or the initial user consent is still unknown (it must be chosen before the first ad if necessary!). Don't show the button.
            binding.adinfoButtonUserconsent.visibility = View.GONE
        } else {
            // the user is in a country where the consent is required, consent status is obtained or required
            binding.adinfoButtonUserconsent.setOnClickListener {
                AdManager.resetUserConsent(requireActivity(), requireContext())
            }
        }

        if(BillingManager.isAdfreePurchased()) {      // change text if item was already bought
            binding.adinfoCardPurchaseHeader.text = getText(R.string.adinfo_adfree_purchase_header_thankyou)
            binding.adinfoCardPurchaseDescription.text = getText(R.string.adinfo_adfree_purchase_description_thankyou)
            binding.adinfoCardPurchasePrice.visibility = View.GONE
        } else {
            BillingManager.adfreeSkuDetails?.price?.let { binding.adinfoCardPurchasePrice.text = it }
        }

        if(BillingManager.isSubscriptionPurchased()) {      // change text if item was already bought
            binding.adinfoCardSubscribeHeader.text = getText(R.string.adinfo_adfree_subscribe_header_thankyou)
            binding.adinfoCardSubscribeDescription.text = getText(R.string.adinfo_adfree_subscribe_description_thankyou)
            binding.adinfoCardSubscribePrice.visibility = View.GONE
        } else {
            BillingManager.adfreeSubSkuDetails?.price?.let {   binding.adinfoCardSubscribePrice.text = it      }
        }


        binding.adinfoCardPurchase.setOnClickListener {
            BillingManager.launchBillingFlow(requireActivity(), BillingManager.adfreeSkuDetails)
        }


        binding.adinfoCardSubscribe.setOnClickListener {
            BillingManager.launchBillingFlow(requireActivity(), BillingManager.adfreeSubSkuDetails)
        }


        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarText(getString(R.string.toolbar_text_adinfo))
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }

        super.onResume()
    }
}