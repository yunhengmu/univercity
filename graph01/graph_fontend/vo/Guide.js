/**
 * Guide VO - 文化导览实体
 */
export class Guide {
  constructor(data) {
    this.id = data.id || null;
    this.userId = data.userId || null;
    this.name = data.name || '';
  }
}
