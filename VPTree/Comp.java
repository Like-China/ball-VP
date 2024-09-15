package VPTree;

import java.util.Comparator;

public class Comp {
    // public static Comparator<NN> NNComparator1 = new Comparator<NN>() {
    // @Override
    // public int compare(NN p1, NN p2) {
    // if (p1.simUpper1 - p2.simUpper1 > 0) {
    // return -1;
    // } else if (p1.simUpper1 == p2.simUpper1) {
    // return p1.mrb.objectID > p1.mrb.objectID ? -1 : 1;
    // } else {
    // return -1;
    // }
    // }
    // };

    public static Comparator<VPNode> NNComparator1 = new Comparator<VPNode>() {
        @Override
        public int compare(VPNode p1, VPNode p2) {
            return p1.getMu() - p2.getMu() > 0 ? -1 : 1;
        }
    };

    public static Comparator<NN> NNComparator2 = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.dist2query - p2.dist2query > 0 ? -1 : 1;
        }
    };
}
