package com.maryam.aicareerassistant.model;

import java.util.Collections;
import java.util.List;

/**
 * Request body structure for Gemini API (generateContent endpoint).
 */
public class GeminiRequest {

    private final List<Content> contents;

    public GeminiRequest(String promptText) {
        Part part = new Part(promptText);
        Content content = new Content(Collections.singletonList(part));
        this.contents = Collections.singletonList(content);
    }

    public List<Content> getContents() { return contents; }

    public static class Content {
        private final List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
        public List<Part> getParts() { return parts; }
    }

    public static class Part {
        private final String text;
        public Part(String text) { this.text = text; }
        public String getText() { return text; }
    }
}