package org.morriswa.messageboard.util;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

public class Functions {
    public static GregorianCalendar timestampToGregorian(Timestamp timestamp) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp.getTime());
        return cal;
    }

    public static String blobTypeToImageFormat(String blobType) {
        final int del = blobType.indexOf("/");
        final String prefix = blobType.substring(0, del);
        final String format = blobType.substring(del + 1);

        if (!prefix.equals("image")) throw new RuntimeException("expected image file, got some other format");

        return format;
    }
}
