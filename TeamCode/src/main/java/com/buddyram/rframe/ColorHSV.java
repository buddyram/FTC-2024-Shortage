package com.buddyram.rframe;

import androidx.annotation.NonNull;

public class ColorHSV {
    public final double h;
    public final double s;
    public final double v;

    public ColorHSV(double h, double s, double v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }

    public double distanceColor(ColorHSV other) {
        return Math.sqrt(
            Math.pow((this.h - other.h), 2) +
            Math.pow((this.s - other.s), 2) +
            Math.pow((this.v - other.v), 2)
        );
    }

    @NonNull
    public String toString() {
        return "(" + this.h + ", " + this.s + ", " + this.v + ")";
    }
}
