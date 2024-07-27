package evaluation;

import java.util.ArrayList;
import balltree.*;
import Distance.*;

public class BJAlgRepartition {
    /// query, double[]base set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> queries = new ArrayList<>();
    public ArrayList<double[]> db = new ArrayList<>();
    l2Distance dist = new l2Distance();
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public double searchCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;

    public BJAlgRepartition(ArrayList<double[]> queries, ArrayList<double[]> db, double repartirionRatio,
            int minLeafNB) {
        this.queries = queries;
        this.db = db;
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
    }

    /**
     * conduct BJ-Alg without reportition strategy to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> rangeQuery(double range) {
        long t1 = System.currentTimeMillis();
        HBallTree bt = new HBallTree(minLeafNB, db, repartirionRatio);
        HBallNode root = bt.buildBallTree();
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<double[]> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (double[] q : queries) {
            ArrayList<double[]> ballRangeResult = bt.searchRange(root, q, range);
            res.addAll(ballRangeResult);
        }
        searchCount = bt.searchCount;
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println("Ball-reparition based nn-Search time cost: " + fTime);
        System.out.println("Ball-reparition based nn-Search result size: " + res.size());
        return res;

    }

}