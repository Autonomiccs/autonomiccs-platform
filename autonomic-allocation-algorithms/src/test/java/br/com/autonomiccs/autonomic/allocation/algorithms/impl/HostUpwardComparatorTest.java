package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForSmallHosts.HostUpwardComparator;

@RunWith(MockitoJUnitRunner.class)
public class HostUpwardComparatorTest {

    private HostUpwardComparator hostComparator = new HostUpwardComparator();

    @Test
    public void compareTestH1BiggerThanH2() {
        HostResources h1 = getScoredHostMock(1d);
        HostResources h2 = getScoredHostMock(0d);
        int result = hostComparator.compare(h1, h2);

        verifyGetScoreExecution(h1, h2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void compareTestH1SmallerThanH2() {
        HostResources h1 = getScoredHostMock(0d);
        HostResources h2 = getScoredHostMock(1d);
        int result = hostComparator.compare(h1, h2);

        verifyGetScoreExecution(h1, h2);
        Assert.assertEquals(-1, result);
    }

    private void verifyGetScoreExecution(HostResources h1, HostResources h2) {
        Mockito.verify(h1).getScore();
        Mockito.verify(h2).getScore();
    }

    private HostResources getScoredHostMock(double score) {
        HostResources h = Mockito.mock(HostResources.class);
        Mockito.when(h.getScore()).thenReturn(score);
        return h;
    }

}
