package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole.subbehaviours.ParkingChooser;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import jade.core.behaviours.ParallelBehaviour;

public class ParkingChooserRole extends ParallelBehaviour {

    private final DriverManagerAgent driverMangerAgent;

    public ParkingChooserRole(DriverManagerAgent a, int endCondition, Localization localization) {
        super(a, endCondition);
        driverMangerAgent = a;
        //updateDataStore();
        addSubBehaviour(new ParkingChooser(this, localization));

    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverMangerAgent;
    }
}
