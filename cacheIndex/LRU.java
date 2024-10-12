package cacheIndex;

// Dynamic Caching— Least-Recently-Used (LRU)
public class LRU extends Linercache {

    public LRU(int capacity) {
        super(capacity);
    }

    // Add a point to cache, success return true
    public void update(Point p) {
        if (cachedPoints.contains(p)) {
            cachedPoints.remove(p);
            cachedPoints.add(p);
        } else {
            cachedPoints.add(p);
            if (cachedPoints.size() > capacity) {
                cachedPoints.remove(0);
            }
        }
    }

}
