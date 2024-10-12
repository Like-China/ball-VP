package kGrapgh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import cacheIndex.Point;

public class KNNGraphSearch {

    // Method to build the kNN graph
    public static KNNGraph buildKNNGraph(Point[] points, int k) {
        KNNGraph knnGraph = new KNNGraph();

        // For each point, find the k-nearest neighbors
        for (Point p : points) {
            PriorityQueue<PointDistancePair> pq = new PriorityQueue<>((a, b) -> Double.compare(b.distance, a.distance));

            // Calculate the distance from point p to all other points
            for (Point q : points) {
                if (!p.equals(q)) {
                    double dist = p.distance(q);
                    pq.add(new PointDistancePair(q, dist));

                    // Keep only the closest k neighbors in the priority queue
                    if (pq.size() > k) {
                        pq.poll(); // Remove the farthest point in the queue
                    }
                }
            }

            // Add edges between point p and its k-nearest neighbors
            while (!pq.isEmpty()) {
                knnGraph.addEdge(p, pq.poll().point);
            }
        }

        return knnGraph;
    }

    // Method to find the k-nearest neighbors of a given point using BFS/DFS from a
    // random initial point
    public static List<Point> findKNN(KNNGraph knnGraph, Point targetPoint, int k) {
        PriorityQueue<PointDistancePair> pq = new PriorityQueue<>((a, b) -> Double.compare(b.distance, a.distance));
        Set<Point> visited = new HashSet<>();

        // Start from the target point
        pq.add(new PointDistancePair(targetPoint, 0));
        visited.add(targetPoint);

        while (!pq.isEmpty() && pq.size() < k) {
            PointDistancePair current = pq.poll();
            Point currentPoint = current.point;

            // Explore neighbors of the current point
            for (Point neighbor : knnGraph.getNeighbors(currentPoint)) {
                if (!visited.contains(neighbor)) {
                    double dist = targetPoint.distance(neighbor);
                    pq.add(new PointDistancePair(neighbor, dist));
                    visited.add(neighbor);
                }
            }
        }

        // Collect the k nearest neighbors
        List<Point> result = new ArrayList<>();
        while (!pq.isEmpty() && result.size() < k) {
            result.add(pq.poll().point);
        }

        return result;
    }

    public static void main(String[] args) {
        // Example input
        Point[] points = {
                new Point(1, new double[] { 1.0, 2.0 }),
                new Point(2, new double[] { 2.0, 3.0 }),
                new Point(3, new double[] { 3.0, 4.0 }),
                new Point(4, new double[] { 5.0, 6.0 }),
                new Point(5, new double[] { 7.0, 8.0 }),
        };

        int k = 2; // Find 2 nearest neighbors

        // Build the kNN graph
        KNNGraph knnGraph = buildKNNGraph(points, k);

        // Print the kNN graph
        System.out.println("KNN Graph:");
        knnGraph.printGraph();

        // Find k-NN of a specific point (e.g., point with id 1)
        Point targetPoint = new Point(1, new double[] { 1.0, 2.0 });
        List<Point> neighbors = findKNN(knnGraph, targetPoint, k);

        // Print the k-NN results
        System.out.println("k-NN of Point " + targetPoint.id + ":");
        for (Point neighbor : neighbors) {
            System.out.print(neighbor.id + " ");
        }
        System.out.println();
    }
}
