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
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.databinding.FragmentSyncBinding
import android.content.pm.PackageManager
import at.techbee.jtx.database.ICalDatabase
import at.techbee.jtx.database.ICalDatabaseDao
import android.content.Intent
import android.net.Uri
import android.widget.Toast


class SyncFragment : Fragment() {

    lateinit var binding: FragmentSyncBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao

    private var collectionsString = ""


    companion object {

        private const val DAVX5_PACKAGE_NAME = "at.bitfire.davdroid"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentSyncBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application
        this.dataSource = ICalDatabase.getInstance(application).iCalDatabaseDao

        val remoteCollections = dataSource.getAllRemoteCollections()

        remoteCollections.observe(viewLifecycleOwner, { collectionList ->

            collectionsString = ""   // reset the string before the observer rebuilds the collections
            collectionList.forEach {
                collectionsString += it.accountName + " (" + it.displayName + ")\n"
            }

            updateUI()

        })

        binding.syncButtonAddAccount.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(
                "at.bitfire.davdroid",
                "at.bitfire.davdroid.ui.setup.LoginActivity"
            )
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.sync_toast_intent_open_davx5_failed, Toast.LENGTH_LONG).show()
                Log.w("SyncFragment", "DAVx5 should be there but opening the Activity failed. \n$e")
            }
        }

        binding.syncButtonPlaystore.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$DAVX5_PACKAGE_NAME")))
            } catch (anfe: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$DAVX5_PACKAGE_NAME")))
            }
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

        updateUI()

        super.onResume()
    }

    /**
     * This function updates the UI and sets the elements to Visible or Gone depending on what is needed:
     * 1. DAVx5 is not installed
     * 2. DAVx5 is installed but no remote collections are synced
     * 3. DAVx5 is installed and remote collections were found
     */
    private fun updateUI() {

        if(isDAVx5Available()) {
            binding.syncPleaseInstall.visibility = View.GONE
            binding.syncPleaseInstallText.visibility = View.GONE
            binding.syncPleaseInstallLink.visibility = View.GONE
            binding.syncButtonPlaystore.visibility = View.GONE
            binding.syncOrDownloadOnGooglePlay.visibility = View.GONE
            binding.syncLinkDavx5.visibility = View.GONE
            binding.syncFurtherInfoDavx5.visibility = View.GONE

            if(collectionsString.isNotBlank()) {
                // DAVx5 is installed and collections were found
                binding.syncCongratulationsTextNoCollections.visibility = View.GONE
                binding.syncCongratulationsSynchedCollections.text = collectionsString

                binding.syncCongratulationsTextWithCollections.visibility = View.VISIBLE
                binding.syncCongratulationsSynchedCollectionsText.visibility = View.VISIBLE
                binding.syncCongratulationsSynchedCollections.visibility = View.VISIBLE
            } else {
                // DAVx5 is installed but no remote collections were found
                binding.syncCongratulationsTextWithCollections.visibility = View.GONE
                binding.syncCongratulationsSynchedCollectionsText.visibility = View.GONE
                binding.syncCongratulationsSynchedCollections.visibility = View.GONE

                binding.syncCongratulationsTextNoCollections.visibility = View.VISIBLE
            }
        } else {
            // DAVx5 is not installed, show messages for user to install

            binding.syncCongratulations.visibility = View.GONE
            binding.syncCongratulationsSynchedCollections.visibility = View.GONE
            binding.syncCongratulationsTextNoCollections.visibility = View.GONE
            binding.syncCongratulationsTextWithCollections.visibility = View.GONE
            binding.syncCongratulationsSynchedCollectionsText.visibility = View.GONE
            binding.syncCongratulationsJtxWebsite.visibility = View.GONE
            binding.syncButtonAddAccount.visibility = View.GONE

        }

    }

    /**
     * This function checks if DAVx5 is installed through the package manager
     * @return true if DAVx5 was found, else false
     */
    private fun isDAVx5Available(): Boolean {
        return try {
            activity?.packageManager?.getApplicationInfo(DAVX5_PACKAGE_NAME, 0)
            //Toast.makeText(activity, "DAVx5 was found :-)", Toast.LENGTH_LONG).show()
            true
        } catch (e: PackageManager.NameNotFoundException) {
            //Toast.makeText(activity, "DAVx5 NOT found :-(", Toast.LENGTH_LONG).show()
            false
        }
    }

}