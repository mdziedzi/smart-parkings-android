package com.marcindziedzic.smartparkingsandroid.util;

import android.location.Location;

import com.google.android.gms.maps.GoogleMap;

/**
 * Interface used for localization purposes.
 */
public interface LocationPermissions {

    /**
     * Resolves location permissions
     *
     * @return True when the permissions has been granted.
     */
    boolean getLocationPermission();

    /**
     * Sets the location to be enabled during using the app.
     */
    void setLocationEnabled();

    /**
     * Sets the map.
     */
    void setMap(GoogleMap mMap);

    /**
     * Recenter the map to current location.
     */
    void recenter();

    /**
     * Gets the current location.
     *
     * @return Current location.
     */
    Location getCurrentLocation();
}
