package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.AllocationAlgorithm;

@RunWith(MockitoJUnitRunner.class)
public class AllocationAlgorithmBaseTest {

    private AllocationAlgorithmBase spyAlgorithm;

    @Before
    public void setUp() {
        spyAlgorithm = Mockito.spy(new AllocationAlgorithmBase());
    }

    @Test
    public void implementsAllocationAlgorithm() {
        Assert.assertTrue(AllocationAlgorithm.class.isAssignableFrom(AllocationAlgorithmBase.class));
    }

    @Test
    public void cloneListOfClustersTest() {
        List<ClusterResourcesAvailableToStart> clusters = new ArrayList<ClusterResourcesAvailableToStart>();
        ClusterResourcesAvailableToStart cluster1 = Mockito.mock(ClusterResourcesAvailableToStart.class);
        clusters.add(cluster1);
        List<ClusterResourcesAvailableToStart> clonedClusters = spyAlgorithm.cloneListOfClusters(clusters);

        Assert.assertNotNull(clonedClusters);
        Assert.assertEquals(1, clonedClusters.size());
        Assert.assertEquals(clusters, clonedClusters);
        Assert.assertEquals(cluster1, clonedClusters.get(0));
    }

    @Test
    public void cloneListOfHostsTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        HostResources host = Mockito.mock(HostResources.class);
        hosts.add(host);

        List<HostResources> clonedHosts = spyAlgorithm.cloneListOfHosts(hosts);

        Assert.assertNotNull(clonedHosts);
        Assert.assertEquals(1, clonedHosts.size());
        Assert.assertEquals(hosts, clonedHosts);
        Assert.assertEquals(host, clonedHosts.get(0));
    }

    @Test
    public void rankClustersToAllocationTest() {
        List<ClusterResourcesAvailableToStart> clusters = new ArrayList<ClusterResourcesAvailableToStart>();
        Mockito.doReturn(clusters).when(spyAlgorithm).cloneListOfClusters(clusters);

        List<ClusterResourcesAvailableToStart> clonedClustersToAllocation = spyAlgorithm.rankClustersToAllocation(clusters);

        Mockito.verify(spyAlgorithm).cloneListOfClusters(clusters);
        Assert.assertEquals(clusters, clonedClustersToAllocation);
    }

    @Test
    public void needsToActivateHostTest() {
        CloudResources cloudCapacity = Mockito.mock(CloudResources.class);
        Assert.assertFalse(spyAlgorithm.needsToActivateHost(cloudCapacity));
    }

    @Test
    public void rankHostsToStartTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        Mockito.doReturn(hosts).when(spyAlgorithm).cloneListOfHosts(hosts);

        List<HostResources> clonedHostsToStart = spyAlgorithm.rankHostsToStart(hosts);

        Mockito.verify(spyAlgorithm).cloneListOfHosts(hosts);
        Assert.assertEquals(hosts, clonedHostsToStart);
    }

}
