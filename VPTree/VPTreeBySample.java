
package VPTree;

import Distance.*;
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
	public VPTreeBySample(double[][] vectors, DistanceFunction d, int sampleNB) {

		super(vectors, d);
		this.sampleNB = sampleNB;
		root = recurse(list);

	}

	/**
	 * Helper function of the Constructor
	 * Recursively builds the VP Tree
	 * 
	 * @param ArrayList of Item objects
	 * @return Node object which is the root of the VP Tree
	 */
	private VPNode recurse(ArrayList<Item> list) {
		if (list.isEmpty()) {
			return null;
		}

		// Early stop condition
		// if (list.size() <= 5) {
		// // Create a leaf node
		// VPNode leafNode = new VPNode(null); // or a node with some dummy item
		// leafNode.items = new ArrayList<>(list);
		// return leafNode;
		// }

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
		n.setLeft(recurse(L));
		n.setRight(recurse(R));
		return n;
	}

}
