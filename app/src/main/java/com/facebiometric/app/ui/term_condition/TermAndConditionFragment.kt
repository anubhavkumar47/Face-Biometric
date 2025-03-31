package com.facebiometric.app.ui.term_condition

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.facebiometric.app.R


class TermAndConditionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_term_and_condition, container, false)

        val webView: WebView = view.findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true  // Enable JavaScript
        webSettings.domStorageEnabled = true  // Enable local storage

        webView.loadUrl("https://facebiometric.com/legel.html#terms-of-service") // Replace with your website URL
        return view
    }


}