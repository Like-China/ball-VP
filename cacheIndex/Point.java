package cacheIndex;

import java.util.Objects;

// Class to represent a point in space
public class Point {
    double[] coordinates;
    int id; // Unique identifier for the point

    public Point(int id, double[] coordinates) {
        this.id = id;
        this.coordinates = coordinates;
    }

    // Euclidean distance calculation between this point and another point
    public double distance(Point other) {
        double sum = 0;
        for (int i = 0; i < this.coordinates.length; i++) {
            sum += Math.pow(this.coordinates[i] - other.coordinates[i], 2);
        }
        return Math.sqrt(sum);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Point))
            return false;
        Point other = (Point) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
