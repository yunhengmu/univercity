package com.ruoyi.framework.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class DeepSeekConfig {
//
//    @Value("${spring.ai.openai.api-key}")
//    private String apiKey;
//
//    @Value("${spring.ai.openai.base-url}")
//    private String baseUrl;
//
//    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
//    private String model;
//
//    @Bean
//    public OpenAiApi openAiApi() {
//        return new OpenAiApi(baseUrl, apiKey);
//    }
//
//    @Bean
//    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
//        OpenAiChatOptions options = OpenAiChatOptions.builder()
//                .model(model)
//                .temperature(0.7)
//                .maxTokens(2000)
//                .build();
//
//        return new OpenAiChatModel(openAiApi, options);
//    }
//
//}
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai.chat.options")
public class DeepSeekConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    // 读取配置中的参数
    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:4000}")
    private Integer maxTokens;

    @Bean
    public OpenAiApi openAiApi() {
        // 确保baseUrl以/v1结尾（Spring AI 0.8+版本需要）
        String correctedBaseUrl = baseUrl;
//        if (!correctedBaseUrl.endsWith("/v1")) {
//            correctedBaseUrl = correctedBaseUrl + "/v1";
//        }
        System.out.println("使用API端点: " + correctedBaseUrl);
        return new OpenAiApi(correctedBaseUrl, apiKey);
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)  // 使用从配置读取的值
                .build();

        System.out.println("模型配置: model=" + model +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens);

        return new OpenAiChatModel(openAiApi, options);
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }
}