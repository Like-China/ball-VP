package linearcache;

import utils.Point;

// Dynamic Caching— Least-Recently-Used (LRU)
public class LRU extends Linercache {

    public LRU(int capacity) {
        super(capacity);
    }

    // Add a point to cache, success return true
    public Point update(Point p) {
        if (cachedPoints.contains(p)) {
            cachedPoints.remove(p);
            cachedPoints.add(p);
            return p;
        } else {
            Point p1 = null;
            if (cachedPoints.size() == capacity) {
                p1 = cachedPoints.remove(0);
            }
            cachedPoints.add(p);
            return p1;
        }
    }

}
