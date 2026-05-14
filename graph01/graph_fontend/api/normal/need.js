import request from '@/utils/request'

/**
 * 获取需求列表
 * @param {number} userId - 用户 ID
 */
export function getNeeds(userId) {
  return request({
    url: '/need/getNeeds',
    method: 'get',
    params: { userId }
  })
}

/**
 * 添加需求
 * @param {Object} data - 需求数据
 */
export function addNeed(data) {
  return request({
    url: '/need/addNeed',
    method: 'post',
    data
  })
}

/**
 * 删除需求
 * @param {number} id - 需求 ID
 */
export function deleteNeed(id) {
  return request({
    url: '/need/deleteNeed',
    method: 'delete',
    params: { id }
  })
}

/**
 * 更新需求
 * @param {Object} data - 需求数据
 */
export function updateNeed(data) {
  return request({
    url: '/need/updateNeed',
    method: 'post',
    data
  })
}
