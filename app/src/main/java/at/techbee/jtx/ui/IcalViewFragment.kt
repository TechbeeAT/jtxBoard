/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.content.SharedPreferences
import androidx.fragment.app.Fragment


class IcalViewFragment : Fragment() {


    private var summary2delete: String = ""

    private lateinit var settings: SharedPreferences

/*
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {


        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())

        icalViewViewModel.icalEntity.observe(viewLifecycleOwner) {

            if (it == null) {
                if(summary2delete.isEmpty())
                    Toast.makeText(context, R.string.view_toast_entry_does_not_exist_anymore, Toast.LENGTH_LONG).show()
                //findNavController().navigate(NavigationDirections.actionGlobalIcalListFragment())
                return@observe   // just make sure that nothing else happens
            }
            if(it.ICalCollection?.readonly == false
                && it.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE
                && BillingManager.getInstance().isProPurchased.value == false) {
                //hideEditingOptions()
                val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_remote_entries_blocked, Snackbar.LENGTH_INDEFINITE)
                //snackbar.setAction(R.string.more) {
                    //findNavController().navigate(R.id.action_global_buyProFragment)
                //}
                snackbar.show()
            }

            if(it.property.geoLat != null && it.property.geoLong != null)
                MapManager(requireContext()).addMap(binding.viewLocationMap, it.property.geoLat!!, it.property.geoLong!!, it.property.location)

      }

        return binding.root
    }

 */
}

