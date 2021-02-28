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

public class Node implements Runnable {

    private static final Logger logger = LogManager.getLogger(Node.class);

    private String id;
    private int portNumber;
    private BlockingQueue<Grid> fringeProposed;
    private BlockingQueue<Grid> fringeApproved;
    private Socket client;
    private ObjectInputStream clientInput;
    private ObjectOutputStream clientOutput;
    private final AtomicBoolean complete;

    public Node(String id, int portNumber, BlockingQueue<Grid> fringeProposed, BlockingQueue<Grid> fringeApproved, AtomicBoolean complete) {
        this.id = id;
        this.portNumber = portNumber;
        this.fringeProposed = fringeProposed;
        this.fringeApproved = fringeApproved;
        this.complete = complete;
    }

    @Override
    public void run() {
        try {
            setUp();
            // Receive initial setup from the paired manager
            Grid initialGrid = (Grid) this.clientInput.readObject();
            this.fringeApproved.put(initialGrid);
            while (true) {
                synchronized (complete) {
                    if (complete.get()) {
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
                    }
                    if (! response.isShouldContinue()) {
                        synchronized (complete) {
                            complete.set(true);
                        }
                        break;
                    }
                }
            }
            logger.info(String.format("Node [%s] has exited.", this.id));

        } catch (IOException e) {
            logger.error(String.format("Unable to establish connection to port %d, ex, %s. Client exits", portNumber, e.toString()));
        } catch (ClassNotFoundException e) {
            logger.error(String.format("Unable to retrieve object from the socket, ex, %s. Client exits.", e.toString()));
        } catch (InterruptedException e) {
            logger.error(String.format("Node is interrupted, ex, %s.", e.toString()));
        } catch (Throwable e) {
            logger.error(String.format("Unable to setup connection to port %d, ex, %s. Client exits", portNumber, e.toString()));
        }
    }

    private void setUp() throws Throwable {
            this.client = new Socket("localhost", portNumber);
            logger.info(String.format("Client connects to port %d", portNumber));
            this.clientInput = new ObjectInputStream(client.getInputStream());
            this.clientOutput = new ObjectOutputStream(client.getOutputStream());
    }
}

