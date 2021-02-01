import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Grid grid = new Grid(2);
        grid.loadGrid("src/test/sample_grid_small.txt");
        BlockingQueue<Grid> fringe = new ArrayBlockingQueue(1024);
        fringe.put(grid);
        BlockingQueue<Grid> explored_grids = new ArrayBlockingQueue(1024);
        int num_threads = 5;
        AtomicInteger threads_waiting = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean((false));
        for (int i = 0; i < num_threads; i++){
            (new Thread(new Solver(i+1, fringe, explored_grids,threads_waiting, num_threads, complete))).start();
        }
        return;
    }
}
