import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Solver implements Runnable {
    private final int ID;
    private static final Logger logger = LogManager.getLogger(Solver.class);
    protected BlockingQueue<Grid> fringe;
    protected BlockingQueue<Grid> explored_grids;
    private final AtomicInteger threads_waiting;
    private AtomicBoolean complete;
    int num_threads;
    private Grid tempGrid;


    public Solver(int ID, BlockingQueue<Grid> fringe, BlockingQueue<Grid> explored_grids,
                  AtomicInteger threads_waiting, int num_threads, AtomicBoolean complete) {
        this.ID = ID;
        this.fringe = fringe;
        this.explored_grids = explored_grids;
        this.threads_waiting = threads_waiting;
        this.num_threads = num_threads;
        this.complete = complete;
    }

    public void run() {
        while (threads_waiting.get() < num_threads && !complete.get()) {
            while (fringe.isEmpty()) {
                synchronized (threads_waiting) {
                    threads_waiting.incrementAndGet();
                    try {
                        threads_waiting.wait();
                    } catch (InterruptedException e) {
                        logger.info(String.format("Solver %s waiting has been interrupted.", Integer.toString(this.ID)));
                    }
                    threads_waiting.decrementAndGet();
                }
            }
            try {
                tempGrid = fringe.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String indexKey = tempGrid.findNextIndexToSolveGrid();
            Map<String, List<Integer>> possibleValues = tempGrid.getPossibleValues();
            List<Integer> values = possibleValues.get(indexKey);
            logger.debug(String.format("indexKey: %s",indexKey));
            logger.debug(String.format("Number of possible values: %d", values.size()));
            Integer testValue = values.get(0);
            int rowIndex = indexKey.charAt(0);
            int colIndex = indexKey.charAt(1);
            tempGrid.reduce(rowIndex, colIndex, testValue);
            logger.debug(String.format("Reduced grid at row %d and col %d given value %d",rowIndex,colIndex,testValue));
            if(!checkExploredGrids(tempGrid,explored_grids)){
                if (tempGrid.validateGrid()) {
                    if(tempGrid.isSolution()){
                        tempGrid.printResult();
                        complete.set(true);
                        complete.notifyAll();
                    }
                    else{
                        try {
                            fringe.put(tempGrid);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            explored_grids.put(tempGrid);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        threads_waiting.notifyAll();
                    }
                } else {
                    try {
                        explored_grids.put(tempGrid);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("No solution can be found for the provided grid.");
        complete.set(true);
        complete.notifyAll();
    }

    private boolean checkExploredGrids(Grid tempGrid, BlockingQueue<Grid> explored_grids){
        for (Grid explored_grid : explored_grids) {
            if (tempGrid.equals(explored_grid)) {
                return true;
            }
        }
        return false;
    }
}
