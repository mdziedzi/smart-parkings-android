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

        void startMapsActivity();
    }

    /**
     * Defines login feature presenter requirements.
     */
    interface UserActionsListener {

        void logIn();

        void signUp();

        /**
         * Resolves if user has configured Google Play Services
         *
         * @param locationServices Interface to comunicate Localization Android API with presenter
         */
        void resolveGPSRequirements(LocationServices locationServices);

        void connectToService(ServiceBinder serviceBinder, String agentName);

        void onParkingChoosen();
    }
}
