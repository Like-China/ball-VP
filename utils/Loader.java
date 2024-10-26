package utils;

import java.util.*;
import evaluation.Settings;
import java.io.BufferedReader;
import java.io.FileReader;

public class Loader {
    public ArrayList<Point> db = new ArrayList<>();
    public ArrayList<Point> query = new ArrayList<>();

    public void loadData(int qNB, int dbNB, int dim) {
        if (Settings.data == "random") {
            generateRandomdata(qNB, dbNB, dim);
            return;
        }
        String dbPath = Settings.dirPath + Settings.data + "_db.txt";
        ArrayList<double[]> allVectors = loadRealData(dbPath, qNB + dbNB, dim, false);
        for (int i = 0; i < qNB; i++) {
            query.add(new Point(i, allVectors.get(i), true));
        }
        for (int i = qNB; i < qNB + dbNB; i++) {
            db.add(new Point(i, allVectors.get(i), false));
        }

        // String qPath = Settings.dirPath + Settings.data + "_query.txt";
        // query = loadRealData(qPath, qNB, dim, true);
        // String dbPath = Settings.dirPath + Settings.data + "_db.txt";
        // allPoints.addAll(allPoints)
        // db = loadRealData(dbPath, dbNB, dim, false);
    }

    public void generateRandomdata(int qNB, int dbNB, int dim) {
        Random r = new Random(0);
        db = new ArrayList<>();
        query = new ArrayList<>();
        for (int id = 0; id < qNB; id++) {
            double[] point = new double[dim];
            for (int j = 0; j < dim; j++) {
                point[j] = r.nextDouble();
            }
            query.add(new Point(id, point, true));
        }
        for (int id = 0; id < dbNB; id++) {
            double[] point = new double[dim];
            for (int j = 0; j < dim; j++) {
                point[j] = r.nextDouble();
            }
            db.add(new Point(id, point, false));
        }
    }

    public ArrayList<double[]> loadRealData(String path, double readNB, int dim, boolean isQueryPoint) {
        BufferedReader reader;
        ArrayList<double[]> data = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(path));
            String lineString;
            while ((lineString = reader.readLine()) != null && data.size() < readNB) {
                String[] line = lineString.split(",");
                double[] vec = new double[dim];
                int idx = 0;
                for (String l : line) {
                    vec[idx++] = Double.parseDouble(l);
                    if (idx >= dim) {
                        break;
                    }
                }
                data.add(vec);
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        // System.out.printf("data size: %d\n", data.size());
        if (Settings.isShuffle) {
            Collections.shuffle(data);
        }
        return data;

    }

}
