import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws IOException {
        Grid grid = new Grid(3);
        grid.loadGrid("src/test/sample_grid.txt");
        BlockingQueue<Grid> fringe = null;
        BlockingQueue<Grid> explored_grids = null;
        int num_threads = 5;
        AtomicInteger threads_waiting = new AtomicInteger(0);
        for (int i = 0; i < num_threads; i++){
            (new Thread(new Solver(i+1, fringe, explored_grids, grid, threads_waiting, num_threads))).start();
        }
        return;
    }
}
