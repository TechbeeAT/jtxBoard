/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import at.techbee.jtx.NavigationDirections
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalCollection.Factory.LOCAL_ACCOUNT_TYPE
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import at.techbee.jtx.databinding.FragmentIcalViewBinding
import at.techbee.jtx.flavored.AdManager
import at.techbee.jtx.flavored.BillingManager
import at.techbee.jtx.flavored.MapManager
import at.techbee.jtx.util.SyncUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin


class IcalViewFragment : Fragment() {

    private var _binding: FragmentIcalViewBinding? = null
    val binding get() = _binding!!

    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    lateinit var icalViewViewModel: IcalViewViewModel
    private var optionsMenu: Menu? = null

    private var summary2delete: String = ""

    private lateinit var settings: SharedPreferences


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this._binding = FragmentIcalViewBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val arguments = IcalViewFragmentArgs.fromBundle((requireArguments()))

        val markwon = Markwon.builder(requireContext())
            .usePlugin(StrikethroughPlugin.create())
            .build()

        settings = PreferenceManager.getDefaultSharedPreferences(requireContext())


        // set up view model
        val model: IcalViewViewModel by viewModels { IcalViewViewModelFactory(application, arguments.item2show) }
        icalViewViewModel = model
        binding.model = icalViewViewModel
        binding.lifecycleOwner = viewLifecycleOwner



        // set up observers
        icalViewViewModel.entryToEdit.observe(viewLifecycleOwner) {
            if (it != null) {
                icalViewViewModel.entryToEdit.value = null

                // if the item is an instance of a recurring entry, make sure that the user is aware of this
                val originalId =
                    icalViewViewModel.icalEntity.value?.property?.recurOriginalIcalObjectId
                if (originalId != null && icalViewViewModel.icalEntity.value?.property?.isRecurLinkedInstance == true) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.view_recurrence_note_to_original_dialog_header))
                        .setMessage(getString(R.string.view_recurrence_note_to_original))
                        .setPositiveButton("Continue") { _, _ ->
                            icalViewViewModel.icalEntity.value?.let { entity ->
                                this.findNavController().navigate(
                                    IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(
                                        entity
                                    )
                                )

                            }
                        }
                        .setNegativeButton("Go to Original") { _, _ ->
                            this.findNavController().navigate(
                                IcalViewFragmentDirections.actionIcalViewFragmentSelf()
                                    .setItem2show(originalId)
                            )
                        }
                        .show()
                } else {
                    this.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentToIcalEditFragment(it)
                    )
                }
            }
        }


        icalViewViewModel.icalEntity.observe(viewLifecycleOwner) {

            if (it == null) {
                if(summary2delete.isEmpty())
                    Toast.makeText(context, R.string.view_toast_entry_does_not_exist_anymore, Toast.LENGTH_LONG).show()
                findNavController().navigate(NavigationDirections.actionGlobalIcalListFragment())
                return@observe   // just make sure that nothing else happens
            }
            if(it.ICalCollection?.readonly == false
                && it.ICalCollection?.accountType != LOCAL_ACCOUNT_TYPE
                && BillingManager.getInstance()?.isProPurchased?.value == false) {
                //hideEditingOptions()
                val snackbar = Snackbar.make(requireView(), R.string.buypro_snackbar_remote_entries_blocked, Snackbar.LENGTH_INDEFINITE)
                //snackbar.setAction(R.string.more) {
                    //findNavController().navigate(R.id.action_global_buyProFragment)
                //}
                snackbar.show()
            }

            if (!SyncUtil.isDAVx5CompatibleWithJTX(application) || it.ICalCollection?.accountType == LOCAL_ACCOUNT_TYPE)
                optionsMenu?.findItem(R.id.menu_view_syncnow)?.isVisible = false


            // setting the description with Markdown
            it.property.description?.let { desc ->
                val descMarkwon = markwon.toMarkdown(desc)
                binding.viewDescription.text = descMarkwon
            }

            if(it.property.geoLat != null && it.property.geoLong != null)
                MapManager(requireContext()).addMap(binding.viewLocationMap, it.property.geoLat!!, it.property.geoLong!!, it.property.location)


            it.property.recurOriginalIcalObjectId?.let { origId ->
                binding.viewRecurrenceGotooriginalButton.setOnClickListener { view ->
                    view.findNavController().navigate(
                        IcalViewFragmentDirections.actionIcalViewFragmentSelf().setItem2show(origId)
                    )
                }
            }
      }

        // show ads only for AdFlavors and if the subscription was not purchased (gplay flavor only)
        if(AdManager.getInstance()?.isAdFlavor() == true && BillingManager.getInstance()?.isProPurchased?.value == false)
            AdManager.getInstance()?.addAdViewToContainerViewFragment(binding.viewAdContainer, requireContext(), AdManager.getInstance()?.unitIdBannerView)
        else
            binding.viewAdContainer.visibility = View.GONE

        return binding.root
    }
}

