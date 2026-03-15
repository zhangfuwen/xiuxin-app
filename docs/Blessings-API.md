# Blessings API 文档

正念/禅语功能的 RESTful API 接口文档

---

## 📡 基本信息

**Base URL**: `http://47.254.68.82/api/blessings`

**Content-Type**: `application/json`

**字符编码**: `UTF-8`

---

## 🔑 通用参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `user_id` | string | 否 | 用户唯一标识，用于返回点赞/收藏状态 |

---

## 📖 API 端点

### 1. 获取禅语列表

**请求**
```http
GET /api/blessings?limit=20&offset=0&category=禅宗&user_id=xxx
```

**查询参数**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `limit` | integer | 20 | 返回数量 (最大 100) |
| `offset` | integer | 0 | 分页偏移量 |
| `category` | string | - | 分类筛选：禅宗/儒家/道家/佛经 |
| `user_id` | string | - | 用户 ID，用于返回交互状态 |

**响应示例**
```json
{
  "success": true,
  "data": [
    {
      "id": 11,
      "user_id": "test-user-1",
      "user_name": "测试用户",
      "text": "测试发布的禅语",
      "source": "测试",
      "practice": "今日练习内容",
      "category": "禅宗",
      "like_count": 1,
      "favorite_count": 0,
      "created_at": "2026-03-14 03:23:52",
      "updated_at": "2026-03-14 03:23:52",
      "is_liked": false,
      "is_favorited": false
    }
  ],
  "pagination": {
    "limit": 20,
    "offset": 0,
    "count": 1
  }
}
```

---

### 2. 获取单条禅语

**请求**
```http
GET /api/blessings/{id}
```

**响应示例**
```json
{
  "success": true,
  "data": {
    "id": 11,
    "user_id": "test-user-1",
    "user_name": "测试用户",
    "text": "测试发布的禅语",
    "source": "测试",
    "practice": "今日练习",
    "category": "禅宗",
    "like_count": 1,
    "favorite_count": 0,
    "created_at": "2026-03-14 03:23:52"
  }
}
```

---

### 3. 发布禅语

**请求**
```http
POST /api/blessings
Content-Type: application/json

{
  "user_id": "android_user_123",
  "user_name": "修心用户",
  "text": "应无所住而生其心。",
  "source": "《金刚经》",
  "practice": "不执着于任何事物，心才能自由",
  "category": "禅宗"
}
```

**必填字段**
- `user_id` - 用户 ID
- `user_name` - 用户名
- `text` - 禅语内容

**可选字段**
- `source` - 出处
- `practice` - 今日练习
- `category` - 分类 (默认：禅宗)

**响应示例**
```json
{
  "success": true,
  "data": {
    "id": 12,
    "user_id": "android_user_123",
    "user_name": "修心用户",
    "text": "应无所住而生其心。",
    "source": "《金刚经》",
    "practice": "不执着于任何事物，心才能自由",
    "category": "禅宗",
    "like_count": 0,
    "favorite_count": 0,
    "created_at": "2026-03-15 13:40:00"
  }
}
```

---

### 4. 更新禅语

**请求**
```http
PUT /api/blessings/{id}
Content-Type: application/json

{
  "user_id": "android_user_123",
  "text": "更新后的内容",
  "source": "更新后的出处"
}
```

**说明**: 只能更新自己发布的禅语

---

### 5. 删除禅语

**请求**
```http
DELETE /api/blessings/{id}
Content-Type: application/json

{
  "user_id": "android_user_123"
}
```

**说明**: 软删除，只能删除自己发布的禅语

---

### 6. 点赞/取消点赞

**请求**
```http
POST /api/blessings/{id}/like
Content-Type: application/json

{
  "user_id": "android_user_123"
}
```

**响应示例**
```json
{
  "success": true,
  "data": {
    "active": true,
    "count": 5
  }
}
```

**说明**: 
- `active: true` 表示已点赞
- `active: false` 表示已取消点赞
- `count` 是当前点赞总数

---

### 7. 收藏/取消收藏

**请求**
```http
POST /api/blessings/{id}/favorite
Content-Type: application/json

{
  "user_id": "android_user_123"
}
```

**响应示例**
```json
{
  "success": true,
  "data": {
    "active": true,
    "count": 3
  }
}
```

---

### 8. 获取评论列表

**请求**
```http
GET /api/blessings/{id}/comments?limit=50&offset=0
```

**响应示例**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "blessing_id": 11,
      "user_id": "user_456",
      "user_name": "评论用户",
      "content": "很好的禅语！",
      "like_count": 0,
      "created_at": "2026-03-15 10:00:00"
    }
  ]
}
```

---

### 9. 添加评论

**请求**
```http
POST /api/blessings/{id}/comments
Content-Type: application/json

{
  "user_id": "android_user_123",
  "user_name": "修心用户",
  "content": "很好的禅语！"
}
```

**响应示例**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "blessing_id": 11,
    "user_id": "android_user_123",
    "user_name": "修心用户",
    "content": "很好的禅语！",
    "like_count": 0,
    "created_at": "2026-03-15 13:45:00"
  }
}
```

---

### 10. 获取统计数据

**请求**
```http
GET /api/blessings/stats
```

**响应示例**
```json
{
  "success": true,
  "data": {
    "total_blessings": 100,
    "total_comments": 250,
    "by_category": {
      "禅宗": 60,
      "儒家": 20,
      "道家": 15,
      "佛经": 5
    },
    "top_liked": [
      {
        "id": 1,
        "text": "心本无生因境有...",
        "like_count": 15
      }
    ]
  }
}
```

---

## ❌ 错误响应

**400 Bad Request**
```json
{
  "success": false,
  "error": "Missing required field: text"
}
```

**404 Not Found**
```json
{
  "success": false,
  "error": "Blessing not found"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "error": "Error listing blessings: [details]"
}
```

---

## 📝 使用示例

### cURL 示例

**获取列表**
```bash
curl "http://47.254.68.82/api/blessings?limit=10&category=禅宗"
```

**发布禅语**
```bash
curl -X POST "http://47.254.68.82/api/blessings" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "test_user",
    "user_name": "测试用户",
    "text": "菩提本无树，明镜亦非台",
    "source": "六祖慧能",
    "category": "禅宗"
  }'
```

**点赞**
```bash
curl -X POST "http://47.254.68.82/api/blessings/11/like" \
  -H "Content-Type: application/json" \
  -d '{"user_id": "test_user"}'
```

### JavaScript 示例

```javascript
// 获取禅语列表
async function getBlessings(category = null) {
  let url = 'http://47.254.68.82/api/blessings?limit=20';
  if (category) {
    url += `&category=${encodeURIComponent(category)}`;
  }
  
  const response = await fetch(url);
  const data = await response.json();
  return data.data;
}

// 发布禅语
async function publishBlessing(text, source, category) {
  const response = await fetch('http://47.254.68.82/api/blessings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      user_id: 'user_' + Date.now(),
      user_name: '修心用户',
      text: text,
      source: source,
      category: category
    })
  });
  return await response.json();
}

// 点赞
async function toggleLike(blessingId, userId) {
  const response = await fetch(
    `http://47.254.68.82/api/blessings/${blessingId}/like`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ user_id: userId })
    }
  );
  return await response.json();
}
```

---

## 📊 数据模型

### Blessing (禅语)
```typescript
interface Blessing {
  id: number;
  user_id: string;
  user_name: string;
  text: string;
  source?: string;
  practice?: string;
  category: string; // 禅宗/儒家/道家/佛经
  like_count: number;
  favorite_count: number;
  created_at: string;
  updated_at: string;
  is_liked?: boolean;      // 仅当提供 user_id 时返回
  is_favorited?: boolean;  // 仅当提供 user_id 时返回
}
```

### Comment (评论)
```typescript
interface Comment {
  id: number;
  blessing_id: number;
  user_id: string;
  user_name: string;
  content: string;
  like_count: number;
  created_at: string;
}
```

---

## 🔗 相关资源

- Web 页面：http://47.254.68.82/blessings
- 源码仓库：https://github.com/zhangfuwen/xiuxin-app

---

**最后更新**: 2026-03-15
