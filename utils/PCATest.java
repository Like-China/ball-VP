package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PCATest {

    public static void main(String[] args) {
        int originalDimension = 128; // You can change this dimension
        int numVectors = 1000000; // Number of vectors
        int targetDimension = 20;

        // Generate random vectors with specified original dimension
        List<float[]> vectors = generateRandomVectors(numVectors, originalDimension);

        // Perform PCA to reduce dimensions
        long t1 = System.currentTimeMillis();
        List<float[]> reducedVectors = SimplePCA.reduceDimensions(vectors, targetDimension);
        long t2 = System.currentTimeMillis();
        System.out.println("PCA completed in " + (t2 - t1) + " ms");
        // Compare distances
        System.out.println("\nComparison of relative distances:");
        // for (int i = 0; i < vectors.size(); i++) {
        // for (int j = i + 1; j < vectors.size(); j++) {
        // double originalDistance = calculateDistance(vectors.get(i), vectors.get(j));
        // double reducedDistance = calculateDistance(reducedVectors.get(i),
        // reducedVectors.get(j));
        // System.out.printf("Original vs Reduced for vector %d and vector %d: %.4f vs
        // %.4f%n",
        // i, j, originalDistance, reducedDistance);
        // }
        // }
    }

    private static List<float[]> generateRandomVectors(int numVectors, int dimension) {
        Random rand = new Random();
        List<float[]> vectors = new ArrayList<>();
        for (int i = 0; i < numVectors; i++) {
            float[] vector = new float[dimension];
            for (int j = 0; j < dimension; j++) {
                vector[j] = rand.nextFloat(); // Random float values between 0 and 10
            }
            vectors.add(vector);
        }
        return vectors;
    }

    private static double calculateDistance(float[] vector1, float[] vector2) {
        double sum = 0;
        for (int i = 0; i < vector1.length; i++) {
            sum += Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
