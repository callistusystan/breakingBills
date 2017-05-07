package edu.monash.fit3027.breakingbills;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Callistus on 5/5/2017.
 */

public class Utils {
    public static String getMonthYear(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sfd = new SimpleDateFormat("MMMM yyyy");
        String monthYear = sfd.format(date);

        return monthYear;
    }
}
