package com.akram.prioritymatrix.ui.report;

import android.graphics.drawable.Drawable;

public class AppUsage {

    private String appTitle;
    private Long appTime;
    private Drawable appIcon;

    public AppUsage(String appTitle, Long appTime, Drawable appIcon) {
        this.appTitle = appTitle;
        this.appTime = appTime;
        this.appIcon = appIcon;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public Long getAppTime() {
        return appTime;
    }

    public void setAppTime(Long appTime) {
        this.appTime = appTime;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
}
