package com.example.demo.config;

import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.example.demo.service.ClaudeResumeAdvisor;
import com.example.demo.service.DemoResumeAdvisor;
import com.example.demo.service.ResumeAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Chooses how AI resume suggestions are produced. With an API key (ANTHROPIC_API_KEY, see
// application.properties) it calls Claude; without one it uses demo mode, so the app,
// the tests and CI all work with no key and no cost.
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public ResumeAdvisor resumeAdvisor(@Value("${careerconnect.ai.api-key:}") String apiKey,
                                       @Value("${careerconnect.ai.model:claude-opus-5-5}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            log.info("No ANTHROPIC_API_KEY set: AI resume suggestions run in demo mode (sample suggestions)");
            return new DemoResumeAdvisor();
        }
        log.info("AI resume suggestions use the Claude API with model {}", model);
        return new ClaudeResumeAdvisor(AnthropicOkHttpClient.builder().apiKey(apiKey).build(), model);
    }
}
