package utils;

import java.io.*;
import java.util.*;

import evaluation.Settings;

public class Loader {

    public Point[] query, db;
    String dataName;
    String filePath;

    public Loader(String dataName) throws IOException {
        this.filePath = "/data/home/like/";
        this.dataName = dataName;
    }

    public List<float[]> loadFvecs(String filePath, int readSize) throws IOException {
        List<float[]> vectors = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
                DataInputStream dis = new DataInputStream(new BufferedInputStream(fis))) {
            while (dis.available() > 0 && vectors.size() < readSize) {
                // Read dimension (int = 4 bytes)
                int dimension = Integer.reverseBytes(dis.readInt());
                // Read float values
                float[] vector = new float[dimension];
                for (int i = 0; i < dimension; i++) {
                    vector[i] = Float.intBitsToFloat(Integer.reverseBytes(dis.readInt()));
                }
                vectors.add(vector);
            }
        }
        return vectors;
    }

    public List<float[]> loadBvecs(String filePath, int readSize) throws IOException {
        List<float[]> vectors = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            while (dis.available() > 0 && vectors.size() < readSize) {
                int dim = Integer.reverseBytes(dis.readInt());
                float[] vector = new float[dim];
                // read each byte as unsigned int
                for (int i = 0; i < dim; i++) {
                    vector[i] = dis.readUnsignedByte();
                }
                vectors.add(vector);
                // show the progress
                if (vectors.size() % 500000 == 0) {
                    System.out.println("Loaded vectors:  " + vectors.size() + "/" + readSize);
                }
            }
        }
        return vectors;
    }

    public void loadData(int qNB, int dbNB, int dim) throws IOException {
        List<float[]> tempVectors = new ArrayList<>();
        switch (dataName) {
            case "random":
                Random r = new Random(0);
                for (int id = 0; id < qNB + dbNB; id++) {
                    float[] vector = new float[dim];
                    for (int j = 0; j < dim; j++) {
                        vector[j] = r.nextFloat();
                    }
                    tempVectors.add(vector);
                }
                break;
            case "SIFT":
                filePath = filePath + "sift/sift_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "DEEP":
                filePath = filePath + "deep1M/deep1M_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "BIGANN":
                filePath = filePath + "bigann/bigann_base.bvecs";
                tempVectors = loadBvecs(filePath, qNB + dbNB);
                break;
            default:
                throw new IllegalArgumentException("Unknown datasets: " + dataName);
        }

        // get vectors with a specifized dimension
        List<float[]> allVectors = new ArrayList<>();
        for (float[] vector : tempVectors) {
            allVectors.add(Arrays.copyOf(vector, dim));
        }
        if (Settings.isShuffle) {
            Collections.shuffle(allVectors);
        }
        assert allVectors.get(0).length == dim : "Dimensional is not aligned!";
        assert allVectors.size() == qNB + dbNB : "Data is limited!";

        query = new Point[qNB];
        db = new Point[dbNB];
        for (int i = 0; i < qNB; i++) {
            query[i] = new Point(i, allVectors.get(i), true);
        }
        for (int i = qNB; i < qNB + dbNB; i++) {
            db[i - qNB] = new Point(i, allVectors.get(i), false);
        }

    }

    // public static void main(String[] args) throws IOException {
    // String dataName = "SIFT";
    // int qNB = 100000;
    // int dbNB = 900000;
    // Loader l = new Loader(dataName);
    // l.loadData(qNB, dbNB, 128);
    // System.out.println(l.query.size() + "/" + l.db.size());

    // }

}
