/**
 * 讯飞语音合成测试示例
 * 
 * 使用方法：
 * 1. 在需要使用的页面引入此工具
 * 2. 配置讯飞应用的 appId、apiKey、apiSecret
 * 3. 调用相关函数进行测试
 */

import { xfTextToSpeech, playXfAudio, testXfConnection, updateXfConfig } from '@/utils/xfyun-tts'

/**
 * 测试1：基础语音合成测试
 */
export async function testBasicTTS() {
  try {
    console.log('=== 开始基础TTS测试 ===')
    
    // 配置讯飞参数（请替换为你的实际配置）
    updateXfConfig({
      appId: 'YOUR_APP_ID',
      apiKey: 'YOUR_API_KEY',
      apiSecret: 'YOUR_API_SECRET'
    })
    
    // 测试连接
    const isConnected = await testXfConnection()
    console.log('连接测试结果:', isConnected)
    
    if (!isConnected) {
      console.error('连接测试失败，请检查配置')
      return
    }
    
    // 合成语音
    const result = await xfTextToSpeech('你好，这是讯飞语音合成测试', {
      vcn: 'xiaoyan', // 发音人
      speed: 50,      // 语速
      volume: 50,     // 音量
      pitch: 50       // 音调
    })
    
    console.log('合成成功:', result)
    uni.showToast({ title: '合成成功', icon: 'success' })
    
  } catch (error) {
    console.error('TTS测试失败:', error)
    uni.showToast({ title: '测试失败: ' + error.message, icon: 'none' })
  }
}

/**
 * 测试2：播放合成的音频
 */
export async function testPlayAudio() {
  try {
    console.log('=== 开始音频播放测试 ===')
    
    // 配置讯飞参数
    updateXfConfig({
      appId: 'YOUR_APP_ID',
      apiKey: 'YOUR_API_KEY',
      apiSecret: 'YOUR_API_SECRET'
    })
    
    // 播放音频
    const result = await playXfAudio('欢迎使用路听应用，祝您使用愉快', {
      vcn: 'xiaoyan',
      speed: 50,
      volume: 60,
      pitch: 50
    })
    
    console.log('播放结果:', result)
    uni.showToast({ title: '播放完成', icon: 'success' })
    
  } catch (error) {
    console.error('音频播放失败:', error)
    uni.showToast({ title: '播放失败: ' + error.message, icon: 'none' })
  }
}

/**
 * 测试3：不同发音人测试
 */
export async function testDifferentVoices() {
  const voices = [
    { name: 'xiaoyan', desc: '小燕-女声' },
    { name: 'xiaofeng', desc: '小峰-男声' },
    { name: 'aisxping', desc: '艾舒-童声' },
    { name: 'vinn', desc: '威尼-男声' }
  ]
  
  for (const voice of voices) {
    try {
      console.log(`=== 测试发音人: ${voice.desc} ===`)
      
      await playXfAudio(`你好，我是${voice.desc}`, {
        vcn: voice.name,
        speed: 50,
        volume: 50
      })
      
      // 等待播放完成
      await new Promise(resolve => setTimeout(resolve, 3000))
      
    } catch (error) {
      console.error(`${voice.desc} 测试失败:`, error)
    }
  }
}

/**
 * 测试4：不同语速测试
 */
export async function testDifferentSpeeds() {
  const speeds = [
    { value: 30, desc: '慢速' },
    { value: 50, desc: '正常' },
    { value: 70, desc: '快速' }
  ]
  
  for (const speed of speeds) {
    try {
      console.log(`=== 测试语速: ${speed.desc} (${speed.value}) ===`)
      
      await playXfAudio(`这是${speed.desc}的语音播报测试`, {
        vcn: 'xiaoyan',
        speed: speed.value,
        volume: 50
      })
      
      // 等待播放完成
      await new Promise(resolve => setTimeout(resolve, 3000))
      
    } catch (error) {
      console.error(`${speed.desc} 测试失败:`, error)
    }
  }
}

/**
 * 在Vue组件中使用示例
 */
export const usageExample = {
  // 在 methods 中添加
  methods: {
    async handleTTSTest() {
      uni.showLoading({ title: '正在合成...' })
      
      try {
        // 配置参数
        updateXfConfig({
          appId: 'your_app_id',
          apiKey: 'your_api_key',
          apiSecret: 'your_api_secret'
        })
        
        // 合成并播放
        await playXfAudio('这是一段测试文本', {
          vcn: 'xiaoyan',
          speed: 50,
          volume: 60
        })
        
        uni.hideLoading()
        uni.showToast({ title: '播放完成', icon: 'success' })
        
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: '失败: ' + error.message, icon: 'none' })
      }
    }
  }
}

/**
 * 完整的测试流程
 */
export async function runAllTests() {
  console.log('========================================')
  console.log('开始运行所有讯飞TTS测试')
  console.log('========================================')
  
  // 1. 基础测试
  await testBasicTTS()
  
  // 等待2秒
  await new Promise(resolve => setTimeout(resolve, 2000))
  
  // 2. 播放测试
  await testPlayAudio()
  
  // 等待2秒
  await new Promise(resolve => setTimeout(resolve, 2000))
  
  // 3. 不同发音人测试
  await testDifferentVoices()
  
  // 等待2秒
  await new Promise(resolve => setTimeout(resolve, 2000))
  
  // 4. 不同语速测试
  await testDifferentSpeeds()
  
  console.log('========================================')
  console.log('所有测试完成')
  console.log('========================================')
}

export default {
  testBasicTTS,
  testPlayAudio,
  testDifferentVoices,
  testDifferentSpeeds,
  runAllTests,
  usageExample
}
