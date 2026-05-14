<template>
  <view class="community-page">
    <!-- 顶部导航栏 -->
    <view class="community-header">
      <text class="header-title">社区书架</text>
      <text class="header-subtitle">发现更多精彩书籍</text>
    </view>
    
    <!-- 书籍列表 -->
    <scroll-view 
      class="book-list" 
      scroll-y 
      @scrolltolower="loadMore"
      lower-threshold="100"
    >
      <!-- 加载中状态 -->
      <view v-if="loading && bookList.length === 0" class="loading-container">
        <uni-icons type="spinner-cycle" size="48" color="#3b82f6" class="loading-icon"></uni-icons>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 空状态 -->
      <view v-if="!loading && bookList.length === 0" class="empty-container">
        <uni-icons type="book" size="64" color="#cbd5e1"></uni-icons>
        <text class="empty-text">暂无书籍</text>
      </view>
      
      <!-- 书籍列表 -->
      <view class="books-grid">
        <view 
          v-for="(book, index) in bookList" 
          :key="book.id"
          class="book-card"
          @click="handleBookClick(book)"
        >
          <!-- 渐变背景装饰 -->
          <view class="card-decoration"></view>
          
          <!-- 书籍类型标签 -->
          <view v-if="book.type" class="type-tag" :class="'type-' + getTagClass(book.type)">
            <uni-icons 
              :type="getBookTypeIcon(book.type)" 
              size="18" 
              class="tag-icon"
            ></uni-icons>
            <text>{{ book.type }}</text>
          </view>
          
          <!-- 书籍信息区域 -->
          <view class="book-content">
            <text class="book-name">{{ book.name || '未命名书籍' }}</text>
            <view v-if="book.world" class="world-info">
              <uni-icons type="location-filled" size="14" color="#94a3b8"></uni-icons>
              <text>{{ book.world }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载更多提示 -->
      <view v-if="loading && bookList.length > 0" class="loading-more">
        <uni-icons type="spinner-cycle" size="24" color="#3b82f6" class="loading-icon-small"></uni-icons>
        <text class="loading-more-text">加载中...</text>
      </view>
      
      <!-- 没有更多数据 -->
      <view v-if="!hasMore && bookList.length > 0" class="no-more">
        <text class="no-more-text">没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { getShelvedBooks } from '@/api/normal/upLog'

export default {
  data() {
    return {
      bookList: [],
      loading: false,
      pageNum: 1,
      pageSize: 10,
      hasMore: true
    };
  },
  onLoad() {
    this.loadBooks();
  },
  onShow() {
    // 每次显示页面时刷新数据
    this.pageNum = 1;
    this.hasMore = true;
    this.loadBooks();
  },
  methods: {
    /**
     * 加载书籍列表
     */
    async loadBooks() {
      if (this.loading) return;
      
      this.loading = true;
      
      try {
        const response = await getShelvedBooks(this.pageNum, this.pageSize);
        
        if (response && response.rows) {
          const newBooks = response.rows;
          
          if (this.pageNum === 1) {
            this.bookList = newBooks;
          } else {
            this.bookList = [...this.bookList, ...newBooks];
          }
          
          // 判断是否还有更多数据
          // 只有当返回数据等于pageSize时，才认为可能还有更多
          this.hasMore = newBooks.length === this.pageSize;
        } else {
          if (this.pageNum === 1) {
            this.bookList = [];
          }
          this.hasMore = false;
        }
      } catch (error) {
        console.error('加载书籍列表失败:', error);
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
        // 加载失败时回退页码
        if (this.pageNum > 1) {
          this.pageNum--;
        }
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 加载更多
     */
    loadMore() {
      if (!this.hasMore || this.loading) return;
      
      this.pageNum++;
      this.loadBooks();
    },
    
    /**
     * 处理书籍点击
     */
    handleBookClick(book) {
      
      if (!book.id) {
        uni.showToast({
          title: '书籍ID不存在',
          icon: 'none'
        });
        return;
      }
      
      // 跳转到书籍详情页面
      uni.navigateTo({
        url: `/pages/book/book?bookId=${book.id}`,
        fail: (error) => {
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          });
        }
      });
    },
    
    /**
     * 根据书籍类型获取对应图标
     */
    getBookTypeIcon(type) {
      const iconMap = {
        '玄幻': 'star-filled',
        '奇幻': 'magic',
        '恐怖': 'warn-filled',
        '言情': 'heart-filled',
        '科幻': 'paperplane-filled',
        '都市': 'home-filled',
        '历史': 'clock-filled',
        '武侠': 'medal-filled'
      };
      return iconMap[type] || 'book-filled';
    },
    
    /**
     * 根据书籍类型获取标签样式类名
     */
    getTagClass(type) {
      const classMap = {
        '玄幻': 'fantasy',
        '奇幻': 'magic',
        '恐怖': 'horror',
        '言情': 'romance',
        '科幻': 'scifi',
        '都市': 'urban',
        '历史': 'history',
        '武侠': 'martial'
      };
      return classMap[type] || 'default';
    }
  }
};
</script>

<style lang="scss" scoped>
/* 页面容器 */
.community-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  /* iPhone 刘海屏顶部安全区域适配 */
  padding-top: 100rpx;
}

/* 顶部导航栏 */
.community-header {
  padding: 24rpx 32rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  box-shadow: 0 2rpx 20rpx rgba(0, 0, 0, 0.06), 0 0 0 1rpx rgba(0, 0, 0, 0.03);
  border-radius: 0 0 24rpx 24rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8rpx;
  letter-spacing: 1rpx;
}

.header-subtitle {
  display: block;
  font-size: 24rpx;
  color: #64748b;
  font-weight: 400;
  letter-spacing: 0.5rpx;
}

/* 书籍列表 */
.book-list {
  flex: 1;
  padding: 24rpx;
  overflow-y: auto;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

/* 书籍卡片 */
.book-card {
  position: relative;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 280rpx;
  display: flex;
  flex-direction: column;
}

.book-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4rpx;
  background: linear-gradient(90deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-card:active {
  transform: translateY(-4rpx) scale(0.98);
  box-shadow: 0 12rpx 32rpx rgba(59, 130, 246, 0.15);
}

.book-card:active::before {
  opacity: 1;
}

/* 卡片装饰背景 */
.card-decoration {
  position: absolute;
  top: -100rpx;
  right: -100rpx;
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

/* 类型标签 */
.type-tag {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  margin: 20rpx 20rpx 0;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 600;
  backdrop-filter: blur(10rpx);
  -webkit-backdrop-filter: blur(10rpx);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  align-self: flex-start;
}

.tag-icon {
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
}

/* 不同类型配色 */
.type-fantasy {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(96, 165, 250, 0.1) 100%);
  color: #2563eb;
}

.type-magic {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.15) 0%, rgba(167, 139, 250, 0.1) 100%);
  color: #7c3aed;
}

.type-horror {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(248, 113, 113, 0.1) 100%);
  color: #dc2626;
}

.type-romance {
  background: linear-gradient(135deg, rgba(236, 72, 153, 0.15) 0%, rgba(244, 114, 182, 0.1) 100%);
  color: #db2777;
}

.type-scifi {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.15) 0%, rgba(34, 211, 238, 0.1) 100%);
  color: #0891b2;
}

.type-urban {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15) 0%, rgba(251, 191, 36, 0.1) 100%);
  color: #d97706;
}

.type-history {
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.15) 0%, rgba(156, 163, 175, 0.1) 100%);
  color: #6b7280;
}

.type-martial {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(52, 211, 153, 0.1) 100%);
  color: #059669;
}

.type-default {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.15) 0%, rgba(129, 140, 248, 0.1) 100%);
  color: #6366f1;
}

/* 书籍内容区域 */
.book-content {
  position: relative;
  z-index: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  flex: 1;
}

.book-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  letter-spacing: 0.5rpx;
  min-height: 96rpx;
}

.world-info {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #94a3b8;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-top: 4rpx;
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

/* 加载更多 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 32rpx 0;
}

.loading-icon-small {
  animation: spin 1s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.loading-more-text {
  font-size: 24rpx;
  color: #64748b;
  font-weight: 400;
}

/* 没有更多 */
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx 0;
}

.no-more-text {
  font-size: 24rpx;
  color: #94a3b8;
  font-weight: 400;
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
</style>
