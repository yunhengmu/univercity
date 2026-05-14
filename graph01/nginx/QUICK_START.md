# 🚀 Nginx SSL 快速开始

## ✅ 已完成的工作

1. ✅ 生成自签名 SSL 证书（cert.pem 和 key.pem）
2. ✅ 创建 Nginx 配置文件（nginx.conf）
3. ✅ 创建启动/停止脚本

## 📦 使用前准备

### 安装 Nginx

**方法 1：直接下载（推荐）**
1. 访问：https://nginx.org/en/download.html
2. 下载 "nginx/Windows-x.x.x" 稳定版本
3. 解压到任意目录，例如：`C:\nginx`

**方法 2：使用 Chocolatey**
```powershell
choco install nginx
```

## 🎯 快速启动（3 步）

### 步骤 1：复制文件到 Nginx 目录

假设你安装在 `C:\nginx`，执行以下命令：

```powershell
# 复制证书和配置文件
Copy-Item "D:\java-table\graph01\nginx\conf\*" "C:\nginx\conf\" -Force
```

### 步骤 2：启动后端服务

确保你的后端服务在 **8088 端口**运行。

### 步骤 3：启动 Nginx

**方法 A：使用批处理脚本（推荐）**
```powershell
cd D:\java-table\graph01\nginx
.\start-nginx.bat
```

**方法 B：手动启动**
```powershell
cd C:\nginx
.\nginx.exe
```

## ✨ 测试访问

打开浏览器访问：
```
https://localhost:9002/api/your-endpoint
```

⚠️ **首次访问会提示证书不安全**，这是正常的（因为是自签名证书）：
- Chrome: 点击"高级" → "继续前往 localhost (不安全)"
- Edge: 点击"详细信息" → "继续转到网页"

## 🛑 停止 Nginx

**方法 A：使用批处理脚本**
```powershell
cd D:\java-table\graph01\nginx
.\stop-nginx.bat
```

**方法 B：命令行**
```powershell
cd C:\nginx
.\nginx.exe -s stop
```

## 🔧 常用操作

### 重新加载配置（不中断服务）
```powershell
nginx -s reload
```

### 检查配置是否正确
```powershell
nginx -t
```

### 查看 Nginx 是否运行
```powershell
tasklist | findstr nginx
```

## 📝 配置说明

当前配置会将以下路径的请求转发到后端：
- `/favorite/*` → `http://127.0.0.1:8088/favorite/*`
- `/api/*` → `http://127.0.0.1:8088/api/*`
- `/need/*` → `http://127.0.0.1:8088/need/*`
- `/share/*` → `http://127.0.0.1:8088/share/*`
- `/upLog/*` → `http://127.0.0.1:8088/upLog/*`

## ❓ 常见问题

### Q1: 启动时提示端口被占用？
```powershell
# 查找占用 9002 端口的进程
netstat -ano | findstr :9002

# 终止该进程（替换 <PID> 为实际进程 ID）
taskkill /PID <PID> /F
```

### Q2: 访问返回 502 Bad Gateway？
- 检查后端服务是否在 8088 端口运行
- 访问 `http://localhost:8088` 确认后端正常

### Q3: 如何修改代理的后端地址？
编辑 `conf/nginx.conf`，找到这一行：
```nginx
proxy_pass http://127.0.0.1:8088;
```
修改为你需要的地址，然后执行 `nginx -s reload`

## 📂 文件结构

```
nginx/
├── conf/
│   ├── cert.pem          # SSL 证书
│   ├── key.pem           # SSL 私钥
│   └── nginx.conf        # Nginx 配置文件
├── start-nginx.bat       # 启动脚本
├── stop-nginx.bat        # 停止脚本
└── README.md            # 详细说明文档
```

## 💡 提示

- 自签名证书仅用于开发测试
- 生产环境请使用正式的 SSL 证书（如 Let's Encrypt）
- 详细文档请查看 [README.md](./README.md)

---
**祝你使用愉快！** 🎉
