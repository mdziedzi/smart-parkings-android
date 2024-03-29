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
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import static com.marcindziedzic.smartparkingsandroid.util.Constants.N_GENERATED_PARKINGS;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_NAME_DRIVER;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_TYPE_DRIVER;
import static com.marcindziedzic.smartparkingsandroid.util.Constants.SD_TYPE_PARKING;

/**
 * Agent that manages the driver environment.
 */
public class DriverAgent extends Agent implements DriverInterface {

    private static final String TAG = "DriverAgent";

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
                    System.out.println("Created DriverAgent " + getAID().getName() + " with lat: " + this.localization.getLatitude()
                            + " lon: " + this.localization.getLongitude());
                }
            }
        }

        registerO2AInterface(DriverInterface.class, this);

        registerDriverAgentInDf();

        // Update list of parkings
        aviableParkings = getActualParkingAids();

        initAndroidAppEnvironment();

        addBehaviour(new ParkingDataCollectorRole(this, ParallelBehaviour.WHEN_ALL));


//
    }

    /**
     * Inits the android app modules after the agent is successfully created.
     */
    private void initAndroidAppEnvironment() {
        loginPresenter.onAgentStarted();
    }

    /**
     * Registers driver agent in Directory Facilitator. It is necessary to find that agent.
     */
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

    /**
     * Search for the parking agents.
     *
     * @return All AIDs founded.
     * @see AID
     */
    public ArrayList<AID> getActualParkingAids() {

        // Update list of parkings
        DFAgentDescription searchTemplate = new DFAgentDescription();
        ServiceDescription templateServiceDescription = new ServiceDescription();
        templateServiceDescription.setType(SD_TYPE_PARKING);
        searchTemplate.addServices(templateServiceDescription);

        SearchConstraints sc = new SearchConstraints();
        sc.setMaxDepth((long) 10000000);
        sc.setMaxResults((long) 10000000);

        DFAgentDescription[] result = new DFAgentDescription[0];
        try {
            result = DFService.search(this, searchTemplate, sc);
        } catch (FIPAException e) {
            Log.d(TAG, "getActualParkingAids: " + result.length);
            e.printStackTrace();
        }

        ArrayList<AID> parkingAidArray = new ArrayList<>();
        for (DFAgentDescription aResult : result) {
            parkingAidArray.add(aResult.getName());
        }
        Log.d(TAG, "getActualParkingAids: " + parkingAidArray.size());
        return parkingAidArray;
    }

    /**
     * Enables to perform updating parking list.
     * @param parkingData Data about available parkings.
     */
    public void updateParkingList(ArrayList<ParkingOffer> parkingData) {
        Log.d(TAG, "updateParkingList: ");
        Log.d(TAG, "updateParkingList: Collect " + parkingData.size() + " parkings out of " + N_GENERATED_PARKINGS);
        parkings = parkingData;
        if (getParkingsInfoCallback != null) {
            getParkingsInfoCallback.onParkingDataCollected(parkings);
        }
    }

    /**
     * It should be called after choosing the parking to update parking state.
     *
     * @param bestParking Best parking offer
     * @param sender      Agent which send the best offer.
     */
    public void onParkingChoosen(ParkingOffer bestParking, AID sender) {
        Log.d(TAG, "onParkingChoosen: ");
        this.bestParking = bestParking;
        this.bestParkingAgent = sender;
        getBestParkingCallback.onBestParkingFound(bestParking);
    }

    public AID getBestParkingAgent() {
        return bestParkingAgent;
    }
}

