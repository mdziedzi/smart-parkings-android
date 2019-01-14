package com.marcindziedzic.smartparkingsandroid.util;

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
    public static int TIMEOUT_WAITING_FOR_PARKING_REPLY = 5000;

    public static final String SD_TYPE_PARKING = "parking";
    //    public static String IP = "192.168.0.10";
    public static String IP = "18.218.173.5";
//    public static final String IP = "192.168.62.41";
    public static String PORT = "1099";

    public static final String SD_TYPE_DRIVER = "driver";
    public static final String SD_NAME_DRIVER = "JADE-driver";

    public static final int REQUEST_INITIATOR_TIMEOUT = 5000;

    public static final String DESTINATION_TITLE = "miejsce docelowe";

    public static final String SHARED_PREFERENCES_NAME = "app_preferences";
    public static final String PREFERENCES_KEY = "user_preference";
    public static final int DEFAULT_PREFERENCE_VALUE = 50;

    public static final double PRICE_NORMALIZATION_FACTOR = 1;
    public static final double DISTANCE_NORMALIZATION_FACTOR = 9.266385;



}


