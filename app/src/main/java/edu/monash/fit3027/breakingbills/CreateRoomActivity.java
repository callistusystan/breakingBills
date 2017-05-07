package edu.monash.fit3027.breakingbills;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;

public class CreateRoomActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CONTACT = 100;

    // edit texts
    private EditText roomTitleEditText;
    private EditText nicknameEditText;

    // buttons
    private FloatingActionButton createRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        // init views
        roomTitleEditText = (EditText) findViewById(R.id.activity_create_room_roomTitleEditText);
        nicknameEditText = (EditText) findViewById(R.id.activity_create_room_nicknameEditText);

        createRoomButton = (FloatingActionButton) findViewById(R.id.activity_create_room_createRoomButton);
        createRoomButton.setOnClickListener(this);

        // set action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT) {
            switch (RESULT_OK) {
                case REQUEST_CONTACT:
                    break;
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void writeNewRoom() {
        // get database ref
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        String currentUserUid = getCurrentUserUid();

        String roomTitle = roomTitleEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();

        // get new room's Uid
        String roomUid = databaseRef.child("rooms").push().getKey();

        // Init room, with current user as the host
        Room room = new Room(roomTitle, currentUserUid, 0);

        // Put current user as one of the room's members
        room.members.put(currentUserUid, true);

        // Init member and details
        Member member = new Member(nickname, true, false, Member.EXPECTING_PAYMENT, 0, -1);

        room.memberDetail.put(currentUserUid, member.toMap());

        // get key-value map
        Map<String, Object> roomValues = room.toMap();

        // specify locations in db to update
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/rooms/" + roomUid, roomValues);

        // update db
        databaseRef.updateChildren(childUpdates);
    }

    public boolean allFieldsEntered() {
        // returns true if all fields are entered
        boolean valid = true;
        EditText[] editTexts = { roomTitleEditText, nicknameEditText };

        for (EditText e : editTexts) {
            if (e.getText().toString().trim().equals("")) {
                e.setError("You must enter this field!");
                valid = false;
            }
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        if (allFieldsEntered()) {
            writeNewRoom();
            this.setResult(RESULT_OK);
            finish();
        }
    }
}
