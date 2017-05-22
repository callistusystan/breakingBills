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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import edu.monash.fit3027.breakingbills.FirebaseHelper;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;
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
        String hostUid = getRoomActivity().getRoomHostUid();
        return memberUid.equals(hostUid);
    }

    public boolean isHost() {
        return isHost(getCurrentUserUid());
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
                            final CheckBox payment_dialog_checkbox = (CheckBox) dialogView.findViewById(R.id.payment_dialog_changeCheckBox);
                            payment_dialog_checkbox.setVisibility(View.GONE);

                            final String myUid = getCurrentUserUid();
                            final String clickedMemberUid = memberUid;

                            Room room = FirebaseHelper.getInstance().getRooms().get(getRoomUid());
                            final Member myDetails = new Member(room.memberDetail.get(myUid));
                            final Member clickedMemberDetails = new Member(room.memberDetail.get(clickedMemberUid));

                            if (myDetails.cost == 0 && !myUid.equals(clickedMemberUid)) {
                                getRoomActivity().showMessageDialog(R.layout.message_dialog,
                                        "Oops!",
                                        "You have to set your cost first!",
                                        "Ok",
                                        "");
                                return;
                            }

                            // if you are the host, only a dialog if you owe them change
                            // if you are a member, and clicking on the host, make it MAKE payment
                            // if you are a member, and clicking on yourself, make it SET cost
                            if ((isHost() && !isHost(clickedMemberUid) && clickedMemberDetails.amountPaid > clickedMemberDetails.cost)
                                    || (!isHost() && isHost(clickedMemberUid) && myDetails.amountPaid < myDetails.cost)
                                ) {
                                // show dialog if already have a pending payment
                                if (myDetails.pendingPayments != null && myDetails.pendingPayments.containsKey(memberUid)) {
                                    getRoomActivity().showMessageDialog(R.layout.message_dialog,
                                                                        "Oops!",
                                                                        "You already have a payment pending to this user! Go to the Payments Tab to edit it!",
                                                                        "Ok",
                                                                        "");
                                    return;
                                }
                                payment_dialog_label.setText("Make payment");
                                payment_dialog_checkbox.setVisibility(View.VISIBLE);
                            } else if (myUid.equals(clickedMemberUid)) {
                                payment_dialog_label.setText("Set cost");
                            } else {
                                getRoomActivity().showMessageDialog(R.layout.message_dialog,
                                        "Oops!",
                                        "No action can be done with this user!",
                                        "Ok",
                                        "");
                                return;
                            }

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
                                            final String amount = payment_dialog_amountEditText.getText().toString().trim();

                                            // only process if both fields are not empty
                                            if (!amount.equals("") && Utils.convertStringToLongCurrency(amount) > 0){
                                                // if you are the host, and you owe someone change, MAKE payment
                                                // if you are a member, and clicking on the host, make it MAKE payment
                                                // if you are a member, and clicking on yourself, make it SET cost
                                                if ((isHost() && !isHost(clickedMemberUid) && clickedMemberDetails.amountPaid > clickedMemberDetails.cost)
                                                        || (!isHost() && isHost(clickedMemberUid) && myDetails.amountPaid < myDetails.cost)
                                                    ) {
                                                    boolean wantChange = payment_dialog_checkbox.isChecked();
                                                    makePayment(clickedMemberUid, amount, wantChange);
                                                    getRoomActivity().showSnackbar(getRoomActivity().findViewById(R.id.fragment_all_people_layout),
                                                            "Payment sent! Waiting for confirmation");
                                                } else if (myUid.equals(clickedMemberUid)) {
                                                    setAmount(amount);
                                                    getRoomActivity().showSnackbar(getRoomActivity().findViewById(R.id.fragment_all_people_layout),
                                                                                    "Amount set!");
                                                }

                                                ((AlertDialog) dialog).hide();
                                            } else {
                                                payment_dialog_amountEditText.setError("You must enter an amount greater than 0!");
                                            }
                                        }
                                    });
                                }
                            });
                            alertDialog.show();
                        }
                    });

                    // set up UI components
                    viewHolder.bindToMember(model, memberUid, getCurrentUserUid());
                }
            };
        membersRecyclerView.setAdapter(recyclerAdapter);
    }

    public void setAmount(String newCostString) {
        final long newCost = convertStringToLongCurrency(newCostString);

        FirebaseHelper.getInstance().setCost(getRoomUid(), getCurrentUserUid(), newCost);
    }

    public void makePayment(String payeeUid, String newPaymentString, boolean wantChange) {
        final long newPayment = Utils.convertStringToLongCurrency(newPaymentString);
        FirebaseHelper.getInstance().makePayment(getRoomUid(), getCurrentUserUid(), payeeUid, newPayment, wantChange);
    }

    public Query getMembersQuery() {
        Query membersQuery = databaseRef.child("rooms/"+getRoomUid()+"/memberDetail").orderByChild("timestamp");

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
