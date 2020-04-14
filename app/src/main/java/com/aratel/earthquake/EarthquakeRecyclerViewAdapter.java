package com.aratel.earthquake;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The View Holder will be used to hold a reference to each View from the Earthquake item layout.
 * The role of the Earthquake Recycler View Adapter is to provide populated View layouts based on the list of Earthquakes it maintains
 */

public class EarthquakeRecyclerViewAdapter extends RecyclerView.Adapter<EarthquakeRecyclerViewAdapter.ViewHolder> {

    private final List<Earthquake> mEarthquake;

    public EarthquakeRecyclerViewAdapter(List<Earthquake> earthquake) {
        this.mEarthquake = earthquake;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_earthquake,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.earthquake = mEarthquake.get(position);
        holder.detailsView.setText(mEarthquake.get(position).toString());
    }


    @Override
    public int getItemCount() {
        return mEarthquake.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final  View parentView;
        public final TextView detailsView;
        public Earthquake earthquake;

        public ViewHolder(@NonNull View view) {
            super(view);
            parentView = view;
            detailsView = view.findViewById(R.id.list_item_earthquake_details);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + detailsView.getText() + "'";
        }
    }
}


