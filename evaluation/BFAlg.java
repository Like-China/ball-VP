package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.stream.IntStream;

import Distance.*;
import VPTree.Comp;
import VPTree.Item;
import VPTree.NN;
import VPTree.VPNode;

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

    public ArrayList<PriorityQueue<NN>> kNNSearch(int k) {
        double t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> nnList = new ArrayList<>();
        for (int i = 0; i < qData.length; i++) {
            PriorityQueue<NN> res = new PriorityQueue<>(Comp.NNComparator2);
            for (int j = 0; j < dbData.length; j++) {
                double[] vec = dbData[j];
                double dist = distFunction.distance(qData[i], vec);
                if (res.size() < k) {
                    res.add(new NN(vec, dist));
                } else {
                    // Check if current node is closer than the farthest neighbor in the result
                    // queue
                    double maxKdist = res.peek().dist2query;
                    if (dist < maxKdist) {
                        res.poll(); // Remove the farthest
                        res.add(new NN(vec, dist));
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