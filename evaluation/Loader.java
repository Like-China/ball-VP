package evaluation;

import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;

public class Loader {
    // store all double[] LOCATION in a ArryLiat<double[]> (real values)
    ArrayList<double[]> locations = new ArrayList<>();
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> queries = new ArrayList<>();
    public ArrayList<double[]> db = new ArrayList<>();

    public ArrayList<double[]> loadRealData(String path, double readNB, int dim) {
        // obtain all locations
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
                    vec[id++] = Double.parseDouble(l) + r.nextDouble();
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
        return data;
    }

}
