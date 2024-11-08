package com.buddyram.rframe;

/**
 * A 3d vector, with x, y, and z coordinates.
 * @author Ram Stewart<ram@buddyram.com>
 */
public class Vector3D {
    public final double x, y, z;

    /**
     * Create a new 3d vector, with x, y, and z coordinates.
     * @param x the x coordinate of the vector
     * @param y the y coordinate of the vector
     * @param z the z coordinate of the vector
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Add this 3d vector to another 3d vector.
     * @param other the other 3d vector
     * @return a new 3d vector, the result of the sum of the vectors
     */
    public Vector3D add(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Find the distance between this 3d vector and another 3d vector.
     * @param other the other 3d vector
     * @return a double, the distance between the two 3d vectors
     */
    public double distance(Vector3D other) {
        return Math.sqrt(Math.pow(Math.abs(this.x - other.x), 2) +
                         Math.pow(Math.abs(this.y - other.y), 2) +
                         Math.pow(Math.abs(this.z - other.z), 2)
        );
    }

    /**
     * Check if the two if this 3d vector is equal to another 3d vector.
     * @param other the other 3d vector
     * @return a boolean, True if the two 3d vectors are equal, and False if the two 3d vectors are not equal
     */
    public boolean equals(Vector3D other) {
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }
}
