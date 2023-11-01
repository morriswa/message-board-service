package org.morriswa.messageboard.util;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

public class Functions {
    public static GregorianCalendar timestampToGregorian(Timestamp timestamp) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp.getTime());
        return cal;
    }
}
