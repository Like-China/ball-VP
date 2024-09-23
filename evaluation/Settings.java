package evaluation;

import Distance.DistanceFunction;
import Distance.l1Distance;
import Distance.l2Distance;
import Distance.linfDistance;

public class Settings {

        // early stop construction of VP-tree
        public static boolean isEarlyStopConstruct = true;
        // the bucket size (leafnode capacity) in the balltree and VP-tree
        public static int bucketSize = 20;
        public static int dbNB = 1000000;
        public static int qNB = 1000;
        public static int dim = 10;
        // the number of selected VP candidates
        public static int sampleNB = 10;
        public static String dirPath = "/home/like/data/";
        // public static String data = "random";
        public static String data = "sift";
        // public static String data = "Deep1M";
        public static boolean isShuffle = false;
        // the number of processed moving objects
        public static DistanceFunction distFunction = new l2Distance();

}