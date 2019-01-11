package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ReservationistRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ReservationistRole.subbehaviours.Reservationist;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import jade.core.behaviours.ParallelBehaviour;

public class ReservationistRole extends ParallelBehaviour {

    private final DriverManagerAgent driverMangerAgent;

    public ReservationistRole(DriverManagerAgent a, int endCondition, Localization localization) {
        super(a, endCondition);
        driverMangerAgent = a;
        //updateDataStore();
        addSubBehaviour(new Reservationist(this, localization));

    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverMangerAgent;
    }
}
