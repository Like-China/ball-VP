package linearcache;

public class LFU extends Linercache {

    public LFU(int capacity) {
        super(capacity);
    }

    // Add a point to cache, success return true
    public void update(Point p) {
        // if p is already in the cache, update its hit update by adding 1
        // else, add P into the cache
        if (cachedPoints.contains(p)) {
            p.addHitCount();
        } else {
            if (cachedPoints.size() == capacity) {
                double minExpense = Double.MAX_VALUE;
                Point minP = cachedPoints.get(0);
                for (Point pp : cachedPoints) {
                    double e = pp.hitCount;
                    if (e < minExpense) {
                        minExpense = e;
                        minP = pp;
                    }
                }
                cachedPoints.remove(minP);
            }
            cachedPoints.add(p);
        }
    }

}
