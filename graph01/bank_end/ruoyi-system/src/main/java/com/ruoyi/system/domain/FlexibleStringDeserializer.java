package com.ruoyi.system.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class FlexibleStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node == null || node.isNull()) {
            return null;
        } else if (node.isArray()) {
            if (node.size() > 0) {
                return node.get(0).asText(); // 如果是数组，取第一个元素
            }
            return null;
        } else if (node.isTextual()) {
            return node.asText(); // 如果是字符串，直接返回
        } else if (node.isObject()) {
            return node.toString(); // 对象转换为字符串
        } else {
            return node.asText(null); // 其他情况尝试转为文本
        }
    }
}