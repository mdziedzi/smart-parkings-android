package com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole.subbehaviours.Tenant;

import jade.core.behaviours.ParallelBehaviour;

public class TenantRole extends ParallelBehaviour {

    private final DriverAgent driverAgent;

    public TenantRole(DriverAgent a, int endCondition) {
        super(a, endCondition);
        this.driverAgent = a;

        addSubBehaviour(new Tenant(this));
    }

    public DriverAgent getDriverAgent() {
        return driverAgent;
    }


}
