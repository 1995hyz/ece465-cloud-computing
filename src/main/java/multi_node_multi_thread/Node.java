package multi_node_multi_thread;

import multi_node_multi_thread.model.ManagerResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Node implements Runnable {

    private static final Logger logger = LogManager.getLogger(Node.class);

    private String id;
    private int portNumber;
    private BlockingQueue<Grid> fringeProposed;
    private BlockingQueue<Grid> fringeApproved;
    private Socket client;
    private ObjectInputStream clientInput;
    private ObjectOutputStream clientOutput;
    private final AtomicInteger threadsWaiting;
    private final AtomicBoolean complete;
    private Grid solution;

    public Node(String id, int portNumber, BlockingQueue<Grid> fringeProposed, BlockingQueue<Grid> fringeApproved,
                AtomicInteger threadsWaiting, AtomicBoolean complete) throws Throwable {
        this.id = id;
        this.portNumber = portNumber;
        this.fringeProposed = fringeProposed;
        this.fringeApproved = fringeApproved;
        this.threadsWaiting = threadsWaiting;
        this.complete = complete;
        setUp();
    }

    @Override
    public void run() {
        try {
            // Receive initial setup from the paired manager
            Grid initialGrid = (Grid) this.clientInput.readObject();
            this.fringeApproved.put(initialGrid);
            synchronized (this.threadsWaiting) {
                this.threadsWaiting.notifyAll();
            }
            while (true) {
                synchronized (complete) {
                    if (complete.get()) {
                        this.clientOutput.writeObject(this.solution);
                        this.clientOutput.reset();
                        break;
                    }
                }
                if (! this.fringeProposed.isEmpty()) {
                    Grid gridProposed = this.fringeProposed.poll();
                    // Propose the grid to the manager to check if the grid has been explored
                    clientOutput.writeObject(gridProposed);
                    clientOutput.reset();
                    ManagerResponse response = (ManagerResponse) clientInput.readObject();
                    if (! response.isGridExplored()) {
                        logger.debug(String.format("Node [%s] has a grid approved", this.id));
                        this.fringeApproved.put(gridProposed);
                        synchronized (this.threadsWaiting) {
                            this.threadsWaiting.notifyAll();
                        }
                    } else {
                        logger.debug(String.format("Node [%s] has a grid rejected", this.id));
                    }
                    if (! response.isShouldContinue()) {
                        synchronized (this.complete) {
                            this.complete.set(true);
                        }
                        synchronized (this.threadsWaiting) {
                            this.threadsWaiting.notifyAll();
                        }
                        break;
                    }
                } else {
                    synchronized (this.complete) {
                        if (this.complete.get()) {
                            this.clientOutput.writeObject(this.solution);
                            this.clientOutput.reset();
                            break;
                        }
                    }
                    synchronized (this.threadsWaiting) {
                        this.threadsWaiting.incrementAndGet();
                        try {
                            this.threadsWaiting.wait();
                        } catch (InterruptedException e) {
                            logger.info(String.format("Solver %s waiting has been interrupted.", this.id));
                        }
                        this.threadsWaiting.decrementAndGet();
                    }
                }
            }
            logger.info(String.format("Node [%s] has exited.", this.id));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(String.format("Unable to establish connection to port %d, ex, %s. Client exits", portNumber, e.toString()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(String.format("Unable to retrieve object from the socket, ex, %s. Client exits.", e.toString()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error(String.format("Node is interrupted, ex, %s.", e.toString()));
        }
    }

    public void setSolution(Grid solution) {
        this.solution = solution;
    }

    private void setUp() throws Throwable {
            this.client = new Socket("localhost", portNumber);
            logger.info(String.format("Client connects to port %d", portNumber));
            this.clientInput = new ObjectInputStream(client.getInputStream());
            this.clientOutput = new ObjectOutputStream(client.getOutputStream());
    }
}

