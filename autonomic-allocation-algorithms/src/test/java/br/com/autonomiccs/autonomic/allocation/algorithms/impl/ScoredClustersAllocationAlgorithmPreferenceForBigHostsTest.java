package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

@RunWith(MockitoJUnitRunner.class)
public class ScoredClustersAllocationAlgorithmPreferenceForBigHostsTest extends ScoredClustersAllocationAlgorithm {

    private ScoredClustersAllocationAlgorithmPreferenceForBigHosts spyAlgorithm;
    private List<HostResources> hosts;

    @Before
    public void setup() {
        spyAlgorithm = PowerMockito.spy(new ScoredClustersAllocationAlgorithmPreferenceForBigHosts());
        hosts = new ArrayList<HostResources>();
    }

    @Test
    public void sortHostsTest() {
        Mockito.doNothing().when(spyAlgorithm).sortHostsDownwardScore(hosts);
        spyAlgorithm.sortHosts(hosts);

        Mockito.verify(spyAlgorithm).sortHostsDownwardScore(hosts);
    }

    @Test
    public void sortHostsDownwardScoreTest() {
        List<HostResources> hosts = createHostsWithScore();

        spyAlgorithm.sortHostsDownwardScore(hosts);

        Assert.assertEquals(4.0, hosts.get(0).getScore(), 0);
        Assert.assertEquals(3.0, hosts.get(1).getScore(), 0);
        Assert.assertEquals(2.0, hosts.get(2).getScore(), 0);
        Assert.assertEquals(1.0, hosts.get(3).getScore(), 0);
    }

}
