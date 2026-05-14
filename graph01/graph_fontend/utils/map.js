// D:\java-table\graph01\graph_fontend\utils\map.js

/**
 * 高德地图工具类
 * 封装地图初始化、定位、搜索、路径规划等功能
 */

// 地图配置常量
const MAP_CONFIG = {
    key: process.env.AMAP_KEY || 'your-amap-key-here',
    securityCode: process.env.AMAP_SECURITY_CODE || 'your-security-code-here',
    version: '1.4.15',
    searchKey: process.env.AMAP_SEARCH_KEY || 'your-search-key-here'
}

/**
 * 标准化位置数据格式
 * 确保所有位置数据都是 { lng: Number, lat: Number } 格式
 * @param {Object|Array} location - 位置数据，可以是对象或数组
 * @returns {Object} 标准化的位置对象 { lng, lat }
 */
export function normalizeLocation(location) {
    if (!location) {
        console.warn('[位置格式化] 输入为空');
        return null;
    }
    
    // 如果是数组 [lng, lat]
    if (Array.isArray(location)) {
        const lng = Number(location[0]);
        const lat = Number(location[1]);
        
        if (isNaN(lng) || isNaN(lat)) {
            console.error('[位置格式化] 数组格式无效:', location);
            return null;
        }
        
        return { lng, lat };
    }
    
    // 如果是对象
    if (typeof location === 'object') {
        // 支持多种字段名: lng/longitude, lat/latitude
        const lng = Number(location.lng || location.longitude);
        const lat = Number(location.lat || location.latitude);
        
        if (isNaN(lng) || isNaN(lat)) {
            console.error('[位置格式化] 对象格式无效:', location);
            return null;
        }
        
        // 验证坐标范围
        if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
            console.error('[位置格式化] 坐标超出有效范围:', { lng, lat });
            return null;
        }
        
        return { lng, lat };
    }
    
    console.error('[位置格式化] 未知格式:', location);
    return null;
}

/**
 * 验证位置数据是否有效
 * @param {Object} location - 位置对象
 * @returns {Boolean} 是否有效
 */
export function isValidLocation(location) {
    if (!location || typeof location !== 'object') {
        return false;
    }
    
    const lng = Number(location.lng || location.longitude);
    const lat = Number(location.lat || location.latitude);
    
    return !isNaN(lng) && !isNaN(lat) && 
           lng >= -180 && lng <= 180 && 
           lat >= -90 && lat <= 90;
}

/**
 * 初始化高德地图安全配置
 */
export function initAMapSecurity() {
    if (typeof window !== 'undefined') {
        window._AMapSecurityConfig = {
            securityJsCode: MAP_CONFIG.securityCode
        }
    }
}

/**
 * 加载高德地图脚本
 * @returns {Promise} 返回 AMap 对象
 */
export function loadAMapScript() {
    return new Promise((resolve, reject) => {
        // 检查是否已加载
        if (window.AMap) {
            resolve(window.AMap)
            return
        }

        const existingScript = document.querySelector(`script[src*="${MAP_CONFIG.key}"]`)
        if (existingScript) {
            // 脚本已存在但未初始化完成
            const checkInterval = setInterval(() => {
                if (window.AMap) {
                    clearInterval(checkInterval)
                    resolve(window.AMap)
                }
            }, 100)
            return
        }

        // 创建回调函数
        window.onAMapJSLoader = () => {
            resolve(window.AMap)
        }

        // 加载脚本
        const script = document.createElement('script')
        script.type = 'text/javascript'
        script.src = `https://webapi.amap.com/maps?v=${MAP_CONFIG.version}&key=${MAP_CONFIG.key}&callback=onAMapJSLoader`
        script.onerror = () => {
            reject(new Error('地图脚本加载失败'))
        }
        document.head.appendChild(script)
    })
}

/**
 * 创建地图实例
 * @param {Object} config 地图配置
 * @returns {Object} 地图实例
 */
export function createMapInstance(containerId, config = {}) {
    if (!window.AMap) {
        throw new Error('AMap 未初始化')
    }

    const defaultConfig = {
        zoom: 15,
        viewMode: '2D',
        mapStyle: 'amap://styles/normal',
        resizeEnable: true,
        plugins: ['AMap.Scale']
    }

    const mergedConfig = { ...defaultConfig, ...config }
    return new window.AMap.Map(containerId, mergedConfig)
}

/**
 * 加载地图插件
 * @param {Array} plugins 插件名称数组
 * @returns {Promise}
 */
export function loadMapPlugins(plugins) {
    if (!window.AMap) {
        throw new Error('AMap 未初始化')
    }

    const pluginPromises = plugins.map(pluginName => {
        return new Promise(resolve => {
            window.AMap.plugin(pluginName, () => {
                resolve()
            })
        })
    })

    return Promise.all(pluginPromises)
}

/**
 * 初始化地理定位插件
 * @param {Object} map 地图实例
 * @param {Object} options 定位配置
 * @returns {Object} 定位插件实例
 */
export function initGeolocation(map, options = {}) {
    const defaultOptions = {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
        convert: true,
        showButton: true,
        buttonPosition: 'LB',
        buttonOffset: new window.AMap.Pixel(10, 70),
        showMarker: false,
        showCircle: true,
        panToLocation: true,
        zoomToAccuracy: true
    }

    const mergedOptions = { ...defaultOptions, ...options }
    const geolocation = new window.AMap.Geolocation(mergedOptions)

    if (map) {
        map.addControl(geolocation)
    }

    return geolocation
}

/**
 * 初始化地点搜索插件
 * @param {Object} options 搜索配置
 * @returns {Object} 搜索插件实例
 */
export function initPlaceSearch(options = {}) {
    const defaultOptions = {
        pageSize: 10,
        pageIndex: 1,
        city: "全国",
        citylimit: false
    }

    const mergedOptions = { ...defaultOptions, ...options }
    return new window.AMap.PlaceSearch(mergedOptions)
}

/**
 * 搜索地点
 * @param {String} keyword 搜索关键词
 * @returns {Promise<Array>} 搜索结果
 */
export async function searchPlace(keyword) {
    if (!keyword || !keyword.trim()) {
        return []
    }

    try {
        const url = `https://restapi.amap.com/v5/place/text?keywords=${encodeURIComponent(keyword)}&key=${MAP_CONFIG.searchKey}`
        const [err, res] = await uni.request({ url })

        if (err || res.statusCode !== 200 || res.data.status !== '1') {
            return []
        }

        return res.data.pois || []
    } catch (error) {
        console.error('地点搜索失败:', error)
        return []
    }
}

/**
 * 获取当前位置（原生 API）
 * @returns {Promise<Object>} 位置信息
 */
export function getCurrentPosition() {
    return new Promise((resolve, reject) => {
        // #ifdef MP-WEIXIN
        // 微信小程序使用 uni.getLocation,启用高精度定位
        uni.getLocation({
            type: 'gcj02',  // 国测局坐标系,与高德一致
            altitude: true,  // 启用海拔信息,提高精度
            isHighAccuracy: true,  // 开启高精度定位
            highAccuracyExpireTime: 15000,  // 高精度定位超时时间15秒
            success: (res) => {
                
                // 标准化位置数据
                const location = normalizeLocation({
                    lng: res.longitude,
                    lat: res.latitude
                });
                
                if (!location) {
                    reject(new Error('位置数据格式错误'));
                    return;
                }
                
                resolve(location);
            },
            fail: (error) => {
                console.log('[定位] 高精度定位失败，尝试普通精度定位...', error);
                // 尝试普通精度定位作为降级方案
                uni.getLocation({
                    type: 'gcj02',
                    altitude: false,
                    isHighAccuracy: false,
                    success: (res) => {
                        console.log('[定位] 普通精度定位成功');
                        
                        const location = normalizeLocation({
                            lng: res.longitude,
                            lat: res.latitude
                        });
                        
                        if (!location) {
                            reject(new Error('位置数据格式错误'));
                            return;
                        }
                        
                        resolve(location);
                    },
                    fail: (err) => {
                        console.error('[定位] 普通精度定位也失败:', err);
                        reject(new Error(`定位失败:${err.errMsg}`));
                    }
                })
            }
        })
        // #endif
        
        // #ifdef H5
        if (!navigator.geolocation) {
            reject(new Error('浏览器不支持地理位置 API'))
            return
        }

        navigator.geolocation.getCurrentPosition(
            position => {
                const { latitude, longitude } = position.coords
                resolve({ lng: longitude, lat: latitude })
            },
            error => reject(new Error(`定位失败：${error.message}`)),
            { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
        )
        // #endif
    })
}

/**
 * 使用高德地图定位插件获取位置
 * @returns {Promise<Object>} 位置信息
 */
export function getAMapLocation() {
    return new Promise((resolve, reject) => {
        if (!window.AMap) {
            reject(new Error('AMap 未初始化'))
            return
        }

        const geolocation = new window.AMap.Geolocation({
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0,
            convert: true
        })

        geolocation.getCurrentPosition((status, result) => {
            if (status === 'complete') {
                const { lng, lat } = result.position
                resolve({ lng, lat })
            } else {
                reject(new Error(`高德地图定位失败：${result}`))
            }
        })
    })
}

/**
 * 设置地图中心点
 * @param {Object} map 地图实例
 * @param {Array} center 中心点坐标 [lng, lat]
 */
export function setMapCenter(map, center) {
    if (map && center) {
        map.setCenter(center)
    }
}

/**
 * 缩放地图
 * @param {Object} map 地图实例
 * @param {Number} delta 缩放级别变化
 */
export function zoomMap(map, delta) {
    if (!map) return

    const currentZoom = map.getZoom()
    const newZoom = currentZoom + delta

    // 限制缩放范围
    if (newZoom >= 3 && newZoom <= 18) {
        map.setZoom(newZoom)
    }
}

/**
 * 添加标记点
 * @param {Object} map 地图实例
 * @param {Object} options 标记配置
 * @returns {Object} 标记实例
 */
export function addMarker(map, options) {
    if (!window.AMap || !map) return null

    const defaultOptions = {
        title: '标记点',
        icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
        animation: 'AMAP_ANIMATION_DROP'
    }

    const mergedOptions = { ...defaultOptions, ...options }
    const marker = new window.AMap.Marker(mergedOptions)

    if (map) {
        map.add(marker)
    }

    return marker
}

/**
 * 清除地图上的覆盖物
 * @param {Object} map 地图实例
 * @param {String} type 覆盖物类型
 */
export function clearOverlays(map, type = null) {
    if (!map) return

    if (type) {
        const overlays = map.getAllOverlays(type)
        overlays.forEach(overlay => map.remove(overlay))
    } else {
        map.clearMap()
    }
}

/**
 * 绘制导航路径
 * @param {Object} map 地图实例
 * @param {Object} origin 起点
 * @param {Object} destination 终点
 * @returns {Object} 驾车路线规划实例
 */
export function drawNavigationPath(map, origin, destination) {
    if (!window.AMap || !map) return null

    try {
        const [destLng, destLat] = destination.location.split(',').map(Number)

        // 清除旧的路径
        if (map.driving) {
            map.driving.clear()
        }

        return new Promise((resolve, reject) => {
            window.AMap.plugin('AMap.Driving', () => {
                try {
                    const driving = new window.AMap.Driving({
                        map: map,
                        panel: false,
                        hideMarkers: false,
                        polylineOptions: {
                            strokeColor: '#2563eb',
                            strokeWeight: 6,
                            strokeOpacity: 0.8
                        },
                        markerOptions: {
                            origin: {
                                icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
                                title: '起点'
                            },
                            destination: {
                                icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
                                title: '终点'
                            }
                        }
                    })

                    driving.search(
                        new window.AMap.LngLat(origin.lng, origin.lat),
                        new window.AMap.LngLat(destLng, destLat),
                        (status, result) => {
                            if (status === 'complete') {
                                if (result.routes && result.routes.length > 0) {
                                    const path = result.routes[0].path
                                    if (path && path.length > 0) {
                                        map.setFitView()
                                    }
                                }
                                resolve(driving)
                            } else {
                                let errorMsg = '路径规划失败'
                                if (result === 'USERKEY_PLAT_NOMATCH') errorMsg = 'API Key 平台不匹配'
                                else if (result === 'INVALID_USER_KEY') errorMsg = 'API Key 无效'
                                else if (result === 'OVER_QUERY_LIMIT') errorMsg = 'API 调用次数超限'

                                uni.showToast({ title: errorMsg, icon: 'none' })
                                reject(new Error(errorMsg))
                            }
                        }
                    )

                    map.driving = driving
                } catch (error) {
                    uni.showToast({ title: '绘制路径失败', icon: 'none' })
                    reject(error)
                }
            })
        })
    } catch (error) {
        uni.showToast({ title: '绘制路径失败', icon: 'none' })
        return null
    }
}

/**
 * 更新用户位置标记
 * @param {Object} map 地图实例
 * @param {Object} location 位置信息
 * @param {Object} userMarker 当前标记实例
 * @returns {Object} 新的标记实例
 */
export function updateUserMarker(map, location, userMarker = null) {
    // #ifdef H5
    if (!window.AMap || !map || !location) return userMarker

    const { lng, lat } = location

    if (userMarker) {
        userMarker.setPosition([lng, lat])
        return userMarker
    } else {
        const marker = new window.AMap.Marker({
            position: [lng, lat],
            title: '当前位置',
            icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png',
            animation: 'AMAP_ANIMATION_DROP'
        })
        map.add(marker)
        return marker
    }
    // #endif
    
    // #ifdef MP-WEIXIN
    // 微信小程序中，标记点通过 markers 数组管理，不需要单独操作
    return location
    // #endif
}

/**
 * 绑定地图点击事件
 * @param {Object} map 地图实例
 * @param {Function} onClick 点击回调函数
 */
export function bindMapClickEvent(map, onClick) {
    // #ifdef H5
    if (!map || !onClick) return

    map.on('click', e => {
        const { lng, lat } = e.lnglat
        onClick({ lng, lat })
    })
    // #endif
    
    // #ifdef MP-WEIXIN
    // 微信小程序中，地图点击事件通过 @markertap 或 @tap 处理
    // 这里提供一个包装函数，实际使用时需要在模板中绑定
    if (onClick) {
        map._clickHandler = onClick
    }
    // #endif
}

/**
 * 销毁地图实例
 * @param {Object} map 地图实例
 */
export function destroyMap(map) {
    // #ifdef H5
    if (map) {
        map.destroy()
    }
    // #endif
    
    // #ifdef MP-WEIXIN
    // 微信小程序地图组件由框架自动管理，无需手动销毁
    // #endif
}

export default {
    initAMapSecurity,
    loadAMapScript,
    createMapInstance,
    loadMapPlugins,
    initGeolocation,
    initPlaceSearch,
    searchPlace,
    getCurrentPosition,
    getAMapLocation,
    setMapCenter,
    zoomMap,
    addMarker,
    clearOverlays,
    drawNavigationPath,
    updateUserMarker,
    bindMapClickEvent,
    destroyMap,
    normalizeLocation,
    isValidLocation,
    MAP_CONFIG
}
