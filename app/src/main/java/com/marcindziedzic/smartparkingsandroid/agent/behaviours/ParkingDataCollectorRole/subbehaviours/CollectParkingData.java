package com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.subbehaviours;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.ParkingDataCollectorRole;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;

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

public class CollectParkingData extends OneShotBehaviour {
    private final ParkingDataCollectorRole parentBehaviour;
    private int nResponders = 0;
    private ArrayList<ParkingOffer> parkingData = new ArrayList<>();

    public CollectParkingData(ParkingDataCollectorRole parkingDataCollectorRole) {
        parentBehaviour = parkingDataCollectorRole;
    }

    @Override
    public void action() {
//        // TODO w prakinga nie ma inform
//        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
//                new MessageTemplate(new MessageTemplate.MatchExpression() {
//                    @Override
//                    public boolean match(ACLMessage matchMsg) {
//                        ContentElement content = null;
//                        try {
//                            content = CollectParkingData.this.getAgent().getContentManager().extractContent(matchMsg);
//                        } catch (Codec.CodecException | OntologyException e1) {
//                            e1.printStackTrace();
//                        }
//                        return content instanceof ParkingOffer;
//                    }
//                }));
//        ACLMessage msg = myAgent.receive(mt);
//        if (msg != null) {
//            System.out.println(msg);
//
////            parentBehaviour.getDriverManagerAgent().updateParkingList(msg.get);
//
//                                    ContentElement content = null;
//                        try {
//                            content = getAgent().getContentManager()
//                                    .extractContent(msg);
//                        } catch (Codec.CodecException | OntologyException e1) {
//                            e1.printStackTrace();
//                        }
//
//                        if (content instanceof ParkingOffer) {
//                            ParkingOffer currProposal = (ParkingOffer) content;
//                            parentBehaviour.getDriverManagerAgent().updateParkingList(currProposal);
//
//
//
//                        } else {
//                            System.out.println("err");
//                        }
//
//        } else {
//            block();
//        }


//https://github.com/mihaimaruseac/JADE-ARIA/blob/master/src/examples/protocols/FIPARequestInitiatorAgent.java

        // Fill the REQUEST message
        final ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        for (AID receiver : parentBehaviour.getDriverManagerAgent().getActualParkingAids()) {
            msg.addReceiver(receiver);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        // We want to receive a reply in 1 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000)); //todo add to constraints
//        msg.setContent("dummy-action");
        prepareMsg(msg);

        parentBehaviour.addSubBehaviour(new AchieveREInitiator(getAgent(), msg) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
                ContentElement content = null;
                try {
                    content = CollectParkingData.this.getAgent().getContentManager()
                            .extractContent(msg);
                } catch (Codec.CodecException | OntologyException e1) {
                    e1.printStackTrace();
                }
                if (content instanceof ParkingOffer) {
                    parkingData.add((ParkingOffer) content);
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
                parentBehaviour.getDriverManagerAgent().updateParkingList(parkingData);
            }
        });
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