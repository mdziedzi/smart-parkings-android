package com.marcindziedzic.smartparkingsandroid.util;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

public interface LocationPermissions { //todo rename
    boolean getLocationPermission();

    void setLocationEnabled();

    void setMap(GoogleMap mMap);

    void recenter();

    Location getCurrentLocation();
}
