package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours.CollectParkingData;

import jade.core.behaviours.ParallelBehaviour;

public class ParkingDataCollectorRole extends ParallelBehaviour {

    private final DriverAgent driverAgent;

    public ParkingDataCollectorRole(DriverAgent a, int endCondition) {
        super(a, endCondition);
        driverAgent = a;
//        this.addSubBehaviour(new SendParkingDataRequest(this));
        addSubBehaviour(new CollectParkingData(this));
    }

    public DriverAgent getDriverAgent() {
        return driverAgent;
    }
}
