package edu.monash.fit3027.breakingbills.viewholders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Member;

/**
 * Created by Callistus on 29/4/2017.
 */

public class MemberViewHolder extends RecyclerView.ViewHolder {

    private static final int RED = Color.parseColor("#e61610");
    private static final int ORANGE = Color.parseColor("#ffa124");
    private static final int GREEN = Color.parseColor("#5c983d");

    public TextView nicknameTextView;
    public TextView amountTextView;
    public TextView statusTextView;
    public ImageView iconImageView;

    public MemberViewHolder(View itemView) {
        super(itemView);

        nicknameTextView = (TextView) itemView.findViewById(R.id.item_member_nickname);
        amountTextView = (TextView) itemView.findViewById(R.id.item_member_amount);
        statusTextView = (TextView) itemView.findViewById(R.id.item_member_status);
        iconImageView = (ImageView) itemView.findViewById(R.id.item_member_icon);
    }

    public void bindToMember(Member member, String memberUid, String currentUserUid) {
        // set nickname, also puts a label to indicate the host
        String nicknameSuffix = (member.isHost) ? " (Host)" : "";
        if (memberUid.equals(currentUserUid))
            nicknameTextView.setText("You" + nicknameSuffix);
        else
            nicknameTextView.setText(member.nickname + nicknameSuffix);

        // set amount
        if (!member.isHost) {
            if (member.amount == -1)
                amountTextView.setText("TBD");
            else
                amountTextView.setText(String.format("$%d.%02d", member.amount/100, member.amount%100));
        }

        // set status and icon
        statusTextView.setText(member.status);
        switch (member.status) {
            case Member.PAYMENT_SETTLED:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(GREEN));
                break;
            case Member.EXPECTING_PAYMENT: case Member.PAID_CHANGE: case Member.PAID_CONFIRM:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(ORANGE));
                break;
            case Member.NO_STATUS:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
                break;
            default:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
                break;
        }
    }
}

