package uk.ac.ed.inf.pizzadronz.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LngLatPairRequest {

    @JsonProperty("position1")
    private Position position1;

    @JsonProperty("position2")
    private Position position2;

    @JsonProperty("position")
    private Position position;

    private Region region;

    public Position getPosition1() {
        return position1;
    }

    public void setPosition1(Position position1) {
        this.position1 = position1;
    }

    public Position getPosition2() {
        return position2;
    }

    public void setPosition2(Position position2) {
        this.position2 = position2;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    // Helper method to calculate distance between position1 and position2
    public double calculateDistance() {
        if (position1 == null || position2 == null) {
            throw new IllegalArgumentException("Both positions must be non-null.");
        }

        LngLat lngLat1 = new LngLat(position1.getLng(), position1.getLat());
        LngLat lngLat2 = new LngLat(position2.getLng(), position2.getLat());

        return LngLat.calculateDistance(lngLat1, lngLat2);
    }

    // Method to check if a position is inside the given region
    public boolean isInRegion() {
        if (position == null || region == null) {
            throw new IllegalArgumentException("Position and region must be non-null.");
        }

        LngLat lngLat = new LngLat(position.getLng(), position.getLat());
        return region.isInRegion(lngLat);
    }

    public static class Position {
        private double lng;
        private double lat;

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

        public boolean isValid() {
            return lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90;
        }
    }


    public boolean isCloseTo() {
        if (position1 == null || position2 == null) {
            throw new IllegalArgumentException("Both positions must be non-null.");
        }

        // Get the latitude and longitude for both positions
        double lng1 = position1.getLng();
        double lat1 = position1.getLat();
        double lng2 = position2.getLng();
        double lat2 = position2.getLat();

        // Calculate the differences in longitude and latitude
        double deltaLng = lng2 - lng1;
        double deltaLat = lat2 - lat1;

        // Calculate the Euclidean distance
        double distance = Math.sqrt(deltaLng * deltaLng + deltaLat * deltaLat);

        // Return true if the distance is less than 0.00015
        return distance < 0.00015;
    }



}
