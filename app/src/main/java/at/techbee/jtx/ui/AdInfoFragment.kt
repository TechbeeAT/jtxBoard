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
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager


class AdInfoFragment : Fragment() {

    private var _binding: FragmentAdinfoBinding? = null
    private val binding get() = _binding!!
    lateinit var application: Application


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        this._binding = FragmentAdinfoBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

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
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}