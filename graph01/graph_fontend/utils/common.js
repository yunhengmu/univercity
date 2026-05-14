/**
* 显示消息提示框
* @param content 提示的标题
*/
export function toast(content) {
  uni.showToast({
    icon: 'none',
    title: content
  })
}

/**
* 显示模态弹窗
* @param content 提示的标题
*/
export function showConfirm(content) {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: '提示',
      content: content,
      cancelText: '取消',
      confirmText: '确定',
      success: function(res) {
        resolve(res)
      }
    })
  })
}

/**
* 参数处理
* @param params 参数
*/
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    var part = encodeURIComponent(propName) + "="
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            let params = propName + '[' + key + ']'
            var subPart = encodeURIComponent(params) + "="
            result += subPart + encodeURIComponent(value[key]) + "&"
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&"
      }
    }
  }
  return result
}

/**
 * H5端顶部安全区域适配函数
 * 获取系统信息并计算顶部安全距离
 * @returns {Object} 包含顶部安全距离的对象 { safeTop: number }
 */
export function getSafeAreaInfo() {
  // #ifdef H5
  return new Promise((resolve) => {
    uni.getSystemInfo({
      success: (res) => {
        const statusBarHeight = res.statusBarHeight || 0
        const safeTop = statusBarHeight > 0 ? statusBarHeight : 0
        resolve({
          safeTop,
          statusBarHeight,
          platform: res.platform,
          model: res.model
        })
      },
      fail: () => {
        resolve({
          safeTop: 0,
          statusBarHeight: 0,
          platform: 'unknown',
          model: 'unknown'
        })
      }
    })
  })
  // #endif
  
  // #ifndef H5
  return Promise.resolve({
    safeTop: 0,
    statusBarHeight: 0,
    platform: 'non-h5',
    model: 'non-h5'
  })
  // #endif
}