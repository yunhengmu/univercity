# 敏感信息配置说明

## 重要提示

本项目已将所有的敏感信息（数据库密码、API密钥等）从配置文件中移除，改用环境变量方式进行配置。

## 配置步骤

### 1. 复制环境变量示例文件

```bash
cp .env.example .env
```

### 2. 编辑 .env 文件

打开 `.env` 文件，填入真实的配置值：

```env
DB_PASSWORD=your_real_database_password
ALIYUN_KEY_ID=your_real_aliyun_key_id
ALIYUN_KEY_SECRET=your_real_aliyun_key_secret
# ... 其他配置
```

### 3. 加载环境变量

#### Windows (PowerShell)
```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($matches[1], $matches[2], "User")
    }
}
```

#### Windows (CMD)
```cmd
for /F "tokens=1,2 delims==" %%A in (.env) do setx %%A %%B
```

#### Linux/Mac
```bash
set -a
source .env
set +a
```

### 4. 启动应用

环境变量设置完成后，正常启动 Spring Boot 应用即可。

## 安全建议

1. **永远不要将 `.env` 文件提交到 Git** - 已在 `.gitignore` 中配置
2. **不要将包含真实密钥的配置文件提交到 Git** - 所有敏感信息都使用环境变量
3. **定期轮换密钥** - 建议定期更新数据库密码、API密钥等
4. **使用密钥管理服务** - 生产环境建议使用专业的密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault 等）

## 配置项说明

### 数据库配置
- `DB_PASSWORD`: 数据库密码

### Redis 配置
- `REDIS_PASSWORD`: Redis密码（如果有）

### RabbitMQ 配置
- `RABBITMQ_HOST`: RabbitMQ服务器地址
- `RABBITMQ_USERNAME`: RabbitMQ用户名
- `RABBITMQ_PASSWORD`: RabbitMQ密码

### 阿里云 OSS 配置
- `ALIYUN_KEY_ID`: 阿里云AccessKey ID
- `ALIYUN_KEY_SECRET`: 阿里云AccessKey Secret
- `ALIYUN_BUCKET_NAME`: OSS Bucket名称

### 微信配置
- `WX_APP_ID`: 微信小程序AppID
- `WX_APP_SECRET`: 微信小程序AppSecret

### 微信支付配置
- `WEIXIN_APPID`: 微信支付AppID
- `WEIXIN_PARTNER`: 商户号
- `WEIXIN_PARTNERKEY`: API密钥
- `WEIXIN_NOTIFYURL`: 支付回调地址
- `WEIXIN_CERT_PATH`: 证书文件路径

## 开发环境快速配置（仅用于开发）

如果仅在本地开发环境使用，可以直接在配置文件中设置默认值。当前配置文件已使用 Spring Boot 的环境变量占位符语法：

```yaml
password: ${DB_PASSWORD:your_default_password}
```

其中 `your_default_password` 是默认值，当环境变量未设置时使用。**注意：默认值不应是真实的敏感信息。**

## 问题排查

如果应用启动时提示找不到配置，请检查：

1. 环境变量是否正确设置
2. 环境变量是否在当前会话中生效
3. IDE 是否配置了环境变量（Run/Debug Configuration）
