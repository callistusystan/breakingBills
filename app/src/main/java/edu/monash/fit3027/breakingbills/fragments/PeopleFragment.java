package edu.monash.fit3027.breakingbills.fragments;

/**
 * Created by Callistus on 30/4/2017.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.BaseActivity;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.viewholders.MemberViewHolder;

public class PeopleFragment extends RoomFragment {

    // firebase components
    private DatabaseReference databaseRef;

    // recycler view
    private RecyclerView membersRecyclerView;
    private LinearLayoutManager mManager;

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

    public void initMembersRecyclerView() {
        // show progress dialog
        getRoomActivity().showProgressDialog();

        // Set up FirebaseRecyclerAdapter with the Query
        Query membersQuery = getMembersQuery();

        FirebaseRecyclerAdapter<Member, MemberViewHolder> recyclerAdapter =
            new FirebaseRecyclerAdapter<Member, MemberViewHolder>
                    (Member.class, R.layout.item_member, MemberViewHolder.class, membersQuery) {
                @Override
                protected void populateViewHolder(MemberViewHolder viewHolder, Member model, int position) {
                    final DatabaseReference memberRef = getRef(position);

                    // Set click listener for the whole room view
                    final String memberUid = memberRef.getKey();

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            // init the alert dialog builder
//                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//                            // set the layout for the alert dialog
//                            LayoutInflater inflater = getActivity().getLayoutInflater();
//                            final View setPaymentDialogView = inflater.inflate(R.layout.set_payment_dialog, null);
//
//                            // set the positive and negative buttons' onclicks
//                            builder.setView(setPaymentDialogView)
//                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            // init edit texts
//                                            EditText set_payment_dialog_amountEditText = (EditText) setPaymentDialogView.findViewById(R.id.set_payment_dialog_amountEditText);
//
//                                            String amount = set_payment_dialog_amountEditText.getText().toString().trim();
//
//                                            // only process if both fields are not empty
//                                            if (isValidCurrency(amount)){
//                                                setAmount(memberUid, amount);
//                                            } else {
//                                                // if any is empty, show a error snackbar
//                                                getRoomActivity().showSnackbar(getRoomActivity().findViewById(R.id.activity_room_viewPager), "You must enter both fields!");
//                                            }
//                                        }
//                                    })
//                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                        }
//                                    });
//
//                            // create and show this dialog
//                            AlertDialog alertDialog = builder.create();
//                            alertDialog.show();
                        }
                    });

                    // set up UI components
                    viewHolder.bindToMember(model, memberUid, currentUserUid);
                }
            };
        membersRecyclerView.setAdapter(recyclerAdapter);
    }

    public boolean isValidCurrency(String amount) {
        return true;
    }

    public void setAmount(String memberUid, String amount) {
        DatabaseReference ref = databaseRef.child("rooms/"+getRoomActivity().getRoomUid()+"/memberDetail/"+memberUid);
        long amountLong = Long.parseLong(amount);
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("amount", Long.parseLong(amount));
        myMap.put("status", "Owes " + String.format("$%d.%02d", amountLong/100, amountLong%100));

        ref.setValue(myMap);
    }

    public Query getMembersQuery() {
        Query membersQuery = databaseRef.child("rooms/"+roomUid+"/memberDetail").orderByChild("timestamp");

        // set up a listener to hide the progress dialog
        membersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
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

    public RoomActivity getRoomActivity() {
        return (RoomActivity)getActivity();
    }

}
