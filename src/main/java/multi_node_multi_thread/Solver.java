package multi_node_multi_thread;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Solver implements Runnable {
    private final int ID;
    private static final Logger logger = LogManager.getLogger(Solver.class);
    private BlockingQueue<Grid> fringeProposed;
    private BlockingQueue<Grid> fringeApproved;
    private final AtomicInteger threadsWaiting;
    private final AtomicBoolean complete;
    private int numThreads;
    private Grid tempGrid;
    private int initialRowIndex = -1;
    private int initialColumnIndex = -1;
    private final AtomicBoolean initialRun;
    private Node node;

    public Solver (int id, BlockingQueue<Grid> fringeProposed, BlockingQueue<Grid> fringeApproved,
                   AtomicInteger threadsWaiting, int numThreads, AtomicBoolean complete, AtomicBoolean initialRun, Node node) {
        this.ID = id;
        this.fringeProposed = fringeProposed;
        this.fringeApproved = fringeApproved;
        this.threadsWaiting = threadsWaiting;
        this.numThreads = numThreads;
        this.complete = complete;
        this.initialRun = initialRun;
        this.node = node;
    }

    public void run() {
        while (threadsWaiting.get() < numThreads) {
            while (this.fringeApproved.isEmpty()) {
                synchronized (complete){
                    if(complete.get()){
                        break;
                    }
                }
                synchronized (threadsWaiting) {
                    threadsWaiting.incrementAndGet();
                    try {
                        threadsWaiting.wait();
                    } catch (InterruptedException e) {
                        logger.info(String.format("Solver %s waiting has been interrupted.", Integer.toString(this.ID)));
                    }
                    threadsWaiting.decrementAndGet();
                }
            }
            synchronized (complete){
                if(complete.get()){ break; }
            }
            try {
                this.tempGrid = this.fringeApproved.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            String indexKey;
            synchronized (initialRun) {
                if (initialRun.get()) {
                    this.generateRandomInitialPair();
                    indexKey = Integer.valueOf(this.initialRowIndex).toString() + Integer.valueOf(this.initialColumnIndex).toString();
                    this.initialRun.set(false);
                } else {
                    indexKey = this.tempGrid.findNextIndexToSolveGrid();
                }
            }

            Map<String, List<Integer>> possibleValues = this.tempGrid.getPossibleValues();
            List<Integer> values = possibleValues.get(indexKey);

            for (int value : values){
                synchronized (complete){
                    if(complete.get()){break;}
                }
                int rowIndex = Character.getNumericValue(indexKey.charAt(0));
                int colIndex = Character.getNumericValue(indexKey.charAt(1));
                Grid newGrid = this.tempGrid.copy();
                newGrid.reduce(rowIndex, colIndex, value);
                logger.debug(String.format("Reduced grid at row %d and col %d given value %d", rowIndex, colIndex, value));
                if(! newGrid.canPrune()){
                    if (newGrid.validateGrid()) {
                        if(newGrid.isSolution()){
                            newGrid.printResult();
                            this.node.setSolution(newGrid);
                            synchronized (complete){
                                complete.set(true);
                            }
                        } else {
                            try {
                                this.fringeProposed.put(newGrid);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        synchronized (threadsWaiting){
                            threadsWaiting.notifyAll();
                        }
                    }
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

    private void generateRandomInitialPair() {
        int gridDim = this.tempGrid.getDim();
        do {
            Random randomGenerator = new Random();
            initialRowIndex = randomGenerator.nextInt(gridDim);
            initialColumnIndex = randomGenerator.nextInt(gridDim);
        } while (this.tempGrid.getGridCell(initialRowIndex, initialColumnIndex) > 0);
    }
}
