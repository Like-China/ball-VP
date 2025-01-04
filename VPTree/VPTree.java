
package VPTree;

import utils.NN;
import utils.Point;
import java.util.*;

/**
 * Class to represent a VP Tree
 * It inherits the VPTree abstract class
 */
public class VPTree {
	public VPNode root;
	// the number of node access when conduct queries
	public long nodeAccess = 0;
	public long calcCount = 0;
	// All items
	ArrayList<Item> itemList = new ArrayList<Item>();
	public int nodeNB = 0;
	public int layerNB = 0;

	/**
	 * Constructor
	 * 
	 * @param the Point[] points All points within the VP Tree
	 */
	public VPTree(Point[] points) {
		for (Point p : points) {
			Item itm = new Item(p);
			itemList.add(itm);
		}
	}

	/**
	 * Helper function to delete an Item object from the ArrayList
	 * When a item is added as a vp item, it should be deleted then.
	 * 
	 * @param ArrayList of Item objects
	 */
	public boolean deleteItem(ArrayList<Item> itemList, Item itm) {
		for (int i = 0; i < itemList.size(); ++i) {
			if ((itemList.get(i)).equals(itm)) {
				itemList.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Helper function to get the Median distance of an array
	 * 
	 * @param ArrayList distance array
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
	 * Finds the k nearest neighbors of Point q under the distance range maxD
	 * Deep First Search
	 */
	public PriorityQueue<NN> searchkNNDFS(Point q, int k, double maxD) {
		// recusively search
		PriorityQueue<NN> res = new PriorityQueue<>(k, (a, b) -> Double.compare(b.dist2query, a.dist2query));
		_searchkNNDFS(root, q, k, res, maxD);
		assert res != null && !res.isEmpty();
		return res;
	}

	/**
	 * Finds the k nearest neighbors of Point q under the distance range maxD
	 * Best First Search by recursive search
	 */
	public PriorityQueue<NN> searchkNNBFS(Point q, int k, double maxD) {
		PriorityQueue<NN> res = new PriorityQueue<>(k, (a, b) -> Double.compare(b.dist2query, a.dist2query));
		// recursive search
		_searchkNNBFS(root, q, k, res, maxD);
		return res; // Return the k-nearest neighbors
	}

	private void _searchkNNDFS(VPNode n, Point q, int k, PriorityQueue<NN> res, double maxD) {
		if (n == null) {
			return;
		}
		// Handle the case where the node is a leaf node
		if (n.items != null && !n.items.isEmpty()) {
			for (Item i : n.items) {
				Point db = i.getPoint();
				double dist = q.distanceTo(db);
				calcCount++;
				if (res.size() < k) {
					res.add(new NN(db, dist)); // Add the neighbor if we haven't found k yet
				} else {
					// Check if the current point is closer than the farthest neighbor in the queue
					double maxKdist = Math.min(res.peek().dist2query, maxD);
					if (dist <= maxKdist) {
						res.poll(); // Remove the farthest
						res.add(new NN(db, dist)); // Add the closer point
					}
				}
			}
			return;
		}

		// Non-leaf node case (VP node)
		Point db = n.getItem().getPoint();
		double dist = q.distanceTo(db);
		nodeAccess++;
		calcCount++;
		// Add the VP node itself to the result set if needed
		if (res.size() < k) {
			res.add(new NN(db, dist));
		} else {
			double maxKdist = Math.min(res.peek().dist2query, maxD);
			if (dist <= maxKdist) {
				res.poll();
				res.add(new NN(db, dist));
			}
		}

		double mu = n.getMu(); // Median distance (mu) of the current node's subtree
		double maxKdist = Math.min(res.peek().dist2query, maxD); // Distance of the farthest k-th neighbor found so far
		// Determine which subtree(s) to search:
		if (dist - mu <= maxKdist) {
			_searchkNNDFS(n.getLeft(), q, k, res, maxD);
		}
		if (mu - dist <= maxKdist) {
			_searchkNNDFS(n.getRight(), q, k, res, maxD);
		}
	}

	private void _searchkNNBFS(VPNode n, Point q, int k, PriorityQueue<NN> res, double maxD) {
		if (n == null) {
			return;
		}

		// Handle the case where the node is a leaf node
		if (n.items != null && !n.items.isEmpty()) {
			for (Item i : n.items) {
				Point db = i.getPoint();
				double dist = q.distanceTo(db);
				calcCount++;
				if (res.size() < k) {
					res.add(new NN(db, dist)); // Add the neighbor if we haven't found k yet
				} else {
					// Check if the current point is closer than the farthest neighbor in the queue
					double maxKdist = Math.min(res.peek().dist2query, maxD);
					if (dist <= maxKdist) {
						res.poll(); // Remove the farthest
						res.add(new NN(db, dist)); // Add the closer point
					}
				}
			}
			return;
		}

		// Non-leaf node case (VP node)
		Point db = n.getItem().getPoint();
		double dist = q.distanceTo(db);
		nodeAccess++;
		calcCount++;

		// Add the VP node itself to the result set if needed
		if (res.size() < k) {
			res.add(new NN(db, dist));
		} else {
			double maxKdist = Math.min(res.peek().dist2query, maxD);
			if (dist <= maxKdist) {
				res.poll();
				res.add(new NN(db, dist));
			}
		}

		double mu = n.getMu(); // Median distance (mu) of the current node's subtree
		double maxKdist = Math.min(res.peek().dist2query, maxD); // Distance of the farthest k-th neighbor found so far
		if (dist - mu < mu - dist) {
			if (dist - mu <= maxKdist) {
				_searchkNNDFS(n.getLeft(), q, k, res, maxD);
			}
			if (mu - dist <= maxKdist) {
				_searchkNNDFS(n.getRight(), q, k, res, maxD);
			}
		} else {
			if (mu - dist <= maxKdist) {
				_searchkNNDFS(n.getRight(), q, k, res, maxD);
			}
			if (dist - mu <= maxKdist) {
				_searchkNNDFS(n.getLeft(), q, k, res, maxD);
			}
		}

	}

	public void firstOrderVisit(VPNode node) {
		nodeNB += 1;
		if (node.getLeft() != null) {
			firstOrderVisit(node.getLeft());
		}
		if (node.getRight() != null) {
			firstOrderVisit(node.getRight());
		}
	}

	public void getLayerNB(VPNode node) {
		layerNB += 1;
		if (node.getLeft() != null) {
			getLayerNB(node.getLeft());
		}
	}

}
