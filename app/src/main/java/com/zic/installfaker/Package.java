package com.zic.installfaker;

class Package {
    private String pkgName;
    private long creationDate;
    private boolean installed = false;

    Package(String pkgName, long creationDate) {
        this.pkgName = pkgName;
        this.creationDate = creationDate;
    }

    String getPkgName() {
        return pkgName;
    }

    void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
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
