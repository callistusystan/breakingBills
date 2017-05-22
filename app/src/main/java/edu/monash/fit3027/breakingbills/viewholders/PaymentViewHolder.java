package edu.monash.fit3027.breakingbills.viewholders;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.FirebaseHelper;
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
    public TextView pendingTextView;

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
        pendingTextView = (TextView) itemView.findViewById(R.id.item_payment_pending);
        statusTextView = (TextView) itemView.findViewById(R.id.item_payment_status);
        statusTextView.setVisibility(View.GONE);

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

    public void bindToPayment(final Payment payment, final String paymentUid, final PaymentFragment instance) {
        Room room = instance.getRoom();

        String payerNickname = (String)room.memberDetail.get(payment.payerUid).get("nickname");
        String payeeNickname = (String)room.memberDetail.get(payment.payeeUid).get("nickname");

        payerTextView.setText(payerNickname);
        payeeTextView.setText(payeeNickname);

        amountTextView.setText(" " + Utils.convertLongToStringCurrency(payment.amount));

        // if already responded, show the status
        if (payment.hasResponded) {
            pendingTextView.setVisibility(View.GONE);
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
        final String currentUserUid = instance.getCurrentUserUid();

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
            // if im the payer, allow me to edit and delete
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText("EDIT");

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // init the alert dialog builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(instance.getActivity());

                    // set the layout for the alert dialog
                    LayoutInflater inflater = instance.getActivity().getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.payment_dialog, null);

                    // get views
                    final TextView payment_dialog_label = (TextView) dialogView.findViewById(R.id.payment_dialog_label);
                    final EditText payment_dialog_amountEditText = (EditText) dialogView.findViewById(R.id.payment_dialog_amountEditText);

                    payment_dialog_label.setText("Edit payment amount");

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
                                    String newPaymentAmountString = payment_dialog_amountEditText.getText().toString().trim();

                                    // only process if both fields are not empty
                                    if (!newPaymentAmountString.equals("") && Utils.convertStringToLongCurrency(newPaymentAmountString) > 0){
                                        long newPaymentAmount = Utils.convertStringToLongCurrency(newPaymentAmountString);

                                        FirebaseHelper.getInstance().setPayment(roomUid, paymentUid, newPaymentAmount);

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

            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText("DELETE");

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // specify locations in db to update
                    Map<String, Object> childUpdates = new HashMap<>();

                    // update payment amount
                    childUpdates.put("rooms/"+roomUid+"/payments/"+paymentUid, null);

                    // remove my pendingPayment
                    childUpdates.put("rooms/"+roomUid+"/memberDetail/"+currentUserUid+"/pendingPayments/"+payment.payeeUid, null);

                    // update db
                    databaseRef.updateChildren(childUpdates);
                }
            });
        } else if (currentUserUid.equals(payment.payeeUid)){
            // if im the payee, allow me to confirm/reject
            positiveButton.setVisibility(View.VISIBLE);
            positiveButton.setText("CONFIRM");

            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseHelper.getInstance().processPayment(roomUid, paymentUid, true);

                    arrow.setVisibility(View.GONE);
                    expandedSection.setVisibility(View.GONE);
                }
            });

            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText("REJECT");

            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseHelper.getInstance().processPayment(roomUid, paymentUid, false);

                    arrow.setVisibility(View.GONE);
                    expandedSection.setVisibility(View.GONE);
                }
            });
        }
    }
}

