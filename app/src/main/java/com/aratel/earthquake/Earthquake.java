package com.aratel.earthquake;

import android.location.Location;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Earthquake {

    private String mId;
    private Date mDate;
    private String mDetails;
    private Location mLocation;
    private double mMagnitude;
    private String mLink;

    public String getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    public String getDetails() {
        return mDetails;
    }

    public Location getLocation() {
        return mLocation;
    }

    public double getMagnitude() {
        return mMagnitude;
    }

    public String getLink() {
        return mLink;
    }

    public Earthquake(String id, Date date, String details, Location location, double magnitude, String link) {
        this.mId = id;
        this.mDate = date;
        this.mDetails = details;
        this.mLocation = location;
        this.mMagnitude = magnitude;
        this.mLink = link;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH.mm", Locale.US);
        String dateString = sdf.format(this.mDate);
        return dateString + ": " + this.mMagnitude + " " + this.mDetails;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Earthquake)
            return ((Earthquake) obj).getId().contentEquals(mId);
        else
            return super.equals(obj);
    }
}
