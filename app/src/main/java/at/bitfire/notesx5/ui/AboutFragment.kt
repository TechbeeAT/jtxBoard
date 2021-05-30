package at.bitfire.notesx5.ui

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import at.bitfire.notesx5.BuildConfig
import at.bitfire.notesx5.R
import at.bitfire.notesx5.databinding.FragmentAboutBinding
import at.bitfire.notesx5.databinding.FragmentAboutNotesx5Binding
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

        val aboutNotesx5Binding = FragmentAboutNotesx5Binding.inflate(inflater, container, false)
        aboutNotesx5Binding.aboutNotesx5AppVersion.text = getString(R.string.about_notesx5_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        aboutNotesx5Binding.aboutNotesx5BuildTime.text = getString(R.string.about_notesx5_build_date, SimpleDateFormat.getDateInstance().format(BuildConfig.buildTime))
        binding.aboutLinearlayoutContainer.addView(aboutNotesx5Binding.root)

        binding.aboutTablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                binding.aboutLinearlayoutContainer.removeAllViews()

                when (tab?.position) {
                    0 -> binding.aboutLinearlayoutContainer.addView(aboutNotesx5Binding.root)
                    1 -> return // TODO: replace with translations
                    2 -> return // TODO: replace with libraries
                    else -> binding.aboutLinearlayoutContainer.addView(aboutNotesx5Binding.root)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {  /* nothing to do */  }
            override fun onTabReselected(tab: TabLayout.Tab?) {  /* nothing to do */  }
        })


        return binding.root
    }
}