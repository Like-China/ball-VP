package cacheIndex;

import java.util.ArrayList;

public class Cache {

    public int capacity = 100;
    ArrayList<Point> cachedPoints = new ArrayList<>();

    // remove a point from cache, success return true
    public boolean remove(Point p) {
        if (cachedPoints.isEmpty()) {
            return false;
        }
        if (cachedPoints.contains(p)) {
            cachedPoints.remove(p);
            return true;
        }
        return false;
    }

    // add a point to cache, success return true
    public boolean add(Point p) {
        if (cachedPoints.size() < capacity) {
            cachedPoints.add(p);
            return true;
        }
        return false;
    }

    // Given a point and an integer k, find the maximum distance of the kNNs
    public double find(Point p) {
        double minDist = Double.MAX_VALUE;
        Point minPP = null;
        for (Point pp : cachedPoints) {
            double dist = pp.distance(p);
            if (dist < minDist) {
                minDist = dist;
                minPP = pp;
            }
        }
        // update the hit point
        minPP.addHitCount();
        assert minPP != null;
        double maxDist = minDist;
        for (Point pp : minPP.NNs) {
            double dist = pp.distance(p);
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        assert maxDist != 0;
        return maxDist;
    }

    // run a round of cache search
    // 1. find the kNN distance of a given point
    // 2. determine to put this query point into cache or not
    // 3. if update is triggered, add the query point and remove a existing point

    // Dynamic Caching—Least-Recently-Used (LRU)
    public double LRU(Point p) {
        // init
        if (cachedPoints.size() < capacity) {
            return Double.MAX_VALUE;
        }
        double maxKdist = find(p);
        // if the maxKdsit is ineffective, then update cache
        boolean isEffective = true;
        if (isEffective) {
            remove(cachedPoints.get(0));
            add(p);
        }
        return maxKdist;
    }

    // Static Caching— Highest-Query-Frequency (HQF)
    public double HQF(Point p) {
        // init
        if (cachedPoints.size() < capacity) {
            return Double.MAX_VALUE;
        }
        double maxKdist = find(p);
        // if the maxKdsit is ineffective, then update cache
        boolean isEffective = true;
        if (isEffective) {
            remove(cachedPoints.get(0));
            add(p);
        }
        return maxKdist;
    }

}
