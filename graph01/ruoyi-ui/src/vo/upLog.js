/**
 * 上架记录VO对象
 * @description 对应后端 UpLog 实体类
 */
export class UpLogVO {
  constructor(data = {}) {
    // 如果data为null或undefined，使用空对象
    if (!data) {
      data = {}
    }
    
    // 上架记录ID
    this.id = data.id || null
    
    // 书目ID
    this.bookId = data.bookId || null
    
    // 用户ID
    this.userId = data.userId || null
    
    // 上架状态（0-未上架 / 1-已上架）
    this.status = data.status !== undefined ? data.status : 0
    
    // 上架时间
    this.createTime = data.createTime || null
    
    // 更新时间
    this.updateTime = data.updateTime || null
  }
  
  /**
   * 判断是否已上架
   * @returns {boolean}
   */
  isShelved() {
    return this.status === 1
  }
  
  /**
   * 判断是否未上架
   * @returns {boolean}
   */
  isUnshelved() {
    return this.status === 0
  }
  
  /**
   * 获取状态文本
   * @returns {string}
   */
  getStatusText() {
    return this.status === 1 ? '已上架' : '未上架'
  }
  
  /**
   * 获取状态标签类型（用于Element UI Tag组件）
   * @returns {string}
   */
  getStatusTagType() {
    return this.status === 1 ? 'success' : 'info'
  }
  
  /**
   * 格式化创建时间
   * @param {string} format - 时间格式，默认 'YYYY-MM-DD HH:mm:ss'
   * @returns {string}
   */
  getFormattedCreateTime(format = 'YYYY-MM-DD HH:mm:ss') {
    if (!this.createTime) return '-'
    return this.formatDate(this.createTime, format)
  }
  
  /**
   * 格式化更新时间
   * @param {string} format - 时间格式，默认 'YYYY-MM-DD HH:mm:ss'
   * @returns {string}
   */
  getFormattedUpdateTime(format = 'YYYY-MM-DD HH:mm:ss') {
    if (!this.updateTime) return '-'
    return this.formatDate(this.updateTime, format)
  }
  
  /**
   * 格式化日期
   * @param {string|Date} date - 日期
   * @param {string} format - 格式
   * @returns {string}
   * @private
   */
  formatDate(date, format) {
    if (!date) return '-'
    
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    
    return format
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  }
  
  /**
   * 转换为纯对象
   * @returns {Object}
   */
  toObject() {
    return {
      id: this.id,
      bookId: this.bookId,
      userId: this.userId,
      status: this.status,
      createTime: this.createTime,
      updateTime: this.updateTime
    }
  }
  
  /**
   * 从对象创建实例
   * @param {Object} data - 数据对象
   * @returns {UpLogVO}
   */
  static fromObject(data) {
    return new UpLogVO(data)
  }
  
  /**
   * 从数组创建实例列表
   * @param {Array} dataList - 数据数组
   * @returns {UpLogVO[]}
   */
  static fromList(dataList) {
    if (!Array.isArray(dataList)) return []
    return dataList.map(item => new UpLogVO(item))
  }
}

/**
 * 上架记录查询参数VO
 */
export class UpLogQueryVO {
  constructor(data = {}) {
    // 分页参数
    this.pageNum = data.pageNum || 1
    this.pageSize = data.pageSize || 10
    
    // 查询条件
    this.bookId = data.bookId || null
    this.userId = data.userId || null
    this.status = data.status !== undefined ? data.status : null
    
    // 时间范围
    this.params = {
      beginCreateTime: data.beginCreateTime || null,
      endCreateTime: data.endCreateTime || null
    }
  }
  
  /**
   * 转换为查询对象
   * @returns {Object}
   */
  toQueryParams() {
    const params = {
      pageNum: this.pageNum,
      pageSize: this.pageSize
    }
    
    if (this.bookId !== null && this.bookId !== undefined) {
      params.bookId = this.bookId
    }
    
    if (this.userId !== null && this.userId !== undefined) {
      params.userId = this.userId
    }
    
    if (this.status !== null && this.status !== undefined) {
      params.status = this.status
    }
    
    if (this.params.beginCreateTime) {
      params.params = params.params || {}
      params.params.beginCreateTime = this.params.beginCreateTime
    }
    
    if (this.params.endCreateTime) {
      params.params = params.params || {}
      params.params.endCreateTime = this.params.endCreateTime
    }
    
    return params
  }
}

// 默认导出
export default UpLogVO

/**
 * 书籍VO对象
 * @description 对应后端 Book 实体类
 */
export class BookVO {
  constructor(data = {}) {
    // 如果data为null或undefined，使用空对象
    if (!data) {
      data = {}
    }
    
    // 书籍ID
    this.id = data.id || null
    
    // 书籍名称
    this.name = data.name || '-'
    
    // 用户ID
    this.userId = data.userId || null
    
    // 书籍类型
    this.type = data.type || '-'
    
    // 书籍世界观
    this.world = data.world || '-'
    
    // 文笔风格
    this.style = data.style || '-'
    
    // 书籍主旨
    this.core = data.core || '-'
    
    // 书籍开篇
    this.beginning = data.beginning || '-'
    
    // 书籍发展
    this.development = data.development || '-'
    
    // 书籍转折点
    this.turningPoint = data.turningPoint || '-'
    
    // 书籍高潮
    this.climax = data.climax || '-'
    
    // 书籍结尾
    this.ending = data.ending || '-'
    
    // 文章长度
    this.wordsType = data.wordsType || '-'
    
    // 支线数量
    this.branchNum = data.branchNum || 0
    
    // 开篇，发展，转折，高潮，结尾对应的篇幅
    this.control = data.control || '-'
    
    // 目前进行章数
    this.chapterNum = data.chapterNum || 0
    
    // 小说总共有多少章数
    this.totalChapter = data.totalChapter || 0
  }
  
  /**
   * 转换为纯对象
   * @returns {Object}
   */
  toObject() {
    return {
      id: this.id,
      name: this.name,
      userId: this.userId,
      type: this.type,
      world: this.world,
      style: this.style,
      core: this.core,
      beginning: this.beginning,
      development: this.development,
      turningPoint: this.turningPoint,
      climax: this.climax,
      ending: this.ending,
      wordsType: this.wordsType,
      branchNum: this.branchNum,
      control: this.control,
      chapterNum: this.chapterNum,
      totalChapter: this.totalChapter
    }
  }
  
  /**
   * 从对象创建实例
   * @param {Object} data - 数据对象
   * @returns {BookVO}
   */
  static fromObject(data) {
    return new BookVO(data)
  }
}
