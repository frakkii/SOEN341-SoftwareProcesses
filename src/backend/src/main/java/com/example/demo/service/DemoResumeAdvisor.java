package com.example.demo.service;

import com.example.demo.dto.ResumeFeedback;
import com.example.demo.dto.ResumeFeedback.Suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// Used when no Claude API key is configured, so the feature can be shown and tested
// without calling the API. The page labels this feedback as sample data. A few simple
// checks on DOCX text keep it from being identical for every resume; PDFs get the
// general sample only, since the server doesn't extract PDF text itself.
public class DemoResumeAdvisor implements ResumeAdvisor {

    private static final Pattern NUMBER = Pattern.compile("\\d");
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");

    @Override
    public boolean isDemo() {
        return true;
    }

    @Override
    public ResumeFeedback review(ResumeContent resume) {
        List<Suggestion> suggestions = new ArrayList<>();
        String summary = "This is sample feedback from demo mode, so it isn't based on a real AI review of your resume.";

        if (!resume.isPdf() && resume.text() != null) {
            String text = resume.text();
            int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
            summary += " Your resume has about " + words + " words.";
            if (words < 150) {
                suggestions.add(new Suggestion("Content",
                        "Your resume is quite short. Add a line or two about what you did and achieved in each role or project."));
            } else if (words > 900) {
                suggestions.add(new Suggestion("Clarity",
                        "Your resume is long. Aim for one page by cutting older or less relevant details."));
            }
            if (!NUMBER.matcher(text).find()) {
                suggestions.add(new Suggestion("Impact",
                        "Add numbers where you can, such as team size, users, or time saved, so readers can see the scale of your work."));
            }
            if (!EMAIL.matcher(text).find()) {
                suggestions.add(new Suggestion("Content",
                        "Make sure your email address is at the top so recruiters can contact you."));
            }
        }

        suggestions.add(new Suggestion("Impact",
                "Start each bullet point with an action verb (built, led, improved) and end it with the result."));
        suggestions.add(new Suggestion("Organization",
                "Put your most relevant experience for the jobs you're applying to first."));
        suggestions.add(new Suggestion("Formatting",
                "Keep fonts, dates and bullet styles consistent so the resume is easy to scan."));

        return new ResumeFeedback(
                summary,
                List.of("You've uploaded a resume in a format employers accept.",
                        "Getting feedback before applying is a good habit."),
                suggestions);
    }
}
