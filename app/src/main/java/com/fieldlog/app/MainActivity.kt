package com.fieldlog.app

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Single-Activity WebView wrapper around the Fieldlog web app (bundled in assets/).
 *
 * The app is fully self-contained HTML/JS using localStorage, so no internet
 * permission is needed. Two native hooks make the backup feature work like a
 * real app:
 *   - export: the web app calls AndroidBridge.export(...) and we save the JSON
 *     through the Storage Access Framework (user picks where it lands).
 *   - import: the web app's <input type="file"> triggers onShowFileChooser,
 *     which launches the system picker and feeds the file back to the page.
 */
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var pendingExportJson: String? = null

    // Import: returns the chosen file URI(s) to the WebView's file input.
    private val fileChooser = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cb = fileChooserCallback
        fileChooserCallback = null
        cb?.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        )
    }

    // Export: lets the user choose a save location, then writes the JSON there.
    @Suppress("DEPRECATION")
    private val createDoc = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingExportJson
        pendingExportJson = null
        if (uri != null && json != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                toast("Backup saved")
            } catch (e: Exception) {
                toast("Save failed")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true       // app logic
            domStorageEnabled = true       // localStorage persistence
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }

        webView.addJavascriptInterface(Bridge(), "AndroidBridge")
        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = callback
                return try {
                    fileChooser.launch(params?.createIntent())
                    true
                } catch (e: Exception) {
                    fileChooserCallback = null
                    false
                }
            }
        }

        if (savedInstanceState == null) {
            webView.loadUrl("file:///android_asset/index.html")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    /** Exposed to JavaScript as window.AndroidBridge */
    inner class Bridge {
        @JavascriptInterface
        fun export(filename: String, json: String) {
            pendingExportJson = json
            runOnUiThread {
                val safe = if (filename.endsWith(".json")) filename else "$filename.json"
                createDoc.launch(safe)
            }
        }
    }

    private fun toast(message: String) = runOnUiThread {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::webView.isInitialized) webView.saveState(outState)
    }

    @Deprecated("Back handled via WebView history")
    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
