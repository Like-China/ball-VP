package graphcache;

import linearcache.Point;

// helper function to store NN
public class PointDistancePair {
    public Point point;
    public double distance;

    public PointDistancePair(Point point, double distance) {
        this.point = point;
        this.distance = distance;
    }
}
