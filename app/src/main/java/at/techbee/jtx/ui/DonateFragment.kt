package at.techbee.jtx.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import android.net.Uri
import at.techbee.jtx.databinding.FragmentDonateBinding


class DonateFragment : Fragment() {

    lateinit var binding: FragmentDonateBinding
    private lateinit var inflater: LayoutInflater

    companion object {

        private const val PAYPAL_DONATE_URL = "https://www.paypal.com/donate?hosted_button_id=8BVX7PUVVTCWY"

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentDonateBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_donate), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }

        binding.donateButtonPaypal.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL_DONATE_URL)))
        }

        super.onResume()
    }

}