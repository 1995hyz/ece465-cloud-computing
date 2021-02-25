import com.fasterxml.jackson.databind.ObjectMapper;
import multi_node_multi_thread.Grid;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public class GridTest {

    @Test
    public void test_reduceCellPossibleValues_shouldSucceed() throws Throwable {
        int subDim = 2;
        int dim = subDim * subDim;
        int testRowIndex = 1;
        int testColumnIndex = 1;
        int testValue = 1;
        List<Integer> initialPossibleValues = IntStream.range(1, dim+1)
                .boxed().collect(Collectors.toList());
        List<Integer> removedPossibleValues = new ArrayList<>(initialPossibleValues);
        removedPossibleValues.remove(Integer.valueOf(testValue));
        String key = Integer.valueOf(testRowIndex).toString() + Integer.valueOf(testColumnIndex).toString();
        Grid grid = new Grid(subDim);
        grid.reduce(testRowIndex, testColumnIndex, testValue);
        Map<String, List<Integer>> rowPossibleValues = grid.getGridRowPossibleValues(testRowIndex);
        rowPossibleValues.entrySet().stream().filter(e -> !e.getKey().equals(key)).forEach(e -> {
            Assert.assertEquals(String.format("Position %s has incorrect possible values %s", e.getKey(),
                    e.getValue()), e.getValue(), removedPossibleValues);
        });
        Map<String, List<Integer>> colPossibleValues = grid.getGridColumnPossibleValues(testColumnIndex);
        colPossibleValues.entrySet().stream().filter(e -> !e.getKey().equals(key)).forEach(e -> {
            Assert.assertEquals(String.format("Position %s has incorrect possible values %s", e.getKey(),
                    e.getValue()), e.getValue(), removedPossibleValues);
        });
    }

    @Test
    public void test_validateGrid_shouldSucceed() throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/possibleCellValues.json");
        Map<String, List<Integer>> possibleValues = mapper.readValue(file, Map.class);
        int subDimension = 2;
        Grid grid = new Grid(subDimension);
        Grid gridMocked = Mockito.spy(grid);
        Mockito.when(gridMocked.getGridRowPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            return getGridRowPossibleValues(rowIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.when(gridMocked.getGridColumnPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int columnIndex = (int) invocationOnMock.getArguments()[0];
            return getGridColumnPossibleValues(columnIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.when(gridMocked.getSubGridPossibleValues(Mockito.anyInt(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            int columnIndex = (int) invocationOnMock.getArguments()[1];
            return getSubGridPossibleValues(rowIndex, columnIndex, subDimension, possibleValues);
        });

        Assert.assertTrue("Fail to validate grid", gridMocked.validateGrid());
    }

    @Test
    public void test_validateSubGrid_shouldFail() throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/possibleCellValues_subGrid_fail.json");
        Map<String, List<Integer>> possibleValues = mapper.readValue(file, Map.class);
        int subDimension = 2;
        Grid grid = new Grid(subDimension);
        Grid gridMocked = Mockito.spy(grid);
        Mockito.when(gridMocked.getGridRowPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            return getGridRowPossibleValues(rowIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.when(gridMocked.getGridColumnPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int columnIndex = (int) invocationOnMock.getArguments()[0];
            return getGridColumnPossibleValues(columnIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.when(gridMocked.getSubGridPossibleValues(Mockito.anyInt(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            int columnIndex = (int) invocationOnMock.getArguments()[1];
            return getSubGridPossibleValues(rowIndex, columnIndex, subDimension, possibleValues);
        });

        Assert.assertFalse("Fail to validate grid", gridMocked.validateGrid());
    }

    @Test
    public void test_validate_sameRow_shouldFail() throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/possibleCellValues_sameRow_fail.json");
        Map<String, List<Integer>> possibleValues = mapper.readValue(file, Map.class);
        int subDimension = 2;
        Grid grid = new Grid(subDimension);
        Grid gridMocked = Mockito.spy(grid);
        Mockito.when(gridMocked.getGridRowPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            return getGridRowPossibleValues(rowIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.lenient().when(gridMocked.getGridColumnPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int columnIndex = (int) invocationOnMock.getArguments()[0];
            return getGridColumnPossibleValues(columnIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.lenient().when(gridMocked.getSubGridPossibleValues(Mockito.anyInt(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            int columnIndex = (int) invocationOnMock.getArguments()[1];
            return getSubGridPossibleValues(rowIndex, columnIndex, subDimension, possibleValues);
        });

        Assert.assertFalse("Fail to validate grid", gridMocked.validateGrid());
    }

    @Test
    public void test_validate_sameColumn_shouldFail() throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/test/resources/possibleCellValues_sameColumn_fail.json");
        Map<String, List<Integer>> possibleValues = mapper.readValue(file, Map.class);
        int subDimension = 2;
        Grid grid = new Grid(subDimension);
        Grid gridMocked = Mockito.spy(grid);
        Mockito.when(gridMocked.getGridRowPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            return getGridRowPossibleValues(rowIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.when(gridMocked.getGridColumnPossibleValues(Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int columnIndex = (int) invocationOnMock.getArguments()[0];
            return getGridColumnPossibleValues(columnIndex, subDimension * subDimension, possibleValues);
        });
        Mockito.lenient().when(gridMocked.getSubGridPossibleValues(Mockito.anyInt(), Mockito.anyInt())).thenAnswer(invocationOnMock -> {
            int rowIndex = (int) invocationOnMock.getArguments()[0];
            int columnIndex = (int) invocationOnMock.getArguments()[1];
            return getSubGridPossibleValues(rowIndex, columnIndex, subDimension, possibleValues);
        });

        Assert.assertFalse("Fail to validate grid", gridMocked.validateGrid());
    }

    private Map<String, List<Integer>> getGridRowPossibleValues(int rowIndex, int gridDimension,
                                                                Map<String, List<Integer>> possibleValues) {
        Map<String, List<Integer>> rowPossibleValues = new HashMap<>();
        for (int i=0; i<gridDimension; i++) {
            String key = Integer.valueOf(rowIndex).toString() + Integer.valueOf(i).toString();
            rowPossibleValues.put(key, possibleValues.get(key));
        }
        return rowPossibleValues;
    }

    private Map<String, List<Integer>> getGridColumnPossibleValues(int colIndex, int gridDimension,
                                                                  Map<String, List<Integer>> possibleValues) {
        Map<String, List<Integer>> colPossibleValues = new HashMap<>();
        for (int i=0; i<gridDimension; i++) {
            String key = Integer.valueOf(i).toString() + Integer.valueOf(colIndex).toString();
            colPossibleValues.put(key, possibleValues.get(key));
        }
        return colPossibleValues;
    }

    private Map<String, List<Integer>> getSubGridPossibleValues(int rowIndex, int colIndex, int subGridDimension,
                                                               Map<String, List<Integer>> possibleValues) {
        Map<String, List<Integer>> subGridPossibleValues = new HashMap<>();
        int rowSubGridPosition = rowIndex * subGridDimension;
        int colSubGridPosition = colIndex * subGridDimension;
        for(int i=0; i<subGridDimension; i++) {
            for(int j=0; j<subGridDimension; j++) {
                String key = Integer.valueOf(rowSubGridPosition+i).toString() + Integer.valueOf(colSubGridPosition+j).toString();
                subGridPossibleValues.put(key, possibleValues.get(key));
            }
        }
        return subGridPossibleValues;
    }

}
