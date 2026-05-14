import store from '@/store'
import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { toast, showConfirm, tansParams } from '@/utils/common'

let timeout = 60000
const baseUrl = config.baseUrl

const request = config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  // 是否静默处理错误（不显示 toast）
  const silentError = config.silentError === true
  config.header = config.header || {}
  if (getToken() && !isToken) {
    config.header['Authorization'] = 'Bearer ' + getToken()
  }
  // get请求映射params参数
  if (config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.url = url
  }
  // DELETE请求如果使用了data，需要确保Content-Type正确
  if ((config.method || 'get').toLowerCase() === 'delete' && config.data) {
    config.header['Content-Type'] = 'application/json;charset=UTF-8'
  }
  
  // 🔧 关键修复：创建一个带 abort 方法的 Promise
  let requestTask = null;
  
  const promise = new Promise((resolve, reject) => {
    const startTime = Date.now()
    
    // 🔧 关键修复：保存 requestTask 引用
    requestTask = uni.request({
        method: config.method || 'get',
        timeout: config.timeout || timeout,
        url: config.baseUrl || baseUrl + config.url,
        data: config.data,
        header: config.header,
        enableHttp2: true,  // 启用 HTTP/2
        enableQuic: false,  // 禁用 QUIC
        success: (res) => {
          const duration = Date.now() - startTime
          
          // 特别记录DELETE请求的详细信息
          if ((config.method || 'get').toLowerCase() === 'delete') {
            console.log('=== DELETE请求响应 ===')
            console.log('请求URL:', config.url)
            console.log('响应状态码:', res.statusCode)
            console.log('响应数据:', res.data)
            console.log('耗时:', duration, 'ms')
          }
          
          const code = res.data.code || 200
          const msg = errorCode[code] || res.data.msg || errorCode['default']
          
          if (code === 401) {
            showConfirm('登录状态已过期，您可以继续留在该页面，或者重新加载页面?').then(confirmRes => {
              if (confirmRes.confirm) {
                store.dispatch('LogOut').then(() => {
                  uni.reLaunch({ url: '/pages/index' })
                })
              }
            })
            reject('无效的会话，或者会话已过期，请重新加载页面。')
          } else if (code === 500) {
            if (!silentError) toast(msg)
            reject('500')
          } else if (code !== 200) {
            if (!silentError) toast(msg)
            reject(code)
          } else {
            resolve(res.data)
          }
        },
        fail: (error) => {
          const duration = Date.now() - startTime
          
          let { errMsg } = error
          let message = '请求失败'
          
          if (errMsg && errMsg.includes('timeout')) {
            message = '系统接口请求超时'
          } else if (errMsg && errMsg.includes('fail')) {
            message = '后端接口连接异常'
          }
          
          if (!silentError) toast(message)
          reject(error)
        },
        complete: () => {
          // 请求完成
        }
      })
  });
  
  // 🔧 关键修复：给 Promise 添加 abort 方法
  promise.abort = function() {
    if (requestTask && typeof requestTask.abort === 'function') {
      console.log('[Request] 🚫 中止请求:', config.url);
      requestTask.abort();
    }
  };
  
  return promise;
}

export default request
