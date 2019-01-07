package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ReservationistRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ReservationistRole.subbehaviours.Reservationist;

import jade.core.behaviours.ParallelBehaviour;

public class ReservationistRole extends ParallelBehaviour {

    private final DriverManagerAgent driverMangerAgent;

    public ReservationistRole(DriverManagerAgent a, int endCondition) {
        super(a, endCondition);
        driverMangerAgent = a;
        //updateDataStore();
        addSubBehaviour(new Reservationist(this));

    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverMangerAgent;
    }
}
