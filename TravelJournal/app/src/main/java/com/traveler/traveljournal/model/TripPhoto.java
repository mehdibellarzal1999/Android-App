package com.traveler.traveljournal.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TripPhoto implements Parcelable {
    private String photoUrl;
    private TripLocation location;

    public TripPhoto() {}
    public TripPhoto(String photoUrl, TripLocation location) {
        this.photoUrl = photoUrl;
        this.location = location;
    }

    protected TripPhoto(Parcel in) {
        photoUrl = in.readString();
        location = in.readParcelable(TripLocation.class.getClassLoader());
    }

    public static final Creator<TripPhoto> CREATOR = new Creator<TripPhoto>() {
        @Override
        public TripPhoto createFromParcel(Parcel in) {
            return new TripPhoto(in);
        }

        @Override
        public TripPhoto[] newArray(int size) {
            return new TripPhoto[size];
        }
    };

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public TripLocation getLocation() {
        return location;
    }

    public void setLocation(TripLocation location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(photoUrl);
        parcel.writeParcelable(location, i);
    }
}
