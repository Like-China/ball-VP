
package VPTree;

import evaluation.Settings;
import utils.Point;
import java.util.*;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public class VPTreeBySample extends VPTree {

	// the size of sampling vectors to select VP points
	public int sampleNB = 0;

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTreeBySample(Point[] vectors, int sampleNB, int bucketSize) {
		super(vectors);
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
			System.out.println(1111);
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
		deleteItem(list, vpItem);
		VPNode n = new VPNode(vpItem);

		ArrayList<Double> arr = new ArrayList<Double>();
		Point vpPoint = vpItem.getPoint();
		for (Item itm : list) {
			double dist = itm.getPoint().distanceTo(vpPoint);
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
		// if (L.isEmpty() || R.isEmpty()) {
		// System.out.println(list.size() + "/" + L.size() + "/" + R.size());
		// VPNode leafNode = new VPNode(vpItem);
		// list.add(vpItem);
		// leafNode.items = new ArrayList<>(list);
		// return leafNode;
		// }
		if (!L.isEmpty()) {
			n.setLeft(recurse(L, bucketSize));
		}
		if (!R.isEmpty()) {
			n.setRight(recurse(R, bucketSize));
		}

		return n;
	}

	/**
	 * Helper function to get a random sample of data points
	 * This is used by getVP() method to select the Vantage Point
	 * 
	 * @param ArrayList of Item objects and size of sample
	 * @return ArrayList of Item objects representing the sample
	 */
	public ArrayList<Item> getSample(ArrayList<Item> list) {
		if (list.size() <= sampleNB)
			return list;
		Random rand = new Random(0);
		ArrayList<Item> ans = new ArrayList<Item>();
		HashSet<Integer> set = new HashSet<Integer>();
		while (ans.size() < sampleNB) {
			int i = rand.nextInt(list.size());
			if (set.contains(i))
				continue;
			set.add(i);
			ans.add(list.get(i));
		}
		return ans;
	}

	/**
	 * Helper function to select the Vantage Point using down-sampling
	 * 
	 * @param ArrayList of Item objects
	 * @return Item object containing the Vantage Point
	 */
	public Item getVP(ArrayList<Item> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		ArrayList<Item> samplesA = getSample(list);
		ArrayList<Item> samplesB = getSample(list);
		double best_spread = 0.0;
		Item best = list.get(0);
		for (Item i : samplesA) {
			// all distances from vectors in d to current vp candidate p
			ArrayList<Double> arr = new ArrayList<Double>();
			Point pointA = i.getPoint();
			for (Item j : samplesB) {
				Point pointB = j.getPoint();
				arr.add(pointA.distanceTo(pointB));
			}
			double mu = getMedian(arr);
			double spread = 0.0;
			for (double f : arr) {
				spread += ((f - mu) * (f - mu));
			}

			if (spread > best_spread) {
				best_spread = spread;
				best = i;
			}
		}

		return best;
	}

}
