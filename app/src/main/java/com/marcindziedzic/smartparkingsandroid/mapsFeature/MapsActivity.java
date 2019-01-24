package com.marcindziedzic.smartparkingsandroid.mapsFeature;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.marcindziedzic.smartparkingsandroid.R;
import com.marcindziedzic.smartparkingsandroid.agent.DriverInterface;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.settingsFeature.SettingsActivity;
import com.marcindziedzic.smartparkingsandroid.util.Constants;
import com.marcindziedzic.smartparkingsandroid.util.Localization;
import com.marcindziedzic.smartparkingsandroid.util.LocationPermissions;
import com.marcindziedzic.smartparkingsandroid.util.LocationPermissionsUtil;
import com.marcindziedzic.smartparkingsandroid.util.PreferencesRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jade.core.AID;
import jade.core.MicroRuntime;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * Activity that shows map and everything corresponded with it. This is the main activity in the
 * app.
 */
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
    EditText searchEditText;


    String agentName;
    DriverInterface driverInterface;
    private ParkingOffer choosenParking;
    private ArrayList<Marker> parkingMarkers = new ArrayList<>();
    private MapsContract.Presenter mapsPresenter;
    private LocationPermissions locationPermissions;
    private ImageButton refreshParkingDataButton;
    private ImageButton settingsButton;
    private Localization deviceLocation;
    private Address destinationAddress;
    private Button cancelProposalButton;
    private AID choosenParkingAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initViews();

        setDefaultPreferencesIfEmpty();

        initPresenter();
        initLocationPermissions();
        mapsPresenter.resolveLocationPermissions(locationPermissions);

        agentName = getIntent().getStringExtra("agentName");
        bindAgent(agentName);

    }

    /**
     * Sets default user preferences if the SharedPreferences is empty. It will be empty when the
     * app is running first time after installation.
     */
    private void setDefaultPreferencesIfEmpty() {
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        int myIntValue = sp.getInt(Constants.PREFERENCES_KEY, -1);
        if (myIntValue == -1) {
            sp.edit().putInt(Constants.PREFERENCES_KEY, Constants.DEFAULT_PREFERENCE_VALUE).apply();
        }
        PreferencesRepository.getInstance().setSeekBarValue(Constants.DEFAULT_PREFERENCE_VALUE);
    }

    /**
     * Initializes location permissions.
     */
    private void initLocationPermissions() {
        locationPermissions = new LocationPermissionsUtil(this);
    }

    /**
     * Initializes presenter.
     */
    private void initPresenter() {
        mapsPresenter = new MapsPresenter(this);
    }

    /**
     * Initialize all views e.g. Buttons. All views have set the listeners here.
     */
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
                driverInterface.sendReservationRequest();
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + choosenParking.getLat() + "," + choosenParking.getLon() + "&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });

        navigateToButton = findViewById(R.id.navigateToButton);
        navigateToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigateToButton");
                if (destinationAddress != null) {
                    driverInterface.getBestParkingNearbyDestination(new Localization
                            (destinationAddress.getLatitude(), destinationAddress.getLongitude()), new
                            DriverInterface
                                    .GetBestParkingCallback() {
                                @Override
                                public void onBestParkingFound(final ParkingOffer parkingOffer) {
                                    Log.d(TAG, "onBestParkingFound: ");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "run: ");
                                            choosenParking = parkingOffer;
                                            showProposedParking(parkingOffer);
                                            cancelProposalButton.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                }

            }
        });

        parkNowButton = findViewById(R.id.parkNowButton);
        parkNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: parkNowButton");
                driverInterface.getBestParkingNearby(new DriverInterface
                        .GetBestParkingCallback() { //todo: musisz tu jeszcze jakas lokalizacje
                    // dodać
                    @Override
                    public void onBestParkingFound(final ParkingOffer parkingOffer) {
                        Log.d(TAG, "onBestParkingFound: ");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: ");
                                choosenParking = parkingOffer;
                                showProposedParking(parkingOffer);
                                cancelProposalButton.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                });
            }
        });

        refreshParkingDataButton = findViewById(R.id.refreshParkingDataButton);
        refreshParkingDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverInterface.getParkings(new DriverInterface.GetParkingsInfoCallback() {
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

        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });

        searchEditText = findViewById(R.id.input_search);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute our method for searching
                    destinationAddress = searchForDestination();

                }

                return false;
            }
        });

        cancelProposalButton = findViewById(R.id.cancelProposalButton);
        cancelProposalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToButton.setEnabled(true);
                parkNowButton.setEnabled(true);
                driveButton.setVisibility(View.INVISIBLE);
                cancelProposalButton.setVisibility(View.INVISIBLE);
                uncheckBestParking();
            }
        });

    }

    /**
     * Mark out the chosen best parking. Used when user cancel the reservation.
     */
    private void uncheckBestParking() {
        final LatLng latLng = new LatLng(choosenParking.getLat(), choosenParking.getLon());
        removeMarker(latLng);
        addMarkerOfChosenParking(latLng, BitmapDescriptorFactory.HUE_RED);
    }

    /**
     * Searches for destination that the user typed in TextField.
     *
     * @return Address of the destination.
     */
    private Address searchForDestination() {
        Log.d(TAG, "searchForDestination: ");

        String searchString = searchEditText.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "searchForDestination: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            Log.d(TAG, "searchForDestination: found a location: " + address.toString());

            addDestinationMarker(new LatLng(address.getLatitude(), address.getLongitude()));
            return address;
        }
        return null;
    }

    /**
     * Adds destination marker.
     *
     * @param latLng Localization of the destination.
     */
    private void addDestinationMarker(LatLng latLng) {
        parkingMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(Constants.DESTINATION_TITLE)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination))
        ));
    }

    /**
     * Starts the settings activity.
     */
    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Binds agent to the DriverInterface
     * @see DriverInterface
     * @param agentName Name of the agent.
     */
    private void bindAgent(String agentName) {
        if (driverInterface == null) {
            try {
                driverInterface = MicroRuntime.getAgent(agentName)
                        .getO2AInterface(DriverInterface.class);
            } catch (StaleProxyException e) {
                Log.e(TAG, "onCreate: internal error");
            } catch (ControllerException e) {
                Log.e(TAG, "onCreate:  server error");
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");

        mMap = googleMap;

        locationPermissions.setMap(mMap);
        mapsPresenter.setLocationEnabled(locationPermissions);

        driverInterface.getParkings(new DriverInterface.GetParkingsInfoCallback() {
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

    /**
     * Shows aviable parings on map.
     * @param parkings Collection of the parkings to show.
     */
    private void showAvailableParkings(ArrayList<ParkingOffer> parkings) {
        Log.d(TAG, "showAvailableParkings: ");
        for (ParkingOffer p : parkings) {
            Log.d(TAG, "showAvailableParkings: Current parking data: " + p.getLat() + p.getLon());
            parkingMarkers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(
                    p.getLat(), p.getLon()))
                    .title(String.valueOf(p.getPrice() + "zł"))));
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

    /**
     * Initialize the map.
     */
    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    /**
     * Show proposed parking on map. It is marked as green marker.
     * @param chosenParking
     */
    private void showProposedParking(ParkingOffer chosenParking) {
        Log.d(TAG, "showProposedParking: ");
        final LatLng latLng = new LatLng(chosenParking.getLat(), chosenParking.getLon());
        removeMarker(latLng);
        addMarkerOfChosenParking(latLng, BitmapDescriptorFactory.HUE_GREEN);
        prepareButtonsForProposal();
    }

    /**
     * Sets visibility of the buttons when app show the proposal.
     */
    private void prepareButtonsForProposal() {
        parkNowButton.setEnabled(false);
        navigateToButton.setEnabled(false);
        driveButton.setVisibility(View.VISIBLE);
    }

    /**
     * Add marker of choosen parking on map.
     * @param latLng Localization of the chosen parking
     * @param color Color of the marker.
     */
    private void addMarkerOfChosenParking(LatLng latLng, float color) {
        parkingMarkers.add(mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(choosenParking.getPrice() + "zł"))
                .snippet("wybrany parking")
                .icon(BitmapDescriptorFactory.defaultMarker(color)
                )));
    }

    /**
     * Removes marker form the map.
     * @param latLng Localiztion of the parking to remove from map.
     */
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

    /**
     * Method called after location of the device has changed.
     * @param location Current location of the device.
     */
    public void onLocationChanged(Location location) {
        deviceLocation = new Localization(location.getLatitude(), location.getLongitude());
        driverInterface.setLocalization(new Localization(location.getLatitude(), location.getLongitude()));
    }
}