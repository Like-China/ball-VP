package evaluation;

import java.util.ArrayList;
import Distance.*;

public class BFAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> qData = new ArrayList<>();
    public ArrayList<double[]> dbData = new ArrayList<>();
    public double fTime = 0;
    l2Distance dist = new l2Distance();
    public String info = null;

    public BFAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData) {
        this.qData = qData;
        this.dbData = dbData;
    }

    /**
     * conduct brute-force method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<double[]> rangeSearch(double range) {
        double t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<>();
        // bruteforce
        for (double[] qdata : qData) {
            for (double[] dbdata : dbData) {
                double centerDist = dist.distance(dbdata, qdata);
                if (centerDist <= range) {
                    res.add(dbdata);
                }
            }
        }
        double t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        System.out.println("Brute-Forced based range-Search time cost: " + fTime);
        System.out.println("Brute-Forced based range-Search result size: " + res.size());
        return res;
    }

    public ArrayList<double[]> nnSearch() {
        // brute-based solution
        double t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<double[]>();
        for (int i = 0; i < qData.size(); ++i) {
            double[] candidate = dbData.get(0);
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < dbData.size(); j++) {
                double d = dist.distance(qData.get(i), dbData.get(j));
                if (d < minDist) {
                    minDist = d;
                    candidate = dbData.get(j);
                }
                if (d == 0) {
                    System.out.println("The database has at least one same values to the query");
                    assert 1 == 2;
                }
            }
            res.add(candidate);
        }
        double t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        int n = qData.size();
        info = String.format("**Brute-Forced**\nnn-Search time cost: %.3f ms", fTime / n);
        System.out.println(info);
        return res;
    }

}