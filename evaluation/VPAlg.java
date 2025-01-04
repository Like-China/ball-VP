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
                    Point cachedObjectPoint = cacheNN.point;
                    if (visitedObjectPoints.contains(cachedObjectPoint)) {
                        continue;
                    }
                    double dist = queryPoint.distanceTo(cachedObjectPoint);
                    initSearchCount++;
                    visitedObjectPoints.add(cachedObjectPoint);
                    if (pq.size() < k) {
                        pq.add(new NN(cachedObjectPoint, dist));
                    } else {
                        if (pq.peek().dist2query > dist) {
                            pq.poll();
                            visitedQueryPoints.add(cachedQueryPoint);
                            pq.add(new NN(cachedObjectPoint, dist));
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
        HNSW hnsw = new HNSW(2, k, cacheSize * k);

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

                        System.out.println();
                        // System.out.println("cachedQueryPoints with add: " + queryPoint);
                        // System.out.println("cachedQueryPoints: " + cachedQueryPoints);
                        System.out.println("HNSW with add: " + nns);
                        System.out.println("HNSW points: " + hnsw);
                        hnsw.printConnections();

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

                            System.out.println();
                            // System.out.println("cachedQueryPoints with remove: " + removeP);
                            System.out.println("HNSW with remove: " + removeP.NNs);

                            // System.out.println("cachedQueryPoints with add: " + queryPoint);
                            System.out.println("HNSW with add: " + nns);

                            // System.out.println("cachedQueryPoints: " + cachedQueryPoints);
                            System.out.println("HNSW points " + hnsw);
                            hnsw.printConnections();
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
        System.out.println("[Query Linear + Object HNSW Search] Final Cache Size/Given Cache Size : "
                + cachedQueryPoints.size() + "/" + cacheSize);
        System.out.println("Init Search-time: " + (initSearchTime / n) + " VPTree Search-time: " + (searchTime / n)
                + " Cache Update-time: " + (updateTime / n));
        System.out.println(" Graph update calcCount: " + hnsw.calcCount / n);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectLinear_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        init();
        cacheSize = cacheSize * k;

        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        ArrayList<Point> cachedObjectPoint = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long start, end;
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1. use cached point to get an initial kNN distance
            start = System.currentTimeMillis();
            PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            double maxKdist = Double.MAX_VALUE;
            for (Point p : cachedObjectPoint) {
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
            // update res
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
            // Otherwise, update cache
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoint.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoint.size() < cacheSize) {
                            cachedObjectPoint.add(nnPoint);
                        } else {
                            cachedObjectPoint.remove(0);
                            cachedObjectPoint.add(nnPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoint.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoint.size() < cacheSize) {
                            cachedObjectPoint.add(nnPoint);
                        } else {
                            // remove
                            int farthestT = Integer.MAX_VALUE;
                            Point removeP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoint.remove(removeP);
                            cachedObjectPoint.add(nnPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoint.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoint.size() < cacheSize) {
                            cachedObjectPoint.add(nnPoint);
                        } else {
                            // remove
                            int minHitCount = Integer.MAX_VALUE;
                            Point removeP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoint.remove(removeP);
                            cachedObjectPoint.add(nnPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (cachedObjectPoint.contains(nnPoint)) {
                            continue;
                        }
                        if (cachedObjectPoint.size() < cacheSize) {
                            cachedObjectPoint.add(nnPoint);
                        } else {
                            double minBenefit = Double.MAX_VALUE;
                            Point removeP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    removeP = p;
                                }
                            }
                            cachedObjectPoint.remove(removeP);
                            cachedObjectPoint.add(nnPoint);
                        }
                    }
                    break;
                case "GLO-DFS":
                case "GLO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (!cachedObjectPoint.contains(nnPoint)) {
                            cachedObjectPoint.add(nnPoint);
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
        System.out.println("[Object Level Linear] Final Cache Size/Given Cache Size : " + cachedObjectPoint.size() + "/"
                + cacheSize);
        System.out.println("Init Search-time: " + (initSearchTime / n) + " VPTree Search-time: " + (searchTime / n)
                + " Cache Update-time: " + (updateTime / n));
        System.out.println("Cached Object Size: " + cachedObjectPoint.size());
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectHNSW_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        init();
        cacheSize = cacheSize * k;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long start, end;
        HNSW hnsw = new HNSW(4, 20, cacheSize);
        HashSet<Point> cc = new HashSet<>();
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // 1．use cached point to get an initial kNN distance
            double maxKdist = Double.MAX_VALUE;
            if (i != 0) {
                long startSearch = System.currentTimeMillis();
                PriorityQueue<NN> pairs = hnsw.findKNN(queryPoint, k);
                long endSearch = System.currentTimeMillis();
                initSearchTime += (endSearch - startSearch);
                if (pairs.size() == k) {
                    maxKdist = pairs.peek().dist2query;
                }
            }
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
            end = System.currentTimeMillis();
            long nodeAccessAfter = vp.nodeAccess;
            searchTime += (end - start);
            // 3. update cache
            start = System.currentTimeMillis();
            for (NN nn : nns) {
                nn.point.ts = i;
                nn.point.addHitCount();
                nn.point.expense = nodeAccessAfter - nodeAccessBefore;
            }
            if (i == 0) {
                for (NN nn : nns) {
                    hnsw.addPoint(nn.point, k);
                }
                continue;
            }
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (hnsw.size() < cacheSize) {
                            hnsw.addPoint(nnPoint, k);
                            cc.add(nn.point);
                        } else {
                            Point removeP = hnsw.points.get(0);
                            hnsw.removePoint(removeP, nnPoint);
                            hnsw.addPoint(nnPoint, k);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (hnsw.size() < cacheSize) {
                            hnsw.addPoint(nnPoint, k);
                            cc.add(nn.point);
                        } else {
                            int farthestT = Integer.MAX_VALUE;
                            Point removeP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    removeP = p;
                                }
                            }
                            hnsw.removePoint(removeP, nnPoint);
                            hnsw.addPoint(nn.point, k);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (hnsw.size() < cacheSize) {
                            hnsw.addPoint(nnPoint, k);
                            cc.add(nn.point);
                        } else {
                            int minHitCount = Integer.MAX_VALUE;
                            Point removeP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    removeP = p;
                                }
                            }
                            hnsw.removePoint(removeP, nnPoint);
                            hnsw.addPoint(nn.point, k);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        if (hnsw.size() < cacheSize) {
                            hnsw.addPoint(nnPoint, k);
                            cc.add(nnPoint);
                        } else {
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
                            hnsw.addPoint(nnPoint, k);
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
        hnsw.printGraph();
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
        System.out.println("Init Search-time: " + (initSearchTime / n) + " VPTree Search-time: " + (searchTime / n)
                + " Cache Update-time: " + (updateTime / n));
        System.out.println("Effective count: " + cc.size());
        System.out.println(" Graph calcCount: " + hnsw.calcCount / n);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> bestCache(String cacheStrategy, double factor, double updateThreshold, int k,
            boolean useBFS) {
        // Initilize best caches for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        int cacheK = (int) (k * factor);
        for (Point queryPoint : qData) {
            cache.add(vp.searchkNNDFS(queryPoint, cacheK, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        init();

        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (int i = 0; i < qData.length; i++) {
            Point queryPoint = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
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