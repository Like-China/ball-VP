package cacheIndex;

import java.util.Arrays;
import java.util.Objects;
import java.util.PriorityQueue;

import VPTree.NN;
import evaluation.Settings;

// Class to represent a point in space
public class Point {
    public double[] coordinates;
    public int id; // Unique identifier for the point

    // the timestamp when this point is added into the cache
    public int ts = 0;
    // the hit number if it is stored as a cached point
    public int hitCount = 0;
    // the expense of get the kNN of this point
    public double expense = 0;
    // the NN neighbor of this point
    PriorityQueue<NN> NNs = new PriorityQueue<>();

    public Point(int id, double[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    // Euclidean distance calculation between this point and another point
    public double distance(Point other) {
        return Settings.distFunction.distance(this.coordinates, other.coordinates);
    }

    public void addHitCount() {
        hitCount += 1;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setNNs(PriorityQueue<NN> NNs, double expense) {
        this.expense = expense;
        this.NNs = NNs;
    }

    public PriorityQueue<NN> getNNs() {
        return NNs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Point))
            return false;
        Point other = (Point) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return String.format("Id: %d vector: %s, hit count: %d", id, Arrays.toString(coordinates), hitCount);
    }
}
