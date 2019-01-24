package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours;

import android.util.Log;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.ParkingDataCollectorRole;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;
import com.marcindziedzic.smartparkingsandroid.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

/**
 * Subbehaviour of ParkingDataCollector.
 * Sends request for parking info to every parking.
 * When the data is collected it is showed on the map.
 * Communication i based on Request Protocol.
 */
public class CollectParkingData extends OneShotBehaviour {
    private static final String TAG = OneShotBehaviour.class.getSimpleName();
    private final ParkingDataCollectorRole parentBehaviour;
    private int nResponders = 0;
    private ArrayList<ParkingOffer> parkingData = new ArrayList<>();

    public CollectParkingData(ParkingDataCollectorRole parkingDataCollectorRole) {
        parentBehaviour = parkingDataCollectorRole;
    }

    @Override
    public void action() {

        final long startTime = System.nanoTime();

        // Fill the REQUEST message
        final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        ArrayList<AID> actualParkings = parentBehaviour.getDriverAgent().getActualParkingAids();
        Log.d(TAG, "action: gettingActualParkings " + actualParkings.size());
        for (AID receiver : actualParkings) {
            msg.addReceiver(receiver);
        }

        long interval = System.nanoTime() - startTime;
        Log.d(TAG, "getting parking aids interval: " + interval / 1000000000);

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        // We want to receive a reply in 1 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + Constants
                .REQUEST_INITIATOR_TIMEOUT));

        prepareMsg(msg);

        final long startTime2 = System.nanoTime();

        parentBehaviour.addSubBehaviour(new AchieveREInitiator(getAgent(), msg) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
                ContentElement content = null;
                try {
                    content = CollectParkingData.this.getAgent().getContentManager()
                            .extractContent(inform);
                } catch (Codec.CodecException | OntologyException e1) {
                    e1.printStackTrace();
                }
                if (content instanceof ParkingOffer) {
                    parkingData.add((ParkingOffer) content);
                    Log.d(TAG, "handleInform: " + ((ParkingOffer) content).getLat() + ((ParkingOffer) content).getLon());
                    System.out.println(parkingData.iterator().next().toString());//todo delete
                } else {
                    System.out.println("It's not instance of ParkingOffer");
                }
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent " + refuse.getSender().getName() + " refused to perform the requested action");
                nResponders--;
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
                if (notifications.size() < nResponders) {
                    // Some responder didn't reply within the specified timeout
                    System.out.println("Timeout expired: missing " + (nResponders - notifications.size()) + " responses");
                }
                double interval = System.nanoTime() - startTime2;
                Log.d(TAG, "CollectParkingData interval is: " + interval / 1000000000);
                parentBehaviour.getDriverAgent().updateParkingList(parkingData);
            }
        });
    }

    /**
     * Prepares message for sending. Thanks to that the message could be received by the
     * appropriate role.
     *
     * @param msg Message to fill.
     */
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
