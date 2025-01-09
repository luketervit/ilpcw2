package uk.ac.ed.inf.pizzadronz.models;

public class NextPositionRequest {
    private Position start;
    private Double angle; // Allow null checks

    public Position getStart() {
        return start;
    }

    public void setStart(Position start) {
        this.start = start;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
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

    // Calculate the next position from the start point
    public Position calculateNextPosition() {
        if (start == null || angle == null || !start.isValid()) {
            throw new IllegalArgumentException("Invalid input data.");
        }

        LngLat startLngLat = new LngLat(start.getLng(), start.getLat());
        LngLat nextLngLat = startLngLat.nextPosition(angle, 0.00015); // Fixed distance

        Position nextPosition = new Position();
        nextPosition.setLng(nextLngLat.getLng());
        nextPosition.setLat(nextLngLat.getLat());

        return nextPosition;
    }
}
