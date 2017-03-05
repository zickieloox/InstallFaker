package com.zic.installfaker.utils;

public class TimeUtils {
    public static long getCurMilliSec() {
        return System.currentTimeMillis();
    }

    public static int getDaysSinceEpoch(long milliSec) {
        return (int) milliSec / (1000 * 60 * 60 * 24);
    }
}
