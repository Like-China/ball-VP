/**
    @author Akshay Mattoo
*/

package VPTree;

import java.util.ArrayList;

import utils.Point;

/**
 * Class for representing an item in a VP node
 */
public class Item {
    private Point point;
    private ArrayList<Double> distToVPs;

    /**
     * Constructor
     * 
     * @param Image object
     */
    public Item(Point p) {
        point = p;
        distToVPs = new ArrayList<Double>();
    }

    public Point getPoint() {
        return point;
    }

    /**
     * @return Image stored in the object
     */
    public float[] getVector() {
        return point.vector;
    }

    /**
     * @return ArrayList distToVPs
     */
    public ArrayList<Double> getDistToVPs() {
        return distToVPs;
    }

    /**
     * Appends a new value to distToVPs
     * 
     * @param double d
     */
    public void push(double d) {
        distToVPs.add(d);
    }

    /**
     * @return last value in distToVPs
     */
    public double tail() {
        return distToVPs.get(distToVPs.size() - 1);
    }

}
