package com.marcindziedzic.smartparkingsandroid.loginFeature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.marcindziedzic.smartparkingsandroid.util.GPSStatus;

import static android.content.ContentValues.TAG;

/**
 * Util to check Privileges for the GPS
 */
public class LocationServicesUtil implements LocationServices {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private Context context;

    LocationServicesUtil(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public GPSStatus checkAllPrivileges() {

        Log.d(TAG, "checkAllPrivileges: checking google services version");
        int aviable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (aviable == ConnectionResult.SUCCESS) {
            Log.d(TAG, "checkAllPrivileges: Google Play Services is working");
            return GPSStatus.OK;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(aviable)) {
            Log.d(TAG, "checkAllPrivileges: error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, aviable, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(context, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return GPSStatus.FAILURE;


    }
}
