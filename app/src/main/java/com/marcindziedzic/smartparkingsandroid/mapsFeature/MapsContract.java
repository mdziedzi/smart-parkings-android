package com.marcindziedzic.smartparkingsandroid.mapsFeature;

import com.marcindziedzic.smartparkingsandroid.util.LocationPermissions;

/**
 * Contract between the View and Presenter in Maps Feature.
 */
public interface MapsContract {

    /**
     * Interface of the View
     */
    interface View {
    }

    /**
     * Interface of the Presenter
     */
    interface Presenter {
        /**
         * Resolves location permissions in the app.
         *
         * @param locationPermissions Location permissions object.
         */
        void resolveLocationPermissions(LocationPermissions locationPermissions);

        /**
         * Sets GPS location enabled.
         *
         * @param locationPermissions Location permissions object.
         */
        void setLocationEnabled(LocationPermissions locationPermissions);
    }

}
