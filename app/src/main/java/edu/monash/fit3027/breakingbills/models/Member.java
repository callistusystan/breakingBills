package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Callistus on 29/4/2017.
 */

@IgnoreExtraProperties
public class Member {

    public final static String NO_STATUS = "No status.";
    public final static String PAYMENT_SETTLED = "Payment settled.";
    public final static String PAID_CHANGE = "Paid, but expecting change.";
    public final static String PAID_CONFIRM = "Paid, but waiting for confirmation.";
    public final static String EXPECTING_PAYMENT = "Expecting payment.";

    public String nickname;
    public boolean isHost;
    public boolean hasPaid;
    public String status;
    public long timestamp;
    public int amount;

    public Member() {
    }

    public Member(String nickname, boolean isHost, boolean hasPaid, String status, long timestamp, int amount) {
        this.nickname = nickname;
        this.isHost = isHost;
        this.hasPaid = hasPaid;
        this.status = status;
        this.timestamp = timestamp;
        this.amount = amount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("nickname", nickname);
        result.put("isHost", isHost);
        result.put("hasPaid", hasPaid);
        result.put("status", status);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("amount", -1);

        return result;
    }
}