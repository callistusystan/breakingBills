package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Callistus on 3/5/2017.
 */
@IgnoreExtraProperties
public class Receipt {

    public String uri;
    public long timestamp;

    public Receipt() {
    }

    public Receipt(String uri, long timestamp) {
        this.uri = uri;
        this.timestamp = timestamp;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uri", uri);
        result.put("timestamp", ServerValue.TIMESTAMP);

        return result;
    }
}
