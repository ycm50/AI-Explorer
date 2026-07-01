package com.aiexplorer.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * 浏览器搜索工具。
 * 使用系统 Intent 唤起浏览器，确保兼容所有设备。
 */
object BrowserUtils {

    private const val GOOGLE_SEARCH_URL = "https://www.google.com/search?q="

    /**
     * 直接用默认浏览器打开 Google 搜索。
     */
    fun searchDefault(context: Context, query: String) {
        val url = "$GOOGLE_SEARCH_URL${URLEncoder.encode(query, "UTF-8")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}
