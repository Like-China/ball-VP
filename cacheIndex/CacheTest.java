package cacheIndex;

import java.util.ArrayList;
import java.util.List;

import Distance.DistanceFunction;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPSampleAlg;

public class CacheTest {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    DistanceFunction distFunction = Settings.distFunction;

    public void evaluate(String data, int qSize, int dbSize, int dim, int sampleNB, int k, int bucketSize,
            double factor) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;
        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d \tfactor: %f",
                data, query.size(), db.size(), k, dim, sampleNB, bucketSize, factor);
        System.out.println(setInfo);
        // use KMeans to get clusters
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < query.size(); i++) {
            points.add(new Point(i, query.get(i)));
        }
        int maxIterations = 100;
        List<List<Point>> clusters = KMeans.kMeansCluster(points, 5, maxIterations);
        // for (int i = 0; i < clusters.size(); i++) {
        // System.out.println(clusters.get(i).size());
        // }

        // construct VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction, bucketSize);
        // DFS
        sVP.searchkNNDFS(k);
        // Recu Best-First
        sVP.searchkNNBestFirst(k);
        // cacheTest
        sVP.cacheTest(k);

    }

    public void getClusters() {

    }

    public static void main(String[] args) {
        CacheTest t = new CacheTest();
        t.evaluate(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.sampleNB, Settings.k,
                Settings.bucketSize, Settings.factor);

    }
}
