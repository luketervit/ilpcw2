package uk.ac.ed.inf.pizzadronz.models;

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
        // Euclidean distance formula: sqrt((x2 - x1)^2 + (y2 - y1)^2)
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


}
