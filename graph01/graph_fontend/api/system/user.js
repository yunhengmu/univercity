import upload from '@/utils/upload'
import request from '@/utils/request'

// 用户密码重置
export function updateUserPwd(oldPassword, newPassword) {
  const data = {
    oldPassword,
    newPassword
  }
  return request({
    url: '/system/user/profile/updatePwd',
    method: 'put',
    data: data
  })
}

// 查询用户个人信息
export function getUserProfile() {
  return request({
    url: '/system/user/profile',
    method: 'get'
  })
}

// 根据用户ID获取用户信息
export function getUser(userId) {
  return request({
    url: '/api/getUser',
    method: 'get',
    params: { userId }
  })
}

// 修改用户个人信息
export function updateUserProfile(data) {
  return request({
    url: '/system/user/profile',
    method: 'put',
    data: data
  })
}

// 用户头像上传
export function uploadAvatar(data) {
  return upload({
    url: '/system/user/profile/avatar',
    name: data.name,
    filePath: data.filePath
  })
}

// 更新用户信息（基于CustomerVo）
// @param {Object} data - 用户数据，包含 id, nickName, introduction, imageUrl
export function updateUser(data) {
  const requestData = {
    id: data.id,
    nickName: data.nickName,
    introduction: data.introduction,
    imageUrl: data.imageUrl
  }
  
  return request({
    url: '/api/updateUser',
    method: 'post',
    data: requestData
  })
}
