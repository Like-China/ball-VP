package VPTree;

public class NN {

    public double[] vector = null;
    public double dist2query;

    public NN(double[] vector, double dist2query) {
        this.vector = vector;
        this.dist2query = dist2query;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return vector[0] + "@" + dist2query;
    }

}
