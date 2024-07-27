package balltree;

import java.util.*;


public class HBallTree extends BallTree {

    public double repartirionRatio = -1;

    public HBallTree(int minLeafNB, ArrayList<double[]> db, double repartirionRatio) {
        super(minLeafNB, db);
        this.repartirionRatio = repartirionRatio;
    }

    public HBallNode buildBallTree() {
        // assert this.minLeafNB >= 10;
        for (int i = 0; i < pointNB; i++) {
            this.indexes[i] = i;
        }
        double[] pivot = this.getPivot(this.indexes, 0, pointNB - 1);
        double radius = this.getRadius(this.indexes, 0, pointNB - 1, pivot);
        HBallNode root = new HBallNode(1, 0, pointNB - 1, pivot, radius);
        try {
            this.localBuildBallTree(root, 1);
        } catch (StackOverflowError e) {
            System.out.println("Stackoverflow: " + constructCount);
        }
        return root;
    }

    public void localBuildBallTree(HBallNode node, int depth) {
        if (node.idxEnd - node.idxStart + 1 < super.minLeafNB) {
            return;
        } else {
            constructCount++;
            this.splitNode(node);
            this.localBuildBallTree(node.leftNode, depth + 1);
            this.localBuildBallTree(node.rightNode, depth + 1);
            if (node.extraNode != null) {
                this.localBuildBallTree(node.extraNode, depth + 1);
            }
        }
    }

    public void splitNode(HBallNode node) {
        // 1.get furthest1: furthest far from node's pivot
        double maxDist = 0.0;
        double tempDist = 0.0;
        int idx = node.idxStart;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = dist.distance(input.get(this.indexes[i]), node.pivot);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest1 = input.get(this.indexes[idx]);
        // 2.get furthest2: furthest far from furthest1
        // PriorityQueue<Point> candidate = new PriorityQueue<>(cmp_point);
        idx = node.idxStart;
        maxDist = 0;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = dist.distance(input.get(this.indexes[i]), furthest1);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest2 = input.get(this.indexes[idx]);

        // 3.update indexes
        int split = node.idxEnd;
        idx = node.idxStart;
        double sum = node.idxEnd - node.idxStart + 1;

        while (idx <= split) {
            maxDist = dist.distance(input.get(this.indexes[idx]), furthest1);
            tempDist = dist.distance(input.get(this.indexes[idx]), furthest2);
            if (maxDist > tempDist) {
                int temp = this.indexes[idx];
                this.indexes[idx] = this.indexes[split];
                if (idx != split) {
                    this.indexes[split] = temp;
                    split = split - 1;
                } else {
                    idx = idx + 1;
                }
            } else {
                idx = idx + 1;
            }
        }

        // if it is highly imbalanced, adjust the child to be ternary
        double[] pivotL = null, pivotE = null, pivotR = null;
        double radiusL = 0, radiusE = 0, radiusR = 0;
        int leftWeight = split - node.idxStart;
        int rightWeight = node.idxEnd - split + 1;
        double diff = Math.abs(leftWeight - rightWeight);

        if (diff / sum >= repartirionRatio && leftWeight > minLeafNB && rightWeight > minLeafNB) {
            int split1, split2;
            // get a new partition node
            double[] newNode = null;
            double[] anchor = new double[2];
            anchor[0] = (furthest1[0] + furthest2[0]) / 2;
            anchor[1] = (furthest1[1] + furthest2[1]) / 2;
            double minDist = 10000;
            if (leftWeight > rightWeight) {
                // get a new node for partition
                for (int i = node.idxStart; i < split; i++) {
                    tempDist = dist.distance(input.get(this.indexes[i]), anchor);
                    if (tempDist < minDist) {
                        minDist = tempDist;
                        newNode = input.get(this.indexes[i]);
                    }
                }
                split2 = split;
                // 3.update indexes, really split node
                idx = node.idxStart;
                while (idx <= split) {
                    maxDist = dist.distance(input.get(this.indexes[idx]), newNode);
                    tempDist = dist.distance(input.get(this.indexes[idx]), furthest1);
                    if (maxDist > tempDist) {
                        int temp = this.indexes[idx];
                        this.indexes[idx] = this.indexes[split];
                        if (idx != split) {
                            this.indexes[split] = temp;
                            split = split - 1;
                        } else {
                            idx = idx + 1;
                        }
                    } else {
                        idx = idx + 1;
                    }
                }
                split1 = split;
            } else {
                for (int i = split; i < node.idxEnd; i++) {
                    tempDist = dist.distance(input.get(this.indexes[i]), anchor);
                    if (tempDist < minDist) {
                        minDist = tempDist;
                        newNode = input.get(this.indexes[i]);
                    }
                }
                split1 = split;
                // 3.update indexes, really split node
                idx = split;
                split = node.idxEnd;
                while (idx <= split) {
                    maxDist = dist.distance(input.get(this.indexes[idx]), newNode);
                    tempDist = dist.distance(input.get(this.indexes[idx]), furthest2);
                    if (maxDist > tempDist) {
                        int temp = this.indexes[idx];
                        this.indexes[idx] = this.indexes[split];
                        if (idx != split) {
                            this.indexes[split] = temp;
                            split = split - 1;
                        } else {
                            idx = idx + 1;
                        }
                    } else {
                        // rightDists[idx - node.idxStart] = tempDist;
                        idx = idx + 1;
                    }
                }
                split2 = split;
            }
            // System.out.println(node.idxStart+"/"+split1+"/"+split2+"/"+node.idxEnd);
            // update node info
            pivotL = getPivot(indexes, node.idxStart, split1 - 1);
            radiusL = this.getRadius(indexes, node.idxStart, split1 - 1,
                    pivotL);
            if (split1 != split2) {
                pivotE = getPivot(indexes, split1, split2 - 1);
                radiusE = this.getRadius(indexes, split1, split2 - 1,
                        pivotE);
                node.extraNode = new HBallNode(node.id * 3 + 1, split1, split2 - 1, pivotE, radiusE);
            }
            pivotR = this.getPivot(indexes, split2, node.idxEnd);
            radiusR = this.getRadius(indexes, split2, node.idxEnd, pivotR);
            node.leftNode = new HBallNode(node.id * 3, node.idxStart, split1 - 1, pivotL, radiusL);
            node.rightNode = new HBallNode(node.id * 3 + 2, split2, node.idxEnd, pivotR, radiusR);
        } else {
            // 4.update node info leftNode:idxStart->split-1, rightNode:split->idxEnd
            pivotL = getPivot(indexes, node.idxStart, split - 1);
            radiusL = this.getRadius(indexes, node.idxStart, split - 1, pivotL);
            pivotR = this.getPivot(indexes, split, node.idxEnd);
            radiusR = this.getRadius(indexes, split, node.idxEnd, pivotR);
            node.leftNode = new HBallNode(node.id * 3, node.idxStart, split - 1, pivotL, radiusL);
            node.rightNode = new HBallNode(node.id * 3 + 1, split, node.idxEnd, pivotR, radiusR);
        }
    }

    public ArrayList<double[]> searchRange(HBallNode node, double[] qVector, double range) {
        ArrayList<double[]> res = new ArrayList<>();
        this.rangeSearch(node, qVector, res, range);
        return res;
    }

    public void rangeSearch(HBallNode node, double[] qVector, ArrayList<double[]> res, double range) {
        searchCount++;
        double[] target = qVector;
        double nodeDist = this.dist.distance(target, node.pivot) - node.radius;
        if (nodeDist > range) {
            return;
        }
        if (node.leftNode != null && node.rightNode != null) {
            double leftPivotDist = this.dist.distance(target, node.leftNode.pivot);
            double rightPivotDist = this.dist.distance(target, node.rightNode.pivot);
            double leftBallDist = leftPivotDist - node.leftNode.radius ;
            double rightBallDist = rightPivotDist - node.rightNode.radius;
            if (node.extraNode != null) {
                double extraPivotDist = this.dist.distance(target, node.extraNode.pivot);
                double extraBallDist = extraPivotDist - node.extraNode.radius;
                if (extraBallDist <= range) {
                    rangeSearch(node.extraNode, qVector, res, range);
                }
            }
            if (leftBallDist <= range) {
                rangeSearch(node.leftNode, qVector, res, range);
            }
            if (rightBallDist <= range) {
                rangeSearch(node.rightNode, qVector, res, range);
            }
        } else if (node.leftNode != null || node.rightNode != null) {
            System.out.println("This node only one leaf, Unreasonable!");
        } else if (node.leftNode == null && node.rightNode == null) {
            for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
                double[] candidate = input.get(indexes[i]);
                double dist = this.dist.distance(target, candidate);
                if (dist <= range) {
                    res.add(candidate);
                }
            }
        } else {
            System.out.println("Search unsuccessfully:" + node.id + " " + node.radius);
        }
    }

}
