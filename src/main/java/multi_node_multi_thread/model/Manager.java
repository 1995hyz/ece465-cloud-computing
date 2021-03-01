package multi_node_multi_thread.model;

import multi_node_multi_thread.Grid;
import multi_node_multi_thread.utils.Constants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Manager implements Runnable {

    private static final Logger logger = LogManager.getLogger(Manager.class);

    private int id;
    private int portNumber;
    private Grid initialGrid;
    private Grid solution;
    private BlockingQueue<Grid> exploredGrid;
    private final AtomicInteger threadsWaiting;
    private final AtomicBoolean complete;
    private int counter = 0;

    public Manager(int id, int portNumber, Grid grid, BlockingQueue<Grid> exploredGrid, AtomicInteger threadsWaiting,
                   AtomicBoolean complete) {
        this.id = id;
        this.portNumber = portNumber;
        this.exploredGrid = exploredGrid;
        this.initialGrid = grid;
        this.threadsWaiting = threadsWaiting;
        this.complete = complete;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            logger.info(String.format("Manger [%s] connecting to port %s", id, portNumber));
            Socket socket = serverSocket.accept();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            // Send initial setup to client
            objectOutputStream.writeObject(this.initialGrid);
            objectOutputStream.reset();

            while (true) {
                synchronized (this.complete) {
                    if (this.complete.get()) {
                        objectOutputStream.writeObject(new ManagerResponse(true, false));
                        objectOutputStream.reset();
                        break;
                    }
                }
                try {

                    Grid proposedGrid = (Grid) objectInputStream.readObject();
                    if (proposedGrid == null) {
                        continue;
                    }
                    if (proposedGrid.isFilled()) {
                        synchronized (this.complete) {
                            this.complete.set(true);
                            // Find a solution
                            this.solution = proposedGrid;
                        }
                        objectOutputStream.writeObject(new ManagerResponse(true, false));
                        objectOutputStream.reset();
                        synchronized (this.threadsWaiting) {
                            threadsWaiting.notifyAll();
                        }
                        break;
                    }
                    this.counter++;
                    if (this.exploredGrid.contains(proposedGrid)) {
                        objectOutputStream.writeObject(new ManagerResponse(true, true));
                    } else {
                        objectOutputStream.writeObject(new ManagerResponse(false, true));
                        try {
                            if (this.exploredGrid.size() < Constants.EXPLORED_QUEUE_MAX_SIZE) {
                                this.exploredGrid.put(proposedGrid);
                            } else {
                                this.exploredGrid.poll();
                                this.exploredGrid.put(proposedGrid);
                            }
                        } catch (InterruptedException e) {
                            logger.error(String.format("Manager [%s] fails to add explored grid on to the explored-grid queue." +
                                    " ex, %s ", id, e.toString()));
                        }
                    }
                    objectOutputStream.reset();
                } catch (SocketException e) {
                    logger.error(String.format("Manager [%s] exit with socket exception %s", this.id, e.toString()));
                    synchronized (this.threadsWaiting) {
                        this.threadsWaiting.notifyAll();
                    }
                    break;
                }
                if (this.counter == Constants.MANAGER_COUNTER_MAX_SIZE) {
                    this.counter = 0;
                    synchronized (this.threadsWaiting) {
                        this.threadsWaiting.notifyAll();
                        this.threadsWaiting.incrementAndGet();
                        try {
                            this.threadsWaiting.wait(Constants.MANAGER_WAITING_MAX_TIME_IN_MILISECOND);
                        } catch (InterruptedException e) {
                            logger.info(String.format("Manager %s waiting has been interrupted.", Integer.toString(this.id)));
                        }
                        this.threadsWaiting.decrementAndGet();
                    }
                }
            }

            if (this.solution != null) {
                logger.info(String.format("Manager [%s] found a solution.", this.id));
                this.solution.printResult();
            }
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            logger.error(String.format("Manager [%s] exit with exception %s", this.id, e.toString()));
            synchronized (this.threadsWaiting) {
                this.threadsWaiting.notifyAll();
            }
        }
    }
}
