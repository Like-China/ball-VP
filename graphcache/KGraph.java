package graphcache;

import java.util.*;

import utils.NN;
import utils.Point;

// Class to represent the kNN graph
public class KGraph {
    public HashMap<Point, ArrayList<Point>> adjacencyList; // Graph stored as an adjacency list
    public ArrayList<Point> points;
    // the distance
    public double findCalcCount = 0;
    public double updateCalcCount = 0;

    public KGraph() {
        adjacencyList = new HashMap<>();
        points = new ArrayList<>();
    }

    public void removePoint(Point deleteP, Point incomingP) {
        if (points.contains(deleteP)) {
            points.remove(deleteP);
        } else {
            return;
        }
        // Step 1: Remove the point p from the adjacency list (removes all its neighbors
        adjacencyList.remove(deleteP);
        // Step 2: Remove point p from the neighbors of all other points
        for (Map.Entry<Point, ArrayList<Point>> entry : adjacencyList.entrySet()) {
            ArrayList<Point> neighbors = entry.getValue();
            // Remove point p from each neighbor list
            if (neighbors.remove(deleteP)) {
                neighbors.add(incomingP);
            }
        }
    }

    // Get the neighbors of a point
    public ArrayList<Point> getNeighbors(Point p) {
        return adjacencyList.getOrDefault(p, new ArrayList<>());
    }

    // use a set of points to initial a Kgraph
    public void initGraph(ArrayList<Point> initPoints, int k) {
        // initial all records
        adjacencyList = new HashMap<>();
        points = new ArrayList<>();
        for (Point p : initPoints) {
            this.addPoint(p, k);
        }
    }

    // Given a new point, update the kNN graph by adding it & its k-connection
    public void addPoint(Point p, int K) {
        if (!points.contains(p)) {
            points.add(p);
        }
        adjacencyList.put(p, new ArrayList<>());
        // For each point, find the k-nearest neighbors
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        // Calculate the distance from point p to all other points
        for (Point q : points) {
            if (!p.equals(q)) {
                double dist = p.distanceTo(q);
                updateCalcCount += 1;
                pq.add(new NN(q, dist));
                // Keep only the closest k neighbors in the priority queue
                if (pq.size() > K) {
                    pq.poll(); // Remove the farthest point in the queue
                }
            }
        }
        // Add edges between point p and its k-nearest neighbors
        while (!pq.isEmpty()) {
            Point from = p;
            Point to = pq.poll().point;
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }
    }

    /*
     * Given an inital point, from a given inital point
     * Method to find the k-nearest neighbors of a given point using a greedy search
     */
    public PriorityQueue<NN> findKNN(Point currentPoint, Point targetPoint, int k) {
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        if (points.isEmpty()) {
            return pq;
        }
        Set<Point> visited = new HashSet<>(); // To avoid visiting the same point twice

        // Step 1: Start from a random point in the graph
        Random r = new Random(10);
        if (currentPoint == null) {
            currentPoint = points.get(0);
        }
        double currentDistance = currentPoint.distanceTo(targetPoint);
        pq.add(new NN(currentPoint, currentDistance));
        visited.add(currentPoint);

        if (this.getNeighbors(currentPoint).isEmpty()) {
            return pq;
        }

        // assert this.getNeighbors(currentPoint).size() == k :
        // this.getNeighbors(currentPoint).size() + "/" + k;
        // Step 2: Greedily search neighbors for closer points
        boolean foundCloserNeighbor;
        do {
            foundCloserNeighbor = false;
            // System.out.println("\n* cur point: " + currentPoint.id + " dis:" +
            // currentDistance);
            // Get neighbors of the current point
            double minDist = Double.MAX_VALUE;
            Point minP = null;
            for (Point neighbor : this.getNeighbors(currentPoint)) {

                if (!visited.contains(neighbor)) {
                    double neighborDistance = neighbor.distanceTo(targetPoint);
                    findCalcCount += 1;
                    visited.add(neighbor);
                    // Add to priority queue if we find a closer neighbor
                    // System.out.println("check point: " + neighbor.id + " distance: " +
                    // neighborDistance);
                    if (pq.size() < k || pq.peek().dist2query > neighborDistance) {
                        pq.add(new NN(neighbor, neighborDistance));
                        if (pq.size() > k) {
                            pq.poll(); // Remove the farthest neighbor if we have more than k
                        }
                        // get the nearest next neigbor to the target point
                        if (minDist > neighborDistance) {
                            // System.out.println(minDist + "/" + neighborDistance);
                            minP = neighbor;
                            minDist = neighborDistance;
                        }
                    }
                }
                // else {
                // System.out.println("checked point: " + neighbor.id);
                // }
            }
            // If next neigbor to the target point has closer distance than current neigbor,
            // Update the current point to explore from the closest neighbor
            if (minDist < currentDistance || r.nextDouble() < 0.4) {
                currentPoint = minP;
                currentDistance = minDist;
                foundCloserNeighbor = true;
            }
            if (minP == null) {
                foundCloserNeighbor = false;
            }
        } while (foundCloserNeighbor);
        // if(pq.size() != k) {
        // System.out.println(pq.size() + "/" + k);
        // }
        return pq;
    }

    public int size() {
        return points.size();
    }

    // Print the graph
    public void printGraph() {
        assert this.size() == adjacencyList.size();
        // for (Map.Entry<Point, ArrayList<Point>> entry : adjacencyList.entrySet()) {
        // System.out.print("Point " + entry.getKey().id + " -> ");
        // System.out.println("The number of neighbors: " + entry.getValue().size());
        // }
        for (Point p : points) {
            System.out.print("Point " + p.id + " -> ");
            System.out.print("The number of neighbors: " + adjacencyList.get(p).size() + "\t");
            for (Point pp : adjacencyList.get(p)) {
                System.out.print(pp);
            }
            System.out.println();
        }
        System.out.println();
    }

}