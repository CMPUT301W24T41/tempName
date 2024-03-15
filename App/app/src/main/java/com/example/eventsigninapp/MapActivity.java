package com.example.eventsigninapp;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, DatabaseController.GetCheckInLocationCallback {
    Button backButton;
    DatabaseController dbController;
    ArrayList<Location> locations;
    Event event;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        backButton = findViewById(R.id.back_button);
        dbController = new DatabaseController();
        locations = new ArrayList<Location>();
        event = new Event();
        event.setUuid("0f3389e5-1b6c-4c3a-b2be-88d5d5ef0260");

        dbController.getCheckInLocationsFromFirestore(event, this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        backButton.setOnClickListener(listener -> {
            Intent goBack = new Intent(MapActivity.this, AttendeeListActivity.class);
            startActivity(goBack);
        });
    }

    private void addMarkersToMap() {
        for (Location loc : locations) {
            LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
            map.addMarker(new MarkerOptions().position(point));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        addMarkersToMap();
    }

    @Override
    public void onGetCheckInLocationCallback(Event event, ArrayList<?> checkInLocations) {
        for (int i = 0; i < checkInLocations.size(); i++) {
            GeoPoint gPoint = (GeoPoint) checkInLocations.get(i);
            Location loc = new Location("prov");
            loc.setLatitude(gPoint.getLatitude());
            loc.setLongitude(gPoint.getLongitude());
            locations.add(loc);
        }
    }

}
