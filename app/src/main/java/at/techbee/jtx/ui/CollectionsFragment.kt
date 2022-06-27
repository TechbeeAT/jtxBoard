/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.compose.rememberNavController
import at.techbee.jtx.R
import at.techbee.jtx.database.views.CollectionsView
import at.techbee.jtx.ui.compose.screens.CollectionsScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme
import com.google.android.material.snackbar.Snackbar


class CollectionsFragment : Fragment() {


    private val collectionsViewModel: CollectionsViewModel by activityViewModels()

    private var iCalString2Import: String? = null
    private var iCalImportSnackbar: Snackbar? = null


    private var icsFilepickerTargetCollection: CollectionsView? = null
    private val icsFilepickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //processFileAttachment(result.data?.data)
                val ics = result.data?.data ?: return@registerForActivityResult
                val icsString = context?.contentResolver?.openInputStream(ics)?.readBytes()?.decodeToString() ?: return@registerForActivityResult

                icsFilepickerTargetCollection?.let {
                    collectionsViewModel.isProcessing.postValue(true)
                    collectionsViewModel.insertICSFromReader(it.toICalCollection(), icsString)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        val arguments = CollectionsFragmentArgs.fromBundle((requireArguments()))
        arguments.iCalString?.let { iCalString2Import = it }


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
                        CollectionsScreen(
                            collectionsViewModel = collectionsViewModel,
                            navController = rememberNavController()
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {


        if (iCalString2Import?.isNotEmpty() == true) {
            iCalImportSnackbar = Snackbar.make(
                this.requireView(),
                R.string.collections_snackbar_select_collection_for_ics_import,
                Snackbar.LENGTH_INDEFINITE
            )
            iCalImportSnackbar?.show()
        }
    }

}


