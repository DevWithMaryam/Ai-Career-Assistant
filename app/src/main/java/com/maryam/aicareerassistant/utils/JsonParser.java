package com.maryam.aicareerassistant.utils;

import com.maryam.aicareerassistant.model.ResumeAnalysis;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses raw JSON strings returned by Gemini into model objects.
 * Gemini sometimes wraps JSON in markdown code blocks — we strip those first.
 */
public class JsonParser {

    /**
     * Strips markdown code fences like ```json ... ``` that Gemini sometimes adds.
     */
    public static String cleanJson(String raw) {
        if (raw == null) return "";
        return raw
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    public static ResumeAnalysis parseResumeAnalysis(String jsonString) throws Exception {
        String clean = cleanJson(jsonString);
        JSONObject obj = new JSONObject(clean);

        int score = obj.optInt("score", 0);
        List<String> strengths = parseArray(obj.optJSONArray("strengths"));
        List<String> weaknesses = parseArray(obj.optJSONArray("weaknesses"));
        List<String> suggestions = parseArray(obj.optJSONArray("suggestions"));

        return new ResumeAnalysis(score, strengths, weaknesses, suggestions);
    }

    public static List<String> parseStringList(String jsonString, String key) throws Exception {
        String clean = cleanJson(jsonString);
        JSONObject obj = new JSONObject(clean);
        return parseArray(obj.optJSONArray(key));
    }

    private static List<String> parseArray(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            list.add(array.optString(i, ""));
        }
        return list;
    }
}