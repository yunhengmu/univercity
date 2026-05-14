import request from '@/utils/request'

/**
 * 获取所有已上架书籍（分页）
 * @param {number} pageNum - 页码，默认 1
 * @param {number} pageSize - 每页数量，默认 10
 */
export function getShelvedBooks(pageNum = 1, pageSize = 10) {
  return request({
    url: '/upLog/books',
    method: 'get',
    params: { pageNum, pageSize },
    header: {
      isToken: false
    }
  })
}

/**
 * 提交上架记录
 * @param {number} bookId - 书籍ID
 * @param {number} userId - 用户ID
 */
export function submitUpLog(bookId, userId) {
  return request({
    url: '/upLog/submit',
    method: 'post',
    params: { bookId, userId },
    header: {
      isToken: false
    }
  })
}
