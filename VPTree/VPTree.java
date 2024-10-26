
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
	// the vector list
	ArrayList<Item> list = new ArrayList<Item>();
	public int nodeNB = 0;
	public int layerNB = 0;

	/**
	 * Constructor
	 * 
	 * @param the train dataset of double[]s, Distance metric d to be used
	 */
	public VPTree(Point[] points) {
		for (Point p : points) {
			Item itm = new Item(p);
			list.add(itm);
		}
	}

	public void init() {
		nodeAccess = 0;
		calcCount = 0;
	}

	/**
	 * Helper function to delete an Item object from the ArrayList
	 * 
	 * @param ArrayList of Item objects
	 */
	public boolean deleteItem(ArrayList<Item> list, Item itm) {
		for (int i = 0; i < list.size(); ++i) {
			if ((list.get(i)).equals(itm)) {
				list.remove(i);
				return true;
			}
		}
		return false;
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
	 * Finds the nearest neighbor of Point q from the double[] s in VP Tree
	 * 
	 * @param query double[] object q
	 * @return nearest neighbor double[] object
	 */
	public PriorityQueue<NN> searchkNNDFS(Point q, int k, double maxD) {
		// recusively search
		PriorityQueue<NN> res = new PriorityQueue<>(k, (a, b) -> Double.compare(b.dist2query, a.dist2query));
		_searchkNNDFS(root, q, k, res, maxD);
		assert res != null && !res.isEmpty();
		return res;
	}

	public PriorityQueue<NN> searchkNNBFSHier(Point q, int k, double maxD) {
		// hirec serach using a linkedlist
		PriorityQueue<NN> res = _searchkNNBFS(root, q, k);
		assert res != null;
		return res;
	}

	// Helper function to initialize and start the recursive search
	public PriorityQueue<NN> searchkNNBFSRecu(Point q, int k, double maxD) {
		PriorityQueue<NN> res = new PriorityQueue<>(k, (a, b) -> Double.compare(b.dist2query, a.dist2query));
		// recursive search
		_searchkNNBestFirst(root, q, k, res, maxD);
		return res; // Return the k-nearest neighbors
	}

	/**
	 * Hier Search
	 * 
	 * @param VPNode of VP Tree, query double[] object
	 */
	private PriorityQueue<NN> _searchkNNBFS(VPNode root, Point q, int k) {
		if (root == null) {
			return null;
		}

		// Priority queue to store VPNodes to visit
		LinkedList<VPNode> nodeCandidate = new LinkedList<>();
		// Priority queue to store nearest neighbors
		PriorityQueue<NN> res = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));

		// Start by adding the root node to the candidate queue
		nodeCandidate.offer(root);

		while (!nodeCandidate.isEmpty()) {
			VPNode currentNode = nodeCandidate.poll(); // Get the next candidate node

			// Handle the case where the node is a leaf node
			if (currentNode.items != null) {
				for (Item i : currentNode.items) {
					Point db = i.getPoint();
					double dist = q.distanceTo(db);
					calcCount++;
					if (res.size() < k) {
						res.add(new NN(db, dist));
					} else {
						// Check if current node is closer than the farthest neighbor in the result
						// queue
						double maxKdist = res.peek().dist2query;
						if (dist <= maxKdist) {
							res.poll(); // Remove the farthest
							res.add(new NN(db, dist));
						}
					}
				}
				continue;
			}

			// Handle the case where the node is a non-leaf node
			Point db = currentNode.getItem().getPoint();
			double dist = q.distanceTo(db); // Compute the distance to the query point
			nodeAccess++;
			calcCount++;
			// Add to result queue if we haven't found k neighbors yet
			if (res.size() < k) {
				res.add(new NN(db, dist));
			} else {
				// Check if current node is closer than the farthest neighbor in the result
				// queue
				double maxKdist = res.peek().dist2query;
				if (dist <= maxKdist) {
					res.poll(); // Remove the farthest
					res.add(new NN(db, dist));
				}
			}
			double mu = currentNode.getMu(); // Median distance (mu) of the current node's subtree
			double maxKdist = res.peek().dist2query; // Distance of the farthest k-th neighbor found so far
			// Now, determine which subtrees to search:
			// If the query is further than (mu + maxKdist), search the right subtree
			// Prune subtrees based on the current max distance
			if (dist - mu <= maxKdist && currentNode.getLeft() != null) {
				nodeCandidate.offer(currentNode.getLeft());
			}
			if (mu - dist <= maxKdist && currentNode.getRight() != null) {
				nodeCandidate.offer(currentNode.getRight());
			}
		}

		return res;
	}

	// search kNN recusively using DFS
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
				res.poll(); // Remove the farthest
				res.add(new NN(db, dist)); // Add the closer VP point
			}
		}

		double mu = n.getMu(); // Median distance (mu) of the current node's subtree
		double maxKdist = Math.min(res.peek().dist2query, maxD); // Distance of the farthest k-th neighbor found so far

		// Determine which subtree(s) to search:
		// If query point is closer than mu - maxKdist, search the left subtree first
		// if (dist < mu - maxKdist) {
		// _searchkNNDFS(n.getLeft(), q, k, res, maxD);
		// }
		// // If query point is farther than mu + maxKdist, search the right subtree
		// first
		// else if (dist > mu + maxKdist) {
		// _searchkNNDFS(n.getRight(), q, k, res, maxD);
		// }
		// // Otherwise, both subtrees may contain valid neighbors, so search both
		// else {
		// _searchkNNDFS(n.getLeft(), q, k, res, maxD);
		// _searchkNNDFS(n.getRight(), q, k, res, maxD);
		// }
		if (dist - mu <= maxKdist) {
			_searchkNNDFS(n.getLeft(), q, k, res, maxD);
		}
		if (mu - dist <= maxKdist) {
			_searchkNNDFS(n.getRight(), q, k, res, maxD);
		}

	}

	// search kNN recusively using Best-First
	private void _searchkNNBestFirst(VPNode n, Point q, int k, PriorityQueue<NN> res, double maxD) {
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
				res.poll(); // Remove the farthest
				res.add(new NN(db, dist)); // Add the closer VP point
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
