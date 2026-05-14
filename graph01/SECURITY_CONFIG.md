# 🔐 安全配置指南

## ⚠️ 重要提示

本项目已清理所有硬编码的密钥和敏感信息。在运行项目之前，您必须配置环境变量。

## 📋 配置步骤

### 1. 复制环境变量模板

```bash
# 在项目根目录执行
cp .env.example .env.local
```

### 2. 填写真实的密钥值

编辑 `.env.local` 文件，填入您的真实密钥：

#### 后端配置 (bank_end)

```properties
# DeepSeek API 密钥
DEEPSEEK_API_KEY=sk-your-actual-api-key

# Token 加密密钥（建议使用随机生成的字符串）
TOKEN_SECRET=your-random-secret-key-at-least-32-chars

# 数据库密码
DB_PASSWORD=your-database-password

# Druid 监控控制台密码
DRUID_PASSWORD=your-druid-admin-password
```

#### 前端配置 (graph_fontend)

```javascript
// 讯飞 TTS 配置
XF_APP_ID=your-xfyun-app-id
XF_API_KEY=your-xfyun-api-key
XF_API_SECRET=your-xfyun-api-secret

// 高德地图配置
AMAP_KEY=your-amap-key
AMAP_SECURITY_CODE=your-amap-security-code
AMAP_SEARCH_KEY=your-amap-search-key
```

### 3. 确保 .env.local 不会被提交

项目根目录的 `.gitignore` 已经配置为忽略 `.env.local` 文件。

**验证方法：**
```bash
git status
# 不应该看到 .env.local 在待提交列表中
```

## 🔑 如何获取密钥

### DeepSeek API
1. 访问 https://platform.deepseek.com
2. 注册/登录账号
3. 在 API Keys 页面创建新的密钥

### 讯飞开放平台
1. 访问 https://www.xfyun.cn
2. 创建应用并开通语音合成服务
3. 在应用详情中查看 AppID、APIKey 和 APISecret

### 高德地图开放平台
1. 访问 https://console.amap.com
2. 创建应用并添加 Key
3. 选择 Web 服务、Web 端(JS API)等平台
4. 获取 Key 和安全密钥

## 🛡️ 安全最佳实践

### ✅ 应该做的
- 使用环境变量管理所有密钥
- 定期轮换密钥（建议每 90 天）
- 使用强密码和随机字符串
- 不同环境使用不同的密钥
- 限制 API 密钥的使用范围和配额

### ❌ 不应该做的
- **永远不要**将 `.env.local` 提交到 Git
- **永远不要**在代码中硬编码密钥
- **永远不要**在日志中打印完整密钥
- **永远不要**在前端代码中暴露服务端密钥
- **永远不要**与他人共享您的密钥

## 🚨 如果密钥泄露了怎么办？

1. **立即撤销泄露的密钥**
   - 前往对应的服务平台撤销密钥
   - 生成新的密钥

2. **更新环境变量**
   - 修改 `.env.local` 中的密钥
   - 重启应用使新密钥生效

3. **检查日志和监控**
   - 查看是否有异常使用记录
   - 检查是否有未授权的访问

4. **通知相关人员**
   - 如果是团队项目，通知团队成员
   - 必要时向平台方报告

## 📝 配置文件说明

### 后端配置文件
- `bank_end/ruoyi-admin/src/main/resources/application.yml` - 主配置文件（已使用环境变量）
- `bank_end/ruoyi-admin/src/main/resources/application-druid.yml` - 数据源配置（已使用环境变量）

### 前端配置文件
- `graph_fontend/utils/xfyun-tts.js` - 讯飞TTS配置（已使用环境变量）
- `graph_fontend/utils/map.js` - 高德地图配置（已使用环境变量）
- `graph_fontend/api/normal/graph.js` - API配置（已使用环境变量）

## 🔍 验证配置

启动应用后，检查日志确认：
- 没有 "your-xxx-here" 这样的占位符
- API 调用成功，没有认证错误
- 数据库连接正常

## 📞 需要帮助？

如果在配置过程中遇到问题：
1. 检查环境变量是否正确加载
2. 确认密钥是否有效且未过期
3. 查看应用日志中的错误信息
4. 参考各平台的官方文档

---

**记住：保护密钥就是保护您的应用和用户数据安全！** 🔒
