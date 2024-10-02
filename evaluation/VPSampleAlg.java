package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;

import VPTree.*;
import cacheIndex.FIFO;
import cacheIndex.LFU;
import cacheIndex.LRU;
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
    public int DF_NodeAccess = 0;
    public int BF_NodeAccess = 0;
    public int DF_FIFO_NodeAccess = 0;
    public int BF_FIFO_NodeAccess = 0;
    public int DF_LRU_NodeAccess = 0;
    public int BF_LRU_NodeAccess = 0;
    public int DF_LFU_NodeAccess = 0;
    public int BF_LFU_NodeAccess = 0;
    public int DF_Best_NodeAccess = 0;
    public int BF_Best_NodeAccess = 0;
    // the number of calculation time
    public int DF_CalcCount = 0;
    public int BF_CalcCount = 0;
    public int DF_FIFO_CalcCount = 0;
    public int BF_FIFO_CalcCount = 0;
    public int DF_LRU_CalcCount = 0;
    public int BF_LRU_CalcCount = 0;
    public int DF_LFU_CalcCount = 0;
    public int BF_LFU_CalcCount = 0;
    public int DF_Best_CalcCount = 0;
    public int BF_Best_CalcCount = 0;
    // the search time
    public double DF_Time = 0;
    public double BF_Time = 0;
    public double DF_FIFO_Time = 0;
    public double BF_FIFO_Time = 0;
    public double DF_LRU_Time = 0;
    public double BF_LRU_Time = 0;
    public double DF_LFU_Time = 0;
    public double BF_LFU_Time = 0;
    public double DF_Best_Time = 0;
    public double BF_Best_Time = 0;

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp = null;
    long t1, t2;

    public VPSampleAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int sampleNB,
            DistanceFunction distFunction, int bucketSize) {
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.sampleNB = sampleNB;
        this.distFunction = distFunction;
        t1 = System.currentTimeMillis();
        // tree construction
        vp = new VPTreeBySample(this.dbData, distFunction, sampleNB, bucketSize);
        t2 = System.currentTimeMillis();
        cTime = t2 - t1;
    }

    public ArrayList<PriorityQueue<NN>> DFS(int k) {
        vp.init();
        int n = qData.length;
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNNDFS(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        DF_Time = t2 - t1;
        DF_NodeAccess = vp.nodeAccess / n;
        DF_CalcCount = vp.calcCount / n;
        info = String.format(
                "\n****VPTree DFS-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, DF_Time / n, DF_NodeAccess, DF_CalcCount);
        System.out.println(info);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> BFS(int k) {
        vp.init();
        int n = qData.length;
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNNBestFirst(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        BF_Time = t2 - t1;
        BF_NodeAccess = vp.nodeAccess / n;
        BF_CalcCount = vp.calcCount / n;
        info = String.format(
                "\n****VPTree BFS-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, BF_Time / n, BF_NodeAccess, BF_CalcCount);
        System.out.println(info);

        return res;
    }

    public ArrayList<PriorityQueue<NN>> LRUCache(int cacheSize, double updateThreshold, int k, boolean useBFS) {
        vp.init();
        int n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a LRUCache
        LRU lru = new LRU(cacheSize);
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
            // when the size of LRU is not full, just fill it
            p.setNNs(nns);
            lru.update(p);
        }

        // begin to get NN using LRUCache
        int hitCount = 0;
        assert lru.size() == cacheSize;
        for (int i = cacheSize; i < n; i++) {
            double[] q = qData[i];
            Point p = new Point(i, q);
            double maxKdist = lru.find(p);
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            res.add(nns);
            // update cache
            if (maxKdist / nns.peek().dist2query >= updateThreshold) {
                p.setNNs(nns);
            } else {
                hitCount += 1;
                p = lru.minPP;
            }
            lru.update(p);
        }
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BF_LRU_Time = t2 - t1;
            BF_LRU_NodeAccess = vp.nodeAccess / n;
            BF_LRU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree BFS--LRU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, BF_LRU_Time / n, BF_LRU_NodeAccess, BF_LRU_CalcCount, hitCount);
        } else {
            DF_LRU_Time = t2 - t1;
            DF_LRU_NodeAccess = vp.nodeAccess / n;
            DF_LRU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree DFS--LRU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, DF_LRU_Time / n, DF_LRU_NodeAccess, DF_LRU_CalcCount, hitCount);
        }
        System.out.println(info);

        return res;
    }

    public ArrayList<PriorityQueue<NN>> FIFOCache(int cacheSize, double updateThreshold, int k, boolean useBFS) {
        vp.init();
        int n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a LRUCache
        FIFO lru = new FIFO(cacheSize);
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
            // when the size of LRU is not full, just fill it
            p.setNNs(nns);
            lru.update(p);
        }

        // begin to get NN using LRUCache
        int hitCount = 0;
        assert lru.size() == cacheSize;
        for (int i = cacheSize; i < n; i++) {
            double[] q = qData[i];
            Point p = new Point(i, q);
            double maxKdist = lru.find(p);
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            res.add(nns);
            // update cache
            if (maxKdist / nns.peek().dist2query >= updateThreshold) {
                p.setNNs(nns);
            } else {
                hitCount += 1;
                p = lru.minPP;
            }
            lru.update(p);
        }
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BF_FIFO_Time = t2 - t1;
            BF_FIFO_NodeAccess = vp.nodeAccess / n;
            BF_FIFO_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree BFS--FIFO Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, BF_FIFO_Time / n, BF_FIFO_NodeAccess, BF_FIFO_CalcCount, hitCount);
        } else {
            DF_FIFO_Time = t2 - t1;
            DF_FIFO_NodeAccess = vp.nodeAccess / n;
            DF_FIFO_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree DFS--FIFO Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, DF_FIFO_Time / n, DF_FIFO_NodeAccess, DF_FIFO_CalcCount, hitCount);
        }
        System.out.println(info);

        return res;
    }

    public ArrayList<PriorityQueue<NN>> LFUCache(int cacheSize, double updateThreshold, int k, boolean useBFS) {
        vp.init();
        int n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a HQFCache
        LFU hqf = new LFU(cacheSize);
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
            // when the size of LRU is not full, just fill it
            p.setNNs(nns);
            hqf.update(p);
        }

        // begin to get NN using HQFCache
        int hitCount = 0;
        assert hqf.size() == cacheSize;
        for (int i = cacheSize; i < n; i++) {
            double[] q = qData[i];
            Point p = new Point(i, q);
            double maxKdist = hqf.find(p);
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBestFirst(q, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(q, k, maxKdist);
            }
            res.add(nns);
            // update cache
            if (maxKdist / nns.peek().dist2query >= updateThreshold) {
                p.setNNs(nns);
                hqf.update(p);
            } else {
                hitCount += 1;
                p = hqf.minPP;
                hqf.update(p);
            }

        }
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BF_LFU_Time = t2 - t1;
            BF_LFU_NodeAccess = vp.nodeAccess / n;
            BF_LFU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree BFS--LFU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, BF_LFU_Time / n, BF_LFU_NodeAccess, BF_LFU_CalcCount, hitCount);
        } else {
            DF_LFU_Time = t2 - t1;
            DF_LFU_NodeAccess = vp.nodeAccess / n;
            DF_LFU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree DFS--LFU Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, DF_LFU_Time / n, DF_LFU_NodeAccess, DF_LFU_CalcCount, hitCount);
        }
        System.out.println(info);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> bestCache(double factor, double updateThreshold, int k, boolean useBFS) {
        vp.init();
        int n = qData.length;
        // Initilize best caches for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        int cacheK = (int) (k * factor);
        for (double[] q : qData) {
            cache.add(vp.searchkNNDFS(q, cacheK, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        vp.init();

        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // If the actual maxKDist is 2x less than the cached maxDist, add
        // hitCount by 1
        int hitCount = 0;
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
            if (maxKdist / nns.peek().dist2query < updateThreshold) {
                hitCount += 1;
            }
        }
        t2 = System.currentTimeMillis();

        if (useBFS) {
            BF_Best_Time = t2 - t1;
            BF_Best_NodeAccess = vp.nodeAccess / n;
            BF_Best_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree BFS--Best Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, BF_Best_Time / n, BF_Best_NodeAccess, BF_Best_CalcCount, hitCount);
        } else {
            DF_Best_Time = t2 - t1;
            DF_Best_NodeAccess = vp.nodeAccess / n;
            DF_Best_CalcCount = vp.calcCount / n;
            info = String.format(
                    "\n****VPTree DFS--Best Caching-kNN\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, DF_Best_Time / n, DF_Best_NodeAccess, DF_Best_CalcCount, hitCount);
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