package cacheIndex;

import java.util.LinkedHashMap;
import java.util.Map;

import VPTree.NN;
import evaluation.Settings;

// Dynamic Caching— Least-Recently-Used (LRU)
public class LRUCache {

    private final int capacity;
    private final LinkedHashMap<Point, Integer> cachedPoints;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cachedPoints = new LinkedHashMap<Point, Integer>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Point, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    // Remove a point from cache, success return true
    public boolean remove(Point p) {
        return cachedPoints.remove(p) != null;
    }

    // Add a point to cache, success return true
    public boolean add(Point p) {
        if (cachedPoints.size() < capacity) {
            cachedPoints.put(p, 0);
            return true;
        }
        return false;
    }

    // Given a point and an integer k, find the maximum distance of the kNNs
    public double find(Point p) {
        double minDist = Double.MAX_VALUE;
        Point minPP = null;
        for (Point pp : cachedPoints.keySet()) {
            double dist = pp.distance(p);
            if (dist < minDist) {
                minDist = dist;
                minPP = pp;
            }
        }
        if (minPP != null) {
            remove(minPP);
            add(minPP);
            double maxdist = 0;
            for (NN nn : minPP.NNs) {
                double dist = Settings.distFunction.distance(nn.vector, p.coordinates);
                if (dist >= maxdist) {
                    maxdist = dist;
                }
            }
            return maxdist;
        }
        return Double.MAX_VALUE; // No points found
    }

    public int size() {
        return cachedPoints.size();
    }

}
