package edu.monash.fit3027.breakingbills.viewholders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.fragments.PaymentFragment;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Payment;
import edu.monash.fit3027.breakingbills.models.Room;

/**
 * Created by Callistus on 28/4/2017.
 */

public class PaymentViewHolder extends RecyclerView.ViewHolder {

    private static final int RED = Color.parseColor("#C62828");
    private static final int GREEN = Color.parseColor("#8BC34A");

    public LinearLayout listItem;
    public LinearLayout expandedSection;

    public TextView payerTextView;
    public TextView payeeTextView;
    public TextView amountTextView;
    public TextView statusTextView;

    public ImageView arrow;

    public Button positiveButton;
    public Button negativeButton;

    private boolean showExpanded;

    public PaymentViewHolder(View itemView) {
        super(itemView);

        listItem = (LinearLayout) itemView.findViewById(R.id.item_payment_listItem);

        expandedSection = (LinearLayout) itemView.findViewById(R.id.item_payment_expandedSection);
        expandedSection.setVisibility(View.GONE);

        payerTextView = (TextView) itemView.findViewById(R.id.item_payment_payer);
        payeeTextView = (TextView) itemView.findViewById(R.id.item_payment_payee);
        amountTextView = (TextView) itemView.findViewById(R.id.item_payment_amount);
        statusTextView = (TextView) itemView.findViewById(R.id.item_payment_status);

        arrow = (ImageView) itemView.findViewById(R.id.item_payment_arrow);

        positiveButton = (Button) itemView.findViewById(R.id.item_payment_positiveButton);
        negativeButton = (Button) itemView.findViewById(R.id.item_payment_negativeButton);

        showExpanded = false;
    }

    public void toggle() {
        showExpanded = !showExpanded;
        if (showExpanded) {
            expandedSection.setVisibility(View.VISIBLE);
            arrow.setImageResource(R.drawable.ic_up);
        } else {
            expandedSection.setVisibility(View.GONE);
            arrow.setImageResource(R.drawable.ic_down);
        }
    }

    public void bindToPayment(final Payment payment, final String paymentUid, PaymentFragment instance) {
        Room room = instance.getRoom();

        String payerNickname = (String)room.memberDetail.get(payment.payerUid).get("nickname");
        String payeeNickname = (String)room.memberDetail.get(payment.payeeUid).get("nickname");

        payerTextView.setText(payerNickname);
        payeeTextView.setText(payeeNickname);

        amountTextView.setText(" " + Utils.convertLongToStringCurrency(payment.amount));

        // if already responded, show the status
        if (payment.hasResponded) {
            if (payment.isConfirmed) {
                statusTextView.setText("CONFIRMED");
                statusTextView.setTextColor(ColorStateList.valueOf(GREEN));
            } else {
                statusTextView.setText("REJECTED");
                statusTextView.setTextColor(ColorStateList.valueOf(RED));
            }
            statusTextView.setVisibility(View.VISIBLE);
            return;
        }

        // not yet responded, thus allow relevant users to perform actions
        final String roomUid = instance.getRoomUid();
        String currentUserUid = instance.getCurrentUserUid();

        // allow user to expand/collapse each list item
        if (currentUserUid.equals(payment.payerUid) || currentUserUid.equals(payment.payeeUid)) {
            arrow.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggle();
                }
            });
        }

        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // implement on click logic depending on situation
        if (currentUserUid.equals(payment.payerUid)) {
            // if im the payer, allow me to delete only
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText("EDIT");

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payment.hasResponded = true;
                    payment.isConfirmed = true;
                    databaseRef.child("users/");
                    databaseRef.child("rooms/"+roomUid+"/payments/"+paymentUid).setValue(payment.toMap());

                    arrow.setVisibility(View.GONE);
                    expandedSection.setVisibility(View.GONE);
                }
            });

            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText("DELETE");

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    databaseRef.child("rooms/"+roomUid+"/payments/"+paymentUid).setValue(null);
                }
            });
        } else if (currentUserUid.equals(payment.payeeUid)){
            // if im the payee, allow me to confirm/reject
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText("CONFIRM");

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payment.hasResponded = true;
                    payment.isConfirmed = true;
                    databaseRef.child("users/");
                    databaseRef.child("rooms/"+roomUid+"/payments/"+paymentUid).setValue(payment.toMap());

                    arrow.setVisibility(View.GONE);
                    expandedSection.setVisibility(View.GONE);
                }
            });

            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText("REJECT");

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payment.hasResponded = true;
                    payment.isConfirmed = false;
                    databaseRef.child("users/");
                    databaseRef.child("rooms/"+roomUid+"/payments/"+paymentUid).setValue(payment.toMap());

                    arrow.setVisibility(View.GONE);
                    expandedSection.setVisibility(View.GONE);
                }
            });
        }
    }
}

