package org.morriswa.messageboard.util;

import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.Objects;

public class Functions {
    public static GregorianCalendar timestampToGregorian(Timestamp timestamp) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp.getTime());
        return cal;
    }

    public static String blobTypeToMyType(String blobType) {
        return blobType.substring(
                blobType.indexOf("/") + 1
        );
    }
}
