package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.ParkingDataCollectorRole;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendParkingDataRequest extends OneShotBehaviour {

    private final ParkingDataCollectorRole parentBehaviour;

    public SendParkingDataRequest(ParkingDataCollectorRole parkingDataCollectorRole) {
        parentBehaviour = parkingDataCollectorRole;
    }

    @Override
    public void action() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        for (AID receiver : parentBehaviour.getDriverManagerAgent().getActualParkingAids()) {
            msg.addReceiver(receiver);
            prepareMsg(msg);
            getAgent().send(msg);
        }
    }

    private void prepareMsg(ACLMessage msg) {
        msg.setLanguage(new SLCodec().getName());
        msg.setOntology(SmartParkingsOntology.getInstance().getName());

        ParkingOffer parkingOffer = new ParkingOffer();

        try {
            getAgent().getContentManager().fillContent(msg, parkingOffer);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }
}
