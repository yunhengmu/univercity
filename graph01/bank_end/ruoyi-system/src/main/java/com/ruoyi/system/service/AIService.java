package com.ruoyi.system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.logging.Logger;

@Service
@Slf4j
public class AIService {

    @Autowired
    private ChatClient chatClient;

    // 匹配```json开头标记（兼容空格/换行）
    private static final Pattern JSON_CODE_BLOCK_START = Pattern.compile("^\\s*```[jJ][sS][oO][nN]\\s*");
    // 匹配```结尾标记（兼容空格/换行）
    private static final Pattern JSON_CODE_BLOCK_END = Pattern.compile("\\s*```\\s*$");

    /**
     * 移除Markdown代码块标记
     */
    private String removeCodeBlockMarkers(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        // 移除开头的```json（兼容大小写）
        content = JSON_CODE_BLOCK_START.matcher(content).replaceAll("");
        // 移除结尾的```
        content = JSON_CODE_BLOCK_END.matcher(content).replaceAll("");
        return content.trim();
    }

    public String generateContent(String prompt) {
        try {
            log.info("开始调用DeepSeek API，prompt长度: {}", prompt.length());

            // 获取AI响应内容
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // 清理Markdown代码块标记（```json ... ```）
            content = removeCodeBlockMarkers(content);

            log.info("API调用成功，返回内容长度: {}", content.length());

            return content;

        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }


    public String generateText(String prompt) {
        try {
            log.info("开始调用DeepSeek API，prompt长度: {}", prompt.length());

            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("API调用成功，返回内容长度: {}", content.length());

            return content;

        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }
}