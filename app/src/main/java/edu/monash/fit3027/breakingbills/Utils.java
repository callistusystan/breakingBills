package edu.monash.fit3027.breakingbills;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.abs;

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

    public static String convertLongToStringCurrency(long amount) {
        return String.format("$%d.%02d", abs(amount)/100, abs(amount)%100);
    }

    public static long convertStringToLongCurrency(String amount) {
        BigDecimal amountInDecimal = new BigDecimal(amount);
        long amountInLong = amountInDecimal.multiply(new BigDecimal(100)).longValue();

        return amountInLong;
    }
}
