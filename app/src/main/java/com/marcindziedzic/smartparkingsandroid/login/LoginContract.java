package com.marcindziedzic.smartparkingsandroid.login;

/**
 * Login feature contract interface to establish interfaces between view and presenter in MVC pattern.
 */
public interface LoginContract {

    /**
     * Defines login feature view requirements.
     */
    interface View {

        /**
         * Enable login views after checking app permissions returned positive state.
         */
        void enableLoginViews();

        /**
         * Show error bound to GPS services.
         */
        void showGPSError();

        /**
         * Starts MapsActivity.
         */
        void startMapsActivity();
    }

    /**
     * Defines login feature presenter requirements.
     */
    interface UserActionsListener {

        /**
         * Resolves if user has configured Google Play Services
         * @param locationServices Interface to comunicate Localization Android API with presenter
         */
        void resolveGooglePlayRequirements(LocationServices locationServices);

        /**
         * Creates container and agent in AMS.
         *
         * @param serviceBinder Util for creating container and agent.
         * @param agentName     Name of the new agent.
         */
        void connectToService(ServiceBinder serviceBinder, String agentName);

        /**
         * Method called after agent chooses parking.
         */
        void onParkingChosen();
    }
}
