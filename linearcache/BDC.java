package linearcache;

import utils.Point;

// Dynamic Caching— Least-Recently-Used (BDC)
public class BDC extends Linercache {

    public BDC(int capacity) {
        super(capacity);
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
                    double e = pp.expense * pp.hitCount / Math.pow(currentTs - pp.ts, 1);
                    if (e < minExpense) {
                        // System.out.println();
                        // System.out.println(pp.expense * pp.hitCount);
                        // System.out.println(currentTs - pp.ts);
                        // System.out.println(e);
                        minExpense = e;
                        minP = pp;
                    }
                }
                cachedPoints.remove(minP);
            }
            // p.addHitCount();
            p.ts = currentTs;
            cachedPoints.add(p);
        }
    }

}
