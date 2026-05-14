import { getToken } from '@/utils/auth'

// 页面白名单
const whiteList = [
  '/pages/index', '/pages/book/book', '/pages/record/record', '/pages/user/user'
]

// 检查地址白名单
function checkWhite(url) {
  const path = url.split('?')[0]
  return whiteList.indexOf(path) !== -1
}

// 页面跳转验证拦截器
let list = ["navigateTo", "redirectTo", "reLaunch", "switchTab"]
list.forEach(item => {
  uni.addInterceptor(item, {
    invoke(to) {
      // 暂时移除登录验证，因为没有登录页面
      return true
    },
    fail(err) {
      // 权限检查失败
    }
  })
})
