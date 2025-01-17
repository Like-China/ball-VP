package utils;

import java.util.*;

// Class to represent a point in space
public class Point {
    public int id; // Unique identifier for the point
    public float[] vector;
    public boolean isQueryPoint = true;

    // the last timestamp when this point is added into the cache
    public int ts = 0;
    // the hit number if it is stored as a cached point
    public int hitCount = 0;
    // the node access of getting the kNN
    public long nodeAccess = 0;
    // the NN neighbor of this point
    public PriorityQueue<NN> NNs = new PriorityQueue<>();
    // if the point is an object point, record its reverse kNN query points
    private HashSet<Point> rKNNs = new HashSet<>();

    public Point(int id, float[] vector) {
        this.id = id;
        this.vector = vector;
    }

    public Point(int id, float[] vector, boolean isQueryPoint) {
        this.id = id;
        this.vector = vector;
        this.isQueryPoint = isQueryPoint;
    }

    public void init() {
        NNs = new PriorityQueue<>();
        rKNNs = new HashSet<>();
    }

    public int rkNNSize() {
        return rKNNs.size();
    }

    public void addrKNNs(Point p) {
        rKNNs.add(p);
    }

    public HashSet<Point> getrKNNs() {
        return rKNNs;
    }

    // Euclidean distance calculation between this point and another point
    public double distanceTo(Point other) {
        float[] p1 = this.vector;
        float[] p2 = other.vector;
        double dist = 0.0;
        for (int i = 0; i < p1.length; i++) {
            float diff = p1[i] - p2[i];
            dist += diff * diff;
        }
        // If true Euclidean distance is needed, uncomment the following line
        return Math.sqrt(dist);
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

    public void setNNs(PriorityQueue<NN> NNs, long nodeAccess) {
        this.nodeAccess = nodeAccess;
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
        if (id != other.id) {
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
        return id + " ";// String.format("Id: %d vector: %s", id, Arrays.toString(vector));
    }
}
