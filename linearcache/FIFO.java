package linearcache;

import utils.Point;

// Dynamic Caching— Least-Recently-Used (FIFO)
public class FIFO extends Linercache {

    public FIFO(int capacity) {
        super(capacity);
    }

    // Add a point to cache, success return true
    public void update(Point p) {
        if (cachedPoints.size() == capacity) {
            cachedPoints.remove(0);
        }
        cachedPoints.add(p);
    }

}
