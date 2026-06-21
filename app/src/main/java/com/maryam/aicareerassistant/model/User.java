package com.maryam.aicareerassistant.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a User document stored in Firestore at "users/{userId}".
 */
public class User {

    private String userId;
    private String name;
    private String email;
    private String targetCareer;
    private String resumeUrl;

    public User() {
        // Required empty constructor for Firestore deserialization
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.targetCareer = "";
        this.resumeUrl = "";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("targetCareer", targetCareer == null ? "" : targetCareer);
        map.put("resumeUrl", resumeUrl == null ? "" : resumeUrl);
        return map;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTargetCareer() { return targetCareer; }
    public void setTargetCareer(String targetCareer) { this.targetCareer = targetCareer; }

    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
}