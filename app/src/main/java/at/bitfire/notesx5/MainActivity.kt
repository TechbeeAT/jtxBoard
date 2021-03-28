/*
 * Copyright (c) Patrick Lang in collaboration with bitfire web engineering.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.notesx5

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView


const val CONTACT_READ_PERMISSION_CODE = 100   // this is necessary for the app permission, 100 ist just a freely chosen value


/**
 * This main activity is just a container for our fragments,
 * where the real action is.
 */
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Set up the toolbar with the navigation drawer

        val toolbar: Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_main_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        // React on selection in Navigation View
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            // Handle menu item selected

            when (menuItem.itemId) {
                /*
                R.id.nav_about ->
                    startActivity(Intent(activity, AboutActivity::class.java))
                R.id.nav_app_settings ->
                    startActivity(Intent(activity, AppSettingsActivity::class.java))
                R.id.nav_beta_feedback ->
                    if (!UiUtils.launchUri(activity, Uri.parse(BETA_FEEDBACK_URI), Intent.ACTION_SENDTO, false))
                        Toast.makeText(activity, R.string.install_email_client, Toast.LENGTH_LONG).show()

                 */
                R.id.nav_twitter ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_website ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_manual ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_faq ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_forums ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_donate ->
                    //if (BuildConfig.FLAVOR != App.FLAVOR_GOOGLE_PLAY)
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))

                R.id.nav_privacy ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bitfire.at")))
            }

            true
        }
    }



    // this is called when the user accepts a permission
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == CONTACT_READ_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Contacts Read Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Contacts Read Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

}
