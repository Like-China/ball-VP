package balltree;

import java.util.ArrayList;
import java.util.Arrays;

import Distance.l2Distance;

public class BallTree {
    public int minLeafNB;
    public int pointNB;
    public double[][] db;
    public int[] indexes = null;
    // the number of node access when constructing the ball-tree
    public int constructCount = 0;
    // the number of node access when conduct queries
    public int searchCount = 0;
    public int calcCount = 0;
    l2Distance dist = new l2Distance();

    // for NN search
    private double tau;
    private double[] best;
    private ArrayList<double[]> RangeRes;
    private int dim = 2;

    public BallTree(int minLeafNB, double[][] db) {
        this.minLeafNB = minLeafNB;
        this.pointNB = db.length;
        this.indexes = new int[pointNB];
        this.db = db;
        this.dim = db[0].length;
    }

    public double[] getPivot(int[] indexes, int idxStart, int idxEnd) {
        assert idxEnd + 1 >= idxStart;
        double[] pivot = new double[dim];
        Arrays.fill(pivot, 0);
        for (int i = idxStart; i < idxEnd + 1; i++) {
            for (int j = 0; j < dim; j++) {
                pivot[j] = pivot[j] + db[indexes[i]][j];
            }
        }
        int length = idxEnd - idxStart + 1;
        for (int i = 0; i < dim; i++) {
            pivot[i] = pivot[i] / length;
        }
        return pivot;
    }

    public double getRadius(int[] indexes, int idxStart, int idxEnd, double[] pivot) {
        // the maximum distance between pivot and other points centered at pivot
        double radius = 0;
        for (int i = idxStart; i < idxEnd + 1; i++) {
            // the distance between the center of ellipse and pivot+the radius of ellipse
            double temp = this.dist.distance(pivot, db[indexes[i]]);
            if (temp > radius) {
                radius = temp;
            }
        }
        return radius;
    }

    public BallNode buildBallTree() {
        // assert this.minLeafNB >= 10;
        for (int i = 0; i < pointNB; i++) {
            this.indexes[i] = i;
        }
        double[] pivot = this.getPivot(this.indexes, 0, pointNB - 1);
        double radius = this.getRadius(this.indexes, 0, pointNB - 1, pivot);
        BallNode root = new BallNode(1, 0, pointNB - 1, pivot, radius);
        try {
            this.localBuildBallTree(root, 1);
        } catch (StackOverflowError e) {
            System.out.println("Stackoverflow: " + constructCount);
        }
        return root;
    }

    public void localBuildBallTree(BallNode node, int depth) {
        if (node.idxEnd - node.idxStart + 1 <= this.minLeafNB) {
            return;
        } else {
            constructCount++;
            this.splitNode(node);
            this.localBuildBallTree(node.leftNode, depth + 1);
            this.localBuildBallTree(node.rightNode, depth + 1);
        }
    }

    public void splitNode(BallNode node) {
        // 1.get furthest1: furthest far from node's pivot
        double maxDist = 0.0;
        double tempDist = 0.0;
        int idx = node.idxStart;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = dist.distance(db[indexes[i]], node.pivot);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest1 = db[this.indexes[idx]];
        // 2.get furthest2: furthest far from furthest1
        idx = node.idxStart;
        maxDist = 0;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = dist.distance(db[indexes[i]], furthest1);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest2 = db[this.indexes[idx]];
        // 3.update indexes, split node
        int split = node.idxEnd;
        idx = node.idxStart;
        while (idx <= split) {
            maxDist = dist.distance(db[this.indexes[idx]], furthest1);
            tempDist = dist.distance(db[this.indexes[idx]], furthest2);
            if (maxDist >= tempDist) {
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
        // 4.update node info
        // leftNode:idxStart->split-1, rightNode:split->idxEnd
        double[] pivotL = getPivot(indexes, node.idxStart, split - 1);
        double radiusL = this.getRadius(indexes, node.idxStart, split - 1, pivotL);
        node.leftNode = new BallNode(node.id * 2, node.idxStart, split - 1, pivotL, radiusL);
        double[] pivotR = this.getPivot(indexes, split, node.idxEnd);
        double radiusR = this.getRadius(indexes, split, node.idxEnd, pivotR);
        node.rightNode = new BallNode(node.id * 2 + 1, split, node.idxEnd, pivotR, radiusR);
    }

    public ArrayList<double[]> searchRange(BallNode node, double[] q, double range) {
        RangeRes = new ArrayList<>();
        this._searchRange(node, q, range);
        return RangeRes;
    }

    public double[] searchNN(BallNode node, double[] q) {
        tau = Double.MAX_VALUE;
        best = q;
        this._searchNN(node, q);
        return best;
    }

    public void _searchRange(BallNode node, double[] q, double range) {
        if (node == null)
            return;
        searchCount++;
        calcCount++;
        double nodeDist = this.dist.distance(q, node.pivot) - node.radius;
        if (nodeDist > range) {
            return;
        }
        if (node.leftNode != null && node.rightNode != null) {
            double leftPivotDist = this.dist.distance(q, node.leftNode.pivot);
            double rightPivotDist = this.dist.distance(q, node.rightNode.pivot);
            calcCount += 2;
            double leftBallDist = leftPivotDist - node.leftNode.radius;
            double rightBallDist = rightPivotDist - node.rightNode.radius;
            if (leftBallDist <= range) {
                _searchRange(node.leftNode, q, range);
            }
            if (rightBallDist <= range) {
                _searchRange(node.rightNode, q, range);
            }
        } else if (node.leftNode != null || node.rightNode != null) {
            System.out.println("This node only one leaf, Unreasonable!");
        } else if (node.leftNode == null && node.rightNode == null) { // reach leaf node
            for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
                double[] candidate = db[indexes[i]];
                double dist = this.dist.distance(q, candidate);
                calcCount++;
                if (dist < range) {
                    RangeRes.add(candidate);
                }
            }
        } else {
            System.out.println("No rangeNN Found:" + node.id + " " + node.radius);
        }
    }

    public void _searchNN(BallNode node, double[] q) {
        searchCount++;
        calcCount++;
        double nodeDist = this.dist.distance(q, node.pivot) - node.radius;
        if (nodeDist > tau) {
            return;
        }
        if (node.leftNode != null && node.rightNode != null) {
            calcCount += 2;
            double leftPivotDist = this.dist.distance(q, node.leftNode.pivot);
            double rightPivotDist = this.dist.distance(q, node.rightNode.pivot);
            double leftBallDist = leftPivotDist - node.leftNode.radius;
            double rightBallDist = rightPivotDist - node.rightNode.radius;
            if (leftBallDist < tau) {
                _searchNN(node.leftNode, q);
            }
            if (rightBallDist < tau) {
                _searchNN(node.rightNode, q);
            }
        } else if (node.leftNode != null || node.rightNode != null) {
            System.out.println("This node only one leaf, Unreasonable!");
        } else if (node.leftNode == null && node.rightNode == null) { // reach leaf node
            for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
                double[] candidate = db[indexes[i]];
                double dist = this.dist.distance(q, candidate);
                calcCount++;
                if (dist < tau) {
                    best = candidate;
                    tau = dist;
                }
            }
        } else {
            System.out.println("No NN Found:" + node.id + " " + node.radius);
        }
    }

}
