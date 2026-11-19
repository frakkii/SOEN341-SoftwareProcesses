package com.example.demo.service;

import com.example.demo.dto.ResumeOptimization;
import com.example.demo.dto.ResumeOptimization.Line;
import com.example.demo.exception.ApplicationException;
import com.example.demo.model.Resume;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.service.DocxWriter.Paragraph;
import com.example.demo.service.DocxWriter.Style;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Line-by-line AI suggestions for the job seeker's current resume. The job seeker accepts or
// rejects each suggestion and fills in any figures the AI left as placeholders (like [X%]).
// The accepted lines are put together into an improved resume (DOCX), alongside a report of
// every suggestion. The improved resume can also be saved as a new resume version.
@Service
public class ResumeOptimizationService {

    public static final String NOTICE =
            "These suggestions are AI-generated. Check every line before you use it; the AI can be wrong or miss context.";

    // Far more than a real resume has; a longer reply means something went wrong
    static final int MAX_LINES = 400;
    // Each line is short; this stops a runaway reply from filling the session
    static final int MAX_LINE_CHARS = 1_000;
    static final int MAX_FIGURE_CHARS = 60;
    // A figure for the job seeker to fill in, e.g. [X%] or [number of users]
    private static final Pattern PLACEHOLDER = Pattern.compile("\\[([^\\[\\]\\r\\n]{1," + MAX_FIGURE_CHARS + "})]");
    private static final Set<String> KINDS = Set.of("heading", "bullet", "text");
    private static final Pattern BULLET_MARK = Pattern.compile("^[-*•▪●◦]\\s*");
    private static final int MAX_FILE_NAME = 255;

    private final ResumeRepository resumes;
    private final ResumeAdvisor advisor;

    public ResumeOptimizationService(ResumeRepository resumes, ResumeAdvisor advisor) {
        this.resumes = resumes;
        this.advisor = advisor;
    }

    // The result kept in the job seeker's session until they download or save it
    public record StoredOptimization(Integer personId, Long resumeId, String fileName, boolean demo,
                                     ResumeOptimization optimization) implements Serializable {
    }

    // One line as the page shows it. index is what the page sends back to accept a suggestion;
    // figures are the placeholders in the suggestion, in order, for the job seeker to fill in.
    public record LineView(int index, String kind, String original, String improved, String area, String reason,
                           boolean suggested, List<String> figures) {
    }

    public record OptimizationView(String fileName, boolean demo, String notice, String summary,
                                   int suggestionCount, List<LineView> lines) {
    }

    // What the job seeker chose: the accepted suggestions, and the figures typed into each one
    public record Choices(Collection<Integer> accepted, Map<Integer, List<String>> figures) {
        public Choices {
            accepted = accepted == null ? List.of() : accepted;
            figures = figures == null ? Map.of() : figures;
        }
    }

    public record GeneratedFile(String fileName, byte[] data) {
    }

    public StoredOptimization optimizeCurrentResume(Integer personId) {
        Resume resume = resumes.findFirstByPersonIdAndCurrentTrueOrderByUploadedAtDesc(personId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,
                        "Upload a resume first, then ask for suggestions."));

        ResumeOptimization raw = advisor.optimize(ResumeContentReader.read(resume));
        return new StoredOptimization(personId, resume.getId(), resume.getFileName(), advisor.isDemo(), clean(raw));
    }

    public OptimizationView view(StoredOptimization stored) {
        List<Line> lines = stored.optimization().lines();
        List<LineView> views = new ArrayList<>(lines.size());
        int suggestions = 0;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            boolean suggested = isSuggestion(line);
            suggestions += suggested ? 1 : 0;
            views.add(new LineView(i, line.kind(), line.original(), line.improved(), line.area(), line.reason(),
                    suggested, suggested ? placeholders(line) : List.of()));
        }
        return new OptimizationView(stored.fileName(), stored.demo(), NOTICE, stored.optimization().summary(),
                suggestions, views);
    }

    // The resume with the accepted suggestions in place. Every figure in them must be filled in.
    public GeneratedFile improvedResume(StoredOptimization stored, Choices choices) {
        Map<Integer, String> accepted = acceptedText(stored, choices, true);
        List<Line> lines = stored.optimization().lines();
        List<Paragraph> paragraphs = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            String text = accepted.getOrDefault(i, line.original());
            if (text.isBlank()) {
                continue;   // a suggested new line that wasn't accepted
            }
            Style style = switch (line.kind()) {
                case "heading" -> Style.HEADING;
                case "bullet" -> Style.BULLET;
                default -> Style.TEXT;
            };
            paragraphs.add(Paragraph.of(style, text));
        }
        return new GeneratedFile(outputName(stored.fileName(), "-improved"), DocxWriter.write(paragraphs));
    }

    // Every suggestion with its reason and whether it was accepted. Figures not filled in yet
    // stay as placeholders, so the report can be downloaded at any point.
    public GeneratedFile suggestionsReport(StoredOptimization stored, Choices choices) {
        Map<Integer, String> accepted = acceptedText(stored, choices, false);
        List<Line> lines = stored.optimization().lines();
        long total = lines.stream().filter(ResumeOptimizationService::isSuggestion).count();

        List<Paragraph> p = new ArrayList<>();
        p.add(Paragraph.of(Style.TITLE, "Resume suggestions"));
        p.add(Paragraph.of(Style.NOTE, "For " + stored.fileName() + " · " + LocalDate.now()
                + (stored.demo() ? " · Demo mode: sample suggestions, not a real AI review" : "")));
        p.add(Paragraph.of(Style.TEXT, stored.optimization().summary()));
        p.add(Paragraph.of(Style.TEXT, accepted.size() + " of " + total + " suggestions accepted."));

        int number = 0;
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            if (!isSuggestion(line)) {
                continue;
            }
            number++;
            String area = line.area().isBlank() ? "" : " · " + line.area();
            p.add(Paragraph.of(Style.HEADING, "Suggestion " + number + area + " · "
                    + (accepted.containsKey(i) ? "Accepted" : "Not accepted")));
            p.add(Paragraph.of(Style.TEXT, "Original: " + (line.original().isBlank() ? "(new line)" : line.original())));
            p.add(Paragraph.of(Style.TEXT, "Suggested: " + accepted.getOrDefault(i, line.improved())));
            if (!line.reason().isBlank()) {
                p.add(Paragraph.of(Style.TEXT, "Why: " + line.reason()));
            }
        }
        if (total == 0) {
            p.add(Paragraph.of(Style.TEXT, "The AI had no line-by-line changes to suggest for this resume."));
        }
        p.add(Paragraph.of(Style.NOTE, NOTICE));
        return new GeneratedFile(outputName(stored.fileName(), "-suggestions"), DocxWriter.write(p));
    }

    // Stores the improved resume as the job seeker's new current version; earlier versions are kept
    @Transactional
    public GeneratedFile saveAsNewVersion(Integer personId, StoredOptimization stored, Choices choices) {
        GeneratedFile file = improvedResume(stored, choices);
        resumes.retireCurrent(personId);
        resumes.save(new Resume(personId, file.fileName(), DocxWriter.CONTENT_TYPE, file.data()));
        return file;
    }

    static boolean isSuggestion(Line line) {
        return !line.improved().isBlank() && !line.improved().equals(line.original());
    }

    // Placeholders the AI added to its suggestion. Brackets already in the original line are
    // the job seeker's own text, not figures to fill in.
    static List<String> placeholders(Line line) {
        List<String> labels = new ArrayList<>();
        Matcher m = PLACEHOLDER.matcher(line.improved());
        while (m.find()) {
            if (!line.original().contains(m.group())) {
                labels.add(m.group(1).strip());
            }
        }
        return labels;
    }

    // The final text of each accepted suggestion, with the job seeker's figures filled in.
    // Only real suggestions can be accepted; anything else means the page is out of date.
    private static Map<Integer, String> acceptedText(StoredOptimization stored, Choices choices, boolean requireFigures) {
        List<Line> lines = stored.optimization().lines();
        Map<Integer, String> accepted = new HashMap<>();
        for (Integer index : choices.accepted()) {
            if (index == null || index < 0 || index >= lines.size() || !isSuggestion(lines.get(index))) {
                throw new ApplicationException("Those suggestions don't match your latest results. Ask for suggestions again.");
            }
            accepted.put(index, fillFigures(lines.get(index), choices.figures().get(index), requireFigures));
        }
        return accepted;
    }

    private static String fillFigures(Line line, List<String> given, boolean required) {
        List<String> labels = placeholders(line);
        List<String> values = given == null ? List.of() : given;
        if (values.size() > labels.size()) {
            throw new ApplicationException("Those figures don't match your latest results. Ask for suggestions again.");
        }

        StringBuilder out = new StringBuilder();
        Matcher m = PLACEHOLDER.matcher(line.improved());
        int figure = 0;
        while (m.find()) {
            if (line.original().contains(m.group())) {
                m.appendReplacement(out, Matcher.quoteReplacement(m.group()));
                continue;
            }
            String value = figure < values.size() ? figureValue(values.get(figure)) : "";
            if (value.isEmpty() && required) {
                throw new ApplicationException("Fill in \"" + labels.get(figure) + "\" in the suggestion \""
                        + line.improved() + "\", or reject that suggestion.");
            }
            m.appendReplacement(out, Matcher.quoteReplacement(value.isEmpty() ? m.group() : value));
            figure++;
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String figureValue(String value) {
        String figure = value == null ? "" : value.strip();
        if (figure.length() > MAX_FIGURE_CHARS || figure.chars().anyMatch(c -> c == '[' || c == ']' || Character.isISOControl(c))) {
            throw new ApplicationException("Each figure can be up to " + MAX_FIGURE_CHARS
                    + " characters, without square brackets or line breaks.");
        }
        return figure;
    }

    // Trims the AI's reply into the shape the rest of the code relies on: no nulls, a known
    // kind, no bullet symbols, and no rewrite text on lines that have no rewrite
    private static ResumeOptimization clean(ResumeOptimization raw) {
        if (raw == null || raw.lines() == null || raw.lines().isEmpty()) {
            throw unavailable("The AI didn't return any lines. Please try again.");
        }
        if (raw.lines().size() > MAX_LINES) {
            throw unavailable("The AI's answer was too long to use. Please try again.");
        }
        List<Line> lines = new ArrayList<>();
        for (Line line : raw.lines()) {
            if (line == null) {
                continue;
            }
            String kind = text(line.kind()).toLowerCase(Locale.ROOT);
            if (!KINDS.contains(kind)) {
                kind = "text";
            }
            String original = text(line.original());
            String improved = text(line.improved());
            if ("bullet".equals(kind)) {
                original = BULLET_MARK.matcher(original).replaceFirst("");
                improved = BULLET_MARK.matcher(improved).replaceFirst("");
            }
            if (original.isEmpty() && improved.isEmpty()) {
                continue;
            }
            Line cleaned = new Line(kind, original, improved, text(line.area()), text(line.reason()));
            lines.add(isSuggestion(cleaned) ? cleaned : new Line(kind, original, "", "", ""));
        }
        if (lines.isEmpty()) {
            throw unavailable("The AI didn't return any lines. Please try again.");
        }
        return new ResumeOptimization(text(raw.summary()), List.copyOf(lines));
    }

    private static String text(String value) {
        if (value == null) {
            return "";
        }
        String stripped = value.strip();
        return stripped.length() > MAX_LINE_CHARS ? stripped.substring(0, MAX_LINE_CHARS) : stripped;
    }

    // e.g. "Jane CV.pdf" -> "Jane CV-improved.docx", kept within the 255-character upload limit
    static String outputName(String fileName, String suffix) {
        String base = fileName == null ? "resume" : fileName.strip();
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        if (base.isBlank()) {
            base = "resume";
        }
        int room = MAX_FILE_NAME - suffix.length() - ".docx".length();
        if (base.length() > room) {
            base = base.substring(0, room);
        }
        return base + suffix + ".docx";
    }

    private static ApplicationException unavailable(String message) {
        return new ApplicationException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
