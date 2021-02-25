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
import java.util.concurrent.BlockingQueue;

public class Manager implements Runnable {

    private static final Logger logger = LogManager.getLogger(Manager.class);

    private int id;
    private int portNumber;
    private Grid initialGrid;
    private BlockingQueue<Grid> exploredGrid;

    public Manager(int id, int portNumber, Grid grid, BlockingQueue<Grid> exploredGrid) throws InterruptedException {
        this.id = id;
        this.portNumber = portNumber;
        this.exploredGrid = exploredGrid;
        this.initialGrid = grid;
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
                Grid proposedGrid = (Grid) objectInputStream.readObject();
                if(proposedGrid.isFilled()) {
                    break;
                }
                if (exploredGrid.contains(proposedGrid)) {
                    objectOutputStream.writeObject(Boolean.TRUE);
                } else {
                    objectOutputStream.writeObject(Boolean.FALSE);
                    try {
                        if (exploredGrid.size() < Constants.EXPLORED_QUEUE_MAX_SIZE) {
                            exploredGrid.put(proposedGrid);
                        } else {
                            exploredGrid.poll();
                            exploredGrid.put(proposedGrid);
                        }
                    } catch (InterruptedException e) {
                        logger.error(String.format("Manager [%s] fails to add explored grid on to the explored-grid queue." +
                                        " ex, %s ", id, e.toString()));
                    }


                }
                objectOutputStream.reset();
            }

        } catch (IOException | ClassNotFoundException e) {
            logger.error(String.format("Manager [%s] exit with exception %s", id, e.toString()));
        }
    }
}
