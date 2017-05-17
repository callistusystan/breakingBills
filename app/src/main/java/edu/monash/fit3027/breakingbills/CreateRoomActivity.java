package edu.monash.fit3027.breakingbills;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;

public class CreateRoomActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_CONTACT = 100;

    // edit texts
    private EditText roomTitleEditText;
    private EditText nicknameEditText;
    private EditText totalAmountEditText;

    // buttons
    private FloatingActionButton createRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        // init views
        roomTitleEditText = (EditText) findViewById(R.id.activity_create_room_roomTitleEditText);
        nicknameEditText = (EditText) findViewById(R.id.activity_create_room_nicknameEditText);
        totalAmountEditText = (EditText) findViewById(R.id.activity_create_room_totalAmountEditText);

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
        showProgressDialog("Creating room");
        // get database ref
        final DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final String currentUserUid = getCurrentUserUid();

        final String roomTitle = roomTitleEditText.getText().toString().trim();
        final String nickname = nicknameEditText.getText().toString().trim();
        String amountString = totalAmountEditText.getText().toString().trim();
        final long totalAmount = Utils.convertStringToLongCurrency(amountString);

        // get new room's Uid

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://us-central1-breaking-bills.cloudfunctions.net/createRoom";

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String roomUid = response.get("roomUid").toString();

                            // Init room, with current user as the host
                            Room room = new Room(roomTitle, currentUserUid, totalAmount);

                            // Put current user as one of the room's members
                            room.members.put(currentUserUid, ServerValue.TIMESTAMP);

                            // Init member and details
                            Member member = new Member(nickname, true, Member.EXPECTING_PAYMENT);

                            room.memberDetail.put(currentUserUid, member.toMap());

                            // specify locations in db to update
                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put("/rooms/" + roomUid, room.toMap());

                            // update db
                            databaseRef.updateChildren(childUpdates);

                            setResult(RESULT_OK);
                            hideProgressDialog();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            hideProgressDialog();
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public boolean allFieldsEntered() {
        // returns true if all fields are entered
        boolean valid = true;
        EditText[] editTexts = { roomTitleEditText, totalAmountEditText, nicknameEditText };

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
            if (Utils.convertStringToLongCurrency(totalAmountEditText.getText().toString()) > 0)
                writeNewRoom();
            else {
                totalAmountEditText.setError("You must enter an amount greater than 0!");
            }
        }
    }
}
