package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeans {

    // Function to run k-means clustering
    public static List<List<Point>> kMeansCluster(Point[] points, int k, int maxIterations) {
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
    private static List<Point> initializeCentroids(Point[] points, int k) {
        Random random = new Random(0);
        List<Point> centroids = new ArrayList<>();
        List<Point> copyPoints = new ArrayList<>();
        for (Point point : points) {
            copyPoints.add(point);
        }

        for (int i = 0; i < k; i++) {
            int randomIndex = random.nextInt(copyPoints.size());
            centroids.add(copyPoints.get(randomIndex));
            copyPoints.remove(randomIndex); // avoid duplicate centroids
        }

        return centroids;
    }

    // Assign points to the nearest centroid to form clusters
    private static List<List<Point>> assignPointsToClusters(Point[] points, List<Point> centroids) {
        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ArrayList<>()); // create empty clusters
        }

        for (Point point : points) {
            int nearestCentroidIdx = 0;
            double minDistance = point.distanceTo(centroids.get(0));

            for (int i = 1; i < centroids.size(); i++) {
                double distance = point.distanceTo(centroids.get(i));
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

            int dimension = cluster.get(0).vector.length;
            float[] newvector = new float[dimension];

            for (Point point : cluster) {
                for (int i = 0; i < dimension; i++) {
                    newvector[i] += point.vector[i];
                }
            }

            for (int i = 0; i < dimension; i++) {
                newvector[i] /= cluster.size(); // calculate average
            }

            Point newCentroid = new Point(-1, newvector, true); // -1 for id as it's a centroid, not an actual point
            newCentroids.add(newCentroid);
        }

        return newCentroids;
    }

}
