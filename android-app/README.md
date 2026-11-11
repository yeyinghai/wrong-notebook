# 错题本安卓客户端（Capacitor 壳）

APP 本身不含业务代码，启动后直接加载自部署的 Web 端（`capacitor.config.json` 里的 `server.url`）。

- 换服务器地址：改 `capacitor.config.json` 的 `server.url` 和 `allowNavigation`，推送后 Actions 自动重新打包。
- 下载 APK：仓库 Actions → “Build Android APK” → 最新一次运行 → Artifacts 里的 `wrong-notebook-apk`。
- 产物为 debug 签名 APK，可直接安装；手机需允许“安装未知来源应用”。
