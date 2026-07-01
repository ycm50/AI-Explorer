package com.aiexplorer.data

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences 封装，管理 LLM 设置项。
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_explorer_settings", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var model: String
        get() = prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var promptTemplate: String
        get() = prefs.getString(KEY_PROMPT, DEFAULT_PROMPT) ?: DEFAULT_PROMPT
        set(value) = prefs.edit().putString(KEY_PROMPT, value).apply()

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL = "model"
        private const val KEY_PROMPT = "prompt_template"

        const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1"
        const val DEFAULT_MODEL = "deepseek-chat"

        val DEFAULT_PROMPT = """
You are a search query generator. Given the user's search intent, output a ready-to-search query string.

STRICT RULES (follow exactly):
1. Output ONLY the query string — no explanations, no markdown, no quotes, no extra text
2. KEYWORDS MUST COME FIRST, ALWAYS — the query MUST start with the core search keywords
3. ALL operators (+site:, +filetype:, +intitle:, etc.) go at the VERY END, after all keywords
4. NEVER put any operator before or between keywords
5. Use + before operators: +site:github.com (not site:github.com alone)
6. The + means "must include" — it applies to the operator, not the keyword

Examples:
- "find Shizuku on GitHub" → "Shizuku +site:github.com"
- "Kotlin coroutines PDF on GitHub" → "Kotlin coroutines +filetype:pdf +site:github.com"
- "sui project PDF from GitHub" → "sui +site:github.com +filetype:pdf"
- "Android app on Google Play" → "Android app +site:play.google.com"

Keyword position is non-negotiable: keywords at front, operators at back.
        """.trimIndent()
    }
}
