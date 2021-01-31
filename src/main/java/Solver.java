import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Solver implements Runnable {
    private final int ID;
    private static final Logger logger = LogManager.getLogger(Solver.class);
    protected BlockingQueue<Grid> fringe;
    protected BlockingQueue<Grid> explored_grids;
    private Grid grid;
    private AtomicInteger threads_waiting;
    int num_threads;
    private Grid tempGrid;


    public Solver(int ID, BlockingQueue<Grid> fringe, BlockingQueue<Grid> explored_grids, Grid grid,
                  AtomicInteger threads_waiting, int num_threads) {
        this.ID = ID;
        this.fringe = fringe;
        this.explored_grids = explored_grids;
        this.grid = grid;
        this.threads_waiting = threads_waiting;
        this.num_threads = num_threads;
    }

    public void run() {
        while (threads_waiting.get() < num_threads){
            while (fringe.isEmpty()){
                threads_waiting.incrementAndGet();
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    logger.info(String.format("Solver %s sleeping has been interrupted.", Integer.toString(this.ID)));
                }
                threads_waiting.decrementAndGet();
            }
            try {
                tempGrid = fringe.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
