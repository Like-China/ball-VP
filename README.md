1. 校验best-first
2. 设计query-aware caching以提升NN查找效率，校验提升能力
3. enable without sqrt root (不满足三角不等式？)
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