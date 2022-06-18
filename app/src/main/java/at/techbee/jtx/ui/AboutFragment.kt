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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import at.techbee.jtx.MainActivity
import at.techbee.jtx.R
import at.techbee.jtx.ui.compose.screens.AboutScreen
import at.techbee.jtx.ui.theme.JtxBoardTheme


class AboutFragment : Fragment() {

    lateinit var application: Application
    private val model: AboutViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //this.application = requireNotNull(this.activity).application


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
                        AboutScreen(model.translators, model.releaseinfos)
                    }

                }
            }
        }
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarTitle(getString(R.string.toolbar_text_about), null)
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }
        super.onResume()
    }
}