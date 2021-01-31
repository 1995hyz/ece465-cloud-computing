import java.util.Random;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Philosopher implements Runnable {
    private int ID;
    private Fork leftFork;
    private Fork rightFork;
    private static Logger logger = LogManager.getLogger(Philosopher.class);
    //private Object plate;

    public Philosopher(int ID, Fork leftFork, Fork rightFork) {
        this.ID = ID;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    public void run() {
        Random random = new Random();
        while (true){
            // think
            try {
                Thread.sleep(random.nextInt(30000));
            } catch (InterruptedException e) {
                logger.info(String.format("Philosopher %s thinking has been interrupted.", Integer.toString(this.ID)));
            }
            if (!leftFork.isTaken() && !rightFork.isTaken()){
                leftFork.take();
                rightFork.take();
                System.out.println("Philosopher " + ID + " has picked up forks " + leftFork.getID() + " and " + rightFork.getID());
                // eat
                try {
                    Thread.sleep(random.nextInt(10000));
                } catch (InterruptedException e) {
                    logger.info(String.format("Philosopher %s eating has been interrupted.", Integer.toString(this.ID)));
                }
                leftFork.put();
                rightFork.put();
                System.out.println("Philosopher " + ID + " has put down forks " + leftFork.getID() + " and " + rightFork.getID());


            }
        }

    }
}
