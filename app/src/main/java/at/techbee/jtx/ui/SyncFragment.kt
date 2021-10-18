package at.techbee.jtx.ui

import android.app.Application
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


class SyncFragment : Fragment() {

    lateinit var binding: FragmentSyncBinding
    lateinit var application: Application
    private lateinit var inflater: LayoutInflater
    private lateinit var dataSource: ICalDatabaseDao


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

        val collections = dataSource.getAllCollections()

        collections.observe(viewLifecycleOwner, { collectionList ->
            var collectionsString = ""
            collectionList.forEach {
                if (it.accountName != "LOCAL")          // only add remote collections
                    collectionsString += it.accountName + " (" + it.displayName + ")\n"
            }


            if(isDAVx5Available() && collectionsString.isNotBlank()) {
                binding.syncPleaseInstall.visibility = View.GONE
                binding.syncPleaseInstallText.visibility = View.GONE
                binding.syncCongratulations.visibility = View.VISIBLE

                if(collectionsString.isNotBlank()) {
                    // DAVx5 is installed and collections were found
                    binding.syncCongratulationsTextNoCollections.visibility = View.GONE
                    binding.syncCongratulationsTextWithCollections.visibility = View.VISIBLE
                    binding.syncCongratulationsSynchedCollections.visibility = View.VISIBLE
                    binding.syncCongratulationsSynchedCollections.text = collectionsString
                } else {
                    // DAVx5 is installed but no remote collections were found
                    binding.syncCongratulationsTextNoCollections.visibility = View.VISIBLE
                    binding.syncCongratulationsTextWithCollections.visibility = View.GONE
                    binding.syncCongratulationsSynchedCollections.visibility = View.GONE
                }

            } else {
                // DAVx5 is not installed, show messages for user to install
                binding.syncPleaseInstall.visibility = View.VISIBLE
                binding.syncPleaseInstallText.visibility = View.VISIBLE

                binding.syncCongratulations.visibility = View.GONE
                binding.syncCongratulationsSynchedCollections.visibility = View.GONE
                binding.syncCongratulationsTextNoCollections.visibility = View.GONE
                binding.syncCongratulationsTextWithCollections.visibility = View.GONE
            }
        })




        return binding.root
    }

    override fun onResume() {

        try {
            val activity = requireActivity() as MainActivity
            activity.setToolbarText(getString(R.string.toolbar_text_sync))
        } catch(e: Exception) {
            Log.d("Cast not successful", e.toString())
            //This error will always happen for fragment testing, as the cast to Main Activity cannot be successful
        }

        super.onResume()
    }

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