package com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole.subbehaviours;

import android.util.Log;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole.TenantRole;
import com.marcindziedzic.smartparkingsandroid.ontology.ReservationRequest;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;
import com.marcindziedzic.smartparkingsandroid.util.Constants;

import java.util.Date;
import java.util.Vector;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Subbehaviour of Tenant.
 * Sends request for booking.
 * Communication i based on Request Protocol.
 */
public class Tenant extends OneShotBehaviour {

    private static final String TAG = Tenant.class.getSimpleName();

    private final TenantRole parentBehaviour;

    public Tenant(TenantRole tenantRole) {
        parentBehaviour = tenantRole;

    }

    @Override
    public void action() {
        Log.d(TAG, "action: ");

        // Fill the REQUEST message
        final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        AID debug = parentBehaviour.getDriverAgent().getBestParkingAgent();
        msg.addReceiver(parentBehaviour.getDriverAgent().getBestParkingAgent());
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + Constants
                .REQUEST_INITIATOR_TIMEOUT));

        prepareMsg(msg);

        parentBehaviour.addSubBehaviour(new AchieveREInitiator(getAgent(), msg) {

            @Override
            protected void handleAgree(ACLMessage agree) {
                Log.d(TAG, "handleAgree: zarezerwowano");


            }

            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent " + refuse.getSender().getName() + " refused to perform the requested action");
            }

            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    // FAILURE notification from the JADE runtime: the receiver
                    // does not exist
                    System.out.println("Responder does not exist");
                } else {
                    System.out.println("Agent " + failure.getSender().getName() + " failed to perform the requested action");
                }
            }

            protected void handleAllResultNotifications(Vector notifications) {
                Log.d(TAG, "handleAllResultNotifications: ");
                //todo

            }
        });

    }

    /**
     * Fill the message with the appropriate content. In this case it is ReservationRequest
     * object.
     *
     * @param msg Message to fill
     * @see ReservationRequest
     */
    private void prepareMsg(ACLMessage msg) {
        msg.setLanguage(new SLCodec().getName());
        msg.setOntology(SmartParkingsOntology.getInstance().getName());

        ReservationRequest reservationRequest = new ReservationRequest();

        try {
            getAgent().getContentManager().fillContent(msg, reservationRequest);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }
}
