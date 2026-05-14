<template>
  <view class="container">
    <view class="page">
      <!-- 搜索栏 -->
      <view class="search-container">
        <view class="search-box">
          <uni-icons type="search" size="28" color="#94a3b8" class="search-icon"></uni-icons>
          <input v-model="keyword" placeholder="搜索地理位置" class="search-input" @input="performSearch" />
          <button v-if="keyword" @click="clearSearch" class="clear-btn">×</button>
        </view>
        <view v-if="showSuggestions && searchResults.length > 0" class="search-results">
          <view v-for="poi in searchResults" :key="poi.id" class="search-result-item" @click="atPoi(poi)">
            <uni-icons type="location" size="24" color="#1677ff" class="result-icon"></uni-icons>
            <view class="result-content">
              <text class="result-name">{{ poi.name }}</text>
              <text class="result-address">{{ poi.address || poi.location }}</text>
            </view>
          </view>
        </view>
        <view v-if="showSuggestions && searchResults.length === 0" class="search-results">
          <view class="search-result-item empty">
            <text>暂无相关结果</text>
          </view>
        </view>
      </view>

      <!-- 地图 -->
      <view class="map-container">
        <!-- #ifdef H5 -->
        <view id="amap" class="amap-container"></view>
        <text v-if="!isH5MapLoaded" class="map-placeholder">地图加载中...</text>
        <view class="map-zoom-controls">
          <button class="zoom-btn zoom-in" @click="zoomIn">+</button>
          <button class="zoom-btn zoom-out" @click="zoomOut">-</button>
        </view>
        <!-- #endif -->

        <!-- #ifdef MP-WEIXIN -->
        <map
            id="wxmap"
            class="wxmap-container"
            :latitude="mapCenter.lat"
            :longitude="mapCenter.lng"
            :scale="mapScale"
            :markers="mapMarkers"
            :polyline="mapPolyline"
            :show-location="true"
            @tap="onMapTap"
            @markertap="onMarkerTap"
        ></map>
        <!-- #endif -->
      </view>

      <!-- 位置信息 -->
      <view v-if="userLocation" class="location-info">
        <view class="location-content">
          <view class="location-header">
            <uni-icons type="location" size="28" color="#1677ff" class="location-icon"></uni-icons>
            <text class="location-title">当前位置</text>
          </view>
          <text class="location-address">{{ userLocation.lng.toFixed(6) + ', ' + userLocation.lat.toFixed(6) }}</text>
        </view>
        <button class="refresh-btn" @click="getCurrentLocation">
          <uni-icons type="refresh" size="24" color="#1677ff"></uni-icons>
          <text>刷新</text>
        </button>
      </view>
      <view v-else-if="isLocating" class="location-info locating">
        <view class="location-content">
          <view class="location-header">
            <uni-icons type="refresh" size="28" color="#999"></uni-icons>
            <text class="location-title">正在定位...</text>
          </view>
          <text class="location-address">请稍候，正在获取您的位置</text>
        </view>
      </view>
      <view v-else class="location-info no-location">
        <view class="location-content">
          <view class="location-header">
            <uni-icons type="info" size="28" color="#ff9500"></uni-icons>
            <text class="location-title">位置未获取</text>
          </view>
          <text class="location-address">{{ locationError || '请点击地图选择位置或点击刷新' }}</text>
        </view>
        <button class="refresh-btn" @click="getCurrentLocation">
          <uni-icons type="refresh" size="24" color="#1677ff"></uni-icons>
          <text>刷新</text>
        </button>
      </view>

      <!-- 模式选择 -->
      <view class="settings-list-item" @click="toggleCulturalMode" :class="{ disabled: isNavigating }">
        <view class="settings-list-header">
          <view class="settings-list-left">
            <uni-icons type="compass" size="28" color="#1677ff"></uni-icons>
            <text class="settings-list-title">{{ mode === 1 ? '文化导览模式' : '小说模式' }}</text>
          </view>
          <view class="distance-selector compact">
            <view v-for="option in culturalModeOptions" :key="option.value" class="distance-option"
                  :class="{ active: mode === (option.value ? 1 : 0), disabled: isNavigating }" @click.stop="selectCulturalMode(option.value)">
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 文化导览设置 -->
      <view v-if="mode === 1" class="settings-list-item" @click="toggleCulturalSettings">
        <view class="settings-list-header">
          <view class="settings-list-left">
            <uni-icons type="settings" size="28" color="#1677ff"></uni-icons>
            <text class="settings-list-title">文化导览点一点</text>
          </view>
          <uni-icons :type="showCulturalSettings ? 'up' : 'down'" size="24" color="#94a3b8"></uni-icons>
        </view>
        <view v-if="showCulturalSettings" class="novel-settings-panel">
          <view class="setting-row" >
            <text class="setting-label">导览模式</text>
            <text class="setting-value">文化手指 - 拥有一次选点让AI以此为中心谈论的机会</text>
            <button class="select-point-btn" @click="toggleSelectPointMode">{{ isSelectingPoint ? '取消选点' : '选择中心点' }}</button>
          </view>
        </view>
      </view>

      <!-- 文化导览点回来按钮 -->
      <view v-if="mode === 1 && showBackButton" class="settings-list-item back-button-item">
        <button class="back-to-area-btn" @click="handleBackToArea">
          <uni-icons type="back" size="28" color="#1677ff"></uni-icons>
          <text>文化导览点回来</text>
        </button>
      </view>

      <!-- 小说设置 -->
      <view v-if="mode === 0" class="settings-list-item" @click="toggleNovelSettings">
        <view class="settings-list-header">
          <view class="settings-list-left">
            <uni-icons type="settings" size="28" color="#1677ff"></uni-icons>
            <text class="settings-list-title">小说设置</text>
          </view>
          <uni-icons :type="showNovelSettings ? 'up' : 'down'" size="24" color="#94a3b8"></uni-icons>
        </view>
      </view>

      <!-- 小说设置展开内容 -->
      <view v-if="mode === 0 && showNovelSettings" class="novel-settings-panel">
        <view class="setting-row">
          <text class="setting-label">小说类型</text>
          <view class="distance-selector compact">
            <view v-for="option in novelTypeOptions" :key="option.value" class="distance-option"
                  :class="{ active: selectedNovelType === option.value }" @click="selectNovelType(option.value)">
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
        <view class="setting-row">
          <text class="setting-label">小说篇幅</text>
          <view class="distance-selector compact">
            <view v-for="option in novelLengthOptions" :key="option.value" class="distance-option"
                  :class="{ active: selectedNovelLength === option.value }" @click="selectNovelLength(option.value)">
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
        <view class="setting-row">
          <text class="setting-label">通行方式</text>
          <view class="distance-selector compact">
            <view v-for="option in byWayOptions" :key="option.value" class="distance-option"
                  :class="{ active: selectedByWay === option.value }" @click="selectByWay(option.value)">
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
        <view class="setting-row">
          <text class="setting-label">限速模式</text>
          <view class="distance-selector compact">
            <view v-for="option in speedLimitOptions" :key="option.value" class="distance-option"
                  :class="{ active: selectedSpeedLimit === option.value }" @click="selectSpeedLimit(option.value)">
              <text>{{ option.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 导航按钮 -->
      <button v-if="isNavigating && continueListenBookId" class="cancel-continue-listen-btn" @click="cancelContinueListen"
              :disabled="isProcessing">
        <uni-icons type="close" size="32" color="#ffffff"></uni-icons>
        <text>取消继续听</text>
      </button>

      <button class="start-navigation-btn" :class="{ active: isNavigating }" @click="toggleNavigation"
              :disabled="isProcessing || !selectedDestination">
        <uni-icons :type="isNavigating ? 'close' : 'navigate'" size="32" color="#ffffff"></uni-icons>
        <text>{{ getButtonText }}</text>
      </button>

      <button v-if="isNavigating" class="pause-navigation-btn" :class="{ active: isPaused }" @click="togglePause"
              :disabled="isProcessing">
        <uni-icons :type="isPaused ? 'play' : 'pause'" size="32" color="#ffffff"></uni-icons>
        <text>{{ isPaused ? '继续播放' : '暂停播放' }}</text>
      </button>
    </view>

    <!-- 加载遮罩 -->
    <view v-if="loading" class="loading-overlay">
      <view class="loading-content">
        <view class="loader"></view>
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getArea, getPoi, getUserText, startNavigationNoBook, isNavigation, getDrivingRoute } from '@/api/normal/graph'
import { startNavigationWithPoll, pollTask, startNavigation, getChapter, getArea as getAreaAsync, getPoi as getPoiAsync, pollingManager, getTaskResult } from '@/api/normal/navigation'
import {
  createSpeechUtterance,
  performSpeech,
  handleNonH5Speech,
  cleanupTimersAndWatchers,
  stopNavigation,
  calculateDistance,
  startSpeedDetection
} from '@/utils/navigation.js';

import {
  initAMapSecurity,
  loadAMapScript,
  searchPlace,
  getCurrentPosition,
  getAMapLocation,
  createMapInstance,
  loadMapPlugins,
  drawNavigationPath,
  addMarker,
  clearOverlays,
  setMapCenter,
  zoomMap,
  initGeolocation,
  initPlaceSearch,
  updateUserMarker,
  bindMapClickEvent,
  destroyMap,
  normalizeLocation,
  isValidLocation
} from '@/utils/map.js'

import { getSafeAreaInfo } from '@/utils/common.js'

export default {
  data() {
    return {
      timer: null,
      speechTimer: null,
      positionWatcher: null,
      lastPosition: null,
      lastTimestamp: 0,
      isNavigating: false,
      isPaused: false,
      isProcessing: false,
      isH5MapLoaded: false,
      currentSpeechQueue: [],
      currentSpeakingText: null,
      currentSpeechIndex: 0,
      map: null,
      AMap: null,
      geolocation: null,
      placeSearch: null,
      driving: null,
      // 微信小程序地图数据
      mapCenter: { lng: 116.397428, lat: 39.90923 },
      mapScale: 15,
      mapMarkers: [],
      mapPolyline: [],
      userLocation: null,
      userMarker: null,
      isLocating: false,
      locationError: null,
      keyword: "",
      showSuggestions: false,
      searchResults: [],
      selectedDestination: '',
      nextInstruction: "等待导航开始...",
      navigationParams: null,
      currentBookId: null,
      cacheKey: null,
      selectedNovelType: '随机',
      novelTypeOptions: [
        { value: '随机', label: '随机' },
        { value: '玄幻', label: '玄幻' },
        { value: '恐怖', label: '恐怖' },
        { value: '言情', label: '言情' },
        { value: '奇幻', label: '奇幻' }
      ],
      selectedNovelLength: 'short',
      novelLengthOptions: [
        { value: 'short', label: '短篇' },
        { value: 'medium', label: '中篇' },
        { value: 'long', label: '长篇' }
      ],
      selectedByWay: 'unlimited',
      byWayOptions: [
        { value: 'unlimited', label: '不设限' },
        { value: 'walking', label: '步行' },
        { value: 'cycling', label: '骑行' },
        { value: 'car', label: '汽车' },
        { value: 'train', label: '火车' },
        { value: 'plane', label: '飞机' }
      ],
      selectedSpeedLimit: 'off',
      speedLimitOptions: [
        { value: 'off', label: '关闭' },
        { value: 'low', label: '低速 (≤5km/h)' },
        { value: 'medium', label: '中速 (≤15km/h)' },
        { value: 'high', label: '高速 (≤30km/h)' }
      ],
      culturalModeOptions: [
        { value: false, label: '说' },
        { value: true, label: '览' }
      ],
      currentSpeed: 0,
      speedLimitThreshold: 5,
      loading: false,
      darkMode: false,
      mode: 0,
      showNovelSettings: false,
      isCulturalMode: false,
      culturalTimer: null,
      culturalTimestamps: [],
      showCulturalSettings: false,
      isSelectingPoint: false,
      selectedPointLocation: null,
      speechTextQueue: [],
      requestTasks: [],
      poiRequestTimer: null,
      isPoiRequesting: false,
      // 轮询任务管理
      currentCulturalTask: null, // 当前文化导览轮询任务
      currentPoiTask: null, // 当前 POI 轮询任务
      // 继续听相关
      continueListenBookId: null,
      // 文化导览应用的 content
      appliedContent: null,
      // 文化导览返回按钮相关
      showBackButton: false,
      savedCacheKey: null,
      // 持续定位相关
      locationUpdateTimer: null,
      locationUpdateInterval: 5000,  // 5秒更新一次位置
      // 微信小程序音频上下文
      audioContext: null,
      // 队列架构：文本队列 → 音频播放队列
      speechTextQueue: [],           // 文本队列：存储原始文本
      ttsAudioQueue: [],             // 音频播放队列：已转换完成的音频 {text, audioUrl, index}
      // 是否正在播放音频
      isAudioPlaying: false,
      // 是否正在处理TTS转换
      isTTSConverting: false,
      // 🔧 pollTask 外部控制开关(默认关闭,调用时才开启)
      culturalPollControl: { enabled: false },
      poiPollControl: { enabled: false },
      // TTS 长文本回调
      longTextCallback: null,
    };
  },
  
  /**
   * H5端顶部安全区域适配 - 页面加载时执行
   */
  async onLoad() {
    // #ifdef H5
    try {
      console.log('[H5适配] 📱 开始获取系统信息');
      const safeAreaInfo = await getSafeAreaInfo();
      console.log('[H5适配] ✅ 系统信息获取成功:', safeAreaInfo);
      
      // 根据设备类型动态调整样式
      if (safeAreaInfo.safeTop > 0) {
        console.log(`[H5适配] 🎯 检测到刘海屏，顶部安全距离: ${safeAreaInfo.safeTop}px`);
        // 可以在这里动态设置 CSS 变量或样式
        document.documentElement.style.setProperty('--safe-area-top', `${safeAreaInfo.safeTop}px`);
      } else {
        console.log('[H5适配] ℹ️ 非刘海屏设备，使用默认样式');
      }
    } catch (error) {
      console.error('[H5适配] ❌ 获取系统信息失败:', error);
    }
    // #endif
  },
  computed: {
    currentTime() {
      const now = new Date();
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
    },
    getButtonText() {
      if (this.isProcessing) return '处理中...';
      return this.isNavigating ? '停止导航' : '开始导航';
    }
  },
  methods: {
    async initMap() {
      // #ifdef H5
      initAMapSecurity();

      try {
        const AMap = await loadAMapScript();
        this.AMap = AMap;

        if (window.speechSynthesis) {
          window.speechSynthesis.onvoiceschanged = () => {
            // 语音列表加载完成
          };
        }

        this.createMapInstance();
      } catch (error) {
        uni.showToast({ title: '地图加载失败', icon: 'none' });
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      // 微信小程序不需要初始化地图，直接使用 map 组件
      this.isH5MapLoaded = true;
      // 自动定位
      this.autoLocateAndSearch();
      // #endif
    },
    createMapInstance() {
      // #ifdef H5
      try {
        this.map = createMapInstance('amap');
        this.isH5MapLoaded = true;

        bindMapClickEvent(this.map, ({ lng, lat }) => {
          if (this.isSelectingPoint) {
            clearOverlays(this.map, 'marker');
            const marker = addMarker(this.map, {
              position: [lng, lat],
              title: '中心点',
              icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png'
            });
            this.selectedPointLocation = { lng, lat };
            this.isSelectingPoint = false;
            // 显示返回按钮
            this.showBackButton = true;
            uni.showToast({
              title: `已设置中心点：[${lng.toFixed(4)}, ${lat.toFixed(4)}]`,
              icon: 'success',
              duration: 2000
            });
            // 开始循环请求 POI
            this.startPoiPolling(lng, lat);
          } else {
            clearOverlays(this.map, 'marker');
            const marker = addMarker(this.map, {
              position: [lng, lat],
              title: '点击位置',
              icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png'
            });
            this.atPoi({
              id: `click_${Date.now()}`,
              name: '点击位置',
              location: `${lng},${lat}`
            });
            uni.showToast({
              title: `已选择：[${lng.toFixed(4)}, ${lat.toFixed(4)}]`,
              icon: 'none',
              duration: 2000
            });
          }
        });

        setTimeout(() => {
          Promise.all([
            new Promise(resolve => {
              this.geolocation = initGeolocation(this.map);
              resolve();
            }),
            new Promise(resolve => {
              this.placeSearch = initPlaceSearch();
              resolve();
            })
          ]).then(() => {
            this.autoLocateAndSearch();
          });
        }, 200);
      } catch (error) {
        uni.showToast({ title: '地图初始化失败', icon: 'none' });
      }
      // #endif
    },

    autoLocateAndSearch() {
      this.isLocating = true;
      this.locationError = null;
      getCurrentPosition()
          .then((location) => {
            
            // 验证位置数据
            if (!isValidLocation(location)) {
              this.isLocating = false;
              this.locationError = '位置数据格式错误';
              uni.showToast({ title: '定位数据异常', icon: 'none', duration: 2000 });
              return;
            }
            
            this.isLocating = false;
            const { lng, lat } = location;
            this.userLocation = { lng, lat };
            uni.showToast({ title: '定位成功', icon: 'success', duration: 1500 });
            
            // #ifdef H5
            if (this.map) {
              setMapCenter(this.map, [lng, lat]);
              this.userMarker = updateUserMarker(this.map, location, this.userMarker);
            }
            // #endif
            
            // #ifdef MP-WEIXIN
            this.mapCenter = { lng, lat };
            this.updateUserMarker(location);
            // #endif
          })
          .catch((error) => {
            this.isLocating = false;
            this.locationError = error.message || '定位失败';
            if (!this.userLocation) {
              uni.showToast({ title: '定位失败，请点击地图选择位置', icon: 'none', duration: 3000 });
              // #ifdef H5
              // 只有在地图已初始化时才尝试高德地图定位
              if (this.AMap && this.map) {
                this.tryAMapLocation().catch(err => {
                  // 高德地图定位也失败了
                });
              }
              // #endif
            } else {
              uni.showToast({ title: '定位失败，保持上次位置', icon: 'none', duration: 2000 });
            }
          });
    },
    getCurrentLocation() {
      return new Promise((resolve, reject) => {
        uni.showToast({ title: '获取位置...', icon: 'loading', duration: 2000 });
        this.isLocating = true;
        this.locationError = null;
        getCurrentPosition()
            .then((location) => {
              
              // 验证位置数据
              if (!isValidLocation(location)) {
                this.isLocating = false;
                this.locationError = '位置数据格式错误';
                uni.showToast({ title: '定位数据异常', icon: 'none', duration: 2000 });
                reject(new Error('位置数据格式错误'));
                return;
              }
              
              this.isLocating = false;
              const { lng, lat } = location;
              this.userLocation = { lng, lat };
              uni.showToast({ title: '定位成功', icon: 'success', duration: 1500 });
              
              // #ifdef H5
              if (this.map) {
                setMapCenter(this.map, [lng, lat]);
                this.userMarker = updateUserMarker(this.map, location, this.userMarker);
              }
              // #endif
              
              // #ifdef MP-WEIXIN
              this.mapCenter = { lng, lat };
              this.updateUserMarker(location);
              // #endif
              
              resolve({ lng, lat });
            })
            .catch((error) => {
              this.isLocating = false;
              this.locationError = error.message || '定位失败';
              if (!this.userLocation) {
                uni.showToast({ title: '定位失败，请点击地图选择位置', icon: 'none', duration: 2000 });
                // #ifdef H5
                // 只有在地图已初始化时才尝试高德地图定位
                if (this.AMap && this.map) {
                  this.tryAMapLocation().then(resolve).catch(reject);
                } else {
                  reject(new Error('地图未初始化，无法定位'));
                }
                // #endif
                // #ifdef MP-WEIXIN
                reject(new Error('定位失败'));
                // #endif
              } else {
                uni.showToast({ title: '定位失败，保持上次位置', icon: 'none', duration: 1500 });
                resolve(this.userLocation);
              }
            });
      });
    },

    async performSearch() {
      const keyword = this.keyword.trim();
      if (!keyword) {
        this.searchResults = [];
        return;
      }

      const results = await searchPlace(keyword);
      this.searchResults = results;
      this.showSuggestions = results.length > 0;
    },

    atPoi(poi) {
      const [lng, lat] = poi.location.split(',').map(Number);
      this.selectedDestination = poi;
      this.showSuggestions = false;
      this.keyword = poi.name;
      
      // #ifdef H5
      if (this.map) {
        setMapCenter(this.map, [lng, lat]);
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      this.mapCenter = { lng, lat };
      this.mapScale = 16; // 放大地图
      
      // 添加目的地标记（id 必须是数字）
      this.mapMarkers = this.mapMarkers.filter(m => m.id !== 999);
      this.mapMarkers.push({
        id: 999,
        latitude: lat,
        longitude: lng,
        iconPath: '/static/images/end-marker.png',
        width: 35,
        height: 35,
        callout: {
          content: poi.name,
          color: '#FFFFFF',
          fontSize: 12,
          borderRadius: 4,
          bgColor: '#ff4d4f',
          padding: 5,
          display: 'ALWAYS'
        }
      });
      // #endif
    },

    clearSearch() {
      this.keyword = '';
      this.searchResults = [];
      this.showSuggestions = false;
    },

    async startNavigation() {
      // 🔧 关键修复：防止重复启动导航任务（请求独一性保证）
      if (this.isNavigating) {
        console.warn('[导航启动] ⚠️ 已有导航任务在进行中，拒绝重复启动');
        uni.showToast({ title: '已有导航任务在进行中', icon: 'none', duration: 2000 });
        return;
      }
      
      if (this.isProcessing) {
        console.warn('[导航启动] ⚠️ 正在处理中，请稍候');
        uni.showToast({ title: '正在处理中，请稍候', icon: 'none', duration: 2000 });
        return;
      }
      
      console.log('[导航启动] 🚀 开始启动导航任务');
      
      if (this.timer) { clearInterval(this.timer); this.timer = null; }
      if (this.culturalTimer) { clearInterval(this.culturalTimer); this.culturalTimer = null; this.culturalTimestamps = []; }
      if (this.positionWatcher) { navigator.geolocation.clearWatch(this.positionWatcher); this.positionWatcher = null; }
      this.clearAllSpeechContent();
      this.isProcessing = true;

      try {
        if (!this.selectedDestination) {
          this.isProcessing = false;
          return;
        }
        if (!this.selectedDestination.location) {
          this.isProcessing = false;
          return;
        }

        const dest = this.selectedDestination;
        let origin;
        if (this.userLocation) {
          origin = { lng: this.userLocation.lng, lat: this.userLocation.lat };
        } else {
          uni.showToast({ title: '请先获取当前位置或点击地图选择起点', icon: 'none', duration: 3000 });
          this.isProcessing = false;
          return;
        }

        this.drawNavigationPath(origin, dest);
        let destination;
        try {
          const [destLng, destLat] = dest.location.split(',').map(Number);
          destination = { longitude: destLng, latitude: destLat };
        } catch (error) {
          this.isProcessing = false;
          return;
        }

        let wordsTypeValue;
        switch (this.selectedNovelLength) {
          case 'short': wordsTypeValue = 30; break;
          case 'medium': wordsTypeValue = 80; break;
          case 'long': wordsTypeValue = 230; break;
          default: wordsTypeValue = 30;
        }

        const navigationData = {
          origin: { longitude: origin.lng, latitude: origin.lat },
          destination: destination,
          type: this.selectedNovelType,
          wordsType: wordsTypeValue,
          byWay: this.selectedByWay,
          userId: 1
        };

        this.isNavigating = true;
        this.isProcessing = false;
        this.startSpeedDetection();
        this.nextInstruction = `前往 ${dest.name}`;
        
        // #ifdef MP-WEIXIN
        // 微信小程序：启动持续定位更新
        this.startPositionUpdate();
        // #endif

        if (this.mode === 1) {
          // 文化导览模式：先调用 getUser，再外部轮询异步 getArea
          console.log('[文化导览] 🚀 启动文化导览模式');
          
          // 🔧 关键修复：互斥控制 - 关闭 POI 轮询，开启文化导览轮询
          this.poiPollControl.enabled = false;
          this.culturalPollControl.enabled = true;
          console.log('[文化导览] ✅ culturalPollControl.enabled:', this.culturalPollControl.enabled);
          console.log('[文化导览] 🚫 poiPollControl.enabled:', this.poiPollControl.enabled);
          
          try {
            // 1. 先调用 getUser
            console.log('[文化导览] 🔄 开始调用 getUser...');
            const userText = await getUserText(navigationData);
            let textToSpeak = userText;
            // 优先使用 data 字段的内容
            if (userText && typeof userText === 'object' && userText.data && typeof userText.data === 'string') {
              textToSpeak = userText.data;
              console.log('[文化导览] ✓ 使用 data 字段内容，长度:', textToSpeak.length);
            } else if (userText && typeof userText === 'string') {
              console.log('[文化导览] ✓ getUser 返回成功，长度:', userText.length);
            }
            if (textToSpeak && typeof textToSpeak === 'string' && this.isNavigating) {
              this.addTextToSpeechQueue(textToSpeak);
            }
          } catch (error) {
            console.error('[文化导览] ✗ getUser 失败:', error);
          }
          
          // 2. 重置 cacheKey，确保第一次调用不传 cacheKey
          this.cacheKey = null;
          
          // 3. 启动外部轮询异步 getArea
          console.log('[文化导览] 🔄 启动 fetchCulturalInfo 轮询...');
          this.fetchCulturalInfo();
        } else {
          // 小说模式（mode === 0）
          console.log('[小说模式] 🚀 启动小说模式');
          
          // 🔧 关键修复：互斥控制 - 关闭文化导览和 POI 轮询
          this.culturalPollControl.enabled = false;
          this.poiPollControl.enabled = false;
          console.log('[小说模式] 🚫 culturalPollControl.enabled:', this.culturalPollControl.enabled);
          console.log('[小说模式] 🚫 poiPollControl.enabled:', this.poiPollControl.enabled);
          
          // 检查是否存在继续听的书籍 ID
          const hasContinueListen = this.continueListenBookId !== null && this.continueListenBookId !== undefined && this.continueListenBookId !== '';
          
          if (hasContinueListen) {
            // 继续听模式：按照新逻辑处理
            await this.handleContinueListen(navigationData, dest);
          } else {
            // 正常模式：按原来逻辑处理
            await this.handleNormalNavigation(navigationData);
          }
        }
      } catch (error) {
        uni.showToast({ title: `失败：${error.message}`, icon: 'none' });
        this.isProcessing = false;
      }
    },

    toggleNavigation() {
      // 🔧 关键修复：防止快速连续点击导致并发问题
      if (this.isProcessing) {
        console.warn('[导航切换] ⚠️ 正在处理中，忽略本次点击');
        return;
      }
      
      if (this.isNavigating) {
        console.log('[导航切换] 🛑 停止当前导航');
        this.stopNavigation();
      } else {
        console.log('[导航切换] ▶️ 启动新导航');
        this.startNavigation();
      }
    },

    // ... existing code ...
    async stopNavigation() {
      console.log('=== [停止导航] 开始 ===');
      console.log('[停止导航] 当前 mode:', this.mode);
      console.log('[停止导航] 是否为文化导览模式 (mode === 1):', this.mode === 1);
      console.log('[停止导航] isNavigating:', this.isNavigating);
      
      // 检查是否在文化导览模式
      if (this.mode === 1) {
        console.log('[停止导航] ✓ 进入文化导览模式分支');
        console.log('[停止导航] 准备弹出收藏确认框');
        
        // 弹出确认框询问是否收藏这段旅程
        uni.showModal({
          title: '收藏旅程',
          content: '是否收藏这段旅程？',
          success: async (res) => {
            console.log('=== [停止导航-弹窗回调] 开始 ===');
            console.log('[停止导航-弹窗回调] 用户选择 confirm:', res.confirm);
            console.log('[停止导航-弹窗回调] 用户选择 cancel:', res.cancel);
            
            try {
              // userId 默认为 1
              const userId = 1;
              console.log('[停止导航-弹窗回调] 使用默认 userId:', userId);
              console.log('[停止导航-弹窗回调] 调用参数 - userId:', userId, ', isNavigation:', res.confirm);
              
              // 根据用户选择调用 isNavigation API
              console.log('[停止导航-弹窗回调] 开始调用 isNavigation API...');
              const result = await isNavigation(userId, res.confirm);
              console.log('[停止导航-弹窗回调] ✓ isNavigation API 调用成功!');
              console.log('[停止导航-弹窗回调] API 返回结果:', JSON.stringify(result));
              
              if (res.confirm) {
                console.log('[停止导航-弹窗回调] 显示收藏成功提示');
                uni.showToast({ 
                  title: '已收藏这段旅程', 
                  icon: 'success' 
                });
              } else {
                console.log('[停止导航-弹窗回调] 显示取消收藏提示');
                uni.showToast({ 
                  title: '已取消收藏', 
                  icon: 'none' 
                });
              }
            } catch (error) {
              console.error('=== [停止导航-弹窗回调] API 调用异常 ===');
              console.error('[停止导航-弹窗回调] 错误对象:', error);
              console.error('[停止导航-弹窗回调] 错误消息:', error.message);
              console.error('[停止导航-弹窗回调] 错误堆栈:', error.stack);
              uni.showToast({ 
                title: '收藏失败，请重试', 
                icon: 'none' 
              });
            } finally {
              console.log('[停止导航-弹窗回调] 执行 finally，调用 performStopNavigation');
              // 无论收藏成功与否，都执行停止导航的逻辑
              this.performStopNavigation();
            }
          },
          fail: (err) => {
            console.error('[停止导航] ✗ 弹窗调用失败:', err);
            // 如果弹窗失败，直接停止导航
            this.performStopNavigation();
          }
        });
      } else {
        console.log('[停止导航] ✗ 非文化导览模式 (mode !== 1)，直接停止导航');
        // 非文化导览模式，直接停止导航
        this.performStopNavigation();
      }
    },
    
    /**
     * 执行停止导航的实际逻辑（增强版 - 杀死所有请求）
     */
    performStopNavigation() {
      console.log('[停止导航] 🛑 开始执行停止导航逻辑');
      console.log('[停止导航] 🔍 当前状态:');
      console.log('[停止导航]   - isNavigating:', this.isNavigating);
      console.log('[停止导航]   - mode:', this.mode);
      console.log('[停止导航]   - timer:', !!this.timer);
      console.log('[停止导航]   - culturalTimer:', !!this.culturalTimer);
      console.log('[停止导航]   - poiRequestTimer:', !!this.poiRequestTimer);
      
      // 🔧 关键修复：使用 PollingManager 停止所有轮询任务
      console.log('[停止导航] 🚫 第零步：使用 PollingManager 停止所有轮询');
      pollingManager.stopAllPolling();
      console.log('[停止导航] 🚫 所有轮询任务已停止');
      
      // 🔧 关键修复：取消当前正在运行的轮询任务
      console.log('[停止导航] 🚫 第零步-扩展：取消当前轮询任务');
      if (this.currentCulturalTask) {
        try {
          this.currentCulturalTask.abort();
          console.log('[停止导航] ✅ 已取消文化导览轮询任务');
        } catch (e) {
          console.warn('[停止导航] ⚠️ 取消文化导览轮询任务失败:', e);
        }
        this.currentCulturalTask = null;
      }
      if (this.currentPoiTask) {
        try {
          this.currentPoiTask.abort();
          console.log('[停止导航] ✅ 已取消 POI 轮询任务');
        } catch (e) {
          console.warn('[停止导航] ⚠️ 取消 POI 轮询任务失败:', e);
        }
        this.currentPoiTask = null;
      }
      
      // 🔧 关键修复：立即关闭 pollTask 外部控制开关（互斥机制）
      console.log('[停止导航] 🚫 第一步：关闭 pollTask 外部控制开关');
      this.culturalPollControl.enabled = false;
      this.poiPollControl.enabled = false;
      console.log('[停止导航] 🚫 culturalPollControl.enabled:', this.culturalPollControl.enabled);
      console.log('[停止导航] 🚫 poiPollControl.enabled:', this.poiPollControl.enabled);
      
      // 🔧 关键修复：强制重置播放状态，确保立即清空队列
      this.isAudioPlaying = false;
      this.pendingClearQueue = false;
      this.isTTSConverting = false;
      this.isPoiRequesting = false;
      
      // 🔧 关键修复：立即设置 isNavigating = false，阻止新的请求发起
      this.isNavigating = false;
      console.log('[停止导航] ✅ 第一步：设置 isNavigating = false');
      
      // 🔧 关键修复：先停止所有定时器和请求
      console.log('[停止导航] 🛑 第三步：调用 stopAllRequests');
      this.stopAllRequests();
      
      // 🔧 关键修复：停止 POI 轮询
      console.log('[停止导航] 🛑 第四步：调用 stopPoiPolling');
      this.stopPoiPolling();
      
      // 先清空所有语音队列，防止停止后还有朗读
      console.log('[停止导航] 🗑 第五步：清空所有语音队列');
      this.clearAllSpeechContent();
      
      // 🔧 双队列架构：清空所有 TTS 相关队列
      console.log('[停止导航] 🗑 第五步-扩展：清空 TTS 双队列');
      this.speechTextQueue = [];           // 清空文本队列
      this.ttsConversionQueue = [];        // 清空TTS转换队列
      this.ttsAudioQueue = [];             // 清空音频播放队列
      console.log('[停止导航] ✅ 已清空 speechTextQueue, ttsConversionQueue, ttsAudioQueue');
      
      // 清空继续听的书籍 ID
      this.continueListenBookId = null;
      // 重置文化导览返回按钮相关状态
      this.showBackButton = false;
      this.savedCacheKey = null;
      this.currentBookId = null; // 🔧 清除当前书籍 ID
      this.cacheKey = null; // 🔧 清除缓存 key
      console.log('[停止导航] ✅ 第六步：清除所有状态变量');
      
      // #ifdef MP-WEIXIN
      // 微信小程序：停止持续定位更新
      console.log('[停止导航] 📍 第七步：停止持续定位');
      this.stopPositionUpdate();
      
      // 清理微信小程序音频上下文
      if (this.audioContext) {
        try {
          console.log('[停止导航] ⏹ 停止当前音频');
          this.audioContext.stop();
        } catch (e) {
          console.warn('[停止导航] 停止音频失败', e);
        }
        try {
          console.log('[停止导航] 🗑 销毁音频上下文');
          this.audioContext.destroy();
        } catch (e) {
          console.warn('[停止导航] 销毁音频上下文失败', e);
        }
        this.audioContext = null;
      }
      // #endif
      
      console.log('[停止导航] ✅ 第十步：所有请求和队列已清理完成');
      console.log('[停止导航] 🎉 停止导航流程完成');
      
      // 最后调用原有的 stopNavigation（如果需要）
      // stopNavigation(this, this.isCulturalMode);
    },
// ... existing code ...

    /**
     * 取消继续听
     */
    async cancelContinueListen() {
      // 🔧 关键修复：防止重复调用（请求独一性保证）
      if (this.isProcessing) {
        console.warn('[取消继续听] ⚠️ 正在处理中，忽略本次点击');
        uni.showToast({ title: '正在处理中，请稍候', icon: 'none', duration: 2000 });
        return;
      }
      
      if (!this.continueListenBookId) {
        console.warn('[取消继续听] ⚠️ 没有继续听的书籍 ID');
        return;
      }
      
      console.log('[取消继续听] 🚀 开始取消继续听');
      
      try {
        this.isProcessing = true;
        
        // 清空继续听的书籍 ID
        this.continueListenBookId = null;
        // 清除本地存储
        uni.removeStorageSync('continueListenBook');
        
        // 使用当前的导航参数重新调用 getUser, getArea, start-navigation, getChapter
        if (this.navigationParams) {
          const navigationData = this.navigationParams;
          
          // 1. 调用 getUser
          try {
            const userText = await getUserText(navigationData);
            let textToSpeak = userText;
            // 优先使用 data 字段的内容
            if (userText && typeof userText === 'object' && userText.data && typeof userText.data === 'string') {
              textToSpeak = userText.data;
              console.log('[导航] ✓ 使用 data 字段内容，长度:', textToSpeak.length);
            } else if (userText && typeof userText === 'string') {
              console.log('[导航] ✓ getUser 返回成功，长度:', userText.length);
            }
            if (textToSpeak && typeof textToSpeak === 'string' && this.isNavigating) {
              this.addTextToSpeechQueue(textToSpeak);
            }
          } catch (error) { }
          
          // 2. 调用 getArea
          try {
            const areaResponse = await pollTask(
              () => getAreaAsync({
                longitude: this.userLocation.lng,
                latitude: this.userLocation.lat,
                userId: 1
              }),
              undefined,
              2000
            );
            if (areaResponse && typeof areaResponse === 'object') {
              const keys = Object.keys(areaResponse);
              if (keys.length > 0) {
                this.cacheKey = keys[0];
                const content = areaResponse[this.cacheKey];
                if (content && typeof content === 'string') {
                  this.addTextToSpeechQueue(content);
                }
              }
            }
          } catch (error) { }
          
          // 3. 调用 startNavigationNoBook 获取轨迹 ID（整型）
          try {
            const trajectoryId = await startNavigationNoBook(navigationData);
            
            if (trajectoryId !== null && trajectoryId !== undefined && this.isNavigating) {
              
              // 4. 使用轨迹 ID 调用 getChapter
              const chapterText = await pollTask(
                () => getChapter({ bookId: trajectoryId, trajectoryId: trajectoryId, length: 3000 }),
                undefined,
                2000
              );
              
              if (chapterText && typeof chapterText === 'string' && this.isNavigating) {
                if (chapterText === '结束') {
                  uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
                } else {
                  this.addTextToSpeechQueue(chapterText);
                }
              }
              
              // 5. 设置定时器轮询 getChapter
              this.timer = setInterval(async () => {
                try {
                  if (this.isNavigating && !this.isPaused && this.mode === 0) {
                    const updatedChapterText = await pollTask(
                      () => getChapter({ bookId: trajectoryId, trajectoryId: trajectoryId, length: 3000 }),
                      undefined,
                      2000
                    );
                    if (updatedChapterText && typeof updatedChapterText === 'string' && this.isNavigating && !this.isPaused && this.mode === 0) {
                      if (updatedChapterText === '结束') {
                        clearInterval(this.timer);
                        this.timer = null;
                        uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
                      } else {
                        this.addTextToSpeechQueue(updatedChapterText);
                      }
                    }
                  }
                } catch (err) {
                  // getChapter 调用失败
                }
              }, 120000);
              
              // 保存当前的 bookId 和 trajectoryId
              this.currentBookId = { bookId: trajectoryId, trajectoryId: trajectoryId };
            }
          } catch (error) { }
        }
        
        uni.showToast({
          title: '已取消继续听，重新开始导航',
          icon: 'none'
        });
      } catch (error) {
        uni.showToast({ title: `取消失败：${error.message}`, icon: 'none' });
      } finally {
        this.isProcessing = false;
      }
    },


    togglePause() {
      // 🔧 关键修复：防止在导航中快速切换暂停状态（请求独一性保证）
      if (this.isProcessing) {
        console.warn('[暂停切换] ⚠️ 正在处理中，忽略本次点击');
        return;
      }
      
      if (!this.isNavigating) {
        console.warn('[暂停切换] ⚠️ 未在导航中，无法切换暂停状态');
        return;
      }
      
      this.isPaused = !this.isPaused;
      if (this.isPaused) {
        console.log('[TTS] ⏸ 用户点击暂停');
        uni.showToast({ title: '导航已暂停', icon: 'none' });
        
        // #ifdef H5
        if (window.speechSynthesis) window.speechSynthesis.cancel();
        // #endif
        
        // #ifdef MP-WEIXIN
        // 暂停微信小程序音频
        if (this.audioContext) {
          try {
            this.audioContext.pause();
            console.log('[TTS] ⏸ 音频已暂停');
          } catch (e) {
            console.error('[TTS] 暂停音频失败', e);
          }
        }
        // #endif
        
        if (this.speechTimer) { clearTimeout(this.speechTimer); this.speechTimer = null; }
        if (this.timer) { clearInterval(this.timer); this.timer = null; }
        // 如果是 POI 轮询状态，也要暂停
        if (this.isPoiRequesting) {
          this.stopPoiPolling();
        }
      } else {
        console.log('[TTS] ▶ 用户点击继续');
        uni.showToast({ title: '导航已继续', icon: 'none' });
        
        // 恢复轮询（包括正常导航和继续听模式）
        if (this.isNavigating && this.currentBookId && this.mode === 0) {
          console.log('[TTS] 🔄 恢复 getChapter 轮询定时器');
          this.timer = setInterval(async () => {
            try {
              console.log('[TTS] ⏰ 定时器触发，开始请求 getChapter');
              if (this.currentBookId && this.isNavigating && !this.isPaused && this.mode === 0) {
                const updatedChapterText = await pollTask(
                  () => getChapter({ bookId: this.currentBookId.bookId || this.currentBookId, trajectoryId: this.currentBookId.trajectoryId || 0, length: 3000 }),
                  undefined,
                  2000
                );
                console.log('[TTS] 📖 getChapter 返回:', updatedChapterText);
                if (updatedChapterText && typeof updatedChapterText === 'string' && this.isNavigating && !this.isPaused && this.mode === 0) {
                  if (updatedChapterText === '结束') {
                    console.log('[TTS] 🏁 小说已结束，清除定时器');
                    clearInterval(this.timer);
                    this.timer = null;
                    uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
                  } else {
                    console.log('[TTS] ✓ 添加章节内容到语音队列，长度:', updatedChapterText.length);
                    this.addTextToSpeechQueue(updatedChapterText);
                  }
                } else {
                  console.warn('[TTS] ⚠️ 返回内容无效或状态异常');
                }
              } else {
                console.log('[TTS] ⚠️ 条件不满足，跳过本次请求');
              }
            } catch (err) {
              console.error('[TTS] ❌ 定时器内 getChapter 调用失败:', err);
              console.error('[TTS] ❌ 错误详情:', err.message);
            }
          }, 120000);
          console.log('[TTS] ✅ 定时器已创建，timer ID:', this.timer);
        }
        
        // 如果是 POI 轮询状态，恢复轮询
        if (this.isPoiRequesting && this.selectedPointLocation) {
          this.startPoiPolling(this.selectedPointLocation.lng, this.selectedPointLocation.lat);
        }
        
        // 🔧 双队列架构：恢复播放
        // #ifdef MP-WEIXIN
        console.log('[TTS] 🔄 微信小程序环境，恢复播放');
        console.log('[TTS] 🔍 文本队列长度:', this.speechTextQueue.length);
        console.log('[TTS] 🔍 TTS转换队列长度:', this.ttsConversionQueue.length);
        console.log('[TTS] 🔍 音频播放队列长度:', this.ttsAudioQueue.length);
        console.log('[TTS] 🔍 isAudioPlaying:', this.isAudioPlaying);
        
        // 如果有正在播放的音频，恢复播放
        if (this.audioContext) {
          console.log('[TTS] ▶️ 恢复之前暂停的音频');
          try {
            if (!this.audioContext.src) {
              console.warn('[TTS] ⚠️ 音频已被销毁，重新从队列开始');
              this.isAudioPlaying = false;
              this.processAudioPlaybackQueue();
            } else {
              this.audioContext.play();
              console.log('[TTS] ✅ 音频已恢复播放');
            }
          } catch (e) {
            console.error('[TTS] ❌ 恢复音频失败', e);
            this.isAudioPlaying = false;
            this.processAudioPlaybackQueue();
          }
        } else {
          // 如果没有正在播放的音频，从音频队列重新开始
          console.log('[TTS] 🔄 没有暂停的音频，从音频队列重新开始');
          this.isAudioPlaying = false;
          this.processAudioPlaybackQueue();
        }
        // #endif
        
        // #ifndef MP-WEIXIN
        // H5 环境：恢复 Web Speech API
        if (this.currentSpeakingText) {
          const remainingText = this.currentSpeakingText.substring(this.currentSpeechIndex);
          if (remainingText && remainingText.length > 0) {
            this.speakText(remainingText);
          } else {
            this.currentSpeakingText = null;
            this.processSpeechQueue();
          }
        } else {
          this.processSpeechQueue();
        }
        // #endif
      }
    },

    selectNovelType(type) { this.selectedNovelType = type; },
    selectNovelLength(length) { this.selectedNovelLength = length; },
    selectByWay(way) { this.selectedByWay = way; },
    selectSpeedLimit(limit) {
      this.selectedSpeedLimit = limit;
      switch (limit) {
        case 'low': this.speedLimitThreshold = 5; break;
        case 'medium': this.speedLimitThreshold = 15; break;
        case 'high': this.speedLimitThreshold = 30; break;
        default: this.speedLimitThreshold = 0;
      }
    },
    selectCulturalMode(mode) {
      if (this.isNavigating) {
        uni.showToast({ title: '导航中无法切换模式', icon: 'none', duration: 2000 });
        return;
      }
      this.isCulturalMode = mode; 
      this.mode = mode ? 1 : 0; 
    },
    toggleCulturalMode() {
      if (this.isNavigating) {
        uni.showToast({ title: '导航中无法切换模式', icon: 'none', duration: 2000 });
        return;
      }
      this.isCulturalMode = !this.isCulturalMode; 
      this.mode = this.isCulturalMode ? 1 : 0; 
    },
    toggleCulturalSettings() { this.showCulturalSettings = !this.showCulturalSettings; },
    toggleNovelSettings() { this.showNovelSettings = !this.showNovelSettings; },

    toggleSelectPointMode() {
      // 🔧 关键修复：防止在导航中切换选点模式（请求独一性保证）
      if (this.isProcessing) {
        console.warn('[选点模式] ⚠️ 正在处理中，忽略本次点击');
        uni.showToast({ title: '正在处理中，请稍候', icon: 'none', duration: 2000 });
        return;
      }
      
      this.isSelectingPoint = !this.isSelectingPoint;
      if (this.isSelectingPoint) {
        uni.showToast({ title: '请点击地图选择中心点', icon: 'none', duration: 3000 });
        // 清除之前的中心点和 cacheKey
        this.selectedPointLocation = null;
        this.cacheKey = null;
        this.showBackButton = false;
        this.savedCacheKey = null;
      } else {
        uni.showToast({ title: '已取消选点', icon: 'none' });
      }
    },

    startCulturalNavigation() { this.fetchCulturalInfo(); },

    async fetchCulturalInfo() {
      // 🔧 关键修复：在每次调用前检查状态，防止竞态条件
      if (!this.isNavigating || this.isPaused || this.mode !== 1) {
        console.log('[文化导览] ⚠️ 状态不满足，中止 fetchCulturalInfo');
        return;
      }
      
      // 如果正在使用 getPoi 轮询，就不要再调用 getArea 了
      if (this.isPoiRequesting && this.selectedPointLocation) {
        console.log('[文化导览] ⚠️ POI 轮询进行中，跳过 getArea');
        return;
      }
      
      if (!this.selectedPointLocation && !this.userLocation) {
        uni.showToast({ title: '请先获取当前位置或选择中心点', icon: 'none', duration: 2000 });
        return;
      }
      
      try {
        let location = this.selectedPointLocation || this.userLocation;
        // 文化导览模式下，使用 pollTask 进行轮询请求
        const params = {
          longitude: location.lng,
          latitude: location.lat,
          userId: 1
        };
        // 只有当 appliedContent 有值时才传递 content 参数
        console.log('[文化导览] 🔍 检查 appliedContent:', this.appliedContent);
        console.log('[文化导览] 🔍 appliedContent 类型:', typeof this.appliedContent);
        if (this.appliedContent) {
          params.content = this.appliedContent;
          console.log('[文化导览] ✅ 已添加 content 参数:', this.appliedContent);
        } else {
          console.log('[文化导览] ⚠️ appliedContent 为空，不传递 content 参数');
        }
        
        console.log('[文化导览] 📤 最终请求参数:', params);
        
        // 🔧 关键修复：使用 pollTask 进行一次完整的轮询（内部会自动轮询直到完成）
        // pollTask 会：1. 调用 getAreaAsync 获取 taskId  2. 自动轮询 getTaskResult 直到完成
        const areaResponse = await pollTask(
          () => getAreaAsync(params),
          getTaskResult,
          2000,
          () => !this.isNavigating || this.isPaused || this.mode !== 1 || this.isPoiRequesting
        );
        
        // 🔧 关键修复：再次检查状态，确保仍在导航中
        if (!this.isNavigating || this.isPaused || this.mode !== 1) {
          console.log('[文化导览] ⚠️ 请求返回后状态已变化，丢弃结果');
          return;
        }
        
        // pollingManager 已经处理了 Map 提取，直接检查是否为字符串
        if (areaResponse && typeof areaResponse === 'string') {
          console.log('[文化导览] ✓ 获取到导游词，长度:', areaResponse.length);
          this.addTextToSpeechQueue(areaResponse);
        } else if (areaResponse && typeof areaResponse === 'object') {
          // 优先使用 data 字段的内容
          if (areaResponse.data && typeof areaResponse.data === 'string') {
            console.log('[文化导览] ✓ 使用 data 字段内容，长度:', areaResponse.data.length);
            this.addTextToSpeechQueue(areaResponse.data);
          } else {
            // 兼容旧逻辑：如果没有 data 字段，尝试提取
            const keys = Object.keys(areaResponse).filter(key => key !== '@type');
            if (keys.length > 0) {
              this.cacheKey = keys[0];
              const content = areaResponse[this.cacheKey];
              if (content && typeof content === 'string') {
                console.log('[文化导览] ✓ 从 Map 中提取内容');
                this.addTextToSpeechQueue(content);
              }
            }
          }
        } else {
          console.warn('[文化导览] ⚠️ 返回内容为空或类型未知:', typeof areaResponse);
        }
        
        // 🔧 关键修复：pollTask 已经完成一次完整的轮询，如果需要持续获取，再次调用
        // 这里使用 setTimeout 延迟后再次调用 fetchCulturalInfo，形成循环
        if (this.isNavigating && !this.isPaused && this.mode === 1 && !this.isPoiRequesting) {
          setTimeout(() => {
            // 🔧 关键修复：延迟后再次检查状态
            if (this.isNavigating && !this.isPaused && this.mode === 1 && !this.isPoiRequesting) {
              this.fetchCulturalInfo();
            }
          }, 3000);
        }
      } catch (error) {
        console.error('getArea 请求失败:', error);
        // 出错时不立即重试，等待一段时间后再试
        // 🔧 关键修复：如果进入了 POI 模式，则停止重试
        if (this.isNavigating && !this.isPaused && this.mode === 1 && !this.isPoiRequesting) {
          setTimeout(() => { 
            // 🔧 关键修复：延迟后再次检查状态
            if (this.isNavigating && !this.isPaused && this.mode === 1 && !this.isPoiRequesting) {
              this.fetchCulturalInfo(); 
            }
          }, 5000); // 延长重试间隔到 5 秒
        }
      }
    },

    zoomIn() { 
      // #ifdef H5
      if (this.map) { 
        const currentZoom = this.map.getZoom(); 
        if (currentZoom < 18) this.map.setZoom(currentZoom + 1); 
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      if (this.mapScale < 18) {
        this.mapScale += 1;
      }
      // #endif
    },
    
    zoomOut() { 
      // #ifdef H5
      if (this.map) { 
        const currentZoom = this.map.getZoom(); 
        if (currentZoom > 3) this.map.setZoom(currentZoom - 1); 
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      if (this.mapScale > 3) {
        this.mapScale -= 1;
      }
      // #endif
    },

    updateUserMarker(location) {
      // #ifdef H5
      if (!this.map || !this.AMap || !location) return;

      const h5Lng = location.lng;
      const h5Lat = location.lat;

      if (this.userMarker) {
        this.userMarker.setPosition([h5Lng, h5Lat]);
      } else {
        this.userMarker = new this.AMap.Marker({
          position: [h5Lng, h5Lat],
          title: '当前位置',
          icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
          animation: 'AMAP_ANIMATION_DROP'
        });
        this.map.add(this.userMarker);
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      if (!location) return;
      
      const wxLng = location.lng;
      const wxLat = location.lat;
      
      // 更新用户位置标记（id 必须是数字）
      const userMarkerIndex = this.mapMarkers.findIndex(m => m.id === 1);
      const userMarker = {
        id: 1,
        latitude: wxLat,
        longitude: wxLng,
        iconPath: '/static/images/location-marker.png',
        width: 30,
        height: 30,
        callout: {
          content: '当前位置',
          color: '#FFFFFF',
          fontSize: 12,
          borderRadius: 4,
          bgColor: '#1677ff',
          padding: 5,
          display: 'ALWAYS'
        }
      };
      
      if (userMarkerIndex >= 0) {
        this.$set(this.mapMarkers, userMarkerIndex, userMarker);
      } else {
        this.mapMarkers.push(userMarker);
      }
      // #endif
    },

    tryAMapLocation() {
      return new Promise((resolve, reject) => {
        if (!this.AMap || !this.map) {
          reject(new Error('地图未初始化'));
          return;
        }

        this.AMap.plugin('AMap.Geolocation', () => {
          const geolocation = new this.AMap.Geolocation({
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0,
            convert: true
          });

          geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete') {
              const { lng, lat } = result.position;
              const location = { lng, lat };
              this.userLocation = location;
              this.map.setCenter([lng, lat]);
              this.updateUserMarker(location);
              uni.showToast({ title: '高德地图定位成功', icon: 'success', duration: 1500 });
              resolve(location);
            } else {
              reject(new Error(`高德地图定位失败: ${result}`));
            }
          });
        });
      });
    },

    startSpeedDetection() {
      startSpeedDetection(this, this.speedLimitThreshold);
    },

    drawNavigationPath(origin, destination) {
      // #ifdef H5
      if (!this.AMap || !this.map) return;
      try {
        const [destLng, destLat] = destination.location.split(',').map(Number);
        if (this.driving) this.driving.clear();
        this.AMap.plugin('AMap.Driving', () => {
          try {
            this.driving = new this.AMap.Driving({
              map: this.map,
              panel: false,
              hideMarkers: false,
              polylineOptions: { strokeColor: '#2563eb', strokeWeight: 6, strokeOpacity: 0.8 },
              markerOptions: {
                origin: { icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png', title: '起点' },
                destination: { icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png', title: '终点' }
              }
            });
            this.driving.search(
              new this.AMap.LngLat(origin.lng, origin.lat),
              new this.AMap.LngLat(destLng, destLat),
              function (status, result) {
                if (status === 'complete') {
                  if (result.routes && result.routes.length > 0) {
                    const path = result.routes[0].path;
                    if (path && path.length > 0) this.map.setFitView();
                  }
                } else {
                  let errorMsg = '路径规划失败';
                  if (result === 'USERKEY_PLAT_NOMATCH') errorMsg = 'API Key平台不匹配';
                  else if (result === 'INVALID_USER_KEY') errorMsg = 'API Key无效';
                  else if (result === 'OVER_QUERY_LIMIT') errorMsg = 'API调用次数超限';
                  uni.showToast({ title: errorMsg, icon: 'none' });
                }
              }.bind(this)
            );
          } catch (error) { uni.showToast({ title: '绘制路径失败', icon: 'none' }); }
        });
      } catch (error) { uni.showToast({ title: '绘制路径失败', icon: 'none' }); }
      // #endif
      
      // #ifdef MP-WEIXIN
      // 微信小程序中调用封装好的路径规划 API
      try {
        const [destLng, destLat] = destination.location.split(',').map(Number);
        
        console.log('[微信小程序路径规划] 开始规划路径');
        console.log('[微信小程序路径规划] 起点:', origin);
        console.log('[微信小程序路径规划] 终点:', { lng: destLng, lat: destLat });
        
        // 调用封装好的 API 函数
        getDrivingRoute(
          { longitude: origin.lng, latitude: origin.lat },
          { longitude: destLng, latitude: destLat }
        )
        .then((routeData) => {
          console.log('[微信小程序路径规划] API 响应成功:', routeData);
          
          if (routeData.route && routeData.route.paths && routeData.route.paths.length > 0) {
            const path = routeData.route.paths[0];
            const steps = path.steps;
            
            // 构建路径点数组
            const points = [];
            
            // 添加起点
            points.push({ longitude: origin.lng, latitude: origin.lat });
            
            // 解析每一步的路径
            steps.forEach(step => {
              if (step.polyline) {
                const coords = step.polyline.split(';');
                coords.forEach(coord => {
                  const [lng, lat] = coord.split(',').map(Number);
                  if (!isNaN(lng) && !isNaN(lat)) {
                    points.push({ longitude: lng, latitude: lat });
                  }
                });
              }
            });
            
            // 确保终点在路径中
            points.push({ longitude: destLng, latitude: destLat });
            
            console.log('[微信小程序路径规划] 路径点数:', points.length);
            
            // 清除旧的路径
            this.mapPolyline = [];
            
            // 添加新的路径（真实路径）
            this.mapPolyline.push({
              points: points,
              color: '#2563eb',
              width: 6,
              dottedLine: false,
              arrowLine: true,
              borderColor: '#ffffff',
              borderWidth: 2
            });
            
            // 添加起点和终点标记（id 必须是数字）
            this.mapMarkers = this.mapMarkers.filter(m => m.id !== 2 && m.id !== 3);
            
            this.mapMarkers.push({
              id: 2,
              latitude: origin.lat,
              longitude: origin.lng,
              iconPath: '/static/images/start-marker.png',
              width: 30,
              height: 30,
              callout: {
                content: '起点',
                color: '#FFFFFF',
                fontSize: 12,
                borderRadius: 4,
                bgColor: '#52c41a',
                padding: 5,
                display: 'ALWAYS'
              }
            });
            
            this.mapMarkers.push({
              id: 3,
              latitude: destLat,
              longitude: destLng,
              iconPath: '/static/images/end-marker.png',
              width: 30,
              height: 30,
              callout: {
                content: '终点',
                color: '#FFFFFF',
                fontSize: 12,
                borderRadius: 4,
                bgColor: '#ff4d4f',
                padding: 5,
                display: 'ALWAYS'
              }
            });
            
            // 调整地图视野以包含所有点
            this.adjustMapViewport();
            
            console.log('[微信小程序路径规划] ✓ 路径规划成功');
            uni.showToast({ title: '路径规划成功', icon: 'success', duration: 1500 });
          } else {
            console.error('[微信小程序路径规划] ✗ 路径数据异常');
            // 降级方案：显示直线
            this.drawStraightLine(origin, destLng, destLat);
            uni.showToast({ title: '路径数据异常，显示直线', icon: 'none', duration: 3000 });
          }
        })
        .catch((error) => {
          console.error('[微信小程序路径规划] ✗ 请求失败:', error);
          // 降级方案：显示直线
          this.drawStraightLine(origin, destLng, destLat);
          uni.showToast({ title: `路径规划失败: ${error.message}`, icon: 'none', duration: 3000 });
        });
      } catch (error) {
        console.error('绘制路径失败:', error);
        // 降级方案：显示直线
        const [destLng, destLat] = destination.location.split(',').map(Number);
        this.drawStraightLine(origin, destLng, destLat);
        uni.showToast({ title: '绘制路径异常', icon: 'none' });
      }
      // #endif
    },
    
    /**
     * 绘制直线（降级方案）
     */
    drawStraightLine(origin, destLng, destLat) {
      // 清除旧的路径
      this.mapPolyline = [];
      
      // 添加直线路径
      this.mapPolyline.push({
        points: [
          { longitude: origin.lng, latitude: origin.lat },
          { longitude: destLng, latitude: destLat }
        ],
        color: '#2563eb',
        width: 6,
        dottedLine: false,
        arrowLine: true,
        borderColor: '#ffffff',
        borderWidth: 2
      });
      
      // 添加起点和终点标记（id 必须是数字）
      this.mapMarkers = this.mapMarkers.filter(m => m.id !== 2 && m.id !== 3);
      
      this.mapMarkers.push({
        id: 2,
        latitude: origin.lat,
        longitude: origin.lng,
        iconPath: '/static/images/start-marker.png',
        width: 30,
        height: 30,
        callout: {
          content: '起点',
          color: '#FFFFFF',
          fontSize: 12,
          borderRadius: 4,
          bgColor: '#52c41a',
          padding: 5,
          display: 'ALWAYS'
        }
      });
      
      this.mapMarkers.push({
        id: 3,
        latitude: destLat,
        longitude: destLng,
        iconPath: '/static/images/end-marker.png',
        width: 30,
        height: 30,
        callout: {
          content: '终点',
          color: '#FFFFFF',
          fontSize: 12,
          borderRadius: 4,
          bgColor: '#ff4d4f',
          padding: 5,
          display: 'ALWAYS'
        }
      });
      
      // 调整地图视野
      this.adjustMapViewport();
    },

    clearAllSpeechContent() {
      // #ifdef H5
      if (window.speechSynthesis) window.speechSynthesis.cancel();
      // #endif
      
      // #ifdef MP-WEIXIN
      // 🔧 关键修复：如果正在播放音频，不要立即停止，让它自然播放完成
      if (this.isAudioPlaying) {
        console.log('[TTS] ⚠️ 正在播放中，延迟清空队列');
        // 标记需要清空，但不立即执行
        this.pendingClearQueue = true;
        return;
      }
      
      // 清理微信小程序音频上下文
      if (this.audioContext) {
        try {
          this.audioContext.stop();
          console.log('[TTS] ⏹ 音频已停止');
        } catch (e) {
          console.warn('[TTS] 停止音频失败', e);
        }
        try {
          this.audioContext.destroy();
          console.log('[TTS] 🗑 音频上下文已销毁');
        } catch (e) {
          console.warn('[TTS] 销毁音频上下文失败', e);
        }
        this.audioContext = null;
      }
      // #endif
      
      if (this.speechTimer) { clearTimeout(this.speechTimer); this.speechTimer = null; }
      
      // 🔧 双队列架构：清空所有 TTS 相关队列
      console.log('[TTS] 🗑 清空所有 TTS 队列');
      this.speechTextQueue = [];           // 清空文本队列
      this.ttsConversionQueue = [];        // 清空TTS转换队列
      this.ttsAudioQueue = [];             // 清空音频播放队列
      
      this.currentSpeakingText = null;
      this.currentSpeechIndex = 0;
      this.isTTSPlaying = false; // 🔓 重置播放锁
      
      // 清空长文本分段相关变量
      this.currentLongTextSegments = [];
      this.currentSegmentIndex = 0;
      this.longTextCallback = null;
      
      // 重置 TTS 状态标志
      this.isTTSConverting = false;
      this.isAudioPlaying = false;
      this.pendingClearQueue = false; // 清除待清空标记
      
      console.log('[TTS] ✅ 清空完成');
      console.log('[TTS] 📊 speechTextQueue:', this.speechTextQueue.length);
      console.log('[TTS] 📊 ttsConversionQueue:', this.ttsConversionQueue.length);
      console.log('[TTS] 📊 ttsAudioQueue:', this.ttsAudioQueue.length);
    },

    /**
     * 🔧 TTS 预加载：将文本加入转换队列（仅微信小程序）
     * @param {string} text - 要转换的文本
     */
    /**
     * 转换文本并加入播放队列（简化版）
     * @param {string} text - 要转换的文本
     */
    convertAndEnqueue(text) {
      // #ifdef MP-WEIXIN
      console.log('[TTS] 🔄 开始转换文本');
      
      const plugin = requirePlugin("WechatSI");
      if (!plugin || !plugin.textToSpeech) {
        console.error('[TTS] ❌ 微信同声传译插件不可用');
        return;
      }
      
      // 清理文本
      let cleanedText = text
        .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')
        .replace(/["""]/g, '"')
        .replace(/['''']/g, "'")
        .replace(/：/g, ':')
        .replace(/——/g, '-')
        .replace(/……/g, '...')
        .trim();
      
      // 调用 TTS 转换
      plugin.textToSpeech({
        lang: "zh_CN",
        content: cleanedText,
        success: (res) => {
          console.log('[TTS] ✅ 转换成功');
          
          const audioUrl = res.voice_file || res.filename;
          if (!audioUrl) {
            console.error('[TTS] ❌ 未找到音频 URL');
            return;
          }
          
          // 再次检查导航状态
          if (!this.isNavigating || this.isPaused) {
            console.log('[TTS] ⚠️ 导航已停止或暂停，丢弃音频');
            return;
          }
          
          // 加入播放队列
          this.ttsAudioQueue.push({
            text: text,
            audioUrl: audioUrl
          });
          
          console.log('[TTS] 🎵 音频加入队列，当前长度:', this.ttsAudioQueue.length);
          
          // 立即触发播放
          this.processAudioPlaybackQueue();
        },
        fail: (err) => {
          console.error('[TTS] ❌ 转换失败', err);
        }
      });
      // #endif
    },

    /**
     * 🔧 处理 TTS 转换队列（仅微信小程序）
     */
    async processTTSConversionQueue() {
      // #ifdef MP-WEIXIN
      console.log('[TTS 转换队列] 📊 当前状态:');
      console.log('[TTS 转换队列] 🔍 isTTSConverting:', this.isTTSConverting);
      console.log('[TTS 转换队列] 🔍 ttsConversionQueue 长度:', this.ttsConversionQueue.length);
      
      // 如果正在转换，等待
      if (this.isTTSConverting) {
        console.log('[TTS 预加载] ℹ️ 正在转换中，等待完成');
        return;
      }
      
      // 找到第一个 pending 状态的任务
      const pendingTaskIndex = this.ttsConversionQueue.findIndex(item => item.status === 'pending');
      if (pendingTaskIndex === -1) {
        console.log('[TTS 预加载] ✅ 转换队列为空');
        return;
      }
      
      const pendingTask = this.ttsConversionQueue[pendingTaskIndex];
      
      // 标记为转换中
      this.isTTSConverting = true;
      pendingTask.status = 'converting';
      
      console.log('[TTS 转换队列] 🔄 开始转换第', pendingTaskIndex + 1, '个任务');
      console.log('[TTS 转换队列] 📝 文本预览:', pendingTask.text.substring(0, 50) + '...');
      console.log('[TTS 转换队列] 📏 文本长度:', pendingTask.text.length);
      
      try {
        // 调用微信 TTS 插件进行转换
        const plugin = requirePlugin("WechatSI");
        if (!plugin || !plugin.textToSpeech) {
          throw new Error('微信同声传译插件不可用');
        }
        
        // 清理文本
        let cleanedText = pendingTask.text
          .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')
          .replace(/["""]/g, '"')
          .replace(/['''']/g, "'")
          .replace(/：/g, ':')
          .replace(/——/g, '-')
          .replace(/……/g, '...')
          .trim();
        
        // 调用 TTS 转换
        await new Promise((resolve, reject) => {
          plugin.textToSpeech({
            lang: "zh_CN",
            content: cleanedText,
            success: (res) => {
              console.log('[TTS 预加载] ✓ 转换成功');
              resolve(res);
            },
            fail: (err) => {
              console.error('[TTS 预加载] ✗ 转换失败', err);
              reject(err);
            }
          });
        }).then((res) => {
          // 🔧 关键修复：检查任务是否已被取消或导航已停止
          if (pendingTask.status === 'cancelled' || !this.isNavigating) {
            console.log('[TTS 预加载] 🚫 任务已取消或导航已停止，丢弃结果');
            return;
          }
          
          // 获取音频 URL
          const audioUrl = res.voice_file || res.filename;
          if (!audioUrl) {
            throw new Error('未找到音频 URL');
          }
          
          // 🔧 关键修复：再次检查状态，确保仍在导航中
          if (!this.isNavigating) {
            console.log('[TTS 预加载] 🚫 导航已停止，丢弃音频结果');
            return;
          }
          
          // 更新任务状态
          pendingTask.status = 'completed';
          pendingTask.audioUrl = audioUrl;
          
          // 加入音频播放队列
          this.ttsAudioQueue.push({
            text: pendingTask.text,
            audioUrl: audioUrl,
            timestamp: Date.now(),
            cancelled: false // 默认未取消
          });
          
          console.log('[TTS 转换队列] ✅ 第', pendingTaskIndex + 1, '个任务转换完成');
          console.log('[TTS 预加载] 🎵 音频加入播放队列，当前队列长度:', this.ttsAudioQueue.length);
          
          // 🔧 关键修复：只有在导航进行中才触发音频播放
          if (this.isNavigating && !this.isPaused) {
            console.log('[TTS 预加载] 🚀 立即触发音频播放队列处理');
            console.log('[TTS 预加载] 🔍 isAudioPlaying:', this.isAudioPlaying);
            console.log('[TTS 预加载] 🔍 ttsAudioQueue 长度:', this.ttsAudioQueue.length);
            this.processAudioPlaybackQueue();
          } else {
            console.log('[TTS 预加载] ⚠️ 导航已停止或已暂停，不触发播放');
            console.log('[TTS 预加载] 🔍 isNavigating:', this.isNavigating, ', isPaused:', this.isPaused);
          }
        });
        
      } catch (error) {
        console.error('[TTS 预加载] ❌ 转换异常', error);
        pendingTask.status = 'failed';
      } finally {
        this.isTTSConverting = false;
        
        // 🔧 关键修复：检查是否已被停止，如果是则不再继续处理
        if (this.ttsConversionQueue.length === 0) {
          console.log('[TTS 预加载] ✅ 队列为空，停止转换');
          return;
        }
        
        // 🔧 关键优化：使用微小的延迟（10ms）避免微信插件速率限制，同时保持快速响应
        setTimeout(() => {
          console.log('[TTS 预加载] 🚀 处理下一个转换任务');
          this.processTTSConversionQueue();
        }, 10);
      }
      // #endif
      
      // #ifndef MP-WEIXIN
      // H5 环境不使用预加载队列，直接使用 Web Speech API
      console.log('[TTS 预加载] ⚠️ H5 环境不支持预加载队列');
      // #endif
    },

    /**
     * 🔧 处理音频播放队列(仅微信小程序) - 简化版:有就播放
     */
    processAudioPlaybackQueue() {
      // #ifdef MP-WEIXIN
      console.log('[TTS 播放队列] 📊 检查播放队列');
      console.log('[TTS 播放队列] 🔍 isAudioPlaying:', this.isAudioPlaying);
      console.log('[TTS 播放队列] 🔍 isPaused:', this.isPaused);
      console.log('[TTS 播放队列] 🔍 ttsAudioQueue 长度:', this.ttsAudioQueue.length);
          
      // 已暂停,不播放
      if (this.isPaused) {
        console.log('[TTS 播放队列] ⏸ 已暂停,停止播放');
        return;
      }
          
      // 队列为空
      if (this.ttsAudioQueue.length === 0) {
        console.log('[TTS 播放队列] ✅ 播放队列为空');
        return;
      }
          
      // 如果正在播放,等待当前播放完成(这是正常的队列行为)
      if (this.isAudioPlaying) {
        console.log('[TTS 播放队列] ℹ️ 正在播放中,新音频已在队列中等待,播放完成后会自动继续');
        return;
      }
      
      // 取出第一个音频
      const audioItem = this.ttsAudioQueue.shift();
      
      console.log('[TTS 播放队列] 📤 从队列取出音频');
      console.log('[TTS 播放队列] 📊 取出后队列剩余长度:', this.ttsAudioQueue.length);
      
      // 🔧 关键修复：检查导航状态，如果已停止则清空队列并返回
      if (!this.isNavigating) {
        console.log('[TTS 播放队列] 🚫 导航已停止，丢弃所有待播放音频');
        // 清空剩余队列
        this.ttsAudioQueue = [];
        return;
      }
      
      // 检查音频是否已被取消
      if (audioItem && audioItem.cancelled) {
        console.log('[TTS 播放队列] 🚫 音频已取消，跳过');
        console.log('[TTS 播放队列] 📝 被取消的文本预览:', audioItem.text.substring(0, 50) + '...');
        // 继续下一个
        this.processAudioPlaybackQueue();
        return;
      }
      
      if (!audioItem || !audioItem.audioUrl) {
        console.warn('[TTS 播放队列] ⚠️ 音频项无效，跳过');
        // 继续下一个
        this.processAudioPlaybackQueue();
        return;
      }
      
      console.log('[TTS 播放队列] 🎵 准备播放音频');
      console.log('[TTS 播放队列] 📝 文本预览:', audioItem.text.substring(0, 80) + '...');
      console.log('[TTS 播放队列] 📏 文本长度:', audioItem.text.length);
      console.log('[TTS 播放队列] 🔗 音频URL:', audioItem.audioUrl);
      
      // 播放音频
      this.playAudioItem(audioItem, () => {
        console.log('[TTS 播放队列] ✅ 音频播放完成回调');
        
        // 删除已播放的音频文件
        this.deleteAudioFile(audioItem.audioUrl);
        
        // 继续播放下一个
        console.log('[TTS 播放队列] 🔄 继续播放下一个音频');
        this.processAudioPlaybackQueue();
      });
      // #endif
      
      // #ifndef MP-WEIXIN
      // H5 环境不使用预加载队列
      console.log('[TTS 播放队列] ⚠️ H5 环境不支持音频播放队列');
      // #endif
    },

    /**
     * 🔧 删除已播放的音频文件（仅微信小程序）
     * @param {string} audioUrl - 音频文件路径
     */
    deleteAudioFile(audioUrl) {
      // #ifdef MP-WEIXIN
      if (!audioUrl) {
        return;
      }
      
      // 🔧 关键修复：微信TTS插件返回的是网络URL，不是本地文件，无法删除
      // 只能清理引用，让浏览器/小程序自动回收内存
      if (audioUrl.startsWith('http://') || audioUrl.startsWith('https://')) {
        console.log('[TTS 清理] ℹ️ 远程音频URL，无需删除:', audioUrl.substring(0, 50) + '...');
        return;
      }
      
      // 只有本地文件才尝试删除
      try {
        const fs = uni.getFileSystemManager();
        // 检查文件是否存在
        fs.access({
          path: audioUrl,
          success: () => {
            console.log('[TTS 清理] 🗑 发现本地音频文件，准备删除:', audioUrl);
            // 删除文件
            fs.unlink({
              filePath: audioUrl,
              success: () => {
                console.log('[TTS 清理] ✅ 本地音频文件已删除:', audioUrl);
              },
              fail: (err) => {
                console.warn('[TTS 清理] ⚠️ 删除本地音频文件失败:', err);
              }
            });
          },
          fail: () => {
            console.log('[TTS 清理] ℹ️ 本地音频文件不存在或已被删除:', audioUrl);
          }
        });
      } catch (e) {
        console.warn('[TTS 清理] ⚠️ 删除音频文件异常:', e);
      }
      // #endif
    },

    /**
     * 🔧 播放单个音频项（仅微信小程序）
     * @param {object} audioItem - 音频项 {text, audioUrl}
     * @param {function} onComplete - 播放完成回调
     */
    playAudioItem(audioItem, onComplete) {
      // #ifdef MP-WEIXIN
      console.log('[TTS 播放] 🎬 开始创建音频上下文');
      console.log('[TTS 播放] 📝 文本长度:', audioItem.text.length);
      console.log('[TTS 播放] 🔗 音频URL:', audioItem.audioUrl);
      
      this.isAudioPlaying = true;
      this.currentSpeakingText = audioItem.text;
      
      // 🔧 关键修复：先停止并销毁旧的音频上下文，再创建新的
      if (this.audioContext) {
        try {
          console.log('[TTS 播放] ⚠️ 发现旧音频上下文，准备销毁');
          this.audioContext.stop();
        } catch (e) {
          console.warn('[TTS 播放] 停止旧音频失败', e);
        }
        try {
          console.log('[TTS 播放] 🗑 销毁旧音频上下文');
          this.audioContext.destroy();
        } catch (e) {
          console.warn('[TTS 播放] 销毁旧音频失败', e);
        }
        this.audioContext = null;
      }
      
      const audioContext = uni.createInnerAudioContext();
      this.audioContext = audioContext;
      
      console.log('[TTS 播放] ✅ 新音频上下文已创建');
      
      // 🔧 关键设置：确保音频能正常播放
      audioContext.autoplay = false;  // 不自动播放，手动控制
      audioContext.obeyMuteSwitch = false;  // 忽略系统静音开关
      audioContext.volume = 1.0;  // 最大音量（0.0-1.0）
      
      console.log('[TTS 播放] 🔊 音量设置为:', audioContext.volume);
      console.log('[TTS 播放] 🔇 忽略静音开关:', audioContext.obeyMuteSwitch);
      
      // 设置音频源
      audioContext.src = audioItem.audioUrl;
      console.log('[TTS 播放] 🎵 音频源已设置');
      console.log('[TTS 播放] 🔍 当前 audioContext.src:', audioContext.src);
      
      // 超时保护已移除
      let playbackTimeout = null;
      
      const clearPlaybackTimeout = () => {
        if (playbackTimeout) {
          clearTimeout(playbackTimeout);
          playbackTimeout = null;
        }
      };
      
      // 监听事件
      let hasPlayed = false; // 🔧 防止重复播放
      let playStartTime = null; // 🔧 记录 play() 调用时间
      
      // 播放启动超时检测已移除
      let playStartTimeout = null;
      const clearPlayStartTimeout = () => {
        if (playStartTimeout) {
          clearTimeout(playStartTimeout);
          playStartTimeout = null;
        }
      };
      
      audioContext.onCanplay(() => {
        console.log('[TTS 播放] ✅ 音频已加载，可以播放');
        console.log('[TTS 播放] 🔍 此时 audioContext.src:', audioContext.src);
        
        // 🔧 关键修复：在 onCanplay 中立即播放，不等待 setTimeout
        if (!hasPlayed) {
          hasPlayed = true;
          playStartTime = Date.now();
          console.log('[TTS 播放] ▶ onCanplay 触发，立即调用 play()');
          try {
            audioContext.play();
            console.log('[TTS 播放] ▶ play() 已成功调用');
          } catch (e) {
            console.error('[TTS 播放] ❌ play() 调用失败:', e);
            clearPlayStartTimeout();
          }
        }
      });
      
      audioContext.onPlay(() => {
        console.log('[TTS 播放] ▶️ 音频开始播放');
        console.log('[TTS 播放] 📝 正在播放的文本:', audioItem.text.substring(0, 80) + '...');
        console.log('[TTS 播放] 🔍 此时 isAudioPlaying:', this.isAudioPlaying);
        
        // 🔧 关键新增：清除播放启动超时检测
        clearPlayStartTimeout();
        if (playStartTime) {
          const delay = Date.now() - playStartTime;
          console.log('[TTS 播放] ⏱️ 从调用 play() 到 onPlay 触发耗时:', delay, 'ms');
        }
      });
      
      audioContext.onEnded(() => {
        console.log('[TTS 播放] ✅ 播放完成');
        console.log('[TTS 播放] 📝 已播放完成的文本:', audioItem.text.substring(0, 80) + '...');
        console.log('[TTS 播放] 🔍 此时 isAudioPlaying:', this.isAudioPlaying);
        console.log('[TTS 播放] 🔍 此时 ttsAudioQueue 长度:', this.ttsAudioQueue.length);
        clearPlaybackTimeout(); // 清除超时定时器
        clearPlayStartTimeout(); // 🔧 清除播放启动超时检测
              
        // 🔧 关键修复:先清空引用,再销毁,避免重复销毁
        this.audioContext = null;
        this.isAudioPlaying = false;
        this.currentSpeakingText = null;
              
        try {
          audioContext.destroy();
          console.log('[TTS 预加载] 🗑 音频上下文已销毁');
        } catch (e) {
          console.warn('[TTS 预加载] 销毁音频上下文失败', e);
        }
              
        // 🔧 关键修复:检查是否有待清空的队列
        if (this.pendingClearQueue) {
          console.log('[TTS] 🗑 执行延迟清空队列');
          this.clearAllSpeechContent();
          return; // 清空后不再继续播放
        }
              
        // 🔧 关键修复:检查队列是否为空,如果为空则不再继续
        if (this.ttsAudioQueue.length === 0 && this.ttsConversionQueue.length === 0) {
          console.log('[TTS 预加载] ✅ 队列为空,停止播放链');
          if (onComplete) {
            onComplete();
          }
          return;
        }
              
        console.log('[TTS 播放] 🔄 准备调用 onComplete 回调,继续播放下一个');
        if (onComplete) {
          onComplete();
        }
      });
      
      audioContext.onError((err) => {
        console.error('[TTS 播放] ✗ 播放失败');
        console.error('[TTS 播放] ❌ 错误码:', err.errCode);
        console.error('[TTS 播放] ❌ 错误信息:', err.errMsg);
        console.error('[TTS 播放] ❌ 完整错误对象:', JSON.stringify(err));
        console.error('[TTS 播放] 🔗 失败的音频URL:', audioItem.audioUrl);
        clearPlaybackTimeout(); // 清除超时定时器
        clearPlayStartTimeout(); // 🔧 清除播放启动超时检测
        
        // 🔧 关键修复：先清空引用，再销毁
        this.audioContext = null;
        this.isAudioPlaying = false;
        this.currentSpeakingText = null;
        
        try {
          audioContext.destroy();
        } catch (e) {
          console.warn('[TTS 预加载] 销毁音频上下文失败', e);
        }
        
        // 即使失败也继续下一个
        if (onComplete) {
          onComplete();
        }
      });
      
      // 监听暂停事件（用户可能调整音量）
      audioContext.onPause(() => {
        console.log('[TTS 播放] ⏸ 音频被暂停（可能是音量调节）');
      });
      
      // 监听停止事件
      audioContext.onStop(() => {
        console.log('[TTS 播放] ⏹ 音频被停止');
      });
      
      // 🔧 关键优化：不再使用延迟播放，改为在 onCanplay 中播放
      // 保留此代码作为备用，如果 onCanplay 没有触发
      setTimeout(() => {
        if (hasPlayed) {
          console.log('[TTS 播放] ℹ️ 已在 onCanplay 中播放，跳过备用播放');
          return;
        }
        
        console.log('[TTS 播放] ⚠️ onCanplay 未触发，使用备用播放方案');
        console.log('[TTS 播放] ▶ 准备调用 play()');
        console.log('[TTS 播放] 🔍 此时 audioContext 是否存在:', !!this.audioContext);
        console.log('[TTS 播放] 🔍 此时 audioContext.src:', audioContext.src);
        console.log('[TTS 播放] 🔍 此时 isAudioPlaying:', this.isAudioPlaying);
        
        try {
          audioContext.play();
          console.log('[TTS 播放] ▶ play() 方法已成功调用');
        } catch (e) {
          console.error('[TTS 播放] ❌ play() 调用失败:', e);
          console.error('[TTS 播放] ❌ 错误堆栈:', e.stack);
          // 播放失败也要继续下一个
          this.audioContext = null;
          this.isAudioPlaying = false;
          this.currentSpeakingText = null;
          if (onComplete) {
            onComplete();
          }
        }
      }, 500); // 增加到 500ms，给 onCanplay 更多时间触发
      // #endif
      
      // #ifndef MP-WEIXIN
      console.log('[TTS 预加载] ⚠️ H5 环境不支持 InnerAudioContext');
      // #endif
    },

    /**
     * 处理长文本分段完成后的逻辑
     */
    handleSegmentComplete() {
      console.log('[TTS 分段] 🔄 处理分段完成逻辑');
      console.log('[TTS 分段] 📊 currentSegmentIndex:', this.currentSegmentIndex);
      console.log('[TTS 分段] 📊 currentLongTextSegments.length:', this.currentLongTextSegments.length);
      
      // 索引+1，指向下一段
      this.currentSegmentIndex++;
      
      // 判断是否还有下一段
      if (this.currentSegmentIndex < this.currentLongTextSegments.length) {
        console.log('[TTS 分段] ➡️ 还有下一段，继续播报第', this.currentSegmentIndex + 1, '段');
        const nextSegment = this.currentLongTextSegments[this.currentSegmentIndex];
        console.log('[TTS 分段] 📝 第', this.currentSegmentIndex + 1, '段内容预览:', nextSegment.substring(0, 80));
        console.log('[TTS 分段] 📏 第', this.currentSegmentIndex + 1, '段长度:', nextSegment.length);
        this.currentSpeakingText = nextSegment;
        
        // 🔧 关键修复：直接调用 TTS 转换和播放，不再经过 handleNonH5Speech 的长度检查
        // 因为分段已经在 startLongTextPlayback 中完成，这里的每一段都应该 <= 300 字符
        this.playSingleSegment(nextSegment, () => {
          console.log('[TTS 分段] ✅ 长文本第', this.currentSegmentIndex + 1, '段播报完成');
          // 递归调用，继续检查是否有下一段
          this.handleSegmentComplete();
        });
      } else {
        console.log('[TTS 分段] 🏁 长文本所有分段播报完成');
        
        // 保存回调并清空变量
        const callback = this.longTextCallback;
        this.currentLongTextSegments = [];
        this.currentSegmentIndex = 0;
        this.longTextCallback = null;
        
        // 解锁
        this.isTTSPlaying = false;
        this.currentSpeakingText = null;
        
        // 调用原始回调（如果存在）
        if (callback && typeof callback === 'function') {
          console.log('[TTS 分段] 🔄 调用原始回调');
          try {
            callback();
          } catch (error) {
            console.error('[TTS 分段] ❌ 回调执行失败:', error);
          }
        }
        
        // 处理下一个队列项
        if (!this.isPaused) {
          this.speakNextFromQueue();
        }
      }
    },
    
    /**
     * 播放单个分段（不经过长度检查）
     * @param {string} text - 已分段的文本
     * @param {function} onComplete - 完成回调
     */
    playSingleSegment(text, onComplete) {
      console.log('[TTS 单段] 🎤 开始播放单个分段，长度:', text.length);
      
      // #ifdef MP-WEIXIN
      const plugin = requirePlugin("WechatSI");
      if (!plugin || !plugin.textToSpeech) {
        console.error('[TTS 单段] ❌ 微信同声传译插件不可用');
        if (onComplete) onComplete();
        return;
      }
      
      // 清理文本
      let cleanedText = text
        .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')
        .replace(/["""]/g, '"')
        .replace(/['''']/g, "'")
        .replace(/：/g, ':')
        .replace(/——/g, '-')
        .replace(/……/g, '...')
        .trim();
      
      console.log('[TTS 单段] 🔄 开始 TTS 转换');
      plugin.textToSpeech({
        lang: "zh_CN",
        content: cleanedText,
        success: (res) => {
          console.log('[TTS 单段] ✅ 转换成功');
          
          const audioUrl = res.voice_file || res.filename;
          if (!audioUrl) {
            console.error('[TTS 单段] ❌ 未找到音频 URL');
            if (onComplete) onComplete();
            return;
          }
          
          // 创建音频上下文
          if (this.audioContext) {
            try {
              this.audioContext.stop();
              this.audioContext.destroy();
            } catch (e) {
              console.warn('[TTS 单段] 清理旧音频失败', e);
            }
          }
          
          const audioContext = uni.createInnerAudioContext();
          this.audioContext = audioContext;
          
          audioContext.autoplay = false;
          audioContext.obeyMuteSwitch = false;
          audioContext.volume = 1.0;
          audioContext.src = audioUrl;
          
          console.log('[TTS 单段] 🎵 音频源已设置:', audioUrl);
          
          audioContext.onEnded(() => {
            console.log('[TTS 单段] ✅ 播放完成');
            try {
              audioContext.destroy();
            } catch (e) {
              console.warn('[TTS 单段] 销毁音频失败', e);
            }
            this.audioContext = null;
            
            if (onComplete) {
              onComplete();
            }
          });
          
          audioContext.onError((err) => {
            console.error('[TTS 单段] ❌ 播放失败', err);
            try {
              audioContext.destroy();
            } catch (e) {}
            this.audioContext = null;
            
            // 即使失败也继续下一段
            if (onComplete) onComplete();
          });
          
          // 延迟播放
          setTimeout(() => {
            console.log('[TTS 单段] ▶ 开始播放');
            audioContext.play();
          }, 100);
        },
        fail: (err) => {
          console.error('[TTS 单段] ❌ 转换失败', err);
          if (onComplete) onComplete();
        }
      });
      // #endif
      
      // #ifndef MP-WEIXIN
      // H5 环境降级
      console.log('[TTS 单段] ⚠️ H5 环境，使用降级方案');
      setTimeout(() => {
        if (onComplete) onComplete();
      }, 3000);
      // #endif
    },

    /**
     * 启动长文本分段播报（由 navigation.js 调用）
     * @param {string} text - 完整的长文本
     * @param {function} callback - 全部播报完成后的回调
     */
    startLongTextPlayback(text, callback) {
      console.log('[TTS 长文本] 🚀 启动长文本分段播报');
      console.log('[TTS 长文本] 📏 文本长度:', text.length);
      
      // 清理文本，移除不可见字符、标准化可能导致解析错误的标点符号
      let cleanedText = text
        .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')  // 移除空白和不可见字符
        .replace(/["""]/g, '"')  // 中文双引号（左右）转英文
        .replace(/['''']/g, "'")  // 中文单引号（左右）转英文
        .replace(/：/g, ':')       // 中文冒号转英文（可能引起解析错误）
        .replace(/——/g, '-')     // 中文破折号转英文
        .replace(/……/g, '...')   // 中文省略号转英文
        // 注意：保留中文逗号、句号等，因为 TTS 需要它们来正确断句
        .trim();
      console.log('[TTS 长文本] 🧹 清理后长度:', cleanedText.length);
      console.log('[TTS 长文本] 📝 清理后预览:', cleanedText.substring(0, 80));
      
      // 使用 splitAndSpeak 的逻辑进行分段，但不立即播报
      const MAX_LENGTH = 300;  // 降低分段长度，避免微信TTS插件处理长文本失败
      const segments = [];
      
      // 按句子分割
      const sentences = cleanedText.match(/[^。！？.!?]+[。！？.!?]+/g) || [cleanedText];
      console.log('[TTS 长文本] 📝 句子数量:', sentences.length);
      
      let currentSegment = '';
      sentences.forEach(sentence => {
        if ((currentSegment + sentence).length > MAX_LENGTH && currentSegment) {
          // 清理并验证每一段
          const cleanedSegment = currentSegment.trim();
          if (cleanedSegment.length > 0) {
            segments.push(cleanedSegment);
          }
          currentSegment = sentence;
        } else {
          currentSegment += sentence;
        }
      });
      if (currentSegment) {
        // 清理最后一段
        const cleanedSegment = currentSegment.trim();
        if (cleanedSegment.length > 0) {
          segments.push(cleanedSegment);
        }
      }
      
      console.log('[TTS 长文本] 📊 共分为', segments.length, '段');
      segments.forEach((seg, idx) => {
        console.log(`[TTS 长文本]   第${idx + 1}段长度:`, seg.length);
        console.log(`[TTS 长文本]   第${idx + 1}段预览:`, seg.substring(0, 50));
      });
      
      // 保存分段数组和回调
      this.currentLongTextSegments = segments;
      this.currentSegmentIndex = 0;
      this.longTextCallback = callback; // 保存回调
      
      // 开始播报第一段
      if (segments.length > 0) {
        const firstSegment = segments[0];
        this.currentSpeakingText = firstSegment;
        
        console.log('[TTS 长文本] ▶ 开始播报第 1 段');
        // 🔧 关键修复：使用 playSingleSegment，避免重复分段
        this.playSingleSegment(firstSegment, () => {
          console.log('[TTS 长文本] ✅ 第 1 段播报完成');
          // 调用 handleSegmentComplete 处理后续段落
          this.handleSegmentComplete();
        });
      } else {
        console.warn('[TTS 长文本] ⚠️ 分段结果为空');
        if (callback) callback();
      }
    },

    /**
     * 添加文本到语音队列
     * @param {string} text - 要播报的文本
     */
    addTextToSpeechQueue(text) {
      console.log('[TTS] 📥 收到文本，长度:', text ? text.length : 0);
      
      // 检查参数有效性
      if (!text || typeof text !== 'string' || text.trim() === '') {
        console.warn('[TTS] ⚠️ 文本为空或无效，跳过');
        return;
      }
      
      // 如果导航已停止或已暂停，不加入队列
      if (!this.isNavigating || this.isPaused) {
        console.warn('[TTS] ⚠️ 导航未进行或已暂停，跳过入队');
        return;
      }
      
      // #ifdef MP-WEIXIN
      // 将文本加入文本队列
      console.log('[TTS] 🔄 将文本加入 speechTextQueue');
      this.speechTextQueue.push(text);
      console.log('[TTS] 📊 当前文本队列长度:', this.speechTextQueue.length);
      
      // 触发TTS转换流程
      this.processTTSConversionQueue();
      
      // 触发音频播放队列处理
      this.processAudioPlaybackQueue();
      // #endif
      
      // #ifndef MP-WEIXIN
      // H5 环境：使用 Web Speech API
      console.log('[TTS] 🔄 使用 H5 Web Speech API');
      this.speechTextQueue.push(text);
      this.processSpeechQueue();
      // #endif
    },

    /**
     * 处理TTS转换队列
     * 从文本队列中取出文本，调用微信TTS插件转换为音频，放入音频播放队列
     */
    async processTTSConversionQueue() {
      // #ifdef MP-WEIXIN
      console.log('[TTS] 🔄 开始处理TTS转换队列');
      console.log('[TTS] 📊 文本队列长度:', this.speechTextQueue.length);
      
      // 如果正在转换或队列为空，跳过
      if (this.isTTSConverting || this.speechTextQueue.length === 0) {
        console.log('[TTS] ℹ️ 正在转换中或文本队列为空，跳过');
        return;
      }
      
      // 标记正在转换
      this.isTTSConverting = true;
      
      try {
        // 从文本队列中取出一个文本
        const text = this.speechTextQueue.shift();
        if (!text) {
          console.log('[TTS] ⚠️ 文本队列为空');
          this.isTTSConverting = false;
          return;
        }
        
        console.log('[TTS] 📝 开始转换文本，长度:', text.length);
        console.log('[TTS] 📋 文本预览:', text.substring(0, 50) + '...');
        
        // 检查是否为长文本（超过300字符）
        if (text.length > 300) {
          console.log('[TTS] 📏 长文本，需要切片');
          // 按句子切片
          const MAX_LENGTH = 300;
          const sentences = text.match(/[^。！？.!?]+[。！？.!?]+/g) || [text];
          const segments = [];
          let currentSegment = '';
          
          sentences.forEach(sentence => {
            if ((currentSegment + sentence).length > MAX_LENGTH && currentSegment) {
              segments.push(currentSegment.trim());
              currentSegment = sentence;
            } else {
              currentSegment += sentence;
            }
          });
          if (currentSegment) {
            segments.push(currentSegment.trim());
          }
          
          console.log('[TTS] 📊 共分为', segments.length, '段');
          
          // 逐个转换切片
          for (let i = 0; i < segments.length; i++) {
            const segment = segments[i];
            console.log('[TTS] 📝 转换第', i + 1, '段，长度:', segment.length);
            await this.convertTextToAudio({ text: segment });
          }
        } else {
          // 短文本直接转换
          console.log('[TTS] 📏 短文本，直接转换');
          await this.convertTextToAudio({ text: text });
        }
        
      } catch (error) {
        console.error('[TTS] ❌ TTS转换异常:', error);
      } finally {
        this.isTTSConverting = false;
        // 继续处理下一个文本
        setTimeout(() => {
          this.processTTSConversionQueue();
        }, 100);
      }
      // #endif
    },
    
    /**
     * 🔧 将单个文本转换为音频（微信TTS插件）
     * @param {object} task - 转换任务对象
     */
    convertTextToAudio(task) {
      // #ifdef MP-WEIXIN
      return new Promise((resolve, reject) => {
        console.log('[TTS 预加载] 🔄 开始调用微信TTS插件');
        
        const plugin = requirePlugin("WechatSI");
        if (!plugin || !plugin.textToSpeech) {
          console.error('[TTS 预加载] ❌ 微信同声传译插件不可用');
          task.status = 'failed';
          this.isTTSConverting = false;
          resolve();
          return;
        }
        
        // 清理文本
        let cleanedText = task.text
          .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')
          .replace(/["""]/g, '"')
          .replace(/['''']/g, "'")
          .replace(/：/g, ':')
          .replace(/——/g, '-')
          .replace(/……/g, '...')
          .trim();
        
        if (cleanedText.length === 0) {
          console.warn('[TTS 预加载] ⚠️ 清理后文本为空');
          task.status = 'failed';
          this.isTTSConverting = false;
          resolve();
          return;
        }
        
        // 调用微信TTS插件
        plugin.textToSpeech({
          lang: "zh_CN",
          content: cleanedText,
          success: (res) => {
            console.log('[TTS 预加载] ✅ TTS转换成功');
            
            // 🔧 关键修复：微信 TTS 插件返回的字段是 voice_file
            const audioUrl = res.voice_file || res.filename;
            if (!audioUrl) {
              console.error('[TTS 预加载] ❌ 未找到音频 URL');
              task.status = 'failed';
              this.isTTSConverting = false;
              resolve();
              return;
            }
            
            console.log('[TTS 预加载] 🎵 音频URL:', audioUrl);
            
            // 🔧 关键修复：再次检查导航状态，防止停止后仍加入队列
            if (!this.isNavigating) {
              console.log('[TTS 预加载] 🚫 导航已停止，丢弃音频结果');
              task.status = 'cancelled';
              this.isTTSConverting = false;
              resolve();
              return;
            }
            
            // 更新任务状态
            task.status = 'completed';
            
            // 添加到音频播放队列
            const audioItem = {
              text: task.text,
              audioUrl: audioUrl,
              index: this.ttsAudioQueue.length + 1,
              createdAt: Date.now()
            };
            
            this.ttsAudioQueue.push(audioItem);
            console.log('[TTS 预加载] ✅ 已添加到音频播放队列，当前队列长度:', this.ttsAudioQueue.length);
            
            // 释放转换锁
            this.isTTSConverting = false;
            
            // 继续处理下一个文本
            setTimeout(() => {
              this.processTTSConversionQueue();
            }, 100);
            
            // 触发音频播放队列处理
            if (!this.isAudioPlaying && this.isNavigating && !this.isPaused) {
              console.log('[TTS 预加载] ▶ 触发音频播放队列处理');
              this.processAudioPlaybackQueue();
            }
            
            resolve();
          },
          fail: (err) => {
            console.error('[TTS 预加载] ❌ TTS转换失败');
            console.error('[TTS 预加载] ❌ 错误码:', err.retcode, '错误信息:', err.msg);
            
            task.status = 'failed';
            this.isTTSConverting = false;
            
            // 即使失败也继续处理下一个
            setTimeout(() => {
              this.processTTSConversionQueue();
            }, 100);
            
            resolve();
          }
        });
      });
      // #endif
    },
    
    /**
     * 处理音频播放队列
     * 按顺序播放已转换完成的音频文件
     */
    processAudioPlaybackQueue() {
      // #ifdef MP-WEIXIN
      console.log('[TTS] 🔄 开始处理音频播放队列');
      console.log('[TTS] 📊 音频播放队列长度:', this.ttsAudioQueue.length);
      
      // 队列为空，无需处理
      if (this.ttsAudioQueue.length === 0) {
        console.log('[TTS] ⚠️ 音频队列为空，跳过');
        return;
      }
      
      // 已暂停或导航已停止，不处理
      if (this.isPaused || !this.isNavigating) {
        console.log('[TTS] ⏸ 已暂停或导航已停止，等待');
        return;
      }
      
      // 如果当前正在播放，等待播放完成
      if (this.isAudioPlaying) {
        console.log('[TTS] ℹ️ 当前正在播放，等待完成');
        return;
      }
      
      // 开始播放队列中的第一个音频
      console.log('[TTS] ▶ 开始播放下一个音频');
      this.playNextAudio();
      // #endif
    },
    
    /**
     * 播放下一个音频（从音频播放队列中取出并播放）
     */
    playNextAudio() {
      // #ifdef MP-WEIXIN
      // 检查是否有待播放的音频且未暂停
      if (this.ttsAudioQueue.length > 0 && !this.isPaused && this.isNavigating) {
        // 如果当前正在播放，等待
        if (this.isAudioPlaying) {
          console.warn('[TTS] ⚠️ 正在播放中，等待当前音频完成');
          return;
        }
        
        // 从队列头部取出音频
        const audioItem = this.ttsAudioQueue.shift();
        
        // 检查导航状态
        if (!this.isNavigating) {
          console.log('[TTS] 🚫 导航已停止，丢弃所有待播放音频');
          this.ttsAudioQueue = [];
          return;
        }
        
        console.log('[TTS] 🎬 开始播放音频，索引:', audioItem.index);
        console.log('[TTS] 📝 文本长度:', audioItem.text.length);
        console.log('[TTS] 🔗 音频URL:', audioItem.audioUrl);
        console.log('[TTS] 📊 剩余音频队列长度:', this.ttsAudioQueue.length);
        
        // 播放音频
        this.playAudioItem(audioItem, () => {
          console.log('[TTS] ✅ 音频播放完成回调');
          
          // 播放完成后，继续播放下一个
          if (!this.isPaused && this.isNavigating) {
            setTimeout(() => {
              this.playNextAudio();
            }, 200); // 延迟200ms，让播放更流畅
          }
        });
      } else if (this.isPaused) {
        console.log('[TTS] ⏸ 已暂停，等待继续');
      } else if (!this.isNavigating) {
        console.log('[TTS] 🛑 导航已停止，清空音频队列');
        this.ttsAudioQueue = [];
      } else {
        console.log('[TTS] 🏁 音频队列为空，全部播放完成');
      }
      // #endif
    },
    
    /**
     * 🔧 处理语音文本队列（H5环境使用）
     */
    processSpeechQueue() {
      console.log('[TTS 处理] 🔄 开始处理语音队列');
      console.log('[TTS 处理] 📊 speechTextQueue 长度:', this.speechTextQueue.length);
      console.log('[TTS 处理] 🔍 isPaused:', this.isPaused);
      console.log('[TTS 处理] 🔍 isTTSPlaying:', this.isTTSPlaying);
      
      // 队列为空，无需处理
      if (this.speechTextQueue.length === 0) {
        console.log('[TTS 处理] ⚠️ 队列为空，跳过');
        return;
      }
      
      // 已暂停，等待继续
      if (this.isPaused) {
        console.log('[TTS 处理] ⏸ 已暂停，等待继续');
        return;
      }
      
      // 如果当前正在播放，新文本已在 speechTextQueue 中，等待自动继续
      if (this.isTTSPlaying) {
        console.log('[TTS 处理] ℹ️ 当前正在播放，新文本已加入 speechTextQueue，等待自动继续');
        console.log('[TTS 处理] 📊 speechTextQueue 长度:', this.speechTextQueue.length);
        return;
      }
      
      // 当前空闲，开始播报队列中的第一条
      console.log('[TTS 处理] 📤 开始播报队列中的文本');
      this.speakNextFromQueue();
    },

    /**
     * 从队列中取下一条文本播报
     */
    speakNextFromQueue() {
      // 检查是否有待播放的文本且未暂停
      if (this.speechTextQueue.length > 0 && !this.isPaused) {
        // 🔒 设置播放锁，防止并发播放
        if (this.isTTSPlaying) {
          console.warn('[TTS] ⚠️ 正在播放中，等待当前文本完成');
          return;
        }
        
        const text = this.speechTextQueue.shift(); // 从队列头部取出
        this.currentSpeakingText = text;
        this.isTTSPlaying = true; // 🔒 加锁
        
        console.log('[TTS] 🔒 设置播放锁，开始播报，剩余队列长度:', this.speechTextQueue.length);
        console.log('[TTS] 📝 内容预览:', text.substring(0, 50) + '...');
        
        // #ifdef H5
        // H5 环境使用 Web Speech API
        if (window.speechSynthesis) {
          const utterance = new SpeechSynthesisUtterance(text);
          utterance.lang = 'zh-CN';
          utterance.onstart = () => { 
            this.currentSpeechIndex = 0; 
          };
          utterance.onend = () => { 
            console.log('[TTS] ✅ H5 播报完成，解锁');
            this.isTTSPlaying = false; // 🔓 解锁
            this.currentSpeakingText = null;
            if (!this.isPaused) this.speakNextFromQueue(); 
          };
          utterance.onerror = (event) => { 
            console.error('[TTS] ❌ H5 播报错误:', event);
            this.isTTSPlaying = false; // 🔓 解锁
            this.currentSpeakingText = null;
            if (!this.isPaused) this.speakNextFromQueue(); 
          };
          utterance.onboundary = (event) => { 
            if (event.name === 'word') {
              this.currentSpeechIndex = event.charIndex;
            }
          };
          try { 
            speechSynthesis.speak(utterance); 
          } catch (error) { 
            console.error('[TTS] ❌ H5 播报异常:', error);
            this.isTTSPlaying = false; // 🔓 解锁
            this.currentSpeakingText = null;
            uni.showToast({ title: `语音播报失败`, icon: 'none', duration: 3000 });
            if (!this.isPaused) this.speakNextFromQueue(); 
          }
        } else {
          // H5 降级方案
          uni.showToast({ title: `语音: ${text.substring(0, 20)}...`, icon: 'none', duration: 3000 });
          setTimeout(() => { 
            this.isTTSPlaying = false; // 🔓 解锁
            this.currentSpeakingText = null;
            if (!this.isPaused) this.speakNextFromQueue(); 
          }, 3000);
        }
        // #endif
        
        // #ifndef H5
        // 微信小程序环境：使用微信同声传译插件
        handleNonH5Speech(text, () => {
          console.log('[TTS] ✅ 微信小程序播报完成，解锁');
          this.isTTSPlaying = false; // 🔓 解锁
          this.currentSpeakingText = null;
          if (!this.isPaused) {
            this.speakNextFromQueue();
          }
        }, this); // 传递 this 作为 context，用于管理音频上下文
        // #endif
      } else if (this.isPaused) {
        // 已暂停
        console.log('[TTS] ⏸ 已暂停，等待继续');
      } else {
        // 队列为空，全部播放完毕
        console.log('[TTS] 🏁 队列为空，全部播放完成');
        this.currentSpeakingText = null;
        this.isTTSPlaying = false; // 🔓 确保解锁
        this.speechTimer = null;
      }
    },

    /**
     * 播报文本数组（微信小程序适配）
     * @param {Array} textArray - 要播报的文本数组
     */
    speakTextArray(textArray) {
      if (!textArray || textArray.length === 0) {
        console.log('[TTS] ⚠️ 文本数组为空');
        return;
      }
      
      // 重要：如果当前没有在播放，清空 currentSpeechQueue，避免累积
      if (!this.currentSpeakingText && this.currentSpeechQueue.length === 0) {
        this.currentSpeechQueue = [];
        console.log('[TTS] 🗑 清空播放队列，准备新序列');
      }
      
      // 将文本添加到当前播放队列
      this.currentSpeechQueue = [...this.currentSpeechQueue, ...textArray];
      console.log('[TTS] 📝 添加到播放队列，当前播放队列长度:', this.currentSpeechQueue.length);
      console.log('[TTS] 📋 队列内容预览:');
      this.currentSpeechQueue.forEach((item, idx) => {
        console.log(`   [${idx + 1}] 长度: ${item.length}, 预览: ${item.substring(0, 30)}...`);
      });
      
      const speakNext = () => {
        // 检查是否有待播放的文本且未暂停
        if (this.currentSpeechQueue.length > 0 && !this.isPaused) {
          // 🔒 设置播放锁，防止并发播放
          if (this.isTTSPlaying) {
            console.warn('[TTS] ⚠️ 正在播放中，等待当前文本完成');
            return;
          }
          
          const text = this.currentSpeechQueue.shift();
          this.currentSpeakingText = text;
          this.isTTSPlaying = true; // 🔒 加锁
          
          console.log('[TTS] 🔒 设置播放锁，开始播报第', this.currentSpeechQueue.length + 1, '条，长度:', text.length);
          console.log('[TTS] 📝 内容预览:', text.substring(0, 50) + '...');
          
          // #ifdef H5
          // H5 环境使用 Web Speech API
          if (window.speechSynthesis) {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'zh-CN';
            utterance.onstart = () => { 
              this.currentSpeechIndex = 0; 
            };
            utterance.onend = () => { 
              console.log('[TTS] ✅ H5 播报完成，解锁');
              this.isTTSPlaying = false; // 🔓 解锁
              if (!this.isPaused) speakNext(); 
            };
            utterance.onerror = (event) => { 
              console.error('[TTS] ❌ H5 播报错误:', event);
              this.isTTSPlaying = false; // 🔓 解锁
              if (!this.isPaused) speakNext(); 
            };
            utterance.onboundary = (event) => { 
              if (event.name === 'word') {
                this.currentSpeechIndex = event.charIndex;
              }
            };
            try { 
              speechSynthesis.speak(utterance); 
            } catch (error) { 
              console.error('[TTS] ❌ H5 播报异常:', error);
              this.isTTSPlaying = false; // 🔓 解锁
              uni.showToast({ title: `语音播报失败`, icon: 'none', duration: 3000 });
              if (!this.isPaused) speakNext(); 
            }
          } else {
            // H5 降级方案
            uni.showToast({ title: `语音: ${text.substring(0, 20)}...`, icon: 'none', duration: 3000 });
            setTimeout(() => { 
              this.isTTSPlaying = false; // 🔓 解锁
              if (!this.isPaused) speakNext(); 
            }, 3000);
          }
          // #endif
          
          // #ifndef H5
          // 微信小程序环境：使用微信同声传译插件
          handleNonH5Speech(text, () => {
            console.log('[TTS] ✅ 微信小程序播报完成，解锁');
            this.isTTSPlaying = false; // 🔓 解锁
            if (!this.isPaused) {
              speakNext();
            }
          }, this); // 传递 this 作为 context，用于管理音频上下文
          // #endif
        } else if (this.isPaused) {
          // 已暂停
          console.log('[TTS] ⏸ 已暂停，等待继续');
        } else {
          // 当前序列播放完毕
          console.log('[TTS] 🏁 当前序列播放完毕');
          this.currentSpeakingText = null;
          this.currentSpeechQueue = []; // 清空播放队列
          this.isTTSPlaying = false; // 🔓 确保解锁
          this.speechTimer = null;
          
          // 重要：检查 speechTextQueue 中是否有新添加的内容
          console.log('[TTS] 🔍 检查 speechTextQueue 中是否有新内容，当前队列长度:', this.speechTextQueue.length);
          if (this.speechTextQueue.length > 0) {
            console.log('[TTS] ✅ 发现新内容，继续处理队列');
            // 立即处理队列，不需要延迟
            this.processSpeechQueue();
          } else {
            console.log('[TTS] 🏁 队列为空，全部播放完成');
          }
        }
      };
      
      // 启动播放
      if (!this.isPaused) {
        if (!this.speechTimer) {
          console.log('[TTS] ▶ 开始播放队列');
          speakNext();
        }
      }
    },



    /**
     * 处理继续听逻辑
     */
    async handleContinueListen(navigationData, dest) {
      try {
        // 1. 调用 getUser
        const userText = await getUserText(navigationData);
        let textToSpeak = userText;
        // 优先使用 data 字段的内容
        if (userText && typeof userText === 'object' && userText.data && typeof userText.data === 'string') {
          textToSpeak = userText.data;
          console.log('[继续听] ✓ 使用 data 字段内容，长度:', textToSpeak.length);
        } else if (userText && typeof userText === 'string') {
          console.log('[继续听] ✓ getUser 返回成功，长度:', userText.length);
        }
        if (textToSpeak && typeof textToSpeak === 'string' && this.isNavigating) {
          this.addTextToSpeechQueue(textToSpeak);
        }
      } catch (error) { }

      try {
        // 2. 调用 getArea
        if (this.userLocation) {
          console.log('[文化导览] 🔄 开始调用 getArea...');
          const areaResponse = await pollTask(
            () => getAreaAsync({
              longitude: this.userLocation.lng,
              latitude: this.userLocation.lat,
              userId: 1,
              content: this.appliedContent
            }),
            undefined,
            2000
          );
          console.log('[文化导览] ✅ getArea 返回:', areaResponse);
          console.log('[文化导览] 📊 返回类型:', typeof areaResponse);
          console.log('[文化导览] 📏 返回长度:', typeof areaResponse === 'string' ? areaResponse.length : 'N/A');
          
          // pollTask 已经处理了 Map 提取，这里直接检查是否为字符串
          if (areaResponse && typeof areaResponse === 'string') {
            console.log('[文化导览] ✓ 检测到字符串内容，准备添加到语音队列');
            console.log('[文化导览] 📝 内容预览:', areaResponse.substring(0, 50) + '...');
            console.log('[文化导览] 🔍 isNavigating:', this.isNavigating);
            console.log('[文化导览] 🔍 isPaused:', this.isPaused);
            
            this.addTextToSpeechQueue(areaResponse);
            
            console.log('[文化导览] ✅ 已添加到语音队列');
            console.log('[文化导览] 📊 当前语音队列长度:', this.speechTextQueue.length);
          } else if (areaResponse && typeof areaResponse === 'object') {
            console.log('[文化导览] ⚠️ 返回仍是对象，尝试手动提取');
            // 兼容旧逻辑：如果还是对象，尝试提取
            const keys = Object.keys(areaResponse).filter(key => key !== '@type');
            console.log('[文化导览] 🔑 可用的 keys:', keys);
            if (keys.length > 0) {
              this.cacheKey = keys[0];
              const content = areaResponse[this.cacheKey];
              console.log('[文化导览] ✓ 从 Map 中获取内容, cacheKey:', this.cacheKey);
              console.log('[文化导览] 📝 内容类型:', typeof content);
              if (content && typeof content === 'string') {
                console.log('[文化导览] ✓ 添加内容到语音队列');
                this.addTextToSpeechQueue(content);
              }
            }
          } else {
            console.warn('[文化导览] ⚠️ 返回内容为空或类型未知:', areaResponse);
          }
        } else {
          console.warn('[文化导览] ⚠️ userLocation 为空，跳过 getArea 调用');
        }
      } catch (error) {
        console.error('[文化导览] ✗ getArea 失败:', error);
        console.error('[文化导览] ✗ 错误详情:', error.message);
        console.error('[文化导览] ✗ 错误堆栈:', error.stack);
      }

      try {
        // 3. 调用 startNavigationNoBook 获取轨迹 ID（整型）
        const trajectoryId = await startNavigationNoBook(navigationData);
        
        if (trajectoryId !== null && trajectoryId !== undefined && this.isNavigating) {
          // 4. 🔧 关键优化：立即发起第一次 getChapter 请求，不等朗读完成
          console.log('[继续听] ⚡ 立即发起第一次 getChapter 请求');
          const chapterText = await pollTask(
            () => getChapter({ bookId: this.continueListenBookId, trajectoryId: trajectoryId, length: 3000 }),
            undefined,
            2000
          );
          console.log('[继续听] getChapter 返回:', chapterText, '类型:', typeof chapterText);
          
          if (chapterText && typeof chapterText === 'string' && this.isNavigating) {
            if (chapterText === '结束') {
              uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
            } else {
              console.log('[继续听] ✓ 添加章节内容到语音队列');
              this.addTextToSpeechQueue(chapterText);
            }
          }
          
          // 5. 🔧 关键优化：清除旧的定时器（防止重复创建）
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
            console.log('[继续听] 🗑 清除旧定时器');
          }
          
          // 保存当前的 bookId 和 trajectoryId
          this.currentBookId = { bookId: this.continueListenBookId, trajectoryId: trajectoryId };
          console.log('[继续听] 💾 保存 currentBookId:', this.currentBookId);
          
          // 🔧 关键优化：立即发起第二次 getChapter 请求，不等朗读完成
          console.log('[继续听] ⚡ 文本已入队，立即发起下一次 getChapter 请求');
          setTimeout(() => {
            this.fetchNextChapterContinueListen(trajectoryId);
          }, 100); // 延迟 100ms，避免过于频繁的请求
          
          // 设置保底定时器，每 120 秒自动请求一次
          console.log('[继续听] ⏰ 创建保底定时器，间隔: 120秒');
          this.timer = setInterval(() => {
            console.log('[继续听] ⏰ 保底定时器触发');
            if (this.currentBookId && this.isNavigating && !this.isPaused && this.mode === 0) {
              console.log('[继续听] ✅ 条件满足，调用 fetchNextChapterContinueListen');
              this.fetchNextChapterContinueListen(trajectoryId);
            } else {
              console.log('[继续听] ⚠️ 条件不满足，跳过本次请求');
            }
          }, 120000);
          
          console.log('[继续听] ✅ 定时器已创建，timer ID:', this.timer);
        }
      } catch (error) { }
      
      this.navigationParams = navigationData;
    },

    /**
     * 🔧 继续听模式：获取下一个章节内容（异步轮询，返回后立即发起下一次请求）
     */
    async fetchNextChapterContinueListen(trajectoryId) {
      try {
        if (!this.currentBookId || !this.isNavigating || this.isPaused || this.mode !== 0) {
          console.log('[继续听] ⚠️ 条件不满足，跳过请求');
          return;
        }
        
        console.log('[继续听] 📝 请求参数:', {
          bookId: this.currentBookId.bookId,
          trajectoryId: trajectoryId,
          length: 3000
        });
        
        // 使用 pollTask 进行异步轮询请求
        const updatedChapterText = await pollTask(
          () => getChapter({ bookId: this.currentBookId.bookId, trajectoryId: trajectoryId, length: 3000 }),
          undefined,
          2000
        );
        
        console.log('[继续听] 📖 getChapter 返回:', updatedChapterText);
        console.log('[继续听] 📊 返回类型:', typeof updatedChapterText);
        
        if (updatedChapterText && typeof updatedChapterText === 'string' && this.isNavigating && !this.isPaused && this.mode === 0) {
          if (updatedChapterText === '结束') {
            console.log('[继续听] 🏁 小说已结束，清除定时器');
            clearInterval(this.timer);
            this.timer = null;
            uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
          } else {
            console.log('[继续听] ✓ 添加章节内容到语音队列，长度:', updatedChapterText.length);
            this.addTextToSpeechQueue(updatedChapterText);
            
            // 🔧 关键优化：加入队列后立即发起下一次请求，不等朗读完成
            console.log('[继续听] ⚡ 文本已入队，立即发起下一次 getChapter 请求');
            setTimeout(() => {
              this.fetchNextChapterContinueListen(trajectoryId);
            }, 100); // 延迟 100ms，避免过于频繁的请求
          }
        } else {
          console.warn('[继续听] ⚠️ 返回内容无效或状态异常');
          console.warn('[继续听] 📊 updatedChapterText:', updatedChapterText);
          console.warn('[继续听] 📊 isNavigating:', this.isNavigating);
          console.warn('[继续听] 📊 isPaused:', this.isPaused);
          console.warn('[继续听] 📊 mode:', this.mode);
        }
      } catch (err) {
        console.error('[继续听] ❌ getChapter 调用失败:', err);
        console.error('[继续听] ❌ 错误详情:', err.message);
        console.error('[继续听] ❌ 错误堆栈:', err.stack);
      }
    },

    /**
     * 处理正常导航逻辑（增强版 - 确保 getArea 内容被朗读）
     */
    async handleNormalNavigation(navigationData) {
      console.log('[小说模式] 🚀 开始正常导航流程');
      
      // 1. 调用 getUser
      try {
        console.log('[小说模式] 📞 调用 getUser...');
        const userText = await getUserText(navigationData);
        let textToSpeak = userText;
        // 优先使用 data 字段的内容
        if (userText && typeof userText === 'object' && userText.data && typeof userText.data === 'string') {
          textToSpeak = userText.data;
          console.log('[小说模式] ✓ 使用 data 字段内容，长度:', textToSpeak.length);
        } else if (userText && typeof userText === 'string') {
          console.log('[小说模式] ✓ getUser 返回成功，长度:', userText.length);
        }
        if (textToSpeak && typeof textToSpeak === 'string' && this.isNavigating && this.mode === 0) {
          this.addTextToSpeechQueue(textToSpeak);
        } else {
          console.warn('[小说模式] ⚠️ getUser 返回无效或状态异常');
        }
      } catch (error) {
        console.error('[小说模式] ✗ getUser 失败:', error);
        console.error('[小说模式] ✗ 错误详情:', error.message);
      }

      // 2. 调用 getArea
      try {
        if (this.userLocation) {
          console.log('[小说模式] 📞 调用 getArea...');
          const params = {
            longitude: this.userLocation.lng,
            latitude: this.userLocation.lat,
            userId: 1
          };
          // 只有当 appliedContent 有值时才传递 content 参数
          if (this.appliedContent) {
            params.content = this.appliedContent;
            console.log('[小说模式] 📝 添加 content 参数:', this.appliedContent);
          }
          
          console.log('[小说模式] 🔍 getArea 请求参数:', params);
          
          // 🔧 关键修复：小说模式的 getArea 只调用一次，不需要互斥控制，直接启用
          const areaResponse = await pollTask(
            () => getAreaAsync(params),
            undefined,
            2000,
            // 🔧 关键修复：传递中止检查函数，增加 mode 检查
            () => !this.isNavigating || this.isPaused || this.mode !== 0 || this.isPoiRequesting,
            // 🔧 关键新增：小说模式直接启用，不互斥
            { enabled: true }
          );
          
          console.log('[小说模式] 📨 getArea 返回:', areaResponse, '类型:', typeof areaResponse);
          
          // 🔧 关键修复：再次检查状态，确保仍在小说模式
          if (!this.isNavigating || this.mode !== 0) {
            console.warn('[小说模式] ⚠️ 请求返回后状态已变化，丢弃 getArea 结果');
            return;
          }
          
          // pollTask 已经处理了 Map 提取，这里直接检查是否为字符串
          if (areaResponse && typeof areaResponse === 'string') {
            console.log('[小说模式] ✓ getArea 返回字符串，长度:', areaResponse.length);
            console.log('[小说模式] 📝 内容预览:', areaResponse.substring(0, 50) + '...');
            this.addTextToSpeechQueue(areaResponse);
            console.log('[小说模式] ✅ getArea 内容已加入语音队列');
          } else if (areaResponse && typeof areaResponse === 'object') {
            // 兼容旧逻辑：如果还是对象，尝试提取
            console.log('[小说模式] 🔍 getArea 返回对象，尝试提取');
            const keys = Object.keys(areaResponse).filter(key => key !== '@type');
            console.log('[小说模式] 🔑 可用的 keys:', keys);
            if (keys.length > 0) {
              this.cacheKey = keys[0];
              const content = areaResponse[this.cacheKey];
              console.log('[小说模式] ✓ 从 Map 中获取内容, cacheKey:', this.cacheKey);
              console.log('[小说模式] 📝 内容类型:', typeof content, ', 长度:', typeof content === 'string' ? content.length : 'N/A');
              if (content && typeof content === 'string') {
                this.addTextToSpeechQueue(content);
                console.log('[小说模式] ✅ getArea 内容已加入语音队列');
              } else {
                console.warn('[小说模式] ⚠️ 提取的内容为空或类型错误');
              }
            } else {
              console.warn('[小说模式] ⚠️ 返回对象中没有有效的 key');
            }
          } else {
            console.warn('[小说模式] ⚠️ getArea 返回内容为空或类型未知:', areaResponse);
          }
        } else {
          console.warn('[小说模式] ⚠️ userLocation 为空，跳过 getArea 调用');
        }
      } catch (error) {
        console.error('[小说模式] ✗ getArea 失败:', error);
        console.error('[小说模式] ✗ 错误详情:', error.message);
        console.error('[小说模式] ✗ 错误堆栈:', error.stack);
        // 🔧 关键修复：不再静默吞掉错误，显示提示
        uni.showToast({ 
          title: `区域信息获取失败`, 
          icon: 'none',
          duration: 2000
        });
      }

      // 3. 调用 startNavigationWithPoll 获取 bookId 和 trajectoryId
      let bookId = null;
      let isValidBookId = false;
      try {
        console.log('[小说模式] 📞 调用 startNavigation API...');
        console.log('[小说模式] 📝 请求参数:', JSON.stringify(navigationData));
        // 小说模式调用 startNavigationWithPoll API
        bookId = await startNavigationWithPoll(navigationData, (status) => {
          console.log('[小说模式] 📊 任务状态:', status);
        }, 2000);
        console.log('[小说模式] ✓ startNavigation 返回成功');
        console.log('[小说模式] 📊 返回值:', bookId, '类型:', typeof bookId);
        
        // 处理不同的返回格式
        let actualBookId, actualTrajectoryId;
        if (typeof bookId === 'object' && bookId !== null) {
          // 如果返回的是对象 {bookId: xxx, trajectoryId: xxx}
          actualBookId = bookId.bookId;
          actualTrajectoryId = bookId.trajectoryId;
          console.log('[小说模式] 📋 解析对象格式: bookId=', actualBookId, ', trajectoryId=', actualTrajectoryId);
        } else if (typeof bookId === 'number') {
          // 兼容旧逻辑：如果返回的是数字（trajectoryId）
          actualBookId = null;  // bookId 未知
          actualTrajectoryId = bookId;  // trajectoryId
          console.warn('[小说模式] ⚠️ 返回数字格式（已废弃），trajectoryId=', bookId);
          console.warn('[小说模式] ⚠️ 无法获取 bookId，可能导致 getChapter 失败');
        } else {
          actualBookId = bookId;
          actualTrajectoryId = bookId;  // 🔧 修复：使用 bookId 作为 trajectoryId，而不是 0
          console.warn('[小说模式] ⚠️ 未知格式，假设返回值既是 bookId 也是 trajectoryId');
        }
        
        isValidBookId = actualBookId !== null && actualBookId !== undefined && actualBookId !== '';
        
        if (isValidBookId && this.isNavigating) {
          console.log('[小说模式] ✅ bookId 有效，准备启动链式章节获取');
          console.log('[小说模式] 📝 bookId:', actualBookId, ', trajectoryId:', actualTrajectoryId);
          
          // 🔧 关键优化：在这里直接启动链式调用，不要等到 try-catch 外面
          console.log('[小说模式] 🚀 开始链式调用初始化流程');
          
          // 保存 bookId 和 trajectoryId 供链式调用使用
          this.currentBookId = { bookId: actualBookId, trajectoryId: actualTrajectoryId };
          console.log('[小说模式] 💾 保存 currentBookId:', this.currentBookId);
          
          // 清除旧的定时器（防止重复创建）
          if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
            console.log('[小说模式] 🗑 清除旧定时器');
          }
          
          // 立即发起第一次 getChapter 请求，不等朗读完成
          console.log('[小说模式] ⚡ 立即发起第一次 getChapter 请求');
          console.log('[小说模式] 🔍 当前状态 - isNavigating:', this.isNavigating, ', isPaused:', this.isPaused, ', mode:', this.mode);
          
          // 使用 setTimeout 确保在下一个事件循环中执行，避免阻塞
          setTimeout(() => {
            console.log('[小说模式] 🔄 执行 fetchNextChapter');
            this.fetchNextChapter();
          }, 0);
          
          // 设置保底定时器，每 120 秒自动请求一次（防止链式调用中断）
          console.log('[小说模式] ⏰ 创建保底定时器，间隔: 120秒');
          this.timer = setInterval(() => {
            console.log('[小说模式] ⏰ 保底定时器触发');
            if (this.currentBookId && this.isNavigating && !this.isPaused && this.mode === 0) {
              console.log('[小说模式] ✅ 条件满足，调用 fetchNextChapter');
              this.fetchNextChapter();
            } else {
              console.log('[小说模式] ⚠️ 条件不满足，跳过本次请求');
              console.log('[小说模式] 🔍 currentBookId:', !!this.currentBookId, ', isNavigating:', this.isNavigating, ', isPaused:', this.isPaused, ', mode:', this.mode);
            }
          }, 120000);
          
          console.log('[小说模式] ✅ 链式调用初始化完成，timer ID:', this.timer);
        } else {
          console.error('[小说模式] ❌ bookId 无效或导航已停止');
          console.error('[小说模式] 📊 isValidBookId:', isValidBookId);
          console.error('[小说模式] 📊 isNavigating:', this.isNavigating);
          console.error('[小说模式] 📊 actualBookId:', actualBookId);
          console.error('[小说模式] 📊 actualTrajectoryId:', actualTrajectoryId);
        }
      } catch (error) {
        console.error('[小说模式] ✗ startNavigation 失败:', error);
        console.error('[小说模式] 错误详情:', error.message);
        uni.showToast({ 
          title: `导航启动失败: ${error.message || '未知错误'}`, 
          icon: 'none',
          duration: 3000
        });
      }
      
      this.navigationParams = navigationData;
    },

    /**
     * 🔧 获取下一个章节内容（异步轮询，返回后立即发起下一次请求）
     */
    async fetchNextChapter() {
      console.log('[小说模式] 🚀 fetchNextChapter 被调用');
      console.log('[小说模式] 🔍 状态检查:');
      console.log('[小说模式]   - currentBookId:', this.currentBookId);
      console.log('[小说模式]   - isNavigating:', this.isNavigating);
      console.log('[小说模式]   - isPaused:', this.isPaused);
      console.log('[小说模式]   - mode:', this.mode);
      
      try {
        if (!this.currentBookId) {
          console.log('[小说模式] ⚠️ 条件不满足：currentBookId 为空');
          return;
        }
        if (!this.isNavigating) {
          console.log('[小说模式] ⚠️ 条件不满足：isNavigating 为 false');
          return;
        }
        if (this.isPaused) {
          console.log('[小说模式] ⚠️ 条件不满足：isPaused 为 true');
          return;
        }
        if (this.mode !== 0) {
          console.log('[小说模式] ⚠️ 条件不满足：mode 不是 0 (当前:', this.mode, ')');
          return;
        }
        
        console.log('[小说模式] ✅ 所有条件满足，开始请求 getChapter');
        console.log('[小说模式] 📝 请求参数:', {
          bookId: this.currentBookId.bookId,
          trajectoryId: this.currentBookId.trajectoryId,
          length: 3000
        });
        
        // 生成唯一的任务ID
        const taskId = `novel_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
        
        // 使用 pollingManager 进行异步轮询请求（等待task接口返回）
        const updatedChapterText = await pollingManager.startPolling(
          taskId,
          (taskId) => getChapter({ 
            bookId: this.currentBookId.bookId, 
            trajectoryId: this.currentBookId.trajectoryId, 
            length: 3000 
          }),
          2000,
          () => !this.isNavigating || this.isPaused || this.mode !== 0
        );
        
        console.log('[小说模式] 📖 getChapter 返回:', updatedChapterText);
        console.log('[小说模式] 📊 返回类型:', typeof updatedChapterText);
        
        if (updatedChapterText && typeof updatedChapterText === 'string' && this.isNavigating && !this.isPaused && this.mode === 0) {
          if (updatedChapterText === '结束') {
            console.log('[小说模式] 🏁 小说已结束，清除定时器');
            clearInterval(this.timer);
            this.timer = null;
            uni.showToast({ title: '本小说已经结束', icon: 'none', duration: 3000 });
          } else {
            console.log('[小说模式] ✓ 添加章节内容到语音队列，长度:', updatedChapterText.length);
            this.addTextToSpeechQueue(updatedChapterText);
            
            // 🔧 关键优化：加入队列后立即发起下一次请求，不等朗读完成
            console.log('[小说模式] ⚡ 文本已入队，立即发起下一次 getChapter 请求');
            setTimeout(() => {
              this.fetchNextChapter();
            }, 100); // 延迟 100ms，避免过于频繁的请求
          }
        } else {
          console.warn('[小说模式] ⚠️ 返回内容无效或状态异常');
          console.warn('[小说模式] 📊 updatedChapterText:', updatedChapterText);
          console.warn('[小说模式] 📊 isNavigating:', this.isNavigating);
          console.warn('[小说模式] 📊 isPaused:', this.isPaused);
          console.warn('[小说模式] 📊 mode:', this.mode);
        }
      } catch (err) {
        console.error('[小说模式] ❌ getChapter 调用失败:', err);
        console.error('[小说模式] ❌ 错误详情:', err.message);
        console.error('[小说模式] ❌ 错误堆栈:', err.stack);
      }
    },

    /**
     * 获取书籍 ID（不带书籍信息）
     */
    getBookIdNoBook(navigationData) {
      return getBookIdNoBook(navigationData, this);
    },

    /**
     * 开始 POI 轮询请求（增强版 - 防止竞态条件）
     */
    startPoiPolling(lng, lat) {
      console.log('[POI 启动] 🚀 开始 POI 轮询');
      console.log('[POI 启动] 🔍 当前状态 - isNavigating:', this.isNavigating, ', mode:', this.mode);
          
      // 🔧 关键修复：先强制停止所有后端访问和定时器
      console.log('[POI 启动] 🛑 第一步：停止所有现有请求');
      this.stopAllRequests();
      
      // 🔧 关键修复：等待一小段时间，确保所有异步操作完成
      setTimeout(() => {
        this._startPoiPollingInternal(lng, lat);
      }, 50);
    },
    
    /**
     * POI 轮询内部实现（延迟执行，避免竞态条件）
     */
    _startPoiPollingInternal(lng, lat) {
      console.log('[POI 启动] 🔄 第二步：开始 POI 轮询内部流程');
              
      // 🔧 关键修复：再次检查状态，确保仍在导航中且是文化导览模式
      if (!this.isNavigating || this.mode !== 1) {
        console.warn('[POI 启动] ⚠️ 状态异常，中止 POI 启动');
        return;
      }
              
      // 🔧 关键修复：强制清空所有语音队列（文本数组和声音数组）
      console.log('[POI 启动] 🗑 第三步：强制清空所有语音队列');
      console.log('[POI 启动] 📊 清空前 - speechTextQueue:', this.speechTextQueue.length);
      console.log('[POI 启动] 📊 清空前 - ttsConversionQueue:', this.ttsConversionQueue.length);
      console.log('[POI 启动] 📊 清空前 - ttsAudioQueue:', this.ttsAudioQueue.length);
              
      // 停止当前音频播放
      if (this.audioContext) {
        try {
          console.log('[POI 启动] ⏹ 停止当前正在播放的音频');
          this.audioContext.stop();
        } catch (e) {
          console.warn('[POI 启动] 停止音频失败', e);
        }
        try {
          console.log('[POI 启动] 🗑 销毁音频上下文');
          this.audioContext.destroy();
        } catch (e) {
          console.warn('[POI 启动] 销毁音频失败', e);
        }
        this.audioContext = null;
      }
              
      // 清空所有队列
      this.speechTextQueue = [];
          
      // 🔧 关键修复：标记所有待转换的任务为已取消
      this.ttsConversionQueue.forEach(task => {
        if (task.status === 'pending' || task.status === 'converting') {
          task.status = 'cancelled';
          console.log('[POI 启动] 🚫 取消任务:', task.text.substring(0, 30) + '...');
        }
      });
      this.ttsConversionQueue = [];
          
      // 🔧 关键修复：标记所有待播放的音频为已取消
      this.ttsAudioQueue.forEach(audio => {
        audio.cancelled = true;
      });
      this.ttsAudioQueue = [];
          
      this.currentSpeakingText = null;
      this.isTTSPlaying = false;
      this.isAudioPlaying = false;
      this.pendingClearQueue = false;
      this.isTTSConverting = false; // 🔧 关键：停止正在进行的 TTS 转换
              
      console.log('[POI 启动] ✅ 第四步：已清空所有队列');
      console.log('[POI 启动] 📊 清空后 - isAudioPlaying:', this.isAudioPlaying);
      console.log('[POI 启动] 📊 清空后 - isTTSConverting:', this.isTTSConverting);
                  
      // 在启动 getPoi 之前，保存当前最新的 getArea 的 cacheKey
      // 这样点击“文化导览点回来”时，可以使用这个 cacheKey 恢复 getArea 请求
      this.savedCacheKey = this.cacheKey;
      console.log('[POI 启动] 💾 第五步：保存 getArea 的 cacheKey:', this.savedCacheKey);
                
      // 🔧 关键修复：互斥控制 - 关闭文化导览轮询，开启 POI 轮询
      this.culturalPollControl.enabled = false;
      this.poiPollControl.enabled = true;
      console.log('[POI 启动] 🚫 culturalPollControl.enabled:', this.culturalPollControl.enabled);
      console.log('[POI 启动] ✅ poiPollControl.enabled:', this.poiPollControl.enabled);
                        
      this.isPoiRequesting = true;
      this.cacheKey = null; // 重置 cacheKey，让 getPoi 从头开始
      this.hasClearedSpeech = false; // 标记是否已清空过语音
              
      console.log('[POI 启动] ✅ 第六步：设置 POI 轮询状态');
                  
      // 🔧 关键修复：直接调用 requestPoiInfo，不再使用 setInterval
      // requestPoiInfo 内部会使用 pollTask 进行完整轮询，完成后如果需要继续，会再次调用自己
      console.log('[POI 启动] 🚀 第七步：发起第一次 POI 请求');
      this.requestPoiInfo(lng, lat);
              
      console.log('[POI 启动] ✅ POI 轮询已成功启动');
    },

    /**
     * 请求 POI 信息（增强版 - 防止竞态条件）
     */
    async requestPoiInfo(lng, lat) {
      // 🔧 关键修复：在每次调用前检查状态，防止竞态条件
      if (!this.isPoiRequesting || !this.isNavigating || this.mode !== 1) {
        console.log('[POI] ⚠️ 状态不满足，中止 requestPoiInfo');
        return;
      }
    
      try {
        // 🔧 关键修复：如果已经有正在运行的 POI 轮询任务，先取消它
        if (this.currentPoiTask) {
          console.log('[POI] 🚫 已有轮询任务正在运行，先取消');
          try {
            this.currentPoiTask.abort();
          } catch (e) {
            console.warn('[POI] ⚠️ 取消轮询任务失败:', e);
          }
          this.currentPoiTask = null;
        }
        
        console.log('[POI] 🔄 开始请求 POI 信息');
        console.log('[POI] 📍 位置:', { lng, lat });
        console.log('[POI] 🔑 cacheKey:', this.cacheKey);
        
        // 发送请求，第一次请求（cacheKey 为 null）时静默处理错误
        const silentError = !this.cacheKey;
        
        // 构建请求参数
        const params = {
          longitude: lng,
          latitude: lat,
          userId: 1,
          cacheKey: this.cacheKey
        };
        // 只有当 appliedContent 有值时才传递 content 参数
        if (this.appliedContent) {
          params.content = this.appliedContent;
        }
        
        // 使用 pollTask 进行轮询请求，明确指定 getTaskResult 作为轮询请求函数
        console.log('[POI] 🚀 准备调用 pollTask');
        console.log('[POI] 🔍 isPoiRequesting:', this.isPoiRequesting);
        
        const pollPromise = pollTask(
          () => getPoiAsync(params),
          getTaskResult,
          2000,
          // 🔧 关键修复：传递中止检查函数，当退出 POI 模式时立即中止
          () => !this.isPoiRequesting || !this.isNavigating || this.isPaused || this.mode !== 1
        );
        
        // 保存当前轮询任务
        this.currentPoiTask = pollPromise;
        
        const response = await pollPromise;
    
        // 🔧 关键修复：再次检查状态，确保仍在 POI 模式中
        if (!this.isPoiRequesting || !this.isNavigating || this.mode !== 1) {
          console.log('[POI] ⚠️ 请求返回后状态已变化，丢弃结果');
          return;
        }
    
        console.log('[POI] ✅ 收到响应:', response);
        console.log('[POI] 📊 响应类型:', typeof response);
        
        // 处理返回值（map 类型）
        if (response && typeof response === 'object') {
          // 优先使用 data 字段的内容
          if (response.data && typeof response.data === 'string') {
            console.log('[POI] ✓ 使用 data 字段内容，长度:', response.data.length);
            this.addTextToSpeechQueue(response.data);
          } else {
            const keys = Object.keys(response).filter(key => key !== '@type');
            console.log('[POI] 🔑 可用的 keys:', keys);
            
            if (keys.length > 0) {
              // 获取返回的 key
              const returnedKey = keys[0];
                  
              // 🔧 已移除：不再在这里清空队列，因为 startPoiPolling 已经清空过了
              // if (!this.hasClearedSpeech) {
              //   console.log('[POI] 🗑 首次请求，清空语音队列');
              //   this.clearAllSpeechContent();
              //   this.hasClearedSpeech = true;
              // }
              
              // 更新 cacheKey 为返回的 key，用于下一次请求
              this.cacheKey = returnedKey;
              console.log('[POI] 💾 更新 cacheKey:', this.cacheKey);
                  
              // 获取对应的 value 内容
              const content = response[returnedKey];
              console.log('[POI] 📝 内容类型:', typeof content);
              console.log('[POI] 📏 内容长度:', typeof content === 'string' ? content.length : 'N/A');
                  
              // 如果内容不为空，添加到统一语音队列
              if (content && typeof content === 'string' && content.trim()) {
                console.log('[POI] ✅ 添加内容到语音队列');
                this.addTextToSpeechQueue(content);
              } else {
                console.warn('[POI] ⚠️ 内容为空或类型错误:', content);
              }
            } else {
              console.warn('[POI] ⚠️ 响应中没有有效的 key');
            }
          }
        } else if (response && typeof response === 'string') {
          // 兼容直接返回字符串的情况
          console.log('[POI] 📝 直接返回字符串，长度:', response.length);
          if (response.trim()) {
            this.addTextToSpeechQueue(response);
          }
        } else {
          console.warn('[POI] ⚠️ 响应格式未知:', response);
        }
        
        // 🔧 关键修复：pollTask 已经完成一次完整的轮询，如果需要持续获取，再次调用
        // 这里使用 setTimeout 延迟后再次调用 requestPoiInfo，形成循环
        if (this.isPoiRequesting && this.isNavigating && !this.isPaused && this.mode === 1) {
          setTimeout(() => {
            // 🔧 关键修复：延迟后再次检查状态
            if (this.isPoiRequesting && this.isNavigating && !this.isPaused && this.mode === 1) {
              this.requestPoiInfo(lng, lat);
            }
          }, 5000); // 5秒后再次请求
        }
      } catch (error) {
        console.error('[POI] ❌ 请求失败:', error);
        console.error('[POI] ❌ 错误详情:', error.message);
        
        // 🔧 关键修复：出错时也尝试重试
        if (this.isPoiRequesting && this.isNavigating && !this.isPaused && this.mode === 1) {
          setTimeout(() => { 
            // 🔧 关键修复：延迟后再次检查状态
            if (this.isPoiRequesting && this.isNavigating && !this.isPaused && this.mode === 1) {
              this.requestPoiInfo(lng, lat); 
            }
          }, 5000); // 延长重试间隔到 5 秒
        }
      }
    },

    /**
     * 停止所有后端请求（增强版）
     */
    stopAllRequests() {
      console.log('[停止请求] 🛑 开始停止所有请求');
      
      // 清除所有定时器
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
        console.log('[停止请求] ✓ 已清除小说章节定时器');
      }
      if (this.culturalTimer) {
        clearInterval(this.culturalTimer);
        this.culturalTimer = null;
        this.culturalTimestamps = [];
        console.log('[停止请求] ✓ 已清除文化导览定时器');
      }
      if (this.poiRequestTimer) {
        clearInterval(this.poiRequestTimer);
        this.poiRequestTimer = null;
        console.log('[停止请求] ✓ 已清除 POI 轮询定时器');
      }
      if (this.speechTimer) {
        clearTimeout(this.speechTimer);
        this.speechTimer = null;
        console.log('[停止请求] ✓ 已清除语音定时器');
      }
      
      // #ifdef MP-WEIXIN
      // 微信小程序：停止持续定位更新
      this.stopPositionUpdate();
      // #endif
      
      // 🔧 关键修复：取消所有进行中的请求（包括 requestTasks 和可能的其他请求）
      if (this.requestTasks && this.requestTasks.length > 0) {
        console.log('[停止请求] 🗑 取消', this.requestTasks.length, '个进行中的请求');
        this.requestTasks.forEach(task => {
          if (task && typeof task.abort === 'function') {
            try {
              task.abort();
              console.log('[停止请求] ✓ 已取消一个请求');
            } catch (e) {
              console.warn('[停止请求] ⚠️ 取消请求失败:', e.message);
            }
          }
        });
        this.requestTasks = [];
      }
      
      // 🔧 关键修复：重置所有状态标志，防止竞态条件
      this.isPoiRequesting = false;
      this.isTTSConverting = false;
      this.isAudioPlaying = false;
      this.pendingClearQueue = false;
      
      console.log('[停止请求] ✅ 所有请求已停止');
    },

    /**
     * 停止语音播放
     */
    stopSpeech() {
      console.log('[TTS] 🛑 停止语音播放');
      
      // #ifdef H5
      if (window.speechSynthesis) {
        window.speechSynthesis.cancel();
      }
      // #endif
      
      // #ifdef MP-WEIXIN
      // 🔧 关键修复：先停止并销毁当前正在播放的音频
      if (this.audioContext) {
        try {
          console.log('[TTS] ⏹ 停止当前音频');
          this.audioContext.stop();
        } catch (e) {
          console.warn('[TTS] 停止音频失败', e);
        }
        try {
          console.log('[TTS] 🗑 销毁音频上下文');
          this.audioContext.destroy();
        } catch (e) {
          console.warn('[TTS] 销毁音频上下文失败', e);
        }
        this.audioContext = null;
      }
      // #endif
      
      if (this.speechTimer) {
        clearTimeout(this.speechTimer);
        this.speechTimer = null;
      }
      
      // 清空语音队列
      this.speechTextQueue = [];
      this.currentSpeakingText = null;
      this.currentSpeechIndex = 0;
      this.isTTSPlaying = false; // 🔓 重置播放锁
      
      // #ifdef MP-WEIXIN
      // 🔧 关键修复：同时清空 TTS 预加载队列
      console.log('[TTS] 🗑 清空前 - 转换队列长度:', this.ttsConversionQueue.length);
      console.log('[TTS] 🗑 清空前 - 播放队列长度:', this.ttsAudioQueue.length);
      
      this.ttsConversionQueue = [];
      this.ttsAudioQueue = [];
      this.isAudioPlaying = false;
      this.pendingClearQueue = false;
      
      console.log('[TTS] 🗑 已清空 TTS 预加载队列');
      console.log('[TTS] 🗑 清空后 - isAudioPlaying:', this.isAudioPlaying);
      // #endif
      
      console.log('[TTS] ✅ 停止完成');
    },

    /**
     * 停止 POI 轮询
     */
    stopPoiPolling() {
      this.isPoiRequesting = false;
      if (this.poiRequestTimer) {
        clearInterval(this.poiRequestTimer);
        this.poiRequestTimer = null;
      }
    },

    /**
     * 处理文化导览点回来按钮点击（增强版 - 防止竞态条件）
     */
    /**
     * 处理“文化导览点回来”按钮点击
     * 从 POI 模式切换回 getArea 模式
     */
    handleBackToArea() {
      // 🔧 关键修复：防止重复调用（请求独一性保证）
      if (this.isProcessing) {
        console.warn('[返回区域] ⚠️ 正在处理中，忽略本次点击');
        uni.showToast({ title: '正在处理中，请稍候', icon: 'none', duration: 2000 });
        return;
      }
          
      if (!this.isNavigating || this.mode !== 1) {
        console.warn('[返回区域] ⚠️ 状态不满足，无法返回区域模式');
        return;
      }
          
      if (!this.showBackButton) {
        console.warn('[返回区域] ⚠️ 返回按钮未显示，无需操作');
        return;
      }
          
      console.log('[返回区域] 🔄 开始切换回文化导览模式');
      console.log('[返回区域] 🔍 当前状态 - isNavigating:', this.isNavigating, ', mode:', this.mode);
          
      // 🔧 关键修复：先停止 POI 轮询和所有请求
      console.log('[返回区域] 🛑 第一步：停止 POI 轮询');
      this.stopPoiPolling();
      this.stopAllRequests();
      
      // 🔧 关键修复：等待一小段时间，确保所有异步操作完成
      setTimeout(() => {
        this._handleBackToAreaInternal();
      }, 50);
    },
    
    /**
     * 返回区域内部实现（延迟执行，避免竞态条件）
     */
    _handleBackToAreaInternal() {
      console.log('[返回区域] 🔄 第二步：执行返回区域内部流程');
      
      // 🔧 关键修复：再次检查状态，确保仍在导航中且是文化导览模式
      if (!this.isNavigating || this.mode !== 1) {
        console.warn('[返回区域] ⚠️ 状态异常，中止返回操作');
        return;
      }
      
      // 🔧 关键修复：强制清空所有语音队列（与 startPoiPolling 保持一致）
      console.log('[返回区域] 🗑 第三步：强制清空所有语音队列');
      console.log('[返回区域] 📊 清空前 - speechTextQueue:', this.speechTextQueue.length);
      console.log('[返回区域] 📊 清空前 - ttsConversionQueue:', this.ttsConversionQueue.length);
      console.log('[返回区域] 📊 清空前 - ttsAudioQueue:', this.ttsAudioQueue.length);
      
      // 停止当前音频播放
      if (this.audioContext) {
        try {
          console.log('[返回区域] ⏹ 停止当前正在播放的音频');
          this.audioContext.stop();
        } catch (e) {
          console.warn('[返回区域] 停止音频失败', e);
        }
        try {
          console.log('[返回区域] 🗑 销毁音频上下文');
          this.audioContext.destroy();
        } catch (e) {
          console.warn('[返回区域] 销毁音频失败', e);
        }
        this.audioContext = null;
      }
      
      // 清空所有队列
      this.speechTextQueue = [];
      
      // 标记所有待转换的任务为已取消
      this.ttsConversionQueue.forEach(task => {
        if (task.status === 'pending' || task.status === 'converting') {
          task.status = 'cancelled';
          console.log('[返回区域] 🚫 取消任务:', task.text.substring(0, 30) + '...');
        }
      });
      this.ttsConversionQueue = [];
      
      // 标记所有待播放的音频为已取消
      this.ttsAudioQueue.forEach(audio => {
        audio.cancelled = true;
      });
      this.ttsAudioQueue = [];
      
      this.currentSpeakingText = null;
      this.isTTSPlaying = false;
      this.isAudioPlaying = false;
      this.pendingClearQueue = false;
      this.isTTSConverting = false;
      
      console.log('[返回区域] ✅ 第四步：已清空所有队列');
      console.log('[返回区域] 📊 清空后 - isAudioPlaying:', this.isAudioPlaying);
      console.log('[返回区域] 📊 清空后 - isTTSConverting:', this.isTTSConverting);
      
      // 恢复之前保存的 cacheKey
      if (this.savedCacheKey) {
        this.cacheKey = this.savedCacheKey;
        console.log('[返回区域] 💾 第五步：恢复 cacheKey:', this.cacheKey);
      } else {
        // 如果没有保存的 cacheKey，重置为 null，让 getArea 从头开始
        this.cacheKey = null;
        console.log('[返回区域] 💾 第五步：没有保存的 cacheKey，重置为 null');
      }
      
      // 🔧 关键修复：互斥控制 - 关闭 POI 轮询，开启文化导览轮询
      this.poiPollControl.enabled = false;
      this.culturalPollControl.enabled = true;
      console.log('[返回区域] 🚫 poiPollControl.enabled:', this.poiPollControl.enabled);
      console.log('[返回区域] ✅ culturalPollControl.enabled:', this.culturalPollControl.enabled);
      
      // 隐藏返回按钮
      this.showBackButton = false;
      console.log('[返回区域] 👁 第六步：隐藏返回按钮');
      
      // 重新开始调用 fetchCulturalInfo（使用 getArea 接口）
      if (this.isNavigating && !this.isPaused && this.mode === 1) {
        console.log('[返回区域] 🚀 第七步：重新启动文化导览轮询（仅 getArea）');
        this.fetchCulturalInfo();
        uni.showToast({
          title: '已切换回区域导览模式',
          icon: 'success',
          duration: 2000
        });
      } else {
        console.warn('[返回区域] ⚠️ 状态不满足，无法重启文化导览');
      }
      
      console.log('[返回区域] ✅ 返回区域操作已完成');
    },
    
    /**
     * 开始持续定位更新（导航过程中）
     */
    startPositionUpdate() {
      // #ifdef MP-WEIXIN
      // 清除旧的定时器
      if (this.locationUpdateTimer) {
        clearInterval(this.locationUpdateTimer);
      }
      
      console.log('[持续定位] 开始每5秒更新一次位置');
      
      // 立即执行一次
      this.updateUserLocation();
      
      // 设置定时器，每5秒更新一次
      this.locationUpdateTimer = setInterval(() => {
        if (this.isNavigating && !this.isPaused) {
          this.updateUserLocation();
        } else {
          this.stopPositionUpdate();
        }
      }, this.locationUpdateInterval);
      // #endif
    },
    
    /**
     * 停止持续定位更新
     */
    stopPositionUpdate() {
      if (this.locationUpdateTimer) {
        clearInterval(this.locationUpdateTimer);
        this.locationUpdateTimer = null;
        console.log('[持续定位] 已停止');
      }
    },
    
    /**
     * 更新用户位置（微信小程序）
     */
    updateUserLocation() {
      // #ifdef MP-WEIXIN
      uni.getLocation({
        type: 'gcj02',
        altitude: true,
        isHighAccuracy: true,
        highAccuracyExpireTime: 10000,
        success: (res) => {
          console.log('[持续定位] 原始数据:', {
            longitude: res.longitude,
            latitude: res.latitude,
            accuracy: res.accuracy,
            type: typeof res.longitude
          });
          
          // 标准化位置数据
          const newLocation = normalizeLocation({
            lng: res.longitude,
            lat: res.latitude
          });
          
          if (!newLocation) {
            console.error('[持续定位] 数据格式化失败');
            return;
          }
          
          // 只在位置有明显变化时才更新（避免频繁渲染）
          if (this.userLocation) {
            const distance = Math.sqrt(
              Math.pow(newLocation.lng - this.userLocation.lng, 2) +
              Math.pow(newLocation.lat - this.userLocation.lat, 2)
            );
            
            // 如果移动距离小于 0.0001 度（约10米），不更新
            if (distance < 0.0001) {
              console.log('[持续定位] 移动距离过小，跳过更新');
              return;
            }
            
            console.log('[持续定位] 移动距离:', (distance * 111000).toFixed(2), '米');
          }
          
          console.log('[持续定位] ✓ 位置更新:', newLocation);
          this.userLocation = newLocation;
          this.mapCenter = newLocation;
          this.updateUserMarker(newLocation);
        },
        fail: (error) => {
          console.warn('[持续定位] ✗ 更新失败:', error);
        }
      });
      // #endif
    },
    
    /**
     * 微信小程序地图点击事件
     */
    onMapTap(e) {
      // #ifdef MP-WEIXIN
      const { latitude, longitude } = e.detail;
      
      if (this.isSelectingPoint) {
        // 选择中心点模式
        this.selectedPointLocation = { lng: longitude, lat: latitude };
        this.isSelectingPoint = false;
        this.showBackButton = true;
        
        // 更新标记
        this.mapMarkers = this.mapMarkers.filter(m => m.id !== 'center_point');
        this.mapMarkers.push({
          id: 'center_point',
          latitude: latitude,
          longitude: longitude,
          iconPath: '/static/images/center-marker.png',
          width: 30,
          height: 30,
          callout: {
            content: '中心点',
            color: '#FFFFFF',
            fontSize: 12,
            borderRadius: 4,
            bgColor: '#1677ff',
            padding: 5,
            display: 'ALWAYS'
          }
        });
        
        uni.showToast({
          title: `已设置中心点：[${longitude.toFixed(4)}, ${latitude.toFixed(4)}]`,
          icon: 'success',
          duration: 2000
        });
        
        // 开始循环请求 POI（startPoiPolling 内部会先停止之前的轮询）
        this.startPoiPolling(longitude, latitude);
      } else {
        // 普通点击，选择目的地
        this.atPoi({
          id: `click_${Date.now()}`,
          name: '点击位置',
          location: `${longitude},${latitude}`
        });
        
        uni.showToast({
          title: `已选择：[${longitude.toFixed(4)}, ${latitude.toFixed(4)}]`,
          icon: 'none',
          duration: 2000
        });
      }
      // #endif
    },
    
    /**
     * 微信小程序标记点点击事件
     */
    onMarkerTap(e) {
      // #ifdef MP-WEIXIN
      const markerId = e.detail.markerId;
      console.log('点击标记点:', markerId);
      // 可以根据不同的标记点 ID 执行不同操作
      // #endif
    },
    
    /**
     * 调整地图视野以包含所有标记点
     */
    adjustMapViewport() {
      // #ifdef MP-WEIXIN
      if (this.mapMarkers.length === 0) return;
      
      // 计算所有标记点的边界
      let minLat = Infinity, maxLat = -Infinity;
      let minLng = Infinity, maxLng = -Infinity;
      
      this.mapMarkers.forEach(marker => {
        if (marker.latitude < minLat) minLat = marker.latitude;
        if (marker.latitude > maxLat) maxLat = marker.latitude;
        if (marker.longitude < minLng) minLng = marker.longitude;
        if (marker.longitude > maxLng) maxLng = marker.longitude;
      });
      
      // 设置地图中心点为边界中心
      this.mapCenter = {
        lng: (minLng + maxLng) / 2,
        lat: (minLat + maxLat) / 2
      };
      
      // 根据边界范围自动调整缩放级别
      const latDiff = maxLat - minLat;
      const lngDiff = maxLng - minLng;
      const maxDiff = Math.max(latDiff, lngDiff);
      
      // 根据距离差值计算合适的缩放级别
      if (maxDiff > 1) {
        this.mapScale = 10;
      } else if (maxDiff > 0.5) {
        this.mapScale = 12;
      } else if (maxDiff > 0.1) {
        this.mapScale = 14;
      } else if (maxDiff > 0.05) {
        this.mapScale = 15;
      } else {
        this.mapScale = 16;
      }
      // #endif
    },
  },

  mounted() {
    this.$nextTick(() => {
      setTimeout(() => { this.initMap(); }, 200);
    });
  },

  onShow() {
    console.log('[页面显示] 📱 页面重新显示');
    console.log('[页面显示] 🔍 isNavigating:', this.isNavigating);
    console.log('[页面显示] 🔍 isPaused:', this.isPaused);
    console.log('[页面显示] 🔍 isAudioPlaying:', this.isAudioPlaying);
    console.log('[页面显示] 🔍 ttsAudioQueue 长度:', this.ttsAudioQueue.length);
    
    // 页面显示时，检查是否有继续听的书籍
    const continueListenData = uni.getStorageSync('continueListenBook');
    if (continueListenData) {
      this.continueListenBookId = continueListenData.id;
      console.log('继续听书籍已设置:', continueListenData);
    }
    
    // 从 Vuex store 中获取文化导览 content
    console.log('[Index] 🔍 开始从 Store 读取 culturalContent...');
    console.log('[Index] 🔍 Store state:', this.$store.state);
    console.log('[Index] 🔍 cultural module:', this.$store.state.cultural);
    
    const culturalContent = this.$store.state.cultural.culturalContent;
    console.log('[Index] 🔍 culturalContent 值:', culturalContent);
    console.log('[Index] 🔍 culturalContent 类型:', typeof culturalContent);
    
    if (culturalContent) {
      this.appliedContent = culturalContent;
      console.log('[Index] ✅ 文化导览 content 已从 Store 应用:', culturalContent);
      console.log('[Index] ✅ appliedContent 已设置为:', this.appliedContent);
    } else {
      console.log('[Index] ⚠️ culturalContent 为空，appliedContent 保持为:', this.appliedContent);
    }
    
    // #ifdef MP-WEIXIN
    // 微信小程序：页面从后台恢复时，如果正在导航且未暂停，尝试恢复音频播放
    if (this.isNavigating && !this.isPaused) {
      console.log('[页面显示] 🔄 检测到导航进行中，尝试恢复音频播放');
      
      // 如果当前没有正在播放的音频，且有待播放的队列，触发播放
      if (!this.isAudioPlaying && this.ttsAudioQueue.length > 0) {
        console.log('[页面显示] ▶️ 发现待播放音频，立即触发播放');
        this.processAudioPlaybackQueue();
      } else if (this.isAudioPlaying) {
        console.log('[页面显示] ℹ️ 音频正在播放中，无需恢复');
      } else {
        console.log('[页面显示] ⚠️ 没有待播放的音频');
      }
      
      // 如果转换队列为空但有待处理的文本，可能需要重新触发
      if (this.ttsConversionQueue.length === 0 && this.speechTextQueue.length > 0) {
        console.log('[页面显示] ⚠️ 转换队列为空但有文本队列，尝试处理');
        this.processSpeechQueue();
      }
    }
    // #endif
  },

  beforeDestroy() {
    console.log('[页面销毁] 🗑 开始清理所有资源');
    
    // 停止所有请求和定时器
    this.stopAllRequests();
    this.stopPoiPolling();
    
    // 清空所有语音队列
    this.clearAllSpeechContent();
    
    // #ifdef MP-WEIXIN
    // 微信小程序：停止持续定位更新
    this.stopPositionUpdate();
    
    // 清理音频上下文
    if (this.audioContext) {
      try {
        console.log('[页面销毁] ⏹ 停止音频播放');
        this.audioContext.stop();
      } catch (e) {
        console.warn('[页面销毁] 停止音频失败', e);
      }
      try {
        console.log('[页面销毁] 🗑 销毁音频上下文');
        this.audioContext.destroy();
      } catch (e) {
        console.warn('[页面销毁] 销毁音频上下文失败', e);
      }
      this.audioContext = null;
    }
    // #endif
    
    // #ifdef H5
    // H5：销毁地图实例
    if (this.map) {
      console.log('[页面销毁] 🗑 销毁地图实例');
      this.map.destroy();
      this.map = null;
    }
    // #endif
    
    // 重置所有状态
    this.isNavigating = false;
    this.isPaused = false;
    this.isProcessing = false;
    this.isPoiRequesting = false;
    this.isTTSConverting = false;
    this.isAudioPlaying = false;
    
    console.log('[页面销毁] ✅ 所有资源已清理完成');
  }
};
</script>

<style lang="scss" scoped>
.container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color-primary);
  color: var(--text-color-primary);
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  /* iPhone 刘海屏顶部安全区域适配 - 100rpx = 50px, 覆盖所有刘海屏设备 */
  padding-top: 100rpx;
}

.page {
  flex: 1;
  padding-bottom: 100rpx;
  overflow-y: auto;
  /* iPhone 刘海屏顶部安全区域适配（如果 .container 不生效时使用） */
  padding-top: constant(safe-area-inset-top);
  padding-top: env(safe-area-inset-top);
}

/* 搜索栏 */
.search-container {
  padding: 20rpx;
  background: var(--gradient-bg-light);
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
  position: relative;
  border-radius: 0 0 30rpx 30rpx;
}

.search-box {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--bg-color-tertiary) 0%, var(--border-color-medium) 100%);
  border-radius: 50rpx;
  padding: 16rpx 24rpx;
}

.search-icon {
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-color-primary);
}

.clear-btn {
  background: none;
  border: none;
  padding: 0;
  margin-left: 10rpx;
  color: var(--text-color-placeholder);
  font-size: 32rpx;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 20rpx;
  right: 20rpx;
  background: var(--bg-color-secondary);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-medium);
  margin-top: 8rpx;
  max-height: 600rpx;
  overflow-y: auto;
  z-index: 100;
}

.search-result-item {
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid var(--border-color-light);
  display: flex;
  align-items: flex-start;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item.empty {
  text-align: center;
  color: var(--text-color-placeholder);
}

.result-icon {
  margin-right: 16rpx;
  margin-top: 4rpx;
}

.result-content {
  flex: 1;
}

.result-name {
  font-size: 30rpx;
  font-weight: 500;
  display: block;
}

.result-address {
  font-size: 24rpx;
  color: var(--text-color-secondary);
  margin-top: 8rpx;
  display: block;
}

/* 地图 */
.map-container {
  height: 44.44vh;
  margin: 20rpx 20rpx;
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8rpx 32rpx var(--shadow-color-medium);
  background: var(--bg-color-tertiary);
}

.amap-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.wxmap-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

.map-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #94a3b8;
  font-size: 36rpx;
  z-index: 1;
}

.map-zoom-controls {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  z-index: 10;
}

.zoom-btn {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background-color: var(--bg-color-secondary);
  border: 1rpx solid var(--border-color-light);
  font-size: 36rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx var(--shadow-color-light);
  color: var(--text-color-primary);
}

.zoom-in {
  color: #1677ff;
}

.zoom-out {
  color: #666;
}

/* 位置信息 */
.location-info {
  margin: 0 20rpx 20rpx;
  background: var(--gradient-card-light);
  border-radius: 24rpx;
  padding: 16rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
}

.location-content {
  flex: 1;
}

.location-header {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.location-icon {
  margin-right: 12rpx;
}

.location-title {
  font-size: 30rpx;
  font-weight: 500;
}

.location-address {
  font-size: 24rpx;
  color: var(--text-color-secondary);
  margin-top: 10rpx;
}

.refresh-btn {
  background-color: var(--bg-color-secondary);
  color: var(--theme-blue);
  border: 1rpx solid var(--theme-blue);
  border-radius: 40rpx;
  padding: 16rpx 32rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

/* 设置列表 */
.settings-list-item {
  margin: 0 20rpx 20rpx;
  background: var(--gradient-card-light);
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
}

.settings-list-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.settings-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-list-left {
  display: flex;
  align-items: center;
}

.settings-list-left uni-icons {
  margin-right: 16rpx;
}

.settings-list-title {
  font-size: 30rpx;
  font-weight: 500;
  color: var(--text-color-primary);
}

/* 设置面板 */
.novel-settings-panel {
  margin-top: 20rpx;
  background: linear-gradient(135deg, var(--bg-color-tertiary) 0%, var(--bg-color-primary) 100%);
  border-radius: 24rpx;
  padding: 20rpx;
}

.setting-row {
  margin-bottom: 20rpx;
}

.setting-row:last-child {
  margin-bottom: 0;
}

.setting-label {
  font-size: 26rpx;
  color: var(--text-color-tertiary);
  margin-bottom: 12rpx;
  display: block;
  font-weight: 500;
}

.setting-value {
  font-size: 24rpx;
  color: var(--text-color-secondary);
}

.select-point-btn {
  margin-top: 12rpx;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 30rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  align-self: flex-start;
}

/* 文化导览点回来按钮 */
.back-button-item {
  padding: 16rpx 30rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 2rpx solid #e8eaed;
}

.back-to-area-btn {
  width: 100%;
  background: linear-gradient(135deg, #ffffff 0%, #f5f7fa 100%);
  color: #1677ff;
  border: 2rpx solid #1677ff;
  border-radius: 40rpx;
  padding: 20rpx 32rpx;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 4rpx 16rpx rgba(22, 119, 255, 0.15);
  transition: all 0.3s ease;
}

.back-to-area-btn:active {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  transform: scale(0.98);
}

/* 距离选择器 */
.distance-selector {
  display: flex;
  justify-content: space-between;
  padding: 20rpx;
  background: var(--gradient-card-light);
  border-radius: 40rpx;
  box-shadow: 0 4rpx 20rpx var(--shadow-color-light);
}

.distance-selector.compact {
  padding: 12rpx;
  margin: 0;
  border-radius: 32rpx;
}

.distance-selector.compact .distance-option {
  padding: 16rpx 0;
  font-size: 22rpx;
}

.distance-option {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 50rpx;
  font-size: 24rpx;
  transition: all 0.3s ease;
}

.distance-option.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.distance-option.active {
  background-color: rgba(59, 130, 246, 0.1);
  color: var(--theme-blue);
  font-weight: 500;
}

/* 导航按钮 */
.start-navigation-btn {
  background: linear-gradient(135deg, #1677ff 0%, #3b82f6 100%);
  color: #ffffff;
  border-radius: 48rpx;
  padding: 24rpx;
  margin: 0 20rpx 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: none;
  box-shadow: 0 6rpx 24rpx rgba(22, 119, 255, 0.3);
}

.start-navigation-btn.active {
  background: linear-gradient(135deg, #f97316 0%, #fb923c 100%);
  box-shadow: 0 6rpx 24rpx rgba(249, 115, 22, 0.3);
}

.start-navigation-btn[disabled] {
  background: linear-gradient(135deg, #94a3b8 0%, #cbd5e1 100%);
  opacity: 0.6;
}

/* 暂停按钮 */
.pause-navigation-btn {
  background: linear-gradient(135deg, #fa8c16 0%, #fb923c 100%);
  color: #ffffff;
  border-radius: 48rpx;
  padding: 24rpx;
  margin: 0 20rpx 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: none;
  box-shadow: 0 6rpx 24rpx rgba(250, 140, 22, 0.3);
}

.pause-navigation-btn.active {
  background: linear-gradient(135deg, #52c41a 0%, #6ee7b7 100%);
  box-shadow: 0 6rpx 24rpx rgba(82, 196, 26, 0.3);
}

.pause-navigation-btn[disabled] {
  background: linear-gradient(135deg, #94a3b8 0%, #cbd5e1 100%);
  opacity: 0.6;
}

/* 取消继续听按钮 */
.cancel-continue-listen-btn {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #ffffff;
  border-radius: 48rpx;
  padding: 24rpx;
  margin: 0 20rpx 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border: none;
  box-shadow: 0 6rpx 24rpx rgba(255, 77, 79, 0.3);
}

.cancel-continue-listen-btn[disabled] {
  background: linear-gradient(135deg, #94a3b8 0%, #cbd5e1 100%);
  opacity: 0.6;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5000;
}

.loading-content {
  text-align: center;
}

.loader {
  width: 48rpx;
  height: 48rpx;
  border: 3rpx solid var(--border-color-medium);
  border-top: 3rpx solid var(--theme-blue);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.page {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.page::-webkit-scrollbar {
  display: none;
}
</style>