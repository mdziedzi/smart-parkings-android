package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours.CollectParkingData;

import jade.core.behaviours.ParallelBehaviour;

public class ParkingDataCollectorRole extends ParallelBehaviour {

    private final DriverManagerAgent driverManagerAgent;

    public ParkingDataCollectorRole(DriverManagerAgent a, int endCondition) {
        super(a, endCondition);
        driverManagerAgent = a;
//        this.addSubBehaviour(new SendParkingDataRequest(this));
        addSubBehaviour(new CollectParkingData(this));
    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverManagerAgent;
    }
}
