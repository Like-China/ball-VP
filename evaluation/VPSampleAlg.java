package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;

import VPTree.*;
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
                "**\tVPSampleTree\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.3fms \t%5d \t\t%5d",
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
                "**\tVPSampleTree DFS-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
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
                "**\tVPSampleTree Best-First-kNN (%s)\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
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

    // cache test for a batch of queries
    public ArrayList<PriorityQueue<NN>> cacheTest(int k) {

        // VP-tree construction, obtain a set of vectors within the same bucket
        int n = qData.length;
        // VP-tree DFS with caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> DFSRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = 100;
            // refine kNN with the initial kNN knowledge
            DFSRes.add(vp.searchkNNDFS(q, k, maxKdist));
        }
        t2 = System.currentTimeMillis();
        DFCacheTime = t2 - t1;
        DFCacheNodeAccess = vp.nodeAccess / n;
        DFCacheCalcCount = vp.calcCount / n;
        info = String.format(
                "**\tVPSampleTree DFS with Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, DFCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> BFRecucacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = 100;
            // refine kNN with the initial kNN knowledge
            BFRecucacheRes.add(vp.searchkNNBestFirst(q, k, maxKdist));
        }
        t2 = System.currentTimeMillis();
        BFRecuCacheTime = t2 - t1;
        BFRecuCacheNodeAccess = vp.nodeAccess / n;
        BFRecuCacheCalcCount = vp.calcCount / n;
        info = String.format(
                "**\tVPSampleTree Best-First with Caching Knowledge (Recursion)-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, BFRecuCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // check the results
        assert BFRecucacheRes.size() == DFSRes.size();
        for (int i = 0; i < BFRecucacheRes.size(); i++) {
            PriorityQueue<NN> nns1 = DFSRes.get(i);
            // PriorityQueue<NN> nns2 = BFHiercacheRes.get(i);
            PriorityQueue<NN> nns3 = BFRecucacheRes.get(i);
            // assert nns1.size() == nns2.size();
            while (!nns1.isEmpty()) {
                double d1 = nns1.poll().dist2query;
                // double d2 = nns2.poll().dist2query;
                double d3 = nns3.poll().dist2query;
                // assert d1 == d2 : i + "/" + d1 + "/" + d2;
                assert d1 == d3 : i + "/" + d1 + "/" + d3;
                // assert d2 == d3 : i + "/" + d2 + "/" + d3;
            }
        }
        return BFRecucacheRes;
    }

    // cache test for a batch of queries
    public ArrayList<PriorityQueue<NN>> cacheTest(double factor, int k) {

        // VP-tree construction, obtain a set of vectors within the same bucket
        int n = qData.length;
        // Initilize 100NN for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        int cacheK = (int) (k * factor);
        for (double[] q : qData) {
            cache.add(vp.searchkNNDFS(q, cacheK, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        // System.out.println();
        // double ftime = t2 - t1;
        // info = String.format(
        // "**\tVPSampleTree DFS without Caching Knowledge-kNN\nconstruct time / mean
        // search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d
        // \t\t%5d",
        // cTime, ftime / n, vp.nodeAccess / n, vp.calcCount / n);
        // System.out.println(info);
        // System.out.println();

        // VP-tree DFS with caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> DFSRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            // refine kNN with the initial kNN knowledge
            DFSRes.add(vp.searchkNNDFS(q, k, maxKdist));
        }
        t2 = System.currentTimeMillis();
        DFCacheTime = t2 - t1;
        DFCacheNodeAccess = vp.nodeAccess / n;
        DFCacheCalcCount = vp.calcCount / n;
        info = String.format(
                "**\tVPSampleTree DFS with Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, DFCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> BFRecucacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            // refine kNN with the initial kNN knowledge
            BFRecucacheRes.add(vp.searchkNNBestFirst(q, k, maxKdist));
        }
        t2 = System.currentTimeMillis();
        BFRecuCacheTime = t2 - t1;
        BFRecuCacheNodeAccess = vp.nodeAccess / n;
        BFRecuCacheCalcCount = vp.calcCount / n;
        info = String.format(
                "**\tVPSampleTree Best-First with Caching Knowledge (Recursion)-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, BFRecuCacheTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // check the results
        assert BFRecucacheRes.size() == DFSRes.size();
        for (int i = 0; i < BFRecucacheRes.size(); i++) {
            PriorityQueue<NN> nns1 = DFSRes.get(i);
            // PriorityQueue<NN> nns2 = BFHiercacheRes.get(i);
            PriorityQueue<NN> nns3 = BFRecucacheRes.get(i);
            // assert nns1.size() == nns2.size();
            while (!nns1.isEmpty()) {
                double d1 = nns1.poll().dist2query;
                // double d2 = nns2.poll().dist2query;
                double d3 = nns3.poll().dist2query;
                // assert d1 == d2 : i + "/" + d1 + "/" + d2;
                assert d1 == d3 : i + "/" + d1 + "/" + d3;
                // assert d2 == d3 : i + "/" + d2 + "/" + d3;
            }
        }
        return BFRecucacheRes;
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