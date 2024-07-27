package evaluation;

public class Settings {

        // the node size in the balltree
        public static int minLeafNB = 4;
        public static int dbNB = 200000;
        public static int qNB = 100;
        public static int dim = 10;
        public static int sampleNB = 100;
        // public static String data = "Geolife";
        // public static String data = "Deep1M";
        public static String dirPath = "/home/like/data/";
        public static String data = "sift";
        public static boolean isShuffle = true;
        public static boolean evaluteRandom = false;
        // the number of processed moving objects
        public static int objectNB = (data == "Geolife") ? 2000 : 10000;
        // public static String geolifePath = "D:/VLDB/Geolife/Data/";
        public static String geolifePath = "/home/like/data/Geolife/Data/";
        public static String portoPath = "/home/like/data/porto.csv";
        // the default and varying number of moving objects
        public static int cardinality = (data == "Geolife") ? 100000 : 20000;
        public static int[] cardinalities = (data == "Geolife") ? new int[] { 100000, 200000, 300000, 400000, 500000 }
                        : new int[] { 20000, 40000, 60000, 80000, 100000 };
        // the default and varying re-partition threshold
        public static double repartitionRatio = 0.3;
        public static double[] repartitionRatios = { 0.1, 0.2, 0.3, 0.4, 0.5 };
}