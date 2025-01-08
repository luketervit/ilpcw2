package uk.ac.ed.inf.pizzadronz.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constant.SystemConstants;
import uk.ac.ed.inf.pizzadronz.models.LngLat;

import java.util.*;

@Service
public class CalcDeliveryPath {

    private final RestTemplate restTemplate;

    public CalcDeliveryPath(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<LngLat> calculateDeliveryPath(String restaurantName) {
        // Fetch required data
        List<Map<String, List<LngLat>>> noFlyZones = fetchNoFlyZones();
        List<LngLat> centralArea = fetchCentralArea();
        LngLat restaurantLocation = fetchRestaurantLocation(restaurantName);
        LngLat appleton = new LngLat(SystemConstants.APPLETON_LNG, SystemConstants.APPLETON_LAT);

        if (restaurantLocation == null) {
            throw new IllegalArgumentException("Restaurant not found: " + restaurantName);
        }

        // Use A* algorithm to calculate the path
        return aStarPathfinding(restaurantLocation, appleton, noFlyZones, centralArea);
    }

    private List<Map<String, List<LngLat>>> fetchNoFlyZones() {
        try {
            String url = SystemConstants.NO_FLY_ZONES_API_URL;
            Map<String, List<LngLat>>[] noFlyZones = restTemplate.getForObject(url, Map[].class);
            return Arrays.asList(noFlyZones);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch no-fly zones: " + e.getMessage());
        }
    }

    private List<LngLat> fetchCentralArea() {
        try {
            String url = SystemConstants.CENTRAL_REGION_API_URL;
            LngLat[] centralArea = restTemplate.getForObject(url, LngLat[].class);
            return Arrays.asList(centralArea);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch central area: " + e.getMessage());
        }
    }

    private LngLat fetchRestaurantLocation(String restaurantName) {
        try {
            String url = SystemConstants.RESTAURANTS_API_URL;
            Map<String, Object>[] restaurants = restTemplate.getForObject(url, Map[].class);

            for (Map<String, Object> restaurant : restaurants) {
                if (restaurantName.equalsIgnoreCase((String) restaurant.get("name"))) {
                    Map<String, Double> location = (Map<String, Double>) restaurant.get("location");
                    return new LngLat(location.get("lng"), location.get("lat"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch restaurants: " + e.getMessage());
        }
        return null;
    }

    private List<LngLat> aStarPathfinding(LngLat start, LngLat goal, List<Map<String, List<LngLat>>> noFlyZones, List<LngLat> centralArea) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getFScore));
        Map<LngLat, LngLat> cameFrom = new HashMap<>();
        Map<LngLat, Double> gScore = new HashMap<>();
        Map<LngLat, Double> fScore = new HashMap<>();

        openSet.add(new Node(start, 0, calculateHeuristic(start, goal)));
        gScore.put(start, 0.0);
        fScore.put(start, calculateHeuristic(start, goal));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            LngLat currentPosition = current.getLngLat();

            if (currentPosition.isCloseTo(goal)) {
                return reconstructPath(cameFrom, currentPosition, goal);
            }

            for (double angle : SystemConstants.VALID_ANGLES) {
                LngLat neighbor = currentPosition.nextPosition(angle, SystemConstants.DRONE_MOVE_DISTANCE);

                if (!isInCentralArea(neighbor, centralArea) || isInNoFlyZone(neighbor, noFlyZones)) {
                    continue; // Skip invalid neighbors
                }

                double tentativeGScore = gScore.getOrDefault(currentPosition, Double.MAX_VALUE) + SystemConstants.DRONE_MOVE_DISTANCE;

                if (tentativeGScore < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, currentPosition);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, tentativeGScore + calculateHeuristic(neighbor, goal));

                    if (openSet.stream().noneMatch(node -> node.getLngLat().equals(neighbor))) {
                        openSet.add(new Node(neighbor, tentativeGScore, tentativeGScore + calculateHeuristic(neighbor, goal)));
                    }
                }
            }
        }

        throw new IllegalStateException("No valid path found from the restaurant to Appleton.");
    }

    private List<LngLat> reconstructPath(Map<LngLat, LngLat> cameFrom, LngLat current, LngLat goal) {
        List<LngLat> path = new ArrayList<>();
        path.add(goal);

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

    private boolean isInCentralArea(LngLat position, List<LngLat> centralArea) {
        return position.isInPolygon(centralArea);
    }

    private boolean isInNoFlyZone(LngLat position, List<Map<String, List<LngLat>>> noFlyZones) {
        for (Map<String, List<LngLat>> noFlyZone : noFlyZones) {
            for (List<LngLat> polygon : noFlyZone.values()) {
                if (position.isInPolygon(polygon)) {
                    return true;
                }
            }
        }
        return false;
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
