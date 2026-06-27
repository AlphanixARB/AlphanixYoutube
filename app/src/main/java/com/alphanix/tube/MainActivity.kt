package com.alphanix.tube

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alphanix.tube.R

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val TAG = "YOUTUBE_DEBUG"

    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null
    private var isTransitioningToVideo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_dark)
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            swipeRefreshLayout.isEnabled = (scrollY == 0)
        }

        setupWebViewSettings()
        setupOnBackPressed()
    }

    private fun setupWebViewSettings() {
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

        // 🚀 اسکریپت با رهگیری کلیک روی ویدیوها
        val cssInject = """
            (function() {
                try {
                    if (window.location.href.indexOf('/feed/subscriptions') !== -1) {
                        var oldStyle = document.getElementById('stylus-fast');
                        if (oldStyle) oldStyle.remove();
                    } else {
                        var cssText = ' .ytThumbnailViewModelImage, ytm-thumbnail-cover img, .video-thumbnail-img { filter: blur(2px) !important; } .html5-video-player.ad-showing .video-stream, .ad-created.ad-showing video { filter: brightness(0) !important; background: black !important; } ad-slot-renderer { filter: brightness(0) !important; background: black !important; }';
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

                    // 🚀 رهگیری کلیک روی ویدیوها و ارسال به اندروید
                    function handleVideoClick(e) {
                        var target = e.target.closest('a[href*="/watch"], a[href*="/shorts"]');
                        if (target) {
                            var url = target.href;
                            if (window.androidInterface) {
                                window.androidInterface.onVideoClicked(url);
                            }
                            e.preventDefault();
                            e.stopPropagation();
                            return false;
                        }
                    }
                    document.addEventListener('click', handleVideoClick, true);

                } catch(err) {
                    console.error('INJECT_ERROR: ' + err.message);
                }
            })();
        """.trimIndent()

        // 🚀 افزودن اینترفیس جاوا برای ارتباط با اندروید
        webView.addJavascriptInterface(VideoClickInterface(), "androidInterface")

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
                fullscreenContainer = FrameLayout(this@MainActivity)
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

                updateKeepScreenOn()
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
                    updateKeepScreenOn(url)
                }
                swipeRefreshLayout.isRefreshing = false
            }
        }

        webView.loadUrl("https://m.youtube.com")
    }

    // 🚀 اینترفیس جاوا برای دریافت کلیک روی ویدیوها
    inner class VideoClickInterface {
        @JavascriptInterface
        fun onVideoClicked(url: String) {
            runOnUiThread {
                isTransitioningToVideo = true
                val intent = Intent(this@MainActivity, VideoPlayerActivity::class.java)
                intent.putExtra("videoUrl", url)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
    }

    private fun updateKeepScreenOn(url: String? = webView.url) {
        if (url?.contains("/watch") == true || url?.contains("/shorts") == true) {
            webView.keepScreenOn = true
        } else {
            webView.keepScreenOn = false
        }
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    customView != null -> webView.webChromeClient?.onHideCustomView()
                    webView.canGoBack() -> webView.goBack()
                    else -> goToBackground()
                }
            }
        })
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isTransitioningToVideo) {
            isTransitioningToVideo = false
            return
        }
        triggerSystemPiP()
    }

    override fun onResume() {
        super.onResume()
        isTransitioningToVideo = false
    }

    private fun goToBackground() {
        moveTaskToBack(true)
    }

    private fun triggerSystemPiP() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
                return
            } catch (e: Exception) {
                Log.e(TAG, "PiP failed: ${e.message}")
            }
        }
        goToBackground()
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        swipeRefreshLayout.isEnabled = !isInPictureInPictureMode

        if (!isInPictureInPictureMode) {
            updateKeepScreenOn()
        }
    }
}