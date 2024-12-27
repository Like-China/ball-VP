package graphcache;

import java.io.IOException;
import java.util.*;

import evaluation.BFAlg;
import evaluation.Settings;
import utils.Loader;
import utils.NN;
import utils.Point;

public class HNSWTest {
    // public static void main(String[] args) throws IOException {
    // // 创建HNSWKGraph实例
    // int maxLevel = 3;
    // int M = 20;
    // int maxSize = 1000;
    // HNSW graph = new HNSW(maxLevel, M, maxSize);

    // Loader l = new Loader(Settings.data);
    // l.loadData(1000, 1000, 7);

    // Point[] queryPoints = l.query.toArray(new Point[l.query.size()]);
    // Point[] dbPoints = l.db.toArray(new Point[l.db.size()]);
    // for (Point p : dbPoints) {
    // graph.addPoint(p, M);
    // }

    // // 打印图结构
    // // graph.printGraph();

    // // 执行搜索
    // float[] queryVector = { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f };
    // Point queryPoint = new Point(-1, queryVector);
    // int K = 5;
    // PriorityQueue<NN> results = graph.searchLayer(queryPoint, K, null, 0, new
    // PriorityQueue<>());

    // // 打印搜索结果
    // System.out.println("\n查询点: " + Arrays.toString(queryVector));

    // while (!results.isEmpty()) {
    // NN nn = results.poll();
    // System.out.println(
    // "ID: " + nn.point.id + " vector: " + Arrays.toString(nn.point.vector) + ",
    // 距离: " + nn.dist2query);
    // }

    // }

    // 测试代码
    public static void main(String[] args) throws IOException {
        // load data
        Loader l = new Loader(Settings.data);
        l.loadData(2000, 20000, 10);
        Point[] queryPoints = l.query.toArray(new Point[l.query.size()]);
        Point[] dbPoints = l.db.toArray(new Point[l.db.size()]);
        // 创建HNSWKGraph实例
        int maxLevel = 3;
        int M = 10;
        int maxSize = 1000000;

        long t0 = System.currentTimeMillis();
        HNSW graph = new HNSW(maxLevel, M, maxSize);
        for (Point p : dbPoints) {
            graph.addPoint(p, M);
        }
        long t1 = System.currentTimeMillis();
        System.out.println("HNSW构建完成,时间: " + (t1 - t0) + " ms");
        graph.printGraph();

        // 计算精确kNN和HNSW的kNN，并比较recall
        // 使用BFAlg计算精
        int K = 10;
        t1 = System.currentTimeMillis();
        BFAlg bfAlg = new BFAlg(queryPoints, dbPoints);
        ArrayList<PriorityQueue<NN>> exactResults = bfAlg.kNNSearch(K);
        long t2 = System.currentTimeMillis();
        System.out.println("精确kNN计算完成，时间: " + (t2 - t1) + " ms");
        // 使用HNSW计算近
        ArrayList<PriorityQueue<NN>> hnswResults = new ArrayList<>();
        for (Point q : queryPoints) {
            PriorityQueue<NN> result = graph.findKNN(q, K);
            hnswResults.add(result);
        }
        long t3 = System.currentTimeMillis();
        System.out.println("Graph calcCount: " + graph.calcCount / queryPoints.length);
        System.out.println("HNSW计算完成，时间: " + (t3 - t2) + " ms");
        // 计算平均召回率
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
        System.out.println("平均召回率: " + averageRecall);
    }

}
