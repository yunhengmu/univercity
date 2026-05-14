<template>
  <view class="page" data-page="record">
    <!-- 筛选标签 -->
    <scroll-view class="filter-tabs" scroll-x enable-flex>
      <view class="filter-tabs-content">
        <view 
          v-for="tab in filterTabs" 
          :key="tab.value"
          class="filter-tab"
          :class="{ active: currentFilter === tab.value }"
          @click="selectFilter(tab.value)"
          hover-class="filter-tab-hover"
          :hover-stay-time="150"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 收藏子分类标签 - 仅在收藏页面显示 -->
    <scroll-view v-if="currentFilter === 'favorite'" class="sub-filter-tabs" scroll-x enable-flex>
      <view class="sub-filter-tabs-content">
        <view 
          v-for="tab in favoriteSubTabs" 
          :key="tab.value"
          class="sub-filter-tab"
          :class="{ active: currentFavoriteType === tab.value }"
          @click="selectFavoriteType(tab.value)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </scroll-view>
    
    <!-- 删除确认弹窗 -->
    <view v-if="showDeleteModal" class="delete-modal-overlay" @click="closeDeleteModal">
      <view class="delete-modal" @click.stop>
        <view class="modal-header">
          <uni-icons type="error-filled" size="48" color="#ef4444"></uni-icons>
          <text class="modal-title">确认删除</text>
        </view>
        <view class="modal-body">
          <text class="modal-content">确定要删除这条记录吗？</text>
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
    
    <!-- 继续听提示 - 点击继续听后显示 -->
    <view v-if="continueListenBookId" class="continue-listen-tip">
      <uni-icons type="checkmark" size="20" color="#52c41a"></uni-icons>
      <text>已选择继续听：{{ continueListenBookTitle }}</text>
      <button class="cancel-btn" @click="cancelContinueListen">取消</button>
    </view>
    
    <!-- 小说列表 -->
    <scroll-view 
      v-if="currentFilter === 'favorite' && currentFavoriteType === 'book'" 
      class="records-list" 
      scroll-y 
      @scrolltolower="loadMoreRecords"
    >
      <view 
        v-for="(record, index) in bookRecords" 
        :key="record.uniqueKey"
        class="record-item"
        :data-id="record.id"
        :data-loveid="record.loveId"
        :data-title="record.title"
        :data-typeid="record.typeId"
        :data-tags="JSON.stringify(record.tags || [])"
        :data-description="record.description || ''"
        @tap="onRecordTap"
        hover-class="record-item-hover"
      >
        <view class="record-header">
          <text class="record-title">{{ record.title }}</text>
          <view class="header-buttons">
            <button class="continue-listen-btn" @click.stop="selectContinueListen(record)">
              <uni-icons type="play" size="20" color="#ffffff"></uni-icons>
              <text>继续听</text>
            </button>
            <button class="publish-btn" @click.stop="() => publishBook(record.id)">
              <uni-icons type="upload" size="20" color="#ffffff"></uni-icons>
              <text>发布</text>
            </button>
            <button 
              class="cancel-favorite-btn" 
              :data-loveid="record.loveId"
              :data-id="record.id"
              @click.stop="onCancelFavoriteTap"
            >
              <uni-icons type="heart-filled" size="20" color="#ffffff"></uni-icons>
              <text>取消收藏</text>
            </button>
          </view>
        </view>
        <view class="record-tags">
          <view 
            v-for="tag in (record.tags || [])" 
            :key="tag"
            class="tag"
            :class="'tag-' + getTagClassName(tag)"
          >
            <uni-icons 
              :type="tag === '玄幻' ? 'star' : tag === '恐怖' ? 'warn' : tag === '言情' ? 'heart' : tag === '奇幻' ? 'magic' : 'tag'"
              size="16"
              :color="tag === '玄幻' ? '#1677ff' : tag === '恐怖' ? '#ff4d4f' : tag === '言情' ? '#fa8c16' : tag === '奇幻' ? '#722ed1' : '#64748b'"
              style="margin-right: 6rpx;"
            ></uni-icons>
            <text>{{ getTagLabel(tag) }}</text>
          </view>
          <view v-if="record.typeId !== undefined" class="tag tag-type">
            <uni-icons type="star" size="16" color="#722ed1" style="margin-right: 6rpx;"></uni-icons>
            <text>{{ getTypeLabel(record.typeId) }}</text>
          </view>
        </view>
        <text class="record-description">{{ record.description }}</text>
        <view class="record-stats">
          <text>
            <uni-icons type="clock" size="20" color="#64748b" style="margin-right: 6rpx;"></uni-icons>
            {{ record.duration }}
          </text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="bookRecords.length === 0" class="empty-records">
        <text class="empty-text">暂无小说收藏</text>
        <text class="empty-subtext">去收藏一些喜欢的小说吧！</text>
      </view>
    </scroll-view>
    
    <!-- 文化导览列表 -->
    <scroll-view 
      v-if="currentFilter === 'favorite' && currentFavoriteType === 'cultural'" 
      class="records-list" 
      scroll-y
    >
      <view 
        v-for="(record, index) in culturalRecords" 
        :key="record.uniqueKey"
        class="record-item"
        :data-id="record.id"
        :data-title="record.title"
        :data-typeid="record.typeId"
        :data-tags="JSON.stringify(record.tags || [])"
        :data-description="record.description || ''"
        @tap="onRecordTap"
        hover-class="record-item-hover"
      >
        <view class="record-header">
          <text class="record-title">{{ record.title }}</text>
          <view class="header-buttons">
            <button 
              class="cancel-favorite-btn" 
              :data-loveid="record.loveId"
              :data-id="record.id"
              @click.stop="onCancelFavoriteTap"
            >
              <uni-icons type="heart-filled" size="20" color="#ffffff"></uni-icons>
              <text>取消收藏</text>
            </button>
          </view>
        </view>
        <view class="record-tags">
          <view 
            v-for="tag in (record.tags || [])" 
            :key="tag"
            class="tag"
            :class="'tag-' + getTagClassName(tag)"
          >
            <uni-icons type="tag" size="16" color="#64748b" style="margin-right: 6rpx;"></uni-icons>
            <text>{{ getTagLabel(tag) }}</text>
          </view>
          <view v-if="record.typeId !== undefined" class="tag tag-type">
            <uni-icons type="star" size="16" color="#722ed1" style="margin-right: 6rpx;"></uni-icons>
            <text>{{ getTypeLabel(record.typeId) }}</text>
          </view>
        </view>
        <text class="record-description">{{ record.description }}</text>
        <view class="record-stats">
          <text>
            <uni-icons type="clock" size="20" color="#64748b" style="margin-right: 6rpx;"></uni-icons>
            {{ record.duration }}
          </text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="culturalRecords.length === 0" class="empty-records">
        <text class="empty-text">暂无文化导览收藏</text>
        <text class="empty-subtext">去收藏一些文化导览吧！</text>
      </view>
    </scroll-view>
    
    <!-- 非收藏页面的记录列表（全部、标签筛选） -->
    <scroll-view 
      v-if="currentFilter !== 'favorite'" 
      class="records-list" 
      scroll-y 
      @scrolltolower="loadMoreRecords"
    >
      <!-- 加载中状态 -->
      <view v-if="loadingMore && filteredRecords.length === 0" class="initial-loading">
        <uni-icons type="spinner-cycle" size="48" color="#3b82f6" class="loading-icon"></uni-icons>
        <text class="loading-text">加载中...</text>
      </view>
      
      <!-- 记录列表 -->
      
      <!-- 非收藏页面的记录（全部、标签筛选） -->
      <view 
        v-if="currentFilter !== 'favorite'"
        v-for="(record, index) in filteredRecords" 
        :key="record.uniqueKey"
        class="record-item"
        :data-id="record.id"
        :data-title="record.title"
        :data-typeid="record.typeId"
        :data-tags="JSON.stringify(record.tags || [])"
        :data-description="record.description || ''"
        @tap="onRecordTap"
        hover-class="record-item-hover"
      >
        <view class="record-header">
          <text class="record-title">{{ record.title }}</text>
          <view class="header-buttons">
            <button class="continue-listen-btn" @click.stop="selectContinueListen(record)">
              <uni-icons type="play" size="20" color="#ffffff"></uni-icons>
              <text>继续听</text>
            </button>
            <button 
              class="toggle-favorite-btn" 
              :data-id="record.id"
              @tap.stop="onToggleFavoriteTap"
            >
              {{ isRecordFavorite(record.id) ? '取消收藏' : '收藏' }}
            </button>
          </view>
        </view>
        <view class="record-tags">
          <view 
            v-for="tag in (record.tags || [])" 
            :key="tag"
            class="tag"
            :class="'tag-' + getTagClassName(tag)"
          >
            <uni-icons 
              :type="tag === '玄幻' ? 'star' : tag === '恐怖' ? 'warn' : tag === '言情' ? 'heart' : tag === '奇幻' ? 'magic' : 'tag'"
              size="16"
              :color="tag === '玄幻' ? '#1677ff' : tag === '恐怖' ? '#ff4d4f' : tag === '言情' ? '#fa8c16' : tag === '奇幻' ? '#722ed1' : '#64748b'"
              style="margin-right: 6rpx;"
            ></uni-icons>
            <text>{{ getTagLabel(tag) }}</text>
          </view>
        </view>
        <text class="record-description">{{ record.description }}</text>
        <view class="record-stats">
          <text>
            <uni-icons type="clock" size="20" color="#64748b" style="margin-right: 6rpx;"></uni-icons>
            {{ record.duration }}
          </text>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredRecords.length === 0" class="empty-records">
        <text class="empty-text">暂无记录</text>
        <text class="empty-subtext">开始你的第一次探索吧！</text>
      </view>
      
      <!-- 加载更多提示 -->
      <view v-if="loadingMore" class="loading-more">
        <text>加载中...</text>
      </view>
      
      <!-- 没有更多数据提示 -->
      <view v-if="!hasMore && filteredRecords.length > 0" class="no-more-data">
        <text>没有更多数据了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import config from '@/config'
import { getFavorites, getFavoritesBook, getFavoritesGuide, addFavorite, deleteFavorite, deleteGuide } from '@/api/normal/favorite'
import { submitUpLog } from '@/api/normal/upLog'
import { Love } from '@/vo/Love'
import { Favorite } from '@/vo/Favorite'

export default {
  data() {
    return {
      // 当前显示的记录
      records: [],
      // 分页相关
      pageNum: 1,
      pageSize: 10,
      total: 0,
      // 加载更多状态
      loadingMore: false,
      hasMore: true,
      // 筛选标签当前选中项
      currentFilter: 'all',
      // 筛选标签选项
      filterTabs: [
        { value: 'all', label: '全部' },
        { value: 'favorite', label: '收藏' },
        { value: '玄幻', label: '玄幻' },
        { value: '恐怖', label: '恐怖' },
        { value: '言情', label: '言情' },
        { value: '奇幻', label: '奇幻' },
        { value: '科幻', label: '科幻' },
        { value: '都市', label: '都市' }
      ],
      // 收藏子分类选项（去掉"全部"按钮）
      favoriteSubTabs: [
        { value: 'book', label: '小说' },
        { value: 'cultural', label: '文化导览' }
      ],
      // 当前收藏类型筛选（默认为小说）
      currentFavoriteType: 'book',
      // 收藏列表（用于快速查找）
      favorites: [],
      // 继续听的书籍 ID
      continueListenBookId: null,
      continueListenBookTitle: '',
      // 删除弹窗状态
      showDeleteModal: false,
      currentDeleteId: null
    };
  },
  computed: {
    /**
     * 小说收藏记录
     */
    bookRecords() {
      return this.records.filter(item => item.typeId === Love.BOOK)
    },
    
    /**
     * 文化导览收藏记录
     */
    culturalRecords() {
      return this.records.filter(item => item.typeId === Love.CULTURAL)
    },
    
    /**
     * 筛选轨迹记录（用于非收藏页面）
     */
    filteredRecords() {
      console.log('====== filteredRecords 计算属性被调用 ======')
      console.log('currentFilter:', this.currentFilter)
      console.log('this.records:', this.records)
      
      if (this.currentFilter === 'all') {
        console.log('返回全部记录，数量:', this.records?.length || 0)
        return this.records || []
      } else if (this.currentFilter === 'favorite') {
        // 收藏页面不使用这个计算属性，直接使用 bookRecords 和 culturalRecords
        return []
      } else {
        const result = (this.records || []).filter(record => 
          (record.tags || []).some(tag => tag.includes(this.currentFilter))
        )
        console.log('标签筛选结果数量:', result.length)
        return result
      }
    }
  },
  methods: {
    /**
     * 加载收藏列表 (同时加载小说和文化导览)
     */
    async loadFavorites() {
      try {
        console.log('====== 开始加载收藏列表 ======')
        
        // 并行加载小说和文化导览
        const [booksRes, guidesRes] = await Promise.all([
          getFavoritesBook(1),
          getFavoritesGuide(1)
        ])
        
        console.log('小说收藏数据:', booksRes)
        console.log('文化导览收藏数据:', guidesRes)
        
        const allRecords = []
        
        // 处理小说数据
        if (booksRes && Array.isArray(booksRes)) {
          const bookRecords = booksRes
            .filter(book => {
              const isValid = book && (book.name || book.title) && book.id;
              if (!isValid) {
                console.warn('过滤掉无效的小说数据:', book);
              }
              return isValid;
            })
            .map(book => {
              const record = {
                id: book.id,
                loveId: book.loveId || book.favoriteId || book.id,
                title: book.name || book.title,
                tags: [book.type || '玄幻'],
                description: book.core || '',
                distance: '',
                duration: '',
                favorite: false,
                path: [],
                typeId: Love.BOOK,
                uniqueKey: 'book-' + book.id  // 添加唯一标识
              };
              console.log('小说记录映射:', record);
              return record;
            });
          allRecords.push(...bookRecords);
          console.log('小说记录数量:', bookRecords.length);
        } else {
          console.warn('小说收藏数据为空或不是数组:', booksRes);
        }
        
        // 处理文化导览数据
        if (guidesRes && Array.isArray(guidesRes)) {
          const guideRecords = guidesRes
            .filter(guide => {
              const isValid = guide && (guide.favoriteId || guide.id);
              if (!isValid) {
                console.warn('过滤掉无效的文化导览数据:', guide);
              }
              return isValid;
            })
            .map(guide => {
              const recordId = guide.favoriteId || guide.id;
              // 确定 loveId：如果是收藏记录，应该使用 guide.id；如果 guide.id 就是收藏ID，则直接使用
              const loveId = guide.loveId || guide.id;
              const record = {
                id: recordId,
                loveId: loveId,
                title: guide.name || `导航_${recordId}`,
                tags: ['文化'],
                description: '',
                distance: '',
                duration: '',
                favorite: false,
                path: [],
                typeId: Love.CULTURAL,
                uniqueKey: 'cultural-' + recordId  // 添加唯一标识
              };
              console.log('文化导览记录映射:', record);
              return record;
            });
          allRecords.push(...guideRecords);
          console.log('文化导览记录数量:', guideRecords.length);
        } else {
          console.warn('文化导览收藏数据为空或不是数组:', guidesRes);
        }
        
        this.records = allRecords;
        console.log('总收藏记录数量:', this.records.length);
        console.log('所有收藏记录:', this.records);
      } catch (error) {
        console.error('加载收藏列表失败，错误详情:', error);
        uni.showToast({ title: '加载收藏失败', icon: 'none' });
      }
    },
    
    /**
     * 加载收藏书籍的详细信息
     */
    async loadFavoriteBooksDetails() {
      console.log('====== 开始加载收藏书籍详情 ======')
      
      if (!this.favorites || this.favorites.length === 0) {
        console.log('没有收藏数据，跳过加载')
        return
      }
      
      try {
        // 提取所有 favoriteId
        const bookIds = this.favorites.map(fav => fav.favoriteId).filter(id => id != null)
        console.log('需要加载的书籍 ID 列表:', bookIds)
        console.log('待加载书籍数量:', bookIds.length)
        
        if (bookIds.length === 0) {
          console.log('没有有效的书籍 ID，跳过加载')
          return
        }
        
        // 第三步：批量获取书籍详情
        console.log('开始并发请求书籍详情...')
        const bookDetailsPromises = bookIds.map(bookId => {
          return new Promise((resolve, reject) => {
            uni.request({
              url: `${config.baseUrl}/api/getBookDetail?bookId=${bookId}`,
              method: 'GET',
              success: (res) => {
                console.log(`书籍 ${bookId} 详情响应:`, res)
                if (res.statusCode === 200 && res.data) {
                  // 检查返回的数据格式
                  let bookData = res.data
                  if (bookData.data) {
                    bookData = bookData.data
                  }
                  resolve(bookData)
                } else {
                  console.warn(`获取书籍 ${bookId} 详情失败，状态码:`, res.statusCode)
                  resolve(null) // 如果某本书获取失败，返回 null
                }
              },
              fail: (err) => {
                console.error(`获取书籍 ${bookId} 详情失败:`, err)
                resolve(null)
              }
            })
          })
        })
        
        const bookDetails = await Promise.all(bookDetailsPromises)
        console.log('所有书籍详情请求完成:', bookDetails)
        
        // 过滤掉 null 值
        const validBookDetails = bookDetails.filter(book => book !== null)
        console.log('有效的书籍详情数量:', validBookDetails.length)
        
        // 第四步：将书籍详情与收藏记录关联
        const recordsWithDetails = []
        this.favorites.forEach(fav => {
          const bookDetail = validBookDetails.find(book => book.id === fav.favoriteId)
          if (bookDetail) {
            console.log(`找到书籍 ${fav.favoriteId} 的详情:`, bookDetail.name || bookDetail.title)
            recordsWithDetails.push({
              id: bookDetail.id,
              title: bookDetail.name || bookDetail.title,
              tags: [bookDetail.type],
              description: bookDetail.core,
              distance: '',
              duration: '',
              favorite: false,
              path: [],
              favoriteId: fav.id, // 收藏记录的 ID
              typeId: fav.typeId // 收藏类型
            })
          } else {
            console.warn(`未找到书籍 ID ${fav.favoriteId} 的详情`) 
          }
        })
        
        // 更新 records 数组用于显示
        this.records = recordsWithDetails
        console.log('====== 收藏书籍详情加载完成 ======')
        console.log('最终收藏记录列表:', this.records, '总数:', this.records.length)
        
      } catch (error) {
        console.error('加载收藏书籍详情失败，错误详情:', error)
        uni.showToast({ title: '加载书籍详情失败', icon: 'none' })
      }
    },
    
    /**
     * 处理取消收藏按钮点击（小程序兼容）
     */
    onCancelFavoriteTap(event) {
      console.log('====== 取消收藏按钮点击 ======')
      const dataset = event.currentTarget.dataset
      console.log('dataset:', dataset)
      
      const loveId = dataset.loveid
      const recordId = dataset.id
      
      console.log('loveId:', loveId, '类型:', typeof loveId)
      console.log('recordId:', recordId, '类型:', typeof recordId)
      
      if (!loveId && loveId !== 0) {
        console.error('loveId 无效')
        uni.showToast({
          title: '数据异常',
          icon: 'none'
        })
        return
      }
      
      // 查找对应的记录，确认是小说还是文化导览
      const record = this.records.find(r => r.id === recordId)
      console.log('找到的记录:', record)
      
      // 调用取消收藏函数
      this.cancelFavorite(loveId, recordId)
    },
    
    /**
     * 取消收藏
     */
    async cancelFavorite(loveId, recordId) {
      console.log('====== 取消收藏函数被调用 ======')
      console.log('传入的 loveId:', loveId, '类型:', typeof loveId)
      console.log('传入的 recordId:', recordId, '类型:', typeof recordId)
      
      try {
        console.log('取消收藏，收藏记录 ID (loveId):', loveId)
        
        // 查找对应的记录，判断是小说还是文化导览
        const record = this.records.find(r => r.id === recordId)
        console.log('找到的记录:', record)
        
        // 根据记录类型调用不同的删除接口
        if (record && record.typeId === Love.CULTURAL) {
          // 文化导览使用 deleteGuide 接口
          console.log('取消文化导览收藏，调用 deleteGuide 接口，recordId:', recordId)
          await deleteGuide(recordId)
        } else {
          // 小说使用 deleteFavorite 接口
          console.log('取消小说收藏，调用 deleteFavorite 接口，loveId:', loveId)
          await deleteFavorite(loveId)
        }
        
        // 从 favorites 数组中移除
        const index = this.favorites.findIndex(fav => fav.id === loveId)
        if (index !== -1) {
          this.favorites.splice(index, 1)
        }
        
        // 从 records 数组中移除
        const recordIndex = this.records.findIndex(record => record.id === recordId)
        if (recordIndex !== -1) {
          this.records.splice(recordIndex, 1)
        }
        
        uni.showToast({ title: '已取消收藏', icon: 'success' })
      } catch (error) {
        console.error('取消收藏失败:', error)
        uni.showToast({ title: '取消收藏失败', icon: 'none' })
      }
    },
    
    /**
     * 切换收藏状态
     */
    async toggleFavorite(recordId) {
      console.log('====== 切换收藏状态 ======');
      console.log('recordId:', recordId);
      console.log('currentFilter:', this.currentFilter);
      console.log('当前 records 数量:', this.records.length);
      
      // 根据当前选中的收藏类型来判断
      const currentType = this.currentFavoriteType === 'book' ? Love.BOOK : Love.CULTURAL;
      
      // 检查是否已经收藏
      const existingFavorite = this.favorites.find(fav => fav.favoriteId === recordId && fav.typeId === currentType)
      
      if (existingFavorite) {
        // 如果已收藏，取消收藏
        await this.cancelFavorite(existingFavorite.id, recordId)
      } else {
        // 如果未收藏，添加收藏
        try {
          const data = {
            favoriteId: recordId,
            typeId: currentType,
            userId: 1
          }
          await addFavorite(data)
          
          // 添加到收藏列表
          const newFavorite = new Favorite({
            id: Date.now(),
            favoriteId: recordId,
            typeId: currentType,
            userId: 1
          })
          this.favorites.push(newFavorite)
          
          // 从当前 records 数组中删除该项
          const recordIndex = this.records.findIndex(record => record.id === recordId)
          console.log('查找记录索引:', recordIndex);
          if (recordIndex !== -1) {
            console.log('移除前 records 数量:', this.records.length);
            this.records.splice(recordIndex, 1)
            console.log('移除后 records 数量:', this.records.length);
            console.log('已移除的记录 ID:', recordId);
            
            // 强制触发视图更新
            this.$forceUpdate()
          } else {
            console.warn('未找到要移除的记录，recordId:', recordId);
          }
          
          uni.showToast({ 
            title: '已加入收藏', 
            icon: 'success',
            duration: 1500
          })
          
          // 提示用户去收藏页面查看
          setTimeout(() => {
            uni.showModal({
              title: '收藏成功',
              content: '已添加到收藏夹，是否前往查看？',
              success: (res) => {
                if (res.confirm) {
                  // 切换到收藏标签
                  this.selectFilter('favorite')
                }
              }
            })
          }, 1500)
        } catch (error) {
          console.error('添加收藏失败:', error)
          uni.showToast({ title: '添加收藏失败', icon: 'none' })
        }
      }
    },
    
    /**
     * 检查记录是否已收藏
     */
    isRecordFavorite(recordId) {
      // 根据当前选中的收藏类型来判断
      const currentType = this.currentFavoriteType === 'book' ? Love.BOOK : Love.CULTURAL;
      return this.favorites.some(fav => fav.favoriteId === recordId && fav.typeId === currentType)
    },
    
    /**
     * 处理收藏按钮点击（小程序兼容）
     */
    onToggleFavoriteTap(event) {
      const recordId = event.currentTarget.dataset.id;
      console.log('====== 收藏按钮点击 ======');
      console.log('recordId from dataset:', recordId);
      
      if (!recordId && recordId !== 0) {
        console.error('recordId 无效');
        uni.showToast({
          title: '数据异常',
          icon: 'none'
        });
        return;
      }
      
      this.toggleFavorite(recordId);
    },
    
    /**
     * 处理记录点击事件（小程序兼容）
     */
    onRecordTap(event) {
      const dataset = event.currentTarget.dataset;
      
      // 从 data 属性中重建 record 对象
      const record = {
        id: dataset.id,
        title: dataset.title,
        typeId: parseInt(dataset.typeid),
        tags: JSON.parse(dataset.tags || '[]'),
        description: dataset.description || '',
        distance: '',
        duration: '',
        favorite: false,
        path: []
      };
      
      console.log('====== 点击记录 ======');
      console.log('record:', record);
      console.log('currentFilter:', this.currentFilter);
      
      this.handleRecordClick(record);
    },
    
    /**
     * 处理记录点击事件
     */
    handleRecordClick(record) {
      console.log('====== 点击记录 ======');
      console.log('record:', record);
      console.log('currentFilter:', this.currentFilter);
      
      if (this.currentFilter === 'favorite') {
        // 收藏页面，根据类型跳转
        this.navigateToFavoritedItem(record)
      } else {
        // 非收藏页面，跳转到书籍详情
        if (!record.id) {
          console.error('记录 ID 不存在，无法跳转');
          uni.showToast({
            title: '数据异常，无法跳转',
            icon: 'none'
          });
          return;
        }
        this.navigateToBookDetail(record.id)
      }
    },
    
    /**
     * 跳转到书籍详情或文化导览
     */
    navigateToFavoritedItem(record) {
      console.log('====== 准备跳转收藏项 ======');
      console.log('record:', record);
      console.log('typeId:', record.typeId);
      console.log('Love.BOOK:', Love.BOOK, 'Love.CULTURAL:', Love.CULTURAL);
      console.log('showDeleteModal:', this.showDeleteModal);
      console.log('continueListenBookId:', this.continueListenBookId);
      
      if (!record.id) {
        console.error('记录 ID 不存在，无法跳转');
        uni.showToast({
          title: '数据异常，无法跳转',
          icon: 'none'
        });
        return;
      }
      
      // 如果是小说 (typeId 为 0),跳转到 book/book 页面
      if (record.typeId === Love.BOOK) {
        console.log('跳转到书籍详情页，bookId:', record.id);
        this.navigateToBookDetail(record.id)
      } else if (record.typeId === Love.CULTURAL) {
        // 如果是文化导览，跳转到 guide/guide 页面
        console.log('准备跳转到文化导览页面，guideId:', record.id)
        uni.navigateTo({
          url: `/pages/guide/guide?guideId=${record.id}`,
          success: (res) => {
            console.log('跳转成功:', res)
          },
          fail: (error) => {
            console.error('跳转失败:', error)
            uni.showToast({
              title: `跳转失败：${error.errMsg}`,
              icon: 'none',
              duration: 3000
            })
          }
        })
      } else {
        console.error('未知的 typeId:', record.typeId);
        uni.showToast({
          title: '未知的内容类型',
          icon: 'none'
        });
      }
    },
    
    /**
     * 选择筛选标签
     */
    selectFilter(filter) {
      console.log('====== 切换筛选标签 ======');
      console.log('从', this.currentFilter, '切换到', filter);
      console.log('当前 records 数量:', this.records.length);
      
      this.currentFilter = filter;
      // 重置分页参数
      this.pageNum = 1;
      this.hasMore = true;
      this.loadingMore = false;
      
      // 关键：先清空 records，防止数据混乱
      this.records = [];
      console.log('已清空 records，准备加载新数据');
      
      // 如果切换到收藏标签，加载收藏列表
      if (filter === 'favorite') {
        console.log('加载收藏列表');
        this.loadFavorites();
      } else if (filter === 'all') {
        // 切换到全部，重新加载所有书籍数据
        console.log('加载全部书籍数据');
        this.loadBooks();
      } else {
        // 切换到其他标签，也重新加载书籍数据
        console.log('加载标签筛选的书籍数据:', filter);
        this.loadBooks();
      }
    },
    
    /**
     * 选择收藏类型（只需切换显示，不需要重新加载数据）
     */
    selectFavoriteType(type) {
      this.currentFavoriteType = type;
      console.log('切换到收藏类型:', type);
    },
    
    /**
     * 创建新记录
     */
    createNewRecord() {
      uni.showToast({
        title: '创建新记录',
        icon: 'success'
      });
      // 跳转到第一页
      uni.switchTab({ url: '/pages/index/index' });
    },
    
    /**
     * 删除记录
     */
    deleteRecord(id) {
      this.currentDeleteId = id;
      this.showDeleteModal = true;
    },
    
    /**
     * 关闭删除弹窗
     */
    closeDeleteModal() {
      this.showDeleteModal = false;
      this.currentDeleteId = null;
    },
    
    /**
     * 确认删除
     */
    confirmDelete() {
      if (!this.currentDeleteId) return;
      
      // 发送 DELETE 请求到后端接口
      uni.request({
        url: `${config.baseUrl}/api/deleteBook?bookId=${this.currentDeleteId}`,
        method: 'DELETE',
        success: (response) => {
          // 从 records 数组中删除记录
          const index = this.records.findIndex(record => record.id === this.currentDeleteId);
          if (index !== -1) {
            this.records.splice(index, 1);
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            });
          }
          this.closeDeleteModal();
        },
        fail: (error) => {
          console.error('删除失败:', error);
          uni.showToast({
            title: '删除失败，请重试',
            icon: 'none'
          });
          this.closeDeleteModal();
        }
      });
    },
    
    /**
     * 跳转到书籍详情页面
     */
    navigateToBookDetail(bookId) {
      console.log('====== 准备跳转到书籍详情页面 ======');
      console.log('bookId:', bookId);
      console.log('bookId 类型:', typeof bookId);
      console.log('跳转路径:', `/pages/book/book?bookId=${bookId}`);
      
      // 校验 bookId 是否有效
      if (!bookId && bookId !== 0) {
        console.error('bookId 无效，无法跳转');
        uni.showToast({
          title: '书籍ID无效，无法跳转',
          icon: 'none'
        });
        return;
      }
      
      uni.navigateTo({
        url: `/pages/book/book?bookId=${bookId}`,
        success: (res) => {
          console.log('跳转成功:', res)
        },
        fail: (error) => {
          console.error('跳转失败:', error)
          uni.showToast({
            title: `跳转失败：${error.errMsg}`,
            icon: 'none',
            duration: 3000
          })
        }
      })
    },
    
    /**
     * 加载更多记录
     */
    loadMoreRecords() {
      console.log('触发加载更多:', { loadingMore: this.loadingMore, hasMore: this.hasMore, pageNum: this.pageNum });
      if (this.loadingMore || !this.hasMore) return;
      
      this.loadingMore = true;
      this.pageNum++;
      
      console.log('开始加载更多数据，页码:', this.pageNum);
      // 加载更多书籍数据
      this.loadBooks();
    },
    
    /**
     * 加载书籍数据
     */
    loadBooks() {
      const url = `${config.baseUrl}/api/getBooks?userId=1&pageNum=${this.pageNum}&pageSize=${this.pageSize}`;
      console.log('====== 开始加载书籍数据 ======');
      console.log('请求 URL:', url);
      console.log('当前 pageNum:', this.pageNum, 'pageSize:', this.pageSize);
          
      uni.request({
        url: url,
        method: 'GET',
        timeout: 480000, // 8 分钟超时
        success: (res) => {
          console.log('书籍数据响应:', res);
          if (res.statusCode === 200) {
            const books = res.data;
            console.log('后端返回的原始数据:', books);
            console.log('数据数量:', Array.isArray(books) ? books.length : '非数组');
            
            // 转换书籍数据为记录格式
            const bookRecords = books.map(book => {
              if (!book.id) {
                console.warn('书籍数据缺少 ID:', book);
              }
              return {
                id: book.id,
                title: book.name,
                date: new Date().toISOString().split('T')[0],
                tags: [book.type],
                description: book.core,
                distance: '',
                duration: '',
                favorite: false,
                path: [],
                typeId: Love.BOOK  // 添加 typeId 字段
              };
            });
                
            console.log('转换后的记录数量:', bookRecords.length);
                
            // 如果是第一页，替换 records，否则追加
            if (this.pageNum === 1) {
              this.records = bookRecords;
              console.log('第一页，替换 records');
            } else {
              this.records = [...this.records, ...bookRecords];
              console.log('非第一页，追加 records');
            }
                
            console.log('当前 records 总数:', this.records.length);
                
            // 检查是否还有更多数据
            if (bookRecords.length < this.pageSize) {
              this.hasMore = false;
              console.log('没有更多数据了（返回数量 < pageSize）');
            } else {
              this.hasMore = true;
              console.log('还有更多数据');
            }
                
            console.log('书籍数据加载成功, hasMore:', this.hasMore);
          } else {
            console.error('加载书籍数据失败，状态码:', res.statusCode);
            uni.showToast({ title: '加载书籍数据失败', icon: 'none' });
          }
              
          // 无论成功或失败，都设置加载状态为 false
          this.loadingMore = false;
          console.log('====== 书籍数据加载完成 ======');
        },
        fail: (err) => {
          console.error('请求书籍数据失败:', err);
          uni.showToast({ title: '网络错误，请稍后重试', icon: 'none' });
              
          // 失败时也设置加载状态为 false
          this.loadingMore = false;
          console.log('====== 书籍数据加载失败 ======');
        }
      });
    },
    
    /**
     * 获取标签显示文本
     */
    getTagLabel(tag) {
      return tag;
    },
    
    /**
     * 获取标签的英文类名
     */
    getTagClassName(tag) {
      const tagMap = {
        '玄幻': 'xuanhuan',
        '恐怖': 'kongbu',
        '言情': 'yanqing',
        '奇幻': 'qihuan'
      };
      return tagMap[tag] || tag;
    },
    
    /**
     * 获取收藏类型标签文本
     */
    getTypeLabel(typeId) {
      if (typeId === Love.BOOK) {
        return '小说';
      } else if (typeId === Love.CULTURAL) {
        return '文化导览';
      }
      return '未知';
    },
    
    /**
     * 选择继续听的书籍
     */
    selectContinueListen(record) {
      this.continueListenBookId = record.id;
      this.continueListenBookTitle = record.title;
      
      // 保存到本地存储
      uni.setStorageSync('continueListenBook', {
        id: record.id,
        title: record.title
      });
      
      uni.showToast({
        title: `已选择：${record.title}`,
        icon: 'success',
        duration: 2000
      });
      
      // 跳转到首页
      setTimeout(() => {
        uni.switchTab({ url: '/pages/index/index' });
      }, 1500);
    },
    
    /**
     * 取消继续听
     */
    cancelContinueListen() {
      this.continueListenBookId = null;
      this.continueListenBookTitle = '';
      // 清除本地存储
      uni.removeStorageSync('continueListenBook');
      uni.showToast({
        title: '已取消继续听',
        icon: 'none'
      });
    },
    
    /**
     * 发布书籍
     */
    async publishBook(bookId) {
      try {
        console.log('开始发布书籍，ID:', bookId);
        const userId = 1; // 默认用户 ID
        
        uni.showLoading({
          title: '发布中...',
          mask: true
        });
        
        const response = await submitUpLog(bookId, userId);
        console.log('发布响应:', response);
        
        uni.hideLoading();
        
        if (response.code === 200) {
          uni.showToast({
            title: '发布成功',
            icon: 'success',
            duration: 2000
          });
        } else {
          uni.showToast({
            title: response.msg || '发布失败',
            icon: 'none',
            duration: 2000
          });
        }
      } catch (error) {
        console.error('发布书籍失败:', error);
        uni.hideLoading();
        uni.showToast({
          title: '发布失败，请重试',
          icon: 'none',
          duration: 2000
        });
      }
    }
  },
  mounted() {
    // 默认加载全部书籍数据
    console.log('====== record 页面挂载 ======');
    console.log('开始加载书籍数据...');
    this.loadBooks();
      
    // 注意：不在此处加载收藏列表，只有当用户点击“收藏”标签时才加载
      
    // 检查是否有继续听的书籍（从全局状态或存储中获取）
    // 这里可以根据需要从 store 或者 storage 中读取
    const continueListenData = uni.getStorageSync('continueListenBook');
    if (continueListenData) {
      this.continueListenBookId = continueListenData.id;
      this.continueListenBookTitle = continueListenData.title;
    }
  },
  onShow() {
    // 页面显示时重新加载收藏列表，确保数据同步
    console.log('====== record 页面显示 ======')
    console.log('currentFilter:', this.currentFilter);
    console.log('records 数量:', this.records.length);
    console.log('filteredRecords 数量:', this.filteredRecords.length);
    console.log('bookRecords 数量:', this.bookRecords.length);
    console.log('culturalRecords 数量:', this.culturalRecords.length);
    
    // 确保所有弹窗都已关闭，防止遮挡导致按钮无法点击
    if (this.showDeleteModal) {
      console.log('检测到删除弹窗未关闭，强制关闭');
      this.closeDeleteModal();
    }
      
    if (this.currentFilter === 'favorite') {
      console.log('当前是收藏页面，重新加载收藏列表');
      this.loadFavorites()
    }
  }
};
</script>

<style lang="scss" scoped>
/* 页面容器 */
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  background-color: var(--bg-color-primary);
  /* iPhone 刘海屏顶部安全区域适配 */
  padding-top: 100rpx;
}

/* #ifdef H5 */
/* H5端特殊处理 - 为按钮点击区域留出足够空间 */
.page[data-page="record"] {
  /* 移除全局CSS中的padding-top: 0，使用自定义的安全区域 */
  padding-top: constant(safe-area-inset-top) !important;
  padding-top: env(safe-area-inset-top) !important;
  
  /* 确保筛选标签不被遮挡 */
  .filter-tabs {
    margin-top: 20rpx;
  }
}
/* #endif */

/* 筛选标签 - 横向滚动 */
.filter-tabs {
  width: 100%;
  white-space: nowrap;
  padding: 16rpx 0;
  background: var(--bg-color-secondary);
  box-shadow: 0 2rpx 12rpx var(--shadow-color-light);
  border-radius: 0 0 32rpx 32rpx;
  margin-bottom: 16rpx;
  position: relative;
  z-index: 100;
}

.filter-tabs-content {
  display: inline-flex;
  align-items: center;
  padding: 0 16rpx;
  gap: 12rpx;
}

.filter-tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 14rpx 32rpx;
  border-radius: 50rpx;
  background: rgba(59, 130, 246, 0.1);
  box-shadow: 0 1rpx 6rpx var(--shadow-color-light);
  font-size: 28rpx;
  font-weight: 500;
  color: var(--theme-blue);
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
  user-select: none;
}

.filter-tab-hover {
  transform: translateY(-2rpx);
  background: rgba(59, 130, 246, 0.2);
  box-shadow: 0 2rpx 8rpx var(--shadow-color-light);
}

.filter-tab.active {
  background: var(--theme-blue);
  color: #ffffff;
  box-shadow: 0 3rpx 10rpx rgba(59, 130, 246, 0.3);
}

/* 收藏子分类标签 */
.sub-filter-tabs {
  width: 100%;
  white-space: nowrap;
  padding: 12rpx 0;
  background: var(--bg-color-secondary);
  box-shadow: 0 2rpx 8rpx var(--shadow-color-light);
  border-radius: 0 0 24rpx 24rpx;
  margin-bottom: 16rpx;
  position: relative;
  z-index: 99;
}

.sub-filter-tabs-content {
  display: inline-flex;
  align-items: center;
  padding: 0 16rpx;
  gap: 12rpx;
}

.sub-filter-tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10rpx 24rpx;
  border-radius: 40rpx;
  background: rgba(114, 46, 209, 0.1);
  box-shadow: 0 1rpx 6rpx var(--shadow-color-light);
  font-size: 24rpx;
  font-weight: 500;
  color: #722ed1;
  transition: all 0.3s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.sub-filter-tab.active {
  background: #722ed1;
  color: #ffffff;
  box-shadow: 0 3rpx 10rpx rgba(114, 46, 209, 0.3);
}

/* 轨迹列表 */
.records-list {
  flex: 1;
  padding: 16rpx;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

/* 初始加载状态 */
.initial-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 28rpx;
  color: #64748b;
  font-weight: 500;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.record-item {
  background: var(--gradient-card-light);
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.record-item-hover {
  opacity: 0.8;
  transform: scale(0.98);
  box-shadow: 0 2rpx 12rpx var(--shadow-color-medium);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.header-buttons {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.header-buttons button {
  margin: 0;
  padding: 0;
  line-height: normal;
}

.record-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color-primary);
  flex: 1;
}

.cancel-favorite-btn {
  padding: 8rpx 20rpx !important;
  font-size: 22rpx;
  color: #ffffff !important;
  background: linear-gradient(135deg, #722ed1 0%, #531dab 100%) !important;
  border: none !important;
  border-radius: 20rpx !important;
  display: flex !important;
  align-items: center !important;
  gap: 6rpx;
  box-shadow: 0 2rpx 8rpx rgba(114, 46, 209, 0.3);
  transition: all 0.3s ease;
  line-height: normal !important;
  margin: 0 !important;
}

.cancel-favorite-btn::after {
  border: none !important;
}

.cancel-favorite-btn text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 500;
}

.publish-btn {
  padding: 8rpx 20rpx !important;
  font-size: 22rpx;
  color: #ffffff !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  border: none !important;
  border-radius: 20rpx !important;
  display: flex !important;
  align-items: center !important;
  gap: 6rpx;
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  line-height: normal !important;
  margin: 0 !important;
}

.publish-btn::after {
  border: none !important;
}

.publish-btn text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 500;
}

.toggle-favorite-btn {
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  color: #fa8c16;
  background: transparent;
  border: 1rpx solid #fa8c16;
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.continue-listen-btn {
  padding: 8rpx 20rpx !important;
  font-size: 22rpx;
  color: #ffffff !important;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%) !important;
  border: none !important;
  border-radius: 20rpx !important;
  display: flex !important;
  align-items: center !important;
  gap: 6rpx;
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
  transition: all 0.3s ease;
  line-height: normal !important;
  margin: 0 !important;
}

.continue-listen-btn::after {
  border: none !important;
}

.continue-listen-btn text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 500;
}

.tag-type {
  color: #722ed1;
  background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
}

.record-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag {
  display: flex;
  align-items: center;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 500;
  color: var(--text-color-tertiary);
  background: linear-gradient(135deg, var(--bg-color-primary) 0%, var(--bg-color-secondary) 100%);
}

.tag-xuanhuan {
  color: #1677ff;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
}

.tag-kongbu {
  color: #ff4d4f;
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
}

.tag-yanqing {
  color: #fa8c16;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.tag-qihuan {
  color: #722ed1;
  background: linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 100%);
}

.record-description {
  font-size: 26rpx;
  line-height: 1.5;
  color: var(--text-color-secondary);
  margin-bottom: 16rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-stats {
  display: flex;
  gap: 24rpx;
  font-size: 24rpx;
  color: var(--text-color-tertiary);
  align-items: center;
}

/* 继续听提示 */
.continue-listen-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border-radius: 16rpx;
  margin: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.2);
  position: relative;
  z-index: 10;
}

.continue-listen-tip text {
  flex: 1;
  font-size: 26rpx;
  color: #52c41a;
  font-weight: 500;
}

.cancel-btn {
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  color: #52c41a;
  background: transparent;
  border: 1rpx solid #52c41a;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  position: relative;
  z-index: 11;
}

/* 空状态 */
.empty-records {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  gap: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #64748b;
}

.empty-subtext {
  font-size: 26rpx;
  color: #94a3b8;
}

.create-btn {
  padding: 16rpx 48rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 28rpx;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;
}

/* 加载更多 */
.loading-more {
  display: flex;
  justify-content: center;
  padding: 32rpx 0;
  font-size: 26rpx;
  color: #64748b;
}

.no-more-data {
  display: flex;
  justify-content: center;
  padding: 32rpx 0;
  font-size: 26rpx;
  color: #94a3b8;
}

/* 自定义删除弹窗 */
.delete-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: overlayFadeIn 0.3s ease;
}

.delete-modal {
  width: 700rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
}

.modal-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx 32rpx;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
  border-bottom: 2rpx solid #fecaca;
}

.modal-header .uni-icons {
  margin-bottom: 16rpx;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #dc2626;
  text-align: center;
}

.modal-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 32rpx;
}

.modal-content {
  font-size: 32rpx;
  line-height: 1.8;
  color: #1e293b;
  text-align: center;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.modal-tip {
  font-size: 26rpx;
  color: #94a3b8;
  text-align: center;
  padding: 8rpx 24rpx;
  background: #f1f5f9;
  border-radius: 16rpx;
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 32rpx;
  background: #f8fafc;
  border-top: 1rpx solid #e2e8f0;
}

.modal-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  transition: all 0.3s ease;
  border: none;
}

.modal-btn.cancel {
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #475569;
  box-shadow: 0 4rpx 12rpx rgba(148, 163, 184, 0.2);
}

.modal-btn.confirm {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(239, 68, 68, 0.4);
}

@keyframes overlayFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50rpx) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>