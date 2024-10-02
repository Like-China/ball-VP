package evaluation;

import Distance.DistanceFunction;
import Distance.l1Distance;
import Distance.l2Distance;
import Distance.linfDistance;

public class Settings {

        // early stop construction of VP-tree
        public static boolean isEarlyStopConstruct = true;
        // the bucket size (leafnode capacity) in the balltree and VP-tree
        public static int dbNB = 1000000;
        public static int qNB = 10000;
        // the dimension size
        public static int dim = 10;
        public static int[] dims = new int[] { 10, 20, 30, 40, 50 };
        // the number of selected VP candidates
        public static int sampleNB = 100;
        public static int[] sampleNBs = new int[] { 100, 200, 300, 400, 500 };
        // the bucket size
        public static int bucketSize = 20;
        public static int[] bucketSizes = new int[] { 10, 20, 30, 40, 50 };
        // the k
        public static int k = 10;
        public static int[] ks = new int[] { 10, 20, 30, 40, 50 };
        // the expand factor
        public static double factor = 3;
        public static double[] factors = new double[] { 1, 1.5, 2, 2.5, 3 };
        // the cache size
        public static int cacheSize = 100;
        // the update thresold of caching
        public static double updateThreshold = 3f;

        public static String dirPath = "/home/like/data/";
        // public static String data = "random";
        public static String data = "sift";
        // public static String data = "Deep1M";
        public static boolean isShuffle = false;
        // the number of processed moving objects
        public static DistanceFunction distFunction = new l2Distance();

}