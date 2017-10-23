package com.takipi.oss.storage.caching;

public class StringUtil {
    public static String padRight(String str, int targetLength) {
        return String.format("%1$-" + targetLength + "s", str);
    }

    public static String padLeft(String str, int targetLength) {
        return String.format("%1$" + targetLength + "s", str);
    }
}
