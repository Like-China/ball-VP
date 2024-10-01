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
import evaluation.BJAlg;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPSampleAlg;

public class VPTreeBySampleTester {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    DistanceFunction distFunction = Settings.distFunction;
    public int[] DFNodeAccess = new int[5];
    public int[] BFHierNodeAccess = new int[5];
    public int[] BFRecuNodeAccess = new int[5];
    public int[] DFCacheNodeAccess = new int[5];
    public int[] BFHierCacheNodeAccess = new int[5];
    public int[] BFRecuCacheNodeAccess = new int[5];
    // the number of calculation time
    public int[] DFCalcCount = new int[5];
    public int[] BFHierCalcCount = new int[5];
    public int[] BFRecuCalcCount = new int[5];
    public int[] DFCacheCalcCount = new int[5];
    public int[] BFHierCacheCalcCount = new int[5];
    public int[] BFRecuCacheCalcCount = new int[5];
    // the search time
    public double[] DFTime = new double[5];
    public double[] BFHierTime = new double[5];
    public double[] BFRecuTime = new double[5];
    public double[] DFCacheTime = new double[5];
    public double[] BFHierCacheTime = new double[5];
    public double[] BFRecuCacheTime = new double[5];

    public void checkNN(ArrayList<double[]> query, ArrayList<double[]> a, ArrayList<double[]> b) {
        assert query.size() == a.size();
        assert query.size() == b.size();
        for (int i = 0; i < query.size(); i++) {
            double dist1 = distFunction.distance(a.get(i), query.get(i));
            double dist2 = distFunction.distance(b.get(i), query.get(i));
            assert dist1 == dist2 : i + "/" + dist1 + "/" + dist2;
        }
    }

    // check the results
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

    public void testNN(String data, int qSize, int dbSize, int dim, int sampleNB) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format("Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tsample: %d", data,
                query.size(), db.size(), dim, sampleNB);
        System.out.println(setInfo);
        // ball-tree
        BJAlg bj = new BJAlg(query, db, 10, distFunction);
        ArrayList<double[]> BJNNRes = bj.nnSearch();
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<double[]> VPNNRes = sVP.nnSearch();
        // result check
        checkNN(query, BJNNRes, VPNNRes);
    }

    public void testkNN(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // Brute-Force
        BFAlg bf = new BFAlg(query, db, distFunction);
        ArrayList<PriorityQueue<NN>> BFkNNRes = bf.kNNSearch(k);
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.searchkNNDFS(k);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = BFkNNRes.get(i);
            assert nn1.size() == nn2.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                assert d1 == d2;
            }
        }
    }

    public void bestFirstTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // VP-sampleNB
        // DFS
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.searchkNNDFS(k);
        // Recu Best-First
        VPSampleAlg sVP2 = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes2 = sVP2.searchkNNBestFirst(k);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = VPkNNRes2.get(i);
            assert nn1.size() == nn2.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                assert d1 == d2;
            }
        }
    }

    public void cacheTest() {
        // load data
        loadData(Settings.qNB, Settings.dbNB, Settings.dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                Settings.data,
                query.size(), db.size(), Settings.k, Settings.dim, Settings.sampleNB, Settings.bucketSize);
        System.out.println(setInfo);

        VPSampleAlg sVP = new VPSampleAlg(query, db, Settings.sampleNB, distFunction, Settings.bucketSize);
        ArrayList<PriorityQueue<NN>> DFSBestRes = sVP.bestCache(Settings.factor, Settings.k, false);
        ArrayList<PriorityQueue<NN>> BFSBestRes = sVP.bestCache(Settings.factor, Settings.k, true);
        checkKNN(DFSBestRes, BFSBestRes);
        ArrayList<PriorityQueue<NN>> DFSLRURes = sVP.LRUCache(Settings.cacheSize, Settings.k, false);
        checkKNN(DFSBestRes, DFSLRURes);
        ArrayList<PriorityQueue<NN>> BFSLRURes = sVP.LRUCache(Settings.cacheSize, Settings.k, true);
        checkKNN(DFSBestRes, BFSLRURes);
    }

    public void evaluate(int i, String data, int qSize, int dbSize, int dim, int sampleNB, int k, int bucketSize,
            double factor) {
        // load data
        loadData(qSize, dbSize, dim);
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d \tfactor: %f",
                data, query.size(), db.size(), k, dim, sampleNB, bucketSize, factor);
        System.out.println(setInfo);
        // construct VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, bucketSize);
        // DFS
        sVP.searchkNNDFS(k);
        // Recu Best-First
        sVP.searchkNNBestFirst(k);
        // cacheTest
        sVP.LRUCache(k, 100, false);

        DFNodeAccess[i] = sVP.DFNodeAccess;
        BFHierNodeAccess[i] = sVP.BFHierNodeAccess;
        BFRecuNodeAccess[i] = sVP.BFRecuNodeAccess;
        DFCacheNodeAccess[i] = sVP.DFCacheNodeAccess;
        BFHierCacheNodeAccess[i] = sVP.BFHierCacheNodeAccess;
        BFRecuCacheNodeAccess[i] = sVP.BFRecuCacheNodeAccess;
        // the number of calculation time
        DFCalcCount[i] = sVP.DFCalcCount;
        BFHierCalcCount[i] = sVP.BFHierCalcCount;
        BFRecuCalcCount[i] = sVP.BFRecuCalcCount;
        DFCacheCalcCount[i] = sVP.DFCacheCalcCount;
        BFHierCacheCalcCount[i] = sVP.BFHierCacheCalcCount;
        BFRecuCacheCalcCount[i] = sVP.BFRecuCacheCalcCount;
        // the search time
        DFTime[i] = sVP.DFTime;
        BFHierTime[i] = sVP.BFHierTime;
        BFRecuTime[i] = sVP.BFRecuTime;
        DFCacheTime[i] = sVP.DFCacheTime;
        BFHierCacheTime[i] = sVP.BFHierCacheTime;
        BFRecuCacheTime[i] = sVP.BFRecuCacheTime;

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
                out.write("DFNodeAccess=" + Arrays.toString(t.DFNodeAccess));
                out.newLine();
                // out.write("BFHierNodeAccess\n" + Arrays.toString(t.BFHierNodeAccess));
                // out.newLine();
                out.write("BFRecuNodeAccess=" + Arrays.toString(t.BFRecuNodeAccess));
                out.newLine();
                out.write("DFCacheNodeAccess=" + Arrays.toString(t.DFCacheNodeAccess));
                out.newLine();
                // out.write("BFHierCacheNodeAccess\n" +
                // Arrays.toString(t.BFHierCacheNodeAccess));
                // out.newLine();
                out.write("BFRecuCacheNodeAccess=" + Arrays.toString(t.BFRecuCacheNodeAccess));
                out.newLine();
                out.newLine();
                out.write("DFCalcCount=" + Arrays.toString(t.DFCalcCount));
                out.newLine();
                // out.write("BFHierCalcCount\n" + Arrays.toString(t.BFHierCalcCount));
                // out.newLine();
                out.write("BFRecuCalcCount=" + Arrays.toString(t.BFRecuCalcCount));
                out.newLine();
                out.write("DFCacheCalcCount=" + Arrays.toString(t.DFCacheCalcCount));
                out.newLine();
                // out.write("BFHierCacheCalcCount\n" +
                // Arrays.toString(t.BFHierCacheCalcCount));
                // out.newLine();
                out.write("BFRecuCacheCalcCount=" + Arrays.toString(t.BFRecuCacheCalcCount));
                out.newLine();
                out.newLine();
                out.write("DFTime=" + Arrays.toString(t.DFTime));
                out.newLine();
                // out.write("BFHierTime\n" + Arrays.toString(t.BFHierTime));
                // out.newLine();
                out.write("BFRecuTime=" + Arrays.toString(t.BFRecuTime));
                out.newLine();
                out.write("DFCacheTime=" + Arrays.toString(t.DFCacheTime));
                out.newLine();
                // out.write("BFHierCacheTime\n" + Arrays.toString(t.BFHierCacheTime));
                // out.newLine();
                out.write("BFRecuCacheTime=" + Arrays.toString(t.BFRecuCacheTime));
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
        // t.testNN("sift", 1000, 100000, 10, 50);
        // t.testkNN("sift", 1000, 100000, 10, 50, 10);
        // t.bestFirstTest("sift", 1000, 1000000, 10, 10, 10);
        t.cacheTest();
        System.exit(0);

        long t1 = System.currentTimeMillis();

        String setInfo = String.format("\nVarying factor" + Arrays.toString(Settings.factors));
        for (int i = 0; i < 5; i++) {
            double factor = Settings.factors[i];
            System.out.println(setInfo + ": " + factor);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, Settings.k,
                    Settings.bucketSize, factor);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying bucketSize" + Arrays.toString(Settings.bucketSizes));
        for (int i = 0; i < 5; i++) {
            int bucketSize = Settings.bucketSizes[i];
            System.out.println(setInfo + ": " + bucketSize);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, Settings.k,
                    bucketSize, Settings.factor);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying dimension" + Arrays.toString(Settings.dims));
        for (int i = 0; i < 5; i++) {
            int dim = Settings.dims[i];
            System.out.println(setInfo + ": " + dim);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, dim, Settings.sampleNB, Settings.k,
                    Settings.bucketSize, Settings.factor);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying sampleNB" + Arrays.toString(Settings.sampleNBs));
        for (int i = 0; i < 5; i++) {
            int sampleNB = Settings.sampleNBs[i];
            System.out.println(setInfo + ": " + sampleNB);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, sampleNB, Settings.k,
                    Settings.bucketSize, Settings.factor);
        }
        writeFile(setInfo, t);

        setInfo = String.format("\nVarying k" + Arrays.toString(Settings.ks));
        for (int i = 0; i < 5; i++) {
            int k = Settings.ks[i];
            System.out.println(setInfo + ": " + k);
            t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, k,
                    Settings.bucketSize, Settings.factor);
        }
        writeFile(setInfo, t);

        long t2 = System.currentTimeMillis();
        System.out.println("Time Cost: " + (t2 - t1));

    }
}
