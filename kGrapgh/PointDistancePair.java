package kGrapgh;

import cacheIndex.Point;

// Helper class to store a point and its distance for use in the priority queue
public class PointDistancePair {
    Point point;
    double distance;

    public PointDistancePair(Point point, double distance) {
        this.point = point;
        this.distance = distance;
    }
}
