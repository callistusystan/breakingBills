package edu.monash.fit3027.breakingbills;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.monash.fit3027.breakingbills.models.Member;

import static java.lang.Math.abs;

/**
 * A class containing helper methods for utility functions like getting the month and year from a
 * UNIX long timestamp, conversion of currencies, and member statuses
 *
 * Created by Callistus on 5/5/2017.
 */

public class Utils {

    /**
     * A helper method to get the month year from a UNIX long timestamp
     *
     * @param timestamp
     * @return a string in the format MMMM yyyy, e.g. May 2017
     */
    public static String getMonthYear(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sfd = new SimpleDateFormat("MMMM yyyy");
        String monthYear = sfd.format(date);

        return monthYear;
    }

    /**
     * A helper method to get the string currency representation of a long
     *
     * @param amount
     * @return a string representing the amount, e.g. $1.00
     */
    public static String convertLongToStringCurrency(long amount) {
        return String.format("$%d.%02d", amount/100, abs(amount)%100);
    }

    /**
     * A helper method to get the amount in long from a string currency
     *
     * @param amount
     * @return a long representing the amount in cents, e.g. $1.00 => 100
     */
    public static long convertStringToLongCurrency(String amount) {
        BigDecimal amountInDecimal = new BigDecimal(amount);
        long amountInLong = amountInDecimal.multiply(new BigDecimal(100)).longValue();

        return amountInLong;
    }

    /**
     * A helper method to determine the status of a member, based on their cost and amount paid
     *
     * @param member
     * @return a String representing the member's status
     */
    public static String determineMemberStatus(Member member) {
        // get the cost and amount paid
        long cost = member.cost;
        long amountPaid = member.amountPaid;

        // handle base cases
        if (cost == 0) return "No status";
        else if (cost == amountPaid) return "Payment settled";

        // check if any payment is pending
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

    /**
     * Method overloading to allow a Map as an argument
     *
     * @param memberDetail
     * @return a String representing the member's status
     */
    public static String determineMemberStatus(Map<String, Object> memberDetail) {
        Member member = new Member(memberDetail);
        return determineMemberStatus(member);
    }
}
