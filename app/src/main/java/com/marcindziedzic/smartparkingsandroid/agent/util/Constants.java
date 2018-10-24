package com.marcindziedzic.smartparkingsandroid.agent.util;

public final class Constants {

    //Boot.class
    public static int N_GENERATED_PARKINGS = 5;
    public static int N_GENERATED_DRIVERS = 20;

    public static int SLEEP_BEFORE_PARKINGS_PRODUCTION = 10;
    public static int SLEEP_BEFORE_DRIVERS_PRODUCTION = 500;
    public static int SLEEP_BETWEEN_EACH_DRIVER_PRODUCTION = 1000;

    public static int MAX_LATITUDE = 100;
    public static int MAX_LONGITUDE = 100;
    public static int LONGITUDE = 0;
    public static int LATITUDE = 0;
    public static int MIN_LATITUDE = 0;
    public static int MIN_LONGITUDE = 0;

    public static double FLOOR_FACTOR = 1e2;

    public static int MAX_BASE_PRICE = 10;
    public static int MIN_BASE_PRICE = 1;
    public static int BASE_PRICE = 1;

    public static int MIN_CAPACITY = 10;
    public static int MAX_CAPACITY = 1;
    public static int CAPACITY = 20;

    public static int MAX_OCCUPIED_PLACES = 20;
    public static int MIN_OCCUPIED_PLACES = 2;
    public static int OCCUPIED_PLACES = 0;

    // ParkingManagerAgent.class
    public static double MIN_PARKING_PRICE = 0.2;
    public static double STATIC_PARKING_PRICE = 0.2;

    // DriverManagerAgent.class
    public static int TIMEOUT_WAITING_FOR_PARKING_REPLY = 2000;
    public static double PRICE_FACTOR = 0.1;
    public static double DISTANCE_FACTOR = 0.01;
}

