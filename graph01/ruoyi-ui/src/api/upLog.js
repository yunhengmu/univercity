import request from '@/utils/request'

// 查询上架记录列表
export function listUpLog(query) {
  return request({
    url: '/upLog/list',
    method: 'get',
    params: query
  })
}

// 查询上架记录详细
export function getUpLog(id) {
  return request({
    url: '/upLog/' + id,
    method: 'get'
  })
}

// 新增上架记录
export function addUpLog(data) {
  return request({
    url: '/upLog',
    method: 'post',
    data: data
  })
}

// 修改上架记录
export function updateUpLog(data) {
  return request({
    url: '/upLog',
    method: 'put',
    data: data
  })
}

// 删除上架记录
export function delUpLog(id) {
  return request({
    url: '/upLog/' + id,
    method: 'delete'
  })
}

// 批量删除上架记录
export function batchDelUpLog(ids) {
  return request({
    url: '/upLog/batch',
    method: 'delete',
    data: ids
  })
}

// 上架（将状态改为1）
export function shelveUpLog(id) {
  return request({
    url: '/upLog/shelve/' + id,
    method: 'put'
  })
}

// 下架（将状态改为0）
export function unshelveUpLog(id) {
  return request({
    url: '/upLog/unshelve/' + id,
    method: 'put'
  })
}

// 分页查询所有已上架的书籍列表(XML方式)
export function getShelvedBooksByXml(query) {
  return request({
    url: '/upLog/books',
    method: 'get',
    params: query
  })
}

// 分页查询所有上架记录(不带筛选条件)
export function listAllUpLog(query) {
  return request({
    url: '/upLog/all',
    method: 'get',
    params: query
  })
}

// 查询单个书籍信息
export function getOneBook(bookId) {
  return request({
    url: '/upLog/getOneBook',
    method: 'get',
    params: { bookId }
  })
}

// 查询书籍章节列表
export function getChapters(bookId) {
  return request({
    url: '/upLog/getChapters',
    method: 'get',
    params: { bookId }
  })
}
