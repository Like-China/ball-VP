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
        public static int qNB = 50000;
        public static int[] qNBs = new int[] { 10000, 20000, 30000, 40000, 500000 };
        public static int dbNB = 50000;
        public static int[] dbNBs = new int[] { 100000, 200000, 300000, 400000, 5000000 };
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
        // the cache size (need larger than k and M)
        public static int cacheSize = dbNB / 100;
        public static int[] cacheSizes = new int[] { dbNB * 1 / 100, dbNB * 2 / 100, dbNB * 3 / 100, dbNB * 4 / 100,
                        dbNB * 5 / 100 };
        // the update thresold of caching
        public static double updateThreshold = 2f;
        public static double[] updateThresholds = new double[] { 1, 1.5, 2, 2.5, 3 };
        public static String data = "QCL";
        public static boolean isShuffle = false;

}
