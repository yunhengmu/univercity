<template>
  <view class="guide-page">
    <!-- 顶部导航栏 -->
    <view class="guide-header">
      <button class="back-btn" @click="goBack">
        <uni-icons type="left" size="24" color="#1677ff"></uni-icons>
      </button>
      <text class="guide-title">{{ guideName || '文化导览' }}</text>
      <button class="delete-btn" @click="handleDeleteGuide">
        <uni-icons type="trash" size="20" color="#ef4444"></uni-icons>
        <text>删除</text>
      </button>
    </view>
    
    <!-- 删除确认弹窗 -->
    <view v-if="showDeleteModal" class="delete-modal-overlay" @click="closeDeleteModal">
      <view class="delete-modal" @click.stop>
        <view class="modal-header">
          <uni-icons type="error-filled" size="48" color="#ef4444"></uni-icons>
          <text class="modal-title">确认删除</text>
        </view>
        <view class="modal-body">
          <text class="modal-content">确定要删除吗？</text>
          <text class="modal-tip">删除后将无法恢复</text>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeDeleteModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>取消</text>
          </button>
          <button class="modal-btn confirm" @click="confirmDelete">
            <uni-icons type="trash" size="20" color="#ffffff"></uni-icons>
            <text>确定删除</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- 删除列表项确认弹窗 -->
    <view v-if="showDeleteItemModal" class="delete-modal-overlay" @click="closeDeleteItemModal">
      <view class="delete-modal" @click.stop>
        <view class="modal-header">
          <uni-icons type="error-filled" size="48" color="#ef4444"></uni-icons>
          <text class="modal-title">确认删除</text>
        </view>
        <view class="modal-body">
          <text class="modal-content">确定要删除此条内容吗？</text>
          <text class="modal-tip">删除后将无法恢复</text>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeDeleteItemModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>取消</text>
          </button>
          <button class="modal-btn confirm" @click="confirmDeleteItem">
            <uni-icons type="trash" size="20" color="#ffffff"></uni-icons>
            <text>确定删除</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- AI区域内容列表 -->
    <scroll-view class="content-list" scroll-y>
      <!-- 加载中状态 -->
      <view v-if="loading" class="loading-container">
        <uni-icons type="spinner-cycle" size="48" color="#3b82f6" class="loading-icon"></uni-icons>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 空状态 -->
      <view v-if="!loading && aiAreaList.length === 0" class="empty-container">
        <uni-icons type="info" size="64" color="#cbd5e1"></uni-icons>
        <text class="empty-text">暂无内容</text>
      </view>
      
      <!-- 内容列表 -->
      <view 
        v-for="(item, index) in aiAreaList" 
        :key="item.id"
        class="content-item"
        :class="item.type === 'IS_SELF' ? 'type-self' : 'type-no-self'"
      >
        <view class="item-header">
          <view class="type-badge" :class="item.type === 'IS_SELF' ? 'badge-self' : 'badge-no-self'">
            <uni-icons 
              :type="item.type === 'IS_SELF' ? 'person-filled' : 'person'" 
              size="16" 
              color="#ffffff"
            ></uni-icons>
            <text>{{ item.type === 'IS_SELF' ? '细览' : '总观' }}</text>
          </view>
          <button class="item-delete-btn" @click="handleDeleteItem(item.id)">
            <uni-icons type="trash" size="18" color="#ef4444"></uni-icons>
          </button>
        </view>
        <view class="item-content">
          <text class="content-text">{{ item.contentAi }}</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { deleteGuide, getAiAreaBook, deleteAiarea } from '@/api/normal/favorite'
import { Guide } from '@/vo/Guide'
import { Aiarea } from '@/vo/Aiarea'

export default {
  data() {
    return {
      guideId: null,
      guideName: '',
      aiAreaList: [],
      loading: false,
      showDeleteModal: false,
      showDeleteItemModal: false,
      currentDeleteItemId: null
    };
  },
  onLoad(options) {
    // 接收传递的 guideId 和 name 参数
    if (options.guideId) {
      this.guideId = options.guideId;
      this.guideName = options.name || '文化导览';
      // 加载AI区域内容
      this.loadAiAreaContent();
    } else {
      uni.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    }
  },
  methods: {
    /**
     * 返回上一页
     */
    goBack() {
      uni.navigateBack();
    },
    
    /**
     * 清理AI生成内容中的提示词和指令
     */
    cleanAiContent(content) {
      if (!content) return '';
      
      // 需要过滤的关键词模式
      const patterns = [
        /请从以下内容，提取文化点。/g,
        /【指令】用\s*\d+-\d+\s*字概括主要内容。/g,
        /【要求】/g,
        /-\s*思维发散，有文化.*?/g,
        /-\s*不要推理，直接出内容/g,
        /-\s*仅输出总结内容，不要其他文字/g,
        /-\s*采用机器朗读，去掉.*?等字符/g,
        /\n+/g  // 多个换行符
      ];
      
      let cleaned = content;
      patterns.forEach(pattern => {
        cleaned = cleaned.replace(pattern, '');
      });
      
      // 去除首尾空白和多余的空行
      return cleaned.trim().replace(/\n{3,}/g, '\n\n');
    },
    
    /**
     * 加载AI区域内容
     */
    async loadAiAreaContent() {
      if (!this.guideId) return;
      
      this.loading = true;
      
      try {
        const response = await getAiAreaBook(this.guideId);
        
        if (response && Array.isArray(response)) {
          // 将返回的数据转换为 Aiarea 对象数组，并清理内容
          this.aiAreaList = response.map(item => {
            const aiArea = new Aiarea(item);
            // 清理 contentAi 字段中的提示词和指令
            if (aiArea.contentAi) {
              aiArea.contentAi = this.cleanAiContent(aiArea.contentAi);
            }
            return aiArea;
          });
        } else {
          this.aiAreaList = [];
        }
      } catch (error) {
        console.error('加载AI区域内容失败:', error);
        uni.showToast({
          title: '加载内容失败',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 处理删除文化导览
     */
    handleDeleteGuide() {
      this.showDeleteModal = true;
    },
    
    /**
     * 关闭删除弹窗
     */
    closeDeleteModal() {
      this.showDeleteModal = false;
    },
    
    /**
     * 确认删除
     */
    async confirmDelete() {
      if (!this.guideId) return;
      
      try {
        await deleteGuide(this.guideId);
        
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        });
        
        // 关闭弹窗
        this.closeDeleteModal();
        
        // 延迟返回上一页
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);
      } catch (error) {
        console.error('删除文化导览失败:', error);
        uni.showToast({
          title: '删除失败，请重试',
          icon: 'none'
        });
        this.closeDeleteModal();
      }
    },
    
    /**
     * 处理删除列表项
     */
    handleDeleteItem(itemId) {
      this.currentDeleteItemId = itemId;
      this.showDeleteItemModal = true;
    },
    
    /**
     * 关闭删除列表项弹窗
     */
    closeDeleteItemModal() {
      this.showDeleteItemModal = false;
      this.currentDeleteItemId = null;
    },
    
    /**
     * 确认删除列表项
     */
    async confirmDeleteItem() {
      if (!this.currentDeleteItemId) return;
      
      try {
        await deleteAiarea(this.currentDeleteItemId);
        
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        });
        
        // 立即从列表中移除该项
        const index = this.aiAreaList.findIndex(item => item.id === this.currentDeleteItemId);
        if (index !== -1) {
          this.aiAreaList.splice(index, 1);
        }
        
        // 关闭弹窗
        this.closeDeleteItemModal();
      } catch (error) {
        console.error('删除AI区域内容失败:', error);
        uni.showToast({
          title: '删除失败，请重试',
          icon: 'none'
        });
        this.closeDeleteItemModal();
      }
    }
  }
};
</script>

<style lang="scss" scoped>
/* 页面容器 */
.guide-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  /* iPhone 刘海屏顶部安全区域适配 */
  padding-top: 100rpx;
}

/* 顶部导航栏 */
.guide-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 20rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(0, 0, 0, 0.03);
  border-radius: 0 0 24rpx 24rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn,
.delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 6rpx 16rpx;
  height: 52rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
  border: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.back-btn::before,
.delete-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.back-btn:active::before,
.delete-btn:active::before {
  width: 200rpx;
  height: 200rpx;
}

.back-btn {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  color: #475569;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08), inset 0 1rpx 0 rgba(255, 255, 255, 0.9);
  border: 1rpx solid rgba(0, 0, 0, 0.06);
}

.back-btn:active {
  transform: scale(0.96);
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.06);
}

.delete-btn {
  background: transparent;
  color: #ef4444;
  border: 1.5rpx solid #ef4444;
  box-shadow: none;
}

.delete-btn:active {
  transform: scale(0.96);
  background: rgba(239, 68, 68, 0.08);
}

.guide-title {
  flex: 1;
  text-align: center;
  font-size: 30rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1rpx;
}

/* 删除弹窗 */
.delete-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(8rpx);
  -webkit-backdrop-filter: blur(8rpx);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.delete-modal {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border-radius: 28rpx;
  padding: 40rpx 28rpx 28rpx;
  width: 560rpx;
  max-width: 85vw;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15), 0 0 0 1rpx rgba(255, 255, 255, 0.5) inset;
  animation: slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1rpx solid rgba(255, 255, 255, 0.8);
}

@keyframes slideUp {
  from {
    transform: translateY(40rpx) scale(0.95);
    opacity: 0;
  }
  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 32rpx;
}

.modal-content {
  font-size: 28rpx;
  color: #475569;
  text-align: center;
  line-height: 1.6;
  font-weight: 500;
}

.modal-tip {
  font-size: 24rpx;
  color: #94a3b8;
  text-align: center;
  font-weight: 400;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
}

.modal-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
  font-weight: 600;
  border: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.modal-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.modal-btn:active::before {
  width: 200rpx;
  height: 200rpx;
}

.modal-btn.cancel {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #64748b;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06), inset 0 1rpx 0 rgba(255, 255, 255, 0.8);
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.modal-btn.cancel:active {
  transform: scale(0.96);
  box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.04);
}

.modal-btn.confirm {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 14rpx rgba(239, 68, 68, 0.3), inset 0 1rpx 0 rgba(255, 255, 255, 0.2);
}

.modal-btn.confirm:active {
  transform: scale(0.96);
  box-shadow: 0 2rpx 10rpx rgba(239, 68, 68, 0.25);
}

/* 内容列表 */
.content-list {
  flex: 1;
  padding: 24rpx;
  overflow-y: auto;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  gap: 20rpx;
}

.loading-icon {
  animation: spin 1s cubic-bezier(0.4, 0, 0.2, 1) infinite;
  filter: drop-shadow(0 4rpx 8rpx rgba(59, 130, 246, 0.3));
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: 26rpx;
  color: #64748b;
  font-weight: 500;
  letter-spacing: 1rpx;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  gap: 20rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 1rpx;
}

/* 内容项 */
.content-item {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  -webkit-backdrop-filter: blur(10rpx);
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(0, 0, 0, 0.03);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-left: 5rpx solid transparent;
  position: relative;
  overflow: hidden;
}

.content-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.content-item:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
}

/* NO_SELF 类型 - 总观内容 - 蓝色系 */
.content-item.type-no-self {
  border-left-color: #3b82f6;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.04) 0%, rgba(255, 255, 255, 0.95) 100%);
}

/* IS_SELF 类型 - 细览内容 - 绿色系 */
.content-item.type-self {
  border-left-color: #10b981;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.04) 0%, rgba(255, 255, 255, 0.95) 100%);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.item-delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.08);
  border: 1.5rpx solid rgba(239, 68, 68, 0.2);
  padding: 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.item-delete-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.2);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.item-delete-btn:active::before {
  width: 100rpx;
  height: 100rpx;
}

.item-delete-btn:active {
  transform: scale(0.9);
  background: rgba(239, 68, 68, 0.15);
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 18rpx;
  border-radius: 50rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.12);
  letter-spacing: 0.5rpx;
  position: relative;
  overflow: hidden;
}

.type-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0) 100%);
  pointer-events: none;
}

.badge-no-self {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3), inset 0 1rpx 0 rgba(255, 255, 255, 0.2);
}

.badge-self {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.3), inset 0 1rpx 0 rgba(255, 255, 255, 0.2);
}

.item-content {
  padding: 12rpx 0;
}

.content-text {
  font-size: 28rpx;
  line-height: 1.8;
  color: #334155;
  word-break: break-word;
  letter-spacing: 0.5rpx;
  font-weight: 400;
}
</style>
