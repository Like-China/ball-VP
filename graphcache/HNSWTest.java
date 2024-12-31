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
        l.loadData(10000, 10000, 10);
        Point[] queryPoints = l.query.toArray(new Point[l.query.size()]);
        Point[] dbPoints = l.db.toArray(new Point[l.db.size()]);
        // create a HNSW index
        int maxLevel = 2;
        int M = 20;
        int maxSize = 1000000;
        long t0 = System.currentTimeMillis();
        HNSW graph = new HNSW(maxLevel, M, maxSize);
        for (Point p : dbPoints) {
            graph.addPoint(p, M);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("HNSW establish time cost: " + (t1 - t0) + " ms");
        // print the HNSW index info
        graph.printGraph();

        // test delete point
        l = new Loader("SIFT");
        l.loadData(11000, 100, 10);
        dbPoints = l.db.toArray(new Point[l.db.size()]);
        // for (int i = 0; i < 100; i++) {
        // graph.removePoint(graph.points.get(i), dbPoints[i]);
        // }

        // query and calculate recall
        int K = 40;
        t1 = System.currentTimeMillis();
        BFAlg bfAlg = new BFAlg(queryPoints, dbPoints);
        ArrayList<PriorityQueue<NN>> exactResults = bfAlg.kNNSearch(K);
        long t2 = System.currentTimeMillis();
        System.out.println("Brute-force time cost: " + (t2 - t1) + " ms");
        ArrayList<PriorityQueue<NN>> hnswResults = new ArrayList<>();
        for (Point q : queryPoints) {
            PriorityQueue<NN> result = graph.findKNN(q, K);
            hnswResults.add(result);
        }
        long t3 = System.currentTimeMillis();
        System.out.println("Mean calc Count of each query: " + graph.calcCount / queryPoints.length);
        System.out.println("HNSW time cost:: " + (t3 - t2) + " ms");
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
        double averageRecall = totalRecall / queryPoints.length;
        System.out.println("Mean recall: " + averageRecall);
    }

}
