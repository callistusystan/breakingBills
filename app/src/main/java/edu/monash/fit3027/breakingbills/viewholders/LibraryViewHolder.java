package edu.monash.fit3027.breakingbills.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.monash.fit3027.breakingbills.R;
import edu.monash.fit3027.breakingbills.models.Library;

/**
 * A list view holder to visually show library information in the about app screen.
 *
 * Reference:
 *  1. https://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156 for
 *      learning how to implement view holders for recycler views
 *
 * Created by Callistus on 1/6/2017.
 */

public class LibraryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    // the view holder's view
    View itemView;

    // image views
    ImageView iconImageView;
    ImageView arrow;

    // text views
    TextView nameTextView;
    TextView detailTextView;
    TextView licenseTextView;

    // the model of this view holder
    Library library;

    private boolean showExpanded = false;

    public LibraryViewHolder(View itemView) {
        super(itemView);

        // set the itemview
        this.itemView = itemView;

        // link all the views
        iconImageView = (ImageView) itemView.findViewById(R.id.item_library_icon);
        arrow = (ImageView) itemView.findViewById(R.id.item_library_arrow);

        nameTextView = (TextView) itemView.findViewById(R.id.item_library_name);
        detailTextView = (TextView) itemView.findViewById(R.id.item_library_detail);
        licenseTextView = (TextView) itemView.findViewById(R.id.item_library_licenseNotice);

        // hide the license view
        licenseTextView.setVisibility(View.GONE);
    }

    /**
     * A helper method to set all the views in this view holder.
     * Also used to instantiate the library attribute
     *
     * @param library
     */
    public void setView(Library library) {
        // store in attribute
        this.library = library;

        // set the icon
        iconImageView.setImageResource(library.getIcon());

        // set the text views
        nameTextView.setText(library.getName());
        detailTextView.setText(library.getDetail());
        licenseTextView.setText(library.getLicense());

        // set an onclick listener that shows/hide the license notice
        this.itemView.setOnClickListener(this);
    }

    /**
     * A helper method to show/Hide the license notice
     */
    public void toggle() {
        showExpanded = !showExpanded;
        if (showExpanded) {
            licenseTextView.setVisibility(View.VISIBLE);
            arrow.setImageResource(R.drawable.ic_up);
        } else {
            licenseTextView.setVisibility(View.GONE);
            arrow.setImageResource(R.drawable.ic_down);
        }
    }

    @Override
    public void onClick(View v) {
        toggle();
    }
}
