package evaluation;

import Distance.DistanceFunction;
import Distance.l1Distance;
import Distance.l2Distance;
import Distance.linfDistance;

public class Settings {

        // the node size in the balltree
        public static int minLeafNB = 4;
        public static int dbNB = 100000;
        public static int qNB = 1000;
        public static int dim = 10;
        // the number of selected VP candidates
        public static int sampleNB = 500;
        public static String dirPath = "/home/like/data/";
        // public static String data = "random";
        public static String data = "sift";
        // public static String data = "Deep1M";
        public static boolean isShuffle = true;
        // public static boolean evaluteRandom = false;
        // the number of processed moving objects
        public static DistanceFunction distFunction = new l2Distance();
}