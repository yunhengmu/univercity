package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * 自定义反序列化器：将空数组转换为 null，字符串保持原样
 */
class EmptyArrayAsNullStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return p.getValueAsString();
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            // 跳过空数组，返回 null
            p.skipChildren();
            return null;
        } else {
            // 其他情况（如 null）也返回 null
            return null;
        }
    }
}
