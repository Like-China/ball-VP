package VPTree;

import java.io.IOException;
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
                Loader l;
                try {
                        l = new Loader(Settings.data);
                        l.loadData(qSize, dbPointsSize, dim);
                        this.queryPoints = l.query.toArray(new Point[l.query.size()]);
                        this.dbPoints = l.db.toArray(new Point[l.db.size()]);
                        // use KMeans to get clusters
                        boolean isUseKmeans = Settings.isUseKmeans;
                        if (isUseKmeans) {
                                ArrayList<Point> points = new ArrayList();
                                this.queryPoints = new Point[l.query.size()];
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
                ArrayList<PriorityQueue<NN>> DFSRes = sVP.DFS_BFS("#DFS", k, false, updateThreshold, false);
                Util.checkKNN(BFRes, DFSRes);

                /*
                 * 2. Best-First test (To test the VP-tree kNNs results of BFS and DFS based
                 * solutions)
                 */
                ArrayList<PriorityQueue<NN>> BFSRes = sVP.DFS_BFS("#BFS", k, false, updateThreshold, true);
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
                                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d \t factor: %f \t cacheSize: %d     ",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize, factor,
                                cacheSize);
                System.out.println(setInfo);

                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFSRes, BFSRes, DFSBestRes, BFSBestRes, DFSBDCRes, BFSBDCRes, DFSGLORes,
                                BFSGLORes;
                ArrayList<PriorityQueue<NN>> DFSLRURes, BFSLRURes, DFSLFURes, BFSLFURes, DFSFIFORes, BFSFIFORes,
                                BFSGlobalRes;

                DFSRes = sVP.DFS_BFS("#DFS", k, false, updateThreshold, false);
                // ArrayList<PriorityQueue<NN>> DFSRes1 = sVP.DFS_BFS("#DFS", k, true,
                // updateThreshold, false);
                // // Util.checkKNN(DFSRes, DFSRes1);
                // BFSBDCRes = sVP.DFS_BFS("#BFS", k, false, updateThreshold, true);
                // ArrayList<PriorityQueue<NN>> BFSRes1 = sVP.DFS_BFS("#BFS", k, true,
                // updateThreshold, true);
                // Util.checkKNN(BFSBDCRes, BFSRes1);

                // BFSBDCRes = sVP.bestCache("Best-BFS", factor, updateThreshold, k, true);

                // BFSBDCRes = sVP.queryLinear_Cache("Global", cacheSize, updateThreshold, k,
                // true);
                // DFSLRURes = sVP.queryLinear_To_ObjectLinear_Cache("Global", cacheSize,
                // updateThreshold, k,
                // true);
                // Util.checkKNN(BFSBDCRes, DFSLRURes);

                // BFSBDCRes = sVP.queryLinear_Cache("BDC", cacheSize, updateThreshold, k,
                // true);
                System.out.println("---------------------------------------------------------------------------");

                // DFSLRURes = sVP.queryLinear_Cache("BDC-BFS", cacheSize, updateThreshold, k,
                // true);
                // sVP.queryLinear_To_ObjectLinear_Cache("BDC-BFS", cacheSize, updateThreshold,
                // k, true);
                // BFSBDCRes = sVP.ObjectLinear_Cache("BDC-BFS", cacheSize, updateThreshold, k,
                // true);
                // sVP.queryLinear_Cache("FIFO-BFS", cacheSize, updateThreshold, k, true);
                // sVP.queryLinear_To_ObjectLinear_Cache("FIFO-BFS", cacheSize,
                // updateThreshold, k, true);
                // sVP.ObjectLinear_Cache("FIFO-BFS", cacheSize, updateThreshold, k,
                // true);
                // DFSLRURes = sVP.ObjectHNSW_Cache("FIFO-BFS", cacheSize, updateThreshold, k,
                // true);
                // DFSLRURes = sVP.ObjectHNSW_Cache("Global", cacheSize, updateThreshold, k,
                // true);
                DFSLRURes = sVP.ObjectLinear_Cache("Global", cacheSize, updateThreshold, k,
                                true);
                System.out.println("---------------------------------------------------------------------------");
                // Util.checkKNN(BFSBDCRes, DFSLRURes);
                System.out.println("cache test-time cost: " + (System.currentTimeMillis() - t1));
                System.exit(0);

                sVP.queryLinear_Cache("LRU-BFS", cacheSize, updateThreshold, k, true);
                BFSBDCRes = sVP.queryLinear_To_ObjectLinear_Cache("LRU-BFS", cacheSize,
                                updateThreshold, k, true);
                DFSLRURes = sVP.ObjectLinear_Cache("LRU-BFS", cacheSize, updateThreshold, k,
                                true);
                sVP.ObjectHNSW_Cache("LRU-BFS", cacheSize, updateThreshold, k, true);
                Util.checkKNN(BFSBDCRes, DFSLRURes);
                System.out.println("---------------------------------------------------------------------------");

                sVP.queryLinear_Cache("FIFO-BFS", cacheSize, updateThreshold, k, true);
                sVP.queryLinear_To_ObjectLinear_Cache("FIFO-BFS", cacheSize,
                                updateThreshold, k, true);
                sVP.ObjectLinear_Cache("FIFO-BFS", cacheSize, updateThreshold, k,
                                true);
                sVP.ObjectHNSW_Cache("FIFO-BFS", cacheSize, updateThreshold, k, true);
                System.out.println("---------------------------------------------------------------------------");

                sVP.queryLinear_Cache("LFU-BFS", cacheSize, updateThreshold, k, true);
                BFSBDCRes = sVP.queryLinear_To_ObjectLinear_Cache("LFU-BFS", cacheSize,
                                updateThreshold, k, true);
                DFSLRURes = sVP.ObjectLinear_Cache("LFU-BFS", cacheSize, updateThreshold, k,
                                true);
                sVP.ObjectHNSW_Cache("LFU-BFS", cacheSize, updateThreshold, k, true);
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
                VPAlg.DFS_BFS("#BFS", k, false, updateThreshold, true);
                // Util.checkKNN(DFSRes, BFSRes);
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
