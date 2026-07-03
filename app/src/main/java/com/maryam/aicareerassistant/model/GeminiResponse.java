package com.maryam.aicareerassistant.model;

import java.util.List;

/**
 * Parses the JSON response returned by Gemini API generateContent endpoint.
 * Only fields we actually use are declared — Gson ignores the rest.
 */
public class GeminiResponse {

    private List<Candidate> candidates;

    public List<Candidate> getCandidates() { return candidates; }

    public static class Candidate {
        private Content content;
        public Content getContent() { return content; }
    }

    public static class Content {
        private List<Part> parts;
        public List<Part> getParts() { return parts; }
    }

    public static class Part {
        private String text;
        public String getText() { return text; }
    }

    /**
     * Convenience method — extracts the first text result from the response.
     * Returns null if response structure is empty or malformed.
     */
    public String getFirstText() {
        if (candidates == null || candidates.isEmpty()) return null;
        Candidate candidate = candidates.get(0);
        if (candidate.getContent() == null) return null;
        List<Part> parts = candidate.getContent().getParts();
        if (parts == null || parts.isEmpty()) return null;
        return parts.get(0).getText();
    }
}