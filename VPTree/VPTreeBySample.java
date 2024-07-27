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
public class VPTreeBySample {
	private VPNode root;
	private DistanceFunction dFunc;
	// the distance of the currently discovered NN
	private double tau;
	// the currently discovered NN
	private double[] best;
	private ArrayList<double[]> rangeRes;
	// the number of node access when conduct queries
	public int searchCount = 0;

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTreeBySample(Collection<double[]> collection, DistanceFunction d, int sampleNB) {

		dFunc = d;
		tau = java.lang.Double.MAX_VALUE;
		best = null;

		ArrayList<Item> list = new ArrayList<Item>();
		for (double[] pixels : collection) {
			Item itm = new Item(pixels);
			list.add(itm);
		}

		root = recurse(list, sampleNB);
	}

	/**
	 * Helper function of the Constructor
	 * Recursively builds the VP Tree
	 * 
	 * @param ArrayList of Item objects
	 * @return Node object which is the root of the VP Tree
	 */
	private VPNode recurse(ArrayList<Item> list, int sampleNB) {
		if (list.size() == 0)
			return null;
		VPNode n = new VPNode(getVP(list, sampleNB));
		deleteItem(list, n.getItem());

		for (Item itm : list) {
			double dist = dFunc.distance(itm.getPixels(), n.getItem().getPixels());
			itm.push(dist);
		}

		double mu = getMedian(list);
		n.setMu(mu);

		ArrayList<Item> L = new ArrayList<Item>();
		ArrayList<Item> R = new ArrayList<Item>();

		for (Item itm : list) {
			if (itm.tail() < mu)
				L.add(itm);
			else
				R.add(itm);
		}

		n.setLeft(recurse(L, sampleNB));
		n.setRight(recurse(R, sampleNB));
		return n;
	}

	/**
	 * Helper function to select the Vantage Point
	 * 
	 * @param ArrayList of Item objects
	 * @return Item object containing the Vantage Point
	 */
	public Item getVP(ArrayList<Item> list, int sampleNB) {
		if (list.size() == 1)
			return list.get(0);

		int size = sampleNB;
		if (list.size() < sampleNB)
			size = list.size();
		ArrayList<Item> p;
		if (size != list.size()) {
			p = getSample(list, size);
		} else {
			p = list;
		}
		double best_spread = 0.0;
		Item best = list.get(0);
		for (Item i : p) {
			ArrayList<Item> d;
			if (size != list.size()) {
				d = getSample(list, size);
			} else {
				d = list;
			}

			ArrayList<Double> arr = new ArrayList<Double>();
			for (Item j : d) {
				arr.add(dFunc.distance(j.getPixels(), i.getPixels()));
			}

			Collections.sort(arr);

			size = arr.size();
			double mu = 0.0;
			if ((size % 2) == 1)
				mu = arr.get(size / 2);
			else
				mu = (arr.get(size / 2) + arr.get((size / 2) - 1)) / 2.00;

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

	/**
	 * Helper function to get a random sample of data points
	 * This is used by getVP() method to select the Vantage Point
	 * 
	 * @param ArrayList of Item objects and size of sample
	 * @return ArrayList of Item objects representing the sample
	 */
	public ArrayList<Item> getSample(ArrayList<Item> list, int size) {
		if (list.size() <= size)
			return list;

		Random rand = new Random();
		ArrayList<Item> ans = new ArrayList<Item>();
		HashSet<Integer> set = new HashSet<Integer>();

		while (ans.size() < size) {
			int i = rand.nextInt(list.size());
			if (set.contains(i))
				continue;
			set.add(i);
			ans.add(list.get(i));
		}
		return ans;
	}

	/**
	 * Helper function to delete an Item object from the ArrayList
	 * 
	 * @param ArrayList of Item objects
	 */
	public void deleteItem(ArrayList<Item> list, Item itm) {
		for (int i = 0; i < list.size(); ++i) {
			if ((list.get(i)).equals(itm)) {
				list.remove(i);
				return;
			}
		}
		return;
	}

	/**
	 * Helper function to get the Median distance of the data points from vantage
	 * point
	 * 
	 * @param ArrayList of Item objects
	 * @return median as a double
	 */
	public double getMedian(ArrayList<Item> list) {
		if (list.size() == 0)
			return 0.0;

		ArrayList<Double> arr = new ArrayList<Double>();
		for (Item i : list) {
			arr.add(i.tail());
		}

		Collections.sort(arr);
		int size = arr.size();

		if ((size % 2) == 1)
			return arr.get(size / 2);
		else
			return (arr.get(size / 2) + arr.get((size / 2) - 1)) / 2.00;
	}

	/**
	 * Finds the nearest neighbor of double[] q from the double[]s in VP Tree
	 * 
	 * @param query double[] object q
	 * @return nearest neighbor double[] object
	 */
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
	private void _searchOneNN(VPNode n, double[] q) {
		if (n == null)
			return;
		if ((n.getItem().getPixels()).equals(q)) {
			tau = 0.0;
			best = n.getItem().getPixels();
			return;
		}

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

	public ArrayList<double[]> searchRange(double[] q, double range) {
		rangeRes = new ArrayList<>();
		_searchRange(root, q, range);
		return rangeRes;
	}

	private void _searchRange(VPNode n, double[] q, double range) {
		if (n == null)
			return;
		double dist = dFunc.distance(q, n.getItem().getPixels());
		double mu = n.getMu();

		if (dist <= range) {
			rangeRes.add(n.getItem().getPixels());
		}

		// If tau circle lies completely outside mu circle
		if (dist > range + mu) {
			_searchRange(n.getRight(), q, range);
		}
		// If tau circle lies completely inside mu circle
		// else if (dist < Math.max(range, mu) - Math.min(range, mu)) {
		else if (dist < mu - range) {
			_searchRange(n.getLeft(), q, range);
		}
		// If both circles overlap
		else {
			_searchRange(n.getLeft(), q, range);
			_searchRange(n.getRight(), q, range);
		}
	}

	/**
	 * @return the DistanceFunction metric used in building this VP Tree
	 */
	public DistanceFunction getDistanceFunc() {
		return dFunc;
	}
}
