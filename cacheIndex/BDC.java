package cacheIndex;

import java.util.ArrayList;

import VPTree.NN;
import evaluation.Settings;

// Dynamic Caching— Least-Recently-Used (BDC)
public class BDC {

    private final int capacity;
    public ArrayList<Point> cachedPoints;
    public Point minPP = null;

    public BDC(int capacity) {
        this.capacity = capacity;
        cachedPoints = new ArrayList<>();
    }

    public void update(Point p, int currentTs) {
        if (cachedPoints.contains(p)) {
            p.addHitCount();
        } else {
            if (cachedPoints.size() == capacity) {
                double minExpense = Double.MAX_VALUE;
                Point minP = cachedPoints.get(0);
                for (Point pp : cachedPoints) {
                    assert currentTs - pp.ts > 0;
                    double e = pp.expense * pp.hitCount / Math.pow(currentTs - pp.ts, 2);
                    if (e < minExpense) {
                        minExpense = e;
                        minP = pp;
                    }
                }
                cachedPoints.remove(minP);
            }
            p.addHitCount();
            p.ts = currentTs;
            cachedPoints.add(p);
        }
    }

    public void print() {
        for (Point p : cachedPoints) {
            System.out.println(p.id + "/" + p.hitCount + "/" + p.expense + "/" + p.ts);
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
