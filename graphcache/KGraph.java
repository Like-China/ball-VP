package graphcache;

import java.util.*;

import utils.NN;
import utils.Point;

// Class to represent the kNN graph
public class KGraph {
    public HashMap<Point, ArrayList<Point>> adjacencyList; // Graph stored as an adjacency list
    public HashSet<Point> points;

    public KGraph() {
        adjacencyList = new HashMap<>();
        points = new HashSet<>();
    }

    // Add a connection between two points
    public void addEdge(Point from, Point to) {
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }

    public void removePoint(Point p) {
        if (points.contains(p)) {
            points.remove(p);
        } else {
            return;
        }
        // Step 1: Remove the point p from the adjacency list (removes all its neighbors
        adjacencyList.remove(p);
        // Step 2: Remove point p from the neighbors of all other points
        for (Map.Entry<Point, ArrayList<Point>> entry : adjacencyList.entrySet()) {
            ArrayList<Point> neighbors = entry.getValue();
            neighbors.remove(p); // Remove point p from each neighbor list
        }
    }

    // Get the neighbors of a point
    public ArrayList<Point> getNeighbors(Point p) {
        return adjacencyList.getOrDefault(p, new ArrayList<>());
    }

    // Print the graph
    public void printGraph() {
        for (Map.Entry<Point, ArrayList<Point>> entry : adjacencyList.entrySet()) {
            System.out.print("Point " + entry.getKey().id + " -> ");
            for (Point neighbor : entry.getValue()) {
                System.out.print(neighbor.id + " ");
            }
            System.out.println();
        }
    }

    // use a set of points to initial a Kgraph
    public void initGraph(ArrayList<Point> initPoints, int k) {
        // initial all records
        adjacencyList = new HashMap<>();
        points = new HashSet<>();

        points.addAll(initPoints);
        for (Point p : initPoints) {
            this.updateGraph(p, k);
        }

    }

    // Given a new point, update the kNN graph by adding this point and its
    // k-connection
    public void updateGraph(Point p, int k) {
        if (adjacencyList.containsKey(p)) {
            return;
        }
        // For each point, find the k-nearest neighbors
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        // Calculate the distance from point p to all other points
        for (Point q : points) {
            if (!p.equals(q)) {
                double dist = p.distanceTo(q);
                pq.add(new NN(q, dist));
                // Keep only the closest k neighbors in the priority queue
                if (pq.size() > k) {
                    pq.poll(); // Remove the farthest point in the queue
                }
            }
        }
        // Add edges between point p and its k-nearest neighbors
        while (!pq.isEmpty()) {
            this.addEdge(p, pq.poll().point);
        }
    }

    // Given an inital point
    // Method to find the k-nearest neighbors of a given point using a greedy search
    // from a given inital point
    public PriorityQueue<NN> findKNN(Point currentPoint, Point targetPoint, int k) {
        PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        Set<Point> visited = new HashSet<>(); // To avoid visiting the same point twice

        // Step 1: Start from a random point in the graph
        Random r = new Random(10);
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
            if (minDist < currentDistance || r.nextDouble() < 0.8) {
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

    // public static void main(String[] args) {
    // // Example input
    // Random r = new Random(10);
    // int k = 10; // Find k nearest neighbors
    // ArrayList<Point> points = new ArrayList<>();
    // for (int i = 0; i < 50; i++) {
    // points.add(new Point(i, new double[] { r.nextDouble(), r.nextDouble() }));
    // }
    // // Build the kNN graph
    // KGraph knnGraph = new KGraph();
    // knnGraph.initGraph(points, k);
    // for (int i = 50; i < 1000; i++) {
    // Point p = new Point(i, new double[] { r.nextDouble(), r.nextDouble() });
    // points.add(p);
    // knnGraph.points.add(p);
    // knnGraph.updateGraph(p, k);
    // }

    // // Print the kNN graph
    // // System.out.println("KNN Graph:");
    // // knnGraph.printGraph();
    // // knnGraph.removePoint(points.get(44));
    // // System.out.println("KNN Graph:");
    // // knnGraph.printGraph();

    // // Find k-NN of a specific point (e.g., point with id 1)
    // Point targetPoint = new Point(8, new double[] { r.nextDouble(),
    // r.nextDouble() });
    // PriorityQueue<NN> neighbors = knnGraph.findKNN(targetPoint,
    // targetPoint, k);
    // // Print the k-NN results
    // System.out.println("k-NN (Graph) of Point " + targetPoint.id + ":");
    // while (!neighbors.isEmpty()) {
    // NN pair = neighbors.poll();
    // System.out.println(pair.point.id + "/" + pair.distance);
    // }

    // // brute-force
    // PriorityQueue<NN> pq = new PriorityQueue<>((a, b) ->
    // Double.compare(b.distance, a.distance));
    // for (Point neighbor : points) {
    // double dist = targetPoint.distance(neighbor);
    // pq.add(new NN(neighbor, dist));
    // if (pq.size() > k) {
    // pq.poll();
    // }
    // }
    // System.out.println("k-NN (Exact) of Point " + targetPoint.id + ":");
    // while (!pq.isEmpty()) {
    // NN pair = pq.poll();
    // System.out.println(pair.point.id + "/" + pair.distance);
    // }

    // }
}