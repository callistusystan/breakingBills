package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Callistus on 27/4/2017.
 */

@IgnoreExtraProperties
public class Room {
    public String title;
    public String hostUid;
    public long timestamp;
    public Map<String, Boolean> members = new HashMap<>();
    public Map<String, Map<String, Object>> memberDetail = new HashMap<>();
    public Map<String, Object> receipts = new HashMap<>();

    public Room() {
    }

    public Room(String title, String hostUid, long timestamp) {
        this.title = title;
        this.hostUid = hostUid;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("hostUid", hostUid);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("members", members);
        result.put("memberDetail", memberDetail);
        result.put("receipts", receipts);

        return result;
    }
}