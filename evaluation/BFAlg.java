package evaluation;

import java.util.ArrayList;
import java.util.stream.IntStream;

import Distance.*;

public class BFAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public double[][] qData;
    public double[][] dbData;
    public double fTime = 0;
    public DistanceFunction distFunction;
    public String info = null;

    public BFAlg(ArrayList<double[]> qData, ArrayList<double[]> dbData, DistanceFunction distFunction) {
        // Converting ArrayList to array for better performance
        this.qData = qData.toArray(new double[qData.size()][]);
        this.dbData = dbData.toArray(new double[dbData.size()][]);
        this.distFunction = distFunction;
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
        // Avoiding sqrt operations
        for (double[] qdata : qData) {
            for (double[] dbdata : dbData) {
                double centerDist = distFunction.distance(dbdata, qdata);
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
        for (int i = 0; i < qData.length; i++) {
            double[] candidate = null;
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < dbData.length; j++) {
                double d = distFunction.distance(qData[i], dbData[j]);
                if (d < minDist) {
                    minDist = d;
                    candidate = dbData[j];
                }
                assert candidate != null;
                assert d != 0 : "At least one same value as the query!";
            }
            res.add(candidate);
        }
        double t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        info = String.format("**\tBrute-Forced\nnn-Search time cost: %.3f ms / query", fTime / qData.length);
        System.out.println(info);
        return res;
    }

    public ArrayList<double[]> nnSearchPara() {
        long t1 = System.currentTimeMillis();
        ArrayList<double[]> res = new ArrayList<>();

        // Parallelizing the outer loop
        IntStream.range(0, qData.length).parallel().forEach(i -> {
            double[] candidate = dbData[0];
            double minDist = Double.MAX_VALUE;

            // Unrolled loop and reduced condition checks
            for (double[] dbdata : dbData) {
                double d = distFunction.distance(qData[i], dbdata);
                if (d < minDist) {
                    minDist = d;
                    candidate = dbdata;
                }
                assert d != 0 : "Has at least one same value as the query!";
            }

            synchronized (res) {
                res.add(candidate);
            }
        });

        long t2 = System.currentTimeMillis();
        fTime = (t2 - t1);
        int n = qData.length;
        info = String.format("**\tPara Brute-force\nNearest-neighbor search time cost: %.3f ms / query", fTime / n);
        System.out.println(info);
        return res;
    }

}