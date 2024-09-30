package cacheIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {

    // Function to run k-means clustering
    public static List<List<Point>> kMeansCluster(List<Point> points, int k, int maxIterations) {
        // Step 1: Initialize centroids randomly
        List<Point> centroids = initializeCentroids(points, k);
        List<List<Point>> clusters = new ArrayList<>();

        for (int iter = 0; iter < maxIterations; iter++) {
            // Step 2: Assign points to nearest centroid
            clusters = assignPointsToClusters(points, centroids);

            // Step 3: Recalculate centroids based on clusters
            List<Point> newCentroids = calculateNewCentroids(clusters);

            // If centroids don't change, we have converged
            if (centroids.equals(newCentroids)) {
                break;
            }

            centroids = newCentroids; // update centroids for next iteration
        }

        return clusters;
    }

    // Initialize k random centroids
    private static List<Point> initializeCentroids(List<Point> points, int k) {
        Random random = new Random();
        List<Point> centroids = new ArrayList<>();
        List<Point> copyPoints = new ArrayList<>(points);

        for (int i = 0; i < k; i++) {
            int randomIndex = random.nextInt(copyPoints.size());
            centroids.add(copyPoints.get(randomIndex));
            copyPoints.remove(randomIndex); // avoid duplicate centroids
        }

        return centroids;
    }

    // Assign points to the nearest centroid to form clusters
    private static List<List<Point>> assignPointsToClusters(List<Point> points, List<Point> centroids) {
        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>()); // create empty clusters
        }

        for (Point point : points) {
            int nearestCentroidIdx = 0;
            double minDistance = point.distance(centroids.get(0));

            for (int i = 1; i < centroids.size(); i++) {
                double distance = point.distance(centroids.get(i));
                if (distance < minDistance) {
                    nearestCentroidIdx = i;
                    minDistance = distance;
                }
            }

            clusters.get(nearestCentroidIdx).add(point); // assign to nearest centroid's cluster
        }

        return clusters;
    }

    // Calculate new centroids based on the average of points in each cluster
    private static List<Point> calculateNewCentroids(List<List<Point>> clusters) {
        List<Point> newCentroids = new ArrayList<>();

        for (List<Point> cluster : clusters) {
            if (cluster.isEmpty())
                continue; // avoid empty clusters

            int dimension = cluster.get(0).coordinates.length;
            double[] newCoordinates = new double[dimension];

            for (Point point : cluster) {
                for (int i = 0; i < dimension; i++) {
                    newCoordinates[i] += point.coordinates[i];
                }
            }

            for (int i = 0; i < dimension; i++) {
                newCoordinates[i] /= cluster.size(); // calculate average
            }

            Point newCentroid = new Point(-1, newCoordinates); // -1 for id as it's a centroid, not an actual point
            newCentroids.add(newCentroid);
        }

        return newCentroids;
    }

    public static void main(String[] args) {
        // Example usage
        List<Point> points = new ArrayList<>();
        points.add(new Point(1, new double[] { 1.0, 2.0 }));
        points.add(new Point(2, new double[] { 2.0, 3.0 }));
        points.add(new Point(3, new double[] { 3.0, 3.0 }));
        points.add(new Point(4, new double[] { 5.0, 7.0 }));
        points.add(new Point(5, new double[] { 6.0, 8.0 }));
        points.add(new Point(6, new double[] { 8.0, 8.0 }));

        int k = 2; // number of clusters
        int maxIterations = 100;

        List<List<Point>> clusters = kMeansCluster(points, k, maxIterations);

        // Output the clusters
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ":");
            for (Point p : clusters.get(i)) {
                System.out.println("Point ID: " + p.id + ", Coordinates: " + java.util.Arrays.toString(p.coordinates));
            }
        }
    }
}
