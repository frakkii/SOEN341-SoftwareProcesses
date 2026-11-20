package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization;
import com.example.demo.dto.ResumeOptimization.Line;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Demo mode's rule-based rewrites: they must read as proper sentences and claim no more than the line did
class DemoResumeAdvisorTest {

    private static Map<String, String> rewrites(String... bullets) {
        String text = List.of(bullets).stream().map(b -> "- " + b).collect(Collectors.joining("\n"));
        ResumeOptimization result = new DemoResumeAdvisor()
                .optimize(ResumeAdvisor.ResumeContent.ofText("cv.docx", text));
        return result.lines().stream().collect(Collectors.toMap(Line::original, Line::improved));
    }

    @Test
    void weakOpeningsGetAnActionVerb() {
        assertThat(rewrites("Worked on the telemetry parser", "In charge of the club budget",
                "Helped with the onboarding guide", "Did the weekly reports"))
                .containsEntry("Worked on the telemetry parser", "Built the telemetry parser")
                .containsEntry("In charge of the club budget", "Managed the club budget")
                .containsEntry("Helped with the onboarding guide", "Contributed to the onboarding guide")
                .containsEntry("Did the weekly reports", "Completed the weekly reports");
    }

    @Test
    void responsibleForDoesNotBecomeLed() {
        // "Led" would claim leading a team, which the line doesn't say
        assertThat(rewrites("Responsible for writing unit tests", "Responsible for the team code reviews"))
                .containsEntry("Responsible for writing unit tests", "Took ownership of writing unit tests")
                .containsEntry("Responsible for the team code reviews", "Took ownership of the team code reviews");
    }

    @Test
    void helpedFollowedByAVerbIsNotRewrittenIntoBrokenEnglish() {
        // Was "Supported fix bugs in the ground station dashboard"
        String line = "Helped fix bugs in the ground station dashboard";
        String improved = rewrites(line).get(line);
        assertThat(improved).doesNotStartWith("Supported");
        // It gets the result hint instead, which keeps the wording
        assertThat(improved).isEqualTo(line + ", resulting in [measurable result]");
    }
}
