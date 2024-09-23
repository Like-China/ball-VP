package VPTree;

import java.util.Comparator;

public class Comp {

    public static Comparator<VPNode> NNComparator1 = new Comparator<VPNode>() {
        @Override
        public int compare(VPNode p1, VPNode p2) {
            return p1.distLowerBound - p2.distLowerBound > 0 ? -1 : 1;
        }
    };

    public static Comparator<NN> NNComparator2 = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.dist2query - p2.dist2query > 0 ? -1 : 1;
        }
    };
}
