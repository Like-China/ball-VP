package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

import VPTree.*;
import Distance.*;

public class VPSampleAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public double[][] qData;
    public double[][] dbData;
    public DistanceFunction distFunction;
    // index construction time / filtering time
    public long cTime = 0;
    public double fTime = 0;
    // the number of node accesses
    public int nodeAccess = 0;

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp;
    long t1, t2;

    public VPSampleAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int sampleNB,
            DistanceFunction distFunction) {
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.sampleNB = sampleNB;
        this.distFunction = distFunction;
        long t1 = System.currentTimeMillis();
        // VP-tree construction
        vp = new VPTreeBySample(this.dbData, distFunction, sampleNB);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> nnSearch() {
        long t1 = System.currentTimeMillis();
        VPTreeBySample vp = new VPTreeBySample(dbData, distFunction, sampleNB);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

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
        nodeAccess = vp.nodeAccess / n;
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
        fTime = t2 - t1;
        int n = qData.length;
        info = String.format(
                "**\tVPSampleTree DFS-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        nodeAccess = vp.nodeAccess / n;
        // System.out.println(vp.heapUpdatecost / 10e6);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> searchkNNBestFirst(int k, boolean isHier) {
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNNBestFirst(q, k, Double.MAX_VALUE, isHier));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.length;
        String mode = isHier ? "Hier" : "Recursion";
        info = String.format(
                "**\tVPSampleTree Best-First-kNN (%s)\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                mode, cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        nodeAccess = vp.nodeAccess / n;
        // System.out.println(vp.heapUpdatecost / 10e6);
        return res;
    }

    // cache test within a same bucket
    public ArrayList<PriorityQueue<NN>> cacheTest(int k) {
        // VP-tree construction, obtain a set of vectors within the same bucket
        ArrayList<double[]> testData = new ArrayList<>();
        for (Item i : vp.getRandomLeafNode().items) {
            testData.add(i.getVector());
        }
        int n = testData.size();
        System.out.println("vector within this leaf node (bucket): " + testData.size());

        // VP-tree best-first with caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> bfcacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : testData) {
            // for each query, initial a set of kNN using its brothers wothin the same
            // bucket
            PriorityQueue<NN> initkNN = new PriorityQueue<>(Comp.NNComparator2);
            for (double[] db : testData) {
                double dist = new l2Distance().distance(q, db);
                // if (dist == 0)
                // continue;
                if (initkNN.size() < k) {
                    initkNN.add(new NN(db, dist));
                } else {
                    // Check if current node is closer than the farthest neighbor in the result
                    // queue
                    double maxKdist = initkNN.peek().dist2query;
                    if (dist < maxKdist) {
                        initkNN.poll(); // Remove the farthest
                        initkNN.add(new NN(db, dist));
                    }
                }
            }
            double maxKdist = initkNN.peek().dist2query;
            // refine kNN with the initial kNN knowledge
            bfcacheRes.add(vp.searchkNNBestFirst(q, k, maxKdist, false));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format(
                "**\tVPSampleTree Best-First with Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // VP-tree DFS with caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> cacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : testData) {
            // for each query, initial kNNs using its brothers wothin the same bucket
            PriorityQueue<NN> initkNN = new PriorityQueue<>(Comp.NNComparator2);
            for (double[] db : testData) {
                double dist = new l2Distance().distance(q, db);
                // if (dist == 0)
                // continue;
                if (initkNN.size() < k) {
                    initkNN.add(new NN(db, dist));
                } else {
                    // Check if current node is closer than the farthest neighbor in the result
                    // queue
                    double maxKdist = initkNN.peek().dist2query;
                    if (dist < maxKdist) {
                        initkNN.poll(); // Remove the farthest
                        initkNN.add(new NN(db, dist));
                    }
                }
            }
            double maxKdist = initkNN.peek().dist2query;
            // refine kNN with the initial kNN knowledge
            cacheRes.add(vp.searchkNNDFS(q, k, maxKdist));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format(
                "**\tVPSampleTree DFS with Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // VP-tree DFS without caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : testData) {
            res.add(vp.searchkNNDFS(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;

        info = String.format(
                "**\tVPSampleTree DFS without Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        nodeAccess = vp.nodeAccess / n;

        // check the results
        assert res.size() == cacheRes.size();
        for (int i = 0; i < res.size(); i++) {
            PriorityQueue<NN> nns1 = res.get(i);
            PriorityQueue<NN> nns2 = cacheRes.get(i);
            PriorityQueue<NN> nns3 = bfcacheRes.get(i);
            assert nns1.size() == nns2.size();
            while (!nns1.isEmpty()) {
                double d1 = nns1.poll().dist2query;
                double d2 = nns2.poll().dist2query;
                double d3 = nns3.poll().dist2query;
                assert d1 == d2 : d1 + "/" + d2;
                assert d1 == d3 : d1 + "/" + d3;
            }
        }
        return res;
    }

    // cache test for a batch of queries
    public ArrayList<PriorityQueue<NN>> cacheTest1(int k) {

        // VP-tree construction, obtain a set of vectors within the same bucket
        int n = qData.length;
        // Initilize 100NN for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            cache.add(vp.searchkNNDFS(q, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println();
        info = String.format(
                "**\tVPSampleTree DFS without Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        System.out.println();
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
        fTime = t2 - t1;
        info = String.format(
                "**\tVPSampleTree DFS with Caching Knowledge-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // VP-tree best-first with caching knowledge
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> BFHiercacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            // refine kNN with the initial kNN knowledge
            BFHiercacheRes.add(vp.searchkNNBestFirst(q, k, maxKdist, true));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format(
                "**\tVPSampleTree Best-First with Caching Knowledge (Hier)-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        ArrayList<PriorityQueue<NN>> BFRecucacheRes = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            double[] q = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            // refine kNN with the initial kNN knowledge
            BFRecucacheRes.add(vp.searchkNNBestFirst(q, k, maxKdist, false));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format(
                "**\tVPSampleTree Best-First with Caching Knowledge (Recursion)-kNN\nconstruct time / mean search time / mean node accesses / mean calc count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d",
                cTime, fTime / n, vp.nodeAccess / n, vp.calcCount / n);
        System.out.println(info);

        // check the results
        assert BFRecucacheRes.size() == DFSRes.size();
        for (int i = 0; i < BFRecucacheRes.size(); i++) {
            PriorityQueue<NN> nns1 = DFSRes.get(i);
            PriorityQueue<NN> nns2 = BFHiercacheRes.get(i);
            PriorityQueue<NN> nns3 = BFRecucacheRes.get(i);
            assert nns1.size() == nns2.size();
            while (!nns1.isEmpty()) {
                double d1 = nns1.poll().dist2query;
                double d2 = nns2.poll().dist2query;
                double d3 = nns3.poll().dist2query;
                assert d1 == d2 : i + "/" + d1 + "/" + d2;
                assert d1 == d3 : i + "/" + d1 + "/" + d3;
                assert d2 == d3 : i + "/" + d2 + "/" + d3;
            }
        }
        return BFRecucacheRes;
    }

    public ArrayList<double[]> rangeSearch(double range) {
        long t1 = System.currentTimeMillis();
        VPTreeBySample vp = new VPTreeBySample(dbData, distFunction, sampleNB);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            res.addAll(vp.searchRange(q, range));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println("VP range-Search result size: " + res.size());
        nodeAccess = vp.nodeAccess / qData.length;
        return res;
    }

}