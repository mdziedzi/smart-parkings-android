package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours.CollectParkingData;

import jade.core.behaviours.ParallelBehaviour;

/**
 * Implementation of Gaia project role - ParkingDataCollector.
 * ParkingDataCollector is responsible for collecting info about nearby parking places.
 */
public class ParkingDataCollectorRole extends ParallelBehaviour {

    private final DriverAgent driverAgent;

    public ParkingDataCollectorRole(DriverAgent a, int endCondition) {
        super(a, endCondition);
        driverAgent = a;
        addSubBehaviour(new CollectParkingData(this));
    }

    public DriverAgent getDriverAgent() {
        return driverAgent;
    }
}
