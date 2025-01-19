1. 校验best-first
2. 设计query-aware caching以提升NN查找效率，校验提升能力
3. enable without sqrt root (不满足三角不等式？) (Yes)
best-of-the-best 评估， full the initial kNN, fix the dataset and query
caching index to initial approxmate kNN, then refine kNN using fast vp-tree index
how to dynamically maintain the caching index

alleviate the degrade trending as the dimension increases

1. object-level
2. why sift differs deep
3. test LRU/FRU
4. how to model frequently visited data?
5. explore the data distributions od datasets

1. global cache, more cache size
2. kNN Graph
3. increase the dimension
4. How to get such query-aware data?
5. time-complexity analysis
6. material cache
7. why has two same NN in the result -- When constructing recurse VP-tree, certain vp points are not successfully deleted from the list.
(Resolved in 2024/10/17)
Set cacheSize enough large, then local cache is global cache

10/15 idea
1. We are the first from approximation to the exact solution
2. We provide a new solution (rather than high-dimension then approximate, we use low dimension then fet the exact solution)
3. use PCA to get low-dimension vectors
4. to increase the difference gap between local cache and global cache
5. for our two-layer solution, to decrease the update time as much as possible
Increase the size of queries

10/22
1. focus on the technical improvement in the cahce update time
2. HNSW cache update
3. How to set the update frequence, not very frequently updated scenrio.--
4. cache size influence is expected to have signficant degrades--
5. ask for data --
6. update kGraph --
7. memory cost analysis
8. let the initial NN by Linear Search not be so good
9. re-design the cache update conditions



Data: sift      qSize: 20000    dbSize: 200000  k: 10   dim: 10         sample: 100     Bucket Size: 20
VP tree construction in  7641 ms with 200000 nodes 17 layers

****    DFS
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7641ms                 1.6664ms        13798           13798               0

****    DFS
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7641ms                 1.2505ms         9241            9241               0

****    BFS
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7641ms                 1.2211ms         9827            9827               0

****    BFS
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7641ms                 1.0489ms         7882            7882               0

****    BFS--Best Caching
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.3348ms         2470            2470           20000
---------------------------------------------------------------------------
****    BFS--BDC
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.8578ms         6176            6176           454
[Query Level] Final Cache Size/Given Cache Size : 200/200
Update-time: 0.0184  InitSearch-time: 0.0059   Search-time: 0.833

****    BFS--BDC
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 1.0613ms         5540            5540           1399
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 200/200
Update-time: 0.06315  InitSearch-time: 0.17725   Search-time: 0.82045

****    BFS--BDC
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 2.5282ms         5169            5169           1839
[Object Level Linear] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 1.66205  InitSearch-time: 0.0808   Search-time: 0.7852

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 8287ms                 6.6413ms         8205            8205              73
[Object Level KGraph] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 5.4768  InitSearch-time: 0.0073   Search-time: 1.15695
Effective count: 8589 Graph calcCount: 30.6476
---------------------------------------------------------------------------

****    BFS--LRU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.8422ms         5936            5936           837
[Query Level] Final Cache Size/Given Cache Size : 200/200
Update-time: 2.5E-4  InitSearch-time: 0.0074   Search-time: 0.8344

****    BFS--LRU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 1.0080ms         5204            5204           1928
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 200/200
Update-time: 5.0E-4  InitSearch-time: 0.24765   Search-time: 0.7592

****    BFS--LRU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 1.0878ms         5201            5201           1928
[Object Level Linear] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 0.2397  InitSearch-time: 0.10335   Search-time: 0.7443

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7774ms                 4.9999ms         9059            9059              41
[Object Level KGraph] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 3.71425  InitSearch-time: 0.0041   Search-time: 1.2813
Effective count: 7709 Graph calcCount: 19.47685
---------------------------------------------------------------------------

****    BFS--FIFO
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.8377ms         5938            5938           835
[Query Level] Final Cache Size/Given Cache Size : 200/200
Update-time: 1.0E-4  InitSearch-time: 0.00765   Search-time: 0.8295

****    BFS--FIFO
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.9454ms         5207            5207           1922
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 200/200
Update-time: 4.5E-4  InitSearch-time: 0.1938   Search-time: 0.75075

****    BFS--FIFO
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.8940ms         5204            5204           1912
[Object Level Linear] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 0.08065  InitSearch-time: 0.0757   Search-time: 0.7375

***        FIFO-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7774ms                 4.8241ms         8553            8553              68
[Object Level KGraph] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 3.5973  InitSearch-time: 0.00485   Search-time: 1.22155
Effective count: 7626 Graph calcCount: 28.2825
---------------------------------------------------------------------------

****    BFS--LFU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.8659ms         6176            6176           454
[Query Level] Final Cache Size/Given Cache Size : 200/200
Update-time: 0.00305  InitSearch-time: 0.00635   Search-time: 0.8564

****    BFS--LFU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 0.9935ms         5483            5483           1302
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 200/200
Update-time: 0.0023  InitSearch-time: 0.19925   Search-time: 0.79125

****    BFS--LFU
construct time / mean search time / mean node accesses / mean calc count / hit count:
 7641ms                 1.1301ms         5276            5276           1333
[Object Level Linear] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 0.30115  InitSearch-time: 0.07475   Search-time: 0.75405

***        LFU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 7774ms                 4.6244ms         7478            7478             181
[Object Level KGraph] Final Cache Size/Given Cache Size : 2000/2000
Update-time: 3.52735  InitSearch-time: 0.0068   Search-time: 1.09
Effective count: 9764 Graph calcCount: 39.38015
---------------------------------------------------------------------------
cache test-time cost: 412738

Data: sift      qSize: 20000    dbSize: 500000  k: 100  dim: 10         sample: 100     Bucket Size: 20
VP tree construction in  19332 ms with 500000 nodes 18 layers

***        Best-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 1.5368ms         7355            7355           20000
---------------------------------------------------------------------------

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 4.3222ms        20164           20164             151
[Query Level] Final Cache Size/Given Cache Size : 50/50
Update-time: 0.00605  InitSearch-time: 0.01625   Search-time: 4.29945

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 4.8234ms        18998           18998            1284
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 50/50
Update-time: 0.10105  InitSearch-time: 0.5427   Search-time: 4.17925

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 52.5026ms       16917           16917             986
[Object Level Linear] Final Cache Size/Given Cache Size : 5000/5000
Update-time: 47.96865  InitSearch-time: 0.49515   Search-time: 4.03845

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 382.2930ms      21460           21460             141
[Object Level KGraph] Final Cache Size/Given Cache Size : 5000/5000
Update-time: 377.30935  InitSearch-time: 0.05885   Search-time: 4.9237
Effective count: 48024 Graph calcCount: 273.083
---------------------------------------------------------------------------

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 3.6734ms        19735           19735             280
[Query Level] Final Cache Size/Given Cache Size : 50/50
Update-time: 3.0E-4  InitSearch-time: 0.01525   Search-time: 3.6576

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 3.8524ms        17205           17205            1044
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 50/50
Update-time: 3.5E-4  InitSearch-time: 0.57655   Search-time: 3.2752

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
19332ms                 9.4941ms        17195           17195            1050
[Object Level Linear] Final Cache Size/Given Cache Size : 5000/5000
Update-time: 5.9161  InitSearch-time: 0.29945   Search-time: 3.27835



Data: Deep1M    qSize: 20000    dbSize: 50000   k: 100  dim: 10         sample: 100     Bucket Size: 20
VP tree construction in  1789 ms with 50000 nodes 15 layers

***        Best-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 1.9925ms        22423           22423           20000
---------------------------------------------------------------------------

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.0328ms        27744           27744             303
[Query Level] Final Cache Size/Given Cache Size : 5/5
Update-time: 0.00125  InitSearch-time: 0.00845   Search-time: 3.0227

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 4.6199ms        27441           27441            2798
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 5/5
Update-time: 0.03125  InitSearch-time: 0.08445   Search-time: 4.5038

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 12.6251ms       27714           27714            1611
[Object Level Linear] Final Cache Size/Given Cache Size : 500/500
Update-time: 7.5999  InitSearch-time: 0.05885   Search-time: 4.9656

***        BDC-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 28.0345ms       27843           27843             293
[Object Level KGraph] Final Cache Size/Given Cache Size : 500/500
Update-time: 24.4461  InitSearch-time: 0.04145   Search-time: 3.54605
Effective count: 19360 Graph calcCount: 199.96115
---------------------------------------------------------------------------

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 2.8305ms        27891           27891              63
[Query Level] Final Cache Size/Given Cache Size : 5/5
Update-time: 3.0E-4  InitSearch-time: 0.00845   Search-time: 2.8215

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 2.8901ms        27743           27743            1381
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 5/5
Update-time: 2.5E-4  InitSearch-time: 0.08795   Search-time: 2.80165

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.0434ms        27743           27743            1362
[Object Level Linear] Final Cache Size/Given Cache Size : 500/500
Update-time: 0.15735  InitSearch-time: 0.05165   Search-time: 2.8339

***        LRU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 20.1304ms       27860           27860             273
[Object Level KGraph] Final Cache Size/Given Cache Size : 500/500
Update-time: 16.19645  InitSearch-time: 0.0333   Search-time: 3.9003
Effective count: 17654 Graph calcCount: 184.097
---------------------------------------------------------------------------

***        FIFO-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.7503ms        27891           27891              63
[Query Level] Final Cache Size/Given Cache Size : 5/5
Update-time: 2.5E-4  InitSearch-time: 0.0074   Search-time: 3.74235

***        FIFO-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.9213ms        27743           27743            1367
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 5/5
Update-time: 4.5E-4  InitSearch-time: 0.10155   Search-time: 3.81875

***        FIFO-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 4.1723ms        27744           27744            1357
[Object Level Linear] Final Cache Size/Given Cache Size : 500/500
Update-time: 0.0997  InitSearch-time: 0.05565   Search-time: 4.0167

***        FIFO-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 20.5913ms       27845           27845             416
[Object Level KGraph] Final Cache Size/Given Cache Size : 500/500
Update-time: 15.99245  InitSearch-time: 0.04175   Search-time: 4.5564
Effective count: 15845 Graph calcCount: 199.57695
---------------------------------------------------------------------------

***        LFU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.6788ms        27741           27741             322
[Query Level] Final Cache Size/Given Cache Size : 5/5
Update-time: 8.0E-4  InitSearch-time: 0.0089   Search-time: 3.66865

***        LFU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 3.6742ms        27417           27417            3219
[Query2Object Level Linear] Final Cache Size/Given Cache Size : 5/5
Update-time: 5.5E-4  InitSearch-time: 0.08075   Search-time: 3.5924

***        LFU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 4.1313ms        27637           27637            1865
[Object Level Linear] Final Cache Size/Given Cache Size : 500/500
Update-time: 0.42035  InitSearch-time: 0.05465   Search-time: 3.6556

***        LFU-BFS 
construct time / mean search time / mean node accesses / mean calc count/ hit count:
 1789ms                 21.2154ms       27722           27722             644
[Object Level KGraph] Final Cache Size/Given Cache Size : 500/500
Update-time: 16.7203  InitSearch-time: 0.04225   Search-time: 4.4524
Effective count: 20190 Graph calcCount: 205.06065
---------------------------------------------------------------------------
cache test-time cost: 2951275


DF_stime =[9.9705,9.9848,9.9144 9.2702 9.5946 ]
BF_stime =[1.1251,1.2670, 1.4058 1.3419 1.4178]
Graph_stime =[1.960725, 2.137275  2.563875 2.5164 2.768475 ]
Liner_stime = [1.85505  2.20565 2.63305 2.54915 2.6288]

Graph_hit_count  = [73 25702 34975 37693 38705]   
Linear_hit_count = [100,26696, 39904, 37865, 38779] 
Graph_update_ratio = [99.8175, 35.745 , 12.5625,  5.7675,  3.2375]
Linear_update_ratio = [99.75  , 33.26  ,  0.24  ,  5.3375,  3.0525]




2025/01/08 
1. 加上平均近似率指标

2025/01/16
***        LRU-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4647       4647          9682          0.0538ms          0.8091ms          0.5817ms          1.4449ms          1.6152      19962
[Object Level hnsw] Final Cache Size/Given Cache Size : 5000/5000
 Graph calcCount: 970.66265
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 5000
Layer 1--NB OF Nodes: 1496

***        #BFS 
node accesses | calc count | unhit count | search time |
     20393      20393     100000          3.8228ms 

***        Best-BFS 
node accesses | calc count | unhit count | search time |
      7388       7388          0          1.3090ms 

***        FIFO-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      6015       6015          7949          0.0541ms          1.0866ms          0.0978ms          1.2387ms          1.6406      99997
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 304.95557
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2396

***        LRU-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      6028       6028          8219          0.0540ms          1.0885ms          0.1581ms          1.3007ms          1.6511      99997
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 299.64094
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2453

***        LFU-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     14395      14395         78974          0.0250ms          2.7567ms          1.5713ms          4.3532ms          8.4563      99997
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 501.01694
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2398

***        BDC-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      6015       6015          8031          0.0506ms          1.0670ms          0.3789ms          1.4966ms          1.6415      99997
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 306.59819
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2423

***        GLO-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      5984       5984          7065          0.0624ms          1.0709ms          0.2351ms          1.3684ms          1.6298      99997
[Object Level hnsw] Final Cache Size/Given Cache Size : 68223/8000
 Graph calcCount: 342.95291
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 68223
Layer 1--NB OF Nodes: 20410
---------------------------------------------------------------------------
---------------------------------------------------------------------------
Test Done! Cache test-time cost: 2544027 ms
like@dell-PowerEdge-R740:~/codes1/ball-VP$ ^C

like@dell-PowerEdge-R740:~/codes1/ball-VP$  /usr/bin/env /usr/local/jdk-11.0.18/bin/java -cp /home/like/.vscode-server/data/User/workspaceStorage/9de51ba5597546271770798c64b79fa0-2/redhat.java/jdt_ws/ball-VP_cefbcf7b/bin VPTree.Test 
Data Loaded in 3279 ms
Dimension Reduced in 188844 ms
KMeans Cluster Done! Cluster Size: 100
Kmeans Finished in 17365 ms
VP in  35202.0 ms: 800000 nodes 19 layers
Data: DEEP      qSize: 100000   dbSize: 800000  k: 10   dim: 10         sample: 100     Bucket Size: 20         factor: 2.00    cache size: 8000     

***        #BFS 
node accesses | calc count | unhit count | search time |
     82936      82936     100000         24.8959ms 

***        Best-BFS 
node accesses | calc count | unhit count | search time |
     74244      74244          0         21.8817ms 

***        FIFO-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     67020      67020          1457          0.0813ms         19.4511ms          0.0165ms         19.5492ms          1.6562      99989
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 222.48193
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2325

***        LRU-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     66984      66984          1388          0.0816ms         19.2391ms          0.0207ms         19.3415ms          1.6546      99989
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 221.15486
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2375

***        LFU-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     67038      67038          1315          0.0828ms         19.6254ms          0.0199ms         19.7283ms          1.6562      99989
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 229.25151
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2363

***        BDC-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     66965      66965          1366          0.0800ms         19.3481ms          0.0411ms         19.4694ms          1.6535      99989
[Object Level hnsw] Final Cache Size/Given Cache Size : 8000/8000
 Graph calcCount: 225.70699
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 8000
Layer 1--NB OF Nodes: 2370

***        GLO-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     66907      66907          1106          0.0832ms         18.9760ms          0.0110ms         19.0704ms          1.6507      99989
[Object Level hnsw] Final Cache Size/Given Cache Size : 10843/8000
 Graph calcCount: 228.78187
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 10843
Layer 1--NB OF Nodes: 3236
\

-------------------------------------

Data Loaded in 5367 ms
Dimension Reduced in 170957 ms
KMeans Cluster Done! Cluster Size: 100
Kmeans Finished in 8567 ms
VP in  17677.0 ms: 400000 nodes 18 layers
Data: SIFT      qSize: 50000    dbSize: 400000  k: 10   dim: 10         sample: 100     Bucket Size: 20         factor: 1.50    cache size: 4000     

***        BDC-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4268       4268         12285          0.0486ms          0.6502ms          0.4186ms          1.1175ms          1.4265      49999
[Object Level hnsw] Final Cache Size/Given Cache Size : 4000/4000
 Graph calcCount: 543.4014
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 4000
Layer 1--NB OF Nodes: 1129

***        GLO-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4151       4151          9959          0.0635ms          0.6468ms          0.6899ms          1.4005ms          1.3850      49999
[Object Level hnsw] Final Cache Size/Given Cache Size : 91054/4000
 Graph calcCount: 606.48042
HNSW Graph Info:
  The Maximum Layer: 2
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 91054
Layer 1--NB OF Nodes: 27323
---------------------------------------------------------------------------
---------------------------------------------------------------------------
2025/01/18
***        #DFS 
node accesses | calc count | unhit count | search time | meanApproxRatio|
     32508      38508     400000          5.9797ms      Infinityms

***        FIFO-DFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4815       4815         44948          0.0421ms          0.7829ms          0.0895ms          0.9147ms          1.3202     399997
[Object Level hnsw] Final Cache Size/Given Cache Size : 6000/6000
 Graph calcCount: 277.505585
HNSW Graph Info:
  The Maximum Layer: 1
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 6000

***        LRU-DFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4763       4763         38874          0.0401ms          0.7773ms          0.1256ms          0.9431ms          1.3062     399997
[Object Level hnsw] Final Cache Size/Given Cache Size : 6000/6000
 Graph calcCount: 261.712915
HNSW Graph Info:
  The Maximum Layer: 1
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 6000

***        LFU-DFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
     20764      20764        382744          0.0087ms          3.7657ms          1.3209ms          5.0955ms          6.9943     399997
[Object Level hnsw] Final Cache Size/Given Cache Size : 6000/6000
 Graph calcCount: 321.79143
HNSW Graph Info:
  The Maximum Layer: 1
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 6000

***        BDC-BFS 
node accesses | calc count | unhit count | init search-time | vp search time | cache update time | run time | meanApproxRatio | validCount|
      4555       4555         41181          0.0402ms          0.7465ms          0.2454ms          1.0323ms          1.3101     399997
[Object Level hnsw] Final Cache Size/Given Cache Size : 6000/6000
 Graph calcCount: 261.994985
HNSW Graph Info:
  The Maximum Layer: 1
  The Maximum Connections: 20
HNSW Graph Structure:
Layer 0--NB OF Nodes: 6000

