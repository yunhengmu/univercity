package com.ruoyi.system.domain;

import lombok.Data;
import java.util.List;

/**
 * POI详情项
 */
@Data
public class PoiItem {
    /** 父级信息 */
    private List<String> parent;
    /** 距离 */
    private String distance;
    /** 省级编码 */
    private String pcode;
    /** 关键标签 */
    private String keytag;
    /** 重要性 */
    private List<String> importance;
    /** 业务扩展信息 */
    private BizExt biz_ext;
    /** 推荐值 */
    private String recommend;
    /** 类型 */
    private String type;
    /** 图片列表 */
    private List<Photo> photos;
    /** 优惠数量 */
    private String discount_num;
    /** 建筑信息 */
    private List<String> building;
    /** 网格编码 */
    private String gridcode;
    /** 类型编码 */
    private String typecode;
    /** 店铺信息标识 */
    private String shopinfo;
    /** POI权重 */
    private List<String> poiweight;
    /** 城市编码 */
    private String citycode;
    /** 行政区名称 */
    private String adname;
    /** 子级信息 */
    private List<String> children;
    /** 别名 */
    private String alias;
    /** 电话 */
    private String tel;
    /** POI唯一ID */
    private String id;
    /** 标签 */
    private List<String> tag;
    /** 事件 */
    private List<String> event;
    /** 入口经纬度 */
    private String entr_location;
    /** 室内地图标识 */
    private String indoor_map;
    /** 邮箱 */
    private List<String> email;
    /** 时间戳 */
    private String timestamp;
    /** 官网 */
    private List<String> website;
    /** 地址 */
    private String address;
    /** 车位数量 */
    private List<String> space_num;
    /** 行政区编码 */
    private String adcode;
    /** 省份名称 */
    private String pname;
    /** 业务类型 */
    private List<String> biz_type;
    /** 城市名称 */
    private String cityname;
    /** 邮编 */
    private List<String> postcode;
    /** 匹配度 */
    private String match;
    /** 商圈 */
    private String business_area;
    /** 室内数据 */
    private IndoorData indoor_data;
    /** 子类型 */
    private List<String> childtype;
    /** 标签 */
    private List<String> atag;
    /** 出口经纬度 */
    private List<String> exit_location;
    /** 名称 */
    private String name;
    /** 经纬度 */
    private String location;
    /** 店铺ID */
    private List<String> shopid;
    /** 收藏数 */
    private List<String> favorite_num;
    /** 导航POI ID */
    private List<String> navi_poiid;
    /** 团购数 */
    private String groupbuy_num;
    /** 精选评论 */
    private List<String> featured_reviews;
}