package com.aiexplorer.data

import java.net.URLEncoder

/**
 * 一个搜索引擎定义。
 *
 * @param id 持久化用的稳定标识，**不要随意改动**，否则用户已保存的选择会失效。
 * @param displayName 界面上显示的名字。
 * @param urlTemplate 搜索地址模板，`%s` 会被替换为经过 URL 编码的查询串。
 */
data class SearchEngine(
    val id: String,
    val displayName: String,
    val urlTemplate: String,
) {
    /** 生成实际可加载的搜索地址。 */
    fun buildUrl(query: String): String = urlTemplate.replace("%s", encodeQuery(query))

    companion object {
        /** 默认搜索引擎，也是首次使用时的预选项。 */
        const val DEFAULT_ID = "baidu"

        val BUILT_IN: List<SearchEngine> = listOf(
            SearchEngine("baidu", "百度", "https://www.baidu.com/s?wd=%s"),
            SearchEngine("bing", "Bing", "https://www.bing.com/search?q=%s"),
            SearchEngine("google", "Google", "https://www.google.com/search?q=%s"),
            SearchEngine("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s"),
            SearchEngine("sogou", "搜狗", "https://www.sogou.com/web?query=%s"),
            SearchEngine("so360", "360 搜索", "https://www.so.com/s?q=%s"),
        )

        /** 按 id 查找内置引擎，找不到时回退到默认引擎。 */
        fun findById(id: String?): SearchEngine =
            BUILT_IN.firstOrNull { it.id == id }
                ?: BUILT_IN.first { it.id == DEFAULT_ID }

        /**
         * URL 编码查询串。
         *
         * 注意这里把空格编码为 `%20` 而不是 `+`：`+` 只在
         * `application/x-www-form-urlencoded` 的查询串里代表空格，各搜索引擎对它的
         * 处理并不一致，`%20` 是更安全的选择。
         */
        private fun encodeQuery(query: String): String =
            URLEncoder.encode(query, "UTF-8").replace("+", "%20")
    }
}
