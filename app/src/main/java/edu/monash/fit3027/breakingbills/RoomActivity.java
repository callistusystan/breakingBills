package edu.monash.fit3027.breakingbills;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.monash.fit3027.breakingbills.fragments.PeopleFragment;
import edu.monash.fit3027.breakingbills.fragments.ReceiptFragment;

public class RoomActivity extends BaseActivity {

    // firebase components
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    // views
    private FragmentPagerAdapter pagerAdapter;
    private ViewPager viewPager;

    // room
    private String roomUid;
    private String roomTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // init firebase components
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

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

        // init views
        initViews();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void initViews() {
        // Create the adapter that will return a fragment for each section
        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] fragments = new Fragment[] {
                    new PeopleFragment(),
                    new ReceiptFragment()
            };
            private final String[] fragmentNames = new String[] {
                    "People",
                    "Receipts"
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

    public String getRoomUid() {
        return roomUid;
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
}
