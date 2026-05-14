import request from '@/utils/request'

/**
 * 删除Redis缓存
 * @param {number} userId - 用户 ID
 */
export function deleteRedis(userId) {
  return request({
    url: '/api/deleteRedis',
    method: 'delete',
    params: { userId }
  })
}

/**
 * 设置导航状态
 * @param {number} userId - 用户 ID
 * @param {boolean} isNavigation - 是否导航
 */
export function isNavigation(userId, isNavigation) {
  return request({
    url: '/api/isNavigation',
    method: 'post',
    params: { userId, isNavigation }
  })
}

/**
 * 开始导航（无书籍）
 * @param {Object} data - 导航数据，包含 origin, destination, byWay
 */
export function startNavigationNoBook(data) {
  return request({
    url: '/api/startNavigationNoBook',
    method: 'post',
    data,
    timeout: 50000  // AI生成内容需要较长时间，设置为50秒
  })
}

/**
 * 获取POI信息
 * @param {string} longitude - 经度
 * @param {string} latitude - 纬度
 * @param {number} userId - 用户 ID
 * @param {string} cacheKey - 缓存键（可选）
 * @param {string} content - 个性化内容（可选）
 * @param {boolean} silentError - 是否静默处理错误（不显示 toast）
 */
export function getPoi(longitude, latitude, userId, cacheKey, content, silentError = false) {
  return request({
    url: '/api/getPoi',
    method: 'get',
    params: { longitude, latitude, userId, cacheKey, content },
    silentError: silentError
  })
}

/**
 * 获取区域信息
 * @param {string} longitude - 经度
 * @param {string} latitude - 纬度
 * @param {number} userId - 用户 ID
 * @param {string} cacheKey - 缓存键（可选）
 * @param {string} content - 个性化内容（可选）
 * @param {boolean} silentError - 是否静默处理错误（不显示 toast）
 */
export function getArea(longitude, latitude, userId, cacheKey, content, silentError = false) {
  return request({
    url: '/api/getArea',
    method: 'get',
    params: { longitude, latitude, userId, cacheKey, content },
    timeout: 160000,
    silentError: silentError
  })
}

/**
 * 获取用户文本信息
 * @param {Object} data - 导航数据，包含 origin, destination, type, wordsType
 */
export function getUserText(data) {
  return request({
    url: '/api/get-user',
    method: 'post',
    data
  })
}

/**
 * 获取章节信息
 * @param {Object} data - 章节数据，包含 bookId, trajectoryId, length
 */
export function getChapter(data) {
  return request({
    url: '/api/get-chapter',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 * @param {number} userId - 用户 ID
 */
export function getUser(userId) {
  return request({
    url: '/api/getUser',
    method: 'get',
    params: { userId }
  })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户数据
 */
export function updateUser(data) {
  return request({
    url: '/api/updateUser',
    method: 'post',
    data
  })
}

/**
 * 根据标签获取书籍
 * @param {number} userId - 用户 ID
 * @param {string} tags - 标签
 */
export function getBookByTags(userId, tags) {
  return request({
    url: '/api/getBookByTags',
    method: 'get',
    params: { userId, tags }
  })
}

/**
 * 删除书籍
 * @param {number} bookId - 书籍 ID
 */
export function deleteBook(bookId) {
  return request({
    url: '/api/deleteBook',
    method: 'delete',
    params: { bookId }
  })
}

/**
 * 获取书籍列表
 * @param {number} userId - 用户 ID
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 */
export function getBooks(userId, pageNum, pageSize) {
  return request({
    url: '/api/getBooks',
    method: 'get',
    params: { userId, pageNum, pageSize }
  })
}

/**
 * 获取章节列表
 * @param {number} bookId - 书籍 ID
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页数量
 */
export function getChapters(bookId, pageNum, pageSize) {
  return request({
    url: '/api/getChapters',
    method: 'get',
    params: { bookId, pageNum, pageSize },
    timeout: 160000  // AI生成内容需要较长时间，设置为160秒
  })
}

/**
 * 获取高德地图驾车路径规划
 * @param {Object} origin - 起点坐标 { longitude, latitude }
 * @param {Object} destination - 终点坐标 { longitude, latitude }
 * @returns {Promise} 返回路径数据
 */
export function getDrivingRoute(origin, destination) {
  const amapKey = process.env.AMAP_KEY || 'your-amap-key-here';
  const url = `https://restapi.amap.com/v3/direction/driving?origin=${origin.longitude},${origin.latitude}&destination=${destination.longitude},${destination.latitude}&key=${amapKey}`;
  
  return new Promise((resolve, reject) => {
    uni.request({
      url: url,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200 && res.data.status === '1') {
          resolve(res.data);
        } else {
          reject(new Error(res.data.info || '路径规划失败'));
        }
      },
      fail: (error) => {
        reject(error);
      },
      complete: () => {
        // 请求完成
      }
    });
  });
}
