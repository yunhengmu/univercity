// 应用全局配置
module.exports = {
  // 后端服务地址配置（可切换不同环境）
  // 选项1: 本机局域网地址（用于真机调试）
  // baseUrl: 'http://10.31.57.115:9002',
  
  // 选项2: cpolar内网穿透地址
  // baseUrl: 'https://2be7f3bc.r7.cpolar.cn',
  
  // 选项3: tunnelFRP内网穿透地址（当前使用）
  baseUrl: 'http://luting.s7.tunnelfrp.com',
  
  // 选项4: 生产环境地址（部署后使用）
  // baseUrl: 'https://your-production-domain.com',
  // 应用信息
  appInfo: {
    // 应用名称
    name: "ruoyi-app",
    // 应用版本
    version: "1.2.0",
    // 应用logo
    logo: "/static/logo.png",
    // 官方网站
    site_url: "https://ruoyi.vip",
    // 政策协议
    agreements: [{
        title: "隐私政策",
        url: "https://ruoyi.vip/protocol.html"
      },
      {
        title: "用户服务协议",
        url: "https://ruoyi.vip/protocol.html"
      }
    ]
  }
}
