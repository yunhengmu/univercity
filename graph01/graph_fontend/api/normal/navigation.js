import request from '@/utils/request'




/**
 * 获取导航任务结果
 * @param {string} taskId - 任务 ID
 * @returns {Promise} 返回任务状态或结果
 */
export function getTaskResult(taskId) {
  return request({
    url: `/excute/task?taskId=${taskId}`,
    method: 'get'
  })
}









/**
 * 开始导航（异步任务）
 * @param {Object} data - 导航数据，包含 origin, destination, type, wordsType, byWay, userId
 * @returns {Promise} 返回 taskId
 */
export function startNavigation(data) {
  return request({
    url: '/excute/start-navigation',
    method: 'post',
    data
  })
}




/**
 * 获取章节（异步任务）
 * @param {Object} data - 章节数据，包含 bookId, trajectoryId, length
 * @returns {Promise} 返回 taskId
 */
export function getChapter(data) {
  return request({
    url: '/excute/get-chapter',
    method: 'post',
    data
  })
}







/**
 * 获取区域信息（异步任务）
 * @param {Object} params - 参数，包含 longitude, latitude, userId, cacheKey(可选), content(可选)
 * @returns {Promise} 返回 taskId
 */
export function getArea(params) {
  return request({
    url: '/excute/getArea',
    method: 'get',
    params
  })
}

/**
 * 获取POI信息（异步任务）
 * @param {Object} params - 参数，包含 longitude, latitude, userId, cacheKey(可选), content(可选)
 * @returns {Promise} 返回 taskId
 */
export function getPoi(params) {
  return request({
    url: '/excute/getPoi',
    method: 'get',
    params
  })
}







/**
 * 多任务并发轮询管理器（基于 Map）
 * 用于管理多个独立任务的轮询状态，支持启动、停止、清理等操作
 */
class PollingManager {
  constructor() {
    // 存储所有正在轮询的任务: Map<taskId, { timerId, controller, resolve, reject, isResolved }>
    this.pollingMap = new Map();
    console.log('[PollingManager] 🎯 轮询管理器已初始化');
  }

  /**
   * 启动单个任务的轮询
   * @param {string} taskId - 任务唯一标识
   * @param {Function} pollRequest - 轮询请求函数，接收taskId参数，返回Promise
   * @param {number} interval - 轮询间隔（毫秒），默认2000
   * @param {Function} shouldAbort - 可选的中止检查函数，返回 true 则中止轮询
   * @returns {Promise} 返回最终结果
   */
  startPolling(taskId, pollRequest = getTaskResult, interval = 2000, shouldAbort = null) {
    return new Promise((resolve, reject) => {
      console.log(`[PollingManager] 🚀 启动任务轮询, taskId: ${taskId}`);

      // 如果该任务已在轮询，先停止它
      if (this.pollingMap.has(taskId)) {
        console.warn(`[PollingManager] ⚠️ 任务 ${taskId} 已在轮询中，先停止旧任务`);
        this.stopPolling(taskId);
      }

      let pollCount = 0;
      let isResolved = false;
      let currentRequestTask = null;

      // 安全 resolve 函数
      const safeResolve = (value) => {
        if (!isResolved) {
          isResolved = true;
          this._cleanupTask(taskId, currentRequestTask);
          console.log(`[PollingManager] ✅ 任务 ${taskId} 成功解决`);
          resolve(value);
        } else {
          console.warn(`[PollingManager] ⚠️ 任务 ${taskId} 尝试重复 resolve，已忽略`);
        }
      };

      // 安全 reject 函数
      const safeReject = (error) => {
        if (!isResolved) {
          isResolved = true;
          this._cleanupTask(taskId, currentRequestTask);
          console.log(`[PollingManager] ❌ 任务 ${taskId} 失败:`, error.message);
          reject(error);
        } else {
          console.warn(`[PollingManager] ⚠️ 任务 ${taskId} 尝试重复 reject，已忽略`);
        }
      };

      // 创建定时器
      const timerId = setInterval(() => {
        // 检查是否应该中止
        if (shouldAbort && shouldAbort()) {
          console.log(`[PollingManager] 🚫 任务 ${taskId} 通过 shouldAbort 检查中止`);
          safeReject(new Error('轮询已中止'));
          return;
        }

        pollCount++;
        console.log(`[PollingManager] ⏰ 任务 ${taskId} 第${pollCount}次轮询`);

        // 执行轮询请求
        const requestPromise = pollRequest(taskId);

        // 保存当前请求任务（用于中止）并同步更新 Map
        if (requestPromise && typeof requestPromise.abort === 'function') {
          currentRequestTask = requestPromise;
          // 🔧 关键修复：同步更新 Map 中的 currentRequestTask 引用
          const taskInMap = this.pollingMap.get(taskId);
          if (taskInMap) {
            taskInMap.currentRequestTask = requestPromise;
          }
        }

        requestPromise.then(pollRes => {
          console.log(`[PollingManager] 📨 任务 ${taskId} 轮询响应:`, pollRes);

          // 请求完成后清除引用并同步更新 Map
          currentRequestTask = null;
          const taskInMap = this.pollingMap.get(taskId);
          if (taskInMap) {
            taskInMap.currentRequestTask = null;
          }

          // 请求返回后再次检查是否应该中止
          if (shouldAbort && shouldAbort()) {
            console.log(`[PollingManager] 🚫 任务 ${taskId} 请求返回后发现需要中止，丢弃结果`);
            safeReject(new Error('轮询已中止'));
            return;
          }

          if (pollRes.msg === 'PENDING') {
            console.log(`[PollingManager] ⏳ 任务 ${taskId} 进行中... (第${pollCount}次)`);
            // 任务进行中，继续轮询
          } else if (typeof pollRes.msg === 'string' && pollRes.msg.startsWith('ERROR:')) {
            console.error(`[PollingManager] ❌ 任务 ${taskId} 失败:`, pollRes.msg);
            safeReject(new Error(pollRes.msg));
          } else {
            console.log(`[PollingManager] ✅ 任务 ${taskId} 完成! (共轮询${pollCount}次)`);
            // 处理不同的返回类型
            let result = pollRes.data;

            console.log('[PollingManager] 原始返回数据:', pollRes);
            console.log('[PollingManager] data 字段:', result, '类型:', typeof result);
            console.log('[PollingManager] msg 字段:', pollRes.msg, '类型:', typeof pollRes.msg);

            // 🔧 关键修复：只从 data 字段读取实际数据，绝不从 msg 读取
            // msg 字段仅用于判断任务状态（PENDING/SUCCESS/ERROR），不是实际内容
            if (result && typeof result === 'object') {
              // 移除 @type 等元数据字段，提取实际的键值对
              const keys = Object.keys(result).filter(key => key !== '@type');
              if (keys.length > 0) {
                // 取第一个非 @type 的 key 对应的值
                result = result[keys[0]];
                console.log('[PollingManager] ✓ 从 Map 中提取内容, key:', keys[0]);
              }
            }

            // 🔧 关键修复：不再从 msg 获取内容，msg 只是状态标识
            // 如果 data 为空，说明没有实际内容，返回 null
            if (!result) {
              console.warn('[PollingManager] ⚠️ data 字段为空，无实际内容可朗读');
              result = null;
            }

            console.log('[PollingManager] 最终结果:', result, '类型:', typeof result);
            safeResolve(result);
          }
        }).catch(err => {
          // 如果是被中止的，不打印错误
          if (err && err.errMsg && err.errMsg.includes('abort')) {
            console.log(`[PollingManager] 🚫 任务 ${taskId} 请求已被中止`);
            return;
          }

          console.error(`[PollingManager] ❌ 任务 ${taskId} 第${pollCount}次轮询失败:`, err);
          safeReject(err);
        });
      }, interval);

      // 将任务信息存入 Map
      this.pollingMap.set(taskId, {
        timerId,
        controller: null, // 预留 controller 字段（小程序环境可能不支持 AbortController）
        currentRequestTask: null,
        isResolved: false
      });

      console.log(`[PollingManager] 📋 当前活跃任务数: ${this.pollingMap.size}`);
    });
  }

  /**
   * 停止单个任务的轮询
   * @param {string} taskId - 任务唯一标识
   */
  stopPolling(taskId) {
    const task = this.pollingMap.get(taskId);
    if (task) {
      console.log(`[PollingManager] 🛑 停止任务轮询, taskId: ${taskId}`);

      // 清除定时器
      if (task.timerId) {
        clearInterval(task.timerId);
      }

      // 中止当前请求（如果支持）
      if (task.currentRequestTask && typeof task.currentRequestTask.abort === 'function') {
        try {
          task.currentRequestTask.abort();
          console.log(`[PollingManager] 🚫 已中止任务 ${taskId} 的当前请求`);
        } catch (e) {
          console.warn(`[PollingManager] ⚠️ 中止任务 ${taskId} 请求失败:`, e);
        }
      }

      // 从 Map 中删除
      this.pollingMap.delete(taskId);
      console.log(`[PollingManager] 📋 当前活跃任务数: ${this.pollingMap.size}`);
    } else {
      console.warn(`[PollingManager] ⚠️ 任务 ${taskId} 不存在或已停止`);
    }
  }

  /**
   * 停止所有任务的轮询
   */
  stopAllPolling() {
    console.log(`[PollingManager] 🛑 停止所有任务轮询, 当前任务数: ${this.pollingMap.size}`);

    for (const [taskId, task] of this.pollingMap.entries()) {
      if (task.timerId) {
        clearInterval(task.timerId);
      }

      if (task.currentRequestTask && typeof task.currentRequestTask.abort === 'function') {
        try {
          task.currentRequestTask.abort();
          console.log(`[PollingManager] 🚫 已中止任务 ${taskId} 的请求`);
        } catch (e) {
          console.warn(`[PollingManager] ⚠️ 中止任务 ${taskId} 请求失败:`, e);
        }
      }
    }

    this.pollingMap.clear();
    console.log('[PollingManager] 📋 所有任务已清理');
  }

  /**
   * 获取当前活跃任务数量
   * @returns {number}
   */
  getActiveTaskCount() {
    return this.pollingMap.size;
  }

  /**
   * 检查任务是否正在轮询
   * @param {string} taskId - 任务唯一标识
   * @returns {boolean}
   */
  isPolling(taskId) {
    return this.pollingMap.has(taskId);
  }

  /**
   * 内部方法：清理任务资源
   * @private
   */
  _cleanupTask(taskId, currentRequestTask) {
    const task = this.pollingMap.get(taskId);
    if (task) {
      // 清除定时器
      if (task.timerId) {
        clearInterval(task.timerId);
        task.timerId = null;
      }

      // 中止当前请求
      if (currentRequestTask && typeof currentRequestTask.abort === 'function') {
        try {
          currentRequestTask.abort();
          console.log(`[PollingManager] 🚫 清理任务 ${taskId} 时中止请求`);
        } catch (e) {
          console.warn(`[PollingManager] ⚠️ 清理任务 ${taskId} 时中止请求失败:`, e);
        }
      }

      // 从 Map 中删除
      this.pollingMap.delete(taskId);
      console.log(`[PollingManager] 📋 当前活跃任务数: ${this.pollingMap.size}`);
    }
  }
}

// 创建全局单例
const pollingManager = new PollingManager();

/**
 * 通用轮询函数（基于 PollingManager 封装）
 * @param {Function} initRequest - 首次请求函数，返回包含taskId的Promise
 * @param {Function} pollRequest - 轮询请求函数，接收taskId参数，返回Promise
 * @param {number} interval - 轮询间隔时间（毫秒）
 * @param {Function} shouldAbort - 可选的中止检查函数，返回 true 则中止轮询
 * @returns {Promise} 返回最终结果
 */
export function pollTask(initRequest, pollRequest = getTaskResult, interval = 2000, shouldAbort = null) {
  console.log('[pollTask] 🚀 开始异步任务轮询（使用 PollingManager）');

  return new Promise((resolve, reject) => {
    // 1. 执行初始请求获取 taskId
    initRequest().then(res => {
      console.log('[pollTask] 📥 初始请求返回:', res);
      const taskId = res.msg;
      console.log('[pollTask] 🆔 获取到 taskId:', taskId);

      if (!taskId) {
        console.error('[pollTask] ❌ taskId 为空，无法轮询');
        reject(new Error('taskId 为空'));
        return;
      }

      // 2. 使用 PollingManager 启动轮询
      pollingManager.startPolling(taskId, pollRequest, interval, shouldAbort)
        .then(resolve)
        .catch(reject);
    }).catch(err => {
      console.error('[pollTask] ❌ 初始请求失败:', err);
      reject(err);
    });
  });
}

// 导出 PollingManager 实例，供外部直接管理任务
export { pollingManager };

/**
 * 开始导航并轮询获取结果
 * @param {Object} formData - 导航表单数据
 * @param {Function} onProgress - 进度回调（可选）
 * @param {number} pollInterval - 轮询间隔（毫秒），默认2000
 * @returns {Promise} 返回最终导航结果
 */
export function startNavigationWithPoll(formData, onProgress, pollInterval = 2000) {
  return new Promise((resolve, reject) => {
    // 1. 开始任务
    startNavigation(formData).then(res => {
      const taskId = res.msg;
      console.log('[startNavigationWithPoll] 🆔 获取到 taskId:', taskId);
      
      if (!taskId) {
        console.error('[startNavigationWithPoll] ❌ taskId 为空，无法轮询');
        reject(new Error('taskId 为空'));
        return;
      }
      
      // 2. 使用 PollingManager 启动轮询
      pollingManager.startPolling(taskId, getTaskResult, pollInterval, null)
        .then(result => {
          console.log('[startNavigationWithPoll] 轮询结果:', result, '类型:', typeof result);
          
          // PollingManager 已经处理了返回结果，直接使用
          console.log('[startNavigationWithPoll] 最终结果:', result, '类型:', typeof result);
          resolve(result);
        })
        .catch(err => {
          console.error('[startNavigationWithPoll] ❌ 轮询失败:', err);
          reject(err);
        });
    }).catch(err => {
      console.error('[startNavigationWithPoll] ❌ 初始请求失败:', err);
      reject(err);
    });
  });
}
