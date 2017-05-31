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
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.models.Room;

/**
 * A list view holder to visually show room information
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for the idea of view holders
 *
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
        String status = Utils.determineMemberStatus(room.memberDetail.get(currentUserUid));
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

    public void showSectionHeader() {
        this.sectionTextView.setVisibility(View.VISIBLE);
    }

    public void hideSectioHeader() {
        this.sectionTextView.setVisibility(View.GONE);
    }
}

