package com.marcindziedzic.smartparkingsandroid.login;

import com.marcindziedzic.smartparkingsandroid.agent.util.GPSStatus;

/**
 * Inteface created for resolving Android Localization Services.
 * It helps staying MVP pattern clean from Android API.
 */
public interface LocationServices {

    /**
     * Chcecks if Google Play Services are enabled
     *
     * @return Status of privileges
     */
    GPSStatus checkAllPrivileges();

}
