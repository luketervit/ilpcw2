package uk.ac.ed.inf.pizzadronz.models;

import java.util.List;

public class Region {
    private String name;
    private List<LngLat> vertices;

    // Constructor
    public Region(String name, List<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for vertices
    public List<LngLat> getVertices() {
        return vertices;
    }

    // Setter for vertices
    public void setVertices(List<LngLat> vertices) {
        this.vertices = vertices;
    }

    // Validation method
    public boolean isValid() {
        // A region must have at least 3 vertices to form a closed shape
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        // The first and last vertices must be the same
        if (!vertices.get(0).equals(vertices.get(vertices.size() - 1))) {
            return false;
        }

        // Validate each vertex
        for (LngLat vertex : vertices) {
            if (!vertex.isValid()) {
                return false;
            }
        }

        return true;
    }

    // Method to check if a given point is inside the region
    public boolean isInRegion(LngLat point) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        int intersections = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            LngLat v1 = vertices.get(i);
            LngLat v2 = vertices.get(i + 1);

            if (rayIntersectsSegment(point, v1, v2)) {
                intersections++;
            }
        }

        // Point is inside if the number of intersections is odd
        return (intersections % 2) == 1;
    }

    // Helper method to determine if a ray intersects a segment
    private boolean rayIntersectsSegment(LngLat point, LngLat v1, LngLat v2) {
        if (v1.getLat() > v2.getLat()) {
            LngLat temp = v1;
            v1 = v2;
            v2 = temp;
        }

        if (point.getLat() == v1.getLat() || point.getLat() == v2.getLat()) {
            point = new LngLat(point.getLng(), point.getLat() + 0.0001);
        }

        if (point.getLat() < v1.getLat() || point.getLat() > v2.getLat() || point.getLng() >= Math.max(v1.getLng(), v2.getLng())) {
            return false;
        }

        if (point.getLng() < Math.min(v1.getLng(), v2.getLng())) {
            return true;
        }

        double slope = (v2.getLng() - v1.getLng()) / (v2.getLat() - v1.getLat());
        double xIntersection = v1.getLng() + (point.getLat() - v1.getLat()) * slope;

        return point.getLng() < xIntersection;
    }
}
