package uk.ac.ed.inf.pizzadronz.models;

import java.util.List;
import uk.ac.ed.inf.pizzadronz.constant.SystemConstants;


public class LngLat {
    private double lng;
    private double lat;

    public LngLat(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    // Validate if the LngLat is valid
    public boolean isValid() {
        return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
    }

    public static boolean areValid(LngLat position1, LngLat position2) {
        return position1 != null && position2 != null &&
                position1.isValid() && position2.isValid();
    }

    // Method to calculate Euclidean distance between two LngLat points
    public static double calculateDistance(LngLat point1, LngLat point2) {
        double deltaLng = point2.getLng() - point1.getLng();
        double deltaLat = point2.getLat() - point1.getLat();
        return Math.sqrt(deltaLng * deltaLng + deltaLat * deltaLat); // Distance in units
    }

    public LngLat nextPosition(double angle, double distance) {
        double radians = Math.toRadians(angle);
        double deltaLng = distance * Math.cos(radians);
        double deltaLat = distance * Math.sin(radians);
        return new LngLat(this.lng + deltaLng, this.lat + deltaLat);
    }

    public boolean isCloseTo(LngLat other) {
        if (other == null) return false;
        return calculateDistance(this, other) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    public boolean isInPolygon(List<LngLat> polygon) {
        int intersections = 0;
        int numPoints = polygon.size();
        for (int i = 0; i < numPoints; i++) {
            LngLat p1 = polygon.get(i);
            LngLat p2 = polygon.get((i + 1) % numPoints);

            if (rayIntersectsSegment(this, p1, p2)) {
                intersections++;
            }
        }
        return (intersections % 2) == 1;
    }

    private boolean rayIntersectsSegment(LngLat point, LngLat p1, LngLat p2) {
        if (p1.getLat() > p2.getLat()) {
            LngLat temp = p1;
            p1 = p2;
            p2 = temp;
        }

        if (point.getLat() == p1.getLat() || point.getLat() == p2.getLat()) {
            point = new LngLat(point.getLng(), point.getLat() + 0.00001);
        }

        if (point.getLat() < p1.getLat() || point.getLat() > p2.getLat() || point.getLng() >= Math.max(p1.getLng(), p2.getLng())) {
            return false;
        }

        if (point.getLng() < Math.min(p1.getLng(), p2.getLng())) {
            return true;
        }

        double edgeSlope = (p2.getLat() - p1.getLat()) / (p2.getLng() - p1.getLng());
        double pointSlope = (point.getLat() - p1.getLat()) / (point.getLng() - p1.getLng());
        return pointSlope >= edgeSlope;
    }
}
