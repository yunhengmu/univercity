/**
 * Book VO - 小说实体
 */
export class Book {
  constructor(data) {
    this.id = data.id || null;
    this.name = data.name || '';
    this.userId = data.userId || null;
    this.type = data.type || '';
    this.world = data.world || '';
    this.style = data.style || '';
    this.core = data.core || '';
    this.beginning = data.beginning || '';
    this.development = data.development || '';
    this.turningPoint = data.turningPoint || null;
    this.climax = data.climax || '';
    this.ending = data.ending || '';
    this.wordsType = data.wordsType || '';
    this.branchNum = data.branchNum || null;
    this.control = data.control || '';
    this.chapterNum = data.chapterNum || null;
    this.totalChapter = data.totalChapter || null;
    this.favoriteId = data.favoriteId || null;
    this.loveId = data.loveId || null;
  }
}
