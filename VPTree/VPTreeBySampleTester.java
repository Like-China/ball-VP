package VPTree;

import java.util.ArrayList;
import java.util.PriorityQueue;
import Distance.DistanceFunction;
import evaluation.BFAlg;
import evaluation.BJAlg;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPFarAlg;
import evaluation.VPSampleAlg;

public class VPTreeBySampleTester {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    DistanceFunction distFunction = Settings.distFunction;

    public void _check(ArrayList<double[]> query, ArrayList<double[]> a, ArrayList<double[]> b) {
        assert query.size() == a.size();
        assert query.size() == b.size();
        for (int i = 0; i < query.size(); i++) {
            double dist1 = distFunction.distance(a.get(i), query.get(i));
            double dist2 = distFunction.distance(b.get(i), query.get(i));
            assert dist1 == dist2 : dist1 + "/" + dist2;
        }
    }

    public void testNN(String data, int qSize, int dbSize, int dim, int sampleNB) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format("Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tsample: %d", data,
                query.size(), db.size(), dim, sampleNB);
        System.out.println(setInfo);
        // ball-tree
        BJAlg bj = new BJAlg(query, db, 10, distFunction);
        ArrayList<double[]> BJNNRes = bj.nnSearch();
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<double[]> VPNNRes = sVP.nnSearch();
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sampleNB, distFunction);
        ArrayList<double[]> fVPNNRes = fVP.nnSearch();

        // result check
        _check(query, BJNNRes, VPNNRes);
        _check(query, VPNNRes, fVPNNRes);

    }

    public void testkNN(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // Brute-Force
        BFAlg bf = new BFAlg(query, db, distFunction);
        ArrayList<PriorityQueue<NN>> BFkNNRes = bf.kNNSearch(k);
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.searchkNNDFS(k);
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> fVPkNNRes = fVP.searchkNNDFS(k);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = fVPkNNRes.get(i);
            PriorityQueue<NN> nn3 = BFkNNRes.get(i);
            assert nn1.size() == nn2.size();
            assert nn1.size() == nn3.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                double d3 = nn3.poll().dist2query;
                assert d1 == d2;
                assert d1 == d3;
            }
        }
    }

    public void bestFirstTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data, query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // VP-sampleNB
        // DFS
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.searchkNNDFS(k);
        // Hier Best-First
        VPSampleAlg sVP1 = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes1 = sVP1.searchkNNBestFirst(k, true);
        // Recu Best-First
        VPSampleAlg sVP2 = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes2 = sVP2.searchkNNBestFirst(k, false);

        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = VPkNNRes1.get(i);
            PriorityQueue<NN> nn3 = VPkNNRes2.get(i);
            assert nn1.size() == nn2.size();
            while (!nn1.isEmpty()) {
                double d1 = nn1.poll().dist2query;
                double d2 = nn2.poll().dist2query;
                double d3 = nn3.poll().dist2query;
                assert d1 == d2;
                assert d1 == d3;
            }
        }
    }

    public void cacheTest(String data, int qSize, int dbSize, int dim, int sampleNB, int k) {
        // load data
        Loader l = new Loader();
        l.loadData(qSize, dbSize, dim);
        this.db = l.db;
        this.query = l.query;

        System.out.println("\n");
        String setInfo = String.format(
                "Data: %s \tqSize: %d \tdbSize: %d \tk: %d \tdim: %d \tsample: %d \tBucket Size:%d",
                data,
                query.size(), db.size(), k, dim, sampleNB, Settings.bucketSize);
        System.out.println(setInfo);
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.cacheTest1(k);
    }

    public static void main(String[] args) {
        VPTreeBySampleTester t = new VPTreeBySampleTester();
        // t.testNN("sift", 1000, 100000, 10, 50);
        // t.testkNN("sift", 1000, 100000, 10, 50, 10);
        // t.bestFirstTest("sift", 1000, 1000000, 10, 10, 10);
        t.cacheTest("sift", 1000, 1000000, 10, 10, 10);
        System.exit(0);

        for (int i = 0; i < 5; i++) {
            t.cacheTest(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                    Settings.sampleNB, 20);
        }
        // testkNN bestFirstTest cacheTest
        for (int i = 0; i < 5; i++) {
            t.bestFirstTest(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                    Settings.sampleNB, 10);
            System.out.println();
            System.out.println();
        }

    }
}
