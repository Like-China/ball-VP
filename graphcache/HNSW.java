package graphcache;

import java.util.*;
import utils.NN;
import utils.Point;

public class HNSW {
    private List<HashMap<Point, ArrayList<Point>>> layers;
    private int maxLevel; // the maximum level of the graph
    private int M; // the maximum connection of a node
    public ArrayList<Point> points;
    public double calcCount = 0; // include the establish and query
    Random random = new Random(10);

    public HNSW(int maxLevel, int M) {
        this.maxLevel = maxLevel;
        this.M = M;
        this.layers = new ArrayList<>(maxLevel);
        for (int i = 0; i < maxLevel; i++) {
            layers.add(new HashMap<>());
        }
        this.points = new ArrayList<>();
    }

    public void removePoint(Point deleteP, Point incomingP) {
        points.remove(deleteP);
        for (int l = 0; l < maxLevel; l++) {
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            if (currentLayer.containsKey(deleteP)) {
                currentLayer.remove(deleteP);
                for (Point neighbor : deleteP.getrKNNs()) {
                    ArrayList<Point> neighborConnections = currentLayer.get(neighbor);
                    if (neighborConnections == null) {
                        continue;
                    }
                    if (neighborConnections.remove(deleteP) && incomingP != null
                            && !neighborConnections.contains(incomingP)) {
                        neighborConnections.add(incomingP);
                    }
                }
            }

            // System.out.println(l + " " + deleteP.id + " " + currentLayer.size());
            // printConnections();
            // 用一个表记录每个点曾经记录的邻居

            // currentLayer.remove(deleteP);
            // Set<Point> layerPoints = currentLayer.keySet();
            // for (Point p : layerPoints) {
            // ArrayList<Point> deletePConnections = currentLayer.get(p);
            // if (deletePConnections.remove(deleteP)) {
            // // System.out.println(p.id + " " + deletePConnections);
            // while (deletePConnections.size() < M) {
            // if (incomingP != null && !deletePConnections.contains(incomingP)) {
            // deletePConnections.add(incomingP);
            // } else {
            // // randomly select a point
            // Point randPoint =
            // layerPoints.stream().skip(random.nextInt(layerPoints.size()))
            // .findFirst()
            // .get();
            // // System.out.println(randPoint);
            // if (randPoint != deleteP && randPoint != p &&
            // !deletePConnections.contains(randPoint)) {
            // deletePConnections.add(randPoint);
            // }
            // }
            // }
            // // System.out.println(p.id + " " + deletePConnections);
            // // System.out.println();
            // }
            // }

        }

    }

    public void addPoint(Point p, int K) {
        // System.out.println("\n添加点: " + p.id);
        if (points.contains(p))
            return;
        points.add(p);
        int level = assignLevel();
        // System.out.println("分配的层级: " + level);
        PriorityQueue<NN> entryPoints = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));

        for (int l = Math.min(level, maxLevel - 1); l >= 0; l--) {
            PriorityQueue<NN> neighbors = findKNNInLayer(p, Math.min(M, K), l, entryPoints);
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
        while (random.nextFloat() < 0.3 && level < maxLevel - 1) {
            level++;
        }
        return level;
    }

    private void connectNewPoint(Point p, PriorityQueue<NN> neighbors, int layer) {
        HashMap<Point, ArrayList<Point>> currentLayer = layers.get(layer);
        ArrayList<Point> pNeighbors = new ArrayList<>();
        currentLayer.put(p, pNeighbors);

        while (!neighbors.isEmpty()) {
            NN nn = neighbors.poll();
            Point neighbor = nn.point;
            // Add neighbor to p's neighbor list
            pNeighbors.add(neighbor);
            p.addrKNNs(neighbor);
            // Add p to neighbor's neighbor list if p is a better neighbor
            ArrayList<Point> neighborNeighbors = currentLayer.get(neighbor);
            if (neighborNeighbors == null) {
                continue;
            }
            if (neighborNeighbors.size() < M) {
                neighborNeighbors.add(p);
                neighbor.addrKNNs(p);
            } else {
                calcCount += 1;
                if (nn.dist2query < neighbor.distanceTo(neighborNeighbors.get(0))) {
                    neighborNeighbors.remove(0);
                    neighborNeighbors.add(p);
                    neighbor.addrKNNs(p);
                }
            }
        }
    }

    public PriorityQueue<NN> findKNN(Point targetPoint, int k) {
        HashMap<Point, ArrayList<Point>> topLayer = layers.get(maxLevel - 1);
        Point entryPoint = null;
        if (!topLayer.isEmpty()) {
            entryPoint = topLayer.keySet().iterator().next();
        }
        PriorityQueue<NN> result = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
        for (int l = maxLevel - 1; l >= 0; l--) {
            result = searchLayer(targetPoint, k, entryPoint, l, result);
            if (l > 0 && !result.isEmpty()) {
                entryPoint = result.peek().point;
            }
        }
        return result;
    }

    public PriorityQueue<NN> searchLayer(Point targetPoint, int k, Point entryPoint, int layer,
            PriorityQueue<NN> initialCandidates) {
        // for maxlayer-1 layer
        if (initialCandidates.isEmpty() && entryPoint != null) {
            double dist = entryPoint.distanceTo(targetPoint);
            calcCount++;
            initialCandidates.add(new NN(entryPoint, dist));
        }
        return findKNNInLayer(targetPoint, k, layer, initialCandidates);
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
                calcCount++;
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
            for (Point point : currentLayer.keySet()) {
                double dist = point.distanceTo(targetPoint);
                calcCount++;
                visited.add(point);
                result.add(new NN(point, dist));
                if (result.size() >= k) {
                    result.poll();
                }
            }
        } else {
            // int iterations = 0;
            while (!candidates.isEmpty()) {
                // iterations++;
                NN current = candidates.poll();
                // System.out.println("迭代 " + iterations + ": 当前点 " + current.point.id + ", 距离 "
                // + current.dist2query);

                int currentSize = result.size();
                if (currentSize >= k && current.dist2query > result.peek().dist2query) {
                    // System.out.println("达到终止条件，退出循环");
                    break;
                }
                // add current checking candidate to the result if it is closer to the target
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
                            calcCount++;
                            visited.add(neighbor);
                            NN newNN = new NN(neighbor, dist);
                            // System.out.println(" 检查邻居 " + neighbor.id + ", 距离 " + dist);
                            // update candidates
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

    public int size() {
        return points.size();
    }

    public void printGraph() {
        System.out.println("HNSW Graph Info:");
        System.out.println("  The Maximum Layer: " + maxLevel);
        System.out.println("  The Maximum Connections: " + M);
        System.out.println("HNSW Graph Structure:");
        for (int l = 0; l < maxLevel; l++) {
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            System.out.println("Layer " + l + "--NB OF Nodes: " + currentLayer.size());
            // for (Map.Entry<Point, ArrayList<Point>> entry : currentLayer.entrySet()) {
            // System.out.print(" Point " + entry.getKey().id + " -> ");
            // System.out.print("Neighbors: " + entry.getValue().size() + " [");
            // for (Point p : entry.getValue()) {
            // System.out.print(p.id + " ");
            // }
            // System.out.println("]");
            // }
        }
    }

    public void printConnections() {
        System.out.println("HNSW Graph Connections:");
        for (int l = 0; l < maxLevel; l++) {
            HashMap<Point, ArrayList<Point>> currentLayer = layers.get(l);
            System.out.println("Layer " + l + "--NB OF Nodes: " + currentLayer.size());
            for (Point p : points) {
                // for (Point p : currentLayer.keySet()) {
                if (!currentLayer.containsKey(p))
                    continue;
                System.out.print(" Point " + p.id + " -> ");
                System.out.print("Neighbors: " + currentLayer.get(p).size() + " [");
                for (Point nn : currentLayer.get(p)) {
                    System.out.print(nn.id + " ");
                }
                System.out.println("]");
            }
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return points.size() + "@" + points;
    }
}
