package multi_node_multi_thread;

import multi_node_multi_thread.model.Manager;
import multi_node_multi_thread.utils.Constants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Grid grid = new Grid(3);
        grid.loadGrid("src/test/grid_hard.txt");
        BlockingQueue<Grid> exploredGrid = new ArrayBlockingQueue<>(Constants.EXPLORED_QUEUE_MAX_SIZE);
        int nodeCount = 2;
        int threadsCount = 4;
        AtomicBoolean complete = new AtomicBoolean((false));
        List<Thread> managerThreads = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            Thread t = (new Thread(new Manager(i, 1000+i, grid, exploredGrid)));
            t.start();
            managerThreads.add(t);
        }
        for (int i = 0; i < nodeCount; i++){
            (managerThreads.get(i)).join();
        }
/*        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++ ) {
            nodes.add(new Node(Integer.valueOf(i).toString(), grid, threadsCount));
        }

        nodes.forEach(node -> {
            try {
                node.setUp();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });*/
/*        BlockingQueue<multi_node_multi_thread.model.Grid> fringe = new ArrayBlockingQueue<>(1024);
        fringe.put(grid);
        int num_threads = 4;
        AtomicInteger threads_waiting = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean((false));
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < num_threads; i++){
            Thread t = (new Thread(new multi_node_multi_thread.model.Solver(i+1, fringe, threads_waiting, num_threads, complete)));
            t.start();
            threads.add(t);
        }
        for (int i = 0; i < num_threads; i++){
            (threads.get(i)).join();
        }
        long end = System.nanoTime();
        long elapsedTime = end-start;
        logger.info(String.format("multi_node_multi_thread.model.Grid solved in %f milliseconds.", elapsedTime/1e6));*/
    }
}
