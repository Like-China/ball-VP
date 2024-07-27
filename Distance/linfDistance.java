/**
    @author Akshay Mattoo
*/

package Distance;

/**
 * Implements DistanceFunction interface for Chebyshev distance metric
 */
public class linfDistance implements DistanceFunction {

    /**
     * Computes the Chebyshev distance (linf Distance) between two double[] vectors
     * 
     * @param two double[] objects
     * @return Chebyshev distance between double[] one and two
     */
    public double distance(double[] p1, double[] p2) {
        double dist = 0.0;

        if (p1 == null || p2 == null) {
            System.out.println("ERROR! double[] was found to be null");
            return dist;
        }

        if (p1.length != p2.length) {
            System.out.println("ERROR! Images do not have equal number of pixels");
            return dist;
        }

        for (int i = 0; i < p1.length; ++i) {
            double x1 = p1[i];
            double x2 = p2[i];
            double diff = Math.abs(x1 - x2);

            dist = Math.max(dist, diff);
        }

        return dist;
    }
}