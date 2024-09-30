
package VPTree;

import Distance.*;
import evaluation.Settings;

import java.util.ArrayList;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public class VPTreeBySample extends VPTree {

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTreeBySample(double[][] vectors, DistanceFunction d, int sampleNB, int bucketSize) {

		super(vectors, d);
		this.sampleNB = sampleNB;
		root = recurse(list, bucketSize);

	}

	/**
	 * Helper function of the Constructor
	 * Recursively builds the VP Tree
	 * 
	 * @param ArrayList of Item objects
	 * @return Node object which is the root of the VP Tree
	 */
	private VPNode recurse(ArrayList<Item> list, int bucketSize) {
		if (list.isEmpty()) {
			return null;
		}

		// Early stop condition
		if (Settings.isEarlyStopConstruct && list.size() <= bucketSize) {
			// Create a leaf node
			VPNode leafNode = new VPNode(null); // or a node with some dummy item
			leafNode.items = new ArrayList<>(list);
			return leafNode;
		}

		Item vpItem = getVP(list);
		VPNode n = new VPNode(vpItem);

		deleteItem(list, vpItem);

		ArrayList<Double> arr = new ArrayList<Double>();
		for (Item itm : list) {
			double dist = dFunc.distance(itm.getVector(), vpItem.getVector());
			itm.push(dist);
			arr.add(dist);
		}

		double mu = getMedian(arr);
		n.setMu(mu);

		ArrayList<Item> L = new ArrayList<Item>();
		ArrayList<Item> R = new ArrayList<Item>();

		for (Item itm : list) {
			if (itm.tail() < mu)
				L.add(itm);
			else
				R.add(itm);
		}
		// If either side is empty, stop recursion
		if (L.isEmpty() || R.isEmpty()) {
			VPNode leafNode = new VPNode(vpItem);
			list.add(vpItem);
			leafNode.items = new ArrayList<>(list);
			return leafNode;
		}

		n.setLeft(recurse(L, bucketSize));
		n.setRight(recurse(R, bucketSize));
		return n;
	}

}
