<template>
  <view class="form-item">
    <text class="label">{{ title }}</text>
    <view class="image-upload-container">
      <!-- 显示已选择的图片 -->
      <view v-if="imageUrl" class="image-preview">
        <image :src="imageUrl" mode="aspectFill" class="preview-image"></image>
        <view class="delete-btn" @click="onDelete">
          <uni-icons type="closeempty" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
      
      <!-- 选择图片按钮 -->
      <view v-else class="upload-btn" @click="chooseImage">
        <uni-icons type="camera" size="40" color="#999999"></uni-icons>
        <text class="upload-text">点击选择图片</text>
      </view>
    </view>
  </view>
</template>

<script>
import { fileUrl, uploadFile } from '@/utils/upload.js'

export default {
  name: 'ImageUploader',
  props: {
    title: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      imageUrl: '',      // 完整URL，用于显示
      fileName: '',      // 文件名，用于提交
      uploading: false
    }
  },
  methods: {
    // 选择图片
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          if (res.tempFilePaths && res.tempFilePaths.length > 0) {
            const filePath = res.tempFilePaths[0]
            await this.uploadImage(filePath)
          }
        },
        fail: (error) => {
          console.error('选择图片失败:', error)
          uni.showToast({
            title: '选择图片失败: ' + (error.errMsg || '未知错误'),
            icon: 'none',
            duration: 3000
          })
        }
      })
    },
    
    // 上传图片
    async uploadImage(filePath) {
      let loadingShown = false
      
      try {
        // 显示上传中提示
        uni.showLoading({
          title: '上传中...',
          mask: true
        })
        loadingShown = true
        
        // 上传文件到服务器
        this.uploading = true
        const res = await uploadFile(filePath)
        
        // 隐藏loading
        if (loadingShown) {
          uni.hideLoading()
          loadingShown = false
        }
        
        // 上传成功后，获取服务器返回的文件URL
        if (res && res.fileName) {
          // fileName可能是 /upload/xxx 或 upload/xxx 或 xxx
          let fileName = res.fileName
          
          // 确保fileName以 / 开头
          if (!fileName.startsWith('/')) {
            fileName = '/' + fileName
          }
          
          this.fileName = fileName  // 保存fileName用于提交
          this.imageUrl = fileUrl(fileName)  // 保存完整URL用于显示
          uni.showToast({
            title: '上传成功',
            icon: 'success'
          })
        } else {
          throw new Error('上传失败：未获取到文件名')
        }
      } catch (error) {
        // 隐藏loading
        if (loadingShown) {
          uni.hideLoading()
          loadingShown = false
        }
        
        console.error('图片上传失败:', error)
        uni.showToast({
          title: '上传失败: ' + (error.message || '未知错误'),
          icon: 'none',
          duration: 3000
        })
        // 上传失败时清空选择
        this.imageUrl = ''
      } finally {
        this.uploading = false
        // 确保loading被隐藏
        if (loadingShown) {
          uni.hideLoading()
        }
      }
    },
    onDelete() {
      this.imageUrl = ''
      this.fileName = ''
    },
    setImageUrl(url) {
      if (!url) {
        this.fileName = ''
        this.imageUrl = ''
        return
      }
      
      // 判断传入的是否是完整URL还是fileName
      if (url.startsWith('http://') || url.startsWith('https://')) {
        // 如果是完整URL，直接使用
        this.imageUrl = url
        // 尝试提取fileName（去掉baseUrl部分）
        const profileUploadIndex = url.indexOf('/profile/upload/')
        const uploadIndex = url.indexOf('/upload/')
        
        if (profileUploadIndex !== -1) {
          // /profile/upload/ 格式
          this.fileName = url.substring(profileUploadIndex + 1) // 保存 /profile/upload/xxx 格式
        } else if (uploadIndex !== -1) {
          // /upload/ 格式
          this.fileName = url.substring(uploadIndex + 1) // 保存 /upload/xxx 格式
        } else {
          this.fileName = url
        }
      } else {
        // 如果是fileName（不以http开头）
        this.fileName = url.startsWith('/') ? url : '/' + url
        this.imageUrl = fileUrl(this.fileName)  // 拼接成完整URL显示
      }
    },
    getFileName() {
      return this.fileName
    },
    setIsChoose(choose) {
      // 预留方法
    }
  }
}
</script>

<style scoped>
@import '@/static/css/popup.css';
.image-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20rpx;
}

/* 图片预览 */
.image-preview {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.delete-btn {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.8);
  transform: scale(1.1);
}

/* 上传按钮 */
.upload-btn {
  width: 200rpx;
  height: 200rpx;
  border: 3rpx dashed #d1d5db;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: #f9fafb;
  transition: all 0.3s ease;
  cursor: pointer;
}

.upload-btn:hover {
  border-color: #3b82f6;
  background: #eff6ff;
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.2);
}

.upload-text {
  font-size: 24rpx;
  color: #999999;
}
</style>
