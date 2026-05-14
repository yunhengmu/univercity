package com.ruoyi.system.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 处理高德POI中tel字段的混合类型（字符串/数组）反序列化
 */
public class TelListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        List<String> telList = new ArrayList<>();
        JsonNode node = parser.getCodec().readTree(parser);

        // 1. 如果是字符串类型（如"4009501925"）
        if (node.isTextual()) {
            String tel = node.asText().trim();
            if (!tel.isEmpty()) {
                // 处理多个电话用分号分隔的情况（如"010-65131892;010-85007421"）
                String[] tels = tel.split(";");
                for (String t : tels) {
                    if (!t.isEmpty()) {
                        telList.add(t);
                    }
                }
            }
        }
        // 2. 如果是数组类型（如[] 或 ["123456"]）
        else if (node.isArray()) {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                if (element.isTextual()) {
                    String tel = element.asText().trim();
                    if (!tel.isEmpty()) {
                        telList.add(tel);
                    }
                }
            }
        }
        // 3. 空值/其他类型直接返回空List
        return telList;
    }
}