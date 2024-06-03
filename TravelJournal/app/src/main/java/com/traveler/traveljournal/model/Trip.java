package com.traveler.traveljournal.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.traveler.traveljournal.TimeConverter;

import java.util.ArrayList;
import java.util.Objects;

@IgnoreExtraProperties
public class Trip {
    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private String city;
    private String country;

    private String thumbnail;

    private ArrayList<TripPhoto> photos;

    private ArrayList<TripLocation> locations;


    public Trip() {
        photos = new ArrayList<>();
        locations = new ArrayList<>();
    }

    public Trip(String name, String startTime, String endTime, String city, String country, String thumbnail, ArrayList<TripPhoto> tripPhotos, ArrayList<TripLocation> tripLocation) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.city = city;
        this.country = country;
        this.thumbnail = thumbnail;
        this.photos = tripPhotos;
        this.locations = tripLocation;
    }

    public void addPhoto(TripPhoto tripPhoto) {
        photos.add(tripPhoto);
    }

    public void addLocation(TripLocation tripLocation) {
        locations.add(tripLocation);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public ArrayList<TripPhoto> getPhotos() {
        return photos;
    }

    public ArrayList<TripLocation> getLocations() {
        return locations;
    }

    @Exclude
    public TripLocation getLastLocation() {
        if (locations.isEmpty()) return null;
        return locations.get(locations.size() - 1);
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", locations=" + locations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(id, trip.id) && Objects.equals(name, trip.name) && Objects.equals(startTime, trip.startTime) && Objects.equals(endTime, trip.endTime) && Objects.equals(city, trip.city) && Objects.equals(country, trip.country) && Objects.equals(thumbnail, trip.thumbnail) && Objects.equals(photos, trip.photos) && Objects.equals(locations, trip.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startTime, endTime, city, country, thumbnail, photos, locations);
    }
}
