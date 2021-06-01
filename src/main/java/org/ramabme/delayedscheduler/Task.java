package org.ramabme.delayedscheduler;

public interface Task {
    // epoch in milliseconds when to execute this task
    public long scheduledTime();

    // Execute this task
    public void run();
}
