/**
 * Need VO - 需求实体
 */
export class Need {
  constructor(data) {
    this.id = data.id || null;
    this.customerId = data.customerId || null;
    this.needContent = data.needContent || '';
  }
}
