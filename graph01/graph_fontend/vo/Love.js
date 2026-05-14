/**
 * Love 常量类 - 收藏类型定义
 */
export class Love {
  // 小说类型
  static BOOK = 0;
  
  // 文化导览类型
  static CULTURAL = 1;
  
  /**
   * 获取类型标签文本
   * @param {number} typeId - 类型 ID
   * @returns {string} 类型标签
   */
  static getTypeLabel(typeId) {
    if (typeId === this.BOOK) {
      return '小说';
    } else if (typeId === this.CULTURAL) {
      return '文化导览';
    }
    return '未知';
  }
}
