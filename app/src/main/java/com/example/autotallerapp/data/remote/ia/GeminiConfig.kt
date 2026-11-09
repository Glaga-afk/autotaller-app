package com.example.autotallerapp.data.remote.ia

import com.example.autotallerapp.BuildConfig

object GeminiConfig {
    const val URL_BASE = "https://generativelanguage.googleapis.com/"

    val claveApi: String get() = BuildConfig.GEMINI_API_KEY
    val modelo: String get() = BuildConfig.GEMINI_MODEL
}