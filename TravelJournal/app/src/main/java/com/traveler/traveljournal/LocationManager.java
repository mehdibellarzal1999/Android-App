package com.traveler.traveljournal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.traveler.traveljournal.ui.StartTripActivity;

public class LocationManager {

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private final Context context;
    private LocationRequest locationRequest;

    public LocationManager(Context context, LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(LocationCallback callback) {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
        );
    }

    public Task<LocationSettingsResponse> createLocationRequest() {
        locationRequest = new LocationRequest
                .Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .build();

        LocationSettingsRequest locationSettings = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build();

        SettingsClient client = LocationServices.getSettingsClient(context);
        return client.checkLocationSettings(locationSettings);
    }

    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

}
