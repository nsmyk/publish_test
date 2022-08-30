package com.sandsiv.digi

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import com.sandsiv.digi.network.NetworkClient
import java.io.IOException

const val JS_FILE_NAME = "digi_runner.js"
const val PREFS_NAME = "digi_prefs"
const val BASE_URL_KEY = "base_url"
private const val MODIFIED_DATE_KEY = "last-modified"

object DigiModule {

    private val networkClient = NetworkClient()

    @JvmStatic
    fun init(url: String, appContext: Application) {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isNewUrl = prefs.getString(BASE_URL_KEY, null) != Uri.parse(url).getBaseUrl()
        val isFirstLaunch = prefs.getString(MODIFIED_DATE_KEY, null) == null
        if (isNewUrl || isFirstLaunch) {
            loadScript(url, appContext, prefs)
            if (isNewUrl) {
                prefs.edit()
                    .remove(BASE_URL_KEY)
                    .remove(MODIFIED_DATE_KEY)
                    .apply()
            }
        } else {
            checkScriptRelevance(url, appContext, prefs)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun show(
        context: Context,
        surveyId: Int,
        language: String,
        params: HashMap<String, Any> = hashMapOf(),
        margins: Margins = Margins()
    ) {
        val intent = Intent(context, DigiWebViewActivity::class.java).apply {
            putExtra(SURVEY_ID_KEY, surveyId.toString())
            putExtra(LANGUAGE_KEY, language)
            putExtra(PARAMS_KEY, params)
            putExtra(MARGINS_KEY, margins)
        }

        context.startActivity(intent)
    }

    private fun checkScriptRelevance(url: String, appContext: Application, prefs: SharedPreferences) {
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(
                    "DigiModule",
                    "Problem calling JS script's last modified date {${e.message}}"
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val lastModifiedDate = response.headers[MODIFIED_DATE_KEY]
                val savedModifiedDate = prefs.getString(MODIFIED_DATE_KEY, "")
                if (lastModifiedDate != savedModifiedDate) {
                    loadScript(url = url, appContext = appContext, prefs = prefs)
                }
            }
        }

        networkClient.head(url, callback)
    }

    private fun loadScript(url: String, appContext: Application, prefs: SharedPreferences) {
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DigiModule", "Problem calling JS script {${e.message}}")
            }

            override fun onResponse(call: Call, response: Response) {
                val lastModifiedDate = response.headers[MODIFIED_DATE_KEY]
                val script = response.body?.string().orEmpty()
                val baseUrl = Uri.parse(url).getBaseUrl()
                prefs.edit()
                    .putString(BASE_URL_KEY, baseUrl)
                    .putString(MODIFIED_DATE_KEY, lastModifiedDate)
                    .apply()
                appContext.openFileOutput(JS_FILE_NAME, Context.MODE_PRIVATE).use {
                    it.write(script.toByteArray())
                }
            }
        }

        networkClient.get(url, callback)
    }
}

@Parcelize
data class Margins(
    val top: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0
) : Parcelable

fun Uri.getBaseUrl() = "$scheme://$host"