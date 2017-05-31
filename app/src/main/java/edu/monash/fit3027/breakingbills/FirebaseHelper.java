package edu.monash.fit3027.breakingbills;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Payment;
import edu.monash.fit3027.breakingbills.models.Room;
import edu.monash.fit3027.breakingbills.models.User;

import static java.lang.Math.max;

/**
 * A class that contains methods implementing logic to interact with the Firebase database.
 * To ensure only one instance exists, I used the Singleton pattern.
 *
 * Reference: https://en.wikipedia.org/wiki/Singleton_pattern
 *
 * Created by Callistus on 18/5/2017.
 */

public class FirebaseHelper {

    private static FirebaseHelper instance = null;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    private Map<String, User> users = new HashMap<>();
    private Map<String, Room> rooms = new HashMap<>();

    private FirebaseHelper() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        // listen to users in the database
        databaseRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot userDataSnapshot: dataSnapshot.getChildren()) {
                    String userUid = userDataSnapshot.getKey();
                    User user = userDataSnapshot.getValue(User.class);
                    users.put(userUid, user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // listen to rooms in the database
        databaseRef.child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rooms.clear();
                for (DataSnapshot roomDataSnapshot: dataSnapshot.getChildren()) {
                    String roomUid = roomDataSnapshot.getKey();
                    Room room = roomDataSnapshot.getValue(Room.class);
                    rooms.put(roomUid, room);
                }

                System.out.println(rooms);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getUserUid() {
        return auth.getCurrentUser().getUid();
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    /**
     * A helper method to set the cost of a user in a room on Firebase.
     *
     * @param roomUid
     * @param userUid
     * @param newCost
     */
    public void setCost(String roomUid, String userUid, long newCost) {
        Member memberDetail = new Member(rooms.get(roomUid).memberDetail.get(userUid));

        // old cost
        long oldCost = memberDetail.cost;
        long oldOwe = oldCost - memberDetail.amountPaid;
        long newOwe = newCost - memberDetail.amountPaid;

        // specify locations in db to update
        Map<String, Object> childUpdates = new HashMap<>();

        // update in room
        childUpdates.put("rooms/"+roomUid+"/memberDetail/"+userUid+"/cost", newCost);

        // update host's isOwed and user's owe if this is not host
        if (!memberDetail.isHost) {
            String hostUid = rooms.get(roomUid).hostUid;
            childUpdates.put("users/"+userUid+"/owe", users.get(userUid).owe - oldOwe + newOwe);
            childUpdates.put("users/"+hostUid+"/isOwed", users.get(hostUid).isOwed - oldOwe + newOwe);
        }

        // update db
        databaseRef.updateChildren(childUpdates);
    }

    /**
     * A helper method to set the payment in a room on Firebase
     *
     * @param roomUid
     * @param paymentUid
     * @param newPaymentAmount
     */
    public void setPayment(String roomUid, String paymentUid, long newPaymentAmount) {
        Room room = rooms.get(roomUid);
        Payment payment = new Payment(room.payments.get(paymentUid));

        setPayment(roomUid, paymentUid, payment.payerUid, payment.payeeUid, newPaymentAmount, true);
    }

    /**
     * A helper method to set the payment in a room on Firebase
     *
     * @param roomUid
     * @param paymentUid
     * @param payerUid
     * @param payeeUid
     * @param newPaymentAmount
     * @param wantChange
     */
    public void setPayment(String roomUid, String paymentUid, String payerUid, String payeeUid, long newPaymentAmount, boolean wantChange) {
        Room room = rooms.get(roomUid);
        Payment payment;

        if (room.payments.get(paymentUid) == null) {
            payment = new Payment(payerUid, payeeUid, newPaymentAmount);
        } else {
            payment = new Payment(room.payments.get(paymentUid));
            payment.amount = newPaymentAmount;
        }
        payment.wantChange = wantChange;

        // specify locations in db to update
        Map<String, Object> childUpdates = new HashMap<>();

        // update in room
        childUpdates.put("rooms/"+roomUid+"/payments/"+paymentUid, payment.toMap());

        childUpdates.put("rooms/"+roomUid+"/memberDetail/"+payerUid+"/pendingPayments/"+payeeUid, newPaymentAmount);

        // update db
        databaseRef.updateChildren(childUpdates);
    }

    /**
     * A helper method to make a payment in a room on Firebase
     *
     * @param roomUid
     * @param payerUid
     * @param payeeUid
     * @param amount
     * @param wantChange
     */
    public void makePayment(String roomUid, String payerUid, String payeeUid, long amount, boolean wantChange) {
        String paymentUid = databaseRef.child("rooms/"+roomUid+"/payments").push().getKey();
        setPayment(roomUid, paymentUid, payerUid, payeeUid, amount, wantChange);
    }

    /**
     * A helper method to process a payment that has been created in a room on Firebase
     *
     * @param roomUid
     * @param paymentUid
     * @param isConfirmed
     */
    public void processPayment(String roomUid, String paymentUid, Boolean isConfirmed) {
        Room room = rooms.get(roomUid);

        // update payment
        Payment payment = new Payment(room.payments.get(paymentUid));
        payment.hasResponded = true;
        payment.isConfirmed = isConfirmed;

        // get payer and payee uid
        String payerUid = payment.payerUid;
        String payeeUid = payment.payeeUid;

        // specify locations in db to update
        Map<String, Object> childUpdates = new HashMap<>();

        Member payerMember = new Member(room.memberDetail.get(payerUid));
        Member payeeMember = new Member(room.memberDetail.get(payeeUid));

        User payerUser = users.get(payerUid);
        User payeeUser = users.get(payeeUid);

        // update the payment in room
        childUpdates.put("rooms/"+roomUid+"/payments/"+paymentUid, payment.toMap());

        // update payer's pending payments
        childUpdates.put("rooms/"+roomUid+"/memberDetail/"+payerUid+"/pendingPayments/"+payeeUid, null);

        if (isConfirmed) {
            // see how much payer owed
            long payerOwedAmount = payerMember.cost - payerMember.amountPaid;

            // see how much payer paid
            long payerPaidAmount = payment.amount;

            // if payer paid overpaid and doesnt want change, set payerPaidAmount to the owed amount
            if (payerPaidAmount > payerOwedAmount && !payment.wantChange) {
                payerPaidAmount = payerOwedAmount;
            }

            // update payerMember's amount paid
            childUpdates.put("rooms/"+roomUid+"/memberDetail/"+payerUid+"/amountPaid", payerMember.amountPaid + payerPaidAmount);

            // update payeeMember's amountPaid
            childUpdates.put("rooms/"+roomUid+"/memberDetail/"+payeeUid+"/amountPaid", payeeMember.amountPaid - payerPaidAmount);

            boolean doesPayeeOwePayer = payerPaidAmount > payerOwedAmount;

            // update payer's owed
            payerUser.owe = max(payerUser.owe - payerPaidAmount, 0);

            // update payee's isOwed
            payeeUser.isOwed = max(payeeUser.isOwed - payerPaidAmount, 0);

            // this part updates database if payee owes the payer
            if (doesPayeeOwePayer) {
                long owedAmount = payerPaidAmount - payerOwedAmount;

                // update payeeUser's owe
                payeeUser.owe = payeeUser.owe + owedAmount;

                // update payerUser's isOwed
                payerUser.isOwed = payerUser.isOwed + owedAmount;
            }
            childUpdates.put("users/"+payerUid, payerUser.toMap());
            childUpdates.put("users/"+payeeUid, payeeUser.toMap());
        }

        // update db
        databaseRef.updateChildren(childUpdates);
    }
}
