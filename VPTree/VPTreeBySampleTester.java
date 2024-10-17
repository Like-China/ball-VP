package VPTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import evaluation.BFAlg;
import evaluation.Settings;
import evaluation.VPGraphAlg;
import evaluation.VPLinearAlg;
import linearcache.KMeans;
import utils.Loader;
import utils.NN;
import utils.Point;

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

        public boolean checkKNN(ArrayList<PriorityQueue<NN>> res1, ArrayList<PriorityQueue<NN>> res2) {
                assert !res1.isEmpty();
                assert res1.size() == res2.size();

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

        public void loadData(int qSize, int dbPointsSize, int dim) {
                // load queryPoints/database points
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
                System.out.println("Data Loaded\n");
        }

        public void test() {
                String data = Settings.data;
                int k = Settings.k;
                int sampleNB = Settings.sampleNB;
                int qNB = Settings.qNB;
                int dbNB = Settings.dbNB;
                int dim = Settings.dim;
                int bucketSize = Settings.bucketSize;
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
                VPLinearAlg sVP = new VPLinearAlg(queryPoints, dbPoints, sampleNB, Settings.bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes = sVP.DFS(k, false);
                checkKNN(BFRes, DFSRes);

                /*
                 * 2. Best-First test (To test the VP-tree kNNs results of BFS and DFS based
                 * solutions)
                 */
                ArrayList<PriorityQueue<NN>> BFSRes = sVP.BFS(k, false);
                checkKNN(DFSRes, BFSRes);

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
                double factor = Settings.factor;
                int cacheSize = Settings.cacheSize;

                // load data
                loadData(qNB, dbNB, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbPointsSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);

                VPLinearAlg sVP = new VPLinearAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes, BFSRes, DFSBestRes, BFSBestRes, DFSBDCRes, BFSBDCRes, DFSGLORes,
                                BFSGLORes;
                ArrayList<PriorityQueue<NN>> DFSLRURes, BFSLRURes, DFSLFURes, BFSLFURes, DFSFIFORes, BFSFIFORes,
                                BFSGlobalRes;

                // DFSRes = sVP.DFS(k, false);
                // ArrayList<PriorityQueue<NN>> DFSRes1 = sVP.DFS(k, true);
                // checkKNN(DFSRes, DFSRes1);
                // BFSRes = sVP.BFS(k, false);
                // ArrayList<PriorityQueue<NN>> BFSRes1 = sVP.BFS(k, true);
                // checkKNN(BFSRes, BFSRes1);

                // DFSBestRes = sVP.bestCache(factor, updateThreshold, k,
                // false);
                // BFSBestRes = sVP.bestCache(factor, updateThreshold, k,
                // true);

                // DFSGLORes = sVP.GLOCache(cacheSize, updateThreshold, k,
                // false);
                BFSGLORes = sVP.GLOCache(cacheSize, updateThreshold, k,
                                true);

                BFSGlobalRes = sVP.GlobalCache(cacheSize, updateThreshold, k, true);

                // DFSBDCRes = sVP.BDCCache(cacheSize, updateThreshold, k,
                // false);
                BFSBDCRes = sVP.BDCCache(cacheSize, updateThreshold, k,
                                true);

                // DFSLRURes = sVP.LRUCache(cacheSize, updateThreshold, k,
                // false);
                BFSLRURes = sVP.LRUCache(cacheSize, updateThreshold, k, true);

                // DFSFIFORes = sVP.FIFOCache(cacheSize, updateThreshold, k,
                // false);
                BFSFIFORes = sVP.FIFOCache(cacheSize, updateThreshold, k,
                                true);

                // DFSLFURes = sVP.LFUCache(cacheSize, updateThreshold, k,
                // false);
                BFSLFURes = sVP.LFUCache(cacheSize, updateThreshold, k,
                                true);

                // checkKNN(DFSFIFORes, DFSBDCRes);
                // checkKNN(BFSFIFORes, BFSBDCRes);
                // checkKNN(BFSFIFORes, BFSGLORes);
                // checkKNN(BFSLRURes, BFSLFURes);
                // checkKNN(DFSBestRes, BFSFIFORes);
                // checkKNN(DFSLRURes, DFSFIFORes);
                // checkKNN(DFSRes, DFSLFURes);
                // checkKNN(DFSBestRes, BFSLRURes);
                // checkKNN(DFSRes, BFSRes);
                // checkKNN(DFSBestRes, BFSBestRes);
                // checkKNN(DFSBestRes, DFSLRURes);

                System.out.println("time cost: " + (System.currentTimeMillis() - t1));
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
                double factor = Settings.factor;
                // load data
                loadData(qNB, dbNB, dim);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbPointsSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);

                VPLinearAlg sVP = new VPLinearAlg(queryPoints, dbPoints, Settings.sampleNB,
                                Settings.bucketSize);
                VPGraphAlg sVP1 = new VPGraphAlg(queryPoints, dbPoints, Settings.sampleNB,
                                Settings.bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes, BFSRes, DFSBestRes, BFSBestRes, DFSBDCRes, BFSBDCRes, DFSGLORes,
                                BFSGLORes;
                ArrayList<PriorityQueue<NN>> DFSLRURes, BFSLRURes, DFSLFURes, BFSLFURes, DFSFIFORes, BFSFIFORes;

                BFSFIFORes = sVP.FIFOCache(Settings.cacheSize, updateThreshold, k,
                                true);
                // BFSFIFORes = sVP.FIFOCacheInObjectLevel(Settings.cacheSize, updateThreshold,
                // k,
                // true);
                // DFSRes = sVP.DFS(k, false);
                // ArrayList<PriorityQueue<NN>> DFSRes1 = sVP.DFS(k, true);
                // checkKNN(DFSRes, DFSRes1);
                // BFSRes = sVP.BFS(k, false);
                // ArrayList<PriorityQueue<NN>> BFSRes1 = sVP.BFS(k, true);
                // checkKNN(BFSRes, BFSRes1);

                // DFSBestRes = sVP.bestCache(factor, updateThreshold, k,
                // false);
                BFSBestRes = sVP.bestCache(factor, updateThreshold, k,
                                true);
                checkKNN(BFSBestRes, BFSFIFORes);

                // DFSGLORes = sVP.GLOCache(Settings.cacheSize, updateThreshold, k,
                // false);
                //

                // DFSBDCRes = sVP.BDCCache(Settings.cacheSize, updateThreshold, k,
                // false);
                // BFSBDCRes = sVP.BDCCache(Settings.cacheSize, updateThreshold, k,
                // true);

                // DFSLRURes = sVP.LRUCache(Settings.cacheSize, updateThreshold, k,
                // false);

                // BFSLRURes = sVP.LRUCache(Settings.cacheSize, updateThreshold, k,
                // true);
                // DFSLRURes = sVP1.LRUCache(Settings.cacheSize, updateThreshold, k,
                // true);
                BFSGLORes = sVP.GLOCache(10000, updateThreshold, k,
                                true);
                ArrayList<PriorityQueue<NN>> BFSGlobalRes = sVP.GlobalCache(Settings.cacheSize, updateThreshold,
                                k,
                                true);

                // DFSFIFORes = sVP.FIFOCache(Settings.cacheSize, updateThreshold, k,
                // true);

                // checkKNN(DFSFIFORes, BFSFIFORes);
                // checkKNN(DFSFIFORes, BFSBestRes);

                // DFSLFURes = sVP.LFUCache(Settings.cacheSize, updateThreshold, k,
                // false);
                // BFSLFURes = sVP.LFUCache(Settings.cacheSize, updateThreshold, k,
                // true);

                // checkKNN(DFSFIFORes, DFSBDCRes);
                // checkKNN(BFSFIFORes, BFSBDCRes);
                // checkKNN(BFSFIFORes, BFSGLORes);
                // checkKNN(BFSLRURes, BFSLFURes);
                // checkKNN(DFSBestRes, BFSFIFORes);
                // checkKNN(DFSLRURes, DFSFIFORes);
                // checkKNN(DFSRes, DFSLFURes);

                // checkKNN(DFSRes, BFSRes);
                // checkKNN(DFSBestRes, BFSBestRes);
                // checkKNN(DFSBestRes, DFSLRURes);

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
                VPLinearAlg VPAlg = new VPLinearAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                // DFS
                // VPAlg.DFS(k, true);
                // BFS
                VPAlg.BFS(k, false);
                // checkKNN(DFSRes, BFSRes);
                // VPAlg.bestCache(factor, updateThreshold, k, false);
                VPAlg.bestCache(factor, updateThreshold, k, true);
                // VPAlg.LRUCache(cacheSize, updateThreshold, k, false);
                VPAlg.LRUCache(cacheSize, updateThreshold, k, true);
                // VPAlg.LFUCache(cacheSize, updateThreshold, k, false);
                VPAlg.LFUCache(cacheSize, updateThreshold, k, true);
                // VPAlg.FIFOCache(cacheSize, updateThreshold, k, false);
                VPAlg.FIFOCache(cacheSize, updateThreshold, k, true);

                // VPAlg.GLOCache(cacheSize, updateThreshold, k, false);
                VPAlg.GLOCache(cacheSize, updateThreshold, k, true);
                // VPAlg.GlobalCache(cacheSize, updateThreshold, k, false);
                VPAlg.GlobalCache(cacheSize, updateThreshold, k, true);
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
                t.test();
                // t.cacheTest(Settings.updateThreshold);
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
