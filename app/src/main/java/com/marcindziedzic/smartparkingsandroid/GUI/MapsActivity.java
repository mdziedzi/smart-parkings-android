package com.marcindziedzic.smartparkingsandroid.GUI;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marcindziedzic.smartparkingsandroid.R;
import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerInterface;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;

import java.util.ArrayList;
import java.util.Iterator;

import jade.core.MicroRuntime;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1111;
    private static final float DEFAULT_ZOOM = 14f;

    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationClient;

    ImageButton recentreButton;
    ImageButton settingsButton;
    Button parkNowButton;
    Button navigateToButton;
    Button driveButton;

    private Location mLocation;

    String nickname;
    DriverManagerInterface driverManagerInterface;
    private ParkingOffer choosenParking;
    private ArrayList<Marker> parkingMarkers = new ArrayList<>();

    private MyReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recentreButton = findViewById(R.id.recentreButton);
        recentreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), DEFAULT_ZOOM));
                }
            }
        });

        getLocationPermission();

        nickname = getIntent().getStringExtra("nickname");

        try {
            driverManagerInterface = MicroRuntime.getAgent(nickname)
                    .getO2AInterface(DriverManagerInterface.class);
        } catch (StaleProxyException e) {
            Log.e(TAG, "onCreate: internal error");
        } catch (ControllerException e) {
            Log.e(TAG, "onCreate:  server error");
        }

        driveButton = findViewById(R.id.driveButton);
        driveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + choosenParking.getLat() + "," + choosenParking.getLon() + "&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });

        navigateToButton = findViewById(R.id.navigateToButton);

        parkNowButton = findViewById(R.id.parkNowButton);
        parkNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosenParking = driverManagerInterface.getBestParking();
                showProposal(choosenParking);
            }
        });

        myReceiver = new MyReceiver();
        IntentFilter parkingDataReadyFilter = new IntentFilter();
        parkingDataReadyFilter.addAction("PARKING_DATA_READY");
        registerReceiver(myReceiver, parkingDataReadyFilter);
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: Received intent" + action);
            if (action.equalsIgnoreCase("PARKING_DATA_READY")) {
                showAviableParkings(driverManagerInterface.getParkings());
            }
        }
    }

    private void showProposal(ParkingOffer choosenParking) {
        final LatLng latLng = new LatLng(choosenParking.getLat(), choosenParking.getLon());
        removeMarker(latLng);
        addMarkerOfChosenParking(latLng);
        prepareButtonsForProposal();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();

        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void showAviableParkings(ArrayList<ParkingOffer> parkings) {
        for (ParkingOffer p : parkings) {
            parkingMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(p.getLat(), p.getLon())).title(String.valueOf(p.getPrice()))));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task<Location> location = mFusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                mLocation = currentLocation;
                                Log.d(TAG, "onComplete: mLocation: " + mLocation.getLatitude() + "; " + mLocation.getLongitude());
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            } else {
                                Toast.makeText(MapsActivity.this, "Error during fetching location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "onComplete: unable to get current loction");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SECURITY EXCEPTION " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to lat: " + latLng.latitude + ", lng " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void prepareButtonsForProposal() {
        parkNowButton.setEnabled(false);
        navigateToButton.setEnabled(false);
        driveButton.setVisibility(View.VISIBLE);
    }

    private void addMarkerOfChosenParking(LatLng latLng) {
        parkingMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(choosenParking.getPrice()))
                .snippet("to jest snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        ));
    }

    private void removeMarker(LatLng latLng) {
        Iterator<Marker> markerIterator = parkingMarkers.iterator();
        Marker currentMarker;
        while (markerIterator.hasNext()) {
            currentMarker = markerIterator.next();
            if (currentMarker.getPosition().equals(latLng)) {
                parkingMarkers.remove(currentMarker);
                currentMarker.remove();
                break;
            }
        }
    }
}