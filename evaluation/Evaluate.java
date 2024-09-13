package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import Distance.l2Distance;

public class Evaluate {

    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();
    l2Distance dist = new l2Distance();

    public static void writeFile(String setInfo, String otherInfo) {
        try {
            File writeName = new File(Settings.data + "_out.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName, true);
                    BufferedWriter out = new BufferedWriter(writer)) {
                if (setInfo.length() > 0) {
                    out.newLine();
                    out.newLine();
                    out.write(setInfo);
                }
                out.newLine();
                out.write(otherInfo);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            double dist1 = dist.distance(a.get(i), query.get(i));
            double dist2 = dist.distance(b.get(i), query.get(i));
            assert dist1 == dist2;
        }
    }

    public void evaluate_one(String data, int qSize, int dbSize, int dim, int leafSize, int sample) {
        // load data
        loadData(qSize, dbSize, dim);
        System.out.println("\n");
        String setInfo = String.format("Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tleafSize: %d \tsample: %d", data,
                query.size(), db.size(), dim, leafSize, sample);
        System.out.println(setInfo);

        // BF
        BFAlg bf = new BFAlg(query, db);
        // ArrayList<double[]> BFNNRes = bf.nnSearch();
        ArrayList<double[]> BFNNRes = bf.nnSearchPara();
        writeFile(setInfo, bf.info);
        setInfo = "";

        // Ball-Tree
        BJAlg bj = new BJAlg(query, db, leafSize);
        ArrayList<double[]> BJNNRes = bj.nnSearch();
        // ArrayList<double[]> BJNNResPara = bj.nnSearchPara();
        // _check(query, BJNNResPara, BJNNRes);
        writeFile(setInfo, bj.info);

        // VP-Sample
        VPSampleAlg sVP = new VPSampleAlg(query, db, sample);
        ArrayList<double[]> VPNNRes = sVP.nnSearch();
        // ArrayList<double[]> VPNNRes1 = sVP.nnSearchPara();
        // _check(query, VPNNRes, VPNNRes1);
        writeFile(setInfo, sVP.info);

        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sample);
        ArrayList<double[]> fVPNNRes = fVP.nnSearch();
        // ArrayList<double[]> fVPNNRes1 = fVP.nnSearchPara();
        // _check(query, fVPNNRes, fVPNNRes1);
        writeFile(setInfo, fVP.info);

        // result check
        _check(query, BFNNRes, BJNNRes);
        _check(query, BFNNRes, VPNNRes);
        _check(query, BFNNRes, fVPNNRes);
    }

    // the performance when varying various parameters
    public void evaluate_multi() {
        int[] qSizes = new int[] { 1000, 2000, 3000, 4000, 5000 };
        int[] dbSizes = new int[] { 100000, 200000, 200000, 400000, 500000 };
        int[] dims = new int[] { 10, 20, 40, 60, 80, 100 };
        int[] leafSizes = new int[] { 2, 4, 6, 8, 10, 20, 40 };
        int[] samples = new int[] { 100, 200, 300, 400, 500 };
        long t1 = System.currentTimeMillis();

        writeFile("\n\n New Experiments \n\n", "  ");

        for (int qSize : qSizes) {
            writeFile("/Vary the size of queries", qSize + "/");
            evaluate_one(Settings.data, qSize, Settings.dbNB, Settings.dim, Settings.minLeafNB, Settings.sampleNB);
        }
        System.out.println("\nRound 1 time cost: " + (System.currentTimeMillis() - t1) + "\n");
        for (int dbSize : dbSizes) {
            writeFile("/Vary the size of database", dbSize + "/");
            evaluate_one(Settings.data, Settings.qNB, dbSize, Settings.dim, Settings.minLeafNB, Settings.sampleNB);
        }
        System.out.println("\nRound 2 time cost: " + (System.currentTimeMillis() - t1) + "\n");
        for (int dim : dims) {
            writeFile("/Vary the size of dim", dim + "/");
            evaluate_one(Settings.data, Settings.qNB, Settings.dbNB, dim, Settings.minLeafNB, Settings.sampleNB);
        }
        System.out.println("\nRound 3 time cost: " + (System.currentTimeMillis() - t1) + "\n");

    }

    // evaluate the Ball-tree with varied leaf-size under various dimensions for
    // selecting best leafNB
    public void evaluateLeafNB(int expNB, String data, int qSize, int dbSize) {

        int[] leafSizes = new int[] { 4, 8, 12, 20, 30, 100 };
        writeFile("\nEvaluation Best LeafNB \n", "  ");
        for (int dim : new int[] { 10, 20, 40, 60, 80 }) {
            // load data
            loadData(qSize, dbSize, dim);
            System.out.println("\n");
            for (int leafSize : leafSizes) {
                String setInfo = String.format(
                        "Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tleafSize: %d",
                        data, query.size(), db.size(), dim, leafSize);
                System.out.println(setInfo);
                double meanCTime = 0;
                double meanFTime = 0;
                double meanAccess = 0;
                double meanCalcNB = 0;
                for (int j = 0; j < expNB; j++) {
                    // Ball-Tree
                    BJAlg bj = new BJAlg(query, db, leafSize);
                    bj.nnSearch();
                    meanCTime += bj.cTime;
                    meanFTime += bj.fTime;
                    meanAccess += bj.searchCount;
                    meanCalcNB += bj.calcCount;
                }
                String info = "\nBall-Tree: CTime/FTime/Access/CalcNB\n";
                String values = String.format("%8f %8f %8f %8f", meanCTime / expNB, meanFTime / expNB,
                        meanAccess / expNB, meanCalcNB / expNB);
                writeFile(setInfo, info + values);
                System.out.println(info + values);

            }

        }

    }

    // evaluate the VP-tree with varied number of sampled points
    public void evaluateSampleNB(int expNB, String data, int qSize, int dbSize) {

        int[] samples = new int[] { 100, 200, 300, 400, 500 };
        writeFile("\nEvaluation Best SampleNB \n", "  ");
        for (int dim : new int[] { 10, 20, 40, 60, 80 }) {
            // load data
            loadData(qSize, dbSize, dim);
            System.out.println("\n");
            for (int sample : samples) {
                String setInfo = String.format(
                        "\nData: %s \tqSize: %d \tdbSize: %d \tdim: %d \tsample: %d",
                        data,
                        query.size(), db.size(), dim, sample);
                System.out.println(setInfo);
                double meanCTime = 0;
                double meanFTime = 0;
                double meanAccess = 0;
                for (int j = 0; j < expNB; j++) {
                    // VP-Sample
                    VPSampleAlg sVP = new VPSampleAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes = sVP.nnSearch();
                    meanCTime += sVP.cTime;
                    meanFTime += sVP.fTime / query.size();
                    meanAccess += sVP.searchCount;
                    // VP-Farest
                    VPFarAlg fVP = new VPFarAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes1 = fVP.nnSearch();
                }
                String info = "Ball-Tree: CTime/FTime/Access/CalcNB\n";
                String values = String.format("%8f %8f %8f %8f", meanCTime / expNB, meanFTime / expNB,
                        meanAccess / expNB, meanAccess / expNB);
                writeFile(setInfo, info + values);
                System.out.println(info + values);

            }

        }

    }

    public static void main(String[] args) {
        Evaluate e = new Evaluate();
        int expNB = 10;
        // e.evaluateLeafNB(expNB, Settings.data, Settings.qNB, Settings.dbNB);
        // e.evaluateSampleNB(expNB, Settings.data, Settings.qNB, Settings.dbNB);
        e.evaluate_one(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim,
                Settings.minLeafNB, Settings.sampleNB);
        // e.evaluate_multi();
    }
}