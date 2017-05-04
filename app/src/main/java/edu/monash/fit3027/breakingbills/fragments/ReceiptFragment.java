package edu.monash.fit3027.breakingbills.fragments;

/**
 * Created by Callistus on 30/4/2017.
 */

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

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.RoomActivity;
import edu.monash.fit3027.breakingbills.models.Receipt;
import edu.monash.fit3027.breakingbills.viewholders.ReceiptViewHolder;

import static android.app.Activity.RESULT_OK;

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

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViews();
        initReceiptsRecyclerView();
    }

    public void initViews() {
        // Set up Layout Manager, reverse layout so it shows most recent at the top
        receiptsRecyclerViewManager = new GridLayoutManager(getActivity(), 2);

        // Setup GridView to display captured images
        // You could use RecyclerView alternatively (much better performance)
        receiptsRecylerView = (RecyclerView) getRoomActivity().findViewById(R.id.fragment_receipt_receiptsRecyclerView);
//        receiptsRecylerView.addItemDecoration(new GridSpacingItemDecoration(2, 4, true, 0));

        receiptsRecylerView.setHasFixedSize(true);
        receiptsRecylerView.setLayoutManager(receiptsRecyclerViewManager);

        // init text view
        emptyMessageLinearLayout = (LinearLayout) getRoomActivity().findViewById(R.id.fragment_receipt_emptyView);
    }

    public void initReceiptsRecyclerView() {
        // Set up FirebaseRecyclerAdapter with the Query
        Query roomReceiptsQuery = getRoomReceiptsQuery();
        FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder> recyclerAdapter =
                new FirebaseRecyclerAdapter<Receipt, ReceiptViewHolder>
                        (Receipt.class, R.layout.item_receipt, ReceiptViewHolder.class, roomReceiptsQuery) {
                    @Override
                    protected void populateViewHolder(ReceiptViewHolder viewHolder, final Receipt model, int position) {
                        getRoomActivity().hideProgressDialog();
                        final DatabaseReference receiptRef = getRef(position);

                        // Set click listener for the whole room view
                        final String receiptUid = receiptRef.getKey();
                                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Launch RoomActivity on this room
//                                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
//
//                                intent.putExtra("roomUid", roomUid);
//                                intent.putExtra("roomTitle", model.title);
//
//                                startActivity(intent);
                            }
                        });

                        // set up the UI elements
                        viewHolder.bindToRoom(model, getContext());
                    }
                };
        receiptsRecylerView.setAdapter(recyclerAdapter);
    }

    public Query getRoomReceiptsQuery() {
        Query roomReceiptsQuery = databaseRef.child("rooms/"+getRoomActivity().getRoomUid()+"/receipts");

        final ReceiptFragment instance = this;

        roomReceiptsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(getRoomActivity());
                instance.getRoomActivity().hideProgressDialog();
                if (dataSnapshot.getValue() == null) {
                    receiptsRecylerView.setVisibility(View.GONE);
                    emptyMessageLinearLayout.setVisibility(View.VISIBLE);
                } else {
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

    public void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getRoomActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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
