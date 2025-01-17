package utils;

import java.util.ArrayList;
import java.util.List;

public class SimplePCA {

    public static List<float[]> reduceDimensions(List<float[]> vectors, int d) {
        int n = vectors.size();
        int originalDimension = vectors.get(0).length;

        // Step 1: Compute mean vector
        float[] mean = new float[originalDimension];
        for (float[] vector : vectors) {
            for (int j = 0; j < originalDimension; j++) {
                mean[j] += vector[j];
            }
        }
        for (int j = 0; j < originalDimension; j++) {
            mean[j] /= n;
        }

        // Step 2: Center the data
        float[][] centeredData = new float[n][originalDimension];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < originalDimension; j++) {
                centeredData[i][j] = vectors.get(i)[j] - mean[j];
            }
        }

        // Step 3: Compute covariance matrix
        float[][] covarianceMatrix = new float[originalDimension][originalDimension];
        for (int i = 0; i < originalDimension; i++) {
            for (int j = 0; j < originalDimension; j++) {
                float sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += centeredData[k][i] * centeredData[k][j];
                }
                covarianceMatrix[i][j] = sum / (n - 1);
            }
        }

        // Step 4: Compute eigenvalues and eigenvectors (simplified)
        // In practice, use a proper linear algebra library for this step
        EigenDecompositionResult result = eigenDecompose(covarianceMatrix);

        // Step 5: Select top d eigenvectors
        float[][] topEigenVectors = new float[originalDimension][d];
        for (int i = 0; i < originalDimension; i++) {
            for (int j = 0; j < d; j++) {
                topEigenVectors[i][j] = result.eigenVectors[i][j];
            }
        }

        // Step 6: Project data onto the top d eigenvectors
        List<float[]> reducedData = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            float[] reducedVector = new float[d];
            for (int j = 0; j < d; j++) {
                for (int k = 0; k < originalDimension; k++) {
                    reducedVector[j] += centeredData[i][k] * topEigenVectors[k][j];
                }
            }
            reducedData.add(reducedVector);
        }

        return reducedData;
    }

    // Simplified eigen decomposition for symmetric matrices (e.g., covariance
    // matrix)
    // Placeholder for a more robust method; in practice, use a dedicated library.
    private static EigenDecompositionResult eigenDecompose(float[][] matrix) {
        // For simplicity, we use dummy eigenvalues and eigenvectors here.
        // Replace this with a proper eigen decomposition implementation.
        int dim = matrix.length;
        float[] eigenValues = new float[dim];
        float[][] eigenVectors = new float[dim][dim];
        for (int i = 0; i < dim; i++) {
            eigenValues[i] = 1; // Dummy values
            eigenVectors[i][i] = 1; // Identity matrix
        }
        return new EigenDecompositionResult(eigenValues, eigenVectors);
    }

    private static class EigenDecompositionResult {
        float[] eigenValues;
        float[][] eigenVectors;

        EigenDecompositionResult(float[] eigenValues, float[][] eigenVectors) {
            this.eigenValues = eigenValues;
            this.eigenVectors = eigenVectors;
        }
    }
}
