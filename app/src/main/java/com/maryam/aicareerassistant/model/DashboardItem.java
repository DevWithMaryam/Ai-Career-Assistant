package com.maryam.aicareerassistant.model;

/**
 * Represents a single navigation card on the Home Dashboard
 * (e.g. "Resume Analysis", "Skill Gap", etc.)
 */
public class DashboardItem {

    private final String key;
    private final String title;
    private final String subtitle;
    private final int iconRes;
    private final int iconTintColorRes;

    public DashboardItem(String key, String title, String subtitle, int iconRes, int iconTintColorRes) {
        this.key = key;
        this.title = title;
        this.subtitle = subtitle;
        this.iconRes = iconRes;
        this.iconTintColorRes = iconTintColorRes;
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public int getIconRes() { return iconRes; }
    public int getIconTintColorRes() { return iconTintColorRes; }
}