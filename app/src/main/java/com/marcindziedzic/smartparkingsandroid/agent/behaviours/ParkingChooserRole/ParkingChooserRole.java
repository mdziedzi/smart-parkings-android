package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole.subbehaviours.ParkingChooser;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import jade.core.behaviours.ParallelBehaviour;

/**
 * Implementation of Gaia project role - ParkingChooser.
 * ParkingChooser is responsible for choosing the best parking.
 */
public class ParkingChooserRole extends ParallelBehaviour {

    private final DriverAgent driverMangerAgent;

    public ParkingChooserRole(DriverAgent a, int endCondition, Localization localization) {
        super(a, endCondition);
        driverMangerAgent = a;
        addSubBehaviour(new ParkingChooser(this, localization));
    }

    public DriverAgent getDriverManagerAgent() {
        return driverMangerAgent;
    }
}
