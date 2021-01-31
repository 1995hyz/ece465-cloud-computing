import java.util.List;
import java.util.Map;

public class Grid {
    private final int dim;
    private int[][] grid;
    private Map<String, List<Integer>> possibleValues;

    public Grid (int dim){
        this.dim = dim;
        grid = new int[dim][dim];

    }

    public void loadGrid(String filePath) {

        // 0,1,0
        // 2,0,0
        // 0,0,0
        // Load file

        // Parse file
        // Fill grid

    }

    public int getGridCell(int row, int col) {
        return grid[row][col];
    }

    public void setGridCell(int row, int col, int val) {
        this.grid[row][col] = val;
    }

    public void getGridColumn(int colIndex) {

    }

    public void reduce(int rowIndex, int colIndex, int val) {
        int[] row = this.grid[rowIndex];

    }

}
