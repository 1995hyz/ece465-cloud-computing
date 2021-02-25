package multi_node_multi_thread;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {

    private static final Logger logger = LogManager.getLogger(Node.class);

    private String id;
    private Grid grid;
    private BlockingQueue<Grid> fringe = new ArrayBlockingQueue<>(1024);
    private int threadsCount;

    public Node(String id, Grid grid, int threadsCount) {
        this.id = id;
        this.grid = grid.copy();
        this.threadsCount = threadsCount;
    }

    public void setUp() throws Throwable {
        logger.info(String.format("Starting multi_node_multi_thread.model.Node [%s]", this.id));
        this.fringe.put(grid);
        AtomicInteger threads_waiting = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean((false));
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < this.threadsCount; i++){
            Thread t = (new Thread(new Solver(i+1, fringe, threads_waiting, threadsCount, complete)));
            t.start();
            threads.add(t);
        }
        for (int i = 0; i < threadsCount; i++){
            (threads.get(i)).join();
        }
        long end = System.nanoTime();
        long elapsedTime = end-start;
        logger.info(String.format("multi_node_multi_thread.model.Node [%s]: multi_node_multi_thread.model.Grid solved in %f milliseconds.", this.id, elapsedTime/1e6));
    }

    public String getId() {
        return this.id;
    }

}

