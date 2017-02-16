package com.zic.installfaker;

class Package {
    private String pkgName;
    private String appName;
    private long creationDate;
    private boolean installed = false;

    Package(String pkgName, String appName, long creationDate) {
        this.pkgName = pkgName;
        this.appName = appName;
        this.creationDate = creationDate;
    }

    String getPkgName() {
        return pkgName;
    }

    String getAppName() {
        return appName;
    }

    public long getCreationDate() {
        return creationDate;
    }

    boolean isInstalled() {
        return installed;
    }

    void setInstalled(boolean installed) {
        this.installed = installed;
    }

    @Override
    public String toString() {
        return pkgName + "|" + creationDate;
    }
}
