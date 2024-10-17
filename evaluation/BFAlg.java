package evaluation;

import java.util.*;
import utils.*;

public class BFAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public Point[] qData;
    public Point[] dbData;
    public double fTime = 0;
    public String info = null;

    public BFAlg(Point[] qData, Point[] dbData) {
        this.qData = qData;
        this.dbData = dbData;
    }

    public ArrayList<PriorityQueue<NN>> kNNSearch(int k) {
        double t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> nnList = new ArrayList<>();
        for (Point query : qData) {
            PriorityQueue<NN> res = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            for (Point db : dbData) {
                double dist = query.distanceTo(db);
                if (res.size() < k) {
                    res.add(new NN(db, dist));
                } else {
                    // Check if current node is closer
                    double maxKdist = res.peek().dist2query;
                    if (dist < maxKdist) {
                        res.poll(); // Remove the farthest outdated kNN
                        res.add(new NN(db, dist));
                    }
                }
                assert res != null;
            }
            nnList.add(res);
        }
        double t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format("**\tBrute-Forced\nkNN-Search time cost: %.3f ms / query", fTime / qData.length);
        System.out.println(info);
        return nnList;
    }

}