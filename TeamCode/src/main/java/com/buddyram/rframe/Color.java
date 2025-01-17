package com.buddyram.rframe;

public class Color {
    public final double r;
    public final double g;
    public final double b;
    public final double a;

    public Color(double r, double g, double b, double a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public double distanceColor(Color other) {
        return Math.sqrt(
            Math.pow((this.r - other.r), 2) +
            Math.pow((this.g - other.g), 2) +
            Math.pow((this.b - other.b), 2)
        );
    }

    public double distanceColorAlpha(Color other) {
        return Math.sqrt(
            Math.pow((this.r - other.r), 2) +
            Math.pow((this.g - other.g), 2) +
            Math.pow((this.b - other.b), 2) +
            Math.pow((this.a - other.a), 2)
        );
    }
}
