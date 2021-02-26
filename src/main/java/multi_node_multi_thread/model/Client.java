package multi_node_multi_thread.model;

import multi_node_multi_thread.Grid;
import multi_node_multi_thread.Solver;
import multi_node_multi_thread.utils.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static Logger logger = LogManager.getLogger(Client.class);

    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[1]);
        try {
            Socket client = new Socket("localhost", portNumber);
            logger.info(String.format("Client connects to port %d", portNumber));
            ObjectInputStream clientInput = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream clientOutput = new ObjectOutputStream(client.getOutputStream());

            // Receive initial setup from the paired manager
            Grid initialGrid = (Grid) clientInput.readObject();
            BlockingQueue<Grid> fringe = new ArrayBlockingQueue<>(Constants.SOLVER_FRINGE_MAX_SIZE);
            fringe.put(initialGrid);
            int numThreads = 4;
            AtomicInteger threads_waiting = new AtomicInteger(0);
            AtomicBoolean complete = new AtomicBoolean((false));
            long start = System.nanoTime();
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numThreads; i++){
                Thread t = (new Thread(new Solver(i+1, fringe, threads_waiting, numThreads, complete)));
                t.start();
                threads.add(t);
            }
            for (int i = 0; i < numThreads; i++){
                (threads.get(i)).join();
            }
            long end = System.nanoTime();
            long elapsedTime = end-start;
            logger.info(String.format("multi_node_multi_thread.model.Grid solved in %f milliseconds.", elapsedTime/1e6));
        } catch (IOException e) {
            logger.error(String.format("Unable to establish connection to port %d, ex, %s. Client exits", portNumber, e.toString()));
        } catch (ClassNotFoundException e) {
            logger.error(String.format("Unable to retrieve object from the socket, ex, %s. Client exits.", e.toString()));
        } catch (InterruptedException e) {
            logger.error(String.format("Client is interrupted, ex, %s.", e.toString()));
        }
    }
}
