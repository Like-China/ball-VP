package evaluation;

import java.util.ArrayList;
import java.util.stream.IntStream;

import balltree.*;
import Distance.*;

public class BJAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public double[][] qData;
    public double[][] dbData;
    // index construction time / filtering time
    public long cTime = 0;
    public double fTime = 0;
    // the repartition threshold
    public int bucketSize = 0;
    public String info = null;
    public DistanceFunction distFunction;
    public int calcCount = 0;
    public int searchCount = 0;

    public BJAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int bucketSize, DistanceFunction distFunction) {
        // Converting ArrayList to array for better performance
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.bucketSize = bucketSize;
        this.distFunction = distFunction;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> rangeSearch(double range) {
        long t1 = System.currentTimeMillis();
        BallTree bt = new BallTree(bucketSize, dbData, distFunction);
        BallNode root = bt.buildBallTree();
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            ArrayList<double[]> ballRangeResult = bt.searchRange(root, q, range);
            res.addAll(ballRangeResult);
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println("BallTree range Search time cost: " + fTime);
        System.out.println("BallTree range Search result size: " + res.size());
        return res;
    }

    public ArrayList<double[]> nnSearch() {
        long t1 = System.currentTimeMillis();
        BallTree bt = new BallTree(bucketSize, dbData, distFunction);
        BallNode root = bt.buildBallTree();
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : qData) {
            res.add(bt.searchNN(root, q));
        }
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.length;
        info = String.format(
                "**\tBallTree\nnn construct time / mean search time / nn mean node accesses / calc count:\n%8dms\t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, bt.searchCount / n, bt.calcCount / n);
        calcCount = bt.calcCount;
        searchCount = bt.searchCount;
        System.out.println(info);
        return res;
    }

    public ArrayList<double[]> nnSearchPara() {
        long t1 = System.currentTimeMillis();
        BallTree bt = new BallTree(bucketSize, dbData, distFunction);
        BallNode root = bt.buildBallTree();
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;
        t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<>();

        // Parallelizing the outer loop
        IntStream.range(0, qData.length).parallel().forEach(i -> {
            double[] q = qData[i];
            double[] nn = bt.searchNN(root, q);
            synchronized (res) {
                res.add(nn);
            }
        });

        t2 = System.currentTimeMillis();
        fTime = (t2 - t1);
        int n = qData.length;
        info = String.format(
                "**\tParallel BallTree\nnn construct time / mean search time / nn mean node accesses / calc count:\n%8dms\t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, bt.searchCount / n, bt.calcCount / n);
        calcCount = bt.calcCount;
        searchCount = bt.searchCount;
        System.out.println(info);
        return res;
    }

}