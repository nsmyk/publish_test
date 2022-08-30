package com.sandsiv.digi

import android.R
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sandsiv.digi.databinding.ActivitySurveyBinding

const val SURVEY_ID_KEY = "survey_id"
const val LANGUAGE_KEY = "language"
const val PARAMS_KEY = "params"
const val MARGINS_KEY = "margins"
private const val SURVEY_ID_PARAM_NAME = "surveyId"
private const val LANGUAGE_PARAM_NAME = "language"
private const val URL_PARAM_NAME = "apiUrl"
private const val HTML_FILE_NAME = "digi.html"
private const val SCRIPT_URL_PLACEHOLDER = "SCRIPT_URL"
private const val SCRIPT_PARAMS_PLACEHOLDER = "SCRIPT_PARAMS"
private const val JS_CLOSE_EVENT = "window_close"
private const val JS_SHOWN_EVENT = "window_show"
private const val JS_EVENT_HANDLER_NAME = "androidEventHandler"
private const val VIEW_APPEARANCE_DELAY = 100L

class DigiWebViewActivity : AppCompatActivity() {

    private lateinit var vb: ActivitySurveyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(vb.root)

        val margins = intent.getParcelableExtra(MARGINS_KEY) ?: Margins()
        val paramsMap = intent.getSerializableExtra(PARAMS_KEY) as? HashMap<String, Any>
            ?: hashMapOf()
        val surveyId = intent.getStringExtra(SURVEY_ID_KEY).orEmpty()
        val language = intent.getStringExtra(LANGUAGE_KEY).orEmpty()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val apiUrl = prefs.getString(BASE_URL_KEY, null)!!

        prepareHtml(
            paramsMap = paramsMap,
            surveyId = surveyId,
            language = language,
            apiUrl = apiUrl
        )
        configureWebView(margins)

        vb.webView.loadUrl("file:///$filesDir/$HTML_FILE_NAME")
    }

    private fun configureWebView(margins: Margins) {
        with(margins) { vb.webViewContainer.setPadding(end, top, start, bottom) }
        vb.webViewContainer.setOnClickListener { finish() }
        vb.webView.webChromeClient = WebChromeClient()
        vb.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
//            javaScriptCanOpenWindowsAutomatically = true
        }

        vb.webView.webViewClient = WebViewClient()

        val jsEventHandler = object : JsEventHandler {

            @JavascriptInterface
            override fun dispatchEvent(event: String) {
                when(event) {
                    JS_CLOSE_EVENT -> finish()
                    JS_SHOWN_EVENT -> {
                        runOnUiThread {
                            vb.root.postDelayed(
                                {
                                    vb.webView.visibility = View.VISIBLE
                                    vb.webView.animate().alpha(1f)
                                    vb.progress.visibility = View.GONE
                                },
                                VIEW_APPEARANCE_DELAY
                            )
                        }
                    }
                }
            }
        }

        vb.webView.addJavascriptInterface(jsEventHandler, JS_EVENT_HANDLER_NAME)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun prepareHtml(
        surveyId: String,
        language: String,
        apiUrl: String,
        paramsMap: HashMap<String, Any>
    ) {
        val requiredParams = "$SURVEY_ID_PARAM_NAME=$surveyId&$LANGUAGE_PARAM_NAME=$language&" +
                "$URL_PARAM_NAME=$apiUrl"
        val otherParams = paramsMap.entries
            .joinToString(separator = "") { "&${it.key}=${it.value}" }
        val params = requiredParams + otherParams
        val htmlText = StringBuilder()

        assets.open(HTML_FILE_NAME).bufferedReader()
            .use {
                val updatedHtml = it.readText()
                    .replace(SCRIPT_URL_PLACEHOLDER, JS_FILE_NAME)
                    .replace(SCRIPT_PARAMS_PLACEHOLDER, params)
                htmlText.append(updatedHtml)
            }

        openFileOutput(HTML_FILE_NAME, MODE_PRIVATE).use {
            it.write(htmlText.toString().toByteArray())
        }
    }
}

interface JsEventHandler {
    @JavascriptInterface
    fun dispatchEvent(event: String)
}