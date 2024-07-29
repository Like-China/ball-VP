package evaluation;

import java.util.ArrayList;
import balltree.*;
import Distance.*;

public class BJAlg {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> qData = new ArrayList<>();
    public ArrayList<double[]> dbData = new ArrayList<>();
    l2Distance dist = new l2Distance();
    // index construction time / filtering time
    public long cTime = 0;
    public double fTime = 0;
    // the repartition threshold
    public int minLeafNB = 0;
    public String info = null;

    public int calcCount = 0;
    public int searchCount = 0;

    public BJAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, int minLeafNB) {
        this.qData = qData;
        this.dbData = dbData;
        this.minLeafNB = minLeafNB;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> rangeSearch(double range) {
        long t1 = System.currentTimeMillis();
        BallTree bt = new BallTree(minLeafNB, dbData);
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
        BallTree bt = new BallTree(minLeafNB, dbData);
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
        int n = qData.size();
        info = String.format(
                "**BallTree**\nnn construct time / mean search time / nn mean node accesses / calc times:\n%8dms\t%8.3fms \t%8d \t%8d",
                cTime, fTime / n, bt.searchCount / n, bt.calcCount / n);
        calcCount = bt.calcCount;
        searchCount = bt.searchCount;
        System.out.println(info);
        return res;
    }

}