/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.ui.about


import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import at.techbee.jtx.BuildConfig
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.*


class AboutViewModel(application: Application) : AndroidViewModel(application) {

    val translatorsPoeditor: MutableState<List<String>> = mutableStateOf(emptyList())
    val translatorsCrowdin: MutableState<List<String>> = mutableStateOf(emptyList())
    val releaseinfos: MutableLiveData<MutableSet<Release>> = MutableLiveData(mutableSetOf())
    private val app = application

    init {
        getTranslatorInfosPoeditor()
        getReleaseInfos()
        getTranslatorInfosCrowdin()
    }

    /**
     * This method queries the translators from the POEditor API and sets the livedata
     */
    private fun getTranslatorInfosPoeditor() {

        val url = "https://api.poeditor.com/v2/contributors/list"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            { response ->
                try {
                    val translators = mutableListOf<String>()
                    Log.d("jsonResponse", response.toString())
                    val result = response.getJSONObject("result")
                    val contributors = result.getJSONArray("contributors")
                    for(i in 0 until contributors.length()) {
                        val name = contributors.getJSONObject(i).getString("name")
                        Log.d("json", "Name = $name")
                        translators.add(name)
                    }
                    translatorsPoeditor.value = translators
                } catch (e: JSONException) {
                    Log.w("Contributors", "Failed to parse JSON response with contributors\n$e")
                }
            },
            { error ->
                   Log.d("jsonResponse", error.toString())
            }) {

            override fun getBody(): ByteArray {
                return "api_token=7f94161134af8f355eb6feced64dcad5&id=500401".toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/x-www-form-urlencoded"
                return params
            }
        }
        Volley.newRequestQueue(app).add(jsonObjectRequest)
    }


    /**
     * This method queries the members of the translation project on POEditor.com
     */
    private fun getReleaseInfos() {

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
                        Log.d("json", "tag_name = ${release.releaseName}, description = ${release.releaseText}")
                        if(!release.prerelease)   // prereleases are excluded
                            releaseinfos.value?.add(release)
                    }
                } catch (e: JSONException) {
                    Log.w("Gitlab", "Failed to parse JSON response with release info\n$e")
                }
            },
            { error ->
                Log.d("jsonResponse", error.toString())
            }) {
        }
        Volley.newRequestQueue(app).add(jsonArrayRequest)
    }


    /**
     * This method queries the translation project members on Crowdin
     */
    private fun getTranslatorInfosCrowdin() {

        val url = "https://api.crowdin.com/api/v2/projects/557223/members"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            { response ->
                Log.d("Crowdin", response.toString())

                try {
                    Log.d("jsonResponse", response.toString())
                    val translators = mutableListOf<String>()
                    val data = response.getJSONArray("data")
                    for(i in 0 until data.length()) {
                        val name = data.getJSONObject(i).getJSONObject("data").getString("username")
                        Log.d("json", "Name = $name")
                        translators.add(name)
                    }
                    translatorsCrowdin.value = translators
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
        Volley.newRequestQueue(app).add(jsonObjectRequest)
    }
}


data class Release(
    var releaseName: String,
    var releaseText: String?,
    var prerelease: Boolean,
    var githubUrl: String
)
