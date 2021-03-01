package multi_node_multi_thread;

import multi_node_multi_thread.model.Manager;
import multi_node_multi_thread.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Grid grid = new Grid(3);
        grid.loadGrid("src/test/grid_hard.txt");
        BlockingQueue<Grid> exploredGrid = new ArrayBlockingQueue<>(Constants.EXPLORED_QUEUE_MAX_SIZE);
        int nodeCount = 2;
        AtomicBoolean complete = new AtomicBoolean((false));
        AtomicInteger threadsWaiting = new AtomicInteger(0);
        List<Thread> managerThreads = new ArrayList<>();
        for (int i = 0; i < nodeCount; i++) {
            Thread t = (new Thread(new Manager(i, 10000+i, grid, exploredGrid, threadsWaiting, complete)));
            t.start();
            managerThreads.add(t);
        }
        for (int i = 0; i < nodeCount; i++){
            (managerThreads.get(i)).join();
        }
    }
}
