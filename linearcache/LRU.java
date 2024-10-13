package linearcache;

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
            if (cachedPoints.size() == capacity) {
                cachedPoints.remove(0);
            }
            cachedPoints.add(p);
        }
    }

}
