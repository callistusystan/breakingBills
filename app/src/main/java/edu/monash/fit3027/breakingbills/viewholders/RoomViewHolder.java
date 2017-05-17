package edu.monash.fit3027.breakingbills.viewholders;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;

/**
 * Created by Callistus on 28/4/2017.
 */

public class RoomViewHolder extends RecyclerView.ViewHolder {

    private static final int RED = Color.parseColor("#e61610");
    private static final int ORANGE = Color.parseColor("#ffa124");
    private static final int GREEN = Color.parseColor("#5c983d");

    public LinearLayout listItem;

    public TextView sectionTextView;
    public TextView titleTextView;
    public TextView dateTextView;
    public TextView statusTextView;
    public ImageView iconImageView;

    public long timestamp;

    public RoomViewHolder(View itemView) {
        super(itemView);

        listItem = (LinearLayout) itemView.findViewById(R.id.item_room_listItem);
        listItem.setVisibility(View.GONE);

        sectionTextView = (TextView) itemView.findViewById(R.id.item_room_sectionHeader);
        titleTextView = (TextView) itemView.findViewById(R.id.item_room_roomTitle);
        dateTextView = (TextView) itemView.findViewById(R.id.item_room_roomDate);
        statusTextView = (TextView) itemView.findViewById(R.id.item_room_roomStatus);
        iconImageView = (ImageView) itemView.findViewById(R.id.item_room_roomIcon);
    }

    public void hide() {
        listItem.setVisibility(View.GONE);
    }

    public void bindToRoom(Room room, String currentUserUid) {
        listItem.setVisibility(View.VISIBLE);
        // set timestamp
        timestamp = room.timestamp;

        // set sectionTextView
        Date date = new Date(room.timestamp);
        SimpleDateFormat sfd = new SimpleDateFormat("MMMM yyyy");
        sectionTextView.setText(sfd.format(date));

        // set room title
        titleTextView.setText(room.title);

        // set room date
        sfd = new SimpleDateFormat("d MMM");
        dateTextView.setText(sfd.format(date));

        // set status and icon
        String currentUserStatus = room.memberDetail.get(currentUserUid).get("status").toString();
        statusTextView.setText(currentUserStatus);
        switch (currentUserStatus) {
            case Member.PAYMENT_SETTLED:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(GREEN));
                break;
            case Member.EXPECTING_PAYMENT: case Member.PAID_CHANGE:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(ORANGE));
                break;
            case Member.NO_STATUS:
                iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
                break;
            default:
                if (currentUserStatus.endsWith("confirmation."))
                    iconImageView.setBackgroundTintList(ColorStateList.valueOf(ORANGE));
                else
                    iconImageView.setBackgroundTintList(ColorStateList.valueOf(RED));
                break;
        }
    }

    public void showSectionHeader() {
        this.sectionTextView.setVisibility(View.VISIBLE);
    }

    public void hideSectioHeader() {
        this.sectionTextView.setVisibility(View.GONE);
    }
}

