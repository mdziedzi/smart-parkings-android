package com.marcindziedzic.smartparkingsandroid.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.marcindziedzic.smartparkingsandroid.mapsFeature.MapsActivity;

import static android.content.ContentValues.TAG;

public class LocationPermissionsUtil extends Activity implements LocationPermissions { //todo rename

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1111;
    private static final float DEFAULT_ZOOM = 14;
    private final MapsActivity context;
    private boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            mLocation = locationResult.getLastLocation();
            animateCamera(mLocation);
            context.onLocationChanged(mLocation);
        }

    };

    public LocationPermissionsUtil(MapsActivity context) {
        this.context = context;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void animateCamera(Location lastLocation) {
        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mMap.animateCamera(cameraUpdate);
    }

    public boolean getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        return mLocationPermissionsGranted;
    }

    @Override
    public void setLocationEnabled() {
        getDeviceLocation();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }


    @Override
    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;

    }

    @Override
    public void recenter() {
        animateCamera(mLocation);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
                                Toast.makeText(context, "Error during fetching location", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "onComplete: unable to get current loction");
                            Toast.makeText(context, "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SECURITY EXCEPTION " + e.getMessage());
        }
    }

    public void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to lat: " + latLng.latitude + ", lng " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public Location getCurrentLocation() {
        return mLocation;
    }
}
