package com.zic.installfaker;

class Package {
    private String pkgName;
    private String appName;
    private long creationTime;
    private String dayCounter;
    private boolean installed = false;

    Package(String pkgName, String appName, long creationTime, String dayCounter) {
        this.pkgName = pkgName;
        this.appName = appName;
        this.creationTime = creationTime;
        this.dayCounter = dayCounter;
    }

    String getPkgName() {
        return pkgName;
    }

    String getAppName() {
        return appName;
    }

    long getCreationTime() {
        return creationTime;
    }

    String getDayCounter() {
        return dayCounter;
    }

    boolean isInstalled() {
        return installed;
    }

    void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public String toString() {
        return pkgName + "|" + creationTime;
    }
}
