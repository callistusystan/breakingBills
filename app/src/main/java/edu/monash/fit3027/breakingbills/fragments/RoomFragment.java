package edu.monash.fit3027.breakingbills.fragments;

import android.support.v4.app.Fragment;

import edu.monash.fit3027.breakingbills.RoomActivity;

/**
 * Created by Callistus on 1/5/2017.
 */

public abstract class RoomFragment extends Fragment {

    private RoomActivity roomActivity;

    public void setRoomActivity(RoomActivity roomActivity) {
        this.roomActivity = roomActivity;
    }

    public RoomActivity getRoomActivity() {
        return roomActivity;
    }

}
