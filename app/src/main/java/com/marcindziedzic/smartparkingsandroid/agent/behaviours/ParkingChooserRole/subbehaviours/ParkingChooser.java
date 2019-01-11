package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole.subbehaviours;

import android.util.Log;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole.ParkingChooserRole;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.util.Localization;
import com.marcindziedzic.smartparkingsandroid.util.PreferencesRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import static com.marcindziedzic.smartparkingsandroid.util.Constants.DISTANCE_NORMALIZATION_FACTOR;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.PRICE_NORMALIZATION_FACTOR;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.TIMEOUT_WAITING_FOR_PARKING_REPLY;

public class ParkingChooser extends OneShotBehaviour {

    private static final String TAG = ParkingChooser.class.getSimpleName();

    private final ParkingChooserRole parentRole;
    private final Localization localization;
    private int nResponders;
    private ArrayList<ParkingOffer> parkings = new ArrayList<>();
    private ParkingOffer bestParking;

    public ParkingChooser(ParkingChooserRole parkingChooserRole, Localization localization) {
        parentRole = parkingChooserRole;
        this.localization = localization;
    }

    @Override
    public void action() {
        System.out.println("Driver-agent: " + getAgent().getAID().getName() + "have found this " +
                "parkings:");

        final ACLMessage currentMessage = new ACLMessage(ACLMessage.CFP);
        ArrayList<AID> aviableParkings = parentRole.getDriverManagerAgent().getActualParkingAids();
        for (AID receiver : aviableParkings) {
            currentMessage.addReceiver(receiver);
        }
        Log.d(TAG, "action: aviableParkings = " + aviableParkings.size());

        nResponders = aviableParkings.size();

        currentMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // give 2s for reply
        currentMessage.setReplyByDate(new Date(System.currentTimeMillis() + TIMEOUT_WAITING_FOR_PARKING_REPLY));
        currentMessage.setContent("Give my info about you, please.");

        parentRole.addSubBehaviour(new ContractNetInitiator(getAgent(), currentMessage) {

            protected void handlePropose(ACLMessage propose, Vector v) {
                System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
            }

            protected void handleRefuse(ACLMessage refuse) {
                System.out.println("Agent " + refuse.getSender().getName() + " refused");
            }

            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    // FAILURE notification from the JADE runtime: the receiver
                    // does not exist
                    System.out.println("Responder does not exist");
                } else {
                    System.out.println("Agent " + failure.getSender().getName() + " failed");
                }
                // Immediate failure --> we will not receive a response from this agent
                nResponders--;
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                if (responses.size() < nResponders) {
                    // Some responder didn't reply within the specified timeout
                    System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
                }
                // Evaluate proposals.
                ParkingOffer bestProposal = null;
                AID bestProposer = null;
                ACLMessage accept = null;

                Enumeration e = responses.elements();

                while (e.hasMoreElements()) {

                    ACLMessage msg = (ACLMessage) e.nextElement();

                    if (msg.getPerformative() == ACLMessage.PROPOSE) {

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        acceptances.addElement(reply);

                        ContentElement content = null;
                        try {
                            content = getAgent().getContentManager().extractContent(msg);
                        } catch (Codec.CodecException | OntologyException e1) {
                            e1.printStackTrace();
                        }

                        if (content instanceof ParkingOffer) {
                            ParkingOffer currProposal = (ParkingOffer) content;
                            parkings.add(currProposal);
                            if (bestProposal == null) {
                                bestProposal = currProposal;
                            }

                            if (isBetter(currProposal, bestProposal)) {
                                bestProposal = currProposal;
                                bestProposer = msg.getSender();
                                accept = reply;
                            }

                        } else {
                            System.out.println("err");
                        }
                    }
                }
                // Accept the proposal of the best proposer
                if (accept != null) {
                    System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
                    bestParking = bestProposal;
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    if (bestParking != null) {
                        sendParkingHasBeenChosen(bestParking);
                    }
                }
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
            }
        });
    }

    private void sendParkingHasBeenChosen(ParkingOffer bestParking) {
        Log.d(TAG, "sendParkingHasBeenChosen: ");
        parentRole.getDriverManagerAgent().onParkingChoosen(bestParking);
        // todo
    }

    private boolean isBetter(ParkingOffer currProposal, ParkingOffer bestProposal) {
        Log.v(TAG, "isBetter: ");
        float proposalPrice = currProposal.getPrice();
        float proposalLat = currProposal.getLat();
        float proposalLon = currProposal.getLon();

        float chosenPrice = bestProposal.getPrice();
        float chosenLat = bestProposal.getLat();
        float chosenLon = bestProposal.getLon();

        double proposalDist = Math.sqrt(Math.pow(localization
                .getLatitude() - proposalLat, 2) + Math.pow(localization.getLongitude() -
                proposalLon, 2));
        double chosenDist = Math.sqrt(Math.pow(localization.getLatitude()
                - chosenLat, 2) + Math
                .pow(localization.getLongitude() - chosenLon, 2));

        int priceFactor = PreferencesRepository.getInstance().getPriceFactor();
        int distanceFactor = PreferencesRepository.getInstance().getDistanceFactor();

        double currProposalScore = priceFactor * PRICE_NORMALIZATION_FACTOR * proposalPrice +
                distanceFactor * DISTANCE_NORMALIZATION_FACTOR * proposalDist;
        double bestProposalScore = priceFactor * PRICE_NORMALIZATION_FACTOR * chosenPrice +
                distanceFactor * DISTANCE_NORMALIZATION_FACTOR * chosenDist;

        // the less is better
        return currProposalScore <= bestProposalScore;
    }


}
