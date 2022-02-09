/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.databinding.FragmentSyncBinding
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import android.content.Intent
import android.net.Uri
import android.view.*
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import at.techbee.jtx.util.SyncUtil


class SyncFragment : Fragment() {

    lateinit var binding: FragmentSyncBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao
    private lateinit var syncViewModel: SyncViewModel
    private var optionsMenu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentSyncBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val viewModelFactory = SyncViewModelFactory(dataSource, application)
        syncViewModel = ViewModelProvider(this, viewModelFactory)[SyncViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = syncViewModel

        setHasOptionsMenu(true)

        // don't show the sync menu if DAVx5 is not installed
        syncViewModel.isDavx5Available.observe(viewLifecycleOwner) {
            if(!it)
                optionsMenu?.findItem(R.id.menu_sync_syncnow)?.isVisible = false
        }

        binding.syncButtonAddAccount.setOnClickListener {
            // open davx5
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(SyncUtil.DAVX5_PACKAGE_NAME,"${SyncUtil.DAVX5_PACKAGE_NAME}.ui.setup.LoginActivity")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.sync_toast_intent_open_davx5_failed, Toast.LENGTH_LONG).show()
                Log.w("SyncFragment", "DAVx5 should be there but opening the Activity failed. \n$e")
            }
        }

        binding.syncButtonPlaystore.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${SyncUtil.DAVX5_PACKAGE_NAME}")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${SyncUtil.DAVX5_PACKAGE_NAME}")))
            }
        }

        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE or ContentResolver.SYNC_OBSERVER_TYPE_PENDING) {
            syncViewModel.showSyncProgressIndicator.postValue(SyncUtil.isJtxSyncRunning())
        }

        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_sync), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }
        syncViewModel.isDavx5Available.postValue(SyncUtil.isDAVx5CompatibleWithJTX(application))
        super.onResume()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync, menu)
        this.optionsMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_sync_syncnow -> SyncUtil.syncAllAccounts(requireContext())
        }
        return super.onOptionsItemSelected(item)
    }

}