import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Grid {
    private final int dim;
    private int[][] grid;
    private Map<String, List<Integer>> possibleValues;

    public Grid (int dim){
        this.dim = dim;
//        grid = new int[dim][dim];

    }

    public void loadGrid(String filePath) throws IOException {
        Scanner sc = new Scanner(new BufferedReader(new FileReader(filePath)));
        int [][] newGrid = new int[dim][dim];
        while(sc.hasNextLine()) {
            for (int i=0; i<newGrid.length; i++) {
                String[] line = sc.nextLine().trim().split(",");
                for (int j=0; j<line.length; j++) {
                    newGrid[i][j] = Integer.parseInt(line[j]);
                    //System.out.println(line[j]);
                }
            }
        }
        this.grid = newGrid;
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
