package com.aratel.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aratel.earthquake.viewmodel.EarthquakeViewModel;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeListFragment extends Fragment {

    private ArrayList<Earthquake> mEarthquake = new ArrayList<Earthquake>();
    private RecyclerView mRecyclerView;
    private EarthquakeRecyclerViewAdapter mEarthquakeAdapter = new EarthquakeRecyclerViewAdapter(mEarthquake);

    protected EarthquakeViewModel earthquakeViewModel;
    private SwipeRefreshLayout mSwipeToRefreshView;
    private OnListFragmentInteractionListener mListener;

    private int mMinimumMagnitude = 0;

    public EarthquakeListFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (OnListFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // use this method when using ViewModel LiveData and real xml feed data for list of earthquakes
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Retrieve the Earthquake View Model for the parent Activity.
        earthquakeViewModel = ViewModelProviders.of(getActivity()).get(EarthquakeViewModel.class);

        // Get the data from the View Model, and observe any changes.
        earthquakeViewModel.getEarthquakes().observe(getViewLifecycleOwner(), new Observer<List<Earthquake>>() {
            @Override
            public void onChanged(List<Earthquake> earthquakes) {
                // When the view model changes , update the list
                if (earthquakes != null) {
                    setEarthquakes(earthquakes);
                }
            }
        });

        // Register an OnSharedPreferenceChangeListener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.registerOnSharedPreferenceChangeListener(mPrefListener);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PreferencesActivity.PREF_MIN_MAG.equals(key)) {
                List<Earthquake> earthquakes
                        = earthquakeViewModel.getEarthquakes().getValue();
                if (earthquakes != null)
                    setEarthquakes(earthquakes);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_earthquake_list, container, false);
        mRecyclerView = view.findViewById(R.id.list);
        mSwipeToRefreshView = view.findViewById(R.id.swiperefresh);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the Recycler View adapter
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mEarthquakeAdapter);

        //setup the swipe to refresh view
        mSwipeToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateEarthquake();
            }
        });
    }

    //Define a new OnListFragmentInteractionListener within the
    //Earthquake List Fragment; it should include an onListFragmentRefreshRequested method
    //that’s called when we request a refresh via the updateEarthquakes method
    public interface OnListFragmentInteractionListener {
        void onListFragmentRefreshRequested();
    }


    protected void updateEarthquake() {
        if (mListener != null) {
            mListener.onListFragmentRefreshRequested();
        }
    }

    // Update the setEarthquakes method to disable the “refreshing” visual indicator when an
    //update has been received:
    public void setEarthquakes(List<Earthquake> earthquakes) {
        mEarthquake.clear();
        mEarthquakeAdapter.notifyDataSetChanged();

        updateFromPreferences();

        for (Earthquake earthquake : earthquakes
        ) {
            if (earthquake.getMagnitude() >= mMinimumMagnitude) {
                // check for duplicates
                if (!mEarthquake.contains(earthquake)) {
                    mEarthquake.add(earthquake);
                    mEarthquakeAdapter.notifyItemInserted(earthquakes.indexOf(earthquake));
                }
            }

        }

        if (mEarthquake != null && mEarthquake.size() > 0)
            for (int i = mEarthquake.size() - 1; i >= 0; i--) {
                if (mEarthquake.get(i).getMagnitude() < mMinimumMagnitude) {
                    mEarthquake.remove(i);
                    mEarthquakeAdapter.notifyItemRemoved(i);
                }
            }
        mSwipeToRefreshView.setRefreshing(false);
    }

    // this method in this class reads the Shared Preference minimum magnitude value
    private void updateFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        mMinimumMagnitude = Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
    }
}
