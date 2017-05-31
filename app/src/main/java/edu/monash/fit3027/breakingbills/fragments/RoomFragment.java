package edu.monash.fit3027.breakingbills.fragments;

import android.support.v4.app.Fragment;

import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.models.Room;

/**
 * An abstract fragment class representing the base fragment to inherit from
 * with methods to easily get room activity and metadata.
 *
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

    public Room getRoom() { return getRoomActivity().getRoom(); }

    public String getRoomUid() { return getRoomActivity().getRoomUid(); }

    public String getCurrentUserUid() { return getRoomActivity().getCurrentUserUid(); }

}
