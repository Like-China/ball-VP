package evaluation;

import java.util.HashMap;

public class Settings {

        // the cache replacement policy
        public static HashMap<String, Integer> myMap = new HashMap<String, Integer>() {
                {
                        put("#DFS", 0);
                        put("#BFS", 1);
                        put("Best-DFS", 2);
                        put("Best-BFS", 3);
                        put("FIFO-DFS", 4);
                        put("FIFO-BFS", 5);
                        put("LRU-DFS", 6);
                        put("LRU-BFS", 7);
                        put("LFU-DFS", 8);
                        put("LFU-BFS", 9);
                        put("BDC-DFS", 10);
                        put("BDC-BFS", 11);
                        put("GLO-DFS", 12);
                        put("GLO-BFS", 13);

                }
        };

        // early stop construction of VP-tree
        public static boolean isEarlyStopConstruct = false;
        public static boolean isUseKmeans = true;
        // the bucket size (leafnode capacity) in the balltree and VP-tree
        public static int qNB = 20000;
        public static int dbNB = 200000;
        // the dimension size
        public static int dim = 15;
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
        public static double factor = 2;
        public static double[] factors = new double[] { 1, 1.5, 2, 2.5, 3 };
        // the cache size (need larger than k and M)
        public static int cacheSize = dbNB / 100;
        public static int[] cacheSizes = new int[] { 1000, 2000, 3000, 4000, 5000 };
        // the update thresold of caching
        public static double updateThreshold = factor;
        public static double[] updateThresholds = new double[] { 2f, 2.5f, 3f, 3.5f, 4f };

        public static String data = "BIGANN";
        public static boolean isShuffle = false;

}
