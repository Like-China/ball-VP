package cacheIndex;

// Dynamic Caching— Least-Recently-Used (BDC)
public class Global extends Linercache {

    public Global(int capacity) {
        super(capacity);
    }

    public void update(Point p) {
        if (cachedPoints.contains(p)) {
            p.addHitCount();
        } else {
            cachedPoints.add(p);
        }
    }

}
