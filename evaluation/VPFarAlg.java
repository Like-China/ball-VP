package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

import VPTree.*;
import Distance.*;

public class VPFarAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public double[][] qData;
    public double[][] dbData;
    public DistanceFunction distFunction;
    // index construction time / filtering time
    public long cTime = 0;
    public double fTime = 0;
    // the number of node accesses
    public int searchCount = 0;

    public String info = null;
    public int sampleNB;

    public VPFarAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int sampleNB,
            DistanceFunction distFunction) {
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.sampleNB = sampleNB;
        this.distFunction = distFunction;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> nnSearch() {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, distFunction);
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
                "**\tVPFarTree\nnn construct time / mean search time / nn mean node accesses / calc times:\n%8dms \t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, vp.searchCount / n, vp.searchCount / n);
        System.out.println(info);
        searchCount = vp.searchCount / n;

        return res;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<PriorityQueue<NN>> kNNSearch(int k) {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, distFunction);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (double[] q : qData) {
            res.add(vp.searchkNN(q, k));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.length;
        info = String.format(
                "**\tVPFarTree\nkNN construct time / mean search time / nn mean node accesses / calc times:\n%8dms \t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, vp.searchCount / n, vp.searchCount / n);
        System.out.println(info);
        searchCount = vp.searchCount / n;

        return res;
    }

    public ArrayList<double[]> nnSearchPara() {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, distFunction);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;
        t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<>();

        // Parallelizing the outer loop
        IntStream.range(0, qData.length).parallel().forEach(i -> {
            double[] q = qData[i];
            double[] nn = vp.searchOneNN(q);
            synchronized (res) {
                res.add(nn);
            }
        });

        t2 = System.currentTimeMillis();
        fTime = (t2 - t1);
        int n = qData.length;
        info = String.format(
                "**\tPara VPFarTree\nnn construct time / mean search time / nn mean node accesses / calc times:\n%8dms \t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, vp.searchCount / n, vp.searchCount / n);
        System.out.println(info);
        return res;
    }

    public ArrayList<double[]> rangeSearch(double range) {
        long t1 = System.currentTimeMillis();
        VPTreeByFarest vp = new VPTreeByFarest(dbData, distFunction);
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
        searchCount = vp.searchCount / qData.length;
        return res;
    }

}