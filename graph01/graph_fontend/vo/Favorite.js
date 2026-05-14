/**
 * Favorite VO - 收藏实体
 */
export class Favorite {
  constructor(data) {
    this.id = data.id || null;
    this.favoriteId = data.favoriteId || null;
    this.typeId = data.typeId || null;
    this.userId = data.userId || null;
  }
}
