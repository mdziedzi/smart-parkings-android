package com.marcindziedzic.smartparkingsandroid.loginFeature;

public class LoginPresenter implements LoginContract.UserActionsListener {

    private LoginContract.View mLoginView;


    LoginPresenter(LoginContract.View loginView) {
        mLoginView = loginView;

    }

    @Override
    public void resolveGooglePlayRequirements(LocationServices locationServices) {

        switch (locationServices.checkAllPrivileges()) {
            case OK:
                mLoginView.enableLoginViews();
                break;
            case FAILURE:
                mLoginView.showGPSError();
                break;
        }
    }

    @Override
    public void connectToService(ServiceBinder serviceBinder, String agentName) {
        serviceBinder.connectToSmartParkingSystem(this, agentName);


    }

    @Override
    public void onParkingChosen() { //todo delete
        mLoginView.startMapsActivity();
    }

    @Override
    public void onAgentStarted() {
        mLoginView.startMapsActivity();
    }


}
