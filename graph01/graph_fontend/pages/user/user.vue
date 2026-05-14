<template>
  <view class="page profile-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar-wrapper">
        <image 
          :src="getAvatarUrl()" 
          class="avatar"
          mode="aspectFill"
          @error="onAvatarError"
          @load="onAvatarLoad"
          lazy-load
        ></image>
        <!-- 调试信息：显示实际使用的URL -->
        <view v-if="showDebugInfo" class="debug-info">
          <text class="debug-text">{{ getAvatarUrl() }}</text>
        </view>
      </view>
      <text class="username">{{ userInfo.name || '轨迹探索者' }}</text>
      <text class="user-description">{{ userInfo.description || '无' }}</text>
      <view class="button-container">
        <button type="primary" @click="showEditModal" class="edit-button">修改用户信息</button>
      </view>
    </view>
    
    <!-- 设置选项 -->
    <view class="settings-list">
      <!-- 上传头像按钮 -->
      <view class="setting-item" @click="showAvatarUploadModal">
        <view class="setting-content">
          <view class="setting-icon bg-pink">
            <uni-icons type="camera" size="24" color="#ffffff"></uni-icons>
          </view>
          <text>上传头像</text>
        </view>
        <uni-icons type="right" size="16" color="#c7c7cc"></uni-icons>
      </view>
      
      <!-- 需求管理按钮 -->
      <view class="setting-item" @click="showNeedsModal">
        <view class="setting-content">
          <view class="setting-icon bg-blue">
            <uni-icons type="list" size="24" color="#ffffff"></uni-icons>
          </view>
          <text>需求管理</text>
        </view>
        <uni-icons type="right" size="16" color="#c7c7cc"></uni-icons>
      </view>
      
      <!-- 获取他人小说按钮 -->
      <view class="setting-item" @click="showGetBookModal">
        <view class="setting-content">
          <view class="setting-icon bg-purple">
            <uni-icons type="download" size="24" color="#ffffff"></uni-icons>
          </view>
          <text>获取他人小说</text>
        </view>
        <uni-icons type="right" size="16" color="#c7c7cc"></uni-icons>
      </view>
    </view>
    
    <!-- 修改用户信息弹窗 -->
    <view v-if="showEditModalVisible" class="edit-modal-overlay" @click="closeEditModal">
      <view class="edit-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改用户信息</text>
        </view>
        <view class="modal-body">
          <uni-forms ref="form" :model="editUser" labelWidth="80px">
            <uni-forms-item label="姓名" name="nickName">
              <uni-easyinput v-model="editUser.nickName" placeholder="请输入姓名" />
            </uni-forms-item>
            <uni-forms-item label="简介" name="introduction">
              <uni-easyinput v-model="editUser.introduction" placeholder="请输入简介" type="textarea" />
            </uni-forms-item>
            <ImageUploader 
              ref="avatarUploaderRef" 
              title="头像" 
            />
          </uni-forms>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeEditModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>取消</text>
          </button>
          <button class="modal-btn confirm" @click="submitEdit">
            <uni-icons type="checkmark" size="20" color="#ffffff"></uni-icons>
            <text>确定</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- 需求管理弹窗 -->
    <view v-if="showNeedsModalVisible" class="needs-modal-overlay" @click="closeNeedsModal">
      <view class="needs-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">需求管理</text>
          <button class="close-btn" @click.stop="closeNeedsModal">
            <uni-icons type="closeempty" size="24" color="#64748b"></uni-icons>
          </button>
        </view>
        
        <view class="modal-body">
          <!-- 需求列表 -->
          <scroll-view class="needs-scroll" scroll-y>
            <view v-if="needsList.length === 0" class="empty-needs">
              <uni-icons type="info" size="48" color="#cbd5e1"></uni-icons>
              <text class="empty-text">暂时没有要求，该要求仅可以要求文化导览</text>
            </view>
            
            <view v-else class="needs-list">
              <view v-for="need in needsList" :key="need.id" class="need-item">
                <view class="need-content">
                  <text class="need-text">{{ need.needContent }}</text>
                </view>
                <view class="need-actions">
                  <button class="action-btn apply-btn" @click.stop="applyNeed(need)">
                    <uni-icons type="checkmark" size="18" color="#ffffff"></uni-icons>
                    <text>应用</text>
                  </button>
                  <button class="action-btn edit-btn" @click="showEditNeedForm(need)">
                    <uni-icons type="edit" size="18" color="#ffffff"></uni-icons>
                    <text>编辑</text>
                  </button>
                  <button class="action-btn delete-btn" @click="handleDeleteNeed(need.id)">
                    <uni-icons type="trash" size="18" color="#ffffff"></uni-icons>
                    <text>删除</text>
                  </button>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
        
        <view class="modal-footer needs-modal-footer">
          <button class="add-btn" @click="showAddNeedForm">
            <uni-icons type="plus" size="20" color="#ffffff"></uni-icons>
            <text>新建需求</text>
          </button>
          <button class="cancel-requirement-btn" @click="cancelRequirement">
            <uni-icons type="close" size="20" color="#ffffff"></uni-icons>
            <text>取消要求</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- 新建/编辑需求表单弹窗 -->
    <view v-if="showNeedFormModalVisible" class="form-modal-overlay" @click="closeNeedFormModal">
      <view class="form-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEditMode ? '编辑需求' : '新建需求' }}</text>
          <button class="close-btn" @click.stop="closeNeedFormModal">
            <uni-icons type="closeempty" size="24" color="#64748b"></uni-icons>
          </button>
        </view>
        
        <view class="modal-body">
          <uni-easyinput 
            v-model="needForm.needContent" 
            type="textarea" 
            placeholder="请输入需求内容" 
            :maxlength="500"
            auto-height
          />
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeNeedFormModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>取消</text>
          </button>
          <button class="modal-btn confirm" @click="submitNeedForm">
            <uni-icons type="checkmark" size="20" color="#ffffff"></uni-icons>
            <text>确定</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- 上传头像弹窗 -->
    <view v-if="showAvatarUploadModalVisible" class="avatar-upload-modal-overlay" @click="closeAvatarUploadModal">
      <view class="avatar-upload-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">上传头像</text>
          <button class="close-btn" @click.stop="closeAvatarUploadModal">
            <uni-icons type="closeempty" size="24" color="#64748b"></uni-icons>
          </button>
        </view>
        
        <view class="modal-body">
          <view class="avatar-preview">
            <image 
              :src="tempAvatarUrl || getAvatarUrl()" 
              class="preview-avatar"
              mode="aspectFill"
            ></image>
          </view>
          <ImageUploader 
            ref="quickAvatarUploaderRef" 
            title="选择新头像" 
          />
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeAvatarUploadModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>取消</text>
          </button>
          <button class="modal-btn confirm" @click="submitAvatarUpload">
            <uni-icons type="checkmark" size="20" color="#ffffff"></uni-icons>
            <text>保存</text>
          </button>
        </view>
      </view>
    </view>
    
    <!-- 获取他人小说弹窗 -->
    <view v-if="showGetBookModalVisible" class="get-book-modal-overlay" @click="closeGetBookModal">
      <view class="get-book-modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">获取他人小说</text>
          <button class="close-btn" @click.stop="closeGetBookModal">
            <uni-icons type="closeempty" size="24" color="#64748b"></uni-icons>
          </button>
        </view>
        
        <view class="modal-body">
          <!-- 输入分享码 -->
          <view v-if="!receivedBook" class="input-section">
            <uni-easyinput 
              v-model="shareCode" 
              placeholder="请输入分享链接或分享码" 
              :maxlength="500"
            />
          </view>
          
          <!-- 显示书籍信息 -->
          <view v-else class="book-info-section">
            <view class="book-card">
              <view class="book-header">
                <uni-icons type="book-filled" size="32" color="#3b82f6"></uni-icons>
                <text class="book-title">{{ receivedBook.title }}</text>
              </view>
              <view class="book-details">
                <view class="detail-item">
                  <text class="detail-label">作者：</text>
                  <text class="detail-value">{{ receivedBook.author || '未知' }}</text>
                </view>
                <view class="detail-item">
                  <text class="detail-label">章节数：</text>
                  <text class="detail-value">{{ receivedBook.chapterCount || 0 }}</text>
                </view>
              </view>
              <view class="book-actions">
                <button class="action-btn-primary" @click="viewReceivedBook">
                  <uni-icons type="eye" size="18" color="#ffffff"></uni-icons>
                  <text>查看小说</text>
                </button>
              </view>
            </view>
          </view>
        </view>
        
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeGetBookModal">
            <uni-icons type="close" size="20" color="#64748b"></uni-icons>
            <text>关闭</text>
          </button>
          <button v-if="!receivedBook" class="modal-btn confirm" @click="submitShareCode">
            <uni-icons type="checkmark" size="20" color="#ffffff"></uni-icons>
            <text>获取</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUser, updateUser } from '@/api/system/user'
import { getNeeds, addNeed, deleteNeed, updateNeed } from '@/api/normal/need'
import { getFavoritesBook } from '@/api/normal/favorite'
import { postShare } from '@/api/normal/share'
import { Need } from '@/vo/Need'
import { Book } from '@/vo/Book'
import { Love } from '@/vo/Love'
import { Customer } from '@/vo/Customer'
import ImageUploader from '@/components/ImageUploader.vue'
import { fileUrl } from '@/utils/upload.js'

export default {
  components: {
    ImageUploader
  },
  data() {
    return {
      // 统计信息
      recordCount: 76,
      // 用户信息
      userInfo: new Customer({}),
      // 编辑用户信息
      editUser: {
        nickName: '',
        introduction: ''
      },
      // 修改用户信息弹窗
      showEditModalVisible: false,
      // 需求管理弹窗
      showNeedsModalVisible: false,
      // 需求列表
      needsList: [],
      // 新建/编辑需求表单
      needForm: {
        id: null,
        customerId: 1,
        needContent: ''
      },
      // 表单弹窗可见性
      showNeedFormModalVisible: false,
      // 是否为编辑模式
      isEditMode: false,
      // 收藏管理弹窗
      showFavoritesModalVisible: false,
      // 收藏的小说列表
      favoriteBooksList: [],
      // 获取他人小说弹窗
      showGetBookModalVisible: false,
      // 分享码
      shareCode: '',
      // 接收到的书籍信息
      receivedBook: null,
      // 上传头像弹窗
      showAvatarUploadModalVisible: false,
      // 临时头像URL
      tempAvatarUrl: '',
      // 调试信息显示开关
      showDebugInfo: true
    };
  },
  methods: {
    /**
     * 获取头像URL,处理完整URL和fileName的情况
     */
    getAvatarUrl() {
      console.log('=== getAvatarUrl 调用 ===')
      console.log('userInfo.imageUrl:', this.userInfo.imageUrl)
          
      if (!this.userInfo.imageUrl) {
        console.log('头像URL为空,使用默认头像')
        return 'https://api.dicebear.com/9.x/micah/svg?seed=user&backgroundColor=8b5cf6,3b82f6,ec4899'
      }
          
      // 如果已经是完整URL,直接返回
      if (this.userInfo.imageUrl.startsWith('http://') || this.userInfo.imageUrl.startsWith('https://')) {
        return this.userInfo.imageUrl
      }
          
      // 使用 fileUrl 函数拼接 baseUrl
      const fullUrl = fileUrl(this.userInfo.imageUrl)
      console.log('拼接后的完整URL:', fullUrl)
          
      return fullUrl
    },
    
    /**
     * 头像加载成功回调
     */
    onAvatarLoad(e) {
      console.log('✅ 头像加载成功', e)
      console.log('成功的URL:', this.getAvatarUrl())
      // 加载成功后隐藏调试信息
      this.showDebugInfo = false
    },
    
    /**
     * 头像加载失败回调
     */
    onAvatarError(e) {
      console.error('❌ 头像加载失败', e)
      console.error('失败的URL:', this.getAvatarUrl())
      console.error('原始imageUrl:', this.userInfo.imageUrl)
      console.error('错误详情:', JSON.stringify(e.detail))
      
      // 保持显示调试信息
      this.showDebugInfo = true
      
      // 尝试直接在浏览器中打开该URL进行测试
      const testUrl = this.getAvatarUrl()
      console.warn('请在浏览器中打开以下URL测试图片是否可访问:')
      console.warn(testUrl)
      
      // 尝试使用默认头像
      uni.showToast({
        title: '头像加载失败',
        icon: 'none',
        duration: 2000
      })
    },
    
    getUserInfo() {
      console.log('开始获取用户信息...')
      const userId = 1 // Default to userId=1 as requested
      console.log('请求用户ID:', userId)
      getUser(userId).then(res => {
        console.log('=== 获取用户信息成功 ===')
        console.log('原始响应数据:', res)
        console.log('响应数据类型:', typeof res)
        console.log('响应数据键名:', Object.keys(res || {}))
        
        if (res) {
          this.userInfo = new Customer(res)
          console.log('用户信息已更新:', this.userInfo)
          console.log('头像URL:', this.userInfo.imageUrl)
          console.log('处理后的头像URL:', this.getAvatarUrl())
        } else {
          console.error('响应数据为空')
        }
      }).catch(error => {
        console.error('获取用户信息失败:', error)
      })
    },
    
    showEditModal() {
      // 初始化编辑数据
      this.editUser.nickName = this.userInfo.name || ""
      this.editUser.introduction = this.userInfo.description || ""
      
      // 回显头像
      if (this.$refs.avatarUploaderRef && this.userInfo.imageUrl) {
        this.$refs.avatarUploaderRef.setImageUrl(this.userInfo.imageUrl)
      }
      
      // 显示弹窗
      this.showEditModalVisible = true
    },
    
    /**
     * 关闭修改用户信息弹窗
     */
    closeEditModal() {
      this.showEditModalVisible = false
    },
    
    /**
     * 提交修改用户信息
     */
    submitEdit() {
      // 验证表单
      this.$refs.form.validate().then(res => {
        // 获取新选择的头像fileName
        const avatarUploader = this.$refs.avatarUploaderRef
        const newAvatarFileName = avatarUploader ? avatarUploader.getFileName() : ''
        
        console.log('=== 提交用户信息 ===')
        console.log('userInfo:', this.userInfo)
        console.log('editUser:', this.editUser)
        console.log('avatarUploader:', avatarUploader)
        console.log('newAvatarFileName:', newAvatarFileName)
        console.log('userInfo.imageUrl:', this.userInfo.imageUrl)
        
        // 构建请求数据，包含用户 id
        // 如果有新头像则用新的，否则用原来的
        const finalImageUrl = newAvatarFileName || this.userInfo.imageUrl || ''
        
        const requestData = {
          id: this.userInfo.id,
          nickName: this.editUser.nickName || '',
          introduction: this.editUser.introduction || '',
          imageUrl: finalImageUrl
        }
        
        console.log('最终请求数据:', requestData)
        
        // 请求修改用户信息
        updateUser(requestData).then(response => {
          console.log('更新成功响应:', response)
          // 关闭弹窗
          this.closeEditModal()
          // 重新获取用户信息
          this.getUserInfo()
          // 提示成功
          uni.showToast({
            title: '修改成功',
            icon: 'success'
          })
        }).catch(error => {
          console.error('更新失败:', error)
          uni.showToast({
            title: '修改失败',
            icon: 'none'
          })
        })
      }).catch(err => {
        console.error('表单验证失败:', err)
      })
    },
      
    /**
     * 显示需求管理弹窗
     */
    showNeedsModal() {
      this.showNeedsModalVisible = true;
      this.loadNeeds();
    },
        
    /**
     * 关闭需求管理弹窗
     */
    closeNeedsModal() {
      this.showNeedsModalVisible = false;
    },
      
    /**
     * 加载需求列表
     */
    loadNeeds() {
      getNeeds(1).then(res => {
        console.log('获取需求列表成功:', res);
        if (res && Array.isArray(res)) {
          this.needsList = res.map(item => new Need(item));
        } else {
          this.needsList = [];
        }
      }).catch(error => {
        console.error('获取需求列表失败:', error);
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        });
      });
    },
      
    /**
     * 显示新建需求表单
     */
    showAddNeedForm() {
      this.needForm = {
        id: null,
        customerId: 1,
        needContent: ''
      };
      this.isEditMode = false;
      this.showNeedFormModalVisible = true;
    },
        
    /**
     * 显示编辑需求表单
     */
    showEditNeedForm(need) {
      this.needForm = {
        id: need.id,
        customerId: need.customerId,
        needContent: need.needContent
      };
      this.isEditMode = true;
      this.showNeedFormModalVisible = true;
    },
        
    /**
     * 关闭表单弹窗
     */
    closeNeedFormModal() {
      this.showNeedFormModalVisible = false;
    },
      
    /**
     * 提交需求表单
     */
    submitNeedForm() {
      if (!this.needForm.needContent || !this.needForm.needContent.trim()) {
        uni.showToast({
          title: '请输入需求内容',
          icon: 'none'
        });
        return;
      }
        
      if (this.isEditMode) {
        // 更新需求
        updateNeed(this.needForm).then(res => {
          uni.showToast({
            title: '更新成功',
            icon: 'success'
          });
          this.closeNeedFormModal();
          this.loadNeeds();
        }).catch(error => {
          console.error('更新失败:', error);
          uni.showToast({
            title: '更新失败',
            icon: 'none'
          });
        });
      } else {
        // 添加新需求
        addNeed(this.needForm).then(res => {
          uni.showToast({
            title: '添加成功',
            icon: 'success'
          });
          this.closeNeedFormModal();
          this.loadNeeds();
        }).catch(error => {
          console.error('添加失败:', error);
          uni.showToast({
            title: '添加失败',
            icon: 'none'
          });
        });
      }
    },
      
    /**
     * 应用需求到文化导览
     */
    applyNeed(need) {
      console.log('[需求管理] 📝 准备应用需求:', need);
      console.log('[需求管理] 📝 needContent:', need.needContent);
      
      // 将需求的 needContent 保存到 Vuex store
      this.$store.dispatch('cultural/applyCulturalContent', need.needContent);
      
      console.log('[需求管理] ✅ 已 dispatch 到 store');
      console.log('[需求管理] 🔍 检查 store state:', this.$store.state.cultural);
      
      // 显示提示
      uni.showToast({
        title: '仅可以在文化导览里面可以使用',
        icon: 'none',
        duration: 3000
      });
      
      // 跳转到首页
      setTimeout(() => {
        console.log('[需求管理] 🚀 即将跳转到首页');
        uni.switchTab({ url: '/pages/index/index' });
      }, 2000);
    },
          
    /**
     * 取消要求 - 清除文化导览模式下的content参数
     */
    cancelRequirement() {
      // 从 Vuex store 中清除 culturalContent
      this.$store.dispatch('cultural/cancelCulturalContent');
      
      // 显示提示
      uni.showToast({
        title: '已取消要求',
        icon: 'success'
      });
      
      // 关闭需求管理弹窗
      this.closeNeedsModal();
    },
    
    /**
     * 删除需求
     */
    handleDeleteNeed(id) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这条需求吗？',
        success: (res) => {
          if (res.confirm) {
            deleteNeed(id).then(response => {
              uni.showToast({
                title: '删除成功',
                icon: 'success'
              });
              this.loadNeeds();
            }).catch(error => {
              console.error('删除失败:', error);
              uni.showToast({
                title: '删除失败',
                icon: 'none'
              });
            });
          }
        }
      });
    },
    
    /**
     * 显示获取他人小说弹窗
     */
    showGetBookModal() {
      this.showGetBookModalVisible = true;
      this.shareCode = '';
      this.receivedBook = null;
    },
    
    /**
     * 关闭获取他人小说弹窗
     */
    closeGetBookModal() {
      this.showGetBookModalVisible = false;
      this.shareCode = '';
      this.receivedBook = null;
    },
    
    /**
     * 提交分享码
     */
    submitShareCode() {
      if (!this.shareCode || !this.shareCode.trim()) {
        uni.showToast({
          title: '请输入分享链接或分享码',
          icon: 'none'
        });
        return;
      }
      
      const userId = this.userInfo.id || 1;
      
      uni.showLoading({ title: '获取中...' });
      
      postShare(this.shareCode, userId).then(res => {
        uni.hideLoading();
        if (res) {
          // 解析返回的书籍信息
          this.receivedBook = res;
          uni.showToast({
            title: '获取成功',
            icon: 'success'
          });
        } else {
          uni.showToast({
            title: '获取失败，请检查分享码',
            icon: 'none'
          });
        }
      }).catch(error => {
        uni.hideLoading();
        console.error('获取书籍失败:', error);
        uni.showToast({
          title: '获取失败，请检查分享码',
          icon: 'none'
        });
      });
    },
    
    /**
     * 查看接收到的书籍
     */
    viewReceivedBook() {
      if (!this.receivedBook || !this.receivedBook.id) {
        uni.showToast({
          title: '书籍信息不完整',
          icon: 'none'
        });
        return;
      }
      
      // 关闭弹窗
      this.closeGetBookModal();
      
      // 跳转到书籍阅读页面
      uni.navigateTo({
        url: `/pages/book/book?bookId=${this.receivedBook.id}`,
        success: () => {
          console.log('跳转到书籍页面成功');
        },
        fail: (error) => {
          console.error('跳转失败:', error);
          uni.showToast({
            title: '跳转失败',
            icon: 'none'
          });
        }
      });
    },

    /**
     * 显示上传头像弹窗
     */
    showAvatarUploadModal() {
      this.tempAvatarUrl = '';
      this.showAvatarUploadModalVisible = true;
      // 回显当前头像
      if (this.$refs.quickAvatarUploaderRef && this.userInfo.imageUrl) {
        this.$refs.quickAvatarUploaderRef.setImageUrl(this.userInfo.imageUrl);
      }
    },

    /**
     * 关闭上传头像弹窗
     */
    closeAvatarUploadModal() {
      this.showAvatarUploadModalVisible = false;
      this.tempAvatarUrl = '';
    },

    /**
     * 提交头像上传
     */
    submitAvatarUpload() {
      const avatarUploader = this.$refs.quickAvatarUploaderRef;
      const newAvatarFileName = avatarUploader ? avatarUploader.getFileName() : '';
      
      if (!newAvatarFileName) {
        uni.showToast({
          title: '请先选择头像',
          icon: 'none'
        });
        return;
      }
      
      console.log('=== 提交头像上传 ===')
      console.log('userInfo:', this.userInfo)
      console.log('newAvatarFileName:', newAvatarFileName)
      
      // 构建请求数据，包含完整的 CustomerVo 字段
      const requestData = {
        id: this.userInfo.id,
        nickName: this.userInfo.name || '',
        introduction: this.userInfo.description || '',
        imageUrl: newAvatarFileName
      };
      
      console.log('请求数据:', requestData)
      
      uni.showLoading({ title: '保存中...' });
      
      // 更新用户头像
      updateUser(requestData).then(response => {
        console.log('更新成功:', response)
        uni.hideLoading();
        // 关闭弹窗
        this.closeAvatarUploadModal();
        // 重新获取用户信息
        this.getUserInfo();
        // 提示成功
        uni.showToast({
          title: '头像更新成功',
          icon: 'success'
        });
      }).catch(error => {
        console.error('更新头像失败:', error)
        uni.hideLoading();
        uni.showToast({
          title: '更新失败',
          icon: 'none'
        });
      });
    },



  },
  mounted() {
    console.log('组件已挂载，开始获取用户信息...');
    this.getUserInfo();
  }
};
</script>

<style lang="scss" scoped>
/* 个人中心页面 */
.profile-page {
  flex: 1;
  padding: 32rpx 16rpx;
  padding-top: 132rpx; /* 32rpx + 100rpx 安全区域 */
  background: var(--gradient-bg-light);
}

/* 用户信息卡片 */
.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx;
  background: var(--gradient-card-light);
  border-radius: 32rpx;
  box-shadow: 0 8rpx 32rpx var(--shadow-color-light);
  margin-bottom: 32rpx;
  transition: all 0.3s ease;
}

.user-card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.15);
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
  object-fit: cover;
  transition: all 0.3s ease;
}

.avatar-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.debug-info {
  position: absolute;
  bottom: -60rpx;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  max-width: 400rpx;
  word-break: break-all;
  z-index: 10;
}

.debug-text {
  font-size: 20rpx;
  color: #ffffff;
  line-height: 1.4;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.username {
  font-size: 36rpx;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 8rpx;
}

.user-description {
  font-size: 26rpx;
  color: var(--text-color-tertiary);
  margin-bottom: 16rpx;
  text-align: center;
  max-width: 80%;
}

.user-phone {
  font-size: 24rpx;
  color: var(--text-color-placeholder);
  margin-bottom: 32rpx;
}

.button-container {
  width: 100%;
  display: flex;
  justify-content: center;
}

.edit-button {
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

.edit-button:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.5);
}

/* 统计信息 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  background: var(--gradient-card-light);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.bg-green {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-label {
  font-size: 24rpx;
  color: var(--text-color-tertiary);
  margin-bottom: 8rpx;
}

.stat-number {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color-primary);
}

.stat-unit {
  font-size: 24rpx;
  font-weight: 400;
  color: var(--text-color-tertiary);
}

/* 设置选项 */
.settings-list {
  background: var(--gradient-card-light);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
  overflow: hidden;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid var(--border-color-light);
  transition: all 0.3s ease;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:hover {
  background-color: var(--bg-color-tertiary);
}

.setting-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.setting-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.bg-orange {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
}

.bg-blue {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.bg-purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.bg-pink {
  background: linear-gradient(135deg, #ec4899 0%, #db2777 100%);
}

.setting-content text {
  font-size: 28rpx;
  color: var(--text-color-primary);
  font-weight: 500;
}

/* 弹窗样式 */
.popup-content {
  width: 700rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 24rpx;
  box-shadow: 0 12rpx 40rpx rgba(0, 0, 0, 0.2);
  overflow: hidden;
}

/* 自定义编辑用户信息弹窗 */
.edit-modal-overlay {
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

.edit-modal {
  width: 700rpx;
  background: var(--gradient-card-light);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx var(--shadow-color-medium);
  animation: modalSlideIn 0.3s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 40rpx;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-bottom: 2rpx solid #93c5fd;
}

.modal-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1e40af;
}

.modal-body {
  padding: 40rpx;
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  padding: 32rpx 40rpx;
  background: var(--bg-color-tertiary);
  border-top: 1rpx solid var(--border-color-light);
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
  background: linear-gradient(135deg, var(--bg-color-primary) 0%, var(--bg-color-secondary) 100%);
  color: var(--text-color-secondary);
  box-shadow: 0 4rpx 12rpx var(--shadow-color-light);
}

.modal-btn.cancel:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 16rpx rgba(148, 163, 184, 0.3);
}

.modal-btn.confirm {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.4);
}

.modal-btn.confirm:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(59, 130, 246, 0.5);
}

/* 需求管理弹窗样式 */
.needs-modal-overlay {
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

.needs-modal {
  width: 700rpx;
  height: 85vh;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

.needs-modal .modal-header {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-bottom: 2rpx solid #93c5fd;
}

.close-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  padding: 0;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: #bfdbfe;
  transform: rotate(90deg);
}

.needs-modal .modal-body {
  flex: 1;
  padding: 0;
  min-height: 650rpx;
  max-height: none;
}

.needs-scroll {
  height: 100%;
  padding: 48rpx 64rpx;
}

.empty-needs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
  gap: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #64748b;
  text-align: center;
  line-height: 1.6;
}

.needs-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.need-item {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  transition: all 0.3s ease;
}

.need-item:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.need-content {
  margin-bottom: 16rpx;
}

.need-text {
  font-size: 28rpx;
  color: #334155;
  line-height: 1.6;
  word-break: break-all;
}

.need-actions {
  display: flex;
  gap: 12rpx;
  justify-content: flex-end;
}

.apply-btn {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  background: linear-gradient(135deg, #1677ff 0%, #3b82f6 100%);
  color: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(22, 119, 255, 0.3);
}

.apply-btn:hover {
  box-shadow: 0 4rpx 12rpx rgba(22, 119, 255, 0.4);
  transform: translateY(-2rpx);
}

.action-btn {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
}

.edit-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.3);
}

.edit-btn:hover {
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.4);
  transform: translateY(-2rpx);
}

.delete-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.3);
}

.delete-btn:hover {
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.4);
  transform: translateY(-2rpx);
}

.needs-modal-footer {
  padding: 32rpx 40rpx;
  border-top: 1rpx solid #e2e8f0;
  display: flex;
  justify-content: center;
  gap: 16rpx;
}

.add-btn {
  padding: 14rpx 40rpx;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.3);
  transition: all 0.3s ease;
}

.add-btn:hover {
  box-shadow: 0 6rpx 16rpx rgba(16, 185, 129, 0.4);
  transform: translateY(-2rpx);
}

.cancel-requirement-btn {
  padding: 14rpx 40rpx;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  border: none;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  transition: all 0.3s ease;
}

.cancel-requirement-btn:hover {
  box-shadow: 0 6rpx 16rpx rgba(239, 68, 68, 0.4);
  transform: translateY(-2rpx);
}

/* 表单弹窗样式 */
.form-modal-overlay {
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

.form-modal {
  width: 700rpx;
  min-height: 650rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

.form-modal .modal-header {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-bottom: 2rpx solid #93c5fd;
}

.form-modal .modal-body {
  padding: 48rpx 64rpx;
  min-height: 650rpx;
  flex: 1;
  background: #ffffff;
}

.form-modal .modal-footer {
  padding: 32rpx 40rpx;
  background: #f8fafc;
  border-top: 1rpx solid #e2e8f0;
}

/* 输入框动画 - 缓慢平滑 */
.form-modal .modal-body ::v-deep .uni-easyinput__content {
  transition: all 0.4s ease-in-out;
  animation: fadeIn 0.5s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

/* 夜间模式下的需求弹窗 */
.dark-mode .needs-modal-overlay {
  background-color: rgba(0, 0, 0, 0.75);
}

.dark-mode .needs-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.dark-mode .needs-modal .modal-header {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  border-bottom-color: #1e3a8a;
}

.dark-mode .close-btn:hover {
  background: #1e3a8a;
}

.dark-mode .form-modal-overlay {
  background-color: rgba(0, 0, 0, 0.75);
}

.dark-mode .form-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.dark-mode .form-modal .modal-header {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  border-bottom-color: #1e3a8a;
}

.dark-mode .form-modal .modal-body {
  background: #1e293b;
}

.dark-mode .form-modal .modal-footer {
  background: #0f172a;
  border-top-color: #334155;
}

/* 获取他人小说弹窗样式 */
.get-book-modal-overlay {
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

.get-book-modal {
  width: 650rpx;
  min-height: 600rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

.get-book-modal .modal-header {
  background: linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%);
  border-bottom: 2rpx solid #c4b5fd;
}

.get-book-modal .modal-body {
  padding: 48rpx 40rpx;
  flex: 1;
}

.input-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.book-info-section {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.book-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 24rpx;
  padding: 32rpx;
  border: 2rpx solid #bae6fd;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.1);
}

.book-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.book-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1e40af;
  flex: 1;
}

.book-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.detail-label {
  font-size: 26rpx;
  color: #64748b;
  font-weight: 500;
}

.detail-value {
  font-size: 26rpx;
  color: #334155;
  flex: 1;
}

.book-actions {
  display: flex;
  justify-content: center;
}

.action-btn-primary {
  padding: 16rpx 40rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.action-btn-primary:hover {
  box-shadow: 0 6rpx 16rpx rgba(59, 130, 246, 0.4);
  transform: translateY(-2rpx);
}

/* 夜间模式下的获取书籍弹窗 */
.dark-mode .get-book-modal-overlay {
  background-color: rgba(0, 0, 0, 0.75);
}

.dark-mode .get-book-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.dark-mode .get-book-modal .modal-header {
  background: linear-gradient(135deg, #4c1d95 0%, #5b21b6 100%);
  border-bottom-color: #6d28d9;
}

.dark-mode .book-card {
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
  border-color: #3b82f6;
}

.dark-mode .book-title {
  color: #bfdbfe;
}

.dark-mode .detail-label {
  color: #94a3b8;
}

.dark-mode .detail-value {
  color: #e2e8f0;
}

/* 上传头像弹窗样式 */
.avatar-upload-modal-overlay {
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

.avatar-upload-modal {
  width: 650rpx;
  min-height: 700rpx;
  background: var(--gradient-card-light);
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx var(--shadow-color-medium);
  animation: modalSlideIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

.avatar-upload-modal .modal-header {
  background: linear-gradient(135deg, #fce7f3 0%, #fbcfe8 100%);
  border-bottom: 2rpx solid #f9a8d4;
}

.avatar-upload-modal .modal-title {
  color: #be185d;
}

.avatar-preview {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
}

.preview-avatar {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(236, 72, 153, 0.3);
  border: 4rpx solid #fbcfe8;
  object-fit: cover;
}

.dark-mode .avatar-upload-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
}

.dark-mode .avatar-upload-modal .modal-header {
  background: linear-gradient(135deg, #831843 0%, #9d174d 100%);
  border-bottom-color: #be185d;
}

.dark-mode .avatar-upload-modal .modal-title {
  color: #fbcfe8;
}

.dark-mode .preview-avatar {
  border-color: #be185d;
}
</style>