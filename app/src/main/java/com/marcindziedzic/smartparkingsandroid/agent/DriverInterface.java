package com.marcindziedzic.smartparkingsandroid.agent;

import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import java.util.ArrayList;

public interface DriverInterface {

    void getParkings(GetParkingsInfoCallback callback);

    void setLocalization(Localization localization);

    void sendReservationRequest();

    void getBestParkingNearby(GetBestParkingCallback callback);

    void getBestParkingNearbyDestination(Localization localization, GetBestParkingCallback callback);

    interface GetBestParkingCallback {
        void onBestParkingFound(ParkingOffer parkingOffer);
    }

    interface GetParkingsInfoCallback {
        void onParkingDataCollected(ArrayList<ParkingOffer> parkingData);
    }
}
