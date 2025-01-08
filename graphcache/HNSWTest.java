package graphcache;

import java.io.IOException;
import java.util.*;
import evaluation.BFAlg;
import utils.Loader;
import utils.NN;
import utils.Point;

public class HNSWTest {
    public static void main(String[] args) throws IOException {
        // load data
        Loader l = new Loader("SIFT");
        l.loadData(1000, 100000, 10);
        Point[] queryPoints = l.query;
        Point[] dbPoints = l.db;
        // create a HNSW index
        int maxLevel = 2;
        int M = 10;
        long t0 = System.currentTimeMillis();
        HNSW graph = new HNSW(maxLevel, M);
        for (Point p : dbPoints) {
            graph.addPoint(p, M);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("HNSW establish time cost: " + (t1 - t0) + " ms");
        // print the HNSW index info
        graph.printGraph();

        // test delete point
        // l = new Loader("SIFT");
        // int addN = 100;
        // l.loadData(11000, addN, 10);
        // for (int i = 0; i < addN; i++) {
        // graph.removePoint(graph.points.get(i), l.db.get(i));
        // }

        // query and calculate recall
        int K = 40;
        BFAlg bfAlg = new BFAlg(queryPoints, dbPoints);
        ArrayList<PriorityQueue<NN>> exactResults = bfAlg.kNNSearch(K);
        double t2 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> hnswResults = new ArrayList<>();
        for (Point q : queryPoints) {
            PriorityQueue<NN> result = graph.findKNN(q, K);
            hnswResults.add(result);
        }
        double t3 = System.currentTimeMillis();
        System.out.println("Mean calc Count of each query: " + graph.calcCount / queryPoints.length);
        System.out.println("HNSW time cost: " + (t3 - t2) / queryPoints.length + " ms/query");
        double totalRecall = 0.0;
        for (int i = 0; i < queryPoints.length; i++) {
            Set<Point> exactSet = new HashSet<>();
            for (NN nn : exactResults.get(i)) {
                exactSet.add(nn.point);
            }

            int correctCount = 0;
            for (NN nn : hnswResults.get(i)) {
                if (exactSet.contains(nn.point)) {
                    correctCount++;
                }
            }

            double recall = (double) correctCount / K;
            totalRecall += recall;
        }
        double averageRecall = totalRecall * 100 / queryPoints.length;
        System.out.println(String.format("Mean recall: %.2f", averageRecall));
    }

}
