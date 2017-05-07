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

    public void bindToRoom(Receipt receipt, Context context) {
        // receipt view

        // Load the receipt using Glide
        Glide.with(context)
                .load(receipt.uri)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fitCenter()
                .dontAnimate()
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        System.out.println("FAILED LOADING");
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        System.out.println("DONE LOADING");
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(imageView);
    }
}

