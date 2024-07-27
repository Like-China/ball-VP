package balltree;

import java.util.*;

public class HBallNode extends BallNode {
    public HBallNode leftNode = null;
    public HBallNode rightNode = null;
    public HBallNode extraNode = null;

    public HBallNode(int id, int idxStart, int idxEnd, double[] pivot, double radius) {
        super(id, idxStart, idxEnd, pivot, radius);
    }

    public HashMap<Integer, double[]> preOrder(HBallNode root, HashMap<Integer, double[]> searchMap) {
        if (root != null) {
            searchMap.put(root.id, root.pivot);
            if (root.leftNode != null) {
                preOrder(root.leftNode, searchMap);
            }
            if (root.extraNode != null) {
                preOrder(root.extraNode, searchMap);
            }
            if (root.rightNode != null) {
                preOrder(root.rightNode, searchMap);
            }
        }
        return searchMap;
    }

    public List<HBallNode> levelOrder(HBallNode root) {
        if (root == null) {
            return null;
        }
        List<HBallNode> allNodes = new ArrayList<>();

        Queue<HBallNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            HBallNode head = queue.poll();
            if (head.leftNode != null) {
                queue.offer(head.leftNode);
            }
            if (head.extraNode != null) {
                queue.offer(head.extraNode);
            }
            if (head.rightNode != null) {
                queue.offer(head.rightNode);
            }
            allNodes.add(head);
        }
        // print balltree internal information
        double radiusSum = 0;
        for (HBallNode node : allNodes) {
            radiusSum += node.radius;
        }
        System.out.println(allNodes.size() + " / " + radiusSum);
        return allNodes;
    }

}
