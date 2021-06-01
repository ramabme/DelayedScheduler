import org.ramabme.delayedscheduler.DelayedScheduler;
import org.ramabme.delayedscheduler.Task;

import java.util.Random;

public class TestMain {

    public static void main(String[] args) {
        DelayedScheduler scheduler = new DelayedScheduler();
        Random random = new Random();
        // Creating tasks delayed over [5,5+bound) seconds
        int start=5000, bound=2000, id=0;
        for(int i=0; i<20; ++i) {
            Task task = new TestTask(start+random.nextInt(bound), id++);
            scheduler.submitTask(task);
        }
        scheduler.start();
    }

}
