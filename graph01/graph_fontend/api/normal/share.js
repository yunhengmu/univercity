import request from '@/utils/request'

/**
 * 获取加密分享链接
 * @param {number} id - 分享 ID
 */
export function getShare(id) {
  return request({
    url: '/share/getShare',
    method: 'get',
    params: { id }
  })
}

/**
 * 提交分享
 * @param {string} share - 分享内容
 * @param {number} id - 分享 ID
 */
export function postShare(share, id) {
  return request({
    url: '/share/postShare',
    method: 'post',
    params: { share, id }
  })
}
