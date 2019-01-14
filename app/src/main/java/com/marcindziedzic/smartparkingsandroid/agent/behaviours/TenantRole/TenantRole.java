package com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole.subbehaviours.Tenant;

import jade.core.behaviours.ParallelBehaviour;

public class TenantRole extends ParallelBehaviour {

    private final DriverManagerAgent driverManagerAgent;

    public TenantRole(DriverManagerAgent a, int endCondition) {
        super(a, endCondition);
        this.driverManagerAgent = a;

        addSubBehaviour(new Tenant(this));
    }

    public DriverManagerAgent getDriverManagerAgent() {
        return driverManagerAgent;
    }


}
