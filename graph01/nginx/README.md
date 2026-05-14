# Nginx SSL 配置说明

## 📋 已完成的配置

### 1. 自签名证书生成
✅ 已生成自签名 SSL 证书文件：
- `cert.pem` - SSL 证书文件
- `key.pem` - SSL 私钥文件

证书信息：
- 有效期：365 天
- 密钥长度：RSA 4096 位
- 域名：localhost

### 2. Nginx 配置文件
✅ 已创建 `nginx.conf` 配置文件，包含：
- 监听端口：9002 (SSL)
- SSL 证书路径配置
- 代理转发规则：将 `/favorite`, `/api`, `/need`, `/share`, `/upLog` 路径转发到 `http://127.0.0.1:8088`
- SSL 优化配置（TLSv1.2/TLSv1.3）

## 🚀 使用方法

### Windows 系统

#### 方法 1：使用 Nginx for Windows
1. 下载 Nginx for Windows：https://nginx.org/en/download.html
2. 解压到任意目录
3. 将以下文件复制到 Nginx 目录：
   - `conf/cert.pem` → `<nginx_dir>/conf/cert.pem`
   - `conf/key.pem` → `<nginx_dir>/conf/key.pem`
   - `conf/nginx.conf` → `<nginx_dir>/conf/nginx.conf`（替换原文件）

4. 启动 Nginx：
```powershell
cd <nginx_dir>
.\nginx.exe
```

5. 停止 Nginx：
```powershell
.\nginx.exe -s stop
```

6. 重新加载配置：
```powershell
.\nginx.exe -s reload
```

#### 方法 2：使用 Chocolatey 安装
```powershell
# 安装 Nginx
choco install nginx

# 复制配置文件
Copy-Item "D:\java-table\graph01\nginx\conf\*" "C:\tools\nginx\conf\" -Force

# 启动 Nginx
Start-Service nginx
```

### 测试 HTTPS 连接

访问以下地址测试：
- https://localhost:9002/api/... （会提示证书不安全，点击继续访问即可）

## ⚠️ 注意事项

### 1. 浏览器证书警告
由于使用的是自签名证书，浏览器会显示"您的连接不是私密连接"警告。这是正常的，可以：
- Chrome: 点击"高级" → "继续前往 localhost (不安全)"
- Firefox: 点击"高级" → "接受风险并继续"
- Edge: 点击"详细信息" → "继续转到网页"

### 2. 生产环境建议
⚠️ **自签名证书仅用于开发测试**，生产环境请使用：
- Let's Encrypt（免费）
- 购买商业 SSL 证书
- 企业内部 CA 签发证书

### 3. 后端服务
确保后端服务已在 `http://127.0.0.1:8088` 运行，否则代理会失败。

### 4. 防火墙
如果无法访问，检查 Windows 防火墙是否允许 9002 端口：
```powershell
# 添加入站规则
New-NetFirewallRule -DisplayName "Nginx SSL" -Direction Inbound -LocalPort 9002 -Protocol TCP -Action Allow
```

## 🔧 常用命令

### 检查 Nginx 配置
```powershell
nginx -t
```

### 查看 Nginx 版本
```powershell
nginx -v
```

### 查看完整配置
```powershell
nginx -T
```

## 📝 修改代理规则

如需修改代理的路径或目标服务器，编辑 `nginx.conf` 中的 location 块：

```nginx
location ~ /(favorite|api|need|share|upLog)/ {
    proxy_pass http://127.0.0.1:8088;  # 修改为目标服务器地址
    # ... 其他配置
}
```

修改后重新加载配置：
```powershell
nginx -s reload
```

## 🐛 故障排查

### 问题 1：端口被占用
```
Error: bind() to 0.0.0.0:9002 failed (10013: An attempt was made to access a socket...)
```
解决：查找并关闭占用 9002 端口的进程
```powershell
netstat -ano | findstr :9002
taskkill /PID <进程ID> /F
```

### 问题 2：证书文件找不到
```
Error: cannot load certificate "cert.pem"
```
解决：确保证书文件在 Nginx 的 conf 目录下

### 问题 3：后端服务未启动
访问 https://localhost:9002/api/... 返回 502 Bad Gateway
解决：确保后端服务在 8088 端口正常运行

## 📚 相关资源

- Nginx 官方文档：https://nginx.org/en/docs/
- OpenSSL 文档：https://www.openssl.org/docs/
- Let's Encrypt：https://letsencrypt.org/
