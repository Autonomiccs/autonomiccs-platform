package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForSmallHosts.ClusterUpwardComparator;

@RunWith(MockitoJUnitRunner.class)
public class ClusterUpwardComparatorTest {

    private ClusterUpwardComparator clusterComparator = new ClusterUpwardComparator();

    @Test
    public void compareTestH1BiggerThanH2() {
        ClusterResources c1 = getScoredClusterMock(1d);
        ClusterResources c2 = getScoredClusterMock(0d);
        int result = clusterComparator.compare(c1, c2);

        verifyGetScoreExecution(c1, c2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void compareTestH1SmallerThanH2() {
        ClusterResources c1 = getScoredClusterMock(0d);
        ClusterResources c2 = getScoredClusterMock(1d);
        int result = clusterComparator.compare(c1, c2);

        verifyGetScoreExecution(c1, c2);
        Assert.assertEquals(-1, result);
    }

    private void verifyGetScoreExecution(ClusterResources c1, ClusterResources c2) {
        Mockito.verify(c1).getScore();
        Mockito.verify(c2).getScore();
    }

    private ClusterResources getScoredClusterMock(double score) {
        ClusterResources c = Mockito.mock(ClusterResources.class);
        Mockito.when(c.getScore()).thenReturn(score);
        return c;
    }

}
