package VPTree;

import java.io.IOException;
import java.util.*;
import evaluation.*;
import utils.*;

public class Test {

        public Point[] dbPoints = null;
        public Point[] queryPoints = null;

        public static void checkKNN(ArrayList<PriorityQueue<NN>> res1, ArrayList<PriorityQueue<NN>> res2) {
                assert !res1.isEmpty() && !res2.isEmpty() && res1.size() == res2.size();
                for (int i = 0; i < res1.size(); i++) {
                        PriorityQueue<NN> nns1 = new PriorityQueue<>(res1.get(i));
                        PriorityQueue<NN> nns2 = new PriorityQueue<>(res2.get(i));
                        while (!nns1.isEmpty()) {
                                double d1 = nns1.poll().dist2query;
                                double d2 = nns2.poll().dist2query;
                                assert d1 == d2 : i + "/" + d1 + "/" + d2;
                        }
                }
        }

        public void loadData(int qSize, int dbSize, int dim) {
                long t1 = System.currentTimeMillis();
                Loader l;
                try {
                        l = new Loader(Settings.data);
                        l.loadData(qSize, dbSize, dim);
                        this.queryPoints = l.query;
                        this.dbPoints = l.db;
                        // use KMeans to get clusters
                        if (Settings.isUseKmeans) {
                                ArrayList<Point> points = new ArrayList();
                                int maxIterations = 100;
                                List<List<Point>> clusters = KMeans.kMeansCluster(l.query, 5, maxIterations);
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

        public void testVP() {
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
                                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize);
                System.out.println(setInfo);

                // Brute-Force
                BFAlg bf = new BFAlg(queryPoints, dbPoints);
                ArrayList<PriorityQueue<NN>> BFRes = bf.kNNSearch(k);
                // VP with directly deep first search
                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                ArrayList<PriorityQueue<NN>> DFS_NoCache_Res = sVP.DFS_BFS("#DFS", k, false, updateThreshold, false);
                checkKNN(BFRes, DFS_NoCache_Res);
                // VP with best-first strategy
                ArrayList<PriorityQueue<NN>> BFS_NoCache_Res = sVP.DFS_BFS("#BFS", k, false, updateThreshold, true);
                checkKNN(DFS_NoCache_Res, BFS_NoCache_Res);
                System.out.println("Test Done!");
        }

        public void testCache(double updateThreshold) {
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
                VPAlg sVP = new VPAlg(queryPoints, dbPoints, sampleNB, bucketSize);
                String setInfo = String.format(
                                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size: %d \tfactor: %.2f \tcache size: %d     ",
                                data, queryPoints.length, dbPoints.length, k, dim, sampleNB, bucketSize, factor,
                                cacheSize);
                System.out.println(setInfo);

                ArrayList<PriorityQueue<NN>> DFS_NoCache_Res, BFS_NoCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_BestCache_Res, BFS_BestCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_FIFOCache_Res, BFS_FIFOCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_LFUCache_Res, BFS_LFUCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_LRUCache_Res, BFS_LRUCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_BDCCache_Res, BFS_BDCCache_Res;
                ArrayList<PriorityQueue<NN>> DFS_GLOCache_Res, BFS_GLOCache_Res;

                DFS_NoCache_Res = sVP.DFS_BFS("#DFS", k, false, updateThreshold, false);
                // ArrayList<PriorityQueue<NN>> res1 = sVP.DFS_BFS("#DFS", k, true,
                // updateThreshold, false);
                // checkKNN(DFS_NoCache_Res, res1);
                // BFS_NoCache_Res = sVP.DFS_BFS("#BFS", k, false, updateThreshold, true);
                // checkKNN(DFS_NoCache_Res, BFS_NoCache_Res);
                // res1 = sVP.DFS_BFS("#BFS", k, true, updateThreshold, true);
                // checkKNN(BFS_NoCache_Res, res1);

                // DFS_BestCache_Res = sVP.bestCache("Best-DFS", 1, updateThreshold, k,
                // false);
                // checkKNN(DFS_NoCache_Res, DFS_BestCache_Res);
                BFS_BestCache_Res = sVP.bestCache("Best-BFS", factor, updateThreshold, k,
                                true);
                checkKNN(DFS_NoCache_Res, BFS_BestCache_Res);

                /*
                 * queryLinear_Cache1/queryLinear_To_ObjectLinear_Cache1
                 * ObjectLinear_Cache1/ObjectHNSW_Cache1
                 */
                // DFS_FIFOCache_Res = sVP.ObjectHNSW_Cache("FIFO-DFS",
                // cacheSize,
                // updateThreshold, k, false);
                // checkKNN(DFS_NoCache_Res, DFS_FIFOCache_Res);
                BFS_FIFOCache_Res = sVP.ObjectHNSW_Cache("FIFO-BFS",
                                cacheSize,
                                updateThreshold, k, true);
                checkKNN(DFS_NoCache_Res, BFS_FIFOCache_Res);

                // DFS_LRUCache_Res = sVP.ObjectHNSW_Cache("LRU-DFS",
                // cacheSize,
                // updateThreshold, k, false);
                // checkKNN(DFS_NoCache_Res, DFS_LRUCache_Res);
                // BFS_LRUCache_Res = sVP.ObjectHNSW_Cache("LRU-BFS",
                // cacheSize,
                // updateThreshold, k, true);
                // checkKNN(DFS_NoCache_Res, BFS_LRUCache_Res);

                // DFS_LFUCache_Res = sVP.ObjectHNSW_Cache("LFU-DFS",
                // cacheSize,
                // updateThreshold, k, false);
                // checkKNN(DFS_NoCache_Res, DFS_LFUCache_Res);
                // BFS_LFUCache_Res = sVP.ObjectHNSW_Cache("LFU-BFS",
                // cacheSize,
                // updateThreshold, k, true);
                // checkKNN(DFS_NoCache_Res, BFS_LFUCache_Res);

                // DFS_BDCCache_Res = sVP.ObjectHNSW_Cache("BDC-DFS", cacheSize,
                // updateThreshold, k,
                // false);
                // checkKNN(DFS_NoCache_Res, DFS_BDCCache_Res);
                BFS_BDCCache_Res = sVP.ObjectHNSW_Cache("BDC-BFS",
                                cacheSize,
                                updateThreshold, k, true);
                checkKNN(DFS_NoCache_Res, BFS_BDCCache_Res);

                DFS_GLOCache_Res = sVP.ObjectHNSW_Cache("GLO-DFS",
                                cacheSize,
                                updateThreshold, k, false);
                // checkKNN(DFS_NoCache_Res, DFS_GLOCache_Res);
                // BFS_GLOCache_Res = sVP.ObjectHNSW_Cache("GLO-BFS",
                // cacheSize,
                // updateThreshold, k, true);
                // checkKNN(DFS_NoCache_Res, BFS_GLOCache_Res);
                System.out.println("---------------------------------------------------------------------------");
                System.out.println("---------------------------------------------------------------------------");
                System.out.println("Test Done! Cache test-time cost: " + (System.currentTimeMillis() - t1) + " ms");
        }

        public static void main(String[] args) {
                Test t = new Test();
                t.testCache(Settings.updateThreshold);
        }
}
