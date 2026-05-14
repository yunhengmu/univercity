<script>
  import config from './config'
  import { getToken } from '@/utils/auth'

  export default {
    onLaunch: function() {
      this.initApp()
    },
    methods: {
      // 初始化应用
      initApp() {
        // 初始化应用配置
        this.initConfig()
        // 检查用户登录状态
        //#ifdef H5
        this.checkLogin()
        //#endif
        // iPhone 12 及刘海屏设备适配
        this.adaptSafeArea()
      },
      initConfig() {
        this.globalData.config = config
      },
      checkLogin() {
        // 移除强制跳转到登录页面的逻辑
        // 现在未登录用户也可以直接进入首页
      },
      /**
       * iPhone 12 及安全区域适配
       * 检测设备类型并设置全局安全区域变量
       */
      adaptSafeArea() {
        // #ifdef H5 || APP-PLUS
        const systemInfo = uni.getSystemInfoSync()
        const { 
          model, 
          screenHeight, 
          screenWidth,
          safeAreaInsets,
          platform 
        } = systemInfo
        
        const isIPhone = platform === 'ios' || /iphone/i.test(model)
        const hasNotch = isIPhone && safeAreaInsets && safeAreaInsets.top > 20
        
        if (hasNotch) {
          
          // 设置全局变量标记
          this.globalData.isIPhoneWithNotch = true
          this.globalData.isIPhone12 = (
            (screenWidth === 390 && screenHeight === 844) || // iPhone 12/12 Pro/13/14
            (screenWidth === 375 && screenHeight === 812) || // iPhone 12 Mini/X/XS
            (screenWidth === 428 && screenHeight === 926)    // iPhone 12 Pro Max/13/14 Pro Max
          )
          this.globalData.safeAreaInsets = safeAreaInsets
          
          // 动态设置 CSS 变量（H5环境）
          // #ifdef H5
          if (safeAreaInsets) {
            const root = document.documentElement
            root.style.setProperty('--safe-area-inset-top', `${safeAreaInsets.top}px`)
            root.style.setProperty('--safe-area-inset-bottom', `${safeAreaInsets.bottom}px`)
            root.style.setProperty('--safe-area-inset-left', `${safeAreaInsets.left}px`)
            root.style.setProperty('--safe-area-inset-right', `${safeAreaInsets.right}px`)
          }
          // #endif
        } else {
          this.globalData.isIPhoneWithNotch = false
          this.globalData.isIPhone12 = false
        }
        
        // #endif
      }
    }
  }
</script>

<style lang="scss">
  @import '@/static/scss/index.scss';
</style>
