package com.marcindziedzic.smartparkingsandroid.agent;

import android.util.Log;

import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingChooserRole.ParkingChooserRole;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.ParkingDataCollectorRole.ParkingDataCollectorRole;
import com.marcindziedzic.smartparkingsandroid.agent.behaviours.TenantRole.TenantRole;
import com.marcindziedzic.smartparkingsandroid.loginFeature.LoginContract;
import com.marcindziedzic.smartparkingsandroid.ontology.ParkingOffer;
import com.marcindziedzic.smartparkingsandroid.ontology.SmartParkingsOntology;
import com.marcindziedzic.smartparkingsandroid.util.Localization;

import java.util.ArrayList;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_NAME_DRIVER;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_TYPE_DRIVER;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_TYPE_PARKING;

public class DriverManagerAgent extends Agent implements DriverManagerInterface {

    private static final String TAG = "DriverManagerAgent";

    private Localization localization;

    private ArrayList<AID> aviableParkings;

    private ACLMessage currentMessage;

    private int nResponders;

    private Codec codec = new SLCodec();
    private Ontology ontology = SmartParkingsOntology.getInstance();
    private ArrayList<ParkingOffer> parkings = new ArrayList<>();
    private ParkingOffer bestParking;
    private LoginContract.UserActionsListener loginPresenter;
    private GetParkingsInfoCallback getParkingsInfoCallback;
    private GetBestParkingCallback getBestParkingCallback;
    private AID bestParkingAgent;


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

        registerDriverAgentInDf();

        // Update list of parkings
        aviableParkings = getActualParkingAids();

        initAndroidAppEnvironment();

        addBehaviour(new ParkingDataCollectorRole(this, ParallelBehaviour.WHEN_ALL));


//
    }

    private void initAndroidAppEnvironment() {
        loginPresenter.onAgentStarted();
    }

    private void registerDriverAgentInDf() {
        // Register the parking service in the yellow pages
        DFAgentDescription myTemplate = new DFAgentDescription();
        myTemplate.setName(getAID());
        ServiceDescription myServiceDescription = new ServiceDescription();
        myServiceDescription.setType(SD_TYPE_DRIVER);
        myServiceDescription.setName(SD_NAME_DRIVER);
        myTemplate.addServices(myServiceDescription);
        try {
            DFService.register(this, myTemplate);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
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
    public void getParkings(GetParkingsInfoCallback callback) {
        getParkingsInfoCallback = callback;
        addBehaviour(new ParkingDataCollectorRole(this, ParallelBehaviour.WHEN_ALL));
//        callback.onParkingDataCollected(parkings);
    }

    @Override
    public void setLocalization(Localization localization) {
        this.localization = localization;
    }

    @Override
    public void sendReservationRequest() {
        Log.d(TAG, "sendReservationRequest: ");
        addBehaviour(new TenantRole(this, ParallelBehaviour.WHEN_ALL));
    }

    @Override
    public void getBestParkingNearby(GetBestParkingCallback callback) {
        Log.d(TAG, "getBestParkingNearbyDestination: ");
        getBestParkingCallback = callback;
        addBehaviour(new ParkingChooserRole(this, ParallelBehaviour.WHEN_ALL, this.localization));
    }

    @Override
    public void getBestParkingNearbyDestination(Localization localization, GetBestParkingCallback callback) {
        Log.d(TAG, "getBestParkingNearbyDestination: with localization");
        getBestParkingCallback = callback;
        addBehaviour(new ParkingChooserRole(this, ParallelBehaviour.WHEN_ALL, localization));
    }

    public ArrayList<AID> getActualParkingAids() {

        // Update list of parkings
        DFAgentDescription searchTemplate = new DFAgentDescription();
        ServiceDescription templateServiceDescription = new ServiceDescription();
        templateServiceDescription.setType(SD_TYPE_PARKING);
        searchTemplate.addServices(templateServiceDescription);

        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(this, searchTemplate);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        ArrayList<AID> parkingAidArray = new ArrayList<>();
        for (DFAgentDescription aResult : result) {
            parkingAidArray.add(aResult.getName());
        }
        return parkingAidArray;
    }

    /**
     * Enables to perform updating parking list.
     *
     * @param parkingData Data about available parkings.
     */
    public void updateParkingList(ArrayList<ParkingOffer> parkingData) {
        Log.d(TAG, "updateParkingList: ");
        parkings = parkingData;
        if (getParkingsInfoCallback != null) {
            getParkingsInfoCallback.onParkingDataCollected(parkings);
        }

    }

    public void onParkingChoosen(ParkingOffer bestParking, AID sender) {
        // todo wrzucic wszystkie metody które wywołuą role do interfejsu
        Log.d(TAG, "onParkingChoosen: ");
        this.bestParking = bestParking;
        this.bestParkingAgent = sender;
        getBestParkingCallback.onBestParkingFound(bestParking);
    }

    public AID getBestParkingAgent() {
        return bestParkingAgent;
    }
}

