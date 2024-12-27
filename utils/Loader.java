package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import evaluation.Settings;

public class Loader {

    public ArrayList<Point> query = new ArrayList<>();
    public ArrayList<Point> db = new ArrayList<>();
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

    public List<int[]> loadBvecs(String filePath, int readSize) throws IOException {
        List<int[]> vectors = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
            while (dis.available() > 0 && vectors.size() < readSize) {
                // 读取向量的维度
                int dim = Integer.reverseBytes(dis.readInt());
                // 读取向量数据（假设以字节形式存储）
                int[] vector = new int[dim];
                for (int i = 0; i < dim; i++) {
                    vector[i] = dis.readUnsignedByte(); // 将每个字节读取为无符号整数
                }
                // 将向量添加到列表中
                vectors.add(vector);
                // if (vectors.size() % 400000 == 0) {
                // System.out.println("已加载 " + vectors.size() + " 个向量");
                // }
            }
        }
        return vectors;
    }

    public void loadData(int qNB, int dbNB, int dim) throws IOException {
        // 加载数据
        List<float[]> tempVectors = new ArrayList<>();
        switch (dataName) {
            case "random":
                Random r = new Random(0);
                for (int id = 0; id < qNB + dbNB; id++) {
                    float[] point = new float[dim];
                    for (int j = 0; j < dim; j++) {
                        point[j] = r.nextFloat();
                    }
                    tempVectors.add(point);
                }
                break;
            case "SIFTSMALL":
                filePath = filePath + "siftsmall/siftsmall_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "SIFT":
                filePath = filePath + "sift1M/sift_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "GIST":
                filePath = filePath + "gist/gist_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "DEEP":
                filePath = filePath + "deep1M/deep1M_base.fvecs";
                tempVectors = loadFvecs(filePath, qNB + dbNB);
                break;
            case "BIGANN":
                filePath = filePath + "bigann/bigann_base.bvecs";
                List<int[]> intVectors = loadBvecs(filePath, qNB + dbNB);
                tempVectors = convertIntToFloat(intVectors);
                break;
            default:
                throw new IllegalArgumentException("未知数据集: " + dataName);
        }
        // System.out.println(Arrays.toString(tempVectors.get(100)));
        List<float[]> allVectors = new ArrayList<>();
        for (float[] vector : tempVectors) {
            allVectors.add(Arrays.copyOf(vector, dim));
        }

        if (Settings.isShuffle) {
            Collections.shuffle(tempVectors);
        }
        assert allVectors.get(0).length == dim;
        assert allVectors.size() == qNB + dbNB;
        for (int i = 0; i < qNB; i++) {
            query.add(new Point(i, allVectors.get(i), true));
        }
        for (int i = qNB; i < qNB + dbNB; i++) {
            db.add(new Point(i, allVectors.get(i), false));
        }

    }

    private List<float[]> convertIntToFloat(List<int[]> intVectors) {
        List<float[]> floatVectors = new ArrayList<>();
        for (int[] intVector : intVectors) {
            float[] floatVector = new float[intVector.length];
            for (int i = 0; i < intVector.length; i++) {
                floatVector[i] = (float) intVector[i];
            }
            floatVectors.add(floatVector);
        }
        return floatVectors;
    }

}
