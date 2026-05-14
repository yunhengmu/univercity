package com.ruoyi.system.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.system.domain.gaode.BusinessArea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlexibleBusinessAreaDeserializer extends JsonDeserializer<List<BusinessArea>> {
    @Override
    public List<BusinessArea> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node == null || node.isNull()) {
            return new ArrayList<>();
        }
        
        if (node.isArray()) {
            // 如果是数组，直接处理每个元素
            List<BusinessArea> businessAreas = new ArrayList<>();
            for (JsonNode item : node) {
                BusinessArea area = convertNodeToBusinessArea(item);
                if (area != null) {
                    businessAreas.add(area);
                }
            }
            return businessAreas;
        } else if (node.isObject()) {
            // 如果是对象，将其包装为单元素列表
            List<BusinessArea> businessAreas = new ArrayList<>();
            BusinessArea area = convertNodeToBusinessArea(node);
            if (area != null) {
                businessAreas.add(area);
            }
            return businessAreas;
        } else {
            // 其他情况返回空列表
            return new ArrayList<>();
        }
    }
    
    private BusinessArea convertNodeToBusinessArea(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        BusinessArea area = new BusinessArea();
        
        // 设置商圈名称
        JsonNode nameNode = node.get("name");
        if (nameNode != null && !nameNode.isNull()) {
            area.setName(nameNode.asText());
        }
        
        // 设置ID
        JsonNode idNode = node.get("id");
        if (idNode != null && !idNode.isNull()) {
            area.setId(idNode.asText());
        }
        
        // 设置位置
        JsonNode locationNode = node.get("location");
        if (locationNode != null && !locationNode.isNull()) {
            area.setLocation(locationNode.asText());
        }
        
        return area;
    }
}