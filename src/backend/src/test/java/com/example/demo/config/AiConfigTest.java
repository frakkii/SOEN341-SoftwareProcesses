package com.example.demo.config;

import com.example.demo.service.ClaudeResumeAdvisor;
import com.example.demo.service.DemoResumeAdvisor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Building the advisor never calls the API, so this needs no network or real key
class AiConfigTest {

    private final AiConfig config = new AiConfig();

    @Test
    void noApiKeyMeansDemoMode() {
        assertThat(config.resumeAdvisor("", "claude-opus-5-5")).isInstanceOf(DemoResumeAdvisor.class);
        assertThat(config.resumeAdvisor("   ", "claude-opus-5-5").isDemo()).isTrue();
        assertThat(config.resumeAdvisor(null, "claude-opus-5-5").isDemo()).isTrue();
    }

    @Test
    void anApiKeyMeansClaude() {
        var advisor = config.resumeAdvisor("sk-ant-test-not-a-real-key", "claude-opus-5-5");
        assertThat(advisor).isInstanceOf(ClaudeResumeAdvisor.class);
        assertThat(advisor.isDemo()).isFalse();
    }
}
