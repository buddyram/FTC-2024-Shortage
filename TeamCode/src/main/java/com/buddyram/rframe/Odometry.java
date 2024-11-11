package com.buddyram.rframe;

/**
 * An odometry sensor that may sense a vector of any dimension.
 * @param <P> the dimension of the returned point in space
 * @author Ram Stewart<ram@buddyram.com>
 */
public interface Odometry<P> {
    /**
     * Get the position or measurement currently detected.
     * @return a point in space
     */
    public P get();

    /**
     * Initialize the odometry sensor
     */
    public boolean init();
}
