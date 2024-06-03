package com.traveler.traveljournal.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TripLocation implements Parcelable {
    private double latitude;
    private double longitude;

    public TripLocation() {}

    public TripLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected TripLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<TripLocation> CREATOR = new Creator<TripLocation>() {
        @Override
        public TripLocation createFromParcel(Parcel in) {
            return new TripLocation(in);
        }

        @Override
        public TripLocation[] newArray(int size) {
            return new TripLocation[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public String toString() {
        return "TripLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
