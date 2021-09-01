package at.bitfire.notesx5.ui

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.bitfire.notesx5.BuildConfig
import at.bitfire.notesx5.MainActivity
import at.bitfire.notesx5.R
import at.bitfire.notesx5.databinding.FragmentAboutBinding
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat

class AboutFragment : Fragment() {

    lateinit var binding: FragmentAboutBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Get a reference to the binding object and inflate the fragment views.
        this.inflater = inflater
        this.binding = FragmentAboutBinding.inflate(inflater, container, false)
        this.application = requireNotNull(this.activity).application

        //val aboutNotesx5Binding = FragmentAboutNotesx5Binding.inflate(inflater, container, false)
        binding.fragmentAboutNotesx5.aboutNotesx5AppVersion.text = getString(R.string.about_notesx5_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        binding.fragmentAboutNotesx5.aboutNotesx5BuildTime.text = getString(R.string.about_notesx5_build_date, SimpleDateFormat.getDateInstance().format(BuildConfig.buildTime))

        binding.aboutTablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {
                    0 -> {
                        binding.fragmentAboutNotesx5.aboutNotesx5Scrollview.visibility = View.VISIBLE
                        binding.fragmentAboutLibrariesContainerView.visibility = View.INVISIBLE
                    }
                    1 -> {
                        binding.fragmentAboutNotesx5.aboutNotesx5Scrollview.visibility = View.INVISIBLE
                        binding.fragmentAboutLibrariesContainerView.visibility = View.INVISIBLE
                    } // TODO: replace with translations
                    2 -> {
                        binding.fragmentAboutNotesx5.aboutNotesx5Scrollview.visibility = View.INVISIBLE
                        binding.fragmentAboutLibrariesContainerView.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.fragmentAboutNotesx5.aboutNotesx5Scrollview.visibility = View.INVISIBLE
                        binding.fragmentAboutLibrariesContainerView.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {  /* nothing to do */  }
            override fun onTabReselected(tab: TabLayout.Tab?) {  /* nothing to do */  }
        })

        return binding.root
    }

    override fun onResume() {

        val activity = requireActivity() as MainActivity
        activity.setToolbarText("About")

        super.onResume()
    }

}