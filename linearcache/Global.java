package linearcache;

import java.util.*;

import utils.NN;
import utils.Point;

public class Global {

    public int capacity;
    public HashSet<Point> cachedPoints;
    public Point minPP = null;
    public NN minNN = null;

    public Global(int capacity) {
        this.capacity = capacity;
        cachedPoints = new HashSet<>();
    }

    public void add(Point p) {
        cachedPoints.add(p);
    }

    public void print() {
        for (Point p : cachedPoints) {
            System.out.println(p.id + "/" + p.hitCount);
        }
    }

    // Given a point and an integer k, find the maximum distance of the kNNs
    public double find(Point p, int k) {
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        if (this.size() < k) {
            return Double.MAX_VALUE;
        }
        for (Point pp : cachedPoints) {
            double dist = pp.distanceTo(p);
            if (pq.size() < k || dist < pq.peek().dist2query) {
                pq.add(new NN(pp, dist));
            }
            if (pq.size() > k) {
                pq.poll();
            }
        }
        assert pq.size() == k : pq.size() + "/" + k;
        return pq.peek().dist2query;
    }

    public int size() {
        return cachedPoints.size();
    }
}
