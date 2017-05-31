package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Member class used to represent users in a room
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android Firebase class models
 *
 * Created by Callistus on 29/4/2017.
 */

@IgnoreExtraProperties
public class Member {

    public String nickname;
    public boolean isHost;
    public Map<String, Long> pendingPayments;
    public long timestamp;
    public long cost = 0;
    public long amountPaid = 0;

    public Member() {
    }

    public Member(String nickname, boolean isHost) {
        this.nickname = nickname;
        this.isHost = isHost;
    }

    public Member(Map<String, Object> memberDetail) {
        this.nickname = (String) memberDetail.get("nickname");
        this.isHost = (Boolean) memberDetail.get("isHost");
        this.pendingPayments = (Map<String, Long>) memberDetail.get("pendingPayments");
        this.timestamp = (long) memberDetail.get("timestamp");
        this.cost = (long) memberDetail.get("cost");
        this.amountPaid = (long) memberDetail.get("amountPaid");
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("nickname", nickname);
        result.put("isHost", isHost);
        result.put("pendingPayments", pendingPayments);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("cost", cost);
        result.put("amountPaid", amountPaid);

        return result;
    }

    /**
     * Method to get the pending amount
     * @return the amount paid by this member, but yet to be confirmed
     */
    public long getPendingAmount() {
        long pendingAmount = 0;
        for (long amount : pendingPayments.values()) {
            pendingAmount += amount;
        }
        return pendingAmount;
    }
}
