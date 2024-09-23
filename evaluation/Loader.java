package evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

public class Loader {
    public ArrayList<double[]> db = new ArrayList<>();
    public ArrayList<double[]> query = new ArrayList<>();

    public void loadData(int qNB, int dbNB, int dim) {
        if (Settings.data == "random") {
            generateRandomdata(qNB, dbNB, dim);
            return;
        }
        String dbPath = Settings.dirPath + Settings.data + "_db.txt";
        db = loadRealData(dbPath, dbNB, dim);
        String qPath = Settings.dirPath + Settings.data + "_query.txt";
        query = loadRealData(qPath, qNB, dim);
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

    public ArrayList<double[]> loadRealData(String path, double readNB, int dim) {
        BufferedReader reader;
        Random r = new Random();
        ArrayList<double[]> data = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(path));
            String lineString;
            while ((lineString = reader.readLine()) != null) {
                String[] line = lineString.split(",");
                double[] vec = new double[dim];
                int id = 0;
                for (String l : line) {
                    // vec[id++] = Double.parseDouble(l) + r.nextDouble();
                    vec[id++] = Double.parseDouble(l);
                    if (id >= dim) {
                        break;
                    }
                }
                data.add(vec);
                if (data.size() >= readNB) {
                    break;
                }
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
