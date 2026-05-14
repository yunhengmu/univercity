/**
 * 讯飞语音合成工具
 * 用于测试讯飞TTS API
 * 
 * 使用前需要安装依赖:
 * npm install crypto-js
 */

// 尝试引入 crypto-js，如果没有安装则使用备用方案
let CryptoJS = null
try {
  CryptoJS = require('crypto-js')
} catch (e) {
  console.warn('[讯飞TTS] crypto-js 未安装，将使用内置的加密函数')
}

// 讯飞配置信息（请替换为实际的配置）
const XF_CONFIG = {
  appId: process.env.XF_APP_ID || 'your-app-id-here', // 讯飞应用ID
  apiKey: process.env.XF_API_KEY || 'your-api-key-here', // 讯飞API Key
  apiSecret: process.env.XF_API_SECRET || 'your-api-secret-here', // 讯飞API Secret
  hostUrl: 'https://tts-api.xfyun.cn/v2/tts', // 讯飞TTS接口地址
}

/**
 * 生成讯飞鉴权URL
 * @returns {string} 鉴权后的URL
 */
function generateAuthUrl() {
  const url = new URL(XF_CONFIG.hostUrl)
  const host = url.host
  const date = new Date().toUTCString()
  const requestLine = `POST /v2/tts HTTP/1.1`
  
  // 构建签名字符串
  const signatureOrigin = `host: ${host}\ndate: ${date}\n${requestLine}`
  
  // HMAC-SHA256签名
  let signature
  if (CryptoJS) {
    // 使用 crypto-js
    const signatureSha = CryptoJS.HmacSHA256(signatureOrigin, XF_CONFIG.apiSecret)
    signature = CryptoJS.enc.Base64.stringify(signatureSha)
  } else {
    // 使用内置的简易实现（仅用于测试）
    signature = hmacSHA256Base64(signatureOrigin, XF_CONFIG.apiSecret)
  }
  
  // 构建authorization
  const authorizationOrigin = `api_key="${XF_CONFIG.apiKey}", algorithm="hmac-sha256", headers="host date request-line", signature="${signature}"`
  const authorization = btoa(authorizationOrigin)
  
  // 组装最终URL
  url.searchParams.append('authorization', authorization)
  url.searchParams.append('date', date)
  url.searchParams.append('host', host)
  
  return url.toString()
}

/**
 * 构建请求体
 * @param {string} text - 要转换的文本
 * @param {object} options - 可选参数
 * @returns {object} 请求体对象
 */
function buildRequestBody(text, options = {}) {
  const {
    aue = 'lame', // 音频编码格式: lame(mp3), raw(pcm)
    sfl = 0, // 是否返回音频流: 0-不返回, 1-返回
    auf = 'audio/L16;rate=16000', // 音频采样率
    vcn = 'xiaoyan', // 发音人: xiaoyan(小燕-女声), xiaofeng(小峰-男声)等
    speed = 50, // 语速: 0-100
    volume = 50, // 音量: 0-100
    pitch = 50, // 音调: 0-100
    bgs = 0, // 背景音: 0-无, 1-有
  } = options
  
  return {
    common: {
      app_id: XF_CONFIG.appId
    },
    business: {
      aue,
      sfl,
      auf,
      vcn,
      speed,
      volume,
      pitch,
      bgs,
      tte: 'UTF8' // 文本编码
    },
    data: {
      status: 2, // 状态: 2-最后一次传输
      text: btoa(unescape(encodeURIComponent(text))) // Base64编码
    }
  }
}

/**
 * 讯飞语音合成
 * @param {string} text - 要转换的文本
 * @param {object} options - 可选参数
 * @returns {Promise<object>} 返回音频数据和相关信息
 */
export async function xfTextToSpeech(text, options = {}) {
  try {
    // 检查配置
    if (!XF_CONFIG.appId || !XF_CONFIG.apiKey || !XF_CONFIG.apiSecret) {
      throw new Error('请先配置讯飞应用的 appId、apiKey 和 apiSecret')
    }
    
    if (!text || text.trim() === '') {
      throw new Error('文本内容不能为空')
    }
    
    console.log('[讯飞TTS] 开始合成语音', { text: text.substring(0, 50) + '...' })
    
    // 生成鉴权URL
    const authUrl = generateAuthUrl()
    
    // 构建请求体
    const requestBody = buildRequestBody(text, options)
    
    console.log('[讯飞TTS] 请求参数:', JSON.stringify(requestBody, null, 2))
    
    // 发送请求
    return new Promise((resolve, reject) => {
      uni.request({
        url: authUrl,
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: requestBody,
        responseType: 'arraybuffer', // 接收二进制数据
        success: (res) => {
          console.log('[讯飞TTS] 响应状态码:', res.statusCode)
          
          if (res.statusCode === 200) {
            try {
              // 解析响应数据
              const responseStr = String.fromCharCode.apply(null, new Uint8Array(res.data))
              const responseData = JSON.parse(responseStr)
              
              console.log('[讯飞TTS] 响应数据:', responseData)
              
              if (responseData.code !== 0) {
                reject(new Error(`讯飞TTS错误: ${responseData.message || '未知错误'} (code: ${responseData.code})`))
                return
              }
              
              // 获取音频数据
              const audioData = responseData.data?.audio
              
              if (!audioData) {
                reject(new Error('未获取到音频数据'))
                return
              }
              
              // 解码Base64音频数据
              const audioBuffer = base64ToArrayBuffer(audioData)
              
              resolve({
                success: true,
                audioBuffer: audioBuffer,
                audioBase64: audioData,
                sid: responseData.sid, // 会话ID
                message: '语音合成成功'
              })
              
            } catch (parseError) {
              console.error('[讯飞TTS] 解析响应失败:', parseError)
              reject(new Error('解析响应数据失败: ' + parseError.message))
            }
          } else {
            reject(new Error(`请求失败，状态码: ${res.statusCode}`))
          }
        },
        fail: (error) => {
          console.error('[讯飞TTS] 请求失败:', error)
          reject(new Error('网络请求失败: ' + (error.errMsg || '未知错误')))
        }
      })
    })
    
  } catch (error) {
    console.error('[讯飞TTS] 错误:', error)
    throw error
  }
}

/**
 * 播放讯飞合成的音频
 * @param {string} text - 要转换的文本
 * @param {object} options - TTS选项
 * @returns {Promise<object>} 播放结果
 */
export async function playXfAudio(text, options = {}) {
  try {
    console.log('[讯飞TTS] 开始合成并播放音频')
    
    // 合成语音
    const result = await xfTextToSpeech(text, options)
    
    console.log('[讯飞TTS] 合成成功，准备播放')
    
    // #ifdef MP-WEIXIN
    // 微信小程序环境：使用 InnerAudioContext 播放
    const audioContext = uni.createInnerAudioContext()
    
    // 将音频数据转换为临时文件
    const tempFilePath = await saveAudioToFile(result.audioBuffer, options.aue || 'lame')
    
    return new Promise((resolve, reject) => {
      audioContext.src = tempFilePath
      
      audioContext.onPlay(() => {
        console.log('[讯飞TTS] 开始播放')
      })
      
      audioContext.onEnded(() => {
        console.log('[讯飞TTS] 播放结束')
        audioContext.destroy()
        resolve({ success: true, message: '播放完成' })
      })
      
      audioContext.onError((error) => {
        console.error('[讯飞TTS] 播放失败:', error)
        audioContext.destroy()
        reject(new Error('音频播放失败: ' + error.errMsg))
      })
      
      audioContext.play()
    })
    // #endif
    
    // #ifdef H5
    // H5环境：使用 Audio API 播放
    const audioBlob = new Blob([result.audioBuffer], { type: 'audio/mp3' })
    const audioUrl = URL.createObjectURL(audioBlob)
    const audio = new Audio(audioUrl)
    
    return new Promise((resolve, reject) => {
      audio.onended = () => {
        console.log('[讯飞TTS] 播放结束')
        URL.revokeObjectURL(audioUrl)
        resolve({ success: true, message: '播放完成' })
      }
      
      audio.onerror = (error) => {
        console.error('[讯飞TTS] 播放失败:', error)
        URL.revokeObjectURL(audioUrl)
        reject(new Error('音频播放失败'))
      }
      
      audio.play().catch(reject)
    })
    // #endif
    
  } catch (error) {
    console.error('[讯飞TTS] 播放失败:', error)
    throw error
  }
}

/**
 * 将音频数据保存为临时文件
 * @param {ArrayBuffer} audioBuffer - 音频数据
 * @param {string} format - 音频格式
 * @returns {Promise<string>} 临时文件路径
 */
function saveAudioToFile(audioBuffer, format = 'lame') {
  return new Promise((resolve, reject) => {
    const fileName = `xf_tts_${Date.now()}.${format === 'lame' ? 'mp3' : 'pcm'}`
    
    // #ifdef MP-WEIXIN
    const fs = uni.getFileSystemManager()
    const tempFilePath = `${wx.env.USER_DATA_PATH}/${fileName}`
    
    fs.writeFile({
      filePath: tempFilePath,
      data: audioBuffer,
      encoding: 'binary',
      success: () => {
        console.log('[讯飞TTS] 音频文件保存成功:', tempFilePath)
        resolve(tempFilePath)
      },
      fail: (error) => {
        console.error('[讯飞TTS] 音频文件保存失败:', error)
        reject(error)
      }
    })
    // #endif
    
    // #ifdef H5
    resolve(URL.createObjectURL(new Blob([audioBuffer])))
    // #endif
  })
}

/**
 * Base64转ArrayBuffer
 * @param {string} base64 - Base64字符串
 * @returns {ArrayBuffer} ArrayBuffer对象
 */
function base64ToArrayBuffer(base64) {
  const binaryString = atob(base64)
  const bytes = new Uint8Array(binaryString.length)
  
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i)
  }
  
  return bytes.buffer
}

/**
 * 测试讯飞TTS连接
 * @returns {Promise<boolean>} 连接是否成功
 */
export async function testXfConnection() {
  try {
    console.log('[讯飞TTS] 开始测试连接...')
    
    const result = await xfTextToSpeech('测试', {
      vcn: 'xiaoyan',
      speed: 50
    })
    
    console.log('[讯飞TTS] 连接测试成功', result)
    return true
    
  } catch (error) {
    console.error('[讯飞TTS] 连接测试失败:', error)
    return false
  }
}

/**
 * 更新讯飞配置
 * @param {object} config - 新的配置
 */
export function updateXfConfig(config) {
  if (config.appId) XF_CONFIG.appId = config.appId
  if (config.apiKey) XF_CONFIG.apiKey = config.apiKey
  if (config.apiSecret) XF_CONFIG.apiSecret = config.apiSecret
  if (config.hostUrl) XF_CONFIG.hostUrl = config.hostUrl
  
  console.log('[讯飞TTS] 配置已更新', {
    appId: XF_CONFIG.appId ? '已设置' : '未设置',
    apiKey: XF_CONFIG.apiKey ? '已设置' : '未设置',
    apiSecret: XF_CONFIG.apiSecret ? '已设置' : '未设置'
  })
}

/**
 * 获取当前配置
 * @returns {object} 当前配置
 */
export function getXfConfig() {
  return {
    appId: XF_CONFIG.appId,
    apiKey: XF_CONFIG.apiKey ? '***' : '',
    apiSecret: XF_CONFIG.apiSecret ? '***' : '',
    hostUrl: XF_CONFIG.hostUrl
  }
}

/**
 * 内置的 HMAC-SHA256 转 Base64 函数（备用方案）
 * 注意：此实现仅用于测试，生产环境建议使用 crypto-js
 * @param {string} message - 消息
 * @param {string} key - 密钥
 * @returns {string} Base64编码的签名
 */
function hmacSHA256Base64(message, key) {
  // 这是一个简化的实现，实际项目中建议安装 crypto-js
  // 如果需要使用此功能，请运行: npm install crypto-js
  console.error('[讯飞TTS] 请使用 crypto-js 库进行加密')
  console.error('[讯飞TTS] 安装命令: npm install crypto-js')
  throw new Error('crypto-js 未安装，无法生成签名。请安装 crypto-js 或使用后端服务代理请求')
}

export default {
  xfTextToSpeech,
  playXfAudio,
  testXfConnection,
  updateXfConfig,
  getXfConfig
}
