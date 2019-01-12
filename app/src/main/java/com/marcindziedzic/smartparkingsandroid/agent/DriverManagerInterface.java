package com.marcindziedzic.smartparkingsandroid.agent;

import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import java.util.ArrayList;

public interface DriverManagerInterface {

    void getParkings(GetParkingsInfoCallback callback);

    void setLocalization(Localization localization);

    void sendReservationRequest();

    interface GetParkingsInfoCallback {
        void onParkingDataCollected(ArrayList<ParkingOffer> parkingData);
    }

    void getBestParkingNearby(GetBestParkingCallback callback); // todo delete

    void getBestParkingNearbyDestination(Localization localization, GetBestParkingCallback callback);

    interface GetBestParkingCallback {
        void onBestParkingFound(ParkingOffer parkingOffer);
    }
}
