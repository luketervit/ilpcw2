package uk.ac.ed.inf.pizzadronz.constant;

import uk.ac.ed.inf.pizzadronz.models.LngLat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Contains system-wide constants for the PizzaDronz project.
 */
public final class SystemConstants {

    public static final String NO_FLY_ZONES_API_URL = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";
    public static final String CENTRAL_REGION_API_URL = "https://ilp-rest-2024.azurewebsites.net/centralArea";
    public static final String RESTAURANTS_API_URL = "https://ilp-rest-2024.azurewebsites.net/restaurants";

    public static final double NO_FLY_ZONE_BUFFER = 0.0005;
    public static final double[] VALID_ANGLES = {0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0};


    // Order-related constants
    public static final int ORDER_CHARGE_IN_PENCE = 100;
    public static final int MAX_PIZZAS_PER_ORDER = 4;

    // Drone-related constants
    public static final double DRONE_MOVE_DISTANCE = 0.00015;
    public static final double DRONE_IS_CLOSE_DISTANCE = 0.00015;
    public static final int DRONE_MAX_MOVES = 2000;

    // Central region
    public static final String CENTRAL_REGION_NAME = "appleton";
    public static final double APPLETON_LNG = -3.186874;
    public static final double APPLETON_LAT = 55.944494;



    private SystemConstants() {
        // Prevent instantiation
    }
}
