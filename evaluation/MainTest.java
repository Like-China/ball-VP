package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import utils.*;

public class MainTest {

        public Point[] dbPoints = null;
        public Point[] queryPoints = null;

        int varyNB = 5;
        public long[] DF_NodeAccess = new long[varyNB];
        public long[] BF_NodeAccess = new long[varyNB];
        public long[] DF_FIFO_NodeAccess = new long[varyNB];
        public long[] BF_FIFO_NodeAccess = new long[varyNB];
        public long[] DF_LRU_NodeAccess = new long[varyNB];
        public long[] BF_LRU_NodeAccess = new long[varyNB];
        public long[] DF_LFU_NodeAccess = new long[varyNB];
        public long[] BF_LFU_NodeAccess = new long[varyNB];
        public long[] DF_Global_Object_NodeAccess = new long[varyNB];
        public long[] BF_Global_Object_NodeAccess = new long[varyNB];
        public long[] DF_Global_queryPoints_NodeAccess = new long[varyNB];
        public long[] BF_Global_queryPoints_NodeAccess = new long[varyNB];
        public long[] DF_Best_NodeAccess = new long[varyNB];
        public long[] BF_Best_NodeAccess = new long[varyNB];
        // the number of calculation time
        public long[] DF_CalcCount = new long[varyNB];
        public long[] BF_CalcCount = new long[varyNB];
        public long[] DF_FIFO_CalcCount = new long[varyNB];
        public long[] BF_FIFO_CalcCount = new long[varyNB];
        public long[] DF_LRU_CalcCount = new long[varyNB];
        public long[] BF_LRU_CalcCount = new long[varyNB];
        public long[] DF_LFU_CalcCount = new long[varyNB];
        public long[] BF_LFU_CalcCount = new long[varyNB];
        public long[] DF_Global_Object_CalcCount = new long[varyNB];
        public long[] BF_Global_Object_CalcCount = new long[varyNB];
        public long[] DF_Global_queryPoints_CalcCount = new long[varyNB];
        public long[] BF_Global_queryPoints_CalcCount = new long[varyNB];
        public long[] DF_Best_CalcCount = new long[varyNB];
        public long[] BF_Best_CalcCount = new long[varyNB];
        // the search time
        public double[] DF_Time = new double[varyNB];
        public double[] BF_Time = new double[varyNB];
        public double[] DF_FIFO_Time = new double[varyNB];
        public double[] BF_FIFO_Time = new double[varyNB];
        public double[] DF_LRU_Time = new double[varyNB];
        public double[] BF_LRU_Time = new double[varyNB];
        public double[] DF_LFU_Time = new double[varyNB];
        public double[] BF_LFU_Time = new double[varyNB];
        public double[] DF_Global_Object_Time = new double[varyNB];
        public double[] BF_Global_Object_Time = new double[varyNB];
        public double[] DF_Global_queryPoints_Time = new double[varyNB];
        public double[] BF_Global_queryPoints_Time = new double[varyNB];
        public double[] DF_Best_Time = new double[varyNB];
        public double[] BF_Best_Time = new double[varyNB];

        public static void writeFile(String setInfo, MainTest t) {
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
                                out.write("DF_Global_Object_NodeAccess="
                                                + Arrays.toString(t.DF_Global_Object_NodeAccess));
                                out.newLine();
                                out.write("BF_Global_Object_NodeAccess="
                                                + Arrays.toString(t.BF_Global_Object_NodeAccess));
                                out.newLine();
                                out.write("DF_Global_queryPoints_NodeAccess="
                                                + Arrays.toString(t.DF_Global_queryPoints_NodeAccess));
                                out.newLine();
                                out.write("BF_Global_queryPoints_NodeAccess="
                                                + Arrays.toString(t.BF_Global_queryPoints_NodeAccess));
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

                                out.write("DF_Global_Object_CalcCount="
                                                + Arrays.toString(t.DF_Global_Object_CalcCount));
                                out.newLine();
                                out.write("BF_Global_Object_CalcCount="
                                                + Arrays.toString(t.BF_Global_Object_CalcCount));
                                out.newLine();
                                out.write("DF_Global_queryPoints_CalcCount="
                                                + Arrays.toString(t.DF_Global_queryPoints_CalcCount));
                                out.newLine();
                                out.write("BF_Global_queryPoints_CalcCount="
                                                + Arrays.toString(t.BF_Global_queryPoints_CalcCount));
                                out.newLine();
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

                                out.write("DF_Global_Object_Time=" + Arrays.toString(t.DF_Global_Object_Time));
                                out.newLine();
                                out.write("BF_Global_Object_Time=" + Arrays.toString(t.BF_Global_Object_Time));
                                out.newLine();
                                out.write("DF_Global_queryPoints_Time="
                                                + Arrays.toString(t.DF_Global_queryPoints_Time));
                                out.newLine();
                                out.write("BF_Global_queryPoints_Time="
                                                + Arrays.toString(t.BF_Global_queryPoints_Time));

                                out.newLine();
                                out.flush();
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void loadData(int qSize, int dbPointsSize, int dim) {
                long t1 = System.currentTimeMillis();
                // load queryPoints / database points
                Loader l;
                try {
                        l = new Loader(Settings.data);
                        l.loadData(qSize, dbPointsSize, dim);
                        this.queryPoints = l.query;
                        this.dbPoints = l.db;
                        // use KMeans to get clusters
                        if (Settings.isUseKmeans) {
                                ArrayList<Point> points = new ArrayList();
                                this.queryPoints = new Point[l.query.length];
                                int maxIterations = 100;
                                List<List<Point>> clusters = KMeans.kMeansCluster(l.query, 10, maxIterations);
                                for (Point p : clusters.get(0)) {
                                        points.add(p);
                                }
                                this.queryPoints = points.toArray(new Point[points.size()]);
                        }
                        long t2 = System.currentTimeMillis();
                        System.out.println("Data Loaded in " + (t2 - t1) + " ms");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void evaluate(int i, String data, int qSize, int dbPointsSize, int dim, int sampleNB, int k,
                        int bucketSize,
                        double factor, double updateThreshold, int cacheSize) {
                // load data
                loadData(qSize, dbPointsSize, dim);
                String setInfo = String.format(
                                "Data: %s\tqSize: %d\tdbSize: %d\tk: %d\tdim: %d\tsample: %d\tbucket Size:%d\tfactor: %f\tfactor: %f",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize, factor,
                                updateThreshold);
                System.out.println(setInfo);
                VPAlg VPAlg = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                // DFS
                // VPAlg.DFS(k, true);
                // BFS
                VPAlg.DFS_BFS("#BFS", k, false, updateThreshold, true);
                // VPAlg.bestCache(factor, updateThreshold, k, false);
                VPAlg.bestCache("Best-BFS", factor, updateThreshold, k, true);
                // VPAlg.LRUCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryLinear_Cache("LRU", cacheSize, updateThreshold, k, true);
                // VPAlg.LFUCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryLinear_Cache("LFU", cacheSize, updateThreshold, k, true);
                // VPAlg.FIFOCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryLinear_Cache("FIFO", cacheSize, updateThreshold, k, true);
                // VPAlg.GLOCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryLinear_Cache("Global", cacheSize, updateThreshold, k, true);
                // cacheTest

                HashMap<String, Integer> myMap = Settings.myMap;
                DF_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_FIFO_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_FIFO_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_LRU_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_LRU_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_LFU_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_LFU_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_Best_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_Best_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_Global_Object_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_Global_Object_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                DF_Global_queryPoints_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];
                BF_Global_queryPoints_NodeAccess[i] = VPAlg.nodeAccessOfEachMethod[myMap.get("#DFS")];

                // the number of calculation time
                DF_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                DF_FIFO_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_FIFO_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                DF_LRU_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_LRU_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                DF_LFU_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_LFU_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                DF_Best_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_Best_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];

                DF_Global_Object_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_Global_Object_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                DF_Global_queryPoints_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                BF_Global_queryPoints_CalcCount[i] = VPAlg.calcCountOfEachMethod[myMap.get("#DFS")];
                // the search time
                DF_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_FIFO_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_FIFO_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_LRU_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_LRU_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_LFU_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_LFU_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_Best_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_Best_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_Global_Object_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_Global_Object_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                DF_Global_queryPoints_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];
                BF_Global_queryPoints_Time[i] = VPAlg.timeOfEachMethod[myMap.get("#DFS")];

        }

        public static void main(String[] args) {
                MainTest t = new MainTest();
                long t1 = System.currentTimeMillis();
                String setInfo = null;

                // setInfo = String.format("\nVarying factor" +
                // Arrays.toString(factors));
                // for (int i = 0; i < 5; i++) {
                // double factor = Settings.factors[i];
                // System.out.println(setInfo + ": " + factor);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbPointsNB, Settings.dim,
                // Settings.sampleNB, k,
                // Settings.bucketSize, factor, Settings.updateThreshold, Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying cacheSize" +
                // Arrays.toString(Settings.cacheSizes));
                // for (int i = 0; i < 5; i++) {
                // int cacheSize = Settings.cacheSizes[i];
                // System.out.println(setInfo + ": " + cacheSize);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                // Settings.sampleNB, Settings.k,
                // Settings.bucketSize, Settings.factor, Settings.updateThreshold, cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying cacheUpdateThreshold" +
                // Arrays.toString(Settings.updateThresholds));
                // for (int i = 0; i < 5; i++) {
                // double updateThreshold = Settings.updateThresholds[i];
                // System.out.println(setInfo + ": " + updateThreshold);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbPointsNB, Settings.dim,
                // Settings.sampleNB, Settings.k,
                // Settings.bucketSize, Settings.factor, updateThreshold, Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying k" + Arrays.toString(ks));
                // for (int i = 0; i < 5; i++) {
                // int k = Settings.ks[i];
                // System.out.println(setInfo + ": " + k);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbPointsNB, Settings.dim,
                // Settings.sampleNB, k,
                // Settings.bucketSize, Settings.factor, Settings.updateThreshold,
                // Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying bucketSize" +
                // Arrays.toString(Settings.bucketSizes));
                // for (int i = 0; i < 5; i++) {
                // int bucketSize = Settings.bucketSizes[i];
                // System.out.println(setInfo + ": " + bucketSize);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbPointsNB, Settings.dim,
                // Settings.sampleNB, Settings.k,
                // bucketSize, Settings.factor, Settings.updateThreshold, Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying sampleNB" +
                // Arrays.toString(Settings.sampleNBs));
                // for (int i = 0; i < 5; i++) {
                // int sampleNB = Settings.sampleNBs[i];
                // System.out.println(setInfo + ": " + sampleNB);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbPointsNB, Settings.dim,
                // sampleNB, Settings.k,
                // Settings.bucketSize, Settings.factor, Settings.updateThreshold,
                // Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // setInfo = String.format("\nVarying dimension" +
                // Arrays.toString(Settings.dims));
                // for (int i = 0; i < 5; i++) {
                // int dim = Settings.dims[i];
                // System.out.println(setInfo + ": " + dim);
                // t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, dim,
                // Settings.sampleNB, Settings.k,
                // Settings.bucketSize, Settings.factor, Settings.updateThreshold,
                // Settings.cacheSize);
                // }
                // utils.Util.writeFile(setInfo, t);

                // long t2 = System.currentTimeMillis();
                // System.out.println("Time Cost: " + (t2 - t1));

        }
}
