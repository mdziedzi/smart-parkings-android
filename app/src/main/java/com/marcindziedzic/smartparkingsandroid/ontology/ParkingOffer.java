package com.marcindziedzic.smartparkingsandroid.ontology;

import jade.content.AgentAction;

/**
 * Represents part of Parking Ontology.
 * Agents uses this to understand the offer which is sent from parking to driver.
 */

public class ParkingOffer implements AgentAction {

    /**
     * Current price of parking place per hour. The price is in PLN.
     */
    private float price;

    /**
     * Latitude - used for geolocation.
     */
    private float lat;

    /**
     * Longitude - used for geolocation.
     */
    private float lon;

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }
}
