# 文件上传功能验证清单

## ✅ 代码实现检查

### 1. 使用 uni.uploadFile (POST)
- [x] 位置: `utils/upload.js` 第72-94行
- [x] 方法: `uni.uploadFile()` - POST 请求
- [x] 不是 `uni.request()` (GET)

### 2. 字段名配置
- [x] 字段名: `name: 'file'` (第76行)
- [x] 与后端参数名一致

### 3. Token 配置
- [x] header: `{}` (第77行)
- [x] 不携带 Authorization token
- [x] 接口已在白名单中

### 4. 微信小程序兼容性
- [x] 使用 `uni.uploadFile` - 完全兼容微信小程序
- [x] 对应微信原生 API: `wx.uploadFile`

## ⚙️ 需要配置的项

### 微信小程序后台配置

#### 正式环境必须配置：
1. 登录 [微信小程序管理后台](https://mp.weixin.qq.com/)
2. 进入：**开发** → **开发管理** → **开发设置**
3. 找到：**服务器域名** → **uploadFile合法域名**
4. 添加您的后端域名，例如：
   ```
   https://your-api-domain.com
   ```

#### 开发环境临时方案：
1. 打开微信开发者工具
2. 点击右上角 **详情**
3. 勾选：**不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**
4. ⚠️ 注意：此选项仅用于开发调试，发布前必须配置合法域名

## 🧪 测试步骤

### 1. 测试头像上传
1. 进入用户中心页面
2. 点击"修改信息"或"更换头像"
3. 选择一张图片
4. 观察控制台输出：
   ```
   开始上传图片: temp://xxx
   上传响应: {code: 200, fileName: "/profile/upload/xxx.jpg"}
   图片URL: https://your-domain.com/profile/upload/xxx.jpg
   ```

### 2. 验证网络请求
在微信开发者工具的 **Network** 面板中检查：
- [ ] 请求方法: POST
- [ ] 请求URL: `https://your-domain.com/common/upload`
- [ ] 请求头: 不包含 `Authorization`
- [ ] Form Data: 包含 `file` 字段
- [ ] 响应状态: 200

### 3. 验证文件路径
上传成功后，检查：
- [ ] 返回的 `fileName` 格式: `/profile/upload/xxx.jpg`
- [ ] 显示的头像 URL 正确拼接
- [ ] 提交用户信息时携带正确的 `imageUrl`

## 🐛 常见问题排查

### 问题1: 上传失败 - "不在以下 uploadFile 合法域名列表中"
**解决方案:**
- 开发环境: 勾选"不校验合法域名"
- 生产环境: 在小程序后台配置合法域名

### 问题2: 上传成功但图片无法显示
**检查项:**
- [ ] `fileUrl()` 函数是否正确拼接 URL
- [ ] 后端返回的 `fileName` 格式是否正确
- [ ] 浏览器/小程序能否访问该图片 URL

### 问题3: 401 未授权错误
**检查项:**
- [ ] 确认 `header: {}` 没有携带 token
- [ ] 确认后端接口在白名单中
- [ ] 检查后端日志确认是否需要认证

### 问题4: 字段名不匹配
**检查项:**
- [ ] 确认 `name: 'file'` 与后端参数名一致
- [ ] 查看后端接收的参数名是否为 `file`
- [ ] 如果不一致，修改 `name` 的值

## 📝 相关代码位置

### 核心上传逻辑
- **上传工具函数**: `utils/upload.js` - `uploadFile()` 函数
- **图片上传组件**: `components/ImageUploader.vue`
- **用户页面**: `pages/user/user.vue`

### 关键代码片段

**utils/upload.js (第70-95行)**
```javascript
export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      timeout: 10000,
      url: baseUrl + '/common/upload',
      filePath: filePath,
      name: 'file',        // ✅ 字段名为 file
      header: {},          // ✅ 不携带 token
      success: (res) => {
        let result = JSON.parse(res.data)
        if (result.code === 200) {
          resolve(result)
        } else {
          reject(new Error(result.msg || '上传失败'))
        }
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络请求失败'))
      }
    })
  })
}
```

## ✨ 总结

当前文件上传实现**完全符合**所有要求：
- ✅ 使用 `uni.uploadFile` (POST)
- ✅ 字段名为 `file`
- ✅ 不携带 token
- ✅ 微信小程序兼容

只需确保在微信小程序后台配置了正确的服务器域名即可正常使用。
