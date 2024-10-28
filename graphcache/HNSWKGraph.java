package graphcache;

import java.io.IOException;
import java.util.*;
import evaluation.Settings;
import utils.Loader;
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
        // System.out.println("\n添加点: " + p.id);
        if (points.size() >= maxSize) {
            Point oldestPoint = points.remove(0);
            removePoint(oldestPoint, p);
            // System.out.println("移除最旧的点: " + oldestPoint.id);
        }
        if (!points.contains(p)) {
            points.add(p);
            // System.out.println("添加新点: " + p.id);
        }

        int level = assignLevel();
        // System.out.println("分配的层级: " + level);
        PriorityQueue<NN> entryPoints = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));

        for (int l = Math.min(level, maxLevel - 1); l >= 0; l--) {
            int layerM = getLayerM(l);
            // System.out.println("当前层: " + l + ", layerM: " + layerM);
            PriorityQueue<NN> neighbors = findKNNInLayer(p, Math.min(layerM, K), l, entryPoints);
            // System.out.println("找到的邻居数量: " + neighbors.size());
            connectNewPoint(p, neighbors, l);
            // System.out.println("当前层信息：");
            // System.out.println(" 层级：" + l);
            // System.out.println(" 当前层点数：" + layers.get(l).size());
            // System.out.println(" 新添加点的邻居数：" + layers.get(l).get(p).size());
            if (!neighbors.isEmpty()) {
                entryPoints = neighbors;
            }
        }
        // System.out.println("点 " + p.id + " 添加完成");
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
            NN nn = neighbors.poll();
            Point neighbor = nn.point;
            // 为p添加邻居
            pNeighbors.add(neighbor);
            // 为邻居添加p
            ArrayList<Point> neighborNeighbors = currentLayer.get(neighbor);
            if (neighborNeighbors.size() < M) {
                neighborNeighbors.add(p);
            } else {
                if (neighbor.distanceTo(p) < neighbor.distanceTo(neighborNeighbors.get(0))) {
                    neighborNeighbors.remove(0);
                    neighborNeighbors.add(p);
                }
            }
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
        PriorityQueue<NN> candidates = new PriorityQueue<>(Comparator.comparingDouble(a -> a.dist2query));
        PriorityQueue<NN> result = new PriorityQueue<>(Comparator.comparingDouble(a -> -a.dist2query));
        Set<Point> visited = new HashSet<>();

        // System.out.println("初始入口点数量: " + entryPoints.size());

        // 初始化候选点
        if (entryPoints.isEmpty()) {
            if (!currentLayer.isEmpty()) {
                Point randomPoint = currentLayer.keySet().iterator().next();
                double dist = randomPoint.distanceTo(targetPoint);
                updateCalcCount++;
                candidates.add(new NN(randomPoint, dist));
                visited.add(randomPoint);
                // System.out.println("使用随机点作为入口: " + randomPoint.id);
            }
        } else {
            candidates.addAll(entryPoints);
            entryPoints.forEach(nn -> visited.add(nn.point));
            // System.out.println("使用提供的入口点");
        }
        // System.out.println("初始候选点数量: " + candidates.size());

        // 如果当前层级点数较少，直接遍历
        // System.out.println("当前层级点数: " + currentLayer.size() + " 目标k值: " + k);
        if (currentLayer.size() < k) {
            // System.out.println("当前层级点数较少，直接遍历");
            for (Point point : currentLayer.keySet()) {
                double dist = point.distanceTo(targetPoint);
                findCalcCount++;
                visited.add(point);
                result.add(new NN(point, dist));
                if (result.size() >= k) {
                    result.poll();
                }
            }
        } else {
            int iterations = 0;
            while (!candidates.isEmpty()) {
                iterations++;
                NN current = candidates.poll();
                // System.out.println("迭代 " + iterations + ": 当前点 " + current.point.id + ", 距离 "
                // + current.dist2query);

                int currentSize = result.size();
                if (currentSize >= k && current.dist2query > result.peek().dist2query) {
                    // System.out.println("达到终止条件，退出循环");
                    break;
                }
                // 将当前点添加到结果集
                if (currentSize < k) {
                    result.add(current);
                } else if (current.dist2query < result.peek().dist2query) {
                    result.poll();
                    result.add(current);
                }

                ArrayList<Point> neighbors = currentLayer.get(current.point);
                if (neighbors != null && !neighbors.isEmpty()) {
                    // System.out.println(" 邻居数量: " + neighbors.size());
                    for (Point neighbor : neighbors) {
                        if (!visited.contains(neighbor)) {
                            double dist = neighbor.distanceTo(targetPoint);
                            findCalcCount++;
                            visited.add(neighbor);
                            NN newNN = new NN(neighbor, dist);
                            // System.out.println(" 检查邻居 " + neighbor.id + ", 距离 " + dist);
                            // 更新候选点
                            if (result.size() < k) {
                                candidates.add(newNN);
                                // System.out.println("结果集未满 添加到候选点");
                            } else if (dist < result.peek().dist2query) {
                                candidates.add(newNN);
                                // System.out.println("结果集已满 但有更近候选点");
                            }
                        }
                    }
                }

            }
        }
        // System.out.println("搜索完成，最终候选点数量: " + candidates.size());
        // System.out.println("访问的点数: " + visited.size());
        // System.out.println("距离计算次数: " + findCalcCount);

        return result;
    }

    public PriorityQueue<NN> searchLayer(Point targetPoint, int k, Point entryPoint, int layer,
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
        System.out.println("HNSW图参数:");
        System.out.println("  最大层数: " + maxLevel);
        System.out.println("  最大连接数 M: " + M);
        System.out.println("  最大点数: " + maxSize);
        System.out.println();
        System.out.println("HNSW Graph Structure:");
        for (int l = 0; l < maxLevel; l++) {
            System.out.println("Layer " + l + ":");
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            System.out.println(l + " 层级点数: " + currentLayer.size());
            // for (Map.Entry<Point, ArrayList<Point>> entry : currentLayer.entrySet()) {
            // System.out.print(" Point " + entry.getKey().id + " -> ");
            // System.out.print("Neighbors: " + entry.getValue().size() + " [");
            // for (Point p : entry.getValue()) {
            // System.out.print(p.id + " ");
            // }
            // System.out.println("]");
            // }
        }
        System.out.println("Total points: " + points.size());
    }

    // 测试代码
    public static void main(String[] args) throws IOException {
        // 创建HNSWKGraph实例
        int maxLevel = 3;
        int M = 20;
        int maxSize = 1000;
        HNSWKGraph graph = new HNSWKGraph(maxLevel, M, maxSize);

        Loader l = new Loader(Settings.data);
        l.loadData(1000, 1000, 7);

        Point[] queryPoints = l.query.toArray(new Point[l.query.size()]);
        Point[] dbPoints = l.db.toArray(new Point[l.db.size()]);
        for (Point p : dbPoints) {
            graph.addPoint(p, M);
        }

        // 打印图结构
        // graph.printGraph();

        // 执行搜索
        float[] queryVector = { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f };
        Point queryPoint = new Point(-1, queryVector);
        int K = 5;
        PriorityQueue<NN> results = graph.searchLayer(queryPoint, K, null, 0, new PriorityQueue<>());

        // 打印搜索结果
        System.out.println("\n查询点: " + Arrays.toString(queryVector));

        while (!results.isEmpty()) {
            NN nn = results.poll();
            System.out.println(
                    "ID: " + nn.point.id + " vector: " + Arrays.toString(nn.point.vector) + ", 距离: " + nn.dist2query);
        }

    }

}
