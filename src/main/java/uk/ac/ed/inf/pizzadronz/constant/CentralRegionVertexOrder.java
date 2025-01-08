package uk.ac.ed.inf.pizzadronz.constant;

import uk.ac.ed.inf.pizzadronz.models.LngLat;

import java.util.List;
import java.util.Arrays;

/**
 * Defines the order of the coordinates for the "central" region.
 */
public final class CentralRegionVertexOrder {
    // Vertex indices for the central region
    public static final int TOP_LEFT = 0;
    public static final int BOTTOM_LEFT = 1;
    public static final int BOTTOM_RIGHT = 2;
    public static final int TOP_RIGHT = 3;

    // List of LngLat objects representing the central area's vertices
    public static final List<LngLat> CENTRAL_AREA_VERTICES = Arrays.asList(
            new LngLat(-3.192473, 55.946233), // TOP_LEFT
            new LngLat(-3.192473, 55.942617), // BOTTOM_LEFT
            new LngLat(-3.184319, 55.942617), // BOTTOM_RIGHT
            new LngLat(-3.184319, 55.946233)  // TOP_RIGHT
    );

    private CentralRegionVertexOrder() {
        // Prevent instantiation
    }
}
