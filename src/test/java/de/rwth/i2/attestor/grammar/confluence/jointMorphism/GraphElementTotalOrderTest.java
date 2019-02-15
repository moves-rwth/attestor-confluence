package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * Verifies that GraphElement realizes a valid total order.
 * Checks the 'equals', 'hashCode' and 'compareTo' methods
 */
@RunWith(Parameterized.class)
public class GraphElementTotalOrderTest {

    @Parameters(name = "{index}: ({0}, {1}) ({2}, {3}) -> {4}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // id1, lab1, id2, lab2, result
            {0, null, 0, null, 0},
            {5, null, 16, null, -1},
            {0, "a", 0, "b", -1},
            {0, null, 0, "a", -1},
            {0, "b", 2, "a", -1},
            {5, null, 1, "b", 1},
            {5, "test", 5, "test", 0},
        });
    }

    @Parameter
    public int privateId1;
    @Parameter(1)
    public String label1;
    @Parameter(2)
    public int privateId2;
    @Parameter(3)
    public String label2;
    @Parameter(4)
    public int compareResult;

    @Test
    public void testEdgeGraphElementOrder() {
        GraphElement edgeElement1 = new EdgeGraphElement(privateId1, label1);
        GraphElement edgeElement2 = new EdgeGraphElement(privateId2, label2);
        checkComparator(edgeElement1, edgeElement2, compareResult);
        checkComparator(edgeElement2, edgeElement1, -1 * compareResult);
    }

    @Test
    public void testNodeGraphElementOrder() {
        // Only test this if there are no labels set
        if (label1 == null && label2 == null) {
            GraphElement edgeElement1 = new NodeGraphElement(privateId1);
            GraphElement edgeElement2 = new NodeGraphElement(privateId2);
            checkComparator(edgeElement1, edgeElement2, compareResult);
            checkComparator(edgeElement2, edgeElement1, -1 * compareResult);
        }
    }

    private void checkComparator(GraphElement elem1, GraphElement elem2, int expectedResult) {
        int result = elem1.compareTo(elem2);
        // Convert to (-1, 0, 1) for easy equality check
        if (result < 0) {
            result = -1;
        } else if (result > 0) {
            result = 1;
        }
        assertEquals("compareTo() result is wrong", result, expectedResult);
        assertEquals("equals() result is wrong", expectedResult == 0, elem1.equals(elem2));
        if (expectedResult == 0) {
            // Check that hashCode() result match for equal entries
            String msg = "Elements are equal, but their hash does not match";
            assertEquals(msg, elem1.hashCode(), elem2.hashCode());
        }
    }

}
