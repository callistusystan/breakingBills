package edu.monash.fit3027.breakingbills;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import edu.monash.fit3027.breakingbills.models.Library;
import edu.monash.fit3027.breakingbills.viewholders.LibraryViewHolder;

/**
 * A recycler view adapter for use in the About App screen's recycler view.
 *
 * References:
 *  1. https://www.binpress.com/tutorial/android-l-recyclerview-and-cardview-tutorial/156 for learning
 *      how to implement a recycler view adapter
 *
 * Created by Callistus on 1/6/2017.
 */

public class LibraryAdapter extends RecyclerView.Adapter<LibraryViewHolder> {

    // an arrayList of all the libraries
    private ArrayList<Library> libraries;

    public LibraryAdapter(ArrayList<Library> libraries) {
        this.libraries = libraries;
    }

    // required methods due to inheritance
    @Override
    public LibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the view
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_library, parent, false);

        return new LibraryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder holder, int position) {
        // set view holder views base on model
        holder.setView(libraries.get(position));
    }

    @Override
    public int getItemCount() {
        // return number of elements in list
        return libraries.size();
    }

}