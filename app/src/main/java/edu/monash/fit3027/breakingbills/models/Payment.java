package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Payment class used to represent payments made in a room
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android Firebase class models
 *
 * Created by Callistus on 27/4/2017.
 */

@IgnoreExtraProperties
public class Payment {
    public String payerUid;
    public String payeeUid;

    public long amount;
    public boolean hasResponded = false;
    public boolean isConfirmed = false;
    public boolean wantChange = true;
    public long timestamp;

    public Payment() {
    }

    public Payment(String payerUid, String payeeUid, long amount) {
        this.payerUid = payerUid;
        this.payeeUid = payeeUid;
        this.amount = amount;
    }

    public Payment(Map<String, Object> payment) {
        this.payerUid = (String) payment.get("payerUid");
        this.payeeUid = (String) payment.get("payeeUid");
        this.amount = (long) payment.get("amount");
        this.hasResponded = (boolean) payment.get("hasResponded");
        this.isConfirmed = (boolean) payment.get("isConfirmed");
        this.wantChange = (boolean) payment.get("wantChange");
        this.timestamp = (long) payment.get("timestamp");
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("payerUid", payerUid);
        result.put("payeeUid", payeeUid);
        result.put("amount", amount);
        result.put("hasResponded", hasResponded);
        result.put("isConfirmed", isConfirmed);
        result.put("wantChange", wantChange);
        result.put("timestamp", ServerValue.TIMESTAMP);

        return result;
    }
}