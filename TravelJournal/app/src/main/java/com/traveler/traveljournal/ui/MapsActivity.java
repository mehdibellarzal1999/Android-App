package com.traveler.traveljournal.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.traveler.traveljournal.R;
import com.traveler.traveljournal.databinding.ActivityMapsBinding;
import com.traveler.traveljournal.model.TripLocation;
import com.traveler.traveljournal.model.TripPhoto;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private ArrayList<TripLocation> locations;
    private ArrayList<TripPhoto> photos;

    private AlertDialog photoDialog;
    private ImageView photoImv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createDialog();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            locations = getIntent().getParcelableArrayListExtra("locations", TripLocation.class);
            photos = getIntent().getParcelableArrayListExtra("photos", TripPhoto.class);
        } else {
            locations = getIntent().getParcelableArrayListExtra("locations");
            photos = getIntent().getParcelableArrayListExtra("photos");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        if (locations == null) return;
        if (!locations.isEmpty()) {
            TripLocation firstLocation = locations.get(0);
            TripLocation lastLocation = locations.get(locations.size() - 1);

            moveCameraToStartPoint(firstLocation);
            drawStartEndMarkers(firstLocation, lastLocation);
            drawPolyline();
            drawPhotosMarkers();
        }
    }

    private void moveCameraToStartPoint(TripLocation firstLocation) {
        LatLng firstLatLng = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 18));
    }

    private void drawPolyline() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getColor(R.color.blue));
        polylineOptions.width(8);

        for (TripLocation location : locations) {
            polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        mMap.addPolyline(polylineOptions);
    }

    private void drawStartEndMarkers(TripLocation firstLocation, TripLocation lastLocation) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_finish_flag);

        MarkerOptions startMarker = new MarkerOptions()
                .position(new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude()))
                .title("Trip Start");
        MarkerOptions finishMarker = new MarkerOptions()
                .position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .icon(icon)
                .title("Trip End");
        mMap.addMarker(startMarker);
        mMap.addMarker(finishMarker);
    }

    private void drawPhotosMarkers() {
        BitmapDescriptor photoIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_photo_marker);
        for (TripPhoto photo : photos) {
            TripLocation location = photo.getLocation();
            MarkerOptions photoMarker = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(photoIcon)
                    .title("Trip Photo");

            Marker marker = mMap.addMarker(photoMarker);
            if (marker == null) return;
            marker.setTag(photo.getPhotoUrl());
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String photoUrl = (String) marker.getTag();
        if (photoUrl == null) return false;
        Glide.with(photoImv).load(photoUrl).error(R.drawable.place_holder).into(photoImv);
        photoDialog.show();
        return true;
    }

    private void createDialog() {
        View view = getLayoutInflater().inflate(R.layout.marker_photo_dialog, null);
        photoDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(view)
                .create();
        photoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        photoImv = view.findViewById(R.id.photo_marker_image_imv);
    }
}