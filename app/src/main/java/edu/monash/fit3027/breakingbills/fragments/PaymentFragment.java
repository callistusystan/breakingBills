package edu.monash.fit3027.breakingbills.fragments;

/**
 * A fragment class to show all payments made in a room. Also allows relevant users to interact
 * with the payments.
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for the Firebase recycler view
 *
 * Created by Callistus on 30/4/2017.
 */

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.activities.RoomActivity;
import edu.monash.fit3027.breakingbills.models.Payment;
import edu.monash.fit3027.breakingbills.viewholders.PaymentViewHolder;

public class PaymentFragment extends RoomFragment {

    // firebase components
    private DatabaseReference databaseRef;

    // recycler view
    private RecyclerView paymentRecyclerView;
    private LinearLayoutManager mManager;

    private LinearLayout emptyMessageLinearLayout;

    // necessary variables
    private String roomUid;

    public PaymentFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRoomActivity((RoomActivity) getActivity());

        View rootView = inflater.inflate(R.layout.fragment_payment, container, false);

        // init firebase components
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // link views
        paymentRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_payment_recyclerView);
        paymentRecyclerView.setHasFixedSize(true);

        emptyMessageLinearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_payment_emptyView);

        // init necessary variables
        roomUid = getRoomActivity().getRoomUid();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // init views
        initViews();
    }

    /**
     * A helper method to initialize all views in this fragment.
     */
    public void initViews() {
        // Set up Layout Manager, reverse layout so it shows most recent at the top
        mManager = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        paymentRecyclerView.setLayoutManager(mManager);

        initMembersRecyclerView();
    }

    /**
     * A helper method to initialize the recycler view of payments
     */
    public void initMembersRecyclerView() {
        // Set up FirebaseRecyclerAdapter with the Query
        Query paymentsQuery = getPaymentsQuery();

        final PaymentFragment instance = this;

        FirebaseRecyclerAdapter<Payment, PaymentViewHolder> recyclerAdapter =
            new FirebaseRecyclerAdapter<Payment, PaymentViewHolder>
                    (Payment.class, R.layout.item_payment, PaymentViewHolder.class, paymentsQuery) {
                @Override
                protected void populateViewHolder(PaymentViewHolder viewHolder, final Payment model, int position) {
                    final DatabaseReference paymentRef = getRef(position);

                    // Set click listener for the whole room view
                    final String paymentUid = paymentRef.getKey();

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    // set up UI components
                    viewHolder.bindToPayment(model, paymentUid, instance);
                }
            };
        paymentRecyclerView.setAdapter(recyclerAdapter);
    }

    /**
     * A helper method to get the query for all payments made in this room
     *
     * @return a query for all payments made in this room
     */
    public Query getPaymentsQuery() {
        Query paymentsQuery = databaseRef.child("rooms/"+roomUid+"/payments");

        // set up a listener to hide the progress dialog
        paymentsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // show an empty message if there are no payments
                if (dataSnapshot.getValue() == null) {
                    paymentRecyclerView.setVisibility(View.GONE);
                    emptyMessageLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    paymentRecyclerView.setVisibility(View.VISIBLE);
                    emptyMessageLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return paymentsQuery;
    }
}
