package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 导游缓存信息
 * ClassName: TourGuideCache
 * Package: com.ruoyi.system.domain
 * Description: 用于存储导游介绍相关的缓存数据
 *
 * @author 张鹏
 * @Create 2026/4/2
 * @Version 1.0
 */
public class TourGuideCache implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 位置名称
     */
    private String location;
    
    /**
     * 天气信息
     */
    private String weather;
    
    /**
     * 可讨论的话题列表（从大模型提取的关键字段）
     */
    private List<String> topics;
    
    /**
     * 原始提取数据
     */
    private String rawData;
    
    /**
     * 上次生成的导游介绍内容
     */
    private String lastGuideContent;
    
    /**
     * 总结后的内容
     */
    private String summary;
    
    public TourGuideCache() {
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getWeather() {
        return weather;
    }
    
    public void setWeather(String weather) {
        this.weather = weather;
    }
    
    public List<String> getTopics() {
        return topics;
    }
    
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }
    
    public String getRawData() {
        return rawData;
    }
    
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }
    
    public String getLastGuideContent() {
        return lastGuideContent;
    }
    
    public void setLastGuideContent(String lastGuideContent) {
        this.lastGuideContent = lastGuideContent;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @Override
    public String toString() {
        return "TourGuideCache{" +
                "location='" + location + '\'' +
                ", weather='" + weather + '\'' +
                ", topics=" + topics +
                ", rawData='" + rawData + '\'' +
                ", lastGuideContent='" + lastGuideContent + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}
