package com.ruoyi.system.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ruoyi.system.domain.Trajectory;
import com.ruoyi.system.domain.vo.BookResult;
import com.ruoyi.system.domain.vo.ChapterResult;

import java.util.regex.Pattern;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static BookResult deserializeToBookResult(String chatJson) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(chatJson, BookResult.class);
    }

    public static ChapterResult deserializeToChapterResult(String chatJson) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(chatJson, ChapterResult.class);
    }
}