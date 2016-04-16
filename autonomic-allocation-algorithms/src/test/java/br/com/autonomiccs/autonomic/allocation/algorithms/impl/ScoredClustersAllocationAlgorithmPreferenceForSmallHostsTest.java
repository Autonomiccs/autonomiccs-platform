package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

@RunWith(MockitoJUnitRunner.class)
public class ScoredClustersAllocationAlgorithmPreferenceForSmallHostsTest extends ScoredClustersAllocationAlgorithm {

    private ScoredClustersAllocationAlgorithmPreferenceForSmallHosts spyAlgorithm;

    @Before
    public void setup() {
        spyAlgorithm = Mockito.spy(new ScoredClustersAllocationAlgorithmPreferenceForSmallHosts());
    }

    @Test
    public void extendsAllocationAlgorithmBase() {
        Assert.assertTrue(AllocationAlgorithmBase.class.isAssignableFrom(ScoredClustersAllocationAlgorithmPreferenceForSmallHosts.class));
    }

    @Test
    public void rankClustersToAllocationTest() {
        List<ClusterResourcesAvailableToStart> clustersAvailableToStart = createClustersWithScore();

        Mockito.doReturn(clustersAvailableToStart).when(spyAlgorithm).cloneListOfClusters(clustersAvailableToStart);
        Mockito.doNothing().when(spyAlgorithm).setClustersScore(clustersAvailableToStart);
        Mockito.doNothing().when(spyAlgorithm).sortClustersDownwardScore(clustersAvailableToStart);

        spyAlgorithm.rankClustersToAllocation(clustersAvailableToStart);

        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm).cloneListOfClusters(clustersAvailableToStart);
        inOrder.verify(spyAlgorithm).setClustersScore(clustersAvailableToStart);
        inOrder.verify(spyAlgorithm).sortClustersDownwardScore(clustersAvailableToStart);
    }

    @Test
    public void setClustersScoreTest() {
        List<ClusterResourcesAvailableToStart> clustersAvailableToStart = new ArrayList<ClusterResourcesAvailableToStart>();
        ClusterResourcesAvailableToStart spyCluster = Mockito.spy(new ClusterResourcesAvailableToStart(0, null, 0, 0, 0, null));
        clustersAvailableToStart.add(spyCluster);

        Mockito.doReturn(0l).when(spyAlgorithm).calculateClusterScore(Mockito.any(ClusterResourcesAvailableToStart.class));
        Mockito.doNothing().when(spyCluster).setScore(Mockito.anyDouble());

        spyAlgorithm.setClustersScore(clustersAvailableToStart);

        InOrder inOrder = Mockito.inOrder(spyAlgorithm, spyCluster);
        inOrder.verify(spyAlgorithm, Mockito.times(clustersAvailableToStart.size())).calculateClusterScore(Mockito.any(ClusterResourcesAvailableToStart.class));
        inOrder.verify(spyCluster, Mockito.times(clustersAvailableToStart.size())).setScore(Mockito.anyDouble());
    }

    @Test
    public void setEachHostScoreTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        HostResources spyHost = Mockito.spy(new HostResources());
        hosts.add(spyHost);

        Mockito.doReturn(0d).when(spyAlgorithm).calculateHostScore(Mockito.any(HostResources.class));
        Mockito.doNothing().when(spyHost).setScore(Mockito.anyDouble());

        spyAlgorithm.setEachHostScore(hosts);

        InOrder inOrder = Mockito.inOrder(spyAlgorithm, spyHost);
        inOrder.verify(spyAlgorithm, Mockito.times(hosts.size())).calculateHostScore(Mockito.any(HostResources.class));
        inOrder.verify(spyHost, Mockito.times(hosts.size())).setScore(Mockito.anyDouble());
    }

    @Test
    public void calculateClusterScoreTest() {
        ClusterResources cluster = Mockito.mock(ClusterResources.class);
        Mockito.when(cluster.getCpus()).thenReturn(2);
        Mockito.when(cluster.getCpuSpeed()).thenReturn(2000l);
        Mockito.when(cluster.getMemoryInBytes()).thenReturn(4000l);

        long restult = spyAlgorithm.calculateClusterScore(cluster);
        Assert.assertEquals(16000000, restult, 0l);
    }

    @Test
    public void sortClustersDownwardScoreTest() {
        List<ClusterResourcesAvailableToStart> clusters = createClustersWithScore();

        spyAlgorithm.sortClustersDownwardScore(clusters);

        Assert.assertEquals(4.0, clusters.get(0).getScore(), 0);
        Assert.assertEquals(3.0, clusters.get(1).getScore(), 0);
        Assert.assertEquals(2.0, clusters.get(2).getScore(), 0);
        Assert.assertEquals(1.0, clusters.get(3).getScore(), 0);
    }

    @Test
    public void rankHostsToStartTest() {
        List<HostResources> hosts = createHostsWithScore();

        Mockito.doReturn(hosts).when(spyAlgorithm).cloneListOfHosts(hosts);
        Mockito.doReturn(hosts).when(spyAlgorithm).setEachHostScore(hosts);
        Mockito.doNothing().when(spyAlgorithm).sortHosts(hosts);

        spyAlgorithm.rankHostsToStart(hosts);

        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm).cloneListOfHosts(hosts);
        inOrder.verify(spyAlgorithm).setEachHostScore(hosts);
        inOrder.verify(spyAlgorithm).sortHosts(hosts);
    }

    @Test
    public void sortHostsTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        Mockito.doNothing().when(spyAlgorithm).sortHostsUpwardScore(hosts);
        spyAlgorithm.sortHosts(hosts);

        Mockito.verify(spyAlgorithm).sortHostsUpwardScore(hosts);
    }


    @Test
    public void calculateHostScoreTest() {
        HostResources hostToStart = Mockito.mock(HostResources.class);
        Mockito.when(hostToStart.getCpuOverprovisioning()).thenReturn(1f);
        Mockito.when(hostToStart.getCpus()).thenReturn(2);
        Mockito.when(hostToStart.getSpeed()).thenReturn(1000l);
        Mockito.when(hostToStart.getMemoryOverprovisioning()).thenReturn(2f);
        Mockito.when(hostToStart.getTotalMemoryInBytes()).thenReturn(1000l);

        double result = spyAlgorithm.calculateHostScore(hostToStart);

        Assert.assertEquals(4000000, result, 0f);
    }

    @Test
    public void sortHostsUpwardScoreTest() {
        List<HostResources> hosts = createHostsWithScore();

        spyAlgorithm.sortHostsUpwardScore(hosts);

        Assert.assertEquals(1.0, hosts.get(0).getScore(), 0);
        Assert.assertEquals(2.0, hosts.get(1).getScore(), 0);
        Assert.assertEquals(3.0, hosts.get(2).getScore(), 0);
        Assert.assertEquals(4.0, hosts.get(3).getScore(), 0);
    }

}
