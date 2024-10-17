package evaluation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;
import VPTree.*;
import graphcache.KGraph;
import linearcache.LRU;
import utils.NN;
import utils.Point;

public class VPGraphAlg {
    /// query, database vectors
    public Point[] qData;
    public Point[] dbData;
    // index construction time
    public long cTime = 0;
    // the number of node accesses (Deep-First/Best-first + Hier/recursion + Cache)
    public long DF_NodeAccess = 0;
    public long BF_NodeAccess = 0;
    public long DF_FIFO_NodeAccess = 0;
    public long BF_FIFO_NodeAccess = 0;
    public long DF_LRU_NodeAccess = 0;
    public long BF_LRU_NodeAccess = 0;
    public long DF_LFU_NodeAccess = 0;
    public long BF_LFU_NodeAccess = 0;
    public long DF_BDC_NodeAccess = 0;
    public long BF_BDC_NodeAccess = 0;
    public long DF_GLO_NodeAccess = 0;
    public long BF_GLO_NodeAccess = 0;
    public long DF_Best_NodeAccess = 0;
    public long BF_Best_NodeAccess = 0;
    // the number of calculation time
    public long DF_CalcCount = 0;
    public long BF_CalcCount = 0;
    public long DF_FIFO_CalcCount = 0;
    public long BF_FIFO_CalcCount = 0;
    public long DF_LRU_CalcCount = 0;
    public long BF_LRU_CalcCount = 0;
    public long DF_LFU_CalcCount = 0;
    public long BF_LFU_CalcCount = 0;
    public long DF_BDC_CalcCount = 0;
    public long BF_BDC_CalcCount = 0;
    public long DF_GLO_CalcCount = 0;
    public long BF_GLO_CalcCount = 0;
    public long DF_Best_CalcCount = 0;
    public long BF_Best_CalcCount = 0;
    // the search time
    public double DF_Time = 0;
    public double BF_Time = 0;
    public double DF_FIFO_Time = 0;
    public double BF_FIFO_Time = 0;
    public double DF_LRU_Time = 0;
    public double BF_LRU_Time = 0;
    public double DF_LFU_Time = 0;
    public double BF_LFU_Time = 0;
    public double DF_BDC_Time = 0;
    public double BF_BDC_Time = 0;
    public double DF_GLO_Time = 0;
    public double BF_GLO_Time = 0;
    public double DF_Best_Time = 0;
    public double BF_Best_Time = 0;

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp = null;
    long t1, t2;

    public VPGraphAlg(Point[] qData, Point[] dbData, int sampleNB, int bucketSize) {
        this.qData = qData;
        this.dbData = dbData;
        this.sampleNB = sampleNB;
        // tree construction
        t1 = System.currentTimeMillis();
        vp = new VPTreeBySample(dbData, sampleNB, bucketSize);
        t2 = System.currentTimeMillis();
        cTime = t2 - t1;
    }

    public ArrayList<PriorityQueue<NN>> LRUCache(int cacheSize, double updateThreshold, int k, boolean useBFS) {
        vp.init();

        double updateTime = 0;
        double graphSearchTime = 0;

        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        HashSet<Point> cachedPoints = new HashSet<>();
        // initial a LRUCache
        KGraph kGraph = new KGraph();
        // initial a LRUCache
        LRU lru = new LRU(cacheSize);
        int hitCount = 0;
        t1 = System.currentTimeMillis();
        for (int i = 0; i < cacheSize; i++) {
            Point query = qData[i];
            double maxKdist = lru.find(query);
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(query, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(query, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            // update res
            res.add(nns);
            // update cachedPoint
            for (NN nn : nns) {
                cachedPoints.add(nn.point);
            }
            // for (NN nn : nns) {
            // kGraph.points.add(nn.point);
            // }
            // for (NN nn : nns) {
            // kGraph.updateGraph(nn.point, k);
            // }
            // update cache
            query.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            lru.cachedPoints.add(query);
        }
        assert lru.size() == cacheSize : lru.size() + "/" + cacheSize;
        long t3 = System.currentTimeMillis();
        kGraph.initGraph(new ArrayList<>(cachedPoints), k);
        System.out.println("\nInitial time cost: " + (System.currentTimeMillis() - t3));
        System.out.println("initial KGraph size:" + kGraph.size());

        int count = 0;

        for (int i = cacheSize; i < n; i++) {
            Point query = qData[i];
            double maxKdist = lru.find(query);
            Point minPP = lru.minPP;
            Point currentPoint = lru.minNN.point;
            // System.out.println(i);
            // System.out.println("Linear cache init distacne: " + maxKdist);
            // System.out.println("Initial Point: " + minPP.id + "->" + currentPoint.id);
            long startSearch = System.currentTimeMillis();
            PriorityQueue<NN> pairs = kGraph.findKNN(currentPoint, query, k);
            long endSearch = System.currentTimeMillis();
            graphSearchTime += (endSearch - startSearch);
            double maxKdist1 = pairs.peek().dist2query;
            if (maxKdist1 <= maxKdist) {
                count++;
            }
            maxKdist = Math.min(maxKdist, maxKdist1);
            // System.out.println("Graph cache init distacne: " + maxKdist);
            // System.out.println();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(query, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(query, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            // update res
            res.add(nns);

            // update cachedPoint
            if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                hitCount += 1;
                query = lru.minPP;
                lru.update(query);
            } else {
                query.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
                long start = System.currentTimeMillis();
                // remove
                Point deleteP = lru.update(query);
                for (NN nn : deleteP.NNs) {
                    kGraph.removePoint(nn.point);
                }
                // add
                for (NN nn : nns) {
                    kGraph.points.add(nn.point);
                }
                for (NN nn : nns) {
                    kGraph.updateGraph(nn.point, k);
                }
                long end = System.currentTimeMillis();
                updateTime += (end - start);
                // cachedPoints = new HashSet<>();
                // for (Point caheedP : lru.cachedPoints) {
                // for (NN nn : caheedP.NNs) {
                // cachedPoints.add(nn.point);
                // }
                // }
                // System.out.println("Initial...");
                // kGraph.initGraph(new ArrayList<>(cachedPoints), k);
                // System.out.println(cachedPoints.size() + "/" + kGraph.size());
            }
        }
        System.out.println("Final kGraph.size:" + kGraph.size());
        assert lru.size() == cacheSize : lru.size() + "/" + cacheSize;
        t2 = System.currentTimeMillis();
        System.out.println("Effective count: " + count);

        if (useBFS) {
            BF_LRU_Time = t2 - t1;
            BF_LRU_NodeAccess = vp.nodeAccess / n;
            BF_LRU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "****	BFS--LRU-2 Caching\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, BF_LRU_Time / n, BF_LRU_NodeAccess, BF_LRU_CalcCount, hitCount);
        } else {
            DF_LRU_Time = t2 - t1;
            DF_LRU_NodeAccess = vp.nodeAccess / n;
            DF_LRU_CalcCount = vp.calcCount / n;
            info = String.format(
                    "****	DFS--LRU-2 Caching\nconstruct time / mean search time / mean node accesses / mean calc count / hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%d",
                    cTime, DF_LRU_Time / n, DF_LRU_NodeAccess, DF_LRU_CalcCount, hitCount);
        }
        System.out.println(info);
        System.out.println("Update-time: " + (updateTime / n));
        System.out.println("Search-time: " + (graphSearchTime / n));
        return res;
    }

}