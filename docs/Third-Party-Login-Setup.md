# 第三方登录配置指南

本文档说明如何配置 Google 和微信登录功能。

---

## 📋 准备工作

### 1. Google OAuth 2.0 配置

#### 步骤 1: 创建 Google Cloud 项目
1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建新项目或选择现有项目

#### 步骤 2: 启用 Google+ API
1. 在 API 库中搜索 "Google Sign-In"
2. 启用 API

#### 步骤 3: 创建 OAuth 2.0 凭证
1. 前往 "API 和服务" → "凭证"
2. 点击 "创建凭证" → "OAuth 客户端 ID"
3. 选择应用类型：**Android**
4. 填写信息:
   - **应用名称**: 修心
   - **SHA-1 证书指纹**: (见下方获取方法)
   - **包名**: `com.xiuxin.app`

#### 步骤 4: 获取 SHA-1 指纹
```bash
cd /home/admin/Code/xiuxin-app/android
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### 步骤 5: 配置 Web 客户端 ID (可选，用于后端验证)
1. 创建另一个 OAuth 2.0 凭证
2. 选择应用类型：**Web 应用**
3. 复制 **Web 客户端 ID**

#### 步骤 6: 填写配置
编辑 `ThirdPartyAuth.java`:
```java
private static final String GOOGLE_CLIENT_ID = "YOUR_CLIENT_ID.apps.googleusercontent.com";
private static final String GOOGLE_WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";
```

---

### 2. 微信开放平台配置

#### 步骤 1: 注册微信开放平台账号
1. 访问 [微信开放平台](https://open.weixin.qq.com/)
2. 注册开发者账号 (需要营业执照)

#### 步骤 2: 创建移动应用
1. 前往 "管理中心" → "移动应用"
2. 点击 "创建移动应用"
3. 填写应用信息:
   - **应用名称**: 修心
   - **应用图标**: 上传 logo
   - **包名**: `com.xiuxin.app`
   - **应用签名**: (见下方获取方法)

#### 步骤 3: 获取应用签名
使用微信提供的签名生成工具，或运行:
```bash
cd /home/admin/Code/xiuxin-app/android
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android | grep SHA1
```

将 SHA1 转换为微信需要的格式（去掉冒号，小写）。

#### 步骤 4: 等待审核
提交后等待微信审核（通常 1-3 个工作日）

#### 步骤 5: 获取 AppID 和 AppSecret
审核通过后，在应用详情页面查看:
- **AppID**: `wx1234567890abcdef`
- **AppSecret**: `abcdef1234567890...`

#### 步骤 6: 填写配置
编辑 `ThirdPartyAuth.java`:
```java
private static final String WECHAT_APP_ID = "wx1234567890abcdef";
private static final String WECHAT_APP_SECRET = "abcdef1234567890...";
```

---

## 🔧 代码集成

### 1. 添加依赖

已在 `build.gradle` 中添加:
```gradle
// Google Sign-In
implementation 'com.google.android.gms:play-services-auth:20.7.0'

// WeChat SDK (需要时取消注释)
// implementation 'com.tencent.mm.opensdk:wechat-sdk-android:6.8.0'
```

### 2. AndroidManifest 配置

确保 `AndroidManifest.xml` 包含:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. 微信回调 Activity

在 `AndroidManifest.xml` 添加微信回调:
```xml
<activity
    android:name=".wxapi.WXEntryActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```

创建 `wxapi/WXEntryActivity.java`:
```java
package com.xiuxin.app.wxapi;

import android.app.Activity;
import android.os.Bundle;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 处理微信回调
    }
    
    @Override
    public void onReq(BaseReq req) {}
    
    @Override
    public void onResp(BaseResp resp) {
        // 处理登录响应
        finish();
    }
}
```

---

## 🧪 测试

### Google 登录测试
1. 确保配置了正确的 SHA-1 和包名
2. 运行应用，点击 "使用 Google 账号登录"
3. 选择 Google 账号
4. 成功登录后会显示欢迎信息

### 微信登录测试
1. 确保手机上已安装微信
2. 点击 "使用微信登录"
3. 跳转到微信授权页面
4. 同意授权后返回应用

---

## 📝 注意事项

1. **SHA-1 指纹**: 调试版本和发布版本的签名不同，需要分别配置
2. **微信审核**: 微信登录功能需要应用审核通过才能使用
3. **后端验证**: 生产环境应该在后端验证 ID Token，不要仅在客户端验证
4. **隐私政策**: 确保隐私政策说明第三方登录的数据使用

---

## 🔗 相关资源

- [Google Sign-In 文档](https://developers.google.com/identity/sign-in/android)
- [微信开放平台文档](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Access_Guide/Android.html)
- [OAuth 2.0 规范](https://oauth.net/2/)

---

**配置完成后，重新编译 APK 即可使用第三方登录功能！**
