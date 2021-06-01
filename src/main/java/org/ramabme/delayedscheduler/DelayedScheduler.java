package org.ramabme.delayedscheduler;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* DelayedScheduler -
    submitTask(Task) - Tasks can be submitted to it with a scheduledTime when to run them.
                       These are ordered in a priorityqueue.
    start() - start the scheduler. the scheduler waits on them till some task is ready.
              Ready tasks are sent to the executor.
    shutDown() method shuts down the scheduler and executor.
 */
public class DelayedScheduler {
    // special task to shut down the scheduler with immediate(negative) scheduledTime()
    private static final Task shutDownTask = new Task() {
        @Override
        public long scheduledTime() {
            return -1;
        }

        @Override
        public void run() {
        }
    };

    // PriorityQueue ordered by task::scheduledTime
    private final PriorityQueue<Task> taskQueue;
    // synchronization for access to taskQueue
    private final Lock lock = new ReentrantLock();
    private final Condition taskQueueUpdated = lock.newCondition();
    // executor for the ready tasks
    private final ExecutorService taskExecutor;

    public DelayedScheduler() {
        final int numThreads=4;
        Comparator<Task> taskComparator = Comparator.comparingLong(Task::scheduledTime);
        this.taskQueue = new PriorityQueue<>(taskComparator);
        this.taskExecutor = Executors.newFixedThreadPool(numThreads);
    }

    // Wait for ready tasks to meet the scheduledTime, and call the executor on them.
    public void start() {
        long waitTime = 0;
        try {
            while (true) {
                lock.lock();
                try {
                    // wait till taskQueue has a ready task which meets scheduledTime()
                    while (taskQueue.isEmpty() ||
                            (taskQueue.peek().scheduledTime() > System.currentTimeMillis())) {
                        if (taskQueue.isEmpty()) {
                            taskQueueUpdated.await();
                        } else {
                            waitTime = taskQueue.peek().scheduledTime() - System.currentTimeMillis();
                            if (waitTime <= 0) {
                                // some task is ready i.e meets scheduledTime
                                break;
                            } else {
                                // wait for earliest waitTime
                                taskQueueUpdated.await(waitTime, TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                    boolean seenShutDownTask = false;
                    // remove and send all ready tasks to the executor.
                    // If encounters the shutDownTask, quit.
                    while (!taskQueue.isEmpty()) {
                        Task nextTask = taskQueue.peek();
                        if (nextTask.scheduledTime() > System.currentTimeMillis()) {
                            break;
                        } else if (nextTask == shutDownTask) {
                            // shut down task seen
                            seenShutDownTask = true;
                            break;
                        } else {
                            // ready tasks to be sent to executor. Remove them from queue.
                            Task readyTask = taskQueue.poll();
                            taskExecutor.execute(readyTask::run);
                        }
                    }
                    if (seenShutDownTask) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Quitting due to exception");
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        } finally {
            // shut down the executor
            shutdownTaskExecutor();
        }
    }

    // Shuts down the executor
    private void shutdownTaskExecutor() {
        System.out.println("Shutting down executor");
        taskExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
        }
    }

    // Submit task to the scheduler to be scheduled at t.scheduledTime()
    // signal taskQueueUpdated to wait for updated schedule
    public void submitTask(Task t) {
        lock.lock();
        try {
            taskQueue.add(t);
            taskQueueUpdated.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Shuts down the scheduler and the executor by adding the static shutDownTask to the queue.
    public void shutDown() {
        System.out.println("Call to Shut down");
        submitTask(shutDownTask);
    }
}
