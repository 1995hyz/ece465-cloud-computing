import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GridTest {

    @Test
    public void test_reduceCellPossibleValues_ShouldSucceed() throws Throwable {
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
}
