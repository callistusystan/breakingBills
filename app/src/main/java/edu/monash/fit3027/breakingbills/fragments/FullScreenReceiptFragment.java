package edu.monash.fit3027.breakingbills.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import edu.monash.fit3027.breakingbills.R;

/**
 * Created by Callistus on 5/5/2017.
 */

public class FullScreenReceiptFragment extends Fragment {

    public static final String PHOTO_URI = "PHOTO_URI";

    private String roomUid;
    private String photoUid;

    public FullScreenReceiptFragment() {}

    public FullScreenReceiptFragment(String roomUid, String photoUid) {
        this.roomUid = roomUid;
        this.photoUid = photoUid;
//        Bundle bundle = new Bundle(1); // Set bundle with a capacity of 1
//        bundle.putString(PHOTO_URI, this.photoUid);
//        this.setArguments(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_screen_receipt, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.fragment_full_screen_receipt_image);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("rooms/"+roomUid+"/receipts/"+photoUid+".jpg");

        // Load the receipt using Glide
        Glide.with(getContext())
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontAnimate()
                .into(imageView);

        imageView.setOnClickListener((View.OnClickListener) getActivity());

        return v;
    }


}
