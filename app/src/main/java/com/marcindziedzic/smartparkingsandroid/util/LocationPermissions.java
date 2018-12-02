package com.marcindziedzic.smartparkingsandroid.util;

import com.google.android.gms.maps.GoogleMap;

public interface LocationPermissions {
    boolean getLocationPermission();

    void setLocationEnabled();

    void setMap(GoogleMap mMap);

    void recenter();
}
