package com.marcindziedzic.smartparkingsandroid.agent;

import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import java.util.ArrayList;

public interface DriverManagerInterface {

    void getParkings(GetParkingsInfoCallback callback);

    interface GetParkingsInfoCallback {
        void onParkingDataCollected(ArrayList<ParkingOffer> parkingData);
    }

    void getBestParking(GetBestParkingCallback callback); // todo delete

    void getBestParking(Localization localization, GetBestParkingCallback callback);

    interface GetBestParkingCallback {
        void onBestParkingFound(ParkingOffer parkingOffer);
    }
}
