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
import androidx.lifecycle.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.*


class AboutViewModel(application: Application) : AndroidViewModel(application) {

    val translators: MutableLiveData<MutableSet<Pair<String, String>>> = MutableLiveData(mutableSetOf())
    val releaseinfos: MutableLiveData<MutableSet<Pair<String, String>>> = MutableLiveData(mutableSetOf())
    private val app = application

    init {
        getTranslators()
        getReleaseInfos()
    }

    /**
     * This method queries the translators from the POEditor API and sets the livedata
     */
    private fun getTranslators() {

        val url = "https://api.poeditor.com/v2/contributors/list"

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, null,
            { response ->
                try {
                    Log.d("jsonResponse", response.toString())
                    val result = response.getJSONObject("result")
                    val contributors = result.getJSONArray("contributors")
                    for(i in 0 until contributors.length()) {
                        val name = contributors.getJSONObject(i).getString("name")
                        Log.d("json", "Name = $name")

                        val languageLocales = mutableListOf<String>()
                        val languages = contributors.getJSONObject(i).getJSONArray("permissions").getJSONObject(0).getJSONArray("languages")
                        for(j in 0 until languages.length()) {
                            val language = languages.getString(j)
                            //Log.d("json", "Language = $language")
                            languageLocales.add(Locale.forLanguageTag(language).displayLanguage)
                            //Log.d("json", "LanguageLocale = ${languageLocale.displayLanguage}")
                        }

                        translators.value?.add(Pair(name, languageLocales.joinToString(separator = ", ")))
                    }
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
     * This method queries the release infos from gitlab and puts it in livedata
     */
    private fun getReleaseInfos() {

        val url = "https://gitlab.com/api/v4/projects/29468606/repository/tags"

        val jsonArrayRequest: JsonArrayRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                try {
                    Log.d("jsonResponse", response.toString())
                    for(i in 0 until response.length()) {
                        val releaseName = response.getJSONObject(i).getJSONObject("release").getString("tag_name")
                        val releaseText = response.getJSONObject(i).getJSONObject("release").getString("description")
                        Log.d("json", "tag_name = $releaseName, description = $releaseText")
                        releaseinfos.value?.add(Pair(releaseName, releaseText))
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
}

