package uk.ac.ed.inf.pizzadronz.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constant.SystemConstants;
import uk.ac.ed.inf.pizzadronz.models.*;

import java.util.*;

@Service
public class CalcDeliveryPath {

    private final RestTemplate restTemplate;

    public CalcDeliveryPath(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<LngLat> findPath(Order order) {
        // Fetch restaurant coordinates
        RestaurantCoordinates restaurantCoordinates = getRestaurantCoordinates(order);

        // Fetch no-fly zones and central area
        List<Region> noFlyZones = fetchNoFlyZones();
        Region centralRegion = fetchCentralRegion();

        // Starting point (restaurant) and endpoint (Appleton)
        LngLat start = new LngLat(restaurantCoordinates.getLng(), restaurantCoordinates.getLat());
        LngLat goal = new LngLat(SystemConstants.APPLETON_LNG, SystemConstants.APPLETON_LAT);

        // Perform A* algorithm
        return aStarPathfinding(start, goal, noFlyZones, centralRegion);
    }

    private RestaurantCoordinates getRestaurantCoordinates(Order order) {
        List<Restaurant> restaurants = fetchRestaurants();

        Restaurant restaurant = restaurants.stream()
                .filter(r -> r.getMenu().stream()
                        .anyMatch(pizza -> pizza.getName().equalsIgnoreCase(order.getPizzasInOrder().get(0).getName())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Restaurant for the first pizza not found."));

        return new RestaurantCoordinates(restaurant.getName(), restaurant.getLocation().getLng(), restaurant.getLocation().getLat());
    }

    private List<Restaurant> fetchRestaurants() {
        try {
            String url = SystemConstants.RESTAURANTS_API_URL;
            Restaurant[] restaurants = restTemplate.getForObject(url, Restaurant[].class);
            if (restaurants == null || restaurants.length == 0) {
                throw new RuntimeException("No restaurants found from API.");
            }
            return List.of(restaurants);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants: " + e.getMessage(), e);
        }
    }

    private List<Region> fetchNoFlyZones() {
        try {
            String url = SystemConstants.NO_FLY_ZONES_API_URL;
            Region[] noFlyZones = restTemplate.getForObject(url, Region[].class);
            if (noFlyZones == null || noFlyZones.length == 0) {
                throw new RuntimeException("No no-fly zones found from API.");
            }
            return List.of(noFlyZones);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch no-fly zones: " + e.getMessage(), e);
        }
    }

    private Region fetchCentralRegion() {
        try {
            String url = SystemConstants.CENTRAL_REGION_API_URL;
            Region centralRegion = restTemplate.getForObject(url, Region.class);
            if (centralRegion == null) {
                throw new RuntimeException("Failed to fetch central region from API.");
            }
            return centralRegion;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch central region: " + e.getMessage(), e);
        }
    }

    private List<LngLat> aStarPathfinding(LngLat start, LngLat goal, List<Region> noFlyZones, Region centralRegion) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFScore));
        Map<LngLat, LngLat> cameFrom = new HashMap<>();
        Map<LngLat, Double> gScore = new HashMap<>();
        Map<LngLat, Double> fScore = new HashMap<>();

        openSet.add(new Node(start, 0, calculateHeuristic(start, goal)));
        gScore.put(start, 0.0);
        fScore.put(start, calculateHeuristic(start, goal));

        boolean enteredCentralRegion = false;
        int maxIterations = 50000; // Prevent infinite loops
        int iterations = 0;

        while (!openSet.isEmpty()) {
            if (++iterations > maxIterations) {
                throw new IllegalStateException("Pathfinding exceeded maximum iterations.");
            }

            Node current = openSet.poll();
            LngLat currentPosition = current.getLngLat();

            if (currentPosition.isCloseTo(goal)) {
                return reconstructPath(cameFrom, currentPosition);
            }

            for (double angle : SystemConstants.VALID_ANGLES) {
                LngLat neighbor = currentPosition.nextPosition(angle, SystemConstants.DRONE_MOVE_DISTANCE);

                if (!isValidMove(currentPosition, neighbor, noFlyZones, centralRegion, enteredCentralRegion)) {
                    continue;
                }

                if (!enteredCentralRegion && centralRegion.isInRegion(neighbor)) {
                    enteredCentralRegion = true;
                }

                double tentativeGScore = gScore.getOrDefault(currentPosition, Double.MAX_VALUE) + SystemConstants.DRONE_MOVE_DISTANCE;

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, currentPosition);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + calculateHeuristic(neighbor, goal));

                    openSet.add(new Node(neighbor, tentativeGScore, fScore.get(neighbor)));
                }
            }
        }

        throw new IllegalStateException("No path found from the restaurant to Appleton.");
    }

    private boolean isValidMove(LngLat current, LngLat next, List<Region> noFlyZones, Region centralRegion, boolean enteredCentralRegion) {
        if (enteredCentralRegion && !centralRegion.isInRegion(next)) {
            return false;
        }

        for (Region noFlyZone : noFlyZones) {
            if (noFlyZone.isInRegion(next)) {
                return false;
            }
        }

        return true;
    }

    private List<LngLat> reconstructPath(Map<LngLat, LngLat> cameFrom, LngLat current) {
        List<LngLat> path = new ArrayList<>();
        while (cameFrom.containsKey(current)) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private double calculateHeuristic(LngLat a, LngLat b) {
        return LngLat.calculateDistance(a, b);
    }

    public static class RestaurantCoordinates {
        private final String name;
        private final double lng;
        private final double lat;

        public RestaurantCoordinates(String name, double lng, double lat) {
            this.name = name;
            this.lng = lng;
            this.lat = lat;
        }

        public String getName() {
            return name;
        }

        public double getLng() {
            return lng;
        }

        public double getLat() {
            return lat;
        }

        @Override
        public String toString() {
            return "RestaurantCoordinates{" +
                    "name='" + name + '\'' +
                    ", lng=" + lng +
                    ", lat=" + lat +
                    '}';
        }
    }

    private static class Node {
        private final LngLat lngLat;
        private final double gScore;
        private final double fScore;

        public Node(LngLat lngLat, double gScore, double fScore) {
            this.lngLat = lngLat;
            this.gScore = gScore;
            this.fScore = fScore;
        }

        public LngLat getLngLat() {
            return lngLat;
        }

        public double getFScore() {
            return fScore;
        }
    }
}
