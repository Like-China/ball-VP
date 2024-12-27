package evaluation;

import java.util.*;
import VPTree.*;
import graphcache.HNSW;
import utils.NN;
import utils.Point;

public class VPAlg {
    /// queryPoint, database vectors
    public Point[] qData;
    public Point[] dbData;
    // index construction time
    public long cTime = 0;
    // the number of node accesses (Deep-First/Best-first + Hier/recursion + Cache)
    public HashMap<String, Integer> myMap = Settings.myMap;

    public long[] nodeAccessOfEachMethod = new long[myMap.size()];
    public long[] calcCountOfEachMethod = new long[myMap.size()];
    public double[] timeOfEachMethod = new double[myMap.size()];

    public String info = null;
    public int sampleNB;

    public VPTreeBySample vp = null;
    long t1, t2;

    public VPAlg(Point[] qData, Point[] dbData, int sampleNB, int bucketSize) {
        this.qData = qData;
        this.dbData = dbData;
        this.sampleNB = sampleNB;
        // tree construction
        t1 = System.currentTimeMillis();
        vp = new VPTreeBySample(this.dbData, sampleNB, bucketSize);
        vp.getLayerNB(vp.root);
        vp.firstOrderVisit(vp.root);
        t2 = System.currentTimeMillis();
        System.out.println(
                "VP tree construction in  " + (t2 - t1) + " ms with " + vp.nodeNB + " nodes " + vp.layerNB + " layers");
        cTime = t2 - t1;
    }

    public ArrayList<PriorityQueue<NN>> DFS_BFS(String cacheStrategy, int k, boolean useInitkNN,
            double updateThreshold, boolean useBFS) {

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;
        vp.init();
        long n = qData.length;
        int hitCount = 0;
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
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                hitCount += 1;
            }
        }
        t2 = System.currentTimeMillis();
        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {

        vp.init();
        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a cached queryPoint points
        ArrayList<Point> cachedQueryPoints = new ArrayList<>();

        double updateTime = 0;
        double initSearchTime = 0;
        double SearchTime = 0;
        long start, end;
        long hitCount = 0;

        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];

            start = System.currentTimeMillis();
            // use cached point to get a initial kNN distance
            double minDist = Double.MAX_VALUE;
            double maxKdist = Double.MAX_VALUE;
            Point minPP = null;
            for (Point pp : cachedQueryPoints) {
                double dist = pp.distanceTo(queryPoint);
                if (dist < minDist) {
                    minDist = dist;
                    minPP = pp;
                }
            }
            if (minPP != null) {
                maxKdist = 0;
                for (NN nn : minPP.NNs) {
                    double dist = nn.point.distanceTo(queryPoint);
                    if (dist >= maxKdist) {
                        maxKdist = dist;
                    }
                }
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);

            start = System.currentTimeMillis();
            // search exact kNN using VP-tree
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            end = System.currentTimeMillis();
            SearchTime += (end - start);

            // update res
            res.add(nns);

            // update cache
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            cachedQueryPoints.remove(0);
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        } else {
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
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        } else {
                            // remove the queryPoint node with minimal hit count
                            double minExpense = Double.MAX_VALUE;
                            Point minP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                double e = pp.hitCount;
                                if (e < minExpense) {
                                    minExpense = e;
                                    minP = pp;
                                }
                            }
                            cachedQueryPoints.remove(minP);
                            // add the current queryPoint point
                            cachedQueryPoints.add(queryPoint);
                        }
                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    queryPoint.ts = i;
                    if (cachedQueryPoints.size() < cacheSize) {
                        cachedQueryPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        } else {
                            double minExpense = Double.MAX_VALUE;
                            Point minP = cachedQueryPoints.get(0);
                            for (Point pp : cachedQueryPoints) {
                                assert i - pp.ts > 0;
                                double e = pp.expense * pp.hitCount / Math.pow(i - pp.ts, 3);
                                if (e < minExpense) {
                                    minExpense = e;
                                    minP = pp;
                                }
                            }
                            cachedQueryPoints.remove(minP);
                            cachedQueryPoints.add(queryPoint);
                        }

                    }
                    break;
                case "Global":
                    cachedQueryPoints.add(queryPoint);
                    if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                        hitCount += 1;
                    }
                    break;
                default:
                    System.out.println("The cache strategy is not specficed!!");
                    return null;
            }
            end = System.currentTimeMillis();
            updateTime += (end - start);
        }
        // assert cacheStrategy != "Global" && cachedPoints.size() <= cacheSize :
        // cachedPoints.size() + "/" + cacheSize;
        t2 = System.currentTimeMillis();

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        System.out
                .println("[Query Level Linear Search] Final Cache Size/Given Cache Size : " + cachedQueryPoints.size()
                        + "/" + cacheSize);
        System.out.println("Update-time: " + (updateTime / n) + "  InitSearch-time: " + (initSearchTime / n)
                + "   Search-time: " + (SearchTime / n));
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_To_ObjectLinear_Cache(String cacheStrategy, int cacheSize,
            double updateThreshold,
            int k, boolean useBFS) {
        vp.init();
        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a cached queryPoint points
        ArrayList<Point> cachedPoints = new ArrayList<>();

        double updateTime = 0;
        double initSearchTime = 0;
        double SearchTime = 0;
        long start, end;
        long hitCount = 0;

        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];

            // use cached point to get a initial kNN distance
            start = System.currentTimeMillis();
            PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            Point minPP = null;
            double maxKdist = Double.MAX_VALUE;
            HashSet<Point> visitedObjectPoints = new HashSet<>();
            for (Point cachedQueryPoint : cachedPoints) {
                for (NN cacheNN : cachedQueryPoint.getNNs()) {
                    Point cachedObjectPoint = cacheNN.point;
                    if (visitedObjectPoints.contains(cachedObjectPoint)) {
                        continue;
                    }
                    double dist = queryPoint.distanceTo(cachedObjectPoint);
                    if (pq.size() < k) {
                        minPP = cachedQueryPoint;
                        visitedObjectPoints.add(cachedObjectPoint);
                        pq.add(new NN(cachedObjectPoint, dist));
                    } else {
                        if (pq.peek().dist2query > dist) {
                            pq.poll();
                            minPP = cachedQueryPoint;
                            visitedObjectPoints.add(cachedObjectPoint);
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

            // search exact kNN using VP-tree
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            end = System.currentTimeMillis();
            SearchTime += (end - start);

            // update res
            res.add(nns);

            // update cache
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            switch (cacheStrategy) {
                case "FIFO-DFS":
                case "FIFO-BFS":
                    if (cachedPoints.size() < cacheSize) {
                        cachedPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            cachedPoints.remove(0);
                            cachedPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedPoints.size() < cacheSize) {
                        cachedPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedPoints.remove(minPP);
                            cachedPoints.add(minPP);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedPoints.remove(minPP);
                            cachedPoints.add(minPP);
                        } else {
                            // remove the outdated cached queryPoint point
                            cachedPoints.remove(0);
                            cachedPoints.add(queryPoint);
                        }
                    }
                    break;
                case "LFU-DFS":
                case "LFU-BFS":
                    if (cachedPoints.size() < cacheSize) {
                        cachedPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        } else {
                            // remove the queryPoint node with minimal hit count
                            double minExpense = Double.MAX_VALUE;
                            Point minP = cachedPoints.get(0);
                            for (Point pp : cachedPoints) {
                                double e = pp.hitCount;
                                if (e < minExpense) {
                                    minExpense = e;
                                    minP = pp;
                                }
                            }
                            cachedPoints.remove(minP);
                            // add the current queryPoint point
                            cachedPoints.add(queryPoint);
                        }
                    }
                    break;
                case "BDC1":
                    queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
                    queryPoint.ts = i;
                    if (cachedPoints.size() < cacheSize) {
                        cachedPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            minPP.addHitCount();
                        } else {
                            double minExpense = Double.MAX_VALUE;
                            Point minP = cachedPoints.get(0);
                            for (Point pp : cachedPoints) {
                                assert i - pp.ts > 0;
                                double e = pp.expense * pp.hitCount / Math.pow(i - pp.ts, 2);
                                if (e < minExpense) {
                                    minExpense = e;
                                    minP = pp;
                                }
                            }
                            cachedPoints.remove(minP);
                            cachedPoints.add(queryPoint);
                        }

                    }
                    break;
                case "BDC-DFS":
                case "BDC-BFS":
                    for (NN nn : nns) {
                        Point objectPoint = nn.point;
                        objectPoint.addrKNNs(queryPoint);
                    }
                    queryPoint.ts = i;
                    if (cachedPoints.size() < cacheSize) {
                        cachedPoints.add(queryPoint);
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            double minRkNN = Double.MAX_VALUE;
                            Point minP = cachedPoints.get(0);
                            for (Point pp : cachedPoints) {
                                int hitSum = 0;
                                for (NN nn : pp.NNs) {
                                    hitSum += nn.point.rkNNSize();
                                }
                                if (hitSum < minRkNN) {
                                    minRkNN = hitSum;
                                    minP = pp;
                                }
                            }
                            cachedPoints.remove(minP);
                            cachedPoints.add(queryPoint);
                        }
                    }
                    break;
                case "Global":
                    // global cache by default
                    cachedPoints.add(queryPoint);
                    if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                        hitCount += 1;
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

        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        System.out
                .println("[Query2Object Level Linear] Final Cache Size/Given Cache Size : "
                        + cachedPoints.size() + "/" + cacheSize);
        System.out.println("Update-time: " + (updateTime / n) + "  InitSearch-time: " + (initSearchTime / n)
                + "   Search-time: " + (SearchTime / n));
        return res;
    }

    public ArrayList<PriorityQueue<NN>> queryLinear_To_ObjectHNSW_Cache(String cacheStrategy, int cacheSize,
            double updateThreshold, int k,
            boolean useBFS) {
        vp.init();
        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        // initial a cached queryPoint points
        ArrayList<Point> cachedQueryPoints = new ArrayList<>();
        long hitCount = 0;

        double updateTime = 0;
        double initSearchTime = 0;
        double SearchTime = 0;
        long start, end;
        // initial a LRUCache
        HNSW hnsw = new HNSW(4, 16, cacheSize * k);
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];

            // use cached point to get an initial kNN distance
            Point minPP = null;
            double maxDist = Double.MAX_VALUE;
            double maxKdist = Double.MAX_VALUE;

            start = System.currentTimeMillis();
            if (!cachedQueryPoints.isEmpty()) {
                minPP = cachedQueryPoints.get(0);
                for (Point cachedQueryPoint : cachedQueryPoints) {
                    double dist = queryPoint.distanceTo(cachedQueryPoint);
                    if (dist < maxDist) {
                        maxDist = dist;
                        minPP = cachedQueryPoint;
                    }
                }
                PriorityQueue<NN> pairs = hnsw.findKNN(queryPoint, k);
                if (pairs.size() == k) {
                    maxKdist = pairs.peek().dist2query;
                }
            }
            end = System.currentTimeMillis();
            initSearchTime += (end - start);

            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            // update res
            res.add(nns);
            end = System.currentTimeMillis();
            // update cache
            SearchTime += (end - start);
            start = System.currentTimeMillis();
            queryPoint.setNNs(nns, nodeAccessAfter - nodeAccessBefore);
            // If it is hit, then we do not update the cached object points
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

                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            // remove
                            Point deleteP = cachedQueryPoints.get(0);
                            cachedQueryPoints.remove(deleteP);
                            for (NN nn : deleteP.NNs) {
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
                case "LRU-DFS":
                case "LRU-BFS":
                    if (cachedQueryPoints.size() < cacheSize) {
                        // update cached query points
                        cachedQueryPoints.add(queryPoint);
                        // update cached object points
                        for (NN nn : nns) {
                            hnsw.addPoint(nn.point, k);
                        }

                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                            cachedQueryPoints.remove(minPP);
                            cachedQueryPoints.add(minPP);
                        } else {
                            // remove
                            Point deleteP = cachedQueryPoints.get(0);
                            cachedQueryPoints.remove(deleteP);
                            for (NN nn : deleteP.NNs) {
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

                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            // remove
                            int minHitCount = Integer.MAX_VALUE;
                            Point deleteP = cachedQueryPoints.get(0);
                            for (Point p : cachedQueryPoints) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    deleteP = p;
                                }
                            }
                            cachedQueryPoints.remove(deleteP);
                            for (NN nn : deleteP.NNs) {
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

                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        }
                    } else {
                        if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                            hitCount += 1;
                        } else {
                            // remove
                            double minBenefit = Double.MAX_VALUE;
                            Point deleteP = cachedQueryPoints.get(0);
                            for (Point p : cachedQueryPoints) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    deleteP = p;
                                }
                            }
                            cachedQueryPoints.remove(deleteP);
                            for (NN nn : deleteP.NNs) {
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
                case "Global":
                    cachedQueryPoints.add(queryPoint);
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
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

        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        System.out
                .println("[Object Level hnsw] Final Cache Size/Given Cache Size : " + hnsw.size() + "/"
                        + cacheSize);
        System.out.println("Update-time: " + (updateTime / n) + "  InitSearch-time: " + (initSearchTime / n)
                + "   Search-time: " + (SearchTime / n));
        System.out.println(" Graph update calcCount: " + hnsw.calcCount / n);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectLinear_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        vp.init();
        cacheSize = cacheSize * k;
        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        ArrayList<Point> cachedObjectPoint = new ArrayList<>();

        t1 = System.currentTimeMillis();

        double updateTime = 0;
        double initSearchTime = 0;
        double SearchTime = 0;
        long start, end;
        long hitCount = 0;

        HashSet<Point> cc = new HashSet<>();

        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // use cached point to get an initial kNN distance
            start = System.currentTimeMillis();
            PriorityQueue<NN> pq = new PriorityQueue<>((a, b) -> Double.compare(b.dist2query, a.dist2query));
            // use cached point to get an initial kNN distance
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

            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            long nodeAccessBefore = vp.nodeAccess;
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            long nodeAccessAfter = vp.nodeAccess;
            end = System.currentTimeMillis();
            SearchTime += (end - start);

            // update res
            start = System.currentTimeMillis();
            res.add(nns);
            for (NN nn : nns) {
                nn.point.ts = i;
                nn.point.addHitCount();
                nn.point.expense = nodeAccessAfter - nodeAccessBefore;
            }
            // If it is hit, then we do not update the cached object points
            if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                hitCount += 1;
                // continue;
            }
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
                            cc.add(nnPoint);
                        } else {
                            // remove
                            cachedObjectPoint.remove(0);
                            cachedObjectPoint.add(nnPoint);
                            cc.add(nnPoint);
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
                            Point deleteP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    deleteP = p;
                                }
                            }
                            cachedObjectPoint.remove(deleteP);
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
                            Point deleteP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    deleteP = p;
                                }
                            }
                            cachedObjectPoint.remove(deleteP);
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
                            // remove
                            double minBenefit = Double.MAX_VALUE;
                            Point deleteP = cachedObjectPoint.get(0);
                            for (Point p : cachedObjectPoint) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    deleteP = p;
                                }
                            }
                            cachedObjectPoint.remove(deleteP);
                            cachedObjectPoint.add(nnPoint);
                        }
                    }
                    break;
                case "Global":
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

        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        System.out.println("[Object Level Linear] Final Cache Size/Given Cache Size : " + cachedObjectPoint.size() + "/"
                + cacheSize);
        System.out.println("Update-time: " + (updateTime / n) + "  InitSearch-time: " + (initSearchTime / n)
                + "   Search-time: " + (SearchTime / n));
        // System.out.println("Cached Object Size: " + cc.size());
        return res;
    }

    public ArrayList<PriorityQueue<NN>> ObjectHNSW_Cache(String cacheStrategy, int cacheSize, double updateThreshold,
            int k, boolean useBFS) {
        vp.init();

        cacheSize = cacheSize * k;
        long n = qData.length;
        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long hitCount = 0;

        double updateTime = 0;
        double initSearchTime = 0;
        double SearchTime = 0;
        long start, end;

        // initial a LRUCache
        int M = 20;
        HNSW hnsw = new HNSW(4, M, cacheSize);
        HashSet<Point> cc = new HashSet<>();
        for (int i = 0; i < n; i++) {
            Point queryPoint = qData[i];
            // use cached point to get an initial kNN distance
            double maxKdist = Double.MAX_VALUE;
            // initial
            if (i == 0) {
                maxKdist = Double.MAX_VALUE;
            } else {
                long startSearch = System.currentTimeMillis();
                PriorityQueue<NN> pairs = hnsw.findKNN(queryPoint, k);
                long endSearch = System.currentTimeMillis();
                initSearchTime += (endSearch - startSearch);
                if (pairs.size() == k) {
                    maxKdist = pairs.peek().dist2query;
                }
            }

            long nodeAccessBefore = vp.nodeAccess;
            start = System.currentTimeMillis();
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            // update res
            res.add(nns);
            end = System.currentTimeMillis();
            long nodeAccessAfter = vp.nodeAccess;
            SearchTime += (end - start);

            // update cache
            start = System.currentTimeMillis();
            if (i == 0) {
                // ArrayList<Point> initPoints = new ArrayList<>();
                for (NN nn : nns) {
                    // initPoints.add(nn.point);
                    // cc.add(nn.point);
                    hnsw.addPoint(nn.point, k);
                }
                continue;
            }
            for (NN nn : nns) {
                nn.point.ts = i;
                nn.point.addHitCount();
                nn.point.expense = nodeAccessAfter - nodeAccessBefore;
            }
            // If it is hit, then we do not update the cached object points
            if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                hitCount += 1;
                // continue;
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
                            // remove
                            Point deleteP = hnsw.points.get(0);
                            hnsw.removePoint(deleteP, nnPoint);
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
                            // remove
                            int farthestT = Integer.MAX_VALUE;
                            Point deleteP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.ts < farthestT) {
                                    farthestT = p.ts;
                                    deleteP = p;
                                }
                            }
                            hnsw.removePoint(deleteP, nnPoint);
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
                            // remove
                            int minHitCount = Integer.MAX_VALUE;
                            Point deleteP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                if (p.hitCount < minHitCount) {
                                    minHitCount = p.hitCount;
                                    deleteP = p;
                                }
                            }
                            hnsw.removePoint(deleteP, nnPoint);
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
                            // remove
                            double minBenefit = Double.MAX_VALUE;
                            Point deleteP = hnsw.points.get(0);
                            for (Point p : hnsw.points) {
                                double e = p.expense * p.hitCount / Math.pow(i - p.ts, 2);
                                if (e < minBenefit) {
                                    minBenefit = e;
                                    deleteP = p;
                                }
                            }
                            hnsw.removePoint(deleteP, nnPoint);
                            hnsw.addPoint(nnPoint, k);
                        }
                    }
                    break;
                case "Global":
                    for (NN nn : nns) {
                        Point nnPoint = nn.point;
                        hnsw.addPoint(nnPoint, k);
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
        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        System.out
                .println("[Object Level hnsw] Final Cache Size/Given Cache Size : " + hnsw.size() + "/"
                        + cacheSize);
        System.out.println("Update-time: " + (updateTime / n) + "  InitSearch-time: " + (initSearchTime / n)
                + "   Search-time: " + (SearchTime / n));
        System.out.println("Effective count: " + cc.size());
        System.out.println(" Graph calcCount: " + hnsw.calcCount / n);
        return res;
    }

    public ArrayList<PriorityQueue<NN>> bestCache(String cacheStrategy, double factor, double updateThreshold, int k,
            boolean useBFS) {
        vp.init();
        int n = qData.length;
        // Initilize best caches for all queries
        ArrayList<PriorityQueue<NN>> cache = new ArrayList<>();
        t1 = System.currentTimeMillis();
        int cacheK = (int) (k * factor);
        for (Point queryPoint : qData) {
            cache.add(vp.searchkNNDFS(queryPoint, cacheK, Double.MAX_VALUE));
        }
        t2 = System.currentTimeMillis();
        vp.init();

        ArrayList<PriorityQueue<NN>> res = new ArrayList<>();
        t1 = System.currentTimeMillis();
        long hitCount = 0;
        for (int i = 0; i < qData.length; i++) {
            Point queryPoint = qData[i];
            double maxKdist = cache.get(i).peek().dist2query;
            PriorityQueue<NN> nns = new PriorityQueue<>();
            if (useBFS) {
                nns = vp.searchkNNBFSRecu(queryPoint, k, maxKdist);
            } else {
                nns = vp.searchkNNDFS(queryPoint, k, maxKdist);
            }
            res.add(nns);
            if (maxKdist / nns.peek().dist2query <= updateThreshold) {
                hitCount += 1;
            }
        }
        t2 = System.currentTimeMillis();

        int methodID = myMap.getOrDefault(cacheStrategy, -1);
        assert methodID != -1;

        timeOfEachMethod[methodID] = t2 - t1;
        nodeAccessOfEachMethod[methodID] = vp.nodeAccess / n;
        calcCountOfEachMethod[methodID] = vp.calcCount / n;
        info = String.format(
                "\n***        %s \nconstruct time / mean search time / mean node accesses / mean calc count/ hit count:\n%5dms \t\t%5.4fms \t%5d \t\t%5d \t\t%5d",
                cacheStrategy, cTime, timeOfEachMethod[methodID] / n, nodeAccessOfEachMethod[methodID],
                calcCountOfEachMethod[methodID], hitCount);
        System.out.println(info);
        return res;
    }

}