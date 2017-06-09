package edu.monash.fit3027.breakingbills.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.FirebaseHelper;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;
import edu.monash.fit3027.breakingbills.models.User;
import edu.monash.fit3027.breakingbills.viewholders.MemberViewHolder;
import edu.monash.fit3027.breakingbills.viewholders.RoomViewHolder;

import static edu.monash.fit3027.breakingbills.Utils.convertLongToStringCurrency;
import static edu.monash.fit3027.breakingbills.Utils.getMonthYear;
import static java.lang.Math.abs;

/**
 * The main activity screen the user is brought when starting the app
 * Shows all the rooms the user has joined, with options to
 * create/join rooms and view the about screen.
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for the Firebase recycler view
 *  2. https://developer.android.com/guide/topics/ui/dialogs.html for alert dialogs
 *
 * Created by Callistus on 29/4/2017.
 */

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final int CREATE_ROOM_REQ = 100;

    // firebase components
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private FirebaseUser currentUser;

    // views
    private RecyclerView roomsRecyclerView;
    private LinearLayoutManager roomsRecyclerViewManager;
    private BottomSheetDialog bottomSheetDialog;
    private LinearLayout emptyMessageLinearLayout;
    private TextView oweTextView;
    private TextView isOwedTextView;
    private TextView balanceTextView;

    // buttons
    private FloatingActionButton addButton;
    private LinearLayout createButton;
    private LinearLayout joinButton;

    // recyclerAdapter
    FirebaseRecyclerAdapter<Room, RoomViewHolder> recyclerAdapter;

    // hashmaps for setting section headers
    private HashMap<String, String> sectionStartsAtRoomUid;
    private HashMap<String, RoomViewHolder> roomUidRoomViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init firebase components
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // ensure one instance is up
        FirebaseHelper.getInstance();

        // init hash maps
        sectionStartsAtRoomUid = new HashMap<>();
        roomUidRoomViewHolder = new HashMap<>();

        // init views
        initViews();
    }

    public void initViews() {
        // Set up Layout Manager, reverse layout so it shows most recent at the top
        roomsRecyclerViewManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        roomsRecyclerViewManager.setReverseLayout(true);
        roomsRecyclerViewManager.setStackFromEnd(true);

        // init rooms recycler view
        roomsRecyclerView = (RecyclerView) findViewById(R.id.activity_main_roomsRecyclerView);
        roomsRecyclerView.setHasFixedSize(true);
        roomsRecyclerView.setLayoutManager(roomsRecyclerViewManager);

        // init text view
        emptyMessageLinearLayout = (LinearLayout) findViewById(R.id.activity_main_emptyView);
        oweTextView = (TextView) findViewById(R.id.activity_main_oweTextView);
        isOwedTextView = (TextView) findViewById(R.id.activity_main_isOwedTextView);
        balanceTextView = (TextView) findViewById(R.id.activity_main_balanceTextView);

        // init add floating action button
        addButton = (FloatingActionButton) findViewById(R.id.activity_main_addButton);
        addButton.setOnClickListener(this);

        // init bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = this.getLayoutInflater().inflate(R.layout.add_bottom_sheet, null);
        bottomSheetDialog.setContentView(sheetView);

        // init create and join buttons in bottom sheet
        createButton = (LinearLayout) sheetView.findViewById(R.id.bottom_sheet_create);
        createButton.setOnClickListener(this);

        joinButton = (LinearLayout) sheetView.findViewById(R.id.bottom_sheet_join);
        joinButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // get the current user
        currentUser = auth.getCurrentUser();

        // show a helpful loading dialog
        showProgressDialog();

        // Check if user is signed in, else sign in anonymously
        if (currentUser == null) {
            signIn();
        } else {
            // user has signed in, so init rooms
            initUserStatusView();
            initRoomsRecyclerView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_ROOM_REQ) {
            // if we returned from the Create Room Screen with RESULT_OK, a room has been created
            if (resultCode == RESULT_OK) {
                // show a snackbar that a room has been successfully created
                showSnackbar(findViewById(R.id.activity_main_layout), "Room created!");
            }
        }
    }

    /**
     * A helper method to sign the user in anonymously, so that we can store the user in the
     * firebase database.
     */
    public void signIn() {
        // simply sign in anonymously
        auth.signInAnonymously()
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        currentUser = auth.getCurrentUser();

                        // Also, create a store in database to keep track of owed amounts
                        User user = new User(0,0);
                        databaseRef.child("users/" + getCurrentUserUid()).setValue(user.toMap());

                        initUserStatusView();
                        // user has not joined any room, show the emptyMessage
                        roomsRecyclerView.setVisibility(View.GONE);
                        emptyMessageLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        // If sign in fails, display a message to the user.
                        showSnackbar(findViewById(R.id.activity_main_layout), "Sign in failed");
                    }
                }
            });
    }

    /**
     * A helper method to initialize the user status view from Firebase
     */
    public void initUserStatusView() {
        // a query for this specific user
        Query userQuery = databaseRef.child("users/"+getCurrentUserUid());

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                // update textviews based on user data
                oweTextView.setText(convertLongToStringCurrency(user.owe));
                isOwedTextView.setText(convertLongToStringCurrency(user.isOwed));
                if (user.isOwed > user.owe) {
                    balanceTextView.setText("+"+convertLongToStringCurrency(user.isOwed - user.owe));
                    balanceTextView.setTextColor(MemberViewHolder.GREEN);
                } else if (user.isOwed == user.owe) {
                    balanceTextView.setText("+-"+ convertLongToStringCurrency(0));
                    balanceTextView.setTextColor(MemberViewHolder.GREEN);
                } else {
                    balanceTextView.setText("-"+ convertLongToStringCurrency(abs(user.isOwed-user.owe)));
                    balanceTextView.setTextColor(MemberViewHolder.RED);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * A helper method to initialize the room recycler view
     *
     * Reference: https://github.com/firebase/quickstart-android
     */
    public void initRoomsRecyclerView() {
        // Set up FirebaseRecyclerAdapter with the Query
        Query joinedRoomsQuery = getJoinedRoomsQuery();
        recyclerAdapter =
                new FirebaseRecyclerAdapter<Room, RoomViewHolder>
                        (Room.class, R.layout.item_room, RoomViewHolder.class, joinedRoomsQuery) {
            @Override
            protected void populateViewHolder(RoomViewHolder viewHolder, final Room model, int position) {
                if (!model.members.containsKey(getCurrentUserUid())) {
                    viewHolder.itemView.setVisibility(View.INVISIBLE);
                    return;
                }
                final DatabaseReference roomRef = getRef(position);

                // Set click listener for the whole room view
                final String roomUid = roomRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch RoomActivity on this room
                        Intent intent = new Intent(MainActivity.this, RoomActivity.class);

                        intent.putExtra("roomUid", roomUid);
                        intent.putExtra("roomTitle", model.title);

                        startActivity(intent);
                    }
                });

                // get monthyear for the current timestamp
                String monthYear = getMonthYear(model.timestamp);

                viewHolder.bindToRoom(model, getCurrentUserUid());
                if (sectionStartsAtRoomUid.get(monthYear) == roomUid) {
                    viewHolder.showSectionHeader();
                } else {
                    viewHolder.hideSectioHeader();
                }
                roomUidRoomViewHolder.put(roomUid, viewHolder);
            }
        };
        roomsRecyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_main_about) {
            // go to about app activity
            Intent intent = new Intent(this, AboutAppActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.activity_main_addButton) {
            // show bottom sheets
            bottomSheetDialog.show();
            return;
        } else if (viewId == R.id.bottom_sheet_create) {
            // go to create room activity
            Intent intent = new Intent(this, CreateRoomActivity.class);
            startActivityForResult(intent, CREATE_ROOM_REQ);
        } else if (viewId == R.id.bottom_sheet_join) {
            // show a join room form dialog

            // init the alert dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // set the layout for the alert dialog
            LayoutInflater inflater = getLayoutInflater();
            final View joinRoomDialogView = inflater.inflate(R.layout.join_room_dialog, null);

            // set the positive and negative buttons' onclicks
            builder.setView(joinRoomDialogView)
                .setPositiveButton("JOIN", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

            // create and show this dialog
            AlertDialog alertDialog = builder.create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    // init edit texts
                    Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // init edit texts
                            final EditText join_room_dialog_roomIdEditText = (EditText) joinRoomDialogView.findViewById(R.id.join_room_dialog_roomIdEditText);
                            final EditText join_room_dialog_nicknameEditText = (EditText) joinRoomDialogView.findViewById(R.id.join_room_dialog_nicknameEditText);

                            final String roomId = join_room_dialog_roomIdEditText.getText().toString().trim();
                            final String nickname = join_room_dialog_nicknameEditText.getText().toString().trim();

                            // only process if both fields are not empty
                            if (!roomId.equals("") && !nickname.equals("")){
                                // check if room exists
                                databaseRef.child("rooms").child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // do not continue if room does not exist
                                        if (dataSnapshot.getValue() == null) {
                                            join_room_dialog_roomIdEditText.setError("Room does not exist!");
                                            return;
                                        }

                                        // do not continue if already a member
                                        Room roomToUpdate = dataSnapshot.getValue(Room.class);

                                        if (roomToUpdate.members.containsKey(getCurrentUserUid())) {
                                            join_room_dialog_roomIdEditText.setError("You are already in this room!");
                                            return;
                                        }

                                        for (String memberUid: roomToUpdate.memberDetail.keySet()) {
                                            String memberNickName = (String) roomToUpdate.memberDetail.get(memberUid).get("nickname");
                                            if (memberNickName.equals(nickname)) {
                                                join_room_dialog_nicknameEditText.setError("Nickname is already used! ");
                                                return;
                                            }
                                        }

                                        if (roomToUpdate.members.containsKey(getCurrentUserUid())) {
                                            join_room_dialog_roomIdEditText.setError("You are already in this room!");
                                            return;
                                        }

                                        // since room exists and not a member, thus join the room

                                        // indicate in room that current user is now a member
                                        roomToUpdate.members.put(getCurrentUserUid(), roomToUpdate.timestamp+1);

                                        // init the member
                                        Member member = new Member(nickname, false);
                                        roomToUpdate.memberDetail.put(getCurrentUserUid(), member.toMap());

                                        // specify locations in db to update
                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put("/rooms/" + roomId + "/memberDetail", roomToUpdate.memberDetail);
                                        childUpdates.put("/rooms/" + roomId + "/members/" + getCurrentUserUid(), roomToUpdate.timestamp+1);

                                        // update db
                                        databaseRef.updateChildren(childUpdates);


                                        ((AlertDialog) dialog).hide();
                                        showSnackbar(findViewById(R.id.activity_main_layout), "Joined room!");
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                if (roomId.equals(""))
                                    join_room_dialog_roomIdEditText.setError("You must enter this field!");
                                if (nickname.equals(""))
                                    join_room_dialog_nicknameEditText.setError("You must enter this field!");
                            }
                        }
                    });
                }
            });
            alertDialog.show();
        }

        // after any action has been selected, hide the bottom sheet
        bottomSheetDialog.hide();
    }

    /**
     * A helper method to get the query for rooms that this user has joined
     *
     * @return A query
     */
    public Query getJoinedRoomsQuery() {
        Query joinedRoomsQuery = databaseRef.child("rooms").orderByChild("members/"+getCurrentUserUid())
                .limitToLast(100);

        joinedRoomsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hideProgressDialog();

                // check if user has joined any rooms
                boolean joinedAny = false;

                for (DataSnapshot roomDataSnapshot : dataSnapshot.getChildren()) {
                    Room room = roomDataSnapshot.getValue(Room.class);
                    if (room.members.containsKey(getCurrentUserUid())) {
                        joinedAny = true;
                    }
                }

                if (!joinedAny) {
                    // if user has not joined any room, show the emptyMessage
                    roomsRecyclerView.setVisibility(View.GONE);
                    emptyMessageLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    // if user has joined any room, show the room recycler view
                    roomsRecyclerView.setVisibility(View.VISIBLE);
                    emptyMessageLinearLayout.setVisibility(View.GONE);

                    // determine sections that should show section header
                    determineSections(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }
        });

        return joinedRoomsQuery;
    }

    /**
     * A helper method to determine the sections (month-year section) in the room recycler view
     * Reference: http://codetheory.in/android-dividing-listview-sections-group-headers/
     *
     * @param dataSnapshot
     */
    public void determineSections(DataSnapshot dataSnapshot) {
        // clear the section-roomUid map
        sectionStartsAtRoomUid.clear();
        HashMap<String, Long> sectionTimeStamp = new HashMap<>();

        // iterate through every room
        for (DataSnapshot roomDataSnapshot : dataSnapshot.getChildren()) {
            Room room = roomDataSnapshot.getValue(Room.class);

            if (!room.members.containsKey(getCurrentUserUid())) continue;

            // convert room's timestamp into month year
            String monthYear = getMonthYear(room.timestamp);
            if (!sectionTimeStamp.containsKey(monthYear))
                sectionTimeStamp.put(monthYear, room.timestamp);
            sectionTimeStamp.put(monthYear, Math.max(room.timestamp, sectionTimeStamp.get(monthYear)));
        }

        // now set sectionStartsAtRoomUid and show/hide section headers
        for (DataSnapshot roomDataSnapshot : dataSnapshot.getChildren()) {
            String roomUid = roomDataSnapshot.getKey();
            Room room = roomDataSnapshot.getValue(Room.class);

            if (!room.members.containsKey(getCurrentUserUid())) continue;

            // convert room's timestamp into month year
            String monthYear = getMonthYear(room.timestamp);
            sectionTimeStamp.put(monthYear, Math.max(room.timestamp, sectionTimeStamp.get(monthYear)));

            // if this is the most recent timestamp of the section, set it in hash map
            if (room.timestamp == sectionTimeStamp.get(monthYear)) {
                // put this roomUid in the map for monthYear
                sectionStartsAtRoomUid.put(monthYear, roomUid);

                // if viewholder for this room has already been made, simply show section header
                if (roomUidRoomViewHolder.containsKey(roomUid)) {
                    roomUidRoomViewHolder.get(roomUid).showSectionHeader();
                } // else do nothing, because when it is made, it will show the header
            } else {
                // this means this roomUid is not the most recent of the section
                // if this viewholder has already been made, hide the section header
                if (roomUidRoomViewHolder.containsKey(roomUid)) {
                    roomUidRoomViewHolder.get(roomUid).hideSectioHeader();
                }
            }

        }


    }
}
