package com.marcindziedzic.smartparkingsandroid.mapsFeature;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marcindziedzic.smartparkingsandroid.R;
import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerInterface;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.util.LocationPermissions;
import com.marcindziedzic.smartparkingsandroid.util.LocationPermissionsUtil;

import java.util.ArrayList;
import java.util.Iterator;

import jade.core.MicroRuntime;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsContract.View {


    private static final String TAG = "MapsActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1111;
    private static final float DEFAULT_ZOOM = 14f;

    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;

    ImageButton recentreButton;
    Button parkNowButton;
    Button navigateToButton;
    Button driveButton;


    String agentName;
    DriverManagerInterface driverManagerInterface;
    private ParkingOffer choosenParking;
    private ArrayList<Marker> parkingMarkers = new ArrayList<>();
    private MapsContract.Presenter mapsPresenter;
    private LocationPermissions locationPermissions;
    private ImageButton refreshParkingDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initViews();

        initPresenter();
        initLocationPermissions();
        mapsPresenter.resolveLocationPermissions(locationPermissions);


        agentName = getIntent().getStringExtra("agentName");
        bindAgent(agentName);

    }

    private void initLocationPermissions() {
        locationPermissions = new LocationPermissionsUtil(this);
    }

    private void initPresenter() {
        mapsPresenter = new MapsPresenter(this);
    }

    private void initViews() {
        recentreButton = findViewById(R.id.recentreButton);
        recentreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationPermissions.recenter();
            }
        });

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
                showProposedParking(choosenParking);
            }
        });

        refreshParkingDataButton = findViewById(R.id.refreshParkingDataButton);
        refreshParkingDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverManagerInterface.getParkings(new DriverManagerInterface.GetParkingsInfoCallback() {
                    @Override
                    public void onParkingDataCollected(final ArrayList<ParkingOffer> parkingData) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAvailableParkings(parkingData);
                            }
                        });
                    }
                });
            }
        });
    }

    private void bindAgent(String agentName) {
        try {
            driverManagerInterface = MicroRuntime.getAgent(agentName)
                    .getO2AInterface(DriverManagerInterface.class);
        } catch (StaleProxyException e) {
            Log.e(TAG, "onCreate: internal error");
        } catch (ControllerException e) {
            Log.e(TAG, "onCreate:  server error");
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();

        mMap = googleMap;

        locationPermissions.setMap(mMap);
        mapsPresenter.setLocationEnabled(locationPermissions);

        driverManagerInterface.getParkings(new DriverManagerInterface.GetParkingsInfoCallback() {
            @Override
            public void onParkingDataCollected(final ArrayList<ParkingOffer> parkingData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAvailableParkings(parkingData);
                    }
                });
            }
        });
    }

    private void showAvailableParkings(ArrayList<ParkingOffer> parkings) {
        Log.d(TAG, "showAvailableParkings: ");
        for (ParkingOffer p : parkings) {
            Log.d(TAG, "showAvailableParkings: Current parking data: " + p.getLat() + p.getLon());
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

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void showProposedParking(ParkingOffer chosenParking) {
        final LatLng latLng = new LatLng(chosenParking.getLat(), chosenParking.getLon());
        removeMarker(latLng);
        addMarkerOfChosenParking(latLng);
        prepareButtonsForProposal();
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