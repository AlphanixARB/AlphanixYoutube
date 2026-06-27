package com.alphanix.tube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val TAG = "VIDEO_DEBUG"

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null

    private var lastBackPressTime: Long = 0
    private val DOUBLE_BACK_INTERVAL = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        webView = findViewById(R.id.webView)

        val videoUrl = intent.getStringExtra("videoUrl") ?: "https://m.youtube.com"
        setupWebView()
        webView.loadUrl(videoUrl)

        // 🚀 مدیریت دکمه بازگشت با دیسپچر جدید (جایگزین onBackPressed)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // ۱. اگر تمام‌صفحه است، خارج شو
                    customView != null -> {
                        webView.webChromeClient?.onHideCustomView()
                    }
                    // ۲. اگر تاریخچه WebView دارد، به عقب برو (ویدیوی پیشنهادی قبلی)
                    webView.canGoBack() -> {
                        webView.goBack()
                    }
                    // ۳. در غیر این صورت، دوبار کلیک برای خروج
                    else -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastBackPressTime < DOUBLE_BACK_INTERVAL) {
                            // بستن Activity (اینجا دیگه نیازی به super.onBackPressed نیست)
                            finish()
                        } else {
                            triggerSystemPiP()
                            lastBackPressTime = currentTime
                            Toast.makeText(
                                this@VideoPlayerActivity,
                                "برای خروج، دوباره دکمه بازگشت را بزنید",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newUrl = intent?.getStringExtra("videoUrl")
        if (newUrl != null && newUrl != webView.url) {
            webView.loadUrl(newUrl)
        }
        setIntent(intent)
    }

    private fun setupWebView() {
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        WebView.setWebContentsDebuggingEnabled(true)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            settings.safeBrowsingEnabled = false
        }

        settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.setSupportZoom(false)
        settings.loadsImagesAutomatically = true

        val cssInject = """
            (function() {
                try {
                    if (window.location.href.indexOf('/feed/subscriptions') !== -1) {
                        var oldStyle = document.getElementById('stylus-fast');
                        if (oldStyle) oldStyle.remove();
                    } else {
                        var cssText = ' .ytThumbnailViewModelImage, ytm-thumbnail-cover img, .video-thumbnail-img { filter: blur(2px) !important; } .html5-video-player.ad-showing .video-stream, .ad-created.ad-showing video { filter: brightness(0) !important; background: black !important; } ad-slot-renderer { filter: brightness(0) !important; background: black !important; } ';
                        var style = document.getElementById('stylus-fast');
                        if (!style) {
                            style = document.createElement('style');
                            style.id = 'stylus-fast';
                            style.type = 'text/css';
                            (document.head || document.documentElement).appendChild(style);
                        }
                        style.textContent = cssText;
                    }

                    var videoPlayer = document.querySelector('.html5-video-player');
                    var videoElement = document.querySelector('video');
                    if (videoPlayer && videoElement) {
                        if (videoPlayer.classList.contains('ad-showing')) {
                            if (!videoElement.muted) videoElement.muted = true;
                        } else if (videoPlayer.classList.contains('playing-mode')) {
                            if (videoElement.muted) videoElement.muted = false;
                        }
                    }

                } catch(err) {
                    console.error('INJECT_ERROR: ' + err.message);
                }
            })();
        """.trimIndent()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }
                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                val decorView = window.decorView as FrameLayout
                fullscreenContainer = FrameLayout(this@VideoPlayerActivity)
                fullscreenContainer?.setBackgroundColor(android.graphics.Color.BLACK)
                fullscreenContainer?.addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                ))
                decorView.addView(fullscreenContainer, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                ))
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                fullscreenContainer?.keepScreenOn = true
                customView?.keepScreenOn = true
                webView.keepScreenOn = true
            }

            override fun onHideCustomView() {
                if (customView == null) return
                val decorView = window.decorView as FrameLayout
                decorView.removeView(fullscreenContainer)
                fullscreenContainer?.keepScreenOn = false
                customView?.keepScreenOn = false
                fullscreenContainer = null
                customView = null
                customViewCallback = null
                webView.visibility = View.VISIBLE
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                customViewCallback?.onCustomViewHidden()
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let { Log.d(TAG, "ℹ️ JS_LOG: ${it.message()}") }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url?.contains("youtube.com") == true) {
                    view?.evaluateJavascript(cssInject, null)
                }
            }
        }

        webView.setOnScrollChangeListener { _, _, _, _, _ -> }
    }

    // ✅ دکمه بازگشت: همیشه به PiP برو (بدون توجه به تاریخچه WebView)
//    override fun onBackPressed() {
//        when {
//            customView != null -> {
//                webView.webChromeClient?.onHideCustomView()
//            }
//            else -> {
//                val currentTime = System.currentTimeMillis()
//                if (currentTime - lastBackPressTime < DOUBLE_BACK_INTERVAL) {
//                    // دوبار پشت سر هم → بستن Activity و بازگشت به MainActivity
////                    finish()
//                    super.onBackPressed()
//                } else {
//                    // یکبار → رفتن به PiP
//                    triggerSystemPiP()
//                    lastBackPressTime = currentTime
//                    Toast.makeText(
//                        this,
//                        "برای خروج، دوباره دکمه بازگشت را بزنید",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isInPictureInPictureMode) {
            triggerSystemPiP()
        }
    }

    private fun triggerSystemPiP() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Log.e(TAG, "PiP failed: ${e.message}")
                finish()
            }
        } else {
            finish()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            webView.keepScreenOn = true
            lastBackPressTime = 0
        }
    }
}