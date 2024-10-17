package utils;

public class NN {

    public Point point = null;
    public double dist2query;

    public NN(Point p, double dist2query) {
        this.point = p;
        this.dist2query = dist2query;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return point.id + "@" + dist2query;
    }

}
