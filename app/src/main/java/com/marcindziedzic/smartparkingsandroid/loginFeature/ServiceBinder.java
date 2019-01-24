package com.marcindziedzic.smartparkingsandroid.loginFeature;

/**
 * Interface used to enable connection to the specific agent.
 */
public interface ServiceBinder {

    /**
     * Enables object to agent binding.
     *
     * @param loginPresenter Presenter of login feature.
     * @param agentName      Name of the agent.
     */
    void connectToSmartParkingSystem(LoginPresenter loginPresenter, String agentName);
}
