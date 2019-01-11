package com.marcindziedzic.smartparkingsandroid.util;

public class PreferencesRepository {
    private static final PreferencesRepository ourInstance = new PreferencesRepository();
    private int distanceFactor;
    private int priceFactor;


    private PreferencesRepository() {
    }

    public static PreferencesRepository getInstance() {
        return ourInstance;
    }

    public void setSeekBarValue(int seekBarValue) {
        distanceFactor = seekBarValue;
        priceFactor = 100 - seekBarValue;
    }

    public int getDistanceFactor() {
        return distanceFactor;
    }

    public int getPriceFactor() {
        return priceFactor;
    }
}
