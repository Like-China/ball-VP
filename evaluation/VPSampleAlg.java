package evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import VPTree.*;
import cacheIndex.KMeans;
import cacheIndex.LRUCache;
import cacheIndex.Point;
import Distance.*;

public class VPSampleAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public double[][] qData;
    public double[][] dbData;
    public DistanceFunction distFunction;
    // index construction time / filtering time
    public long cTime = 0;

    // the number of node accesses (Deep-First/Best-first + Hier/recursion + Cache)
    public int nodeAccess = 0;
    public int DFNodeAccess = 0;
    public int BFHierNodeAccess = 0;
    public int BFRecuNodeAccess = 0;
    public int DFCacheNodeAccess = 0;
    public int BFHierCacheNodeAccess = 0;
    public int BFRecuCacheNodeAccess = 0;
    // the number of calculation time
    public int calcCount = 0;
    public int DFCalcCount = 0;
    public int BFHierCalcCount = 0;
    public int BFRecuCalcCount = 0;
    public int DFCacheCalcCount = 0;
    public int BFHierCacheCalcCount = 0;
    public int BFRecuCacheCalcCount = 0;
    // the search time
    public double fTime = 0;
    public double DFTime = 0;
    public double BFHierTime = 0;
    public double BFRecuTime = 0;
    public double DFCacheTime = 0;
    public double BFHierCacheTime = 0;
    public double BFRecuCacheTime = 0;

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp;
    long t1, t2;

    public VPSampleAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int sampleNB,
            DistanceFunction distFunction, int bucketSize) {
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.sampleNB = sampleNB;
        this.distFunction = distFunction;
        t1 = System.currentTimeMillis();
        // VP-tree construction
        vp = new VPTreeBySample(this.dbData, distFunction, sampleNB, bucketSize);
        t2 = System.currentTimeMillis();
        cTime = t2 - t1;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> nnSearch() {
        t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchOneNN(q));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.length;
        info = String.format(
                "****VPTree\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.3fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        return res;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * if cachekNN = Double.maxValue, then the cache knowledge is not employed.
     * 
     * @return all candidate pairs
     */
    public ArrayList<PriorityQueue<NN>> searchkNNDFS(int k) {
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNNDFS(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        DFTime = t2 - t1;
        int n = qData.length;
        info = String.format(
                "****VPTree DFS-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, DFTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        DFNodeAccess = vp.nodeAccess / n;
        DFCalcCount = vp.calcCount / n;
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        // System.out.println(vp.heapUpdatecost / 10e6);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> searchkNNBestFirst(int k) {
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNNBestFirst(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();

        int n = qData.length;
        String mode = "Recursion";
        double ftime = t2 - t1;
        info = String.format(
                "****VPTree Best-First-kNN (%s)\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                mode, cTime, ftime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        BFHierTime = t2 - t1;
        BFHierNodeAccess = vp.nodeAccess / n;
        BFHierCalcCount = vp.calcCount / n;

        BFRecuTime = t2 - t1;
        BFRecuNodeAccess = vp.nodeAccess / n;
        BFRecuCalcCount = vp.calcCount / n;

        vp.nodeAccess = 0;
        vp.calcCount = 0;
        // System.out.println(vp.heapUpdatecost / 10e6);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> LRUCache(int cacheSize, int k, boolean useBFS) {
        int n = qData.length;
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a LRUCache
        LRUCache LRU = new LRUCache(cacheSize);
        for (int i = 0; i < cacheSize; i++) {
            double[] q = qData[i];
            Point p = new Point(i, q);
            double maxKdist = Double.MAX_VALUE;
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            // update res
            res.add(nns);
            // update cache
            p.setNNs(nns);
            LRU.add(p);
        }

        // begin to get NN using LRUCache
        // If the actual maxKDist is 2x less than the cached maxDist, add
        // ineffectiveCount by 1
        int ineffectiveCount = 0;
        for (int i = cacheSize; i < qData.length; i++) {
            double[] q = qData[i];
            Point p = new Point(i, q);
            double maxKdist = LRU.find(p);
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query >= 3) {
                ineffectiveCount += 1;
                p.setNNs(nns);
                LRU.add(p);
            }
        }
        System.out.println("Ineffective count: " + ineffectiveCount);
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BFRecuCacheTime = t2 - t1;
            BFRecuCacheNodeAccess = vp.nodeAccess / n;
            BFRecuCacheCalcCount = vp.calcCount / n;
            info = String.format(
                    "****VPTree BFS--LRU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                    cTime, BFRecuCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        } else {
            DFCacheTime = t2 - t1;
            DFCacheNodeAccess = vp.nodeAccess / n;
            DFCacheCalcCount = vp.calcCount / n;
            info = String.format(
                    "****VPTree DFS--LRU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                    cTime, DFCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        }
        System.out.println(info);

        return res;
    }

    // cache using the best prior knowledge
    public ArrayList<PriorityQueue<NN>> bestCache(double factor, int k, boolean useBFS) {
        int n = qData.length;
        // Initilize best caches for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        int cacheK = (int) (k * factor);
        for (double[] q : qData) {
            cache.add(vp.searchkNNDFS(q, cacheK, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();

        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // If the actual maxKDist is 2x less than the cached maxDist, add
        // ineffectiveCount by 1
        int ineffectiveCount = 0;
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query >= 2) {
                ineffectiveCount += 1;
            }
        }
        System.out.println("Ineffective count: " + ineffectiveCount);
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BFRecuCacheTime = t2 - t1;
            BFRecuCacheNodeAccess = vp.nodeAccess / n;
            BFRecuCacheCalcCount = vp.calcCount / n;
            info = String.format(
                    "****VPTree BFS--Best Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                    cTime, BFRecuCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        } else {
            DFCacheTime = t2 - t1;
            DFCacheNodeAccess = vp.nodeAccess / n;
            DFCacheCalcCount = vp.calcCount / n;
            info = String.format(
                    "****VPTree DFS--Best Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                    cTime, DFCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        }
        System.out.println(info);
        return res;
    }

    public ArrayList<double[]> rangeSearch(double range) {

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            res.addAll(vp.searchRange(q, range));
        }
        t2 = System.currentTimeMillis();
        System.out.println("VP range-Search result size: " + res.size());
        return res;
    }

}