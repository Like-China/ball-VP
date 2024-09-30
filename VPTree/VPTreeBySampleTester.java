package VPTree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import Distance.DistanceFunction;
import evaluation.BFAlg;
import evaluation.BJAlg;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPFarAlg;
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

    public void _check(ArrayList<double[]> query, ArrayList<double[]> a, ArrayList<double[]> b) {
        assert query.size() == a.size();
        assert query.size() == b.size();
        for (int i = 0; i < query.size(); i++) {
            double dist1 = distFunction.distance(a.get(i), query.get(i));
            double dist2 = distFunction.distance(b.get(i), query.get(i));
            assert dist1 == dist2 : dist1 + "/" + dist2;
        }
    }

    public void testNN(String data, int qSize, int dbSize, int dim, int sampleNB) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format("Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tsample: %d", data,
                query.size(), db.size(), dim, sampleNB);
        System.out.println(setInfo);
        // ball-tree
        BJAlg bj = new BJAlg(query, db, 10, distFunction);
        ArrayList<double[]> BJNNRes = bj.nnSearch();
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<double[]> VPNNRes = sVP.nnSearch();
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sampleNB, distFunction);
        ArrayList<double[]> fVPNNRes = fVP.nnSearch();

        // result check
        _check(query, BJNNRes, VPNNRes);
        _check(query, VPNNRes, fVPNNRes);

    }

    public void testkNN(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
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
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> fVPkNNRes = fVP.searchkNNDFS(k);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = fVPkNNRes.get(i);
            PriorityQueue<NN> nn3 = BFkNNRes.get(i);
            assert nn1.size() == nn2.size();
            assert nn1.size() == nn3.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                double d3 = nn3.poll().dist2query;
                assert d1 == d2;
                assert d1 == d3;
            }
        }
    }

    public void bestFirstTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // VP-sampleNB
        // DFS
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.searchkNNDFS(k);
        // Hier Best-First
        VPSampleAlg sVP1 = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes1 = sVP1.searchkNNBestFirst(k, true);
        // Recu Best-First
        VPSampleAlg sVP2 = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes2 = sVP2.searchkNNBestFirst(k, false);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = VPkNNRes1.get(i);
            PriorityQueue<NN> nn3 = VPkNNRes2.get(i);
            assert nn1.size() == nn2.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                double d3 = nn3.poll().dist2query;
                assert d1 == d2;
                assert d1 == d3;
            }
        }
    }

    public void cacheTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k, double factor) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data,
                query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, 10);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.cacheTest(factor, k);
    }

    public void evaluate(int i, String data, int qSize, int dbSize, int dim, int sampleNB, int k, int bucketSize,
            double factor) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;
        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d \tfactor: %f",
                data, query.size(), db.size(), k, dim, sampleNB, bucketSize, factor);
        System.out.println(setInfo);
        // construct VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, bucketSize);
        // DFS
        sVP.searchkNNDFS(k);
        // Hier Best-First
        // sVP.searchkNNBestFirst(k, true);
        // Recu Best-First
        sVP.searchkNNBestFirst(k, false);
        // cacheTest
        sVP.cacheTest(factor, k);

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
        // t.cacheTest("sift", 1000, 1000000, 10, 10, 10, 2);
        // System.exit(0);

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
