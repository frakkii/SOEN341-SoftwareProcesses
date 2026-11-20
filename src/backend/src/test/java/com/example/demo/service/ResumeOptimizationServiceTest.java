package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization.Line;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeOptimizationServiceTest {

    private static Line line(String original, String improved) {
        return new Line("bullet", original, improved, "Impact", "Why");
    }

    @Test
    void findsEachFigureTheAiAdded() {
        assertThat(ResumeOptimizationService.placeholders(
                line("Built the app", "Built the app for [number of users] users, cutting costs by [X%]")))
                .containsExactly("number of users", "X%");
    }

    @Test
    void bracketsAlreadyInTheResumeAreNotFigures() {
        assertThat(ResumeOptimizationService.placeholders(
                line("Led [Project Atlas]", "Led [Project Atlas], a team of [team size]")))
                .containsExactly("team size");
        assertThat(ResumeOptimizationService.placeholders(line("Built the app", "Built the Android app"))).isEmpty();
    }
}
