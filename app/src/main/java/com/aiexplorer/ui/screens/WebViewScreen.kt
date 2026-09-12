package com.aiexplorer.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aiexplorer.data.SearchEngine
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 通过 URL 加载一个搜索页面的全屏「应用内浏览器」。
 *
 * 用 [WebView] 承载，提供加载进度、错误提示与返回上一页能力。
 *
 * @param engine 决定用哪个搜索引擎。
 * @param query 已经由 LLM 生成的搜索查询串。
 * @param onClose 关闭当前页面。
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    engine: SearchEngine,
    query: String,
    onClose: () -> Unit,
) {
    val targetUrl = remember(engine, query) { engine.buildUrl(query) }

    // 用一个稳定的容器持有 WebView 引用。
    // 不能直接用 `mutableStateOf`：BackHandler 与「重试」按钮的 lambda 在本行就创建了，
    // 那时 WebView 还不存在，会永久捕获到 null。容器对象本身不变，读到的始终是最新引用。
    val holder = remember { WebViewHolder() }
    var progress by remember { mutableIntStateOf(0) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // 返回键：优先回退网页历史，没有历史可退时才关闭本页面。
    BackHandler(enabled = true) {
        val view = holder.webView
        if (view != null && view.canGoBack()) {
            view.goBack()
        } else {
            onClose()
        }
    }

    // 页面销毁时释放 WebView，避免内存泄漏。
    DisposableEffect(holder) {
        onDispose {
            holder.webView?.apply {
                stopLoading()
                loadUrl("about:blank")
                destroy()
            }
            holder.webView = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.background),
    ) {
        // --- 顶栏 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onClose) {
                Text(
                    text = "← 返回",
                    color = MiuixTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = engine.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiuixTheme.colorScheme.onBackground,
                )
                Text(
                    text = query,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = MiuixTheme.colorScheme.onBackgroundVariant,
                )
            }
        }

        HorizontalDivider(color = MiuixTheme.colorScheme.dividerLine)

        // --- 加载进度 ---
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MiuixTheme.colorScheme.primary,
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }

        // --- 网页内容 ---
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            // 不开放本地文件访问：这里只加载远端搜索页，最小权限。
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?,
                            ) {
                                errorText = null
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                progress = 100
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                // 只有主文档失败才算整页失败，子资源失败忽略。
                                if (request?.isForMainFrame == true) {
                                    progress = 100
                                    errorText = "网页加载失败（${error?.errorCode}）"
                                }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }
                        loadUrl(targetUrl)
                        holder.webView = this
                    }
                },
            )

            // 首次加载时的加载指示
            if (progress == 0 && errorText == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MiuixTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "正在打开 ${engine.displayName}…",
                            color = MiuixTheme.colorScheme.onBackgroundVariant,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            // 加载失败提示
            errorText?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MiuixTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = message,
                            color = MiuixTheme.colorScheme.error,
                            fontSize = 14.sp,
                        )
                        OutlinedButton(
                            onClick = {
                                errorText = null
                                progress = 0
                                holder.webView?.reload()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MiuixTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("重试", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

/** 稳定持有当前 [WebView] 实例，供生命周期回调之外的 lambda 读取。 */
private class WebViewHolder {
    var webView: WebView? = null
}
