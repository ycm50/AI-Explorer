# AI Explorer

通过大模型将自然语言搜索意图转换为搜索引擎查询字符串，一键唤起浏览器搜索。

## 功能

- **自然语言 → 搜索引擎查询**：输入"在 GitHub 上找 Shizuku"，LLM 自动生成 `Shizuku +site:github.com`
- **多行输入**：支持复杂的搜索意图描述
- **浏览器搜索**：默认浏览器搜索 / 系统选择器选择浏览器
- **模型管理**：设置页中一键获取可用模型列表并选择
- **侧边栏导航**：左侧菜单键切换主页与设置

## 技术栈

- **Kotlin** + **Jetpack Compose** (Material3)
- **Miuix** — 小米 HyperOS 设计风格的 Compose Multiplatform UI
- **OkHttp-free**: 使用标准 `HttpURLConnection` 调用 OpenAI 兼容 API
- **SharedPreferences** 持久化设置

## 前置依赖

项目依赖 `kit/` 目录下的本地 Kotlin Compiler 和 Miuix 运行时。`kit/` 已包含在仓库中，无需额外下载。

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
| 提示词 | System Prompt，可自定义模板 | 内置搜索引擎查询模板 |
