package VPTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import Distance.DistanceFunction;
import cacheIndex.KMeans;
import cacheIndex.Point;
import evaluation.BFAlg;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPSampleAlg;

public class VPTreeBySampleTester {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    DistanceFunction distFunction = Settings.distFunction;

    int varyNB = 5;
    public int[] DF_NodeAccess = new int[varyNB];
    public int[] BF_NodeAccess = new int[varyNB];
    public int[] DF_FIFO_NodeAccess = new int[varyNB];
    public int[] BF_FIFO_NodeAccess = new int[varyNB];
    public int[] DF_LRU_NodeAccess = new int[varyNB];
    public int[] BF_LRU_NodeAccess = new int[varyNB];
    public int[] DF_LFU_NodeAccess = new int[varyNB];
    public int[] BF_LFU_NodeAccess = new int[varyNB];
    public int[] DF_Best_NodeAccess = new int[varyNB];
    public int[] BF_Best_NodeAccess = new int[varyNB];
    // the number of calculation time
    public int[] DF_CalcCount = new int[varyNB];
    public int[] BF_CalcCount = new int[varyNB];
    public int[] DF_FIFO_CalcCount = new int[varyNB];
    public int[] BF_FIFO_CalcCount = new int[varyNB];
    public int[] DF_LRU_CalcCount = new int[varyNB];
    public int[] BF_LRU_CalcCount = new int[varyNB];
    public int[] DF_LFU_CalcCount = new int[varyNB];
    public int[] BF_LFU_CalcCount = new int[varyNB];
    public int[] DF_Best_CalcCount = new int[varyNB];
    public int[] BF_Best_CalcCount = new int[varyNB];
    // the search time
    public double[] DF_Time = new double[varyNB];
    public double[] BF_Time = new double[varyNB];
    public double[] DF_FIFO_Time = new double[varyNB];
    public double[] BF_FIFO_Time = new double[varyNB];
    public double[] DF_LRU_Time = new double[varyNB];
    public double[] BF_LRU_Time = new double[varyNB];
    public double[] DF_LFU_Time = new double[varyNB];
    public double[] BF_LFU_Time = new double[varyNB];
    public double[] DF_Best_Time = new double[varyNB];
    public double[] BF_Best_Time = new double[varyNB];

    public boolean checkKNN(ArrayList<PriorityQueue<NN>> res1, ArrayList<PriorityQueue<NN>> res2) {
        if (res1.size() != res2.size()) {
            return false;
        }
        for (int i = 0; i < res1.size(); i++) {
            PriorityQueue<NN> nns1 = new PriorityQueue<>(res1.get(i));
            PriorityQueue<NN> nns2 = new PriorityQueue<>(res2.get(i));
            while (!nns1.isEmpty()) {
                double d1 = nns1.poll().dist2query;
                double d2 = nns2.poll().dist2query;
                assert d1 == d2 : i + "/" + d1 + "/" + d2;
                if (d1 != d2) {
                    return false;
                }
            }
        }
        return true;
    }

    public void loadData(int qSize, int dbSize, int dim) {
        db = new ArrayList<>();
        query = new ArrayList<>();
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        // use KMeans to get clusters
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < l.query.size(); i++) {
            points.add(new Point(i, l.query.get(i)));
        }
        int maxIterations = 100;
        List<List<Point>> clusters = KMeans.kMeansCluster(points, 5, maxIterations);
        for (List<Point> ls : clusters) {
            for (Point p : ls) {
                this.query.add(p.coordinates);
            }
        }
        this.db = l.db;
        System.out.println("\n");
    }

    public void kNNTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // Brute-Force
        BFAlg bf = new BFAlg(query, db, distFunction);
        ArrayList<PriorityQueue<NN>> BFkNNRes = bf.kNNSearch(k);
        // VP-DFS
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.DFS(k);
        checkKNN(BFkNNRes, VPkNNRes);
    }

    public void BFSTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> DFSRes = sVP.DFS(k);
        ArrayList<PriorityQueue<NN>> BFSRes = sVP.BFS(k);
        checkKNN(DFSRes, BFSRes);
    }

    public void cacheTest(double updateThreshold) {
        // load data

        loadData(Settings.qNB, Settings.dbNB, Settings.dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                Settings.data,
                query.size(), db.size(), Settings.k, Settings.dim, Settings.sampleNB, Settings.bucketSize);
        System.out.println(setInfo);

        VPSampleAlg sVP = new VPSampleAlg(query, db, Settings.sampleNB, distFunction, Settings.bucketSize);
        ArrayList<PriorityQueue<NN>> DFSRes = sVP.DFS(Settings.k);
        ArrayList<PriorityQueue<NN>> BFSRes = sVP.BFS(Settings.k);
        checkKNN(DFSRes, BFSRes);
        ArrayList<PriorityQueue<NN>> DFSBestRes = sVP.bestCache(Settings.factor,
                updateThreshold, Settings.k, false);
        ArrayList<PriorityQueue<NN>> BFSBestRes = sVP.bestCache(Settings.factor,
                updateThreshold, Settings.k, true);
        checkKNN(DFSBestRes, BFSBestRes);
        ArrayList<PriorityQueue<NN>> DFSLRURes = sVP.LRUCache(Settings.cacheSize,
                updateThreshold, Settings.k, false);
        checkKNN(DFSBestRes, DFSLRURes);
        ArrayList<PriorityQueue<NN>> BFSLRURes = sVP.LRUCache(Settings.cacheSize,
                updateThreshold, Settings.k, true);
        checkKNN(DFSBestRes, BFSLRURes);
        ArrayList<PriorityQueue<NN>> DFSHQFRes = sVP.LFUCache(Settings.cacheSize,
                updateThreshold, Settings.k, false);
        checkKNN(DFSRes, DFSHQFRes);
        ArrayList<PriorityQueue<NN>> BFSHQFRes = sVP.LFUCache(Settings.cacheSize,
                updateThreshold, Settings.k, true);
        checkKNN(BFSLRURes, BFSHQFRes);
        ArrayList<PriorityQueue<NN>> DFSFIFORes = sVP.FIFOCache(Settings.cacheSize, updateThreshold, Settings.k, false);
        checkKNN(DFSLRURes, DFSFIFORes);
        ArrayList<PriorityQueue<NN>> BFSFIFORes = sVP.FIFOCache(Settings.cacheSize, updateThreshold, Settings.k, true);
        checkKNN(DFSBestRes, BFSFIFORes);
    }

    public void evaluate(int i, String data, int qSize, int dbSize, int dim, int sampleNB, int k, int bucketSize,
            double factor, double updateThreshold) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d \tfactor: %f \tfactor: %f",
                data, query.size(), db.size(), k, dim, sampleNB, bucketSize, factor, updateThreshold);
        System.out.println(setInfo);
        // construct VP-sampleNB
        VPSampleAlg VPAlg = new VPSampleAlg(query, db, sampleNB, distFunction, bucketSize);
        // DFS
        VPAlg.DFS(k);
        // Recu Best-First
        VPAlg.BFS(k);
        VPAlg.BFS(Settings.k);
        // checkKNN(DFSRes, BFSRes);
        VPAlg.bestCache(Settings.factor, updateThreshold, Settings.k, false);
        VPAlg.bestCache(Settings.factor, updateThreshold, Settings.k, true);
        VPAlg.LRUCache(Settings.cacheSize, updateThreshold, Settings.k, false);
        VPAlg.LRUCache(Settings.cacheSize, updateThreshold, Settings.k, true);
        VPAlg.LFUCache(Settings.cacheSize, updateThreshold, Settings.k, false);
        VPAlg.LFUCache(Settings.cacheSize, updateThreshold, Settings.k, true);
        VPAlg.FIFOCache(Settings.cacheSize, updateThreshold, Settings.k, false);
        VPAlg.FIFOCache(Settings.cacheSize, updateThreshold, Settings.k, true);
        // cacheTest
        DF_NodeAccess[i] = VPAlg.DF_NodeAccess;
        BF_NodeAccess[i] = VPAlg.BF_NodeAccess;
        DF_FIFO_NodeAccess[i] = VPAlg.DF_FIFO_NodeAccess;
        BF_FIFO_NodeAccess[i] = VPAlg.BF_FIFO_NodeAccess;
        DF_LRU_NodeAccess[i] = VPAlg.DF_LRU_NodeAccess;
        BF_LRU_NodeAccess[i] = VPAlg.BF_LRU_NodeAccess;
        DF_LFU_NodeAccess[i] = VPAlg.DF_LFU_NodeAccess;
        BF_LFU_NodeAccess[i] = VPAlg.BF_LFU_NodeAccess;
        DF_Best_NodeAccess[i] = VPAlg.DF_Best_NodeAccess;
        BF_Best_NodeAccess[i] = VPAlg.BF_Best_NodeAccess;
        // the number of calculation time
        DF_CalcCount[i] = VPAlg.DF_CalcCount;
        BF_CalcCount[i] = VPAlg.BF_CalcCount;
        DF_FIFO_CalcCount[i] = VPAlg.DF_FIFO_CalcCount;
        BF_FIFO_CalcCount[i] = VPAlg.BF_FIFO_CalcCount;
        DF_LRU_CalcCount[i] = VPAlg.DF_LRU_CalcCount;
        BF_LRU_CalcCount[i] = VPAlg.BF_LRU_CalcCount;
        DF_LFU_CalcCount[i] = VPAlg.DF_LFU_CalcCount;
        BF_LFU_CalcCount[i] = VPAlg.BF_LFU_CalcCount;
        DF_Best_CalcCount[i] = VPAlg.DF_Best_CalcCount;
        BF_Best_CalcCount[i] = VPAlg.BF_Best_CalcCount;
        // the search time
        DF_Time[i] = VPAlg.DF_Time;
        BF_Time[i] = VPAlg.BF_Time;
        DF_FIFO_Time[i] = VPAlg.DF_FIFO_Time;
        BF_FIFO_Time[i] = VPAlg.BF_FIFO_Time;
        DF_LRU_Time[i] = VPAlg.DF_LRU_Time;
        BF_LRU_Time[i] = VPAlg.BF_LRU_Time;
        DF_LFU_Time[i] = VPAlg.DF_LFU_Time;
        BF_LFU_Time[i] = VPAlg.BF_LFU_Time;
        DF_Best_Time[i] = VPAlg.DF_Best_Time;
        BF_Best_Time[i] = VPAlg.BF_Best_Time;

    }

    public static void writeFile(String setInfo, VPTreeBySampleTester t) {
        try {
            File writeName = new File(Settings.data + "_out.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName, true);
                    BufferedWriter out = new BufferedWriter(writer)) {
                if (setInfo.length() > 0) {
                    out.newLine();
                    out.newLine();
                    out.write(setInfo);
                }
                out.newLine();
                out.write("DF_NodeAccess=" + Arrays.toString(t.DF_NodeAccess));
                out.newLine();
                out.write("BF_NodeAccess=" + Arrays.toString(t.BF_NodeAccess));
                out.newLine();
                out.write("DF_FIFO_NodeAccess=" + Arrays.toString(t.DF_FIFO_NodeAccess));
                out.newLine();
                out.write("BF_FIFO_NodeAccess=" + Arrays.toString(t.BF_FIFO_NodeAccess));
                out.newLine();
                out.write("DF_LRU_NodeAccess=" + Arrays.toString(t.DF_LRU_NodeAccess));
                out.newLine();
                out.write("BF_LRU_NodeAccess=" + Arrays.toString(t.BF_LRU_NodeAccess));
                out.newLine();
                out.write("DF_LFU_NodeAccess=" + Arrays.toString(t.DF_LFU_NodeAccess));
                out.newLine();
                out.write("BF_LFU_NodeAccess=" + Arrays.toString(t.BF_LFU_NodeAccess));
                out.newLine();
                out.write("DF_Best_NodeAccess=" + Arrays.toString(t.DF_Best_NodeAccess));
                out.newLine();
                out.write("BF_Best_NodeAccess=" + Arrays.toString(t.BF_Best_NodeAccess));
                out.newLine();
                out.newLine();
                // the number of calculation time
                out.write("DF_CalcCount=" + Arrays.toString(t.DF_CalcCount));
                out.newLine();
                out.write("BF_CalcCount=" + Arrays.toString(t.BF_CalcCount));
                out.newLine();
                out.write("DF_FIFO_CalcCount=" + Arrays.toString(t.DF_FIFO_CalcCount));
                out.newLine();
                out.write("BF_FIFO_CalcCount=" + Arrays.toString(t.BF_FIFO_CalcCount));
                out.newLine();
                out.write("DF_LRU_CalcCount=" + Arrays.toString(t.DF_LRU_CalcCount));
                out.newLine();
                out.write("BF_LRU_CalcCount=" + Arrays.toString(t.BF_LRU_CalcCount));
                out.newLine();
                out.write("DF_LFU_CalcCount=" + Arrays.toString(t.DF_LFU_CalcCount));
                out.newLine();
                out.write("BF_LFU_CalcCount=" + Arrays.toString(t.BF_LFU_CalcCount));
                out.newLine();
                out.write("DF_Best_CalcCount=" + Arrays.toString(t.DF_Best_CalcCount));
                out.newLine();
                out.write("BF_Best_CalcCount=" + Arrays.toString(t.BF_Best_CalcCount));
                out.newLine();
                // the search time
                out.newLine();
                out.write("DF_Time=" + Arrays.toString(t.DF_Time));
                out.newLine();
                out.write("BF_Time=" + Arrays.toString(t.BF_Time));
                out.newLine();
                out.write("DF_FIFO_Time=" + Arrays.toString(t.DF_FIFO_Time));
                out.newLine();
                out.write("BF_FIFO_Time=" + Arrays.toString(t.BF_FIFO_Time));
                out.newLine();
                out.write("DF_LRU_Time=" + Arrays.toString(t.DF_LRU_Time));
                out.newLine();
                out.write("BF_LRU_Time=" + Arrays.toString(t.BF_LRU_Time));
                out.newLine();
                out.write("DF_LFU_Time=" + Arrays.toString(t.DF_LFU_Time));
                out.newLine();
                out.write("BF_LFU_Time=" + Arrays.toString(t.BF_LFU_Time));
                out.newLine();
                out.write("DF_Best_Time=" + Arrays.toString(t.DF_Best_Time));
                out.newLine();
                out.write("BF_Best_Time=" + Arrays.toString(t.BF_Best_Time));
                out.newLine();

                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        VPTreeBySampleTester t = new VPTreeBySampleTester();
        // t.cacheTest(Settings.updateThreshold);
        // System.exit(0);

        long t1 = System.currentTimeMillis();

        String setInfo = String.format("\nVarying factor" + Arrays.toString(Settings.factors));
        for (int i = 0; i < 5; i++) {
            double factor = Settings.factors[i];
            System.out.println(setInfo + ": " + factor);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, Settings.k,
                    Settings.bucketSize, factor, Settings.updateThreshold);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying bucketSize" + Arrays.toString(Settings.bucketSizes));
        for (int i = 0; i < 5; i++) {
            int bucketSize = Settings.bucketSizes[i];
            System.out.println(setInfo + ": " + bucketSize);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, Settings.k,
                    bucketSize, Settings.factor, Settings.updateThreshold);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying dimension" + Arrays.toString(Settings.dims));
        for (int i = 0; i < 5; i++) {
            int dim = Settings.dims[i];
            System.out.println(setInfo + ": " + dim);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, dim, Settings.sampleNB, Settings.k,
                    Settings.bucketSize, Settings.factor, Settings.updateThreshold);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying sampleNB" + Arrays.toString(Settings.sampleNBs));
        for (int i = 0; i < 5; i++) {
            int sampleNB = Settings.sampleNBs[i];
            System.out.println(setInfo + ": " + sampleNB);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, sampleNB, Settings.k,
                    Settings.bucketSize, Settings.factor, Settings.updateThreshold);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying k" + Arrays.toString(Settings.ks));
        for (int i = 0; i < 5; i++) {
            int k = Settings.ks[i];
            System.out.println(setInfo + ": " + k);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, k,
                    Settings.bucketSize, Settings.factor, Settings.updateThreshold);
        }
        writeFile(setInfo, t);

        long t2 = System.currentTimeMillis();
        System.out.println("Time Cost: " + (t2 - t1));

    }
}
