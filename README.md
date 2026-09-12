# AI Explorer

通过大模型将自然语言搜索意图转换为搜索引擎查询字符串，一键唤起浏览器搜索。

## 功能

- **自然语言 → 搜索引擎查询**：输入"在 GitHub 上找 Shizuku"，LLM 自动生成 `Shizuku +site:github.com`
- **多行输入**：支持复杂的搜索意图描述
- **应用内搜索**：默认用内置 WebView 打开结果，可选搜索引擎（百度 / Bing / Google / DuckDuckGo / 搜狗 / 360），选择会被记住
- **浏览器搜索**：也可交给默认浏览器或系统选择器
- **模型管理**：设置页中一键获取可用模型列表并选择
- **恢复默认**：设置页一键还原 Base URL / 模型 / 提示词 / 搜索引擎
- **深色模式**：自动跟随系统深浅色
- **侧边栏导航**：左侧菜单键切换主页与设置

## 技术栈

- **Kotlin** + **Jetpack Compose** (Material3)
- **Miuix** — 小米 HyperOS 设计风格的 Compose Multiplatform UI
- **OkHttp-free**: 使用标准 `HttpURLConnection` 调用 OpenAI 兼容 API
- **SharedPreferences** 持久化设置

## 前置依赖

只需 Android SDK。Miuix 与 Kotlin 工具链均从 **Maven Central / Google Maven** 解析，
无需任何本地仓库或手动下载。

- JDK 21（`gradle/gradle-daemon-jvm.properties` 中声明）
- Android SDK，需包含 `compileSdk = 37` 对应的 platform 与 build-tools

## 构建

```bash
./gradlew assembleDebug
```

APK 输出在 `app/build/outputs/apk/debug/`。

## 设置

首次使用请在设置页（左侧菜单 → 设置）中填写：

| 字段 | 说明 | 默认值 |
|------|------|--------|
| Base URL | API 端点地址 | `https://api.deepseek.com/v1` |
| API Key | 你的 API 密钥 | — |
| 模型 | 点击「获取列表」从 API 拉取 | `deepseek-chat` |
| 默认搜索引擎 | 应用内 WebView 搜索使用 | `百度` |
| 提示词 | System Prompt，可自定义模板 | 内置搜索引擎查询模板 |
