package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public class ClusterResourcesServiceTest {

    protected final static int MEGA_BYTES_TO_BYTES = 1000000;
    protected ClusterResourcesService clusterResourcesService = new ClusterResourcesService();
    protected Random random = new Random();

    private HostResourcesTestUtils hostResourcesUtils = new HostResourcesTestUtils();

    @Test
    public void createClusterResourcesUpOneHostTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(1);
        ClusterResourcesUp cluster = clusterResourcesService.createClusterResourcesUp(random.nextLong(), "cluster", hosts);

        Assert.assertEquals(512, cluster.getUsedMemory());
        Assert.assertEquals(1000l, cluster.getUsedCpu());

        checkCpuAndMemory(cluster);
    }

    @Test
    public void clusterResourcesAvailableToStartTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(1);
        ClusterResourcesAvailableToStart cluster = clusterResourcesService.createClusterResourcesAvailableToStart(random.nextLong(), "cluster", hosts);

        checkCpuAndMemory(cluster);

        Assert.assertEquals(hosts, cluster.getHostsToStart());
    }

    private void checkCpuAndMemory(ClusterResources cluster) {
        Assert.assertEquals(2000l, cluster.getCpuSpeed());
        Assert.assertEquals(8192l * MEGA_BYTES_TO_BYTES, cluster.getMemoryInBytes());
        Assert.assertEquals(4, cluster.getCpus());
    }

    protected List<HostResources> createClusterHomogeneousHosts(int numberOfHosts) {
        List<HostResources> hosts = new ArrayList<HostResources>();
        for (int i = 0; i < numberOfHosts; i++) {
            HostResources host = createHostResources(random.nextLong(), 8192, 4, 2000l);
            hostResourcesUtils.createHostWithSmallVms(host, 1);
            hosts.add(host);
        }
        return hosts;
    }

    private HostResources createHostResources(long hostId, long totalMemory, int cpus, long cpuSpeed) {
        HostResources h = new HostResources();
        h.setHostId(hostId);
        h.setMemoryOverprovisioning(1);
        h.setCpuOverprovisioning(1);
        h.setUsedCpu(0l);
        h.setTotalMemoryInBytes(totalMemory * MEGA_BYTES_TO_BYTES);
        h.setUsedMemoryInMegaBytes(0l);
        h.setCpus(cpus);
        h.setSpeed(cpuSpeed);

        return h;
    }
}
