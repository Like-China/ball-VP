# Intersection Similarity Join Over Moving Objects (IS-Join)
This implementation follows the content of the article "Intersection Similarity Join Over Moving Objects",
which implements the parts of BF-Alg, MJ-Alg and BJ-Alg, and evaluates their performances for different settings.

# Environment
Ubuntu 11

Java SE 1.8.0_91

# balltree
The package 'balltree' implements the indexing structure of our Hball-tree with/without re-partition techniques.
Specifiaclly, the BallNode.java and the BallTree.java are the implementation of Hball-tree without re-partition techniques.
The TernaryBallNode.java and the TernaryBallTree.java are the implementation of Hball-tree with re-partition techniques.

# M-tree
The package 'mtree' implements the indexing construction of M*-tree.

# utils
The package 'utils' implements the classes of Location, Ellipse, ContactPair, etc.

Location: (object id, longititude, latitude, x, y, timestamp) with some distance calculation methods

Ellipse: (object id, location1, location2, ellipse center, a, b, angle, max speed) with MBR calculation and MBR intersection calculation

Data: (Ellipse, center, radius) used for MJ-Alg

Pair: A pair of objects (first, second) of the same type

ContactPair: A pair of objects satisfying contacting (candidate) constraints

Point: A tuple (x,y) represents the sampling point during refining procedure

TimePointMR: time-point motion range implementation

# Evaluation implementation

## Setting
The default parameter settings are listed in Settings.java.
## data loading
All data source is loaded by Loader.java, the data source is detailed below in open data.
## test
The evaluation of BF-Alg, MJ-Alg, BJ#-Alg and BJ-Alg are implemented by BFAlg.java, MJALG.java, BJAlgNoRepartition.java and BJAlg.java, respectively. The refinement is implemented in Refine.java
## main entrance
Evaluate.java is the main entrance of this project for overall evaluation with varied parameters.

# Open Data
The Geolife dataset is available at https://www.kaggle.com/datasets/arashnic/microsoft-geolife-gps-trajectory-dataset.

The Porto dataset is available at https://www.kaggle.com/c/pkdd-15-predict-taxi-service-trajectory-i/data.

