package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public class CloudResourcesServiceTest extends ClusterResourcesServiceTest {

    private CloudResourcesService cloudResourcesService = new CloudResourcesService();

    @Test
    public void createCloudResourcesTestOneCluster() {
        List<ClusterResourcesUp> clustersResourcesUp = createAllClustersResources(1);
        executeCreateCloudResourcesTest(clustersResourcesUp);
    }

    @Test
    public void createCloudResourcesTestFourCluster() {
        List<ClusterResourcesUp> clustersResourcesUp = createAllClustersResources(4);
        executeCreateCloudResourcesTest(clustersResourcesUp);
    }

    private void executeCreateCloudResourcesTest(List<ClusterResourcesUp> clustersResourcesUp) {
        int amountCLusters = clustersResourcesUp.size();
        CloudResources cloud = cloudResourcesService.createCloudResources(clustersResourcesUp);
        Assert.assertEquals(1 * amountCLusters, cloud.getClusters().size());
        Assert.assertEquals(4 * amountCLusters, cloud.getCpus());
        Assert.assertEquals(512 * amountCLusters, cloud.getUsedMemory());
        Assert.assertEquals(1000l * amountCLusters, cloud.getUsedCpu());
        Assert.assertEquals(2000l * amountCLusters, cloud.getCpuSpeed());
        Assert.assertEquals(8192l * MEGA_BYTES_TO_BYTES * amountCLusters, cloud.getMemoryInBytes());
    }

    private List<ClusterResourcesUp> createAllClustersResources(int nOfClusters) {
        List<ClusterResourcesUp> clusters = new ArrayList<ClusterResourcesUp>();
        for (int i = 1; i <= nOfClusters; i++) {
            List<HostResources> hosts = createClusterHomogeneousHosts(1);
            ClusterResourcesUp cluster = clusterResourcesService.createClusterResourcesUp(random.nextLong(), "cluster", hosts);
            clusters.add(cluster);
        }
        return clusters;
    }
}
