package com.marcindziedzic.smartparkingsandroid.login;

import com.marcindziedzic.smartparkingsandroid.util.GPSStatus;

/**
 * Interface created for resolving Android Localization Services.
 * It helps staying MVP pattern clean from Android API.
 */
public interface LocationServices {

    /**
     * Checks if Google Play Services are enabled.
     * @return Status of privileges
     */
    GPSStatus checkAllPrivileges();

}
