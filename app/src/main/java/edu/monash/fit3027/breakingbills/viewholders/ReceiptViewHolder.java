package edu.monash.fit3027.breakingbills.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Receipt;

/**
 * Created by Callistus on 3/5/2017.
 */

public class ReceiptViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout progressBar;
    public ImageView imageView;

    public ReceiptViewHolder(View itemView) {
        super(itemView);

        progressBar = (LinearLayout) itemView.findViewById(R.id.item_receipt_loader);
        imageView = (ImageView) itemView.findViewById(R.id.item_receipt_imageView);
    }

    public void bindToRoom(String roomUid, String receiptUid, Context context) {
        // receipt view
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("rooms/"+roomUid+"/receipts/"+receiptUid+".jpg");

        // Load the receipt using Glide
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .dontAnimate()
                .listener(new RequestListener<StorageReference, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                        System.out.println("FAILED LOADING");
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        System.out.println("DONE LOADING");
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }
}

