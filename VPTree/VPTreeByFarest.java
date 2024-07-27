/**
    @author Akshay Mattoo
*/

package VPTree;

import Distance.*;

import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public final class VPTreeByFarest extends VPTreeBySample {
	private VPNode root;
	private DistanceFunction dFunc;
	// the distance of the currently discovered NN
	private double tau;
	// the currently discovered NN
	private double[] best;
	private ArrayList<double[]> rangeRes;

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTreeByFarest(Collection<double[]> collection, DistanceFunction d) {
		super(collection, d, 100);

		dFunc = d;
		tau = java.lang.Double.MAX_VALUE;
		best = null;

		ArrayList<Item> list = new ArrayList<Item>();
		for (double[] pixels : collection) {
			Item itm = new Item(pixels);
			list.add(itm);
		}

		root = recurse(list, getVP(list, 200));
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

		for (Item itm : list) {
			double dist = dFunc.distance(itm.getPixels(), n.getItem().getPixels());
			itm.push(dist);
		}

		double mu = getMedian(list);
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
		// if (L.size() <= 20) {
		// 	n.items = list;
		// } else {
			n.setLeft(recurse(L, leftVP));
			n.setRight(recurse(R, rightVP));
		// }
		return n;
	}

	/**
	 * Finds the nearest neighbor of double[] q from the double[]s in VP Tree
	 * 
	 * @param query double[] object q
	 * @return nearest neighbor double[] object
	 */
	@Override
	public double[] searchOneNN(double[] q) {
		tau = Double.MAX_VALUE;
		best = q;

		_searchOneNN(root, q);

		return best;
	}

	/**
	 * Helper function to recursively _searchOneNN the VP Tree for the nearest
	 * neighbor of
	 * q
	 * It sets "best" to the nearest neighbor found so far
	 * 
	 * @param VPNode of VP Tree, query double[] object
	 */
	public void _searchOneNN(VPNode n, double[] q) {
		if (n == null)
			return;
		if ((n.getItem().getPixels()).equals(q)) {
			tau = 0.0;
			best = n.getItem().getPixels();
			return;
		}
		// if(n.items != null)
		// {
		// 	for(Item item: n.items)
		// 	{
		// 		double d = dFunc.distance(q, item.getPixels());
		// 		if(d<tau){
		// 			tau = d;
		// 			best = item.getPixels();
		// 		}
		// 	}
		// 	return;
		// }

		double dist = dFunc.distance(q, n.getItem().getPixels());
		searchCount++;
		double mu = n.getMu();

		if (dist < tau) {
			tau = dist;
			best = n.getItem().getPixels();
		}

		// If tau circle lies completely outside mu circle
		if (dist > tau + mu) {
			_searchOneNN(n.getRight(), q);
		}
		// If tau circle lies completely inside mu circle
		else if (dist < Math.max(tau, mu) - Math.min(tau, mu)) {
			_searchOneNN(n.getLeft(), q);
		}
		// If both circles overlap
		else {
			_searchOneNN(n.getLeft(), q);
			_searchOneNN(n.getRight(), q);
		}
	}


}
