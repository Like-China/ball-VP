package VPTree;

import java.util.*;
import evaluation.*;
import utils.*;

public class VPTreeBySampleTester {

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

        public void loadData(int qSize, int dbPointsSize, int dim) {
                long t1 = System.currentTimeMillis();
                // load queryPoints / database points
                Loader l = new Loader();
                l.loadData(qSize, dbPointsSize, dim);
                this.queryPoints = l.query.toArray(new Point[l.query.size()]);
                this.dbPoints = l.db.toArray(new Point[l.db.size()]);
                // use KMeans to get clusters
                boolean isUseKmeans = true;
                if (isUseKmeans) {
                        ArrayList<Point> points = new ArrayList();
                        this.queryPoints = new Point[l.query.size()];
                        int maxIterations = 100;
                        List<List<Point>> clusters = KMeans.kMeansCluster(l.query, 10, maxIterations);
                        // for (List<Point> ls : clusters) {
                        for (Point p : clusters.get(0)) {
                                points.add(p);
                        }
                        this.queryPoints = points.toArray(new Point[points.size()]);
                }
                long t2 = System.currentTimeMillis();
                System.out.println("Data Loaded in " + (t2 - t1) + " ms");
        }

        public void test() {
                String data = Settings.data;
                int k = Settings.k;
                int sampleNB = Settings.sampleNB;
                int qNB = Settings.qNB;
                int dbNB = Settings.dbNB;
                int dim = Settings.dim;
                int bucketSize = Settings.bucketSize;
                double updateThreshold = Settings.updateThreshold;
                // load data
                loadData(qNB, dbNB, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbPointsSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);
                /*
                 * 1. kNN Test (To test the kNNs results of the brute-force solution and VP-tree
                 * based solution)
                 */
                // Brute-Force
                BFAlg bf = new BFAlg(queryPoints, dbPoints);
                ArrayList<PriorityQueue<NN>> BFRes = bf.kNNSearch(k);
                // VP-DFS
                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes = sVP.DFS(k, false, updateThreshold);
                Util.checkKNN(BFRes, DFSRes);

                /*
                 * 2. Best-First test (To test the VP-tree kNNs results of BFS and DFS based
                 * solutions)
                 */
                ArrayList<PriorityQueue<NN>> BFSRes = sVP.BFS(k, false, updateThreshold);
                Util.checkKNN(DFSRes, BFSRes);

        }

        public void cacheTest(double updateThreshold) {
                long t1 = System.currentTimeMillis();
                String data = Settings.data;
                int k = Settings.k;
                int sampleNB = Settings.sampleNB;
                int qNB = Settings.qNB;
                int dbNB = Settings.dbNB;
                int dim = Settings.dim;
                int bucketSize = Settings.bucketSize;
                // double factor = Settings.factor;
                int cacheSize = Settings.cacheSize;
                double factor = Settings.factor;

                // load data
                loadData(qNB, dbNB, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);

                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes, BFSRes, DFSBestRes, BFSBestRes, DFSBDCRes, BFSBDCRes, DFSGLORes,
                                BFSGLORes;
                ArrayList<PriorityQueue<NN>> DFSLRURes, BFSLRURes, DFSLFURes, BFSLFURes, DFSFIFORes, BFSFIFORes,
                                BFSGlobalRes;

                DFSRes = sVP.DFS(k, false, updateThreshold);
                ArrayList<PriorityQueue<NN>> DFSRes1 = sVP.DFS(k, true, updateThreshold);
                // Util.checkKNN(DFSRes, DFSRes1);
                BFSRes = sVP.BFS(k, false, updateThreshold);
                ArrayList<PriorityQueue<NN>> BFSRes1 = sVP.BFS(k, true, updateThreshold);
                Util.checkKNN(BFSRes, BFSRes1);

                BFSBDCRes = sVP.bestCache(factor, updateThreshold, k, true);

                // DFSLRURes = sVP.queryCache("Global", cacheSize, updateThreshold, k, true);
                // DFSLRURes = sVP.queryToObjectCache("Global", cacheSize, updateThreshold, k,
                // true);
                // Util.checkKNN(BFSGLORes, DFSLRURes);

                // BFSBDCRes = sVP.queryCache("BDC", cacheSize, updateThreshold, k, true);
                System.out.println("---------------------------------------------------------------------------");

                DFSLRURes = sVP.queryCache("BDC", cacheSize, updateThreshold, k, true);
                sVP.queryToObjectCache("BDC", cacheSize, updateThreshold, k, true);
                sVP.ObjectLinearCache("BDC", cacheSize, updateThreshold, k, true);
                // DFSLRURes = sVP.ObjectKGraphCache("BDC", cacheSize, updateThreshold, k,
                // true);
                System.out.println("---------------------------------------------------------------------------");
                Util.checkKNN(BFSBDCRes, DFSLRURes);

                sVP.queryCache("LRU", cacheSize, updateThreshold, k, true);
                BFSBDCRes = sVP.queryToObjectCache("LRU", cacheSize, updateThreshold, k, true);
                DFSLRURes = sVP.ObjectLinearCache("LRU", cacheSize, updateThreshold, k, true);
                // sVP.ObjectKGraphCache("LRU", cacheSize, updateThreshold, k, true);
                Util.checkKNN(BFSBDCRes, DFSLRURes);
                System.out.println("---------------------------------------------------------------------------");

                sVP.queryCache("FIFO", cacheSize, updateThreshold, k, true);
                BFSFIFORes = sVP.queryToObjectCache("FIFO", cacheSize, updateThreshold, k, true);
                DFSLRURes = sVP.ObjectLinearCache("FIFO", cacheSize, updateThreshold, k, true);
                // sVP.ObjectKGraphCache("FIFO", cacheSize, updateThreshold, k, true);
                // DFSLRURes = sVP.ObjectKGraphCache(cacheSize, updateThreshold, k, true);
                System.out.println("---------------------------------------------------------------------------");

                sVP.queryCache("LFU", cacheSize, updateThreshold, k, true);
                BFSBDCRes = sVP.queryToObjectCache("LFU", cacheSize, updateThreshold, k, true);
                DFSLRURes = sVP.ObjectLinearCache("LFU", cacheSize, updateThreshold, k, true);
                // sVP.ObjectKGraphCache("LFU", cacheSize, updateThreshold, k, true);
                Util.checkKNN(BFSBDCRes, DFSLRURes);
                System.out.println("---------------------------------------------------------------------------");

                // Util.checkKNN(DFSFIFORes, DFSBDCRes);
                // Util.checkKNN(BFSFIFORes, BFSBDCRes);
                // Util.checkKNN(BFSFIFORes, BFSGLORes);
                // Util.checkKNN(BFSLRURes, BFSLFURes);
                // Util.checkKNN(DFSBestRes, BFSFIFORes);
                // Util.checkKNN(DFSLRURes, DFSFIFORes);
                // Util.checkKNN(DFSRes, DFSLFURes);
                // Util.checkKNN(DFSBestRes, BFSLRURes);
                // Util.checkKNN(DFSRes, BFSRes);
                // Util.checkKNN(DFSBestRes, BFSBestRes);
                // Util.checkKNN(DFSBestRes, DFSLRURes);

                System.out.println("cache test-time cost: " + (System.currentTimeMillis() - t1));
        }

        public void graphTest(double updateThreshold) {
                long t1 = System.currentTimeMillis();
                String data = Settings.data;
                int k = Settings.k;
                int sampleNB = Settings.sampleNB;
                int qNB = Settings.qNB;
                int dbNB = Settings.dbNB;
                int dim = Settings.dim;
                int bucketSize = Settings.bucketSize;
                int cacheSize = Settings.cacheSize;
                double factor = Settings.factor;
                // load data
                loadData(qNB, dbNB, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbPointsSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);

                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes, BFSRes, DFSBestRes, BFSBestRes, DFSBDCRes, BFSBDCRes, DFSGLORes,
                                BFSGLORes;
                ArrayList<PriorityQueue<NN>> DFSLRURes, BFSLRURes, DFSLFURes, BFSLFURes, DFSFIFORes, BFSFIFORes;

                BFSBestRes = sVP.bestCache(factor, updateThreshold, k, true);

                System.out.println("time cost: " + (System.currentTimeMillis() - t1));
        }

        public void evaluate(int i, String data, int qSize, int dbPointsSize, int dim, int sampleNB, int k,
                        int bucketSize,
                        double factor, double updateThreshold, int cacheSize) {
                // load data
                loadData(qSize, dbPointsSize, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbPointsSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d \tfactor: %f \tfactor: %f",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize, factor,
                                updateThreshold);
                System.out.println(setInfo);
                VPAlg VPAlg = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                // DFS
                // VPAlg.DFS(k, true);
                // BFS
                VPAlg.BFS(k, false, updateThreshold);
                // Util.checkKNN(DFSRes, BFSRes);
                // VPAlg.bestCache(factor, updateThreshold, k, false);
                VPAlg.bestCache(factor, updateThreshold, k, true);
                // VPAlg.LRUCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryCache("LRU", cacheSize, updateThreshold, k, true);
                // VPAlg.LFUCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryCache("LFU", cacheSize, updateThreshold, k, true);
                // VPAlg.FIFOCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryCache("FIFO", cacheSize, updateThreshold, k, true);
                // VPAlg.GLOCache(cacheSize, updateThreshold, k, false);
                VPAlg.queryCache("Global", cacheSize, updateThreshold, k, true);
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

                DF_Global_Object_NodeAccess[i] = VPAlg.DF_Global_Object_NodeAccess;
                BF_Global_Object_NodeAccess[i] = VPAlg.BF_Global_Object_NodeAccess;
                DF_Global_queryPoints_NodeAccess[i] = VPAlg.DF_Global_Query_NodeAccess;
                BF_Global_queryPoints_NodeAccess[i] = VPAlg.BF_Global_Query_NodeAccess;

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

                DF_Global_Object_CalcCount[i] = VPAlg.DF_Global_Object_CalcCount;
                BF_Global_Object_CalcCount[i] = VPAlg.BF_Global_Object_CalcCount;
                DF_Global_queryPoints_CalcCount[i] = VPAlg.DF_Global_Query_CalcCount;
                BF_Global_queryPoints_CalcCount[i] = VPAlg.BF_Global_Query_CalcCount;
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
                DF_Global_Object_Time[i] = VPAlg.DF_Global_Object_Time;
                BF_Global_Object_Time[i] = VPAlg.BF_Global_Object_Time;
                DF_Global_queryPoints_Time[i] = VPAlg.DF_Global_Query_Time;
                BF_Global_queryPoints_Time[i] = VPAlg.BF_Global_Query_Time;

        }

        public static void main(String[] args) {
                VPTreeBySampleTester t = new VPTreeBySampleTester();
                // t.test();
                t.cacheTest(Settings.updateThreshold);
                // t.graphTest(Settings.updateThreshold);
                System.exit(0);

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

                setInfo = String.format("\nVarying cacheSize" +
                                Arrays.toString(Settings.cacheSizes));
                for (int i = 0; i < 5; i++) {
                        int cacheSize = Settings.cacheSizes[i];
                        System.out.println(setInfo + ": " + cacheSize);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                                        Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.factor, Settings.updateThreshold, cacheSize);
                }
                utils.Util.writeFile(setInfo, t);

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

                setInfo = String.format("\nVarying dimension" + Arrays.toString(Settings.dims));
                for (int i = 0; i < 5; i++) {
                        int dim = Settings.dims[i];
                        System.out.println(setInfo + ": " + dim);
                        t.evaluate(i, Settings.data, Settings.qNB, Settings.dbNB, dim, Settings.sampleNB, Settings.k,
                                        Settings.bucketSize, Settings.factor, Settings.updateThreshold,
                                        Settings.cacheSize);
                }
                utils.Util.writeFile(setInfo, t);

                long t2 = System.currentTimeMillis();
                System.out.println("Time Cost: " + (t2 - t1));

        }
}
