package edu.monash.fit3027.breakingbills.fragments;

import android.annotation.SuppressLint;
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
 * A fragment class to visually show a receipt in fullscreen view using Glide.
 *
 * Reference:
 *  1. CameraDemo by Josh Olsen, provided in Moodle for full screen photoview setup
 *  2. https://github.com/bumptech/glide for glide related code
 *
 * Created by Callistus on 5/5/2017.
 */

public class FullScreenReceiptFragment extends Fragment {

    private String roomUid;
    private String photoUid;

    public FullScreenReceiptFragment() {}

    @SuppressLint("ValidFragment")
    public FullScreenReceiptFragment(String roomUid, String photoUid) {
        this.roomUid = roomUid;
        this.photoUid = photoUid;
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
