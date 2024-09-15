package VPTree;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import Distance.DistanceFunction;
import evaluation.BJAlg;
import evaluation.Loader;
import evaluation.Settings;
import evaluation.VPFarAlg;
import evaluation.VPSampleAlg;

public class VPTreeBySampleTester {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    DistanceFunction distFunction = Settings.distFunction;

    public void generateRandomdata(int qNB, int dbNB, int dim) {
        Random r = new Random();
        db = new ArrayList<>();
        query = new ArrayList<>();
        for (int i = 0; i < qNB; i++) {
            double[] point = new double[dim];
            for (int j = 0; j < dim; j++) {
                point[j] = r.nextDouble();
            }
            query.add(point);
        }
        for (int i = 0; i < dbNB; i++) {
            double[] point = new double[dim];
            for (int j = 0; j < dim; j++) {
                point[j] = r.nextDouble();
            }
            db.add(point);
        }

    }

    public void loadData(int qNB, int dbNB, int dim) {
        if (Settings.data == "random") {
            generateRandomdata(qNB, dbNB, dim);
            return;
        }
        Loader l = new Loader();
        String dbPath = Settings.dirPath + Settings.data + "_db.txt";
        db = l.loadRealData(dbPath, dbNB, dim);
        String qPath = Settings.dirPath + Settings.data + "_query.txt";
        query = l.loadRealData(qPath, qNB, dim);
    }

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
        loadData(qSize, dbSize, dim);
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
        loadData(qSize, dbSize, dim);
        System.out.println("\n");
        String setInfo = String.format("Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tsample: %d", data,
                query.size(), db.size(), dim, sampleNB);
        System.out.println(setInfo);
        // ball-tree
        // BJAlg bj = new BJAlg(query, db, 10, distFunction);
        // ArrayList<double[]> BJNNRes = bj.nnSearch();
        // VP-sampleNB
        VPSampleAlg sVP = new VPSampleAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> VPkNNRes = sVP.kNNSearch(k);
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sampleNB, distFunction);
        ArrayList<PriorityQueue<NN>> fVPkNNRes = fVP.kNNSearch(k);
        for (int i = 0; i < qSize; i++) {
            PriorityQueue<NN> nn1 = VPkNNRes.get(i);
            PriorityQueue<NN> nn2 = fVPkNNRes.get(i);
            assert nn1.size() == nn2.size();
            assert nn1.poll().dist2query == nn2.poll().dist2query;
        }

    }

    public static void main(String[] args) {
        VPTreeBySampleTester t = new VPTreeBySampleTester();
        t.testNN(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                Settings.sampleNB);
        // t.testkNN(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
        // Settings.sampleNB, 5);
    }
}
