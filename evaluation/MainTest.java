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
        // the node Access
        public long[] DF_NodeAccess = new long[varyNB];
        public long[] BF_NodeAccess = new long[varyNB];
        public long[] DF_Best_NodeAccess = new long[varyNB];
        public long[] BF_Best_NodeAccess = new long[varyNB];
        public long[] DF_FIFO_NodeAccess = new long[varyNB];
        public long[] BF_FIFO_NodeAccess = new long[varyNB];
        public long[] DF_LRU_NodeAccess = new long[varyNB];
        public long[] BF_LRU_NodeAccess = new long[varyNB];
        public long[] DF_LFU_NodeAccess = new long[varyNB];
        public long[] BF_LFU_NodeAccess = new long[varyNB];
        public long[] DF_BDC_NodeAccess = new long[varyNB];
        public long[] BF_BDC_NodeAccess = new long[varyNB];
        public long[] DF_GLO_NodeAccess = new long[varyNB];
        public long[] BF_GLO_NodeAccess = new long[varyNB];
        // the number of calculation time
        public long[] DF_CalcCount = new long[varyNB];
        public long[] BF_CalcCount = new long[varyNB];
        public long[] DF_Best_CalcCount = new long[varyNB];
        public long[] BF_Best_CalcCount = new long[varyNB];
        public long[] DF_FIFO_CalcCount = new long[varyNB];
        public long[] BF_FIFO_CalcCount = new long[varyNB];
        public long[] DF_LRU_CalcCount = new long[varyNB];
        public long[] BF_LRU_CalcCount = new long[varyNB];
        public long[] DF_LFU_CalcCount = new long[varyNB];
        public long[] BF_LFU_CalcCount = new long[varyNB];
        public long[] DF_BDC_CalcCount = new long[varyNB];
        public long[] BF_BDC_CalcCount = new long[varyNB];
        public long[] DF_GLO_CalcCount = new long[varyNB];
        public long[] BF_GLO_CalcCount = new long[varyNB];
        // the search time
        public double[] DF_Time = new double[varyNB];
        public double[] BF_Time = new double[varyNB];
        public double[] DF_Best_Time = new double[varyNB];
        public double[] BF_Best_Time = new double[varyNB];
        public double[] DF_FIFO_Time = new double[varyNB];
        public double[] BF_FIFO_Time = new double[varyNB];
        public double[] DF_LRU_Time = new double[varyNB];
        public double[] BF_LRU_Time = new double[varyNB];
        public double[] DF_LFU_Time = new double[varyNB];
        public double[] BF_LFU_Time = new double[varyNB];
        public double[] DF_BDC_Time = new double[varyNB];
        public double[] BF_BDC_Time = new double[varyNB];
        public double[] DF_GLO_Time = new double[varyNB];
        public double[] BF_GLO_Time = new double[varyNB];

        public static void writeFile(String setInfo, MainTest t) {
                try {
                        File writeName = new File(Settings.data + "_out1.txt");
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
                                out.write("DF_Best_NodeAccess=" + Arrays.toString(t.DF_Best_NodeAccess));
                                out.newLine();
                                out.write("BF_Best_NodeAccess=" + Arrays.toString(t.BF_Best_NodeAccess));
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
                                out.write("DF_BDC_NodeAccess=" + Arrays.toString(t.DF_BDC_NodeAccess));
                                out.newLine();
                                out.write("BF_BDC_NodeAccess=" + Arrays.toString(t.BF_BDC_NodeAccess));
                                out.newLine();
                                out.write("DF_GLO_NodeAccess=" + Arrays.toString(t.DF_GLO_NodeAccess));
                                out.newLine();
                                out.write("BF_GLO_NodeAccess=" + Arrays.toString(t.BF_GLO_NodeAccess));
                                out.newLine();

                                out.newLine();
                                // the number of calculation time
                                out.write("DF_CalcCount=" + Arrays.toString(t.DF_CalcCount));
                                out.newLine();
                                out.write("BF_CalcCount=" + Arrays.toString(t.BF_CalcCount));
                                out.newLine();
                                out.write("DF_Best_CalcCount=" + Arrays.toString(t.DF_Best_CalcCount));
                                out.newLine();
                                out.write("BF_Best_CalcCount=" + Arrays.toString(t.BF_Best_CalcCount));
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
                                out.write("DF_BDC_CalcCount=" + Arrays.toString(t.DF_BDC_CalcCount));
                                out.newLine();
                                out.write("BF_BDC_CalcCount=" + Arrays.toString(t.BF_BDC_CalcCount));
                                out.newLine();
                                out.write("DF_GLO_CalcCount=" + Arrays.toString(t.DF_GLO_CalcCount));
                                out.newLine();
                                out.write("BF_GLO_CalcCount=" + Arrays.toString(t.BF_GLO_CalcCount));
                                out.newLine();
                                out.newLine();
                                // the search time
                                out.newLine();
                                out.write("DF_Time=" + Arrays.toString(t.DF_Time));
                                out.newLine();
                                out.write("BF_Time=" + Arrays.toString(t.BF_Time));
                                out.newLine();
                                out.write("DF_Best_Time=" + Arrays.toString(t.DF_Best_Time));
                                out.newLine();
                                out.write("BF_Best_Time=" + Arrays.toString(t.BF_Best_Time));
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
                                out.write("DF_BDC_Time=" + Arrays.toString(t.DF_BDC_Time));
                                out.newLine();
                                out.write("BF_BDC_Time=" + Arrays.toString(t.BF_BDC_Time));
                                out.newLine();
                                out.write("DF_GLO_Time=" + Arrays.toString(t.DF_GLO_Time));
                                out.newLine();
                                out.write("BF_GLO_Time=" + Arrays.toString(t.BF_GLO_Time));
                                out.newLine();
                                out.flush();
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void loadData(int qSize, int dbSize, int dim) {
                Loader l;
                try {
                        l = new Loader(Settings.data);
                        l.loadData(qSize, dbSize, dim);
                        this.queryPoints = l.query;
                        this.dbPoints = l.db;
                        long t1 = System.currentTimeMillis();
                        // use KMeans to get clusters
                        if (Settings.isUseKmeans) {
                                ArrayList<Point> points = new ArrayList();
                                int maxIterations = 10;
                                int k = 100;
                                List<List<Point>> clusters = KMeans.kMeansCluster(l.query, k, maxIterations);
                                System.out.println("KMeans Cluster Done! Cluster Size: " + clusters.size());
                                for (int i = 0; i < clusters.size(); i++) {
                                        for (Point p : clusters.get(i)) {
                                                points.add(p);
                                        }
                                }
                                this.queryPoints = points.toArray(new Point[points.size()]);
                        }
                        long t2 = System.currentTimeMillis();
                        System.out.println("Kmeans Finished in " + (t2 - t1) + " ms");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void evaluate(int i, String data, int qSize, int dbPointsSize, int dim, int sampleNB, int k,
                        int bucketSize, double updateThreshold, int cacheSize) {
                // load data
                loadData(qSize, dbPointsSize, dim);
                String setInfo = String.format(
                                "Data: %s\tqSize: %d\tdbSize: %d\tk: %d\tdim: %d\tsample: %d\tbucket Size:%d\tfactor: %f",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize,
                                updateThreshold);
                System.out.println(setInfo);
                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);

                sVP.DFS_BFS("#DFS", k, false, updateThreshold, false);
                // sVP.DFS_BFS("#BFS", k, false, updateThreshold, true);
                // sVP.bestCache("Best-DFS", updateThreshold, updateThreshold, k, false);
                // sVP.bestCache("Best-BFS", updateThreshold, updateThreshold, k, true);
                /*
                 * queryLinear_Cache1/queryLinear_To_ObjectLinear_Cache1
                 * ObjectLinear_Cache1/ObjectHNSW_Cache1
                 */
                sVP.ObjectHNSW_Cache("FIFO-DFS", cacheSize, updateThreshold, k, false);
                // sVP.ObjectHNSW_Cache("FIFO-BFS", cacheSize, updateThreshold, k, true);
                sVP.ObjectHNSW_Cache("LRU-DFS", cacheSize, updateThreshold, k, false);
                // sVP.ObjectHNSW_Cache("LRU-BFS", cacheSize, updateThreshold, k, true);
                sVP.ObjectHNSW_Cache("LFU-DFS", cacheSize, updateThreshold, k, false);
                // sVP.ObjectHNSW_Cache("LFU-BFS", cacheSize, updateThreshold, k, true);
                // sVP.ObjectHNSW_Cache("BDC-DFS", cacheSize, updateThreshold, k, false);
                sVP.ObjectHNSW_Cache("BDC-BFS", cacheSize, updateThreshold, k, true);
                // sVP.ObjectHNSW_Cache("GLO-DFS", cacheSize, updateThreshold, k, false);
                sVP.ObjectHNSW_Cache("GLO-BFS", cacheSize, updateThreshold, k, true);

                DF_NodeAccess[i] = sVP.nodeAccessOfEachMethod[0];
                BF_NodeAccess[i] = sVP.nodeAccessOfEachMethod[1];
                DF_Best_NodeAccess[i] = sVP.nodeAccessOfEachMethod[2];
                BF_Best_NodeAccess[i] = sVP.nodeAccessOfEachMethod[3];
                DF_FIFO_NodeAccess[i] = sVP.nodeAccessOfEachMethod[4];
                BF_FIFO_NodeAccess[i] = sVP.nodeAccessOfEachMethod[5];
                DF_LRU_NodeAccess[i] = sVP.nodeAccessOfEachMethod[6];
                BF_LRU_NodeAccess[i] = sVP.nodeAccessOfEachMethod[7];
                DF_LFU_NodeAccess[i] = sVP.nodeAccessOfEachMethod[8];
                BF_LFU_NodeAccess[i] = sVP.nodeAccessOfEachMethod[9];
                DF_BDC_NodeAccess[i] = sVP.nodeAccessOfEachMethod[10];
                BF_BDC_NodeAccess[i] = sVP.nodeAccessOfEachMethod[11];
                DF_GLO_NodeAccess[i] = sVP.nodeAccessOfEachMethod[12];
                BF_GLO_NodeAccess[i] = sVP.nodeAccessOfEachMethod[13];

                // the number of calculation time
                DF_CalcCount[i] = sVP.calcCountOfEachMethod[0];
                BF_CalcCount[i] = sVP.calcCountOfEachMethod[1];
                DF_Best_CalcCount[i] = sVP.calcCountOfEachMethod[2];
                BF_Best_CalcCount[i] = sVP.calcCountOfEachMethod[3];
                DF_FIFO_CalcCount[i] = sVP.calcCountOfEachMethod[4];
                BF_FIFO_CalcCount[i] = sVP.calcCountOfEachMethod[5];
                DF_LRU_CalcCount[i] = sVP.calcCountOfEachMethod[6];
                BF_LRU_CalcCount[i] = sVP.calcCountOfEachMethod[7];
                DF_LFU_CalcCount[i] = sVP.calcCountOfEachMethod[8];
                BF_LFU_CalcCount[i] = sVP.calcCountOfEachMethod[9];
                DF_BDC_CalcCount[i] = sVP.calcCountOfEachMethod[10];
                BF_BDC_CalcCount[i] = sVP.calcCountOfEachMethod[11];
                DF_GLO_CalcCount[i] = sVP.calcCountOfEachMethod[12];
                BF_GLO_CalcCount[i] = sVP.calcCountOfEachMethod[13];
                // the search time
                DF_Time[i] = sVP.timeOfEachMethod[0];
                BF_Time[i] = sVP.timeOfEachMethod[1];
                DF_Best_Time[i] = sVP.timeOfEachMethod[2];
                BF_Best_Time[i] = sVP.timeOfEachMethod[3];
                DF_FIFO_Time[i] = sVP.timeOfEachMethod[4];
                BF_FIFO_Time[i] = sVP.timeOfEachMethod[5];
                DF_LRU_Time[i] = sVP.timeOfEachMethod[6];
                BF_LRU_Time[i] = sVP.timeOfEachMethod[7];
                DF_LFU_Time[i] = sVP.timeOfEachMethod[8];
                BF_LFU_Time[i] = sVP.timeOfEachMethod[9];
                DF_BDC_Time[i] = sVP.timeOfEachMethod[10];
                BF_BDC_Time[i] = sVP.timeOfEachMethod[11];
                DF_GLO_Time[i] = sVP.timeOfEachMethod[12];
                BF_GLO_Time[i] = sVP.timeOfEachMethod[13];
        }

        public static void main(String[] args) {
                MainTest t = new MainTest();
                long t1, t2;
                t1 = System.currentTimeMillis();
                String setInfo = null;

                setInfo = String.format("\nVarying k" + Arrays.toString(Settings.ks));
                for (int i = 0; i < 5; i++) {
                        int k = Settings.ks[i];
                        System.out.println(setInfo + ": " + k);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        Settings.sampleNB, k,
                                        Settings.bucketSize, Settings.updateThreshold,
                                        Settings.cacheSize);
                }
                writeFile(setInfo, t);
                t2 = System.currentTimeMillis();
                System.out.println("Time Cost: " + (t2 - t1));

                setInfo = String.format("\nVarying cacheSize" +
                                Arrays.toString(Settings.cacheSizes));
                for (int i = 0; i < 5; i++) {
                        int cacheSize = Settings.cacheSizes[i];
                        System.out.println(setInfo + ": " + cacheSize);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.updateThreshold, cacheSize);
                }
                writeFile(setInfo, t);
                t2 = System.currentTimeMillis();
                System.out.println("Time Cost: " + (t2 - t1));

                setInfo = String.format("\nVarying cacheUpdateThreshold" +
                                Arrays.toString(Settings.updateThresholds));
                for (int i = 0; i < 5; i++) {
                        double updateThreshold = Settings.updateThresholds[i];
                        System.out.println(setInfo + ": " + updateThreshold);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, updateThreshold, Settings.cacheSize);
                }
                writeFile(setInfo, t);

                setInfo = String.format("\nVarying bucketSize" + Arrays.toString(Settings.bucketSizes));
                for (int i = 0; i < 5; i++) {
                        int bucketSize = Settings.bucketSizes[i];
                        System.out.println(setInfo + ": " + bucketSize);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        Settings.sampleNB, Settings.k,
                                        bucketSize, Settings.updateThreshold, Settings.cacheSize);
                }
                writeFile(setInfo, t);

                setInfo = String.format("\nVarying sampleNB" +
                                Arrays.toString(Settings.sampleNBs));
                for (int i = 0; i < 5; i++) {
                        int sampleNB = Settings.sampleNBs[i];
                        System.out.println(setInfo + ": " + sampleNB);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.updateThreshold,
                                        Settings.cacheSize);
                }
                writeFile(setInfo, t);

                setInfo = String.format("\nVarying dimension" + Arrays.toString(Settings.dims));
                for (int i = 0; i < 5; i++) {
                        int dim = Settings.dims[i];
                        System.out.println(setInfo + ": " + dim);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, dim,
                                        Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.updateThreshold,
                                        Settings.cacheSize);
                }
                writeFile(setInfo, t);
                t2 = System.currentTimeMillis();
                System.out.println("Time Cost: " + (t2 - t1));

                setInfo = String.format("\nVarying data size" + Arrays.toString(Settings.dbNBs));
                for (int i = 0; i < 5; i++) {
                        int dbNB = Settings.dbNBs[i];
                        System.out.println(setInfo + ": " + dbNB);
                        t.evaluate(i, Settings.data, Settings.qNB, dbNB, Settings.dim,
                                        Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.updateThreshold,
                                        Settings.cacheSize);
                }
                writeFile(setInfo, t);
                t2 = System.currentTimeMillis();
                System.out.println("Time Cost: " + (t2 - t1));
        }
}
