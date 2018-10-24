package com.marcindziedzic.smartparkingsandroid.ontology;

import jade.content.AgentAction;

public class ParkingOffer implements AgentAction {

    private float price;

    private float lat;

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
