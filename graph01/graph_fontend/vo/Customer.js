/**
 * Customer VO - 用户实体
 */
export class Customer {
  constructor(data) {
    this.id = data.id || null;
    this.name = data.name || '';
    this.description = data.description || '';
    this.status = data.status || false;
    this.imageUrl = data.imageUrl || '';
    this.createdAt = data.createdAt || '';
    this.updatedAt = data.updatedAt || '';
  }
}
