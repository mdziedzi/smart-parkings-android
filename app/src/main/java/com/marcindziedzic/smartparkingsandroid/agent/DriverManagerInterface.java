package com.marcindziedzic.smartparkingsandroid.agent;

import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;

import java.util.ArrayList;

public interface DriverManagerInterface {

    void getParkings(GetParkingsInfoCallback callback);

    interface GetParkingsInfoCallback {
        void onParkingDataCollected(ArrayList<ParkingOffer> parkingData);
    }

    void getBestParking(GetBestParkingCallback callback);

    interface GetBestParkingCallback {
        void onBestParkingFound(ParkingOffer parkingOffer);
    }
}
