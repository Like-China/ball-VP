package evaluation;

public class Settings {

        // the node size in the balltree
        public static int minLeafNB = 4;
        public static int dbNB = 200000;
        public static int qNB = dbNB / 20;
        public static int dim = 30;
        public static int sampleNB = 100;
        public static String dirPath = "/home/like/data/";
        public static String data = "random";
        // public static String data = "sift";
        // public static String data = "Deep1M";
        public static boolean isShuffle = true;
        // public static boolean evaluteRandom = false;
        // the number of processed moving objects
}