/**
    @author Akshay Mattoo
*/

package Distance;


/**
    Interface for implementing different Distance Metrics classes
*/
public interface DistanceFunction
{
    /**
        Method for computing the distance between two double[]s vectors
        @param two double[] objects
        @return distance between the two double[] objects
    */
    double distance (double[] one, double[] two);
}
