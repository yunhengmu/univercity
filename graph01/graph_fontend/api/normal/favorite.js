import request from '@/utils/request'

/**
 * 获取收藏书目列表
 * @param {number} userId - 用户 ID
 */
export function getFavoritesBook(userId) {
  return request({
    url: '/favorite/getFavoritesBook',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取收藏文化导览列表
 * @param {number} userId - 用户 ID
 */
export function getFavoritesGuide(userId) {
  return request({
    url: '/favorite/getFavoritesGuide',
    method: 'get',
    params: { userId }
  })
}

/**
 * 添加收藏
 * @param {Object} data - 收藏数据，包含 favoriteId, typeId, userId
 */
export function addFavorite(data) {
  return request({
    url: '/favorite/addFavorite',
    method: 'post',
    data
  })
}

/**
 * 删除收藏
 * @param {number} loveId - 收藏记录 ID (loveId)
 */
export function deleteFavorite(loveId) {
  return request({
    url: `/favorite/deleteFavorite?id=${loveId}`,
    method: 'delete'
  })
}

/**
 * 删除文化导览
 * @param {number} id - 文化导览 ID
 */
export function deleteGuide(id) {
  return request({
    url: '/favorite/deleteGuide',
    method: 'delete',
    params: { id }
  })
}

/**
 * 获取AI区域书籍内容
 * @param {number} id - 文化导览 ID
 */
export function getAiAreaBook(id) {
  return request({
    url: '/favorite/getAiAreaBook',
    method: 'get',
    params: { id }
  })
}

/**
 * 获取文化导览列表
 * @param {number} userId - 用户 ID
 */
export function getGuide(userId) {
  return request({
    url: '/favorite/getGuide',
    method: 'get',
    params: { userId }
  })
}

/**
 * 删除AI区域
 * @param {number} id - AI区域 ID
 */
export function deleteAiarea(id) {
  return request({
    url: '/favorite/deleteAiarea',
    method: 'delete',
    params: { id }
  })
}
