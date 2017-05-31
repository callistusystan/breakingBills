package edu.monash.fit3027.breakingbills.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * User class for representing a user in the database
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android Firebase class models
 *
 * Created by Callistus on 27/4/2017.
 */

@IgnoreExtraProperties
public class User {
    public long owe;
    public long isOwed;

    public User() {
    }

    public User(long owe, long isOwed) {
        this.owe = owe;
        this.isOwed = isOwed;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("owe", owe);
        result.put("isOwed", isOwed);

        return result;
    }
}