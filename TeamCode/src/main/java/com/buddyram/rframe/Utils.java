package com.buddyram.rframe;

public class Utils {
    public static double angleDifference(double angle1, double angle2) {
        return Math.abs(angle1 % 360 - angle2 % 360);
    }
}
