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

1. global cache
2. kNN Graph
3. increase the dimension
4. How to get such query-aware data?
5. time-complexity analysis

