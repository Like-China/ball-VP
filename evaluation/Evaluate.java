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
                out.write("\n" + otherInfo);
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
        ArrayList<double[]> BFNNRes = bf.nnSearch();
        writeFile(setInfo, bf.info);
        setInfo = "";
        // Ball-Tree
        BJAlg bj = new BJAlg(query, db, leafSize);
        ArrayList<double[]> BJNNRes = bj.nnSearch();
        writeFile(setInfo, bj.info);
        // VP-Sample
        VPSampleAlg svj = new VPSampleAlg(query, db, sample);
        ArrayList<double[]> VPNNRes = svj.nnSearch();
        writeFile(setInfo, svj.info);
        // VP-Farest
        VPFarAlg fVP = new VPFarAlg(query, db, sample);
        ArrayList<double[]> VPNNRes1 = fVP.nnSearch();
        writeFile(setInfo, fVP.info);
        // check
        _check(query, BFNNRes, BJNNRes);
        _check(query, BFNNRes, VPNNRes);
        _check(query, BFNNRes, VPNNRes1);
    }

    public void evaluate_multi() {
        int[] qSizes = new int[] { 1000, 2000, 2000, 4000, 5000 };
        int[] dbSizes = new int[] { 100000, 200000, 200000, 400000, 500000 };
        int[] dims = new int[] { 10, 20, 40, 60, 80, 100 };
        int[] leafSizes = new int[] { 2, 4, 6, 8, 10, 20, 40 };
        int[] samples = new int[] { 100, 200, 300, 400, 500 };
        long t1 = System.currentTimeMillis();

        writeFile("\n\n Neww Experiments \n\n", "  ");

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
        for (int leafSize : leafSizes) {
            writeFile("/Vary the leafSize", leafSize + "/");
            evaluate_one(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, leafSize, Settings.sampleNB);
        }
        System.out.println("\nRound 4 time cost: " + (System.currentTimeMillis() - t1) + "\n");
        for (int sample : samples) {
            writeFile("/Vary the size of sample", sample + "/");
            evaluate_one(Settings.data, Settings.qNB, Settings.dbNB, Settings.dim, Settings.minLeafNB, sample);
        }
        System.out.println("\nRound 5 time cost: " + (System.currentTimeMillis() - t1) + "\n");

    }

    public void evaluate_capacity(String data, int qSize, int dbSize, int sample) {

        int[] leafSizes = new int[] { 4, 8, 12, 20, 30, 100 };
        writeFile("\n\n Neww Experiments \n\n", "  ");

        for (int dim : new int[] { 10, 20, 40, 60, 80 }) {
            // load data
            loadData(qSize, dbSize, dim);
            System.out.println("\n");
            for (int leafSize : leafSizes) {
                String setInfo = String.format(
                        "Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tleafSize: %d \tsample: %d",
                        data,
                        query.size(), db.size(), dim, leafSize, sample);
                System.out.println(setInfo);
                double minCTime = Double.MAX_VALUE;
                double maxCTime = 0;
                double minFTime = Double.MAX_VALUE;
                double maxFTime = 0;
                double minAccess = Double.MAX_VALUE;
                double maxAccess = 0;
                double minCalcNB = Double.MAX_VALUE;
                double maxCalcNB = 0;
                for (int j = 0; j < 20; j++) {
                    // BF
                    BFAlg bf = new BFAlg(query, db);
                    ArrayList<double[]> BFNNRes = bf.nnSearch();
                    writeFile(setInfo, bf.info);
                    setInfo = "";
                    // Ball-Tree
                    BJAlg bj = new BJAlg(query, db, leafSize);
                    ArrayList<double[]> BJNNRes = bj.nnSearch();
                    writeFile(setInfo, bj.info);
                    // VP-Sample
                    VPSampleAlg svj = new VPSampleAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes = svj.nnSearch();
                    minCTime = minCTime < bj.cTime ? minCTime : bj.cTime;
                    maxCTime = maxCTime > bj.cTime ? maxCTime : bj.cTime;
                    minFTime = minFTime < bj.fTime ? minFTime : bj.fTime;
                    maxFTime = maxFTime > bj.fTime ? maxFTime : bj.fTime;
                    minAccess = minAccess < bj.searchCount ? minAccess : bj.searchCount;
                    maxAccess = maxAccess > bj.searchCount ? maxAccess : bj.searchCount;
                    minCalcNB = minCalcNB < bj.calcCount ? minCalcNB : bj.calcCount;
                    maxCalcNB = maxCalcNB > bj.calcCount ? maxCalcNB : bj.calcCount;
                    writeFile(setInfo, svj.info);
                    // VP-Farest
                    VPFarAlg fVP = new VPFarAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes1 = fVP.nnSearch();
                    writeFile(setInfo, fVP.info);
                }
                String info = "\nBall-Tree: minCTime/maxCTime/diff/minFTime/maxFTime/diff/minAccess/maxAccess/diff/minCalcNB/maxCalcNB/diff\n";
                String values = String.format("%8f %8f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f",
                        minCTime, maxCTime, (maxCTime - minCTime) * 100 / maxCTime, minFTime / qSize, maxFTime / qSize,
                        (maxFTime - minFTime) * 100 / maxFTime,
                        minAccess, maxAccess, (maxAccess - minAccess) * 100 / maxAccess, minCalcNB, maxCalcNB,
                        (maxCalcNB - minCalcNB) * 100 / maxCalcNB);
                writeFile(setInfo, info + values);
                System.out.println(info + values);

            }

        }

    }

    public void evaluate_sample(String data, int qSize, int dbSize, int leafSize) {

        int[] samples = new int[] { 1000, 2000, 3000, 4000, 5000 };
        writeFile("\n\n Neww Experiments \n\n", "  ");

        for (int dim : new int[] { 10, 20, 40, 60, 80 }) {
            // load data
            loadData(qSize, dbSize, dim);
            System.out.println("\n");
            for (int sample : samples) {
                String setInfo = String.format(
                        "Data: %s \tqSize: %d \tdbSize: %d \tdim: %d \tleafSize: %d \tsample: %d",
                        data,
                        query.size(), db.size(), dim, leafSize, sample);
                System.out.println(setInfo);
                double minCTime = Double.MAX_VALUE;
                double maxCTime = 0;
                double minFTime = Double.MAX_VALUE;
                double maxFTime = 0;
                double minAccess = Double.MAX_VALUE;
                double maxAccess = 0;
                double minCalcNB = Double.MAX_VALUE;
                double maxCalcNB = 0;
                for (int j = 0; j < 20; j++) {
                    // BF
                    BFAlg bf = new BFAlg(query, db);
                    ArrayList<double[]> BFNNRes = bf.nnSearch();
                    writeFile(setInfo, bf.info);
                    setInfo = "";
                    // Ball-Tree
                    BJAlg bj = new BJAlg(query, db, leafSize);
                    ArrayList<double[]> BJNNRes = bj.nnSearch();
                    writeFile(setInfo, bj.info);
                    // VP-Sample
                    VPSampleAlg svj = new VPSampleAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes = svj.nnSearch();
                    minCTime = minCTime < svj.cTime ? minCTime : svj.cTime;
                    maxCTime = maxCTime > svj.cTime ? maxCTime : svj.cTime;
                    minFTime = minFTime < svj.fTime ? minFTime : svj.fTime;
                    maxFTime = maxFTime > svj.fTime ? maxFTime : svj.fTime;
                    minAccess = minAccess < svj.searchCount ? minAccess : svj.searchCount;
                    maxAccess = maxAccess > svj.searchCount ? maxAccess : svj.searchCount;
                    minCalcNB = minAccess;
                    maxCalcNB = maxAccess;
                    writeFile(setInfo, svj.info);
                    // VP-Farest
                    VPFarAlg fVP = new VPFarAlg(query, db, sample);
                    ArrayList<double[]> VPNNRes1 = fVP.nnSearch();
                    writeFile(setInfo, fVP.info);
                }
                String info = "\nVP-tree: minCTime/maxCTime/diff/minFTime/maxFTime/diff/minAccess/maxAccess/diff/minCalcNB/maxCalcNB/diff\n";
                String values = String.format("%8f %8f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f",
                        minCTime, maxCTime, (maxCTime - minCTime) * 100 / maxCTime, minFTime / qSize, maxFTime / qSize,
                        (maxFTime - minFTime) * 100 / maxFTime,
                        minAccess, maxAccess, (maxAccess - minAccess) * 100 / maxAccess, minCalcNB, maxCalcNB,
                        (maxCalcNB - minCalcNB) * 100 / maxCalcNB);
                writeFile(setInfo, info + values);
                System.out.println(info + values);

            }

        }

    }

    public static void main(String[] args) {
        // new Main().evaluate_multi();
        new Evaluate().evaluate_sample(Settings.data, Settings.qNB, Settings.dbNB, Settings.minLeafNB);
        new Evaluate().evaluate_capacity(Settings.data, Settings.qNB, Settings.dbNB, Settings.sampleNB);
        // new Main().evaluate_one(Settings.data, Settings.qNB, Settings.dbNB,
        // Settings.dim, Settings.minLeafNB, Settings.sampleNB);
    }
}