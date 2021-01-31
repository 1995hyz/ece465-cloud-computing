import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Solver implements Runnable {
    private int ID;
    private static Logger logger = LogManager.getLogger(Solver.class);
    protected BlockingQueue<int[][]> fringe;
    protected BlockingQueue<int[][]> explored_grids;
    private int[][] grid;
    private int dim = 9;




    public Solver(int ID, BlockingQueue<int[][]> fringe, BlockingQueue<int[][]> explored_grids, int[][] grid) {
        this.ID = ID;
        this.fringe = fringe;
        this.explored_grids = explored_grids;
        this.grid = grid;
    }

    public void run() {
//        Random random = new Random();
//        while (true){
//            // think
//            try {
//                Thread.sleep(random.nextInt(30000));
//            } catch (InterruptedException e) {
//                logger.info(String.format("Solver %s thinking has been interrupted.", Integer.toString(this.ID)));
//            }
//            if (!leftFork.isTaken() && !rightFork.isTaken()){
//                leftFork.take();
//                rightFork.take();
//                System.out.println("Solver " + ID + " has picked up forks " + leftFork.getID() + " and " + rightFork.getID());
//                // eat
//                try {
//                    Thread.sleep(random.nextInt(10000));
//                } catch (InterruptedException e) {
//                    logger.info(String.format("Solver %s eating has been interrupted.", Integer.toString(this.ID)));
//                }
//                leftFork.put();
//                rightFork.put();
//                System.out.println("Solver " + ID + " has put down forks " + leftFork.getID() + " and " + rightFork.getID());
//
//
//            }
//        }
    }
//    public int[][] reduce(Pair<Integer, Integer> index, int value, int[][] grid){
////        Integer row = index.getRow();
////        Integer col = index.getCol();
////        // ...
////        return grid;
//    }
//    public Pair<Pair<Integer, Integer>, Integer> getMinPossibleValues(int[][] grid_unsolved){
//
//    }

}
