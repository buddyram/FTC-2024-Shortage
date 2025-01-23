package com.buddyram.rframe.actions;

import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;

import java.util.Arrays;
import java.util.Collection;

public class MultiAction<R extends Robot> implements RobotAction<R> {

    private final Collection<RobotAction<R>> actions;

    @SafeVarargs
    public MultiAction(RobotAction<R>... actions) {
        this(Arrays.asList(actions));
    }

    public MultiAction(Collection<RobotAction<R>> actions) {
        this.actions = actions;
    }

    @Override
    public boolean run(R drive) throws RobotException {
        for (RobotAction<R> action : this.actions) {
            drive.getLogger().log("MultiAction",  action.getClass().getCanonicalName());
            action.run(drive);
        }
        return true;
    }
}
