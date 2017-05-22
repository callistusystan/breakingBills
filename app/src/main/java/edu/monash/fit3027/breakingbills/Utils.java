package edu.monash.fit3027.breakingbills;

import android.graphics.Color;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.monash.fit3027.breakingbills.models.Member;

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
        return String.format("$%d.%02d", amount/100, abs(amount)%100);
    }

    public static long convertStringToLongCurrency(String amount) {
        BigDecimal amountInDecimal = new BigDecimal(amount);
        long amountInLong = amountInDecimal.multiply(new BigDecimal(100)).longValue();

        return amountInLong;
    }

    public static String determineMemberStatus(Member member) {
        long cost = member.cost;
        long amountPaid = member.amountPaid;

        if (cost == 0) return "No status";
        else if (cost == amountPaid) return "Payment settled";

        boolean isPaymentPending = member.pendingPayments != null;
        long netOwedAmount = cost - amountPaid;
        String newStatus = "";

        if (isPaymentPending || netOwedAmount > 0) {
            newStatus += "Owes " + Utils.convertLongToStringCurrency(netOwedAmount) + ", ";
        }
        newStatus += "Paid " + Utils.convertLongToStringCurrency(amountPaid);
        if (isPaymentPending) {
            return newStatus + "\n" + Utils.convertLongToStringCurrency(member.getPendingAmount()) + " pending";
        }
        if (netOwedAmount < 0) {
            newStatus += ", Is Owed " + Utils.convertLongToStringCurrency(-1*netOwedAmount);
        }
        return newStatus;
    }

    public static String determineMemberStatus(Map<String, Object> memberDetail) {
        Member member = new Member(memberDetail);
        return determineMemberStatus(member);
    }
}
