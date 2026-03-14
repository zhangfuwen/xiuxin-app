# Android 集成文档 - Blessings API

## 📱 概述

修心 Android 应用已成功集成 Blessings API，实现禅语的在线发布、点赞、评论功能。

## 🏗️ 架构

```
app/
├── model/
│   ├── Blessing.java          # 禅语数据模型
│   └── Comment.java           # 评论数据模型
├── api/
│   └── BlessingsApiClient.java  # API 客户端（单例模式）
├── dialog/
│   └── PublishBlessingDialog.java  # 发布对话框
├── fragment/
│   └── BlessingFragment.java  # 禅语列表 Fragment（已更新）
└── adapter/
    └── BlessingAdapter.java   # RecyclerView 适配器（已更新）
```

## 🔧 API 客户端使用

### 获取实例

```java
BlessingsApiClient apiClient = BlessingsApiClient.getInstance();
```

### 设置当前用户

```java
apiClient.setCurrentUser("user-123", "张三");
```

### 获取禅语列表

```java
apiClient.getBlessings("禅宗", 20, new BlessingsApiClient.ApiCallback<List<Blessing>>() {
    @Override
    public void onSuccess(List<Blessing> blessings) {
        // 处理成功
        for (Blessing blessing : blessings) {
            Log.d("Blessing", blessing.text);
        }
    }

    @Override
    public void onError(String error) {
        // 处理错误
        Toast.makeText(context, "加载失败：" + error, Toast.LENGTH_SHORT).show();
    }
});
```

### 发布新禅语

```java
apiClient.publishBlessing(
    "应无所住而生其心。",  // 禅语内容
    "《金刚经》",          // 出处
    "今日练习：不执着...",  // 练习
    "禅宗",               // 分类
    new BlessingsApiClient.ApiCallback<Blessing>() {
        @Override
        public void onSuccess(Blessing blessing) {
            Toast.makeText(context, "发布成功！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String error) {
            Toast.makeText(context, "发布失败：" + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

### 点赞功能

```java
apiClient.toggleLike(blessingId, 
    new BlessingsApiClient.ApiCallback<BlessingsApiClient.InteractionResult>() {
        @Override
        public void onSuccess(BlessingsApiClient.InteractionResult result) {
            // result.active: 是否已点赞
            // result.count: 新的点赞数
            Log.d("Like", "active=" + result.active + ", count=" + result.count);
        }

        @Override
        public void onError(String error) {
            Toast.makeText(context, "点赞失败：" + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

### 获取评论

```java
apiClient.getComments(blessingId, 
    new BlessingsApiClient.ApiCallback<List<Comment>>() {
        @Override
        public void onSuccess(List<Comment> comments) {
            for (Comment comment : comments) {
                Log.d("Comment", comment.userName + ": " + comment.content);
            }
        }

        @Override
        public void onError(String error) {
            Toast.makeText(context, "获取评论失败：" + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

### 添加评论

```java
apiClient.addComment(blessingId, "这是一条测试评论",
    new BlessingsApiClient.ApiCallback<Comment>() {
        @Override
        public void onSuccess(Comment comment) {
            Toast.makeText(context, "评论成功！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String error) {
            Toast.makeText(context, "评论失败：" + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

## 🎨 UI 组件使用

### 发布对话框

```java
PublishBlessingDialog dialog = new PublishBlessingDialog(context);
dialog.setOnPublishListener(new PublishBlessingDialog.OnPublishListener() {
    @Override
    public void onPublishSuccess(Blessing blessing) {
        // 刷新列表
        loadBlessings();
    }

    @Override
    public void onPublishError(String error) {
        // 错误已在对话框中处理
    }
});
dialog.show();
```

### BlessingAdapter 使用

```java
// 从 API 数据创建列表
List<Blessing> apiBlessings = ...;
List<BlessingAdapter.BlessingItem> items = new ArrayList<>();
for (Blessing blessing : apiBlessings) {
    items.add(BlessingAdapter.BlessingItem.fromApiModel(blessing));
}
adapter.setBlessings(items);
```

## 📡 网络配置

### AndroidManifest.xml

已配置以下权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<application
    android:usesCleartextTraffic="true"  <!-- 允许 HTTP -->
    ...>
```

### API 端点

- **基础 URL**: `http://bot.xjbcode.fun/api/blessings`
- **协议**: HTTP/1.1
- **格式**: JSON
- **超时**: 10 秒

## 🔄 数据流

```
用户操作 → Fragment → API Client → HTTP 请求 → 服务器
                                     ↓
用户界面 ← Adapter ← 数据模型 ← JSON 解析 ← HTTP 响应
```

## 🛡️ 错误处理

API Client 已内置错误处理：

1. **网络错误**: 返回错误消息到回调
2. **HTTP 错误**: 抛出异常并捕获
3. **JSON 解析错误**: 返回 null 或空对象
4. **降级策略**: API 失败时显示本地数据

## 📊 性能优化

1. **异步请求**: 所有 API 调用在后台线程执行
2. **主线程回调**: 结果自动切换到主线程
3. **单例模式**: API Client 全局唯一实例
4. **连接复用**: HttpURLConnection 自动管理连接

## 🧪 测试

### 手动测试清单

- [ ] 加载禅语列表（全部分类）
- [ ] 按分类筛选
- [ ] 发布新禅语
- [ ] 点赞/取消点赞
- [ ] 收藏/取消收藏
- [ ] 查看统计
- [ ] 网络错误处理
- [ ] 离线模式（降级到本地数据）

### 测试命令

```bash
# 构建 Debug 版本
cd android
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk

# 查看日志
adb logcat | grep BlessingsApiClient
```

## 🚀 发布

### 版本号更新

修改 `app/build.gradle`:

```gradle
defaultConfig {
    versionCode 6        // 递增
    versionName "1.5.0"  // 新功能版本
}
```

### 构建 Release

```bash
./gradlew assembleRelease
```

## 📝 注意事项

1. **用户系统**: 当前使用临时用户 ID，后续需集成完整认证
2. **网络安全**: 生产环境应使用 HTTPS
3. **数据缓存**: 可添加本地缓存提升性能
4. **图片支持**: 当前仅文本，后续可支持图片分享
5. **推送通知**: 可添加新评论/点赞通知

## 🔗 相关文档

- [后端 API 文档](../../../molt_server/BLESSINGS_API.md)
- [Web 测试页面](http://bot.xjbcode.fun/blessings)
- [API 端点](http://bot.xjbcode.fun:8081/api/blessings)

## 📅 更新日志

### v1.5.0 (2026-03-14)
- ✅ 集成 Blessings API
- ✅ 在线禅语列表
- ✅ 发布功能
- ✅ 点赞/收藏功能
- ✅ 分类筛选
- ✅ 降级策略（离线模式）

### v1.4.0 (2026-03-13)
- 基础版本
- 本地禅语数据
- 静态列表
