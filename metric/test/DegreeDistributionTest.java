package grakn.benchmark.metric.test;

import grakn.benchmark.metric.DegreeDistribution;
import grakn.benchmark.metric.GraphProperties;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

public class DegreeDistributionTest {

    @Test
    public void degreeStreamToDiscreteDistribution() {

        GraphProperties mockProperties = Mockito.mock(GraphProperties.class);

        long[] vertexDegrees = new long[] {
                0, 0, 0,
                1, 1,
                2, 2,
                3, 3,
                4,
                10
        };
        when(mockProperties.vertexDegree(2)).thenReturn(Arrays.stream(vertexDegrees).boxed().collect(Collectors.toList()));

        double[] percentiles = new double[] {0, 20, 50, 80, 100};
        long[] correctDegreeDistribution = new long[] {0, 0, 2, 3, 10};

        long[] computedDegreeDistribution = DegreeDistribution.binaryEdgeDegreeDistribution(mockProperties, percentiles);
        assertArrayEquals(correctDegreeDistribution, computedDegreeDistribution);
    }

    @Test
    public void degreeStreamToNormalizedDistribution() {

        GraphProperties mockProperties = Mockito.mock(GraphProperties.class);

        long[] vertexDegrees = new long[] {
                0, 0, 0,
                1, 1,
                2, 2,
                3, 3,
                4,
                10
        };
        when(mockProperties.vertexDegree(2)).thenReturn(Arrays.stream(vertexDegrees).boxed().collect(Collectors.toList()));
        when(mockProperties.maxAllowedDegree(2)).thenReturn(11l);

        double[] percentiles = new double[] {0, 20, 50, 80, 100};
        double[] correctNormalizedDegreeDistribution = new double[] {0, 0, 2/11.0, 3/11.0, 10/11.0};

        double[] computedDegreeDistribution = DegreeDistribution.normalizedBinaryEdgeDegreeDistribution(mockProperties, percentiles);
        double allowedDeviation = 0.000001;
        assertArrayEquals(correctNormalizedDegreeDistribution, computedDegreeDistribution, allowedDeviation);
    }
}
