
package VPTree;

import Distance.*;
import evaluation.Settings;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public class VPTree {
	public VPNode root;
	public DistanceFunction dFunc;
	// the distance of the currently discovered NN
	public double tau;
	// the currently discovered NN
	public double[] nn = null;
	// the currently discovered NN
	public double[][] kNN = null;
	public ArrayList<double[]> rangeRes;
	// the number of node access when conduct queries
	public int searchCount = 0;
	// the size of sampling vectors to select VP points
	public int sampleNB = Settings.sampleNB;

	ArrayList<Item> list = new ArrayList<Item>();

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTree(double[][] vectors, DistanceFunction d) {

		dFunc = d;
		tau = java.lang.Double.MAX_VALUE;
		nn = null;
		for (double[] vector : vectors) {
			Item itm = new Item(vector);
			list.add(itm);
		}

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
		Random rand = new Random();
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
	public double getMedian(ArrayList<Double> arr) {
		int size = arr.size();
		if (size == 0)
			return 0.0;
		Collections.sort(arr);
		if ((size % 2) == 1)
			return arr.get(size / 2);
		else
			return (arr.get(size / 2) + arr.get((size / 2) - 1)) / 2.00;
	}

	/**
	 * Finds the nearest neighbor of double[] q from the double[] s in VP Tree
	 * 
	 * @param query double[] object q
	 * @return nearest neighbor double[] object
	 */
	public double[] searchOneNN(double[] q) {
		tau = Double.MAX_VALUE;

		_searchOneNN(root, q);
		assert nn != null;

		return nn;
	}

	/**
	 * Helper function to recursively _searchOneNN the VP Tree for the nearest
	 * neighbor of
	 * q
	 * It sets "nn" to the nearest neighbor found so far
	 * 
	 * @param VPNode of VP Tree, query double[] object
	 */
	private void _searchOneNN(VPNode n, double[] q) {
		if (n == null) {
			return;
		}

		// Handle the case where the node is a leaf node
		if (n.items != null) {
			for (Item i : n.items) {
				double dist = dFunc.distance(q, i.getVector());
				if (dist < tau) {
					tau = dist;
					nn = i.getVector();
				}
			}
			return; // Return after checking all items in the leaf node
		}

		double dist = dFunc.distance(q, n.getItem().getVector());
		searchCount++;
		double mu = n.getMu();

		// Update the nearest neighbor if current node is closer
		if (dist < tau) {
			tau = dist;
			nn = n.getItem().getVector();
		}

		// Decide which subtrees to search
		if (dist > tau + mu) {
			// Only search the right subtree if the left subtree cannot contain a closer
			// neighbor
			_searchOneNN(n.getRight(), q);
		} else if (dist < tau - mu) {
			// Only search the left subtree if the right subtree cannot contain a closer
			// neighbor
			_searchOneNN(n.getLeft(), q);
		} else {
			// Search both subtrees if there is overlap
			_searchOneNN(n.getLeft(), q);
			_searchOneNN(n.getRight(), q);
		}
	}

	/**
	 * Finds the nearest neighbor of double[] q from the double[] s in VP Tree
	 * 
	 * @param query double[] object q
	 * @return nearest neighbor double[] object
	 */
	public PriorityQueue<NN> searchkNN(double[] q, int k) {
		tau = Double.MAX_VALUE;
		PriorityQueue<NN> res = _searchkNN(root, q, k);
		assert res != null;
		return res;
	}

	/**
	 * Helper function to recursively _searchOneNN the VP Tree for the nearest
	 * neighbor of
	 * q
	 * It sets "nn" to the nearest neighbor found so far
	 * 
	 * @param VPNode of VP Tree, query double[] object
	 */
	private PriorityQueue<NN> _searchkNN(VPNode root, double[] q, int k) {
		if (root == null) {
			return null;
		}

		// Priority queue to store VPNodes to visit
		PriorityQueue<VPNode> nodeCandidate = new PriorityQueue<>(Comp.NNComparator1);

		// Priority queue to store nearest neighbors
		PriorityQueue<NN> res = new PriorityQueue<>(Comp.NNComparator2);

		// Start by adding the root node to the candidate queue
		nodeCandidate.add(root);

		while (!nodeCandidate.isEmpty()) {
			VPNode currentNode = nodeCandidate.poll(); // Get the next candidate node
			double[] vpVector = currentNode.getItem().getVector();
			double dist = dFunc.distance(q, vpVector); // Compute the distance to the query point

			// Add to result queue if we haven't found k neighbors yet
			if (res.size() < k) {
				res.add(new NN(vpVector, dist));
			} else {
				// Check if current node is closer than the farthest neighbor in the result
				// queue
				double maxKdist = res.peek().dist2query;
				if (dist < maxKdist) {
					res.poll(); // Remove the farthest
					res.add(new NN(vpVector, dist));
				}
			}

			double mu = currentNode.getMu(); // Median distance (mu) of the current node's subtree
			double maxKdist = res.peek().dist2query; // Distance of the farthest k-th neighbor found so far

			// Now, determine which subtrees to search:
			// If the query is further than (mu + maxKdist), search the right subtree
			if (dist > maxKdist + mu) {
				if (currentNode.getRight() != null) {
					nodeCandidate.add(currentNode.getRight());
				}
			}
			// If the query is closer than (mu - maxKdist), search the left subtree
			else if (dist < mu - maxKdist) {
				if (currentNode.getLeft() != null) {
					nodeCandidate.add(currentNode.getLeft());
				}
			}
			// Otherwise, both subtrees could contain valid neighbors, so search both
			else {
				if (currentNode.getLeft() != null) {
					nodeCandidate.add(currentNode.getLeft());
				}
				if (currentNode.getRight() != null) {
					nodeCandidate.add(currentNode.getRight());
				}
			}
		}

		return res;
	}

	public ArrayList<double[]> searchRange(double[] q, double range) {
		rangeRes = new ArrayList<>();
		_searchRange(root, q, range);
		return rangeRes;
	}

	private void _searchRange(VPNode n, double[] q, double range) {
		if (n == null)
			return;
		double dist = dFunc.distance(q, n.getItem().getVector());
		double mu = n.getMu();

		if (dist <= range) {
			rangeRes.add(n.getItem().getVector());
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
	 * Helper function to select the Vantage Point using down-sampling
	 * 
	 * @param ArrayList of Item objects
	 * @return Item object containing the Vantage Point
	 */
	public Item getVP(ArrayList<Item> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		ArrayList<Item> p = getSample(list);
		ArrayList<Item> d = getSample(list);
		double best_spread = 0.0;
		Item best = list.get(0);
		for (Item i : p) {
			// all distances from vectors in d to current vp candidate p
			ArrayList<Double> arr = new ArrayList<Double>();
			for (Item j : d) {
				arr.add(dFunc.distance(j.getVector(), i.getVector()));
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
