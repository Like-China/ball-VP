
package VPTree;

import Distance.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public class VPTreeByFarest extends VPTree {

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTreeByFarest(double[][] vectors, DistanceFunction d) {
		super(vectors, d);
		int i = new Random().nextInt(vectors.length);
		// Item initVpItem = list.get(i);
		Item initVpItem = getVP(list);
		root = recurse(list, initVpItem);
	}

	/**
	 * Helper function of the Constructor
	 * Recursively builds the VP Tree
	 * 
	 * @param ArrayList of Item objects
	 * @return Node object which is the root of the VP Tree
	 */
	private VPNode recurse(ArrayList<Item> list, Item itemVP) {
		if (list.size() == 0)
			return null;
		VPNode n = new VPNode(itemVP);
		deleteItem(list, n.getItem());

		ArrayList<Double> arr = new ArrayList<Double>();
		for (Item itm : list) {
			double dist = dFunc.distance(itm.getVector(), n.getItem().getVector());
			itm.push(dist);
			arr.add(dist);
		}

		double mu = getMedian(arr);
		n.setMu(mu);

		ArrayList<Item> L = new ArrayList<Item>();
		ArrayList<Item> R = new ArrayList<Item>();

		double maxLeft = 0;
		double maxRight = 0;
		Item leftVP = null;
		Item rightVP = null;
		for (Item itm : list) {
			double dist2VP = itm.tail();
			if (dist2VP < mu) {
				L.add(itm);
				if (dist2VP > maxLeft) {
					maxLeft = dist2VP;
					leftVP = itm;
				}
			} else {
				R.add(itm);
				if (dist2VP > maxRight) {
					maxRight = dist2VP;
					rightVP = itm;
				}
			}
		}
		n.leftMax = maxLeft;
		n.rightMax = maxRight;
		n.setLeft(recurse(L, leftVP));
		n.setRight(recurse(R, rightVP));
		return n;
	}

}
