package com.ruoyi.system.domain.gaode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将空数组或非标准格式反序列化为 null 或空列表
 */
class EmptyArrayAsNullListDeserializer extends JsonDeserializer<List<BusinessArea>> {
    @Override
    public List<BusinessArea> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.START_ARRAY) {
            List<BusinessArea> result = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.currentToken() == JsonToken.START_OBJECT) {
                    // 正常的对象格式
                    BusinessArea area = ctxt.readValue(p, BusinessArea.class);
                    result.add(area);
                } else if (p.currentToken() == JsonToken.START_ARRAY) {
                    // 如果元素本身是数组（嵌套数组），尝试从内部数组中提取对象
                    List<BusinessArea> nestedAreas = ctxt.readValue(p, ctxt.getTypeFactory().constructCollectionType(ArrayList.class, BusinessArea.class));
                    if (nestedAreas != null && !nestedAreas.isEmpty()) {
                        result.addAll(nestedAreas);
                    }
                } else {
                    // 其他情况跳过
                    p.skipChildren();
                }
            }
            return result.isEmpty() ? null : result;
        } else if (p.currentToken() == JsonToken.START_OBJECT) {
            // 如果是对象（非标准格式），跳过并返回 null 或空列表
            p.skipChildren();
            return null;
        } else if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        } else {
            // 其他情况也返回 null
            return null;
        }
    }
}

