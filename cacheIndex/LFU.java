package cacheIndex;

// Static Caching— Highest-Query-Frequency (HQF)
import java.util.PriorityQueue;

import VPTree.NN;
import evaluation.Settings;
import java.util.Comparator;

public class LFU {

    public static Comparator<Point> comp = new Comparator<Point>() {
        @Override
        public int compare(Point p1, Point p2) {
            return p1.hitCount - p2.hitCount > 0 ? 1 : -1;
        }
    };

    private final int capacity;
    private PriorityQueue<Point> cachedPoints;
    public Point minPP = null;

    public LFU(int capacity) {
        this.capacity = capacity;
        cachedPoints = new PriorityQueue<>(capacity, comp);
        // Random r = new Random();
        // for (int i = 0; i < 20; i++) {
        // Point p = new Point(i, new double[] { r.nextInt(30), 2 });
        // p.setHitCount(r.nextInt(30));
        // this.update(p);
        // }
        // System.out.println(cachedPoints.size());
        // while (!cachedPoints.isEmpty()) {
        // System.out.println(cachedPoints.poll().getHitCount());
        // }
    }

    public void init() {
        cachedPoints = new PriorityQueue<>(capacity, comp);
    }

    public void print() {
        PriorityQueue<Point> cpy = new PriorityQueue<>(cachedPoints);
        while (!cpy.isEmpty()) {
            Point p = cpy.poll();
            System.out.println(p.id + "/" + p.hitCount);
        }
    }

    // Add a point to cache, success return true
    public void update(Point p) {
        // if p is already in the cache, update its hit update by adding 1
        // else, add P into the cache
        if (cachedPoints.contains(p)) {
            cachedPoints.remove(p);
            p.addHitCount();
            cachedPoints.add(p);
        } else {
            if (cachedPoints.size() == capacity) {
                cachedPoints.poll();
            }
            cachedPoints.add(p);
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

    public PriorityQueue<Point> getCache() {
        return cachedPoints;
    }

    public void show() {
        while (!cachedPoints.isEmpty()) {
            System.out.println(cachedPoints.poll().hitCount);
        }
    }

    // public static void main(String[] args) {
    // new HQF(10);
    // }

}
