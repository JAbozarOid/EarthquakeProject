package com.aratel.earthquake;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aratel.earthquake.viewmodel.EarthquakeViewModel;
import com.aratel.earthquake.viewmodel.MyViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EarthquakeMainActivity extends AppCompatActivity implements EarthquakeListFragment.OnListFragmentInteractionListener{

    private static final String TAG_LIST_FRAGMENT = "TAG_LIST_FRAGMENT";

    private static final int MENU_PREFERENCES = Menu.FIRST+1;
    private static final int SHOW_PREFERENCES = 1;

    private EarthquakeViewModel earthquakeViewModel;

    EarthquakeListFragment mEarthquakeListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_main);

        FragmentManager fm = getSupportFragmentManager();

        // Android will automatically re-add any Fragments that
        // have previously been added after a configuration change,
        // so only add it if this isn't an automatic restart.
        if (savedInstanceState == null) {
            FragmentTransaction ft = fm.beginTransaction();

            mEarthquakeListFragment = new EarthquakeListFragment();
            ft.add(R.id.main_activity_frame, mEarthquakeListFragment, TAG_LIST_FRAGMENT);
            ft.commitNow();
        } else {
            mEarthquakeListFragment = (EarthquakeListFragment) fm.findFragmentByTag(TAG_LIST_FRAGMENT);
        }

        //********** use earthquake mock data begin
        //Date now = Calendar.getInstance().getTime();
        //List<Earthquake> dummyQuakes = new ArrayList<Earthquake>(0);
        //dummyQuakes.add(new Earthquake("0", now, "San Jose", null, 7.3, null));
        //dummyQuakes.add(new Earthquake("1", now, "LA", null, 6.5, null));

        //mEarthquakeListFragment.setEarthquakes(dummyQuakes);
        //********** use earthquake mock data end

        //********** use earthquake real data from xml feed begin
        // Retrieve the Earthquake View Model for this Activity.
        earthquakeViewModel = ViewModelProviders.of(this).get(EarthquakeViewModel.class);

        //********** use earthquake real data from xml feed end


        //********** check connection status begin
        //obtain (or create) an instance of the view model
        // for using check connection status
        // MyViewModel myViewModel = ViewModelProviders.of(this).get(MyViewModel.class);

        //Get the current data and observe it for changes
        // checking internet connection
        /*myViewModel.getData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String strings) {
                if (strings != null) {
                    Toast.makeText(EarthquakeMainActivity.this, "connected", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(EarthquakeMainActivity.this, "disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        //********** check connection status end
    }

    @Override
    public void onListFragmentRefreshRequested() {
        updateEarthquakes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0,MENU_PREFERENCES,Menu.NONE,R.string.menu_settings);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent intent = new Intent(this,PreferencesActivity.class);
                startActivityForResult(intent,SHOW_PREFERENCES);
                return true;
        }
        return false;
    }

    private void updateEarthquakes() {
        // Request the view model update the earthquakes from the USGS feed.
        earthquakeViewModel.loadEarthquakes();
    }
}
