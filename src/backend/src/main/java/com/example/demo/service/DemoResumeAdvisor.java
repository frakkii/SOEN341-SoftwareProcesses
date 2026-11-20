package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization;
import com.example.demo.dto.ResumeOptimization.Line;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Used when no Claude API key is configured, so the feature can be shown and tested
// without calling the API. The page labels these suggestions as sample data.
public class DemoResumeAdvisor implements ResumeAdvisor {

    private static final Pattern NUMBER = Pattern.compile("\\d");

    @Override
    public boolean isDemo() {
        return true;
    }

    // Weak openings and the stronger verb the demo suggests instead
    // Each replacement must read correctly before whatever followed the opening and claim no more
    // than it did. "Helped <verb> ..." has no such replacement ("Supported fix bugs"), so it gets
    // the result hint instead; "Responsible for" became "Led", which claims leading a team.
    private static final List<String[]> WEAK_OPENINGS = List.of(
            new String[] { "responsible for ", "Took ownership of " },
            new String[] { "in charge of ", "Managed " },
            new String[] { "worked on ", "Built " },
            new String[] { "helped with ", "Contributed to " },
            new String[] { "did ", "Completed " });
    private static final Pattern BULLET_MARK = Pattern.compile("^[-*•▪●◦]\\s*");
    private static final int MAX_RESULT_HINTS = 3;

    // Simple rule-based rewrites of DOCX lines. PDFs get a short sample, since the server
    // doesn't extract PDF text itself.
    @Override
    public ResumeOptimization optimize(ResumeContent resume) {
        if (resume.isPdf() || resume.text() == null) {
            return new ResumeOptimization(
                    "This is a sample from demo mode. Demo mode can't read PDF text, so these lines aren't from your resume.",
                    List.of(
                            new Line("heading", "EXPERIENCE", "", "", ""),
                            new Line("bullet", "Responsible for the team website",
                                    "Led the redesign of the team website, cutting page load time by [X%]", "Impact",
                                    "Starting with an action verb and a result shows what you achieved. Fill in the number or remove it."),
                            new Line("bullet", "Worked on customer support tickets",
                                    "Resolved [number] customer support tickets per week", "Impact",
                                    "A specific number shows the scale of your work. Fill it in or remove it.")));
        }

        List<Line> lines = new ArrayList<>();
        int resultHints = 0;
        for (String raw : resume.text().split("\n")) {
            String text = raw.strip();
            if (text.isEmpty()) {
                continue;
            }
            boolean bullet = BULLET_MARK.matcher(text).find();
            text = BULLET_MARK.matcher(text).replaceFirst("");
            String kind = bullet ? "bullet" : isHeading(text) ? "heading" : "text";

            String improved = strongerOpening(text);
            if (improved != null) {
                lines.add(new Line(kind, text, improved, "Impact",
                        "Starting with a strong action verb shows what you did rather than what you were assigned."));
            } else if (bullet && !NUMBER.matcher(text).find() && text.length() > 25 && resultHints < MAX_RESULT_HINTS) {
                resultHints++;
                lines.add(new Line(kind, text, trimEnd(text) + ", resulting in [measurable result]", "Impact",
                        "Ending with the result shows why the work mattered. Fill in the placeholder or remove it."));
            } else {
                lines.add(new Line(kind, text, "", "", ""));
            }
        }
        return new ResumeOptimization(
                "This is sample output from demo mode, made with simple rules rather than a real AI review.",
                lines);
    }

    private static String strongerOpening(String text) {
        String lower = text.toLowerCase(java.util.Locale.ROOT);
        for (String[] opening : WEAK_OPENINGS) {
            if (lower.startsWith(opening[0]) && text.length() > opening[0].length()) {
                return opening[1] + text.substring(opening[0].length());
            }
        }
        return null;
    }

    private static boolean isHeading(String text) {
        return text.length() <= 40 && text.chars().anyMatch(Character::isLetter)
                && text.equals(text.toUpperCase(java.util.Locale.ROOT));
    }

    private static String trimEnd(String text) {
        return text.replaceAll("[.;,\\s]+$", "");
    }
}
