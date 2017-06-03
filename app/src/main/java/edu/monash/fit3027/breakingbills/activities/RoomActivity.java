package edu.monash.fit3027.breakingbills.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import edu.monash.fit3027.breakingbills.FirebaseHelper;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.Utils;
import edu.monash.fit3027.breakingbills.activities.BaseActivity;
import edu.monash.fit3027.breakingbills.fragments.PaymentFragment;
import edu.monash.fit3027.breakingbills.fragments.PeopleFragment;
import edu.monash.fit3027.breakingbills.fragments.ReceiptFragment;
import edu.monash.fit3027.breakingbills.models.Member;
import edu.monash.fit3027.breakingbills.models.Room;

import static java.lang.Math.abs;

/**
 * The room activity screen the user is brought to after tapping a room
 * in the main activity screen.
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for logic regarding tab layouts and viewPager fragments
 *
 * Created by Callistus on 29/4/2017.
 */

public class RoomActivity extends BaseActivity implements View.OnClickListener {

    public static final int RED = Color.parseColor("#e61610");
    public static final int ORANGE = Color.parseColor("#ffa124");
    public static final int GREEN = Color.parseColor("#5c983d");

    public LinearLayout roomStatusSection;
    public LinearLayout expandedSection;

    // views
    private FragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private TextView roomIdTextView;
    private TextView roomTotalCostTextView;
    private TextView roomTotalUnaccountedTextView;

    // image view
    private ImageView arrow;

    // room
    private String roomUid;
    private String roomTitle;

    private boolean isExpanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // ensure one instance is up
        FirebaseHelper.getInstance();

        // get passed data from intent
        Intent intent = getIntent();
        roomUid = intent.getStringExtra("roomUid");
        roomTitle = intent.getStringExtra("roomTitle");

        // init action bar
        setTitle(roomTitle);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // show a loading bar
        showProgressDialog();

        isExpanded = false;

        // init views
        initViews();
    }

    public void toggle() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            expandedSection.setVisibility(View.VISIBLE);
            arrow.setImageResource(R.drawable.ic_up);
        } else {
            expandedSection.setVisibility(View.GONE);
            arrow.setImageResource(R.drawable.ic_down);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.activity_room_roomStatusLinearLayout)
            toggle();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * A helper method to initialize the views on this activity
     */
    public void initViews() {
        roomStatusSection = (LinearLayout) findViewById(R.id.activity_room_roomStatusLinearLayout);
        roomStatusSection.setOnClickListener(this);

        expandedSection = (LinearLayout) findViewById(R.id.activity_room_roomBalanceLinearLayout);
        expandedSection.setVisibility(View.GONE);
        arrow = (ImageView) findViewById(R.id.activity_room_arrow);

        roomTotalCostTextView = (TextView) findViewById(R.id.activity_room_totalCost);
        roomTotalUnaccountedTextView = (TextView) findViewById(R.id.activity_room_totalUnaccounted);

        initExpandedSection();

        if (!isExpanded) toggle();

        // init roomId text view
        roomIdTextView = (TextView) findViewById(R.id.activity_room_roomIdTextView);
        roomIdTextView.setText("Room ID: " + roomUid);

        // Create the adapter that will return a fragment for each section
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] fragments = new Fragment[] {
                    new PeopleFragment(),
                    new ReceiptFragment(),
                    new PaymentFragment()
            };
            private final String[] fragmentNames = new String[] {
                    "People",
                    "Receipts",
                    "Payments"
            };
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }
            @Override
            public int getCount() {
                return fragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return fragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.activity_room_viewPager);
        viewPager.setAdapter(pagerAdapter);

        // link view pager to tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_room_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * A helper method to initialize the expanded section that shows the room's total and
     * amount that has been unaccounted for
     */
    public void initExpandedSection() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        Query roomQuery = databaseRef.child("rooms/"+roomUid);

        roomQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Room room = dataSnapshot.getValue(Room.class);
                roomTotalCostTextView.setText(Utils.convertLongToStringCurrency(room.totalAmount));

                long totalAccounted = 0;
                for (Map<String, Object> memberDetail : room.memberDetail.values()) {
                    Member member = new Member(memberDetail);

                    totalAccounted += member.cost;
                }
                if (room.totalAmount - totalAccounted < 0) {
                    roomTotalUnaccountedTextView.setText("-" + Utils.convertLongToStringCurrency(abs(room.totalAmount - totalAccounted)));
                    roomTotalUnaccountedTextView.setTextColor(ColorStateList.valueOf(RED));
                } else if (room.totalAmount == totalAccounted) {
                    roomTotalUnaccountedTextView.setText(Utils.convertLongToStringCurrency(abs(room.totalAmount - totalAccounted)));
                    roomTotalUnaccountedTextView.setTextColor(ColorStateList.valueOf(GREEN));
                } else {
                    roomTotalUnaccountedTextView.setText(Utils.convertLongToStringCurrency(room.totalAmount - totalAccounted));
                    roomTotalUnaccountedTextView.setTextColor(ColorStateList.valueOf(ORANGE));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // getter methods to get room metadata
    public String getRoomUid() {
        return roomUid;
    }

    public Room getRoom() { return FirebaseHelper.getInstance().getRooms().get(getRoomUid()); }

    public String getRoomHostUid() {
        return getRoom().hostUid;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // if user clicked on the back button, call finish lifecycler method
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
