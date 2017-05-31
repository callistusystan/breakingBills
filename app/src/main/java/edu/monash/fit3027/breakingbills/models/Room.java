package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Room class for representing a room stored in the database
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android Firebase class models
 *
 * Created by Callistus on 27/4/2017.
 */

@IgnoreExtraProperties
public class Room {
    public String title;
    public String hostUid;
    public long timestamp;
    public long totalAmount;
    public Map<String, Object> members = new HashMap<>();
    public Map<String, Map<String, Object>> memberDetail = new HashMap<>();
    public Map<String, Map<String, Object>> receipts = new HashMap<>();
    public Map<String, Map<String, Object>> payments = new HashMap<>();

    public Room() {
    }

    public Room(String title, String hostUid, long totalAmount) {
        this.title = title;
        this.hostUid = hostUid;
        this.totalAmount = totalAmount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("hostUid", hostUid);
        result.put("totalAmount", totalAmount);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("members", members);
        result.put("memberDetail", memberDetail);
        result.put("receipts", receipts);
        result.put("payments", payments);

        return result;
    }
}