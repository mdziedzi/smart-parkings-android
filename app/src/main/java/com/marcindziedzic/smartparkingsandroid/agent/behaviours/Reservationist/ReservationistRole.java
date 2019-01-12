package com.marcindziedzic.smartparkingsandroid.agent.behaviours.Reservationist;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.Reservationist.subbehaviours.Reservationist;

import jade.core.behaviours.ParallelBehaviour;

public class ReservationistRole extends ParallelBehaviour {

    private final DriverManagerAgent driverManagerAgent;

    public ReservationistRole(DriverManagerAgent a, int endCondition) {
        super(a, endCondition);
        this.driverManagerAgent = a;

        addSubBehaviour(new Reservationist(this));
    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverManagerAgent;
    }


}
