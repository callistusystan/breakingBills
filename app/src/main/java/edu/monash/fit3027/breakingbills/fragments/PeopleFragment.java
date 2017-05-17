package edu.monash.fit3027.breakingbills.fragments;

/**
 * Created by Callistus on 30/4/2017.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Payment;
import edu.monash.fit3027.breakingbills.viewholders.MemberViewHolder;

import static edu.monash.fit3027.breakingbills.Utils.convertStringToLongCurrency;

public class PeopleFragment extends RoomFragment implements View.OnClickListener {

    // firebase components
    private DatabaseReference databaseRef;

    // recycler view
    private RecyclerView membersRecyclerView;
    private LinearLayoutManager mManager;

    // button
    private FloatingActionButton addButton;

    // necessary variables
    private String roomUid;
    private String currentUserUid;

    public PeopleFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRoomActivity((RoomActivity) getActivity());

        View rootView = inflater.inflate(R.layout.fragment_all_people, container, false);

        // init firebase components
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // link views
        membersRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_all_people_recyclerView);
        membersRecyclerView.setHasFixedSize(true);

        addButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_all_people_addButton);
        addButton.setOnClickListener(this);

        // init necessary variables
        roomUid = getRoomActivity().getRoomUid();
        currentUserUid = getRoomActivity().getCurrentUserUid();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init views
        initViews();
    }

    public void initViews() {
        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        membersRecyclerView.setLayoutManager(mManager);

        initMembersRecyclerView();
    }

    public boolean isHost(String memberUid) {
        String hostUid = getRoomActivity().getHostUid();
        return memberUid.equals(hostUid);
    }

    public boolean isHost() {
        return isHost(currentUserUid);
    }

    public void initMembersRecyclerView() {
        // show progress dialog
        getRoomActivity().showProgressDialog();

        // Set up FirebaseRecyclerAdapter with the Query
        Query membersQuery = getMembersQuery();

        FirebaseRecyclerAdapter<Member, MemberViewHolder> recyclerAdapter =
            new FirebaseRecyclerAdapter<Member, MemberViewHolder>
                    (Member.class, R.layout.item_member, MemberViewHolder.class, membersQuery) {
                @Override
                protected void populateViewHolder(MemberViewHolder viewHolder, final Member model, int position) {
                    final DatabaseReference memberRef = getRef(position);

                    // Set click listener for the whole room view
                    final String memberUid = memberRef.getKey();

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // init the alert dialog builder
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            // set the layout for the alert dialog
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.payment_dialog, null);

                            // get views
                            final TextView payment_dialog_label = (TextView) dialogView.findViewById(R.id.payment_dialog_label);
                            final EditText payment_dialog_amountEditText = (EditText) dialogView.findViewById(R.id.payment_dialog_amountEditText);

                            Map<String, Boolean> myPendingPayments = (Map<String, Boolean>) getRoom().memberDetail.get(getCurrentUserUid()).get("pendingPayments");

                            // if you are the host, only a dialog if you owe them change
                            // if you are a member, and clicking on the host, make it MAKE payment
                            // if you are a member, and clicking on yourself, make it SET cost
                            if ((isHost() && model.cost < 0) || (!isHost() && isHost(memberUid))) {
                                // show dialog if already have a pending payment
                                if (myPendingPayments != null && myPendingPayments.containsKey(memberUid)) {
                                    getRoomActivity().showMessageDialog(R.layout.message_dialog,
                                                                        "Oops!",
                                                                        "You already have a payment pending to this user! Go to the payments tab to edit it!",
                                                                        "Ok",
                                                                        "");
                                    return;
                                }
                                payment_dialog_label.setText("Make payment");
                            } else if (!isHost() && memberUid.equals(getRoomActivity().getCurrentUserUid())) {
                                payment_dialog_label.setText("Set cost");
                            } else
                                return;

                            builder.setView(dialogView)
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });

                            // create and show this dialog
                            final AlertDialog alertDialog = builder.create();

                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialog) {
                                    Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String amount = payment_dialog_amountEditText.getText().toString().trim();

                                            // only process if both fields are not empty
                                            if (!amount.equals("") && Utils.convertStringToLongCurrency(amount) > 0){
                                                // if you are the host, and you owe someone change, MAKE payment
                                                // if you are a member, and clicking on the host, make it MAKE payment
                                                // if you are a member, and clicking on yourself, make it SET cost
                                                if ((isHost() && model.cost < 0) || (!isHost() && isHost(memberUid))) {
                                                    makePayment(memberUid, amount);
                                                    getRoomActivity().showSnackbar(
                                                            getRoomActivity().findViewById(R.id.fragment_all_people_layout),
                                                            "Payment sent! Waiting for confirmation");
                                                } else if (!isHost() && memberUid.equals(getRoomActivity().getCurrentUserUid())) {
                                                    setAmount(amount);
                                                    getRoomActivity().showSnackbar(
                                                            getRoomActivity().findViewById(R.id.fragment_all_people_layout),
                                                            "Amount set!");
                                                }

                                                ((AlertDialog) dialog).hide();
                                            } else {
                                                payment_dialog_amountEditText.setError("You must enter a cost greater than 0!");
                                            }
                                        }
                                    });
                                }
                            });
                            alertDialog.show();
                        }
                    });

                    // set up UI components
                    viewHolder.bindToMember(model, memberUid, currentUserUid);
                }
            };
        membersRecyclerView.setAdapter(recyclerAdapter);
    }

    public void setAmount(String amount) {
        final long amountInLong = convertStringToLongCurrency(amount);

        databaseRef.child("users/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long prevAmount = (long) getRoom().memberDetail.get(currentUserUid).get("cost");

                String hostUid = getRoomActivity().getHostUid();
                long hostOverallIsOwedAmount = (long)dataSnapshot.child(hostUid+"/isOwed").getValue();
                long myOverallOweAmount = (long)dataSnapshot.child(currentUserUid+"/owe").getValue();

                // specify locations in db to update
                Map<String, Object> childUpdates = new HashMap<>();

                // update host's isOwed
                childUpdates.put("users/"+hostUid+"/isOwed", hostOverallIsOwedAmount - prevAmount + amountInLong);

                // update user's owe
                childUpdates.put("users/"+currentUserUid+"/owe", myOverallOweAmount - prevAmount + amountInLong);

                // update user's cost in room
                childUpdates.put("rooms/"+getRoomActivity().getRoomUid()+"/memberDetail/"+ currentUserUid +"/cost", amountInLong);

                // update user's status in room
                childUpdates.put("rooms/"+getRoomActivity().getRoomUid()+"/memberDetail/"+ currentUserUid +"/status",
                        "Owes " + Utils.convertLongToStringCurrency(amountInLong));

                // update db
                databaseRef.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void makePayment(String memberUid, String amount) {
        String curUserUid = getRoomActivity().getCurrentUserUid();
        long paidAmount = convertStringToLongCurrency(amount);

        // specify locations in db to update
        Map<String, Object> childUpdates = new HashMap<>();

        // update my status
        childUpdates.put("rooms/"+getRoomUid()+"/memberDetail/"+curUserUid+"/status",
                "Paid " + Utils.convertLongToStringCurrency(paidAmount) + ", but waiting for confirmation.");

        // update my pendingPayments
        childUpdates.put("rooms/"+getRoomUid()+"/memberDetail/"+curUserUid+"/pendingPayments/"+memberUid, true);

        // store this payment in database
        Payment payment = new Payment(curUserUid, memberUid, paidAmount);
        String paymentUid = databaseRef.child("rooms/"+getRoomActivity().getRoomUid()+"/payments/").push().getKey();
        childUpdates.put("rooms/"+getRoomUid()+"/payments/"+paymentUid,
                payment.toMap());

        // update db
        databaseRef.updateChildren(childUpdates);
    }

    public Query getMembersQuery() {
        Query membersQuery = databaseRef.child("rooms/"+roomUid+"/memberDetail").orderByChild("timestamp");

        // set up a listener to hide the progress dialog
        membersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getRoomActivity().hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getRoomActivity().hideProgressDialog();
            }
        });

        return membersQuery;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.fragment_all_people_addButton) {
            System.out.println("Maybe add someone?");
        }
    }
}
