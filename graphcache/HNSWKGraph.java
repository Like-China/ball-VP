package graphcache;

import java.util.*;
import utils.NN;
import utils.Point;

public class HNSWKGraph {
    private List<HashMap<Point, ArrayList<Point>>> layers;
    private int maxLevel;
    private int M; // 最大连接数
    private int maxSize; // 图中最大点数
    public ArrayList<Point> points;
    public double findCalcCount = 0;
    public double updateCalcCount = 0;

    public HNSWKGraph(int maxLevel, int M, int maxSize) {
        this.maxLevel = maxLevel;
        this.M = M;
        this.maxSize = maxSize;
        this.layers = new ArrayList<>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            layers.add(new HashMap<>());
        }
        this.points = new ArrayList<>();
    }

    public void removePoint(Point deleteP, Point incomingP) {
        if (!points.contains(deleteP)) {
            return;
        }
        points.remove(deleteP);
        for (int l = 0; l < maxLevel; l++) {
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            if (currentLayer.containsKey(deleteP)) {
                ArrayList<Point> neighbors = currentLayer.remove(deleteP);
                for (Point neighbor : neighbors) {
                    ArrayList<Point> neighborConnections = currentLayer.get(neighbor);
                    neighborConnections.remove(deleteP);
                    if (incomingP != null && !neighborConnections.contains(incomingP)) {
                        neighborConnections.add(incomingP);
                    }
                }
            }
        }
    }

    public void addPoint(Point p, int K) {
        if (points.size() >= maxSize) {
            Point oldestPoint = points.remove(0);
            removePoint(oldestPoint, p);
        }
        if (!points.contains(p)) {
            points.add(p);
        }

        int level = assignLevel();
        PriorityQueue<NN> entryPoints = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));

        for (int l = Math.min(level, maxLevel - 1); l >= 0; l--) {
            int layerM = getLayerM(l);
            PriorityQueue<NN> neighbors = findKNNInLayer(p, Math.min(layerM, K), l, entryPoints);
            connectNewPoint(p, neighbors, l);
            if (!neighbors.isEmpty()) {
                entryPoints = neighbors;
            }
        }
    }

    private int assignLevel() {
        int level = 0;
        while (Math.random() < 0.5 && level < maxLevel - 1) {
            level++;
        }
        return level;
    }

    private int getLayerM(int layer) {
        return Math.max(M, (int) (M / Math.pow(2, maxLevel - layer - 1)));
    }

    private void connectNewPoint(Point p, PriorityQueue<NN> neighbors, int layer) {
        HashMap<Point, ArrayList<Point>> currentLayer = layers.get(layer);
        ArrayList<Point> pNeighbors = new ArrayList<>();
        currentLayer.put(p, pNeighbors);

        while (!neighbors.isEmpty()) {
            Point neighbor = neighbors.poll().point;
            pNeighbors.add(neighbor);
            currentLayer.get(neighbor).add(p);
        }
    }

    public PriorityQueue<NN> findKNN(Point targetPoint, int k) {
        Point entryPoint = getEntryPoint();
        PriorityQueue<NN> result = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, b.dist2query));

        for (int l = maxLevel - 1; l >= 0; l--) {
            result = searchLayer(targetPoint, k, entryPoint, l, result);
            if (l > 0 && !result.isEmpty()) {
                entryPoint = result.peek().point;
            }
        }

        return result;
    }

    private Point getEntryPoint() {
        for (int l = maxLevel - 1; l >= 0; l--) {
            if (!layers.get(l).isEmpty()) {
                return layers.get(l).keySet().iterator().next();
            }
        }
        return null;
    }

    private PriorityQueue<NN> findKNNInLayer(Point targetPoint, int k, int layer, PriorityQueue<NN> entryPoints) {
        HashMap<Point, ArrayList<Point>> currentLayer = layers.get(layer);
        PriorityQueue<NN> candidates = new PriorityQueue<>(Comparator.comparingDouble(a -> -a.dist2query));
        Set<Point> visited = new HashSet<>();

        // 初始化候选点
        if (entryPoints.isEmpty()) {
            if (!currentLayer.isEmpty()) {
                Point randomPoint = currentLayer.keySet().iterator().next();
                double dist = randomPoint.distanceTo(targetPoint);
                updateCalcCount++;
                candidates.add(new NN(randomPoint, dist));
                visited.add(randomPoint);
            }
        } else {
            candidates.addAll(entryPoints);
            entryPoints.forEach(nn -> visited.add(nn.point));
        }

        // 主循环：探索和更新候选点
        while (!candidates.isEmpty()) {
            NN current = candidates.poll();
            if (candidates.size() >= k && current.dist2query > candidates.peek().dist2query) {
                break;
            }

            ArrayList<Point> neighbors = currentLayer.get(current.point);
            if (neighbors != null) {
                for (Point neighbor : neighbors) {
                    if (!visited.contains(neighbor)) {
                        double dist = neighbor.distanceTo(targetPoint);
                        findCalcCount++;
                        visited.add(neighbor);
                        NN newNN = new NN(neighbor, dist);
                    
                        // 更新候选点
                        if (candidates.size() < k) {
                            candidates.add(newNN);
                        } else if (dist < candidates.peek().dist2query) {
                            candidates.poll();
                            candidates.add(newNN);
                        }
                    }
                }
            }
        }

        return candidates;
    }

    private PriorityQueue<NN> searchLayer(Point targetPoint, int k, Point entryPoint, int layer,
            PriorityQueue<NN> initialCandidates) {
        if (initialCandidates.isEmpty() && entryPoint != null) {
            double dist = entryPoint.distanceTo(targetPoint);
            initialCandidates.add(new NN(entryPoint, dist));
        }
        return findKNNInLayer(targetPoint, k, layer, initialCandidates);
    }

    public int size() {
        return points.size();
    }

    public void printGraph() {
        System.out.println("HNSW Graph Structure:");
        for (int l = 0; l < maxLevel; l++) {
            System.out.println("Layer " + l + ":");
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            for (Map.Entry<Point, ArrayList<Point>> entry : currentLayer.entrySet()) {
                System.out.print("  Point " + entry.getKey().id + " -> ");
                System.out.print("Neighbors: " + entry.getValue().size() + " [");
                for (Point p : entry.getValue()) {
                    System.out.print(p.id + " ");
                }
                System.out.println("]");
            }
        }
        System.out.println("Total points: " + points.size());
    }
}
