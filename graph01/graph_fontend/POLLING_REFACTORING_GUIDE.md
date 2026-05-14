# index.vue 轮询管理改造指南

## 📋 改造目标

使用基于 Map 的 `pollingManager` 统一管理所有轮询任务，替代原有的多个独立定时器，实现：
- ✅ 停止导航时一键关闭所有轮询
- ✅ 防止内存泄漏和请求竞态条件
- ✅ 简化代码逻辑，提高可维护性

---

## 🔍 现状分析

### 当前存在的轮询场景

| 场景 | 位置 | 当前实现方式 | 问题 |
|------|------|-------------|------|
| 文化导览 getArea | L1284-1379 | `fetchCulturalInfo()` 递归 + setTimeout | 难以统一停止 |
| POI 轮询 getPoi | L3651-3863 | `startPoiPolling()` + setInterval | 需手动清理定时器 |
| 小说章节 getChapter | L1054, 1141 | setInterval (120秒) | 暂停/恢复逻辑复杂 |
| 继续听模式 | L1011, 1039, 1057 | 多次调用 pollTask | 无统一管理 |

### 当前停止导航的问题

```javascript
performStopNavigation() {
  // ❌ 需要手动清理多个定时器
  if (this.timer) { clearInterval(this.timer); this.timer = null; }
  if (this.culturalTimer) { clearInterval(this.culturalTimer); ... }
  if (this.poiRequestTimer) { clearInterval(this.poiRequestTimer); ... }
  
  // ❌ 可能遗漏某些请求
  this.requestTasks.forEach(task => task.abort());
  
  // ❌ 无法保证所有异步操作都已停止
}
```

---

## 🛠️ 改造步骤

### 步骤 1：在 data 中添加任务 ID 管理

```javascript
data() {
  return {
    
    // 🔧 新增：轮询任务 ID 管理
    culturalTaskId: null,      // 文化导览任务 ID
    poiTaskId: null,           // POI 轮询任务 ID
    chapterTaskId: null,       // 章节轮询任务 ID
  };
}
```

### 步骤 2：导入 pollingManager

确保已导入（L216）：
```javascript
import { startNavigationWithPoll, pollTask, startNavigation, getChapter, 
         getArea as getAreaAsync, getPoi as getPoiAsync, pollingManager } 
from '@/api/normal/navigation'
```

### 步骤 3：改造 fetchCulturalInfo 函数

**原函数位置**：L1284-1379

**改造后代码**：

```javascript
async fetchCulturalInfo() {
  // 状态检查
  if (!this.isNavigating || this.isPaused || this.mode !== 1) {
    console.log('[文化导览] ⚠️ 状态不满足，中止 fetchCulturalInfo');
    return;
  }
  
  // 如果正在使用 getPoi 轮询，就不要再调用 getArea 了
  if (this.isPoiRequesting && this.selectedPointLocation) {
    console.log('[文化导览] ⚠️ POI 轮询进行中，跳过 getArea');
    return;
  }
  
  try {
    let location = this.selectedPointLocation || this.userLocation;
    const params = {
      longitude: location.lng,
      latitude: location.lat,
      userId: 1
    };
    
    if (this.appliedContent) {
      params.content = this.appliedContent;
    }
    
    // 🔧 关键改造：生成唯一 taskId
    this.culturalTaskId = `cultural_${Date.now()}`;
    console.log('[文化导览] 🚀 启动轮询任务, taskId:', this.culturalTaskId);
    
    // 🔧 关键改造：使用 pollingManager 启动轮询
    await pollingManager.startPolling(
      this.culturalTaskId,
      // pollRequest 函数
      async (taskId) => {
        // 检查是否应该中止
        if (!this.isNavigating || this.isPaused || this.mode !== 1 || this.isPoiRequesting) {
          throw new Error('轮询已中止');
        }
        
        const response = await getAreaAsync(params);
        return response;
      },
      3000, // 轮询间隔 3 秒
      // shouldAbort 检查函数
      () => !this.isNavigating || this.isPaused || this.mode !== 1 || this.isPoiRequesting
    ).then(areaResponse => {
      // 处理响应结果
      if (areaResponse && typeof areaResponse === 'string') {
        console.log('[文化导览] ✓ 获取到导游词，长度:', areaResponse.length);
        this.addTextToSpeechQueue(areaResponse);
      } else if (areaResponse && typeof areaResponse === 'object') {
        const keys = Object.keys(areaResponse).filter(key => key !== '@type');
        if (keys.length > 0) {
          this.cacheKey = keys[0];
          const content = areaResponse[this.cacheKey];
          if (content && typeof content === 'string') {
            console.log('[文化导览] ✓ 从 Map 中提取内容');
            this.addTextToSpeechQueue(content);
          }
        }
      }
    }).catch(error => {
      console.error('[文化导览] ❌ 轮询失败:', error.message);
    });
    
  } catch (error) {
    console.error('[文化导览] ❌ 请求异常:', error);
  }
}
```

**改造要点**：
- ✅ 移除了递归调用 `setTimeout(() => this.fetchCulturalInfo(), 3000)`
- ✅ 使用 `pollingManager.startPolling()` 统一管理
- ✅ 通过 `shouldAbort` 函数实现即时中止
- ✅ 任务完成后自动清理，无需手动管理定时器

### 步骤 4：改造 startPoiPolling 函数

**原函数位置**：L3651-3760

**改造后代码**：

```javascript
startPoiPolling(lng, lat) {
  console.log('[POI 启动] 🚀 开始 POI 轮询');
  console.log('[POI 启动] 🔍 当前状态 - isNavigating:', this.isNavigating, ', mode:', this.mode);
      
  // 🔧 关键改造：先停止所有现有轮询任务
  console.log('[POI 启动] 🛑 第一步：停止所有现有轮询任务');
  pollingManager.stopAllPolling();
  this.stopAllRequests();
  
  // 等待一小段时间，确保所有异步操作完成
  setTimeout(() => {
    this._startPoiPollingInternal(lng, lat);
  }, 50);
},

_startPoiPollingInternal(lng, lat) {
  console.log('[POI 启动] 🔄 第二步：开始 POI 轮询内部流程');
      
  // 状态检查
  if (!this.isNavigating || this.mode !== 1) {
    console.warn('[POI 启动] ⚠️ 状态异常，中止 POI 启动');
    return;
  }
      
  // 清空语音队列
  console.log('[POI 启动] 🗑 第三步：强制清空所有语音队列');
  if (this.audioContext) {
    try {
      this.audioContext.stop();
      this.audioContext.destroy();
    } catch (e) {
      console.warn('[POI 启动] 销毁音频失败', e);
    }
    this.audioContext = null;
  }
      
  this.speechTextQueue = [];
  this.ttsConversionQueue.forEach(task => {
    if (task.status === 'pending' || task.status === 'converting') {
      task.status = 'cancelled';
    }
  });
  this.ttsConversionQueue = [];
  this.ttsAudioQueue.forEach(audio => {
    audio.cancelled = true;
  });
  this.ttsAudioQueue = [];
  this.currentSpeakingText = null;
  this.isTTSPlaying = false;
  this.isAudioPlaying = false;
  this.pendingClearQueue = false;
  this.isTTSConverting = false;
      
  console.log('[POI 启动] ✅ 第四步：已清空所有队列');
          
  // 保存 cacheKey
  this.savedCacheKey = this.cacheKey;
  console.log('[POI 启动] 💾 第五步：保存 getArea 的 cacheKey:', this.savedCacheKey);
        
  // 互斥控制
  this.culturalPollControl.enabled = false;
  this.poiPollControl.enabled = true;
  console.log('[POI 启动] 🚫 culturalPollControl.enabled:', this.culturalPollControl.enabled);
  console.log('[POI 启动] ✅ poiPollControl.enabled:', this.poiPollControl.enabled);
                
  this.isPoiRequesting = true;
  this.cacheKey = null;
  this.hasClearedSpeech = false;
      
  console.log('[POI 启动] ✅ 第六步：设置 POI 轮询状态');
  
  // 🔧 关键改造：生成唯一 taskId
  this.poiTaskId = `poi_${Date.now()}`;
  console.log('[POI 启动] 🆔 POI 任务 ID:', this.poiTaskId);
  
  // 🔧 关键改造：使用 pollingManager 启动轮询
  pollingManager.startPolling(
    this.poiTaskId,
    // pollRequest 函数
    async (taskId) => {
      // 检查状态
      if (!this.isPoiRequesting || !this.isNavigating || this.isPaused || this.mode !== 1) {
        throw new Error('POI 轮询已中止');
      }
      
      // 构建请求参数
      const params = {
        longitude: lng,
        latitude: lat,
        userId: 1,
        cacheKey: this.cacheKey
      };
      
      if (this.appliedContent) {
        params.content = this.appliedContent;
      }
      
      const response = await getPoiAsync(params);
      return response;
    },
    5000, // 轮询间隔 5 秒
    // shouldAbort 检查函数
    () => !this.isPoiRequesting || !this.isNavigating || this.isPaused || this.mode !== 1
  ).then(response => {
    // 处理响应
    console.log('[POI] ✅ 收到响应:', response);
    
    if (response && typeof response === 'object') {
      const keys = Object.keys(response).filter(key => key !== '@type');
      
      if (keys.length > 0) {
        const returnedKey = keys[0];
        this.cacheKey = returnedKey;
        console.log('[POI] 💾 更新 cacheKey:', this.cacheKey);
        
        const content = response[returnedKey];
        if (content && typeof content === 'string' && content.trim()) {
          console.log('[POI] ✅ 添加内容到语音队列');
          this.addTextToSpeechQueue(content);
        }
      }
    } else if (response && typeof response === 'string') {
      if (response.trim()) {
        this.addTextToSpeechQueue(response);
      }
    }
  }).catch(error => {
    console.error('[POI] ❌ 轮询失败:', error.message);
  });
  
  console.log('[POI 启动] ✅ POI 轮询已成功启动');
}
```

**改造要点**：
- ✅ 移除了 `setInterval` 定时器
- ✅ 使用 `pollingManager.startPolling()` 管理 POI 轮询
- ✅ 启动前先调用 `pollingManager.stopAllPolling()` 清理旧任务
- ✅ 通过 `shouldAbort` 实现即时中止

### 步骤 5：改造小说章节轮询

**原实现位置**：L1054-1075, L1141-1171

**改造方案**：

```javascript
// 在 handleNormalNavigation 或相关函数中
if (trajectoryId !== null && trajectoryId !== undefined && this.isNavigating) {
  // 生成唯一 taskId
  this.chapterTaskId = `chapter_${Date.now()}`;
  console.log('[小说章节] 🆔 章节任务 ID:', this.chapterTaskId);
  
  // 🔧 关键改造：使用 pollingManager 启动轮询
  pollingManager.startPolling(
    this.chapterTaskId,
    // pollRequest 函数
    async (taskId) => {
      if (!this.isNavigating || this.isPaused || this.mode !== 0) {
        throw new Error('章节轮询已中止');
      }
      
      const response = await getChapter({ 
        bookId: trajectoryId, 
        trajectoryId: trajectoryId, 
        length: 3000 
      });
      return response;
    },
    120000, // 轮询间隔 120 秒
    // shouldAbort 检查函数
    () => !this.isNavigating || this.isPaused || this.mode !== 0
  ).then(chapterText => {
    if (chapterText && typeof chapterText === 'string') {
      if (chapterText === '结束') {
        console.log('[小说章节] 🏁 小说已结束');
        uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
        // 可选：停止该任务的轮询
        pollingManager.stopPolling(this.chapterTaskId);
      } else {
        console.log('[小说章节] ✓ 添加章节内容到语音队列');
        this.addTextToSpeechQueue(chapterText);
      }
    }
  }).catch(error => {
    console.error('[小说章节] ❌ 轮询失败:', error.message);
  });
  
  // 保存当前的 bookId 和 trajectoryId
  this.currentBookId = { bookId: trajectoryId, trajectoryId: trajectoryId };
}
```

**暂停恢复时的改造**（L1139-1171）：

```javascript
// 恢复轮询时，重新启动 pollingManager 任务
if (this.isNavigating && this.currentBookId && this.mode === 0) {
  console.log('[小说章节] 🔄 恢复章节轮询');
  
  // 重新生成 taskId（因为之前的已被停止）
  this.chapterTaskId = `chapter_resumed_${Date.now()}`;
  
  pollingManager.startPolling(
    this.chapterTaskId,
    async (taskId) => {
      if (!this.isNavigating || this.isPaused || this.mode !== 0) {
        throw new Error('章节轮询已中止');
      }
      
      const response = await getChapter({ 
        bookId: this.currentBookId.bookId || this.currentBookId, 
        trajectoryId: this.currentBookId.trajectoryId || 0, 
        length: 3000 
      });
      return response;
    },
    120000,
    () => !this.isNavigating || this.isPaused || this.mode !== 0
  ).then(chapterText => {
    // 处理响应...
  });
}
```

### 步骤 6：改造 performStopNavigation 函数

**这是最关键的改造！**

**原函数位置**：L886-968

**改造后代码**：

```javascript
performStopNavigation() {
  console.log('[停止导航] 🛑 开始执行停止导航逻辑');
  console.log('[停止导航] 🔍 当前状态:');
  console.log('[停止导航]   - isNavigating:', this.isNavigating);
  console.log('[停止导航]   - mode:', this.mode);
  console.log('[停止导航]   - 活跃轮询任务数:', pollingManager.getActiveTaskCount());
  
  // 🔧 关键改造第零步：关闭 pollTask 外部控制开关（互斥机制）
  console.log('[停止导航] 🚫 第零步：关闭 pollTask 外部控制开关');
  this.culturalPollControl.enabled = false;
  this.poiPollControl.enabled = false;
  console.log('[停止导航] 🚫 culturalPollControl.enabled:', this.culturalPollControl.enabled);
  console.log('[停止导航] 🚫 poiPollControl.enabled:', this.poiPollControl.enabled);
  
  // 重置播放状态
  this.isAudioPlaying = false;
  this.pendingClearQueue = false;
  this.isTTSConverting = false;
  this.isPoiRequesting = false;
  
  // 🔧 关键改造第一步：立即设置 isNavigating = false
  this.isNavigating = false;
  console.log('[停止导航] ✅ 第一步：设置 isNavigating = false');
  
  // 🔧🔧🔧 关键改造第二步：一键停止所有轮询任务！
  console.log('[停止导航] 🛑 第二步：调用 pollingManager.stopAllPolling()');
  pollingManager.stopAllPolling();
  console.log('[停止导航] ✅ 所有轮询任务已停止，剩余任务数:', pollingManager.getActiveTaskCount());
  
  // 停止其他定时器（非轮询任务）
  console.log('[停止导航] 🛑 第三步：调用 stopAllRequests');
  this.stopAllRequests();
  
  // 清空语音队列
  console.log('[停止导航] 🗑 第四步：清空所有语音队列');
  this.clearAllSpeechContent();
  
  // 清空 TTS 双队列
  console.log('[停止导航] 🗑 第五步：清空 TTS 双队列');
  this.speechTextQueue = [];
  this.ttsConversionQueue = [];
  this.ttsAudioQueue = [];
  console.log('[停止导航] ✅ 已清空 speechTextQueue, ttsConversionQueue, ttsAudioQueue');
  
  // 清空状态变量
  this.continueListenBookId = null;
  this.showBackButton = false;
  this.savedCacheKey = null;
  this.currentBookId = null;
  this.cacheKey = null;
  this.culturalTaskId = null;
  this.poiTaskId = null;
  this.chapterTaskId = null;
  console.log('[停止导航] ✅ 第六步：清除所有状态变量');
  
  // 微信小程序特定清理
  // #ifdef MP-WEIXIN
  console.log('[停止导航] 📍 第七步：停止持续定位');
  this.stopPositionUpdate();
  
  if (this.audioContext) {
    try {
      console.log('[停止导航] ⏹ 停止当前音频');
      this.audioContext.stop();
    } catch (e) {
      console.warn('[停止导航] 停止音频失败', e);
    }
    try {
      console.log('[停止导航] 🗑 销毁音频上下文');
      this.audioContext.destroy();
    } catch (e) {
      console.warn('[停止导航] 销毁音频上下文失败', e);
    }
    this.audioContext = null;
  }
  // #endif
  
  console.log('[停止导航] ✅ 第十步：所有请求和队列已清理完成');
  console.log('[停止导航] 🎉 停止导航流程完成');
}
```

**改造要点**：
- ✅ **最关键**：添加了 `pollingManager.stopAllPolling()` 一键停止所有轮询
- ✅ 移除了手动清理多个定时器的代码（`timer`, `culturalTimer`, `poiRequestTimer`）
- ✅ 清空任务 ID 变量，防止重复使用
- ✅ 日志更清晰，便于调试

### 步骤 7：改造 handleBackToArea 函数

**原函数位置**：L4001-4031

**改造要点**：

```javascript
handleBackToArea() {
  if (this.isProcessing) {
    console.warn('[返回区域] ⚠️ 正在处理中，忽略本次点击');
    return;
  }
      
  if (!this.isNavigating || this.mode !== 1) {
    console.warn('[返回区域] ⚠️ 状态不满足，无法返回区域模式');
    return;
  }
      
  if (!this.showBackButton) {
    console.warn('[返回区域] ⚠️ 返回按钮未显示，无需操作');
    return;
  }
      
  console.log('[返回区域] 🔄 开始切换回文化导览模式');
  
  // 🔧 关键改造：停止 POI 轮询任务
  console.log('[返回区域] 🛑 第一步：停止 POI 轮询任务');
  if (this.poiTaskId) {
    pollingManager.stopPolling(this.poiTaskId);
    this.poiTaskId = null;
  }
  
  // 停止其他请求
  this.stopAllRequests();
  
  // 等待后执行内部流程
  setTimeout(() => {
    this._handleBackToAreaInternal();
  }, 50);
}
```

---

## 🎯 改造效果对比

### 改造前

```javascript
// ❌ 需要手动管理多个定时器
this.timer = setInterval(...);
this.culturalTimer = setTimeout(...);
this.poiRequestTimer = setInterval(...);

// ❌ 停止时需要逐个清理
if (this.timer) { clearInterval(this.timer); this.timer = null; }
if (this.culturalTimer) { clearInterval(this.culturalTimer); ... }
if (this.poiRequestTimer) { clearInterval(this.poiRequestTimer); ... }

// ❌ 可能遗漏某些请求
this.requestTasks.forEach(task => task.abort());
```

### 改造后

```javascript
// ✅ 使用 pollingManager 统一管理
pollingManager.startPolling(taskId, pollRequest, interval, shouldAbort);

// ✅ 停止时一键清理所有任务
pollingManager.stopAllPolling();

// ✅ 自动清理定时器和请求引用，防止内存泄漏
```

---

## ⚠️ 注意事项

### 1. 微信小程序兼容性

- ✅ `pollingManager` 已针对小程序环境优化，不使用 `AbortController`
- ✅ 使用 `clearInterval` 和状态标志实现中止
- ✅ 兼容微信小程序的异步请求机制

### 2. 视图层更新

- ✅ 停止轮询后，数据变更会触发 Vue 响应式更新
- ✅ 微信小程序中，修改 `data` 中的变量会自动更新视图
- ✅ 如需强制更新，可使用 `this.$forceUpdate()`（极少需要）

### 3. 任务 ID 唯一性

- ✅ 使用时间戳生成唯一 taskId：`` `cultural_${Date.now()}` ``
- ✅ 避免重复 taskId 导致任务冲突
- ✅ 每次启动新任务时生成新 ID

### 4. 互斥控制

- ✅ 保留 `culturalPollControl` 和 `poiPollControl` 外部控制开关
- ✅ 在 `shouldAbort` 基础上增加一层防护
- ✅ 实现立即中止，不等待异步请求返回

---

## 📊 预期收益

| 指标 | 改造前 | 改造后 | 提升 |
|------|--------|--------|------|
| 停止导航代码行数 | ~80 行 | ~50 行 | ↓ 37% |
| 定时器管理复杂度 | 高（3+个定时器） | 低（统一Map管理） | ↓ 显著 |
| 内存泄漏风险 | 中 | 低 | ↓ 显著 |
| 请求竞态条件 | 高 | 低 | ↓ 显著 |
| 代码可维护性 | 中 | 高 | ↑ 显著 |

---

## 🧪 测试建议

### 测试场景 1：正常停止导航

1. 启动文化导览模式
2. 点击"停止导航"
3. 观察控制台日志，确认：
   - ✅ `pollingManager.stopAllPolling()` 被调用
   - ✅ 所有轮询任务已停止
   - ✅ 无后续请求发出

### 测试场景 2：模式切换

1. 启动文化导览模式
2. 点击地图选择中心点，进入 POI 模式
3. 点击"文化导览点回来"
4. 观察控制台日志，确认：
   - ✅ POI 轮询任务已停止
   - ✅ 文化导览轮询重新启动
   - ✅ 无并发请求

### 测试场景 3：快速启停

1. 快速连续点击"开始导航"和"停止导航"
2. 观察控制台日志，确认：
   - ✅ 无并发请求
   - ✅ 旧任务被正确清理
   - ✅ 无内存泄漏警告

### 测试场景 4：长时间运行

1. 启动导航并运行 10 分钟以上
2. 观察内存使用情况
3. 停止导航
4. 确认：
   - ✅ 内存正常释放
   - ✅ 无后台请求继续运行

---

## 📝 总结

通过本次改造，你将获得：

1. ✅ **统一的轮询管理**：所有任务集中在 `pollingManager` 中
2. ✅ **一键停止能力**：调用 `stopAllPolling()` 即可停止所有任务
3. ✅ **防止内存泄漏**：自动清理定时器和请求引用
4. ✅ **简化代码逻辑**：减少 ~37% 的停止导航代码
5. ✅ **提高可维护性**：清晰的职责划分和日志输出

**下一步行动**：
1. 按照上述步骤逐步改造代码
2. 每改造一个函数后进行测试
3. 全部改造完成后进行集成测试
4. 观察实际运行效果，必要时调整

祝你改造顺利！🎉
