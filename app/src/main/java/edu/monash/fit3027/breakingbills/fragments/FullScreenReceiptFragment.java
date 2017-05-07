package edu.monash.fit3027.breakingbills.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import edu.monash.fit3027.breakingbills.R;

/**
 * Created by Callistus on 5/5/2017.
 */

public class FullScreenReceiptFragment extends Fragment {

    public static final String PHOTO_URI = "PHOTO_URI";

    private String photoUri;

    public FullScreenReceiptFragment() {}

    public FullScreenReceiptFragment(String uri) {
        photoUri = uri;
        Bundle bundle = new Bundle(1); // Set bundle with a capacity of 1
        bundle.putString(PHOTO_URI, photoUri);
        this.setArguments(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_screen_receipt, container, false);
        ImageView imageView = (ImageView)v.findViewById(R.id.fragment_full_screen_receipt_image);

        // Load the receipt using Glide
        Glide.with(getContext())
                .load(photoUri)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontAnimate()
                .into(imageView);

        imageView.setOnClickListener((View.OnClickListener) getActivity());

        return v;
    }


}
