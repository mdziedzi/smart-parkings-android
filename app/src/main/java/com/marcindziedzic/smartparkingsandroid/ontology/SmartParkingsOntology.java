package com.marcindziedzic.smartparkingsandroid.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.CFReflectiveIntrospector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.PredicateSchema;

/**
 * Main ontology class. It describes ontology used in project.
 */
public class SmartParkingsOntology extends Ontology implements SmartParkingsVocabulary {

    // The singleton instance of this ontology
    private static Ontology theInstance = new SmartParkingsOntology();

    private SmartParkingsOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance(), new CFReflectiveIntrospector());
        try {
            add(new PredicateSchema(PARKING_OFFER), ParkingOffer.class);

            PredicateSchema ps = (PredicateSchema) getSchema(PARKING_OFFER);
            ps.add(PARKING_OFFER_PRICE, getSchema(BasicOntology.FLOAT));
            ps.add(PARKING_OFFER_LAT, getSchema(BasicOntology.FLOAT));
            ps.add(PARKING_OFFER_LON, getSchema(BasicOntology.FLOAT));
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
        try {
            add(new PredicateSchema(RESERVATION_REQUEST), ReservationRequest.class);

//            PredicateSchema ps = (PredicateSchema) getSchema(RESERVATION_REQUEST);
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }

    public static Ontology getInstance() {
        return theInstance;
    }
}
