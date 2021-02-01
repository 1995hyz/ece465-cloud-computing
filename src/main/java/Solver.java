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
    private final AtomicBoolean complete;
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
        while (threads_waiting.get() < num_threads) {
            while (fringe.isEmpty()) {
                synchronized (complete){
                    if(complete.get()){
                        break;
                    }
                }
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
            synchronized (complete){
                if(complete.get()){break;}
            }
            try {
                tempGrid = fringe.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String indexKey = tempGrid.findNextIndexToSolveGrid();
            Map<String, List<Integer>> possibleValues = tempGrid.getPossibleValues();
            List<Integer> values = possibleValues.get(indexKey);

            for (int value : values){
                synchronized (complete){
                    if(complete.get()){break;}
                }
                int rowIndex = Character.getNumericValue(indexKey.charAt(0));
                int colIndex = Character.getNumericValue(indexKey.charAt(1));
                Grid newGrid = tempGrid.copy();
                newGrid.reduce(rowIndex, colIndex, value);
//                logger.debug(String.format("Reduced grid at row %d and col %d given value %d",rowIndex,colIndex,value));
                if(!checkExploredGrids(newGrid, explored_grids)){
                    if (newGrid.validateGrid()) {
                        if(newGrid.isSolution()){
                            newGrid.printResult();
                            synchronized (complete){
                                complete.set(true);
                            }
                            synchronized (threads_waiting){
                                threads_waiting.notifyAll();
                            }
                        } else {
                            try {
                                fringe.put(newGrid);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            try {
                                explored_grids.put(newGrid);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized (threads_waiting){
                                threads_waiting.notifyAll();
                            }

                        }
                    } else {
                        try {
                            explored_grids.put(newGrid);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    logger.debug("Found grid already in explored queue.");
                }
            }

        }
        synchronized (complete){
            if (!complete.get()){
                System.out.println("No solution can be found for the provided grid.");
            }
        }
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
