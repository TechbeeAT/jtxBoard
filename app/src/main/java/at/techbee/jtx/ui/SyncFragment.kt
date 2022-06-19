/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui

import android.content.ContentResolver
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.screens.SyncScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme
import at.techbee.jtx.util.SyncUtil


class SyncFragment : Fragment() {

    private val syncViewModel: SyncViewModel by activityViewModels()
    private var optionsMenu: Menu? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        // don't show the sync menu if DAVx5 is not installed
        syncViewModel.isDavx5Available.observe(viewLifecycleOwner) {
            if(!it)
                optionsMenu?.findItem(R.id.menu_sync_syncnow)?.isVisible = false
        }

        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
            syncViewModel.isSyncInProgress.postValue(SyncUtil.isJtxSyncRunning(context))
        }


        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                JtxBoardTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SyncScreen(
                            isDAVx5availableLive = syncViewModel.isDavx5Available,
                            remoteCollectionsLive = syncViewModel.remoteCollections,
                            isSyncInProgress = syncViewModel.isSyncInProgress,
                            goToCollections = { findNavController().navigate(R.id.action_global_collectionsFragment) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_sync), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }
        syncViewModel.isDavx5Available.postValue(SyncUtil.isDAVx5CompatibleWithJTX(requireActivity().application))
        super.onResume()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync, menu)
        this.optionsMenu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_sync_syncnow -> SyncUtil.syncAllAccounts(context)
        }
        return super.onOptionsItemSelected(item)
    }
}