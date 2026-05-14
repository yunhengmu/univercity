# 多任务并发轮询管理器使用指南

## 📋 概述

`PollingManager` 是一个基于 Map 的多任务并发轮询管理器，用于管理多个独立任务的轮询状态。它支持：

- ✅ 同时管理多个任务的轮询
- ✅ 单独启动/停止指定任务
- ✅ 一键停止所有任务
- ✅ 防止重复轮询（自动停止旧任务）
- ✅ 请求中止支持
- ✅ 竞态条件防护

## 🚀 基本用法

### 1. 使用 pollTask 函数（推荐 - 兼容旧代码）

```javascript
import { pollTask } from '@/api/normal/navigation'
import { startNavigation, getTaskResult } from '@/api/normal/navigation'

// 启动导航并轮询结果
async function handleNavigation() {
  try {
    const result = await pollTask(
      () => startNavigation(formData),  // 初始请求
      getTaskResult,                     // 轮询请求
      2000,                              // 轮询间隔
      () => this.isNavigating === false  // 中止检查函数
    )
    
    console.log('导航结果:', result)
  } catch (error) {
    console.error('导航失败:', error)
  }
}
```

### 2. 直接使用 PollingManager（高级用法）

```javascript
import { pollingManager, startNavigation, getChapter, getTaskResult } from '@/api/normal/navigation'

// 场景1: 表格中每行都有独立任务
async function startRowTask(rowId, rowData) {
  try {
    // 1. 启动异步任务获取 taskId
    const initRes = await startNavigation(rowData)
    const taskId = initRes.msg
    
    // 2. 使用 PollingManager 轮询该任务
    const result = await pollingManager.startPolling(
      taskId,
      getTaskResult,
      2000,
      () => shouldAbort(rowId)  // 可选的中止检查
    )
    
    console.log(`任务 ${rowId} 完成:`, result)
    updateRowStatus(rowId, 'completed', result)
  } catch (error) {
    console.error(`任务 ${rowId} 失败:`, error)
    updateRowStatus(rowId, 'failed', error.message)
  }
}

// 场景2: 手动停止某个任务
function stopRowTask(rowId) {
  // 需要先获取该行的 taskId
  const taskId = getTaskIdByRowId(rowId)
  if (taskId) {
    pollingManager.stopPolling(taskId)
    updateRowStatus(rowId, 'stopped')
  }
}

// 场景3: 页面卸载时停止所有任务
onUnload() {
  pollingManager.stopAllPolling()
  console.log('页面卸载，已停止所有轮询任务')
}
```

## 📖 API 文档

### PollingManager 类方法

#### 1. `startPolling(taskId, pollRequest, interval, shouldAbort)`

启动单个任务的轮询。

**参数：**
- `taskId` (string): 任务唯一标识
- `pollRequest` (Function): 轮询请求函数，接收 taskId 参数，返回 Promise
- `interval` (number): 轮询间隔（毫秒），默认 2000
- `shouldAbort` (Function): 可选的中止检查函数，返回 true 则中止轮询

**返回：**
- Promise: 解析为最终结果

**示例：**
```javascript
const result = await pollingManager.startPolling(
  'task-123',
  getTaskResult,
  2000,
  () => this.isCancelled  // 当 isCancelled 为 true 时中止
)
```

#### 2. `stopPolling(taskId)`

停止单个任务的轮询。

**参数：**
- `taskId` (string): 任务唯一标识

**示例：**
```javascript
pollingManager.stopPolling('task-123')
```

#### 3. `stopAllPolling()`

停止所有任务的轮询。

**示例：**
```javascript
pollingManager.stopAllPolling()
```

#### 4. `getActiveTaskCount()`

获取当前活跃任务数量。

**返回：**
- number: 活跃任务数

**示例：**
```javascript
const count = pollingManager.getActiveTaskCount()
console.log(`当前有 ${count} 个任务在轮询`)
```

#### 5. `isPolling(taskId)`

检查任务是否正在轮询。

**参数：**
- `taskId` (string): 任务唯一标识

**返回：**
- boolean: 是否正在轮询

**示例：**
```javascript
if (pollingManager.isPolling('task-123')) {
  console.log('任务正在轮询中')
}
```

### pollTask 函数

这是为了保持向后兼容性而封装的函数，内部使用 `PollingManager`。

**参数：**
- `initRequest` (Function): 首次请求函数，返回包含 taskId 的 Promise
- `pollRequest` (Function): 轮询请求函数，默认 `getTaskResult`
- `interval` (number): 轮询间隔（毫秒），默认 2000
- `shouldAbort` (Function): 可选的中止检查函数

**返回：**
- Promise: 解析为最终结果

**示例：**
```javascript
const result = await pollTask(
  () => startNavigation(formData),
  getTaskResult,
  2000,
  () => this.isNavigating === false
)
```

## 💡 实际应用场景

### 场景1: 表格批量任务管理

```vue
<template>
  <view>
    <view v-for="row in tableData" :key="row.id">
      <text>{{ row.name }}</text>
      <text>{{ row.status }}</text>
      <button @click="startTask(row.id)">开始</button>
      <button @click="stopTask(row.id)">停止</button>
    </view>
    <button @click="stopAllTasks">停止全部</button>
  </view>
</template>

<script>
import { pollingManager, startNavigation, getTaskResult } from '@/api/normal/navigation'

export default {
  data() {
    return {
      tableData: [
        { id: 1, name: '任务1', status: 'pending' },
        { id: 2, name: '任务2', status: 'pending' },
        { id: 3, name: '任务3', status: 'pending' }
      ],
      taskMap: {} // 存储 rowId -> taskId 的映射
    }
  },
  
  methods: {
    async startTask(rowId) {
      const row = this.tableData.find(r => r.id === rowId)
      if (!row) return
      
      // 更新状态
      this.$set(row, 'status', 'polling')
      
      try {
        // 启动任务
        const initRes = await startNavigation({ /* 任务数据 */ })
        const taskId = initRes.msg
        
        // 保存映射
        this.taskMap[rowId] = taskId
        
        // 开始轮询
        const result = await pollingManager.startPolling(
          taskId,
          getTaskResult,
          2000,
          () => {
            // 检查是否应该中止
            const currentRow = this.tableData.find(r => r.id === rowId)
            return currentRow && currentRow.status === 'stopped'
          }
        )
        
        // 任务完成
        this.$set(row, 'status', 'completed')
        console.log(`任务 ${rowId} 完成:`, result)
        
      } catch (error) {
        this.$set(row, 'status', 'failed')
        console.error(`任务 ${rowId} 失败:`, error)
      }
    },
    
    stopTask(rowId) {
      const taskId = this.taskMap[rowId]
      if (taskId) {
        pollingManager.stopPolling(taskId)
        
        const row = this.tableData.find(r => r.id === rowId)
        if (row) {
          this.$set(row, 'status', 'stopped')
        }
        
        delete this.taskMap[rowId]
      }
    },
    
    stopAllTasks() {
      pollingManager.stopAllPolling()
      
      // 更新所有任务状态
      this.tableData.forEach(row => {
        if (row.status === 'polling') {
          this.$set(row, 'status', 'stopped')
        }
      })
      
      this.taskMap = {}
    }
  },
  
  onUnload() {
    // 页面卸载时清理所有任务
    pollingManager.stopAllPolling()
  }
}
</script>
```

### 场景2: 导航任务管理

```javascript
import { pollingManager, startNavigation, getTaskResult } from '@/api/normal/navigation'

export default {
  data() {
    return {
      isNavigating: false,
      currentTaskId: null,
      navigationResult: null
    }
  },
  
  methods: {
    async startNavigation() {
      if (this.isNavigating) {
        console.warn('已有导航任务在进行中')
        return
      }
      
      this.isNavigating = true
      this.navigationResult = null
      
      try {
        const result = await pollTask(
          () => startNavigation(this.formData),
          getTaskResult,
          2000,
          () => !this.isNavigating  // 当 isNavigating 为 false 时中止
        )
        
        this.navigationResult = result
        console.log('导航成功:', result)
        
      } catch (error) {
        if (error.message === '轮询已中止') {
          console.log('导航已取消')
        } else {
          console.error('导航失败:', error)
        }
      } finally {
        this.isNavigating = false
        this.currentTaskId = null
      }
    },
    
    cancelNavigation() {
      this.isNavigating = false
      
      // 如果有当前任务 ID，直接停止
      if (this.currentTaskId) {
        pollingManager.stopPolling(this.currentTaskId)
      }
    }
  },
  
  onUnload() {
    // 页面卸载时停止导航
    this.cancelNavigation()
    pollingManager.stopAllPolling()
  }
}
```

### 场景3: 多个异步任务类型混合管理

```javascript
import { pollingManager, getChapter, getArea, getPoi, getTaskResult } from '@/api/normal/navigation'

export default {
  data() {
    return {
      tasks: [] // 存储所有任务信息
    }
  },
  
  methods: {
    // 获取章节
    async fetchChapter(bookId, trajectoryId) {
      const taskId = `chapter-${bookId}-${trajectoryId}`
      
      try {
        const initRes = await getChapter({ bookId, trajectoryId, length: 10 })
        const realTaskId = initRes.msg
        
        const result = await pollingManager.startPolling(
          realTaskId,
          getTaskResult,
          2000
        )
        
        console.log('章节数据:', result)
        return result
        
      } catch (error) {
        console.error('获取章节失败:', error)
        throw error
      }
    },
    
    // 获取区域信息
    async fetchArea(longitude, latitude) {
      const taskId = `area-${longitude}-${latitude}`
      
      try {
        const initRes = await getArea({ longitude, latitude, userId: 1 })
        const realTaskId = initRes.msg
        
        const result = await pollingManager.startPolling(
          realTaskId,
          getTaskResult,
          2000
        )
        
        console.log('区域信息:', result)
        return result
        
      } catch (error) {
        console.error('获取区域信息失败:', error)
        throw error
      }
    },
    
    // 停止特定类型的任务
    stopTasksByType(type) {
      // type: 'chapter', 'area', 'poi'
      for (const [taskId, task] of pollingManager.pollingMap.entries()) {
        if (taskId.startsWith(type)) {
          pollingManager.stopPolling(taskId)
        }
      }
    }
  }
}
```

## ⚠️ 注意事项

### 1. 微信小程序兼容性

- ✅ `setInterval/clearInterval` 完全兼容
- ⚠️ `AbortController` 在小程序中可能不支持，代码中已做兼容处理
- ✅ Map 数据结构在小程序中完全支持

### 2. 视图层更新

在微信小程序中，确保数据变更后界面能正确响应：

```javascript
// ❌ 错误：直接修改数组元素
this.tableData[0].status = 'completed'

// ✅ 正确：使用 $set
this.$set(this.tableData[0], 'status', 'completed')

// ✅ 正确：替换整个对象
this.tableData.splice(0, 1, { ...this.tableData[0], status: 'completed' })
```

### 3. 内存泄漏防护

务必在页面卸载时清理所有任务：

```javascript
// Vue 页面
onUnload() {
  pollingManager.stopAllPolling()
}

// React 页面
useEffect(() => {
  return () => {
    pollingManager.stopAllPolling()
  }
}, [])
```

### 4. 任务 ID 唯一性

确保每个任务的 `taskId` 是唯一的，否则会导致冲突：

```javascript
// ✅ 好的做法：使用唯一标识
const taskId = `${type}-${id}-${timestamp}`

// ❌ 不好的做法：可能重复
const taskId = `task-${id}`
```

## 🔍 调试技巧

### 查看当前活跃任务

```javascript
console.log('活跃任务数:', pollingManager.getActiveTaskCount())

for (const [taskId, task] of pollingManager.pollingMap.entries()) {
  console.log(`任务 ${taskId}:`, task)
}
```

### 日志输出

`PollingManager` 内置了详细的日志输出，包括：

- 🚀 启动任务
- ⏰ 每次轮询
- 📨 收到响应
- ✅ 任务完成
- ❌ 任务失败
- 🛑 停止任务
- 📋 活跃任务数变化

可以通过控制台过滤这些日志进行调试。

## 📊 性能优化建议

1. **合理设置轮询间隔**：根据业务需求调整，不要过于频繁
2. **及时停止不需要的任务**：避免浪费资源
3. **批量操作时使用 stopAllPolling**：一次性清理所有任务
4. **监控活跃任务数**：避免同时运行过多任务

```javascript
// 监控任务数量
const count = pollingManager.getActiveTaskCount()
if (count > 10) {
  console.warn('警告：同时运行的任务数过多')
}
```

## 🎯 总结

`PollingManager` 提供了强大的多任务并发管理能力：

- **简单易用**：通过 `pollTask` 函数保持向后兼容
- **灵活控制**：可以直接使用 `pollingManager` 进行细粒度控制
- **安全可靠**：内置竞态条件防护和请求中止机制
- **易于调试**：详细的日志输出帮助排查问题

根据你的业务场景选择合适的用法即可！
