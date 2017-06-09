package edu.monash.fit3027.breakingbills.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import edu.monash.fit3027.breakingbills.LibraryAdapter;
import edu.monash.fit3027.breakingbills.LicenseInfo;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Library;

/**
 * The about activity screen shows all libraries used in the making of this app.
 * Libraries:
 *  1. Firebase UI: https://github.com/firebase/FirebaseUI-Android
 *  1. Volley: https://github.com/google/volley
 *  2. Glide: https://github.com/bumptech/glide
 *  3. Human-Readable-Ids-JS: https://git.daplie.com/coolaj86/human-readable-ids-js
 *
 * References:
 *  1. https://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156 for recycler view adapter
 *
 * Created by Callistus on 1/6/2017.
 */

public class AboutAppActivity extends BaseActivity {

    // views
    private TextView appInfoTextView;
    private RecyclerView recyclerView;
    private LinearLayoutManager recyclerViewManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        // set action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        // init views
        initViews();
    }

    public void initViews() {
        // init app info text view
        appInfoTextView = (TextView) findViewById(R.id.activity_about_app_appInfo);

        appInfoTextView.setText(
                "Breaking Bills is an app built by Callistus Tan, that aims to facilitate the splitting of bills."
                + "\n\nUsing real-time connectivity, users may connect "
                + "to rooms in real time and collaborate to settle payments. "
                + "In Breaking Bills, receipts are readily available to all users, while "
                + "each group member's cost, payments are tracked as they are made."
        );

        // Set up Layout Manager, reverse layout so it shows most recent at the top
        recyclerViewManager = new LinearLayoutManager(this) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };

        // init rooms recycler view
        recyclerView = (RecyclerView) findViewById(R.id.activity_about_app_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(recyclerViewManager);

        // instantiate and put libraries in an arraylist
        ArrayList<Library> libraries = new ArrayList<>();
        libraries.add(new Library("Firebase/Firebase UI", R.mipmap.ic_firebase, LicenseInfo.firebaseDetail, LicenseInfo.firebaseLicense));
        libraries.add(new Library("Volley", R.mipmap.ic_volley, LicenseInfo.volleyDetail, LicenseInfo.volleyLicense));
        libraries.add(new Library("Glide", R.mipmap.ic_glide, LicenseInfo.glideDetail, LicenseInfo.glideLicense));
        libraries.add(new Library("Human-Readable-Ids-JS", R.mipmap.ic_nodejs, LicenseInfo.humanReadableIdsDetail, LicenseInfo.humanReadableIdsLicense));

        // create the adapter, and take note of libraries
        LibraryAdapter adapter = new LibraryAdapter(libraries);

        // set the recycler view's adapter
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // if the exit button is tapped, call the finish lifecycle method
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
