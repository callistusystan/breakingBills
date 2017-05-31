package edu.monash.fit3027.breakingbills.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import edu.monash.fit3027.breakingbills.PhotoActivity;
import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.models.Receipt;
import edu.monash.fit3027.breakingbills.viewholders.ReceiptViewHolder;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment class to show all receipt images in a room. Also allows users in the room to capture
 * and upload images of receipts to Firebase storage
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for the Firebase recycler view
 *  2. https://firebase.google.com/docs/storage/android/upload-files for uploading images to Firebase storage
 *
 * Created by Callistus on 30/4/2017.
 */

public class ReceiptFragment extends RoomFragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // firebase components
    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    // views
    private LinearLayout emptyMessageLinearLayout;
    private RecyclerView receiptsRecylerView;
    private GridLayoutManager receiptsRecyclerViewManager;

    // button
    private FloatingActionButton cameraButton;

    // arraylist of uris
    private ArrayList<String> photoUids;

    public ReceiptFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRoomActivity((RoomActivity) getActivity());
        View rootView = inflater.inflate(R.layout.fragment_receipt, container, false);

        // init firebase components
        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://breaking-bills.appspot.com");

        // link views
        cameraButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_receipt_cameraButton);
        cameraButton.setOnClickListener(this);

        photoUids = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViews();
        initReceiptsRecyclerView();
    }

    public void initViews() {
        receiptsRecyclerViewManager = new GridLayoutManager(getActivity(), 2);

        // Setup GridView to display captured images
        receiptsRecylerView = (RecyclerView) getRoomActivity().findViewById(R.id.fragment_receipt_receiptsRecyclerView);

        receiptsRecylerView.setHasFixedSize(true);
        receiptsRecylerView.setLayoutManager(receiptsRecyclerViewManager);

        // init text view
        emptyMessageLinearLayout = (LinearLayout) getRoomActivity().findViewById(R.id.fragment_receipt_emptyView);
    }

    /**
     * A helper method to initialize the recycler view to visually show all receipts in the room
     */
    public void initReceiptsRecyclerView() {
        // Set up FirebaseRecyclerAdapter with the Query
        Query roomReceiptsQuery = getRoomReceiptsQuery();
        final FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder>
                        (Receipt.class, R.layout.item_receipt, ReceiptViewHolder.class, roomReceiptsQuery) {
                    @Override
                    protected void populateViewHolder(ReceiptViewHolder viewHolder, final Receipt model, final int position) {
                        getRoomActivity().hideProgressDialog();
                        final DatabaseReference receiptRef = getRef(position);

                        // Set click listener for the whole room view
                        final String receiptUid = receiptRef.getKey();
                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(getActivity(), PhotoActivity.class);

                                System.out.println(photoUids);

                                intent.putExtra("roomUid", getRoomActivity().getRoomUid());
                                intent.putStringArrayListExtra("photoUids", photoUids);
                                intent.putExtra("position", position);

                                startActivity(intent);
                            }
                        });

                        // set up the UI elements
                        viewHolder.bindToRoom(getRoomActivity().getRoomUid(), receiptUid, getContext());
                    }
                };
        receiptsRecylerView.setAdapter(recyclerAdapter);
    }

    /**
     * A helper method to get the query for returning the receipts in the current room
     * @return a query for all receipts in the room
     */
    public Query getRoomReceiptsQuery() {
        Query roomReceiptsQuery = databaseRef.child("rooms/"+getRoomActivity().getRoomUid()+"/receipts");

        final ReceiptFragment instance = this;

        roomReceiptsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // clear old list as we are going to repopulate it
                photoUids.clear();

                // iterate through every receipt, and add to list
                for (DataSnapshot receiptDataSnapshot : dataSnapshot.getChildren()) {
                    photoUids.add(receiptDataSnapshot.getKey());
                }

                // hide progress dialog as we are done
                instance.getRoomActivity().hideProgressDialog();

                if (dataSnapshot.getValue() == null) {
                    // if no images, show an empty message
                    receiptsRecylerView.setVisibility(View.GONE);
                    emptyMessageLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    // if there are images, show the recycler view
                    receiptsRecylerView.setVisibility(View.VISIBLE);
                    emptyMessageLinearLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                instance.getRoomActivity().hideProgressDialog();
            }
        });

        return roomReceiptsQuery;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.fragment_receipt_cameraButton) {
            capturePhoto();
        }
    }

    /**
     * A helper method to start an intent to capture a photo
     */
    public void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getRoomActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // on successful return from capturing a photo, upload image to firebase database and storage

            // get bitmap that was taken
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // convert bitmap into bytes and compress for efficiency
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBAOS = baos.toByteArray();

            // get the image uid from database
            final String imagedUid = databaseRef.child("rooms/" + getRoomActivity().getRoomUid() + "/receipts").push().getKey();

            // set the image storage ref
            String imageName = imagedUid;
            StorageReference imageRef = storageRef.child("rooms/" + getRoomActivity().getRoomUid() + "/receipts/" + imageName + ".jpg");

            getRoomActivity().showProgressDialog("Uploading image");

            // set up the upload task
            UploadTask uploadTask = imageRef.putBytes(dataBAOS);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    getRoomActivity().hideProgressDialog();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")
                    // get image uri
                    Uri imageUri = taskSnapshot.getDownloadUrl();

                    // prep an instance of image to upload to db
                    Receipt newReceipt = new Receipt(imageUri.toString(), 0);

                    // specify the database ref to update
                    DatabaseReference imageRef = databaseRef.child("rooms/"+getRoomActivity().getRoomUid()+"/receipts/"+imagedUid);

                    // update database
                    imageRef.setValue(newReceipt.toMap());
                }
            });
        }
    }
}
