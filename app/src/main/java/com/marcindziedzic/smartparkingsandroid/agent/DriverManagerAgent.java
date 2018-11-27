package com.marcindziedzic.smartparkingsandroid.agent;

import android.content.Intent;
import android.util.Log;

import com.marcindziedzic.smartparkingsandroid.agent.util.Localization;
import com.marcindziedzic.smartparkingsandroid.login.LoginContract;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import static com.marcindziedzic.smartparkingsandroid.agent.util.Constants.DISTANCE_FACTOR;
import static com.marcindziedzic.smartparkingsandroid.agent.util.Constants.PRICE_FACTOR;
import static com.marcindziedzic.smartparkingsandroid.agent.util.Constants.TIMEOUT_WAITING_FOR_PARKING_REPLY;

public class DriverManagerAgent extends Agent implements DriverManagerInterface {

    private static final String TAG = "DriverManagerAgent";

    private Localization localization;

    private AID[] aviableParkings;

    private ACLMessage currentMessage;

    private int nResponders;

    private Codec codec = new SLCodec();
    private Ontology ontology = SmartParkingsOntology.getInstance();
    private ArrayList<ParkingOffer> parkings = new ArrayList<>();
    private ParkingOffer bestParking;
    private LoginContract.UserActionsListener loginPresenter;


    @Override
    protected void setup() {

        // Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        Object[] args = getArguments();
        if (args != null) {
            if(args.length > 0) {
                if (args[0] instanceof LoginContract.UserActionsListener) {
                    loginPresenter = (LoginContract.UserActionsListener) args[0];
                }
                if (args.length > 1) {
                    this.localization = (Localization) args[1];
                    System.out.println("Created DriverManagerAgent " + getAID().getName() + " with lat: " + this.localization.getLatitude()
                            + " lon: " + this.localization.getLongitude());
                }
            }
        }

        registerO2AInterface(DriverManagerInterface.class, this);

        // Register the parking service in the yellow pages
        DFAgentDescription myTemplate = new DFAgentDescription();
        myTemplate.setName(getAID());
        ServiceDescription myServiceDescription = new ServiceDescription();
        myServiceDescription.setType("driver-info");
        myServiceDescription.setName("JADE-driver-info");
        myTemplate.addServices(myServiceDescription);
        try {
            DFService.register(this, myTemplate);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Update list of parkings
        DFAgentDescription searchTemplate = new DFAgentDescription();
        ServiceDescription templateServiceDescription = new ServiceDescription();
        templateServiceDescription.setType("parking-info");
        searchTemplate.addServices(templateServiceDescription);

        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(this, searchTemplate);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        aviableParkings = new AID[result.length];
        for (int i = 0; i < result.length; ++i) {
            aviableParkings[i] = result[i].getName();
        }

        System.out.println("Driver-agent: " + getAID().getName() + "have found this parkings:");
        for (AID aid : aviableParkings) {
            System.out.println(aid.getName());
        }
        System.out.println();
        nResponders = aviableParkings.length;

        currentMessage = new ACLMessage(ACLMessage.CFP);
        for (AID aid : aviableParkings) {
            currentMessage.addReceiver(aid);
        }

        currentMessage.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // give 2s for reply
        currentMessage.setReplyByDate(new Date(System.currentTimeMillis() + TIMEOUT_WAITING_FOR_PARKING_REPLY));
        currentMessage.setContent("Give my info about you, please.");

        addBehaviour(new ContractNetInitiator(this, currentMessage) {

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

            @SuppressWarnings("unchecked")
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
                            content = getContentManager().extractContent(msg);
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
                }
                sendParkingDataReadyBroadcast();
            }

            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
            }
        });
    }

    private void sendParkingDataReadyBroadcast() {
        Intent broadcast = new Intent();
        broadcast.setAction("PARKING_DATA_READY");
        Log.d(TAG, "setup: Sending broadcast" + broadcast.getAction());
        loginPresenter.onParkingChoosen();
    }

    // todo: replace this dummy decision algorithm
    private boolean isBetter(ParkingOffer currProposal, ParkingOffer bestProposal) {
        float proposalPrice = currProposal.getPrice();
        float proposalLat = currProposal.getLat();
        float proposalLon = currProposal.getLon();

        float chosenPrice = bestProposal.getPrice();
        float chosenLat = bestProposal.getLat();
        float chosenLon = bestProposal.getLon();

        double proposalDist = Math.sqrt(Math.pow(localization.getLatitude() - proposalLat, 2) + Math.pow(localization.getLongitude() - proposalLon, 2));
        double chosenDist = Math.sqrt(Math.pow(localization.getLatitude() - chosenLat, 2) + Math.pow(localization.getLongitude() - chosenLon, 2));

        // todo: find the right
        double currProposalScore = PRICE_FACTOR * proposalPrice + DISTANCE_FACTOR * proposalDist;
        double bestProposalScore = PRICE_FACTOR * chosenPrice + DISTANCE_FACTOR * chosenDist;

        // the less is better
        return currProposalScore <= bestProposalScore;
    }

    public Localization getLocalization() {
        return localization;
    }

    @Override
    protected void takeDown() {

        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Driver-agent " + getAID().getName() + " terminating.");
    }
    
    public ArrayList<ParkingOffer> getParkings() {
        return parkings;
    }

    @Override
    public ParkingOffer getBestParking() {
        return bestParking;
    }
}

