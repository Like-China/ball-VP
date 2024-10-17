package utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.PriorityQueue;

import evaluation.Settings;

// Class to represent a point in space
public class Point {
    public double[] vector;
    public int id; // Unique identifier for the point
    public boolean isQueryPoint = true;

    // the timestamp when this point is added into the cache
    public int ts = 0;
    // the hit number if it is stored as a cached point
    public int hitCount = 0;
    // the expense of get the kNN of this point
    public double expense = 0;
    // the NN neighbor of this point
    public PriorityQueue<NN> NNs = new PriorityQueue<>();

    public Point(int id, double[] vector, boolean isQueryPoint) {
        this.id = id;
        this.vector = vector;
        this.isQueryPoint = isQueryPoint;

    }

    // Euclidean distance calculation between this point and another point
    public double distanceTo(Point other) {
        return Settings.distFunction.distance(this.vector, other.vector);
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
        if (!(obj instanceof Point))
            return false;
        Point other = (Point) obj;
        if (id != other.id)
            {
                return false;
            }
        if (vector != other.vector) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vector);
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return String.format("Id: %d vector: %s, hit count: %d", id, Arrays.toString(vector), hitCount);
    }
}
