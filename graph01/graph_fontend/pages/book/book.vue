<template>
	<view class="book-container">
		<!-- 返回按钮 -->
		<view class="back-button" @click="goBack" hover-class="back-button-hover">
			<uni-icons type="left" size="20" color="#333"></uni-icons>
			<text class="back-text">返回</text>
		</view>
		
		<!-- 章节标题 -->
		<view class="chapter-title">
			第{{ currentIndex + 1 }}章 {{ currentChapter.title }}
		</view>
		
		<!-- 章节内容 -->
		<view class="chapter-content">
			{{ currentChapter.content }}
		</view>
		
		<!-- 章节导航 -->
		<view class="chapter-nav">
			<button @click="prevChapter" :disabled="currentIndex <= 0">上一章</button>
			<button @click="showShareModal" class="share-btn">分享</button>
			<button @click="nextChapter" :disabled="currentIndex >= chapters.length - 1 && !hasMore">下一章</button>
		</view>
		
		<!-- 分享弹窗 -->
		<view v-if="showShareModalVisible" class="share-modal-overlay" @click="closeShareModal">
			<view class="share-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">分享小说</text>
					<button class="close-btn" @click.stop="closeShareModal">
						<uni-icons type="closeempty" size="24" color="#64748b"></uni-icons>
					</button>
				</view>
				<view class="modal-body">
					<view class="share-content">
						<text class="share-label">分享链接：</text>
						<view class="share-link-box">
							<text class="share-link-text">{{ shareLink }}</text>
							<button class="copy-btn" @click="copyShareLink">
								<uni-icons type="copy" size="18" color="#ffffff"></uni-icons>
								<text>复制</text>
							</button>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import config from '@/config'
	import { getShare } from '@/api/normal/share'
	
	export default {
		data() {
			return {
				currentIndex: 0,
				chapters: [],
				bookId: '',
				bookTitle: '',
				loading: false,
				currentPage: 1,
				pageSize: 10,
				hasMore: true,
				showShareModalVisible: false,
				shareLink: ''
			}
		},
		computed: {
			currentChapter() {
				return this.chapters[this.currentIndex] || { title: '加载中...', content: '正在获取章节内容...' };
			}
		},
		methods: {
			goBack() {
				uni.navigateBack({
					delta: 1,
					fail: (error) => {
						console.error('返回上一页失败:', error);
						// 如果返回失败，尝试跳转到首页
						uni.navigateTo({
							url: '/pages/index'
						});
					}
				});
			},
			prevChapter() {
				if (this.currentIndex > 0) {
					this.currentIndex--;
				}
			},
			nextChapter() {
				if (this.currentIndex < this.chapters.length - 1) {
					this.currentIndex++;
				} else if (this.hasMore) {
					// 当前章节是最后一章，且还有更多数据，加载下一页
					this.loadNextPage();
				}
			},
			getChapters(bookId, pageNum = 1, isLoadMore = false) {
				const backendUrl = `${config.baseUrl}/api/getChapters?bookId=${bookId}&pageNum=${pageNum}&pageSize=${this.pageSize}`;
				this.loading = true;

				uni.request({
					url: backendUrl,
					method: 'GET',
					header: { 'Content-Type': 'application/json' },
					timeout: 600000, // 10分钟超时
					success: (res) => {
						this.loading = false;
						if (res.statusCode >= 200 && res.statusCode < 300) {
							const chaptersData = res.data;
							if (chaptersData && chaptersData.length > 0) {
								// 转换章节数据格式
								const newChapters = chaptersData.map(chapter => ({
									title: chapter.name,
									content: chapter.text || '暂无内容'
								}));
								
								// 如果是加载更多，追加数据，否则替换数据
								if (isLoadMore) {
									this.chapters = [...this.chapters, ...newChapters];
									// 设置当前索引为新追加的第一章
									this.currentIndex = this.chapters.length - newChapters.length;
								} else {
									this.chapters = newChapters;
									this.currentIndex = 0;
								}
								
								// 检查是否还有更多数据
								this.hasMore = chaptersData.length === this.pageSize;
							} else {
								// 没有更多数据
								this.hasMore = false;
								if (!isLoadMore) {
									this.chapters = [];
								}
							}
						} else {
							console.error('获取章节数据失败:', res);
							uni.showToast({ title: '获取章节数据失败', icon: 'none' });
						}
					},
					fail: (err) => {
						this.loading = false;
						console.error('获取章节内容失败:', err);
						uni.showToast({ title: '获取章节内容失败', icon: 'none' });
					}
				});
			},
			loadNextPage() {
				this.currentPage++;
				this.getChapters(this.bookId, this.currentPage, true);
			},
			/**
			 * 显示分享弹窗
			 */
			showShareModal() {
				this.showShareModalVisible = true;
				this.getShareLink();
			},
			/**
			 * 关闭分享弹窗
			 */
			closeShareModal() {
				this.showShareModalVisible = false;
			},
			/**
			 * 获取分享链接
			 */
			getShareLink() {
				if (!this.bookId) {
					uni.showToast({
						title: '书籍ID不存在',
						icon: 'none'
					});
					return;
				}
				
				uni.showLoading({ title: '生成中...' });
				
				getShare(this.bookId).then(res => {
					uni.hideLoading();
					if (res) {
						this.shareLink = res;
					} else {
						uni.showToast({
							title: '获取分享链接失败',
							icon: 'none'
						});
					}
				}).catch(error => {
					uni.hideLoading();
					console.error('获取分享链接失败:', error);
					uni.showToast({
						title: '获取分享链接失败',
						icon: 'none'
					});
				});
			},
			/**
			 * 复制分享链接
			 */
			copyShareLink() {
				if (!this.shareLink) {
					uni.showToast({
						title: '分享链接为空',
						icon: 'none'
					});
					return;
				}
				
				uni.setClipboardData({
					data: this.shareLink,
					success: () => {
						uni.showToast({
							title: '复制成功',
							icon: 'success'
						});
					},
					fail: () => {
						uni.showToast({
							title: '复制失败',
							icon: 'none'
						});
					}
				});
			}
		},
		onLoad(options) {
			if (options.bookId) {
				this.bookId = options.bookId;
				// 根据书籍 ID 获取章节内容
				this.getChapters(this.bookId);
			}
		},
		// 监听返回按钮点击事件
		onBackPress() {
			// 直接返回上一页
			uni.navigateBack({
				delta: 1,
				fail: (error) => {
					console.error('返回上一页失败:', error);
					// 如果返回失败，尝试跳转到首页
					uni.navigateTo({
						url: '/pages/index'
					});
				}
			});
			// 返回true，阻止默认返回行为
			return true;
		}
	}
</script>

<style>
	.book-container {
		padding: 20rpx;
		padding-top: 120rpx; /* 20rpx + 100rpx 安全区域 */
		min-height: 100vh;
		background-color: var(--bg-color-primary);
		position: relative;
	}
	
	/* #ifdef H5 */
	/* H5端特殊处理 - 确保返回按钮不被遮挡 */
	.book-container {
		/* 为返回按钮留出足够的点击空间 */
		padding-top: calc(120rpx + constant(safe-area-inset-top));
		padding-top: calc(120rpx + env(safe-area-inset-top));
	}
	/* #endif */
	
	/* 返回按钮样式 */
	.back-button {
		display: flex;
		align-items: center;
		position: absolute;
		top: 20rpx;
		left: 20rpx;
		padding: 10rpx 20rpx;
		background-color: var(--bg-color-secondary);
		border-radius: 30rpx;
		box-shadow: 0 2rpx 8rpx var(--shadow-color-light);
		z-index: 9999;
		cursor: pointer;
		transition: all 0.3s ease;
		/* #ifdef H5 */
		/* H5端增强点击区域 */
		min-width: 80rpx;
		min-height: 60rpx;
		-webkit-tap-highlight-color: transparent;
		user-select: none;
		/* #endif */
	}
	
	.back-button-hover {
		background-color: var(--bg-color-tertiary);
		transform: scale(0.98);
		box-shadow: 0 4rpx 12rpx var(--shadow-color-medium);
	}
	
	.back-text {
		font-size: 26rpx;
		color: var(--text-color-primary);
		margin-left: 8rpx;
		font-weight: 500;
	}
	
	.chapter-title {
		font-size: 32rpx;
		font-weight: bold;
		text-align: center;
		margin: 40rpx 0 30rpx 0;
		color: var(--text-color-primary);
		padding: 0 40rpx;
	}
	
	.chapter-content {
		font-size: 28rpx;
		line-height: 48rpx;
		color: var(--text-color-secondary);
		background-color: var(--bg-color-secondary);
		padding: 30rpx;
		border-radius: 10rpx;
		box-shadow: 0 2rpx 10rpx var(--shadow-color-light);
		min-height: 60vh;
	}
	
	.chapter-nav {
		display: flex;
		justify-content: space-between;
		margin-top: 40rpx;
		padding: 0 20rpx;
	}
	
	.chapter-nav button {
		width: 150rpx;
		height: 60rpx;
		font-size: 24rpx;
		border-radius: 30rpx;
		background-color: var(--theme-blue);
		color: #ffffff;
		border: none;
	}
	
	.chapter-nav .share-btn {
		background: linear-gradient(135deg, #10b981 0%, #059669 100%);
		box-shadow: 0 2rpx 8rpx rgba(16, 185, 129, 0.3);
	}
	
	.chapter-nav .share-btn:hover {
		box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.4);
		transform: translateY(-2rpx);
	}
	
	.chapter-nav button:disabled {
		background-color: var(--border-color-medium);
	}
	
	/* 护眼模式 */
	.eye-protection-mode .back-button {
		background-color: var(--bg-color-secondary);
		box-shadow: 0 2rpx 8rpx var(--shadow-color-light);
	}
	
	.eye-protection-mode .back-button-hover {
		background-color: var(--bg-color-tertiary);
		box-shadow: 0 4rpx 12rpx var(--shadow-color-medium);
	}
	
	.eye-protection-mode .back-text {
		color: var(--text-color-primary);
	}
	
	.eye-protection-mode .chapter-title {
		color: var(--text-color-primary);
	}
	
	.eye-protection-mode .chapter-content {
		color: var(--text-color-secondary);
		background-color: var(--bg-color-secondary);
		box-shadow: 0 2rpx 10rpx var(--shadow-color-light);
	}
	
	.eye-protection-mode .chapter-nav button {
		background-color: var(--theme-blue);
	}
	
	.eye-protection-mode .chapter-nav button:disabled {
		background-color: var(--border-color-medium);
	}
	
	/* 分享弹窗样式 */
	.share-modal-overlay {
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
	
	.share-modal {
		width: 600rpx;
		background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
		border-radius: 32rpx;
		overflow: hidden;
		box-shadow: 0 12rpx 48rpx rgba(0, 0, 0, 0.3);
		animation: modalSlideIn 0.3s ease;
	}
	
	.share-modal .modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 32rpx 40rpx;
		background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
		border-bottom: 2rpx solid #93c5fd;
	}
	
	.share-modal .modal-title {
		font-size: 36rpx;
		font-weight: 700;
		color: #1e40af;
	}
	
	.share-modal .close-btn {
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
	
	.share-modal .close-btn:hover {
		background: #bfdbfe;
		transform: rotate(90deg);
	}
	
	.share-modal .modal-body {
		padding: 48rpx 40rpx;
	}
	
	.share-content {
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}
	
	.share-label {
		font-size: 28rpx;
		color: #334155;
		font-weight: 500;
	}
	
	.share-link-box {
		display: flex;
		align-items: center;
		gap: 16rpx;
		background: #f1f5f9;
		padding: 24rpx;
		border-radius: 16rpx;
		border: 2rpx solid #e2e8f0;
	}
	
	.share-link-text {
		flex: 1;
		font-size: 26rpx;
		color: #475569;
		word-break: break-all;
		line-height: 1.6;
	}
	
	.copy-btn {
		padding: 12rpx 24rpx;
		background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
		border: none;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 24rpx;
		font-weight: 500;
		color: #ffffff;
		box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.3);
		transition: all 0.3s ease;
		white-space: nowrap;
	}
	
	.copy-btn:hover {
		box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.4);
		transform: translateY(-2rpx);
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
	
	/* 夜间模式下的分享弹窗 */
	.dark-mode .share-modal-overlay {
		background-color: rgba(0, 0, 0, 0.75);
	}
	
	.dark-mode .share-modal {
		background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
	}
	
	.dark-mode .share-modal .modal-header {
		background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
		border-bottom-color: #1e3a8a;
	}
	
	.dark-mode .share-modal .close-btn:hover {
		background: #1e3a8a;
	}
	
	.dark-mode .share-label {
		color: #cbd5e1;
	}
	
	.dark-mode .share-link-box {
		background: #334155;
		border-color: #475569;
	}
	
	.dark-mode .share-link-text {
		color: #e2e8f0;
	}
</style>
