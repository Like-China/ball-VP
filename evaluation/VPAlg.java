package evaluation;

import java.util.*;
import VPTree.*;
import graphcache.HNSW;
import utils.NN;
import utils.Point;

public class VPAlg {
    /// query vectors, database vectors
    public Point[] qData;
    public Point[] dbData;
    // the number of node accesses (Deep-First/Best-first + Hier/recursion + Cache)
    public HashMap<String, Integer> myMap = Settings.myMap;

    public long[] nodeAccessOfEachMethod = new long[myMap.size()];
    public long[] calcCountOfEachMethod = new long[myMap.size()];
    public double[] timeOfEachMethod = new double[myMap.size()];

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp = null;
    double t1, t2;
    int n = 0;

    // statistics data
    double initSearchTime = 0; // time to get an initial kNN distance
    int initSearchCount = 0;
    double searchTime = 0; // time to search exact kNN using VP-tree
    double updateTime = 0; // time to update cache
    int updateCount = 0; // when the cache cannot generate a high-quality initial kNN, update the cache

    public VPAlg(Point[] qData, Point[] dbData, int sampleNB, int bucketSize) {
        this.qData = qData;
        this.dbData = dbData;
        this.sampleNB = sampleNB;
        n = qData.length;
        // tree construction
        t1 = System.currentTimeMillis();
        vp = new VPTreeBySample(dbData, sampleNB, bucketSize);
        vp.getLayerNB(vp.root);
        vp.firstOrderVisit(vp.root);
        t2 = System.currentTimeMillis();
        System.out.println("VP in  " + (t2 - t1) + " ms: " + vp.nodeNB + " nodes " + vp.layerNB + " layers");
    }

    public void init() {
        for (int i = 0; i < myMap.size(); i++) {
            nodeAccessOfEachMethod[i] = 0;
            calcCountOfEachMethod[i] = 0;
            timeOfEachMethod[i] = 0;
        }
        for (Point p : qData) {
            p.init();
        }
        vp.nodeAccess = 0;
        vp.calcCount = 0;
        initSearchTime = 0; // time to get an initial kNN distance
        initSearchCount = 0;
        searchTime = 0; // time to search exact kNN using VP-tree
        updateTime = 0; // time to update cache
        updateCount = 0;
    }

    public ArrayList<PriorityQueue<NN>> DFS_BFS(String cacheStrategy, int k, boolean useInitkNN,
            double updateThreshold, boolean useBFS) {
        init();
        t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        for (Point queryPoint : qData) {
            double maxKdist = Double.MAX_VALUE;
            if (useInitkNN) {
                PriorityQueue<NN> nns = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
                Random r = new Random(10);
                for (int i = 0; i < Settings.cacheSize; i++) {
                    int randomIdx = r.nextInt(dbData.length);
                    Point db = dbData[randomIdx];
                    double dist = queryPoint.distanceTo(db);
                    if (nns.size() < k) {
                        nns.add(new NN(db, dist));
                    } else {
                        if (nns.peek().dist2query > dist) {
                            nns.poll();
                            nns.add(new NN(db, dist));
                        }
                    }
                }
                assert nns.size() == k;
                maxKdist = nns.peek().dist2query;
            }
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query > updateThreshold) {
                updateCount += 1;
            }
        }
        t2 = System.currentTimeMillis();
        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;
        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | search time |\n%10d %10d %10d \t%8.4fms ",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, timeOfEachMethod[methodID]);
        System.out.println(info);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        init();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        ArrayList<Point> cachedQueryPoints = new ArrayList<>(); // initial a cached queryPoint points
        long start, end;
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1. use cached point to get a initial kNN distance
            start = System.currentTimeMillis();
            double minDist = Double.MAX_VALUE;
            double maxKdist = Double.MAX_VALUE;
            Point minPP = null;
            for (Point pp : cachedQueryPoints) {
                double dist = pp.distanceTo(queryPoint);
                initSearchCount++;
                if (dist < minDist) {
                    minDist = dist;
                    minPP = pp;
                }
            }
            if (minPP != null) {
                maxKdist = 0;
                for (NN nn : minPP.NNs) {
                    double dist = nn.point.distanceTo(queryPoint);
                    initSearchCount++;
                    if (dist >= maxKdist) {
                        maxKdist = dist;
                    }
                }
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);
            // 2. search exact kNN using VP-tree
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            long nodeAccessAfter = vp.nodeAccess;
            end = System.currentTimeMillis();
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query > updateThreshold) {
                            updateCount += 1;
                            cachedQueryPoints.remove(0);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        } else {
                            updateCount += 1;
                            cachedQueryPoints.remove(0);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            minPP.addHitCount();
                        } else {
                            // remove the query point with minimal hit count
                            updateCount += 1;
                            double minExpense = Double.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                double e = pp.hitCount;
                                if (e < minExpense) {
                                    minExpense = e;
                                    removeP = pp;
                                }
                            }
                            cachedQueryPoints.remove(removeP);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    // for (NN nn : nns) {
                    // Point objectPoint = nn.point;
                    // objectPoint.addrKNNs(queryPoint);
                    // objectPoint.addHitCount();
                    // }
                    queryPoint.ts = i;
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);

                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {

                            minPP.addHitCount();
                        } else {
                            updateCount += 1;
                            double minExpense = Double.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                assert i - pp.ts > 0;
                                double e = pp.expense;
                                if (e < minExpense) {
                                    minExpense = e;
                                    removeP = pp;
                                }
                            }

                            // double minRkNN = Double.MAX_VALUE;
                            // Point removeP = cachedQueryPoints.get(0);
                            // for (Point pp : cachedQueryPoints) {
                            // int hitSum = 0;
                            // for (NN nn : pp.NNs) {
                            // hitSum += nn.point.hitCount;
                            // }
                            // if (hitSum < minRkNN) {
                            // minRkNN = hitSum;
                            // removeP = pp;
                            // }
                            // }

                            cachedQueryPoints.remove(removeP);
                            cachedQueryPoints.add(queryPoint);
                        }

                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    cachedQueryPoints.add(queryPoint);
                    if (maxKdist / nns.peek().dist2query > updateThreshold) {
                        updateCount += 1;
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();
            updateTime += (end - start);
        }
        if (cacheStrategy != "GLO-DFS" || cacheStrategy != "GLO-BFS") {
            assert cachedQueryPoints.size() <= cacheSize;
        } else {
            assert cachedQueryPoints.size() == qData.length;
        }
        t2 = System.currentTimeMillis();

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | init search | init search-time | vp search time | cache update time | run time |\n%10d %10d    %10d %10d %15.4fms %15.4fms %15.4fms %15.4fms",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, initSearchCount / n, initSearchTime / n,
                searchTime / n, updateTime / n, timeOfEachMethod[methodID]);
        System.out.println("[Query Level Linear Search] Final Cache Size/Given Cache Size : " + cachedQueryPoints.size()
                + "/" + cacheSize);
        System.out.println(info);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_To_ObjectLinear_Cache(String cacheStrategy, int cacheSize,
            double updateThreshold,
            int k, boolean useBFS) {
        init();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        ArrayList<Point> cachedQueryPoints = new ArrayList<>();// initial a cached query points
        long start, end;

        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1. use cached point to get a initial kNN distance
            start = System.currentTimeMillis();
            PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            double maxKdist = Double.MAX_VALUE;
            HashSet<Point> visitedQueryPoints = new HashSet<>();
            HashSet<Point> visitedObjectPoints = new HashSet<>();
            for (Point cachedQueryPoint : cachedQueryPoints) {
                for (NN cacheNN : cachedQueryPoint.getNNs()) {
                    Point cachedObjectPoints = cacheNN.point;
                    if (visitedObjectPoints.contains(cachedObjectPoints)) {
                        continue;
                    }
                    double dist = queryPoint.distanceTo(cachedObjectPoints);
                    initSearchCount++;
                    visitedObjectPoints.add(cachedObjectPoints);
                    if (pq.size() < k) {
                        pq.add(new NN(cachedObjectPoints, dist));
                    } else {
                        if (pq.peek().dist2query > dist) {
                            pq.poll();
                            visitedQueryPoints.add(cachedQueryPoint);
                            pq.add(new NN(cachedObjectPoints, dist));
                        }
                    }
                }
            }
            if (pq.size() == k) {
                maxKdist = pq.peek().dist2query;
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);
            // 2. search exact kNN using VP-tree
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            long nodeAccessAfter = vp.nodeAccess;
            end = System.currentTimeMillis();
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query > updateThreshold) {
                            updateCount += 1;
                            cachedQueryPoints.remove(0);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            for (Point visitedQueryPoint : visitedQueryPoints) {
                                cachedQueryPoints.remove(visitedQueryPoint);
                                cachedQueryPoints.add(visitedQueryPoint);
                            }
                        } else {
                            updateCount += 1;
                            // remove the outdated cached queryPoint point
                            cachedQueryPoints.remove(0);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            for (Point visitedQueryPoint : visitedQueryPoints) {
                                visitedQueryPoint.addHitCount();
                            }
                        } else {
                            updateCount += 1;
                            // remove the queryPoint node with minimal hit count
                            double minExpense = Double.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                double e = pp.hitCount;
                                if (e < minExpense) {
                                    minExpense = e;
                                    removeP = pp;
                                }
                            }
                            cachedQueryPoints.remove(removeP);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    // for (NN nn : nns) {
                    // Point objectPoint = nn.point;
                    // objectPoint.addHitCount();
                    // }
                    queryPoint.ts = i;
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            for (Point visitedQueryPoint : visitedQueryPoints) {
                                visitedQueryPoint.addHitCount();
                            }
                        } else {
                            updateCount += 1;
                            double minExpense = Double.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                assert i - pp.ts > 0;
                                double e = pp.expense;
                                if (e < minExpense) {
                                    minExpense = e;
                                    removeP = pp;
                                }
                            }

                            // double minRkNN = Double.MAX_VALUE;
                            // Point removeP = cachedQueryPoints.get(0);
                            // for (Point pp : cachedQueryPoints) {
                            // int hitSum = 0;
                            // for (NN nn : pp.NNs) {
                            // hitSum += nn.point.hitCount;
                            // }
                            // if (hitSum < minRkNN) {
                            // minRkNN = hitSum;
                            // removeP = pp;
                            // }
                            // }
                            cachedQueryPoints.remove(removeP);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    cachedQueryPoints.add(queryPoint);
                    if (maxKdist / nns.peek().dist2query > updateThreshold) {
                        updateCount += 1;
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();
            updateTime += (end - start);
        }
        if (cacheStrategy != "GLO-DFS" || cacheStrategy != "GLO-BFS") {
            assert cachedQueryPoints.size() <= cacheSize;
        } else {
            assert cachedQueryPoints.size() == qData.length;
        }
        t2 = System.currentTimeMillis();

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | init search | init search-time | vp search time | cache update time | run time |\n%10d %10d    %10d %10d %15.4fms %15.4fms %15.4fms %15.4fms",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, initSearchCount / n, initSearchTime / n,
                searchTime / n, updateTime / n, timeOfEachMethod[methodID]);
        System.out.println(info);
        System.out.println("[Query Level Linear + Object Level Linear] Final Cache Size/Given Cache Size : "
                + cachedQueryPoints.size()
                + "/" + cacheSize);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_To_ObjectHNSW_Cache(String cacheStrategy, int cacheSize,
            double updateThreshold, int k, boolean useBFS) {
        init();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        ArrayList<Point> cachedQueryPoints = new ArrayList<>();
        long start, end;
        HNSW hnsw = new HNSW(2, k);

        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1. use cached point to get an initial kNN distance
            Point minPP = null;
            double maxDist = Double.MAX_VALUE;
            double maxKdist = Double.MAX_VALUE;
            start = System.currentTimeMillis();
            if (!cachedQueryPoints.isEmpty()) {
                // find the NN query point to the current queryPoint
                minPP = cachedQueryPoints.get(0);
                for (Point cachedQueryPoint : cachedQueryPoints) {
                    double dist = queryPoint.distanceTo(cachedQueryPoint);
                    initSearchCount++;
                    if (dist < maxDist) {
                        maxDist = dist;
                        minPP = cachedQueryPoint;
                    }
                }
                // find the kANN object points to the current queryPoint
                PriorityQueue<NN> pairs = hnsw.findKNN(queryPoint, k);
                if (pairs.size() == k) {
                    maxKdist = pairs.peek().dist2query;
                }
            }

            end = System.currentTimeMillis();
            initSearchTime += (end - start);
            // 2. search exact kNN using VP-tree
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            long nodeAccessAfter = vp.nodeAccess;
            // System.out.println(i + " " + maxKdist + "/" + nns.peek().dist2query);
            end = System.currentTimeMillis();
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":

                    if (cachedQueryPoints.size() < cacheSize) {
                        // update cached query points
                        cachedQueryPoints.add(queryPoint);
                        // update cached object points
                        for (NN nn : nns) {
                            hnsw.addPoint(nn.point, k);
                        }

                        // System.out.println();
                        // System.out.println("cachedQueryPoints with add: " + queryPoint);
                        // System.out.println("cachedQueryPoints: " + cachedQueryPoints);
                        // System.out.println("HNSW with add: " + nns);
                        // System.out.println("HNSW points: " + hnsw);
                        // hnsw.printConnections();

                    } else {
                        if (maxKdist / nns.peek().dist2query > updateThreshold) {
                            updateCount += 1;
                            // remove
                            Point removeP = cachedQueryPoints.get(0);
                            cachedQueryPoints.remove(removeP);
                            for (NN nn : removeP.NNs) {
                                hnsw.removePoint(nn.point, null);
                            }
                            // add
                            cachedQueryPoints.add(queryPoint);
                            for (NN nn : nns) {
                                hnsw.addPoint(nn.point, k);
                            }

                            // System.out.println();
                            // System.out.println("cachedQueryPoints with remove: " + removeP);
                            // System.out.println("HNSW with remove: " + removeP.NNs);
                            // System.out.println("cachedQueryPoints with add: " + queryPoint);
                            // System.out.println("HNSW with add: " + nns);
                            // System.out.println("cachedQueryPoints: " + cachedQueryPoints);
                            // System.out.println("HNSW points " + hnsw);
                            // hnsw.printConnections();
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        // update cached query points
                        cachedQueryPoints.add(queryPoint);
                        // update cached object points
                        for (NN nn : nns) {
                            hnsw.addPoint(nn.point, k);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        } else {
                            updateCount += 1;
                            // remove
                            Point removeP = cachedQueryPoints.get(0);
                            cachedQueryPoints.remove(removeP);
                            for (NN nn : removeP.NNs) {
                                hnsw.removePoint(nn.point, null);
                            }
                            // add
                            cachedQueryPoints.add(queryPoint);
                            for (NN nn : nns) {
                                hnsw.addPoint(nn.point, k);
                            }
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        // update cached query points
                        cachedQueryPoints.add(queryPoint);
                        // update cached object points
                        for (NN nn : nns) {
                            hnsw.addPoint(nn.point, k);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            minPP.addHitCount();
                        } else {
                            updateCount += 1;
                            // select the query point with minimal hit count
                            int minHitCount = Integer.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point p : cachedQueryPoints) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    removeP = p;
                                }
                            }
                            // remove
                            cachedQueryPoints.remove(removeP);
                            for (NN nn : removeP.NNs) {
                                hnsw.removePoint(nn.point, null);
                            }
                            // add
                            cachedQueryPoints.add(queryPoint);
                            for (NN nn : nns) {
                                hnsw.addPoint(nn.point, k);
                            }

                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        // update cached query points
                        cachedQueryPoints.add(queryPoint);
                        // update cached object points
                        for (NN nn : nns) {
                            hnsw.addPoint(nn.point, k);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            minPP.addHitCount();
                        } else {
                            updateCount += 1;
                            // select the query point with minimal benefit
                            double minBenefit = Double.MAX_VALUE;
                            Point removeP = cachedQueryPoints.get(0);
                            for (Point p : cachedQueryPoints) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    removeP = p;
                                }
                            }
                            // remove
                            cachedQueryPoints.remove(removeP);
                            for (NN nn : removeP.NNs) {
                                hnsw.removePoint(nn.point, null);
                            }
                            // add
                            cachedQueryPoints.add(queryPoint);
                            for (NN nn : nns) {
                                hnsw.addPoint(nn.point, k);
                            }
                        }
                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    cachedQueryPoints.add(queryPoint);
                    for (NN nn : nns) {
                        hnsw.addPoint(nn.point, k);
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();
            updateTime += (end - start);
        }
        hnsw.printGraph();
        t2 = System.currentTimeMillis();
        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | init search | init search-time | vp search time | cache update time | run time |\n%10d %10d    %10d %10d %15.4fms %15.4fms %15.4fms %15.4fms",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, initSearchCount / n, initSearchTime / n,
                searchTime / n, updateTime / n, timeOfEachMethod[methodID]);
        System.out.println(info);
        System.out.println("[Query Linear + Object HNSW Search] Final Cache Size/Given Cache Size : "
                + cachedQueryPoints.size() + "/" + cacheSize);
        System.out.println(" Graph update calcCount: " + hnsw.calcCount / n);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectLinear_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        init();
        cacheSize = cacheSize * k;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        ArrayList<Point> cachedObjectPoints = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long start, end;
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1. use cached point to get an initial kNN distance
            start = System.currentTimeMillis();
            PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            double maxKdist = Double.MAX_VALUE;
            for (Point p : cachedObjectPoints) {
                double dist = p.distanceTo(queryPoint);
                if (pq.size() < k) {
                    pq.add(new NN(p, dist));
                } else {
                    if (dist < pq.peek().dist2query) {
                        pq.poll();
                        pq.add(new NN(p, dist));
                    }
                }
            }
            if (pq.size() == k) {
                maxKdist = pq.peek().dist2query;
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);
            // 2. search exact kNN using VP-tree
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            res.add(nns);
            for (NN nn : nns) {
                nn.point.ts = i;
                nn.point.addHitCount();
                nn.point.expense = nodeAccessAfter - nodeAccessBefore;
            }
            end = System.currentTimeMillis();
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            if (maxKdist / nns.peek().dist2query > updateThreshold) {
                updateCount += 1;
            } else {
                if (cachedObjectPoints.size() >= cacheSize) {
                    continue;
                }
            }
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoints.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoints.size() < cacheSize) {
                            cachedObjectPoints.add(nnPoint);
                        } else {
                            cachedObjectPoints.remove(0);
                            cachedObjectPoints.add(nnPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoints.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoints.size() < cacheSize) {
                            cachedObjectPoints.add(nnPoint);
                        } else {
                            // remove
                            int farthestT = Integer.MAX_VALUE;
                            Point removeP = cachedObjectPoints.get(0);
                            for (Point p : cachedObjectPoints) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoints.remove(removeP);
                            cachedObjectPoints.add(nnPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoints.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoints.size() < cacheSize) {
                            cachedObjectPoints.add(nnPoint);
                        } else {
                            // remove
                            int minHitCount = Integer.MAX_VALUE;
                            Point removeP = cachedObjectPoints.get(0);
                            for (Point p : cachedObjectPoints) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoints.remove(removeP);
                            cachedObjectPoints.add(nnPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoints.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoints.size() < cacheSize) {
                            cachedObjectPoints.add(nnPoint);
                        } else {
                            double minBenefit = Double.MAX_VALUE;
                            Point removeP = cachedObjectPoints.get(0);
                            for (Point p : cachedObjectPoints) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoints.remove(removeP);
                            cachedObjectPoints.add(nnPoint);
                        }
                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (!cachedObjectPoints.contains(nnPoint)) {
                            cachedObjectPoints.add(nnPoint);
                        }
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();
            updateTime += (end - start);
        }
        t2 = System.currentTimeMillis();

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time |\n%10d %10d    %10d %15.4fms %15.4fms %15.4fms %15.4fms",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, initSearchTime / n,
                searchTime / n, updateTime / n, timeOfEachMethod[methodID]);
        System.out.println(info);
        System.out
                .println("[Object Level Linear] Final Cache Size/Given Cache Size : " + cachedObjectPoints.size() + "/"
                        + cacheSize);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectHNSW_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        init();
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long start, end;
        HNSW hnsw = new HNSW(2, 20);
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1．use cached point to get an initial kNN distance
            start = System.currentTimeMillis();
            double maxKdist = Double.MAX_VALUE;
            if (i != 0) {
                PriorityQueue<NN> pairs = hnsw.findKNN(queryPoint, k);
                if (pairs != null && pairs.size() == k) {
                    maxKdist = pairs.peek().dist2query;
                }
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);
            // 2. search exact kNN using VP-tree
            long nodeAccessBefore = vp.nodeAccess;
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            long nodeAccessAfter = vp.nodeAccess;
            for (NN nn : nns) {
                nn.point.ts = i;
                nn.point.addHitCount();
                nn.point.expense = nodeAccessAfter - nodeAccessBefore;
            }
            // System.out.println("\n" + i + " " + maxKdist + "/" + nns.peek().dist2query);
            end = System.currentTimeMillis();
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            if (maxKdist / nns.peek().dist2query < updateThreshold)
                continue;
            updateCount += 1;
            // System.out.println("HNSW points " + hnsw);
            // hnsw.printConnections();
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
                        // System.out.println("HNSW with add: " + nn);
                        if (hnsw.size() > cacheSize) {
                            Point removeP = hnsw.points.get(0);
                            hnsw.removePoint(removeP, nnPoint);
                            // System.out.println("HNSW with remove: " + removeP);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
                        if (hnsw.size() > cacheSize) {
                            int farthestT = Integer.MAX_VALUE;
                            Point removeP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    removeP = p;
                                }
                            }
                            hnsw.removePoint(removeP, nnPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
                        if (hnsw.size() > cacheSize) {
                            int minHitCount = Integer.MAX_VALUE;
                            Point removeP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    removeP = p;
                                }
                            }
                            hnsw.removePoint(removeP, nnPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
                        if (hnsw.size() > cacheSize) {
                            double minBenefit = Double.MAX_VALUE;
                            Point removeP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    removeP = p;
                                }
                            }
                            hnsw.removePoint(removeP, nnPoint);
                        }
                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    for (NN nn : nns) {
                        hnsw.addPoint(nn.point, k);
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();

            updateTime += (end - start);
        }

        t2 = System.currentTimeMillis();
        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;
        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time |\n%10d %10d    %10d %15.4fms %15.4fms %15.4fms %15.4fms",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, initSearchTime / n,
                searchTime / n, updateTime / n, timeOfEachMethod[methodID]);
        System.out.println(info);
        System.out.println("[Object Level hnsw] Final Cache Size/Given Cache Size : " + hnsw.size() + "/" + cacheSize);
        System.out.println(" Graph calcCount: " + hnsw.calcCount / n);
        hnsw.printGraph();
        return res;
    }

    public ArrayList<PriorityQueue<NN>> bestCache(String cacheStrategy, double factor, double updateThreshold, int k,
            boolean useBFS) {
        // 1. Initilize best caches for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (Point queryPoint : qData) {
            cache.add(vp.searchkNNDFS(queryPoint, k, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        init();
        // 2. Query
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            Point queryPoint = qData[i];
            double maxKdist = factor * cache.get(i).peek().dist2query;
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBFS(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query > updateThreshold) {
                updateCount += 1;
            }
        }
        t2 = System.currentTimeMillis();
        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;
        timeOfEachMethod[methodID] = (t2 - t1) / n;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nnode accesses | calc count | unhit count | search time |\n%10d %10d %10d \t%8.4fms ",
                cacheStrategy, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], updateCount, timeOfEachMethod[methodID]);
        System.out.println(info);
        return res;
    }

}