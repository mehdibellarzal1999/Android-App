package com.traveler.traveljournal.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.GpxHelper;
import com.traveler.traveljournal.LocationManager;
import com.traveler.traveljournal.R;
import com.traveler.traveljournal.TimeConverter;
import com.traveler.traveljournal.databinding.ActivityStartTripBinding;
import com.traveler.traveljournal.databinding.SaveTripDialogBinding;
import com.traveler.traveljournal.model.Trip;
import com.traveler.traveljournal.model.TripLocation;
import com.traveler.traveljournal.model.TripPhoto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StartTripActivity extends AppCompatActivity {

    private ActivityStartTripBinding binding;
    private SaveTripDialogBinding dialogBinding;
    private boolean isChronometerRunning = false;
    private boolean shouldStartLocationUpdates = false;
    private Trip trip;
    private Bitmap photoBitmap;
    private LocationCallback locationCallback;
    private LocationManager locationManager;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private AlertDialog dialog;

    private ActivityResultLauncher<Intent> captureLauncher;
    private ActivityResultLauncher<String> permissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeFirebase();
        setLocationCallback();
        initializeDialog();
        locationManager = new LocationManager(this, locationCallback);

        binding.startTripBtn.setOnClickListener(view -> {
            if (!isPermissionGranted()) {
                requestPermissions();
                return;
            }
            startRecordingLocation();
        });

        binding.startTripEndTripBtn.setOnClickListener(view -> {
            dialog.show();
        });

        binding.startTripBackBtn.setOnClickListener(view -> {
            locationManager.stopLocationUpdates();
            endChronometer();
            finish();
        });

        captureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        Bundle extras = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) extras.get("data");
                        setPhotoPreview(bitmap);

                    }
                });

        binding.startTripTakePhotoBtn.setOnClickListener(view -> {
            openCamera();
        });

        binding.startTripUploadBtn.setOnClickListener(view -> {
            binding.startTripUploadBtn.setVisibility(View.INVISIBLE);
            binding.startTripUploadProgress.setVisibility(View.VISIBLE);
            byte[] imageBytes = getImageBytes(photoBitmap);
            uploadPhoto(imageBytes);
        });

        setPermissionActivityResult();
    }


    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void uploadPhoto(byte[] bytes) {
        String photoName = String.valueOf(System.currentTimeMillis());
        storage.getReference().child(photoName)
                .putBytes(bytes)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storage.getReference().child(photoName)
                                .getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    savePhoto(uri.toString());
                                    binding.startTripUploadBtn.setVisibility(View.GONE);
                                    binding.startTripThumbnailChip.setVisibility(View.GONE);
                                    binding.startTripUploadProgress.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void savePhoto(String photoUrl) {
        TripLocation currentLocation = trip.getLastLocation();
        if(currentLocation == null){
            return;
        }
        TripLocation photoLocation = new TripLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
        TripPhoto photo = new TripPhoto(photoUrl, photoLocation);
        trip.addPhoto(photo);
        if (binding.startTripThumbnailChip.isChecked())
            trip.setThumbnail(photoUrl);
    }

    private void setPhotoPreview(Bitmap bitmap) {
        this.photoBitmap = bitmap;
        binding.startTripThumbnailChip.setChecked(false);
        binding.startTripPhotoPreviewImv.setImageBitmap(bitmap);
        binding.startTripUploadBtn.setVisibility(View.VISIBLE);
        binding.startTripThumbnailChip.setVisibility(View.VISIBLE);
    }

    private byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            captureLauncher.launch(takePictureIntent);
        }
    }

    private void saveTrip(String tripName) {
        trip.setName(tripName);
        trip.setEndTime(TimeConverter.convertTimeToString(System.currentTimeMillis()));
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
        dialogBinding.saveTripSaveBtn.setVisibility(View.INVISIBLE);
        dialogBinding.saveTripProgress.setVisibility(View.VISIBLE);

        DocumentReference document = firestore.collection(Constants.USER_COLLECTION).document(firebaseUser.getUid())
                .collection(Constants.TRIP_COLLECTION).document();
        trip.setId(document.getId());

        document.set(trip)
                .addOnCompleteListener(task -> {
                    dialogBinding.saveTripSaveBtn.setVisibility(View.VISIBLE);
                    dialogBinding.saveTripProgress.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.save_trip_dialog, null);
        dialogBinding = SaveTripDialogBinding.bind(dialogView);
        dialog = createDialog(dialogBinding.getRoot());

        dialogBinding.saveTripSaveBtn.setOnClickListener(view -> {
            String tripName = Objects.requireNonNull(dialogBinding.saveTripEdt.getText()).toString();
            if (tripName.isEmpty()) return;
            stopRecordLocation();
            saveTrip(tripName);
        });

        dialogBinding.saveTripShareGpxBtn.setOnClickListener(view -> {
            GpxHelper gpxHelper = new GpxHelper(this, trip.getLocations());

            if (writePermissionGranted()) {
                Uri fileUri = gpxHelper.createGpxFile();
                shareFile(fileUri);
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });
    }

    private boolean writePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private void setPermissionActivityResult() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void shareFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_SUBJECT, "My Trip's GPX file");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        startActivity(Intent.createChooser(intent, "Send GPX File"));
    }


    private AlertDialog createDialog(View view) {
        AlertDialog newDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(view)
                .create();
        newDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return newDialog;
    }


    private void stopRecordLocation() {
        resetViews();
        locationManager.stopLocationUpdates();
        endChronometer();
    }

    private void resetViews() {
        binding.startTripBtn.setVisibility(View.VISIBLE);
        binding.tripStartedGroup.setVisibility(View.GONE);
        binding.startTripThumbnailChip.setVisibility(View.GONE);
        binding.startTripUploadBtn.setVisibility(View.GONE);
        binding.startTripPhotoPreviewImv.setImageResource(R.drawable.place_holder);
    }

    private void startRecordingLocation() {
        locationManager.createLocationRequest()
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    trip = new Trip();
                    trip.setStartTime(TimeConverter.convertTimeToString(System.currentTimeMillis()));
                    startChronometer();
                    binding.startTripBtn.setVisibility(View.GONE);
                    binding.tripStartedGroup.setVisibility(View.VISIBLE);
                    locationManager.startLocationUpdates(locationCallback);
                    shouldStartLocationUpdates = true;
                })
                .addOnFailureListener(this, e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(StartTripActivity.this, 0);

                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (shouldStartLocationUpdates) {
            locationManager.startLocationUpdates(locationCallback);
            Log.d("LocationUpdates", "onResume: location updates Resumed");

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("LocationUpdates", "onPause: location updates stopped");
        locationManager.stopLocationUpdates();
    }


    private void setLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateTripInfo(location);
                    }
                }
            }
        };
    }

    private void updateTripInfo(Location location) {
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            TripLocation tripLocation = new TripLocation(latitude, longitude);
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses.isEmpty())
                return;
            Address address = addresses.get(0);
            trip.setCity(address.getLocality());
            trip.setCountry(address.getCountryName());
            trip.addLocation(tripLocation);
            Log.d("onLocationResult", "onLocationResult: " + tripLocation);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private boolean isPermissionGranted() {
        boolean fineLocationGranted = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean coarseLocationGranted = PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return fineLocationGranted && coarseLocationGranted;
    }


    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            locationManager.createLocationRequest();

                        }
                    }
            );


    private void startChronometer() {
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        binding.chronometer.start();
        isChronometerRunning = true;
    }

    private void endChronometer() {
        if (!isChronometerRunning) return;
        binding.chronometer.stop();
        isChronometerRunning = false;
    }
}