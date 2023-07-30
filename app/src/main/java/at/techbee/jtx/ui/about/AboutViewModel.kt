/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about


import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import at.techbee.jtx.BuildConfig
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import org.json.JSONException
import kotlin.collections.set


class AboutViewModel(application: Application) : AndroidViewModel(application) {

    val translatorsCrowdin: SnapshotStateList<String> = mutableStateListOf()
    val contributors: SnapshotStateList<Contributor> = mutableStateListOf()
    val releaseinfos: SnapshotStateList<Release> = SnapshotStateList()
    val libraries = Libs.Builder().withContext(application).build()
    private val _application = application

    init {
        getGitHubReleaseInfos()
        getTranslatorInfosCrowdin()
        getGitHubContributors()
    }

    /**
     * This method queries the Release infos from GitHub
     */
    private fun getGitHubReleaseInfos() {

        val url = "https://api.github.com/repos/TechbeeAT/jtxBoard/releases?per_page=100"

        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                try {
                    Log.d("jsonResponse", response.toString())
                    for(i in 0 until response.length()) {
                        val release = Release(
                            releaseName = response.getJSONObject(i).getString("name"),
                            releaseText = response.getJSONObject(i).getString("body"),
                            prerelease = response.getJSONObject(i).getBoolean("prerelease"),
                            githubUrl = response.getJSONObject(i).getString("html_url")
                        )
                        Log.d("GithubRelease", "tag_name = ${release.releaseName}, description = ${release.releaseText}")
                        if(!release.prerelease && !releaseinfos.contains(release))   // prereleases are excluded
                            releaseinfos.add(release)
                    }
                } catch (e: JSONException) {
                    Log.w("GithubRelease", "Failed to parse JSON response with release info\n$e")
                }
            },
            { error ->
                Log.d("jsonResponse", error.toString())
            }) {
        }
        Volley.newRequestQueue(_application).add(jsonArrayRequest)
    }

    /**
     * This method queries the contributors on GitHub
     */
    private fun getGitHubContributors() {

        val url = "https://api.github.com/repos/TechbeeAT/jtxBoard/collaborators"

        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                try {
                    Log.d("GithubContributor", "response: $response")
                    for(i in 0 until response.length()) {
                        val contributor = Contributor(
                            login = response.getJSONObject(i).getString("login"),
                            url = response.getJSONObject(i).getString("url")?.let { Uri.parse(it) },
                            avatarUrl = response.getJSONObject(i).getString("avatar_url")?.let { Uri.parse(it) }
                        )
                        if(!contributors.contains(contributor))
                            contributors.add(contributor)
                        Log.d("GithubContributor", "login = ${contributor.login}")
                    }
                } catch (e: JSONException) {
                    Log.w("GithubContributor", "Failed to parse JSON response with contributors\n$e")
                }
            },
            { error ->
                Log.d("GithubContributor", "error: $error")
            }) {

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/vnd.github+json"
                headers["Authorization"] = "Bearer ${BuildConfig.GITHUB_CONTRIBUTORS_API_KEY}"
                headers["X-GitHub-Api-Version"] = "2022-11-28"
                return headers
            }
        }
        Volley.newRequestQueue(_application).add(jsonArrayRequest)
    }


    /**
     * This method queries the translation project members on Crowdin
     */
    private fun getTranslatorInfosCrowdin() {

        val url = "https://api.crowdin.com/api/v2/projects/557223/members?limit=100"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                Log.d("Crowdin", response.toString())

                try {
                    Log.d("jsonResponse", response.toString())
                    val data = response.getJSONArray("data")
                    for(i in 0 until data.length()) {
                        val name = data.getJSONObject(i).getJSONObject("data").getString("username")
                        Log.d("json", "Name = $name")
                        if(!translatorsCrowdin.contains(name))
                            translatorsCrowdin.add(name)
                    }
                } catch (e: JSONException) {
                    Log.w("Crowdin", "Failed to parse JSON response with release info\n$e")
                }

            },
            { error ->
                Log.d("jsonResponse", error.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${BuildConfig.CROWDIN_API_KEY}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        Volley.newRequestQueue(_application).add(jsonObjectRequest)
    }
}


data class Release(
    var releaseName: String,
    var releaseText: String?,
    var prerelease: Boolean,
    var githubUrl: String
)

data class Contributor(
    var login: String,
    var url: Uri?,
    var avatarUrl: Uri?
) {
    companion object {
        fun getSample() = Contributor(
            login = "Sample",
            url = Uri.parse("https://github.com/patrickunterwegs"),
            avatarUrl = Uri.parse("")
        )
    }
}
