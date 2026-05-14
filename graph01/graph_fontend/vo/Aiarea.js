/**
 * Aiarea VO - AI区域内容实体
 */
export class Aiarea {
  constructor(data) {
    this.id = data.id || null;
    this.guideId = data.guideId || null;
    this.contentAi = data.contentAi || '';
    this.type = data.type || ''; // NO_SELF 或 IS_SELF
  }
}
