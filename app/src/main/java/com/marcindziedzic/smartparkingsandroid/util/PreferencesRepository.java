package com.marcindziedzic.smartparkingsandroid.util;

/**
 * Repository of users preferences
 */
public class PreferencesRepository {
    private static final PreferencesRepository ourInstance = new PreferencesRepository();
    private int distanceFactor;
    private int priceFactor;


    private PreferencesRepository() {
    }

    public static PreferencesRepository getInstance() {
        return ourInstance;
    }

    /**
     * Sets seek bar value. The distance factor is the same but the price factor is 100 -
     * seekBarValue
     *
     * @param seekBarValue Value of the seek bar.
     */
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
