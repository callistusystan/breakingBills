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
public class Payment {
    public String payerUid;
    public String payeeUid;

    public long amount;
    public boolean hasResponded = false;
    public boolean isConfirmed = false;
    public long timestamp;

    public Payment() {
    }

    public Payment(String payerUid, String payeeUid, long amount) {
        this.payerUid = payerUid;
        this.payeeUid = payeeUid;
        this.amount = amount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("payerUid", payerUid);
        result.put("payeeUid", payeeUid);
        result.put("amount", amount);
        result.put("hasResponded", hasResponded);
        result.put("isConfirmed", isConfirmed);
        result.put("timestamp", ServerValue.TIMESTAMP);

        return result;
    }
}