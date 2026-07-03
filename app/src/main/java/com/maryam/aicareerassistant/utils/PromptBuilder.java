package com.maryam.aicareerassistant.utils;

/**
 * Builds structured prompts for every Gemini API call in the app.
 * Keeping all prompts in one place makes them easy to tune/improve.
 */
public class PromptBuilder {

    /**
     * Resume Analysis prompt — returns JSON with score, strengths,
     * weaknesses, and suggestions.
     */
    public static String buildResumeAnalysisPrompt(String resumeText) {
        return "You are an expert resume reviewer and career coach. " +
                "Analyze the following resume and respond ONLY with a valid JSON object " +
                "(no markdown, no extra text, no code blocks). " +
                "The JSON must have exactly these fields:\n" +
                "{\n" +
                "  \"score\": <integer 0-100>,\n" +
                "  \"strengths\": [<string>, ...],\n" +
                "  \"weaknesses\": [<string>, ...],\n" +
                "  \"suggestions\": [<string>, ...]\n" +
                "}\n\n" +
                "Resume:\n" + resumeText;
    }

    /**
     * Skill Gap prompt — compares resume skills against target career.
     */
    public static String buildSkillGapPrompt(String resumeText, String targetCareer) {
        return "You are a career advisor. " +
                "Compare the skills in the resume below against the requirements for a " +
                targetCareer + " role. " +
                "Respond ONLY with a valid JSON object (no markdown, no extra text):\n" +
                "{\n" +
                "  \"existing_skills\": [<string>, ...],\n" +
                "  \"missing_skills\": [<string>, ...],\n" +
                "  \"priority_skills\": [<string>, ...]\n" +
                "}\n\n" +
                "Resume:\n" + resumeText;
    }

    /**
     * Learning Roadmap prompt — generates a weekly plan.
     */
    public static String buildRoadmapPrompt(String targetCareer, String missingSkills) {
        return "You are a learning coach. Create a 4-week learning roadmap " +
                "for someone who wants to become a " + targetCareer + ". " +
                "They need to learn: " + missingSkills + ". " +
                "Respond ONLY with a valid JSON object (no markdown, no extra text):\n" +
                "{\n" +
                "  \"weeks\": [\n" +
                "    {\n" +
                "      \"week\": <integer>,\n" +
                "      \"focus\": <string>,\n" +
                "      \"tasks\": [<string>, ...],\n" +
                "      \"resources\": [<string>, ...]\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    /**
     * Interview Questions prompt.
     */
    public static String buildInterviewPrompt(String targetCareer, String resumeText) {
        return "You are a senior interviewer. Generate interview questions " +
                "for a " + targetCareer + " candidate based on their resume. " +
                "Respond ONLY with a valid JSON object (no markdown, no extra text):\n" +
                "{\n" +
                "  \"technical_questions\": [<string>, ...],\n" +
                "  \"hr_questions\": [<string>, ...],\n" +
                "  \"resume_based_questions\": [<string>, ...]\n" +
                "}\n\n" +
                "Resume:\n" + resumeText;
    }
}