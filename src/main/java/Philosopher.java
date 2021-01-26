import java.util.Random;

public class Philosopher implements Runnable {
    private int ID;
    private Fork leftFork;
    private Fork rightFork;
    //private Object plate;

    public Philosopher(int ID, Fork leftFork, Fork rightFork) {
        this.ID = ID;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    public void run() {
        Random random = new Random();
        while (true){
            if (!leftFork.taken && !rightFork.taken){
                leftFork.take();
                rightFork.take();
                System.out.println("Philosopher " + ID + " has picked up forks " + leftFork.ID + " and " + rightFork.ID);
                // eat
                try {
                    Thread.sleep(random.nextInt(10000));
                } catch (InterruptedException e) {}
                leftFork.put();
                rightFork.put();
                System.out.println("Philosopher " + ID + " has put down forks " + leftFork.ID + " and " + rightFork.ID);
                // think
                try {
                    Thread.sleep(random.nextInt(10000));
                } catch (InterruptedException e) {}

            }
        }

    }
}
