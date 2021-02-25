package multi_node_multi_thread;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Grid implements Serializable {

    private static final Logger logger = LogManager.getLogger(Grid.class);

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
    public Map<String, List<Integer>> getPossibleValues(){
        return this.possibleValues;
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


    public Map<String, List<Integer>> getSubGridPossibleValues(int rowIndex, int colIndex) {
        if (rowIndex >= this.subDim || colIndex >= this.subDim) {
            logger.error(String.format("multi_node_multi_thread.model.Grid::getSubGridPossibleValues:Cannot get sub-grid with row '%s' and column '%s'" +
                    " when sub-grid dimension is '%s'", rowIndex, colIndex, this.subDim));
            throw new IllegalArgumentException("Sub-grid index exceeds sub-grid dimension");
        }
        Map<String, List<Integer>> subGridPossibleValues = new HashMap<>();
        int rowSubGridPosition = rowIndex  * this.subDim;
        int colSubGridPosition = colIndex * this.subDim;
        for(int i=0; i<this.subDim; i++) {
            for(int j=0; j<this.subDim; j++) {
                String key = Integer.valueOf(rowSubGridPosition+i).toString() + Integer.valueOf(colSubGridPosition+j).toString();
                subGridPossibleValues.put(key, this.possibleValues.get(key));
            }
        }
        return subGridPossibleValues;
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
        Integer removeValue = value;
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
                String key = Integer.valueOf(i).toString() + Integer.valueOf(colIndex).toString();
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
        String key = Integer.valueOf(rowIndex).toString() + Integer.valueOf(colIndex).toString();
        this.possibleValues.replace(key, new ArrayList<>(Collections.singletonList(value)));
        this.grid[rowIndex][colIndex] = value;
    }

    private void fillPossibleValuesOfGrid() {
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j<this.dim; j++) {
                if (this.grid[i][j] != 0) {
//                    logger.debug(String.format("Filling position %s", Integer.valueOf(i).toString() + Integer.valueOf(j).toString()));
                    setGridCell(i, j, this.grid[i][j]);
                }
            }
        }
    }

    /**
     * Validate the grid by scanning each row, each column and each sub-grid to make sure that, for the cells that only
     * have one unique solution, the solution is also unique among others.
     */
    public boolean validateGrid() {
        if (possibleValues.values().stream().anyMatch(List::isEmpty)) {
            return false;
        }
        for (int i=0; i<this.dim; i++) {
            Map<String, List<Integer>> row = this.getGridRowPossibleValues(i);
            // List all cell values that only have one possible value
            List<Integer> rowUniqueList = row.values().stream()
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .collect(Collectors.toList());
            Set<Integer> rowUniqueSet = new HashSet<>(rowUniqueList);
            // If the set size if smaller than the list size, this means the list contains value that is duplicated
            if (rowUniqueList.size() > rowUniqueSet.size()) {
                return false;
            }
        }
        for (int i=0; i<this.dim; i++) {
            Map<String, List<Integer>> column = this.getGridColumnPossibleValues(i);
            List<Integer> columnUniqueList = column.values().stream()
                    .filter(e -> e.size() == 1)
                    .map(e -> e.get(0))
                    .collect(Collectors.toList());
            Set<Integer> columnUniqueSet = new HashSet<>(columnUniqueList);
            if (columnUniqueList.size() > columnUniqueSet.size()) {
                return false;
            }
        }
        for (int i=0; i<this.subDim; i++) {
            for(int j=0; j<this.subDim; j++) {
                Map<String, List<Integer>> subGrid = this.getSubGridPossibleValues(i, j);
                List<Integer> subGridUniqueList = subGrid.values().stream()
                        .filter(e -> e.size() == 1)
                        .map(e -> e.get(0))
                        .collect(Collectors.toList());
                Set<Integer> subGridUniqueSet = new HashSet<>(subGridUniqueList);
                if (subGridUniqueList.size() > subGridUniqueSet.size()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Find the next cell candidate to solve the grid. The index of the cell which has the minimum size of possible
     * values will be returned
     * @return Index string of the candidate cell. The string is in two-digit format. Fist digit is the row index and
     * second digit is the column index
     */
    public String findNextIndexToSolveGrid() {
        int minimumPossibleValue = 9;
        String minimumPossibleValueKey = null;
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j<this.dim; j++) {
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                int possibleValueSize = this.possibleValues.get(key).size();
                if (possibleValueSize > 1 && possibleValueSize <= minimumPossibleValue) {
                    minimumPossibleValueKey = key;
                    minimumPossibleValue = possibleValueSize;
                }

            }
        }
//        logger.debug(String.format("Next index to reduce: %s", minimumPossibleValueKey));
//        logger.debug(String.format("Number of possible values: %d", minimumPossibleValue));
        return minimumPossibleValueKey;
    }

    public String findFirstIndexToSolveGrid() {
        int maximumPossibleValue = 2;
        String maximumPossibleValueKey = null;
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j<this.dim; j++) {
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                int possibleValueSize = this.possibleValues.get(key).size();
                if (possibleValueSize >= maximumPossibleValue) {
                    maximumPossibleValueKey = key;
                    maximumPossibleValue = possibleValueSize;
                }
            }
        }
        return maximumPossibleValueKey;
    }

    @Override
    public boolean equals(Object o) {
        Grid grid2 = (Grid) o;
        for(int i = 0; i < this.dim; i++){
            for(int j = 0; j < this.dim; j++){
                if(this.getGridCell(i,j) != grid2.getGridCell(i,j)){
                    return false;
                }
            }
        }
        return true;
    }

    public void printResult(){
        for(int i = 0; i < this.dim; i++){
            for(int j = 0; j < this.dim; j++){
                if(j == this.dim -1){
                    System.out.println(this.grid[i][j]);
                }
                else{
                    System.out.print(this.grid[i][j] + ",");
                }
            }
        }
    }

    public boolean isSolution(){
        for(int i = 0; i < this.dim; i++){
            for(int j = 0; j < this.dim; j++){
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                if(this.possibleValues.get(key).size() != 1){
                    return false;
                }
            }
        }
        for (int i=0; i<this.dim; i++) {
            for (int j=0; j < this.dim; j++ ) {
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                if (this.possibleValues.get(key).size() == 1) {
                    this.grid[i][j] = this.possibleValues.get(key).get(0);
                }
            }
        }

        return true;
    }

    public Grid copy() {
        return SerializationUtils.clone(this);
    }

    public boolean canPrune(){
        // check if there are any cells with no possible values
        for (int i = 0; i < this.dim; i++){
            for (int j = 0; j < this.dim; j++){
                String key = Integer.valueOf(i).toString() + Integer.valueOf(j).toString();
                if (this.possibleValues.get(key).size() == 0) {
                    logger.debug("Found grid in fringe that had cell(s) with no possible value.");
                    return true;
                }
            }
        }
        // check if there is a number that doesn't occur in the possible values of any row
        for (int i = 0; i < this.dim; i++) {
            Map<String, List<Integer>> rowPossibleValues = this.getGridRowPossibleValues(i);
            Set<Integer> combinedRowValues = new HashSet<>();
            for (Map.Entry<String, List<Integer>> elem : rowPossibleValues.entrySet()){
                List<Integer> values = elem.getValue();
                combinedRowValues.addAll(values);
            }
            if(combinedRowValues.size()!=9){
                logger.debug("Found grid in fringe with row with incomplete set of possible values.");
                return true;
            }
        }

        // check if there is a number that doesn't occur in the possible values of any column
        for (int i = 0; i < this.dim; i++) {
            Map<String, List<Integer>> colPossibleValues = this.getGridColumnPossibleValues(i);
            Set<Integer> combinedColValues = new HashSet<>();
            for (Map.Entry<String, List<Integer>> elem : colPossibleValues.entrySet()){
                List<Integer> values = elem.getValue();
                combinedColValues.addAll(values);
            }
            if (combinedColValues.size() !=9 ){
                logger.debug("Found grid in fringe with column with incomplete set of possible values.");
                return true;
            }
        }

        // check if there is a number that doesn't occur in the possible values of any subgrid
        for (int i = 0; i < this.subDim; i++){
            for (int j = 0; j < this.subDim; j++){
                Map<String, List<Integer>> subGridPossibleValues = this.getSubGridPossibleValues(i,j);
                Set<Integer> combinedSubGridValues = new HashSet<>();
                for (Map.Entry<String, List<Integer>> elem : subGridPossibleValues.entrySet()){
                    List<Integer> values = elem.getValue();
                    combinedSubGridValues.addAll(values);
                }
                if(combinedSubGridValues.size()!=9){
                    logger.debug("Found grid in fringe with subgrid with incomplete set of possible values.");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFilled() {
        for (int i = 0; i < this.dim; i++){
            for (int j = 0; j < this.dim; j++){
                if (this.grid[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
