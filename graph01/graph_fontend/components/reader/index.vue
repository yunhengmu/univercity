<template>
  <view class="reader-container" :class="{ 'dark-mode': darkMode }">
    <!-- 顶部导航栏 -->
    <view class="reader-header">
      <button class="back-btn" @click="goBack">
        <uni-icons type="left" size="24" color="#1677ff"></uni-icons>
      </button>
      <text class="reader-title">{{ bookTitle || '阅读小说' }}</text>
      <view class="header-right">
        <button class="mode-btn" @click="toggleDarkMode">
          <uni-icons :type="darkMode ? 'sunny' : 'moon'" size="24" color="#1677ff"></uni-icons>
        </button>
      </view>
    </view>
    
    <!-- 章节内容区域 -->
    <scroll-view class="content-scroll" scroll-y @scrolltolower="loadMoreChapters">
      <!-- 章节列表 -->
      <view v-if="chapters.length > 0" class="chapters-list">
        <view 
          v-for="(chapter, index) in chapters" 
          :key="chapter.id"
          class="chapter-item"
          @click="selectChapter(index)"
          :class="{ active: currentChapterIndex === index }"
        >
          <text class="chapter-name">{{ chapter.name }}</text>
          <text class="chapter-words">{{ chapter.words }}字</text>
        </view>
      </view>
      
      <!-- 章节内容 -->
      <view v-if="currentChapter" class="chapter-content">
        <text class="content-title">{{ currentChapter.name }}</text>
        <text class="content-text">{{ currentChapter.text }}</text>
      </view>
      
      <!-- 加载中 -->
      <view v-if="loading" class="loading-more">
        <uni-load-more status="loading" :show-icon="true"></uni-load-more>
      </view>
      
      <!-- 没有更多内容 -->
      <view v-if="!hasMore && chapters.length > 0" class="no-more">
        <text>没有更多章节了</text>
      </view>
      
      <!-- 空状态 -->
      <view v-if="chapters.length === 0 && !loading" class="empty-state">
        <text class="empty-text">暂无章节内容</text>
      </view>
    </scroll-view>
    
    <!-- 底部导航 -->
    <view class="reader-footer">
      <button 
        class="footer-btn"
        @click="prevChapter"
        :disabled="currentChapterIndex <= 0"
      >
        <uni-icons type="left" size="20" color="#1677ff"></uni-icons>
        <text>上一章</text>
      </button>
      <text class="chapter-info">
        {{ currentChapterIndex + 1 }} / {{ chapters.length }}
      </text>
      <button 
        class="footer-btn"
        @click="nextChapter"
        :disabled="currentChapterIndex >= chapters.length - 1"
      >
        <text>下一章</text>
        <uni-icons type="right" size="20" color="#1677ff"></uni-icons>
      </button>
    </view>
  </view>
</template>

<script>
import config from '@/config'

export default {
  data() {
    return {
      bookId: '',
      bookTitle: '',
      chapters: [],
      currentChapterIndex: 0,
      currentChapter: null,
      pageNum: 1,
      pageSize: 10,
      loading: false,
      hasMore: true,
      darkMode: false
    };
  },
  onLoad(options) {
    // 接收传递的bookId参数
    if (options.bookId) {
      this.bookId = options.bookId;
      this.bookTitle = options.bookTitle || '';
      // 初始化加载章节
      this.loadChapters();
    }
  },
  computed: {
    // 计算当前章节
    currentChapter() {
      if (this.chapters.length > 0 && this.currentChapterIndex >= 0) {
        return this.chapters[this.currentChapterIndex];
      }
      return null;
    }
  },
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack();
    },
    
    // 切换夜间模式
    toggleDarkMode() {
      this.darkMode = !this.darkMode;
    },
    
    // 加载章节内容
    async loadChapters() {
      if (this.loading || !this.hasMore) return;
      
      this.loading = true;
      
      try {
        const response = await uni.request({
          url: `${config.baseUrl}/api/getChapters`,
          method: 'GET',
          data: {
            bookId: this.bookId,
            pageNum: this.pageNum,
            pageSize: this.pageSize
          }
        });
        
        if (response[1].statusCode === 200) {
          const newChapters = response[1].data || [];
          
          if (newChapters.length > 0) {
            // 添加新章节
            this.chapters = [...this.chapters, ...newChapters];
            // 如果是第一次加载，设置当前章节
            if (this.chapters.length === newChapters.length) {
              this.currentChapterIndex = 0;
            }
            // 增加页码
            this.pageNum++;
          } else {
            // 没有更多章节
            this.hasMore = false;
          }
        } else {
          uni.showToast({
            title: '加载章节失败',
            icon: 'none'
          });
        }
      } catch (error) {
        console.error('加载章节失败:', error);
        uni.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      } finally {
        this.loading = false;
      }
    },
    
    // 加载更多章节
    loadMoreChapters() {
      if (this.loading || !this.hasMore) return;
      this.loadChapters();
    },
    
    // 选择章节
    selectChapter(index) {
      this.currentChapterIndex = index;
    },
    
    // 上一章
    prevChapter() {
      if (this.currentChapterIndex > 0) {
        this.currentChapterIndex--;
      }
    },
    
    // 下一章
    nextChapter() {
      if (this.currentChapterIndex < this.chapters.length - 1) {
        this.currentChapterIndex++;
      } else if (this.hasMore) {
        // 如果已经到最后一章且还有更多章节，加载更多
        this.loadChapters();
      }
    }
  }
};
</script>

<style scoped>
.reader-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  transition: background-color 0.3s ease;
}

.reader-container.dark-mode {
  background-color: #1a1a1a;
}

.reader-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.reader-container.dark-mode .reader-header {
  background-color: #2a2a2a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.back-btn,
.mode-btn {
  padding: 8px;
  background: none;
  border: none;
}

.reader-title {
  font-size: 18px;
  font-weight: bold;
  color: #333333;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .reader-title {
  color: #ffffff;
}

.content-scroll {
  flex: 1;
  height: calc(100vh - 120px);
}

.chapters-list {
  padding: 16px;
}

.chapter-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.reader-container.dark-mode .chapter-item {
  background-color: #2a2a2a;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.chapter-item.active {
  background-color: #e6f7ff;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.3);
}

.reader-container.dark-mode .chapter-item.active {
  background-color: #1a365d;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.5);
}

.chapter-name {
  font-size: 16px;
  color: #333333;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .chapter-name {
  color: #ffffff;
}

.chapter-words {
  font-size: 14px;
  color: #999999;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .chapter-words {
  color: #666666;
}

.chapter-content {
  padding: 24px 16px;
  background-color: #ffffff;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.reader-container.dark-mode .chapter-content {
  background-color: #2a2a2a;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.content-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333333;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .content-title {
  color: #ffffff;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333333;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .content-text {
  color: #e0e0e0;
}

.loading-more {
  padding: 20px;
  text-align: center;
}

.no-more {
  padding: 20px;
  text-align: center;
  color: #999999;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .no-more {
  color: #666666;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-text {
  font-size: 16px;
  color: #999999;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .empty-text {
  color: #666666;
}

.reader-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #ffffff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  transition: background-color 0.3s ease, box-shadow 0.3s ease;
}

.reader-container.dark-mode .reader-footer {
  background-color: #2a2a2a;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.3);
}

.footer-btn {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  background-color: #f0f0f0;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.reader-container.dark-mode .footer-btn {
  background-color: #3a3a3a;
}

.footer-btn:disabled {
  opacity: 0.5;
}

.chapter-info {
  font-size: 14px;
  color: #666666;
  transition: color 0.3s ease;
}

.reader-container.dark-mode .chapter-info {
  color: #999999;
}
</style>