package edu.monash.fit3027.breakingbills.viewholders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.models.Member;

/**
 * Created by Callistus on 29/4/2017.
 */

public class MemberViewHolder extends RecyclerView.ViewHolder {

    public static final int RED = Color.parseColor("#e61610");
    public static final int ORANGE = Color.parseColor("#ffa124");
    public static final int GREEN = Color.parseColor("#5c983d");

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

        // set cost
        amountTextView.setTextColor(RED);
        if (member.cost == 0) {
            amountTextView.setText("TBD");
        } else {
            amountTextView.setText(Utils.convertLongToStringCurrency(member.cost));
            if (member.cost == member.amountPaid){
                amountTextView.setTextColor(GREEN);
            } else if (member.amountPaid > member.cost) {
                amountTextView.setTextColor(ORANGE);
            }
        }

        // set status and icon
        String status = Utils.determineMemberStatus(member);
        statusTextView.setText(status);

        if (status.startsWith("Owes") || status.equals("No status"))
            iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
        else if (status.startsWith("Paid"))
            iconImageView.setBackgroundTintList(ColorStateList.valueOf(ORANGE));
        else if (status.equals("Payment settled"))
            iconImageView.setBackgroundTintList(ColorStateList.valueOf(GREEN));
        else
            iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
    }
}

