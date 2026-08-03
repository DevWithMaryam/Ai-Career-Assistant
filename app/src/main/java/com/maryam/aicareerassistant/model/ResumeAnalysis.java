package com.maryam.aicareerassistant.model;

import java.util.List;

///**
// * Represents a parsed Gemini resume analysis response.
// * Also stored in Firestore under users/{uid}/analysis/{docId}
// */
public class ResumeAnalysis {

    private int score;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private long timestamp;

    public ResumeAnalysis() {}

    public ResumeAnalysis(int score, List<String> strengths,
                          List<String> weaknesses, List<String> suggestions) {
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.suggestions = suggestions;
        this.timestamp = System.currentTimeMillis();
    }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public List<String> getStrengths() { return strengths; }
    public void setStrengths(List<String> strengths) { this.strengths = strengths; }

    public List<String> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<String> weaknesses) { this.weaknesses = weaknesses; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}