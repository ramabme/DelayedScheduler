import org.ramabme.delayedscheduler.Task;

/* TestTask - schedule at runTimeMillis.
   Constructing with delayMillis - will set scheduled time to currentTimeMillis + delayMillis
 */
class TestTask implements Task {
    long runTimeMillis;
    int id;

    public TestTask(long delayMillis, int id) {
        this.runTimeMillis = System.currentTimeMillis() + delayMillis;
        this.id = id;
    }

    @Override
    public long scheduledTime() {
        return runTimeMillis;
    }

    @Override
    public void run() {
        // Throw on wrong schedule - run() before schedule
        if (System.currentTimeMillis() < runTimeMillis) {
            throw new RuntimeException("Task scheduled incorrectly");
        }
        System.out.printf("Running Task : %4d scheduled at %d. Time now is %d.\n",
                id, runTimeMillis, System.currentTimeMillis());
    }
}
