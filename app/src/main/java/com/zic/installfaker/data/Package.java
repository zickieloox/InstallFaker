package com.zic.installfaker.data;

public class Package {
    private String pkgName;
    private String appName;
    private long creationTime;
    private String dayCounter;
    private boolean installed = false;

    public Package(String pkgName, String appName, long creationTime, String dayCounter) {
        this.pkgName = pkgName;
        this.appName = appName;
        this.creationTime = creationTime;
        this.dayCounter = dayCounter;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getAppName() {
        return appName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getDayCounter() {
        return dayCounter;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public String toString() {
        return pkgName + "|" + creationTime;
    }
}
