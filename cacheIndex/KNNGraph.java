package cacheIndex;

import java.util.*;

// Class to represent the kNN graph
public class KNNGraph {
    Map<Point, List<Point>> adjacencyList; // Graph stored as an adjacency list

    public KNNGraph() {
        adjacencyList = new HashMap<>();
    }

    // Add a connection between two points
    public void addEdge(Point from, Point to) {
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }

    // Get the neighbors of a point
    public List<Point> getNeighbors(Point p) {
        return adjacencyList.getOrDefault(p, new ArrayList<>());
    }

    // Print the graph
    public void printGraph() {
        for (Map.Entry<Point, List<Point>> entry : adjacencyList.entrySet()) {
            System.out.print("Point " + entry.getKey().id + " -> ");
            for (Point neighbor : entry.getValue()) {
                System.out.print(neighbor.id + " ");
            }
            System.out.println();
        }
    }
}
