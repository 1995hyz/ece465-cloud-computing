package multi_node_multi_thread.model;

import multi_node_multi_thread.Grid;
import multi_node_multi_thread.Node;
import multi_node_multi_thread.Solver;
import multi_node_multi_thread.utils.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static Logger logger = LogManager.getLogger(Client.class);

    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[1]);
        String id = UUID.randomUUID().toString();
        try {
            BlockingQueue<Grid> fringeProposed = new ArrayBlockingQueue<>(Constants.SOLVER_FRINGE_MAX_SIZE);
            BlockingQueue<Grid> fringeApproved = new ArrayBlockingQueue<>(Constants.SOLVER_FRINGE_MAX_SIZE);
            int numThreads = 4;
            AtomicInteger threads_waiting = new AtomicInteger(0);
            AtomicBoolean complete = new AtomicBoolean((false));
            long start = System.nanoTime();
            List<Thread> threads = new ArrayList<>();
            Thread n = (new Thread(new Node(id, portNumber, fringeProposed, fringeApproved , complete)));
            n.start();
            threads.add(n);
            Thread t1 = (new Thread(new Solver(1, fringeProposed, fringeApproved, threads_waiting, numThreads, complete, true)));
            t1.start();
            threads.add(t1);
            for (int i = 1; i < numThreads; i++){
                Thread t = (new Thread(new Solver(i+1, fringeProposed, fringeApproved, threads_waiting, numThreads, complete, false)));
                t.start();
                threads.add(t);
            }
            for (int i = 0; i < numThreads+1; i++){
                (threads.get(i)).join();
            }
            long end = System.nanoTime();
            long elapsedTime = end-start;
            logger.info(String.format("Grid solved in %f milliseconds.", elapsedTime/1e6));
        } catch (InterruptedException e) {
            logger.error(String.format("Client [%s] has been interrupted, ex, %s", id, e.toString()));
        }
    }
}
