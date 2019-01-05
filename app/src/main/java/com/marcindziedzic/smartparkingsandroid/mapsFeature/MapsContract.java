package com.marcindziedzic.smartparkingsandroid.mapsFeature;

import com.marcindziedzic.smartparkingsandroid.util.LocationPermissions;

public interface MapsContract {

    interface View {
    }

    interface Presenter {
        void resolveLocationPermissions(LocationPermissions locationPermissions);

        void setLocationEnabled(LocationPermissions locationPermissions);
    }

}
