/**
    @author Akshay Mattoo
*/

package VPTree;

import java.util.ArrayList;

/**
 * Class for representing an item in a VP node
 */
public class Item {
    private double[] vector;
    private ArrayList<Double> distToVPs;

    /**
     * Constructor
     * 
     * @param Image object
     */
    public Item(double[] p) {
        vector = p;
        distToVPs = new ArrayList<Double>();
    }

    /**
     * @return Image stored in the object
     */
    public double[] getVector() {
        return vector;
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

    /**
     * Checks whether two Item objects are identical or not
     * Compares the images of the two items and then compares each corresponding
     * value in distToVPs
     * 
     * @return true if the two objects are identical, false otherwise
     */
    public boolean equals(Item itm) {
        if (itm == null)
            return false;
        if (vector.equals(itm.getVector()) == false)
            return false;
        if (itm.getDistToVPs().size() != distToVPs.size())
            return false;

        for (int i = 0; i < distToVPs.size(); ++i) {
            if (distToVPs.get(i) != itm.getDistToVPs().get(i))
                return false;
        }
        return true;
    }
}
