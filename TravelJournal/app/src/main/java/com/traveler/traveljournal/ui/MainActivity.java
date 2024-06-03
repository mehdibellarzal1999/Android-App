package com.traveler.traveljournal.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.GpxHelper;
import com.traveler.traveljournal.databinding.ActivityMainBinding;
import com.traveler.traveljournal.model.Trip;
import com.traveler.traveljournal.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TripListener {

    private ActivityMainBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private User user;
    private TripsAdapter tripsAdapter;
    private List<Trip> allTrips = new ArrayList<>();

    private ActivityResultLauncher<String> permissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeFirebase();
        fetchUserData();


        binding.homeLogoutBtn.setOnClickListener(view -> {
            logOut();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        initRecyclerView();

        binding.homeStartTripBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, StartTripActivity.class));
        });

        binding.homeSettingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        setPermissionActivityResult();

        binding.exploreHeaderSearchSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryTrips(newText);
                return false;
            }
        });

    }

    private void queryTrips(String query) {
         List<Trip> filteredTrips = allTrips.stream()
                .filter(trip -> trip.getName().contains(query))
                .collect(Collectors.toList());
         tripsAdapter.setData(filteredTrips);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTrips();
    }



    private void fetchTrips() {
        binding.homeTripsProgress.setVisibility(View.VISIBLE);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
        ArrayList<Trip> trips = new ArrayList<>();
        firestore.collection(Constants.USER_COLLECTION)
                .document(firebaseUser.getUid())
                .collection(Constants.TRIP_COLLECTION)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            Trip trip = doc.toObject(Trip.class);
                            trips.add(trip);
                        }
                        allTrips = trips;
                        tripsAdapter.setData(trips);
                        binding.homeTripsProgress.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void fetchUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;
       firestore.collection(Constants.USER_COLLECTION)
               .document(firebaseUser.getUid())
               .get()
               .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       user = task.getResult().toObject(User.class);
                       displayUserInfo(user);
                   } else {
                       Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
    }

    private void initRecyclerView() {
        tripsAdapter = new TripsAdapter(MainActivity.this, this);
        binding.homeTripsRv.setLayoutManager(new LinearLayoutManager(this));
        binding.homeTripsRv.setAdapter(tripsAdapter);
        binding.homeTripsRv.setHasFixedSize(true);
    }

    private void displayUserInfo(User user) {
        if (user == null) return;
        binding.homeUsernameTv.setText(user.getUsername());
        binding.homeUserEmailTv.setText(user.getEmail());
    }

    private void logOut() {
        mAuth.signOut();
    }

    @Override
    public void onTripClicked(Trip trip) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putParcelableArrayListExtra("locations", trip.getLocations());
        intent.putParcelableArrayListExtra("photos", trip.getPhotos());
        startActivity(intent);
    }

    @Override
    public void onTripShareClicked(Trip trip) {
        GpxHelper gpxHelper = new GpxHelper(this, trip.getLocations());

        if (writePermissionGranted()) {
            Uri fileUri = gpxHelper.createGpxFile();
            shareFile(fileUri);
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
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

}