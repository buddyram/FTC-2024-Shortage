package com.buddyram.rframe;

public class Utils {
    public static double angleDifference(double angle1, double angle2) {
        double normalizedAngle1 = normalizeAngle(angle1); // current
        double normalizedAngle2 = normalizeAngle(angle2); // target
        return (normalizedAngle2 - normalizedAngle1 + 540) % 360 - 180;
    }

    public static double normalizeAngle(double angle) {
        double modded = angle % 360;
        if (modded < 0) {
            return 360 + modded;
        } else {
            return modded;
        }
    }

    //public static double minimum
}
