package evaluation;

import Distance.*;

public class Settings {

        // early stop construction of VP-tree
        public static boolean isEarlyStopConstruct = false;
        // the bucket size (leafnode capacity) in the balltree and VP-tree
        public static int dbNB = 800000;
        public static int qNB = 200000;
        // the dimension size
        public static int dim = 20;
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
        public static double factor = 1;
        public static double[] factors = new double[] { 1, 1.5, 2, 2.5, 3 };
        // the cache size
        public static int cacheSize = 1000;
        public static int[] cacheSizes = new int[] { 1000, 2000, 3000, 4000, 5000 };
        // the update thresold of caching
        public static double updateThreshold = 1.5f;
        public static double[] updateThresholds = new double[] { 2f, 2.5f, 3f, 3.5f, 4f };

        public static String dirPath = "/data/home/like/sift1M/";
        // public static String data = "random";
        public static String data = "sift";
        // public static String data = "Deep1M";
        public static boolean isShuffle = false;
        // the number of processed moving objects
        public static DistanceFunction distFunction = new l2Distance();

}