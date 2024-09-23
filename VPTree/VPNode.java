/**
    @author Akshay Mattoo
*/

package VPTree;

import java.util.ArrayList;

/**
 * Class to represent a vertex in the VP Tree
 */
public class VPNode {
    private Item itm;
    private VPNode left;
    private VPNode right;
    private double mu;
    // the maximam distance to the Node from the left partition and right partition
    public double leftMax = 0;
    public double rightMax = 0;
    // the elements in this node
    public ArrayList<Item> items = null;
    // the least possible distance of vectors in this node to the query
    public double distLowerBound = 0;

    /**
     * Contructor
     * Initilises mu
     * Sets left and right to null
     * 
     * @param Item object for storing in this Node
     */
    public VPNode(Item i) {
        itm = i;
        left = null;
        right = null;
        mu = 0.0;
    }

    /**
     * @return Item object stored in this node
     */
    public Item getItem() {
        return itm;
    }

    /**
     * @return left child of this Node
     */
    public VPNode getLeft() {
        return left;
    }

    /**
     * Sets left child of this Node object
     * 
     * @param left child Node object
     */
    public void setLeft(VPNode l) {
        left = l;
    }

    /**
     * @return value of mu
     */
    public double getMu() {
        return mu;
    }

    /**
     * Sets value of mu
     * 
     * @param floating point value of mu
     */
    public void setMu(double d) {
        mu = d;
    }

    /**
     * @return right child of this Node
     */
    public VPNode getRight() {
        return right;
    }

    /**
     * Sets right child of this Node object
     * 
     * @param right child Node object
     */
    public void setRight(VPNode r) {
        right = r;
    }
}
