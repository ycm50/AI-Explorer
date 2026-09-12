package com.aiexplorer.ui.utils

import android.content.ActivityNotFoundException
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
     * 构造 Google 搜索的 ACTION_VIEW Intent。
     *
     * 空格编码为 `%20` 而非 `+`：`+` 只在
     * `application/x-www-form-urlencoded` 的查询串里代表空格，各浏览器/搜索引擎
     * 对它的处理并不一致。
     */
    private fun buildSearchIntent(query: String): Intent {
        val encoded = URLEncoder.encode(query, "UTF-8").replace("+", "%20")
        val url = "$GOOGLE_SEARCH_URL$encoded"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * 直接用默认浏览器打开 Google 搜索。
     *
     * @return 是否成功唤起。设备上没有任何可处理 ACTION_VIEW 的应用时返回 false，
     *   而不是抛出 [ActivityNotFoundException] 让应用崩溃。
     */
    fun searchDefault(context: Context, query: String): Boolean = try {
        context.startActivity(buildSearchIntent(query))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    /**
     * 弹出系统选择器，让用户选择用哪个浏览器打开搜索。
     *
     * @return 是否成功唤起。
     */
    fun searchWithChooser(context: Context, query: String): Boolean = try {
        val chooser = Intent.createChooser(buildSearchIntent(query), "选择浏览器").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
