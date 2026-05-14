package com.ruoyi.system.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpResponse {
    private String status;
    private String info;
    private String infocode;
    private String province;
    private String city;
    private String adcode;
    private String rectangle; // 格式："lng1,lat1;lng2,lat2"


    /**
     * 辅助方法：获取矩形范围的左下角和右上角坐标
     * @return [minLng, minLat, maxLng, maxLat]
     */
//    public double[] getBounds() {
//        if (rectangle == null || rectangle.isEmpty()) {
//            return null;
//        }
//        String[] parts = rectangle.split(";");
//        if (parts.length != 2) return null;
//
//        String[] lb = parts[0].split(","); // 左下角
//        String[] rt = parts[1].split(","); // 右上角
//
//        return new double[]{
//            Double.parseDouble(lb[0]), // minLng
//            Double.parseDouble(lb[1]), // minLat
//            Double.parseDouble(rt[0]), // maxLng
//            Double.parseDouble(rt[1])  // maxLat
//        };
//    }
}