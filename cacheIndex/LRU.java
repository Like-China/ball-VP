package cacheIndex;

import java.util.ArrayList;

import VPTree.NN;
import evaluation.Settings;

// Dynamic Caching— Least-Recently-Used (LRU)
public class LRU {

    private final int capacity;
    private final ArrayList<Point> cachedPoints;
    public Point minPP = null;

    public LRU(int capacity) {
        this.capacity = capacity;
        cachedPoints = new ArrayList<>();
    }

    public void print() {
        for (Point p : cachedPoints) {
            System.out.println(p.id + "/" + p.hitCount);
        }
    }

    // Add a point to cache, success return true
    public void update(Point p) {
        if (cachedPoints.contains(p)) {
            cachedPoints.remove(p);
            cachedPoints.add(p);
        } else {
            cachedPoints.add(p);
            if (cachedPoints.size() > capacity) {
                cachedPoints.remove(0);
            }
        }
    }

    // Given a point and an integer k, find the maximum distance of the kNNs
    public double find(Point p) {
        double minDist = Double.MAX_VALUE;
        minPP = null;
        for (Point pp : cachedPoints) {
            double dist = pp.distance(p);
            if (dist < minDist) {
                minDist = dist;
                minPP = pp;
            }
        }
        if (minPP != null) {
            double maxdist = 0;
            for (NN nn : minPP.NNs) {
                double dist = Settings.distFunction.distance(nn.vector, p.coordinates);
                if (dist >= maxdist) {
                    maxdist = dist;
                }
            }
            return maxdist;
        }
        return Double.MAX_VALUE;
    }

    public int size() {
        return cachedPoints.size();
    }

}
