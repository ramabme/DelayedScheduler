import org.ramabme.delayedscheduler.DelayedScheduler;
import org.ramabme.delayedscheduler.Task;

public class TestShutDown {

    public static void main(String[] args) {
        DelayedScheduler scheduler = new DelayedScheduler();
        // Creating tasks
        for(int i=0; i<5; ++i) {
            Task task = new TestTask(5000, i);
            scheduler.submitTask(task);
        }
        // Calling shutdown - testing shutdown before any other tasks
        scheduler.shutDown();
        scheduler.start();
    }

}
