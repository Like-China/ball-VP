package linearcache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import utils.NN;
import utils.Point;

public class Linercache {

    public int capacity;
    public ArrayList<Point> cachedPoints;
    public Point minPP = null;
    public NN minNN = null;

    public Linercache(int capacity) {
        this.capacity = capacity;
        cachedPoints = new ArrayList<>();
    }

    public void print() {
        for (Point p : cachedPoints) {
            System.out.println(p.id + "/" + p.hitCount);
        }
    }

    // Given a point and an integer k, find the maximum distance of the kNNs
    // first search NN in the query level, then search NN in the object level
    public double find(Point p) {
        double minDist = Double.MAX_VALUE;
        minPP = null;
        for (Point pp : cachedPoints) {
            double dist = pp.distanceTo(p);
            if (dist < minDist) {
                minDist = dist;
                minPP = pp;
            }
        }
        if (minPP != null) {
            double maxdist = 0;
            for (NN nn : minPP.NNs) {
                double dist = nn.point.distanceTo(p);
                if (dist >= maxdist) {
                    maxdist = dist;
                    minNN = nn;
                }
            }
            return maxdist;
        }
        return Double.MAX_VALUE;
    }

    // directly search top-k NN in the object-level
    public double find2(Point p, int k) {
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        if (this.size() < capacity) {
            return Double.MAX_VALUE;
        }
        // assert this.size() == capacity : this.size() + "/" + capacity;
        // need to record the NN from which query
        HashSet<Point> cachedObjectPoints = new HashSet<>();
        for (Point pp : cachedPoints) {
            for (NN nn : pp.NNs) {
                cachedObjectPoints.add(nn.point);

            }
        }
        for (Point nnP : cachedObjectPoints) {
            double dist = nnP.distanceTo(p);
            if (pq.size() < k || dist < pq.peek().dist2query) {
                pq.add(new NN(nnP, dist));
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
