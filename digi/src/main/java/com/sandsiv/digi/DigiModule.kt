package com.sandsiv.digi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.parcelize.Parcelize
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import com.sandsiv.digi.network.NetworkClient
import java.io.File
import java.io.IOException

const val JS_FILE_NAME = "digi_runner.js"
const val DEFAULT_JS_FILE_NAME = "default_digi_runner.js"
const val PREFS_NAME = "digi_prefs"
const val BASE_URL_KEY = "base_url"
const val LOGGER_TAG = "DigiModule"
private const val MODIFIED_DATE_KEY = "last-modified"

object DigiModule {

    private val networkClient = NetworkClient()

    @JvmStatic
    fun init(url: String, context: Context) {
        if (!File(context.filesDir, JS_FILE_NAME).exists()) saveDefaultScript(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val baseUrl = Uri.parse(url).getBaseUrl()
        prefs.edit().putString(BASE_URL_KEY, baseUrl).apply()
        val isFirstLaunch = prefs.getString(MODIFIED_DATE_KEY, null) == null
        if (isFirstLaunch) {
            loadScript(url, context, prefs)
        } else {
            checkScriptRelevance(url, context, prefs)
        }
    }

    @JvmOverloads
    @JvmStatic
    fun show(
        context: Context,
        surveyId: Int,
        language: String,
        params: HashMap<String, Any> = hashMapOf(),
        margins: Margins = Margins(),
    ) {
        val intent = Intent(context, DigiWebViewActivity::class.java).apply {
            putExtra(SURVEY_ID_KEY, surveyId.toString())
            putExtra(LANGUAGE_KEY, language)
            putExtra(PARAMS_KEY, params)
            putExtra(MARGINS_KEY, margins)
        }

        context.startActivity(intent)
    }

    private fun checkScriptRelevance(
        url: String,
        appContext: Context,
        prefs: SharedPreferences,
    ) {
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(
                    LOGGER_TAG,
                    "Problem calling JS script's last modified date {${e.message}}",
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

    private fun loadScript(url: String, appContext: Context, prefs: SharedPreferences) {
        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(LOGGER_TAG, "Problem calling JS script {${e.message}}")
            }

            override fun onResponse(call: Call, response: Response) {
                val lastModifiedDate = response.headers[MODIFIED_DATE_KEY]
                val script = response.body?.string().orEmpty()
                prefs.edit()
                    .putString(MODIFIED_DATE_KEY, lastModifiedDate)
                    .apply()
                appContext.openFileOutput(JS_FILE_NAME, Context.MODE_PRIVATE).use {
                    it.write(script.toByteArray())
                }
            }
        }

        networkClient.get(url, callback)
    }

    private fun saveDefaultScript(context: Context) {
        context.assets.open(DEFAULT_JS_FILE_NAME)
            .bufferedReader()
            .use { reader ->
                context.openFileOutput(JS_FILE_NAME, AppCompatActivity.MODE_PRIVATE)
                    .use { outputStream ->
                        outputStream.write(reader.readText().toByteArray())
                    }
            }
    }
}

@Parcelize
data class Margins(
    val top: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0,
) : Parcelable

fun Uri.getBaseUrl() = "$scheme://$host"