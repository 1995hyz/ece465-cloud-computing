import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Initialize sockets
        Grid grid = new Grid(3);
        grid.loadGrid("src/test/grid_hard.txt");
        BlockingQueue<Grid> fringe = new ArrayBlockingQueue(1024);
        fringe.put(grid);
        int num_threads = 4;
        AtomicInteger threads_waiting = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean((false));
        long start = System.nanoTime();
        List threads = new ArrayList();
        for (int i = 0; i < num_threads; i++){
            Thread t = (new Thread(new Solver(i+1, fringe, threads_waiting, num_threads, complete)));
            t.start();
            threads.add(t);
        }
        for (int i = 0; i < num_threads; i++){
            ((Thread) threads.get(i)).join();
        }
        long end = System.nanoTime();
        long elapsedTime = end-start;
        System.out.println(String.format("Grid solved in %f milliseconds.", elapsedTime/1e6));
        return;
    }
}
