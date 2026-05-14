# 微信小程序同声传译 TTS 优化说明

## 📋 概述

本次优化针对 `pages/index.vue` 页面的语音播报功能，全面适配微信同声传译插件（AppID: wx069ba97219f66d99），提升微信小程序环境下的 TTS 体验。

## ✨ 主要改进

### 1. 统一的音频上下文管理

**优化前：**
- 每次 TTS 都创建新的 `InnerAudioContext`
- 缺乏统一管理，容易造成内存泄漏
- 暂停/恢复逻辑不完善

**优化后：**
- 使用 `this.audioContext` 统一管理音频实例
- 自动清理旧的音频上下文，避免资源浪费
- 完善的错误处理和降级方案

### 2. 增强的错误处理

```javascript
// 所有音频操作都包裹在 try-catch 中
try {
  this.audioContext.stop();
  console.log('[TTS] ⏹ 音频已停止');
} catch (e) {
  console.warn('[TTS] 停止音频失败', e);
}
```

### 3. 完善的日志系统

添加了详细的控制台日志，方便调试：
- `[TTS] ✓ 转换成功` - TTS 转换成功
- `[TTS] ▶ 开始播放` - 开始播放音频
- `[TTS] ⏸ 音频已暂停` - 音频暂停
- `[TTS] ⏹ 音频已停止` - 音频停止
- `[TTS] 🗑 音频上下文已销毁` - 资源清理
- `[TTS] ✗ 转换失败` - TTS 转换失败
- `[TTS] ✗ 音频播放失败` - 音频播放失败

### 4. 智能降级方案

当微信同声传译插件不可用时，自动降级为 Toast 提示：
```javascript
function fallbackSpeech(text, callback) {
  uni.showToast({ 
    title: `语音：${text.substring(0, 20)}...`, 
    icon: 'none', 
    duration: 3000 
  });
  setTimeout(() => {
    if (callback) callback();
  }, 3000);
}
```

## 🔧 技术实现

### 修改的文件

1. **utils/navigation.js**
   - 优化 `handleNonH5Speech` 函数
   - 添加 `context` 参数支持
   - 新增 `fallbackSpeech` 降级函数
   - 统一音频上下文管理逻辑

2. **pages/index.vue**
   - 更新 `speakText` 方法，传递 context
   - 优化 `togglePause` 方法的音频暂停逻辑
   - 增强 `clearAllSpeechContent` 的资源清理
   - 增强 `stopSpeech` 的资源清理
   - 增强 `performStopNavigation` 的资源清理

### 核心代码逻辑

#### 1. 音频上下文管理

```javascript
// 创建或复用音频上下文
let audioContext;
if (context) {
  // 销毁旧的音频上下文
  if (context.audioContext) {
    try {
      context.audioContext.destroy();
    } catch (e) {}
  }
  // 创建新的音频上下文
  audioContext = uni.createInnerAudioContext();
  context.audioContext = audioContext;
} else {
  audioContext = uni.createInnerAudioContext();
}
```

#### 2. 播放结束清理

```javascript
audioContext.onEnded(() => {
  console.log('[TTS] 播放完成');
  audioContext.destroy();
  // 如果使用了 context，清空引用
  if (context && context.audioContext === audioContext) {
    context.audioContext = null;
  }
  if (callback) callback();
});
```

#### 3. 暂停功能

```javascript
// #ifdef MP-WEIXIN
if (this.audioContext) {
  try {
    this.audioContext.pause();
    console.log('[TTS] ⏸ 音频已暂停');
  } catch (e) {
    console.error('[TTS] 暂停音频失败', e);
  }
}
// #endif
```

## 📱 兼容性说明

### 微信小程序环境

- ✅ 使用微信同声传译插件进行 TTS
- ✅ 自动管理音频上下文生命周期
- ✅ 完善的暂停/恢复机制
- ✅ 降级方案保证基本功能可用

### H5 环境

- ✅ 使用浏览器原生 `SpeechSynthesis` API
- ✅ 不受微信小程序优化影响

## 🎯 使用场景

### 1. 小说模式导航

- 调用 `getUserText` 获取用户引导文本
- 调用 `getArea` 获取区域介绍
- 轮询 `getChapter` 获取章节内容
- 所有文本通过优化后的 TTS 播报

### 2. 文化导览模式

- 调用 `getArea` 获取文化信息
- 或通过 `getPoi` 轮询获取兴趣点信息
- 实时播报文化导览内容

### 3. 暂停/恢复

- 点击"暂停播放"按钮时，音频正确暂停
- 点击"继续播放"时，从断点继续或播放下一条

### 4. 停止导航

- 停止导航时，立即停止当前音频播放
- 销毁音频上下文，释放资源
- 清空语音队列

## 🔍 调试建议

### 查看 TTS 日志

在微信开发者工具的控制台中，可以查看以下日志：

```
[TTS] ✓ 转换成功
[TTS] ▶ 开始播放
[TTS] 播放完成
[TTS] ⏸ 音频已暂停
[TTS] ⏹ 音频已停止
[TTS] 🗑 音频上下文已销毁
```

### 常见问题排查

1. **TTS 不工作**
   - 检查 manifest.json 是否配置了插件
   - 检查微信公众平台是否添加了插件
   - 查看控制台是否有 "[TTS] 微信同声传译插件不可用" 日志

2. **音频无法暂停**
   - 查看控制台是否有 "[TTS] 暂停音频失败" 错误
   - 确认 `this.audioContext` 是否存在

3. **内存泄漏**
   - 检查是否有未销毁的音频上下文
   - 查看控制台日志确认 "音频上下文已销毁"

## 📝 注意事项

### 1. 插件权限

确保在微信公众平台已添加"微信同声传译"插件：
- AppID: `wx069ba97219f66d99`
- 版本: `0.3.6`

### 2. 开发环境

如果在真机调试时遇到 80082 权限错误，可以临时注释 manifest.json 中的 plugins 配置，待插件审核通过后再启用。

### 3. 音频并发

微信小程序同一时间只能播放一个音频，新音频会自动停止旧音频。这是预期行为。

### 4. 网络依赖

微信同声传译插件需要网络连接才能工作，离线环境下会自动降级为 Toast 提示。

## 🚀 性能优化

1. **及时销毁**：音频播放完成后立即销毁上下文
2. **避免重复创建**：复用 `this.audioContext`
3. **错误容错**：所有操作都有 try-catch 保护
4. **资源清理**：停止导航时彻底清理所有音频资源

## 📊 测试清单

- [ ] 小说模式正常播报
- [ ] 文化导览模式正常播报
- [ ] 暂停功能正常工作
- [ ] 恢复功能正常工作
- [ ] 停止导航时音频正确停止
- [ ] 切换页面时资源正确释放
- [ ] 插件不可用时降级方案生效
- [ ] 控制台日志清晰可读

## 🔄 后续优化方向

1. 添加音量控制功能
2. 支持语速调节
3. 添加语音预加载机制
4. 优化音频队列管理
5. 支持多种语音音色选择

---

**更新日期**: 2026-04-06  
**优化版本**: v1.2.0  
**适用平台**: 微信小程序、H5
