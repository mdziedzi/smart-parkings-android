package com.marcindziedzic.smartparkingsandroid.login;

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
    public void onParkingChosen() {
        mLoginView.startMapsActivity();
    }


}
