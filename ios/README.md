# iOS 版本构建指南

## 方案：WKWebView 封装

将 Web 版本封装为 iOS App，与 Android 版本保持一致。

### 快速开始

1. **打开 Xcode**（Mac 必需）
2. **创建新项目** → App
3. **配置**:
   - Interface: Storyboard
   - Language: Swift
   - Team: None（无开发者账号）

4. **修改 ViewController.swift**:

```swift
import UIKit
import WebKit

class ViewController: UIViewController {
    
    var webView: WKWebView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 创建 WKWebView
        let config = WKWebViewConfiguration()
        webView = WKWebView(frame: view.bounds, configuration: config)
        webView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(webView)
        
        // 加载本地 HTML 或远程 URL
        if let url = URL(string: "http://47.254.68.82") {
            let request = URLRequest(url: url)
            webView.load(request)
        }
    }
}
```

5. **修改 Info.plist** 添加网络权限：

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

6. **构建**:
   - 选择 iPhone 模拟器
   - Cmd + R 运行

### 无开发者账号限制

| 功能 | 支持 |
|------|------|
| 模拟器运行 | ✅ |
| 本地构建 IPA | ✅ |
| 真机安装（7天）| ✅（需手动签名）|
| App Store | ❌ |
| 推送通知 | ❌ |

### GitHub Actions 自动构建

已配置 `.github/workflows/build-ios.yml`，可以：
- 在 macOS runner 上构建
- 生成模拟器可用的 .app 文件
- 下载后在本地模拟器测试

### 下一步

需要完整的 iOS 项目代码吗？我可以创建一个完整的 WKWebView 封装项目。
