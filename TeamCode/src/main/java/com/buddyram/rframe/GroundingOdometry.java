package com.buddyram.rframe;

public class GroundingOdometry<P> implements Odometry<P> {
    public Odometry<P> getRelative() {
        return relative;
    }

    public Odometry<P> getAbsolute() {
        return absolute;
    }

    protected Odometry<P> absolute;
    protected Odometry<P> relative;
    protected GroundingCondition<P> groundingCondition;
    public GroundingOdometry(Odometry<P> absolute, Odometry<P> relative, GroundingCondition<P> groundingCondition) {
        this.absolute = absolute;
        this.relative = relative;
        this.groundingCondition = groundingCondition;
    }

    public P sync() {
        P res = absolute.get();
        if (res != null) {
            this.setPosition(res);
            return res;
        }
        return null;
    }

    @Override
    public P get() {
        P p = null;
        if (groundingCondition.canSync()) {
            p = this.sync();
        }
        if (p != null) {
            return p;
        }
        return this.relative.get();
    }

    @Override
    public boolean init() {
        this.relative.init();
        this.absolute.init();
        return true;
    }

    @Override
    public void setPosition(P pos) {
        this.relative.setPosition(pos);
    }

    @Override
    public void cleanup() {
        this.absolute.cleanup();
        this.relative.cleanup();
    }

    public interface GroundingCondition<P> {
        boolean canSync();
    }
}
