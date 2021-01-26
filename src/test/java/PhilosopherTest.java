public class PhilosopherTest {
    public static void main(String[] args) {
        int philosopherCount = 5;
        Fork[] forks = new Fork[philosopherCount];
        for (int i = 0; i< philosopherCount; i++) {
            forks[i] = new Fork(i+1);
        }
        (new Thread(new Philosopher(1,forks[4],forks[0]))).start();
        (new Thread(new Philosopher(2,forks[0],forks[1]))).start();
        (new Thread(new Philosopher(3,forks[1],forks[2]))).start();
        (new Thread(new Philosopher(4,forks[2],forks[3]))).start();
        (new Thread(new Philosopher(5,forks[3],forks[4]))).start();
    }
}
