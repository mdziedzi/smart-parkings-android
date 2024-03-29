package com.marcindziedzic.smartparkingsandroid.mapsFeature;

import com.marcindziedzic.smartparkingsandroid.util.LocationPermissions;

/**
 * Presenter of the maps feature.
 */
public class MapsPresenter implements MapsContract.Presenter {
    private final MapsContract.View mapsView;

    MapsPresenter(MapsContract.View view) {
        mapsView = view;
    }

    @Override
    public void resolveLocationPermissions(LocationPermissions locationPermissions) {
        if (locationPermissions.getLocationPermission()) {
            //succ
        } else {
            //fail
        }

    }

    @Override
    public void setLocationEnabled(LocationPermissions locationPermissions) {
        locationPermissions.setLocationEnabled();

    }
}
