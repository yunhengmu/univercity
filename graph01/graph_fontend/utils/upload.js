import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { toast, showConfirm, tansParams } from '@/utils/common'
import store from '@/store'

let timeout = 10000
const baseUrl = config.baseUrl

const upload = (config) => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
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
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      timeout: config.timeout || timeout,
      url: baseUrl + config.url,
      filePath: config.filePath,
      name: config.name || 'file',
      header: config.header,
      formData: config.formData,
      success: (res) => {
        let result = JSON.parse(res.data)
        const code = result.code || 200
        const msg = errorCode[code] || result.msg || errorCode['default']
        if (code === 200) {
          resolve(result)
        } else if (code == 401) {
          showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then((res) => {
            if (res.confirm) {
              store.dispatch('LogOut').then((res) => {
                uni.reLaunch({ url: '/pages/login/login' })
              })
            }
          })
          reject('无效的会话，或者会话已过期，请重新登录。')
        } else if (code === 500) {
          toast(msg)
          reject('500')
        } else if (code !== 200) {
          toast(msg)
          reject(code)
        }
      },
      fail: (error) => {
        let { message } = error
        if (message == 'Network Error') {
          message = '后端接口连接异常'
        } else if (message.includes('timeout')) {
          message = '系统接口请求超时'
        } else if (message.includes('Request failed with status code')) {
          message = '系统接口' + message.substr(message.length - 3) + '异常'
        }
        toast(message)
        reject(error)
      },
    })
  })
}

export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      timeout: 10000,
      url: baseUrl + '/common/upload',
      filePath: filePath,
      name: 'file',
      header: {},  // 不携带任何认证信息
      success: (res) => {
        try {
          let result = JSON.parse(res.data)
          if (result.code === 200) {
            resolve(result)
          } else {
            reject(new Error(result.msg || '上传失败'))
          }
        } catch (e) {
          reject(new Error('响应解析失败'))
        }
      },
      fail: (error) => {
        reject(new Error(error.errMsg || '网络请求失败'))
      }
    })
  })
}

export function fileUrl(url) {
  // 如果url为空，返回空字符串
  if (!url) {
    return ''
  }
  
  // 如果url已经包含完整路径，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  
  // 去除开头的斜杠，避免重复
  const cleanUrl = url.startsWith('/') ? url.substring(1) : url
  
  // 如果url已经是 /profile/upload/xxx 或 /upload/xxx 格式，直接拼接baseUrl
  if (cleanUrl.startsWith('profile/upload/') || cleanUrl.startsWith('upload/')) {
    return config.baseUrl + '/' + cleanUrl
  }
  
  // 否则默认拼接成 /profile/upload/xxx 格式（根据数据库实际存储格式）
  return config.baseUrl + '/profile/upload/' + cleanUrl
}

export default upload
