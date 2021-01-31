import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Grid {

    private static Logger logger = LogManager.getLogger(Grid.class);

    private final int dim;
    private final int subDim;
    private Integer[][] grid;
    private Map<String, List<Integer>> possibleValues;

    public Grid (int subDimension){
        this.subDim = subDimension;
        this.dim = subDimension * subDimension;
        IntStream intStream = IntStream.range(1, dim+1);
        List<Integer> initialPossibleValues = intStream.boxed().collect(Collectors.toList());
        grid = new Integer[dim][dim];
        possibleValues = new HashMap<>();
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j<this.dim; j++) {
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                possibleValues.put(key, new ArrayList<>(initialPossibleValues));
            }
        }
    }

    public void loadGrid(String filePath) throws IOException {
        Scanner sc = new Scanner(new BufferedReader(new FileReader(filePath)));
        while(sc.hasNextLine()) {
            for (int i=0; i<this.dim && sc.hasNextLine(); i++) {
                String[] line = sc.nextLine().trim().split(",");
                for (int j=0; j<line.length; j++) {
                    this.grid[i][j] = Integer.parseInt(line[j]);
                }
            }
        }
        fillPossibleValuesOfGrid();
    }

    public int getGridCell(int row, int col) {
        return grid[row][col];
    }

    public void setGridCell(int row, int col, int val) {
        this.grid[row][col] = val;
        String key = Integer.valueOf(row).toString() + Integer.valueOf(col).toString();
        this.possibleValues.replace(key, new ArrayList<>(Collections.singletonList(val)));
        reduce(row, col, val);
    }

    public Map<String, List<Integer>> getGridRowPossibleValues(int rowIndex) {
        Map<String, List<Integer>> rowPossibleValues = new HashMap<>();
        for (int i=0; i<this.dim; i++) {
            String key = Integer.valueOf(rowIndex).toString() + Integer.valueOf(i).toString();
            rowPossibleValues.put(key, this.possibleValues.get(key));
        }
        return rowPossibleValues;
    }

    public Map<String, List<Integer>> getGridColumnPossibleValues(int colIndex) {
        Map<String, List<Integer>> colPossibleValues = new HashMap<>();
        for (int i=0; i<this.dim; i++) {
            String key = Integer.valueOf(i).toString() + Integer.valueOf(colIndex).toString();
            colPossibleValues.put(key, this.possibleValues.get(key));
        }
        return colPossibleValues;
    }

    public Integer[] getGridColumn(int colIndex) {
        Integer[] col = new Integer[this.dim];
        for(int i=0; i< this.dim; i++) {
            col[i] = this.getGridCell(i, colIndex);
        }
        return col;
    }

    /**
     * Take a value and the indexes of the value and reduce the possible values of the cells that are
     * in the same row, column and sub-grid.
     * @param rowIndex Row index of the value, from 0 to the dimension of the grid
     * @param colIndex Column index of the value, from 0 to the dimension of the grid
     * @param value Cell value that should be removed from possible values lists
     */
    public void reduce(int rowIndex, int colIndex, int value) {
        // The Integer boxing is needed to work with list.remove() to remove by value
        Integer removeValue = Integer.valueOf(value);
        // Remove the value from the possible list of the same row
        for(int i=0; i<this.dim; i++) {
            if (i != colIndex) {
                String key = Integer.valueOf(rowIndex).toString() + Integer.valueOf(i).toString();
                this.possibleValues.get(key).remove(removeValue);
            }
        }
        // Remove the value from the possible list of the same column
        for(int i=0; i<this.dim; i++) {
            if( i != rowIndex) {
                String key = Integer.valueOf(i).toString() + Integer.valueOf(rowIndex).toString();
                this.possibleValues.get(key).remove(removeValue);
            }
        }
        // Remove the value from the possible list of the cells in the same sub-grid
        int rowSubGridPosition = (rowIndex / this.subDim) * this.subDim;
        int colSubGridPosition = (colIndex / this.subDim) * this.subDim;
        for(int i=0; i<this.subDim; i++) {
            for(int j=0; j<this.subDim; j++) {
                if ( (rowSubGridPosition+i)!= rowIndex || (colSubGridPosition+j)!=colIndex ) {
                    String key = Integer.valueOf(rowSubGridPosition+i).toString() + Integer.valueOf(colSubGridPosition+j).toString();
                    this.possibleValues.get(key).remove(removeValue);
                }
            }
        }
    }

    private void fillPossibleValuesOfGrid() {
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j<this.dim; j++) {
                System.out.println(i + " " + j + " " + dim);
                if (this.grid[i][j] != 0) {
                    logger.debug(String.format("Filling position %s", Integer.valueOf(i).toString() + Integer.valueOf(j).toString()));
                    setGridCell(i, j, this.grid[i][j]);
                }
            }
        }
    }
}
