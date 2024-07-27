package evaluation;

import java.util.ArrayList;
import java.util.Collections;

import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Loader {
    // store all double[] LOCATION in a ArryLiat<double[]> (real values)
    ArrayList<double[]> locations = new ArrayList<>();
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<double[]> queries = new ArrayList<>();
    public ArrayList<double[]> db = new ArrayList<>();

    /**
     * get all trajectory files in the given dierctory
     * 
     * @param fileInput   the directory that stores trajectory files
     * @param allFileList store all trajectory files of the input fileInput in a
     *                    list
     */
    public void getAllFile(File fileInput, List<File> allFileList) {
        File[] fileList = fileInput.listFiles();
        assert fileList != null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                getAllFile(file, allFileList);
            } else {
                if (!Character.isLetter(file.getName().charAt(0))) {
                    allFileList.add(file);
                }
            }
        }
    }

    /**
     * store all double[] location in ArrayList<double[]> locations
     * 
     * @param readObjNum the maximum number of loaded trajectories/moving objects
     * @param maxSpeed   the maximum speed of a moving object to its averaged speed
     */
    public void getAllData(int readObjNum, double maxSpeed) {
        if (Settings.data == "Porto") {
            getPortoData(readObjNum, maxSpeed);
            return;
        }
        File dir = new File(Settings.geolifePath);
        List<File> allFileList = new ArrayList<>();
        if (!dir.exists()) {
            return;
        }
        getAllFile(dir, allFileList);
        Collections.sort(allFileList);
        if (Settings.isShuffle) {
            Collections.shuffle(allFileList);
        }
        // obtain all locations
        BufferedReader reader;
        List<double[]> locations = new ArrayList<>();
        int id = 0;
        for (File f : allFileList) {
            try {
                reader = new BufferedReader(new FileReader(f));
                String lineString = null;
                for (int i = 0; i < 6; i++) {
                    lineString = reader.readLine();
                }
                while ((lineString = reader.readLine()) != null) {
                    String[] line = lineString.split(",");
                    if (line.length > 5) {
                        double real_lat = Double.parseDouble(line[0]);
                        double real_lon = Double.parseDouble(line[1]);
                        String[] hms = line[line.length - 1].split(":");
                        int ts = Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60
                                + Integer.parseInt(hms[2]);
                        locations.add(new double[] { real_lon, real_lat });
                    }
                }
                reader.close();
                id++;
                if (id >= readObjNum)
                    break;
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        System.out.printf("objects: %d, locations: %d", id, locations.size());
    }

    /**
     * store all double[] location in ArrayList<double[]> locations for
     * Porto dataset
     * 
     * @param readObjNum
     * @param maxSpeed
     */
    public void getPortoData(int readObjNum, double maxSpeed) {
        // obtain all locations
        BufferedReader reader;
        List<double[]> locations = new ArrayList<>();
        int id = 0;
        try {
            reader = new BufferedReader(new FileReader(Settings.portoPath));
            String lineString = reader.readLine();
            while ((lineString = reader.readLine()) != null) {
                String[] line = lineString.split("\\[\\[");
                if (line.length < 2)
                    continue;
                if (id >= readObjNum)
                    break;
                line = line[1].split("],");
                int ts = 0;
                for (String l : line) {
                    l = l.replace("[", "");
                    l = l.replace("]", "");
                    l = l.replace("\"", "");
                    String[] lonlat = l.split(",");
                    double real_lon = Double.parseDouble(lonlat[0]);
                    double real_lat = Double.parseDouble(lonlat[1]);
                    locations.add(new double[] { real_lon, real_lat });
                    ts += 15;
                }
                id++;
            }
            reader.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        System.out.printf("objects: %d, locations: %d", id, locations.size());
    }

    //
    //

    /**
     * first run getAllData() to fill ArrayList<double[]> locations, then run
     * getBatch()
     * to get a batch of double[]
     * 
     * @param cardinality the query size, the remaining is stored in the database
     */
    public void getBatch(int cardinality) {
        queries = new ArrayList<>();
        db = new ArrayList<>();
        int size = locations.size();
        assert size >= 2 * cardinality : "Lack of double[]!";
        // if not shuffle, sequencely read, else shuffle double[]
        if (Settings.isShuffle)
            Collections.shuffle(locations);
        for (int i = 0; i < 2 * cardinality; i++) {
            double[] loc = locations.get(i);
            if (i < cardinality) {
                queries.add(loc);
            } else {
                db.add(loc);
            }
        }
    }

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
