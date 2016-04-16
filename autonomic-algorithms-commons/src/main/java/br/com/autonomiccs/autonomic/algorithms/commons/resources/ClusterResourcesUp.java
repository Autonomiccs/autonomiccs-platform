package br.com.autonomiccs.autonomic.algorithms.commons.resources;

import java.util.List;

/**
 * This object represents the {@link HostResources} that are running in the
 * cluster. TODO ver nome melhor
 */
public class ClusterResourcesUp extends ClusterResources {

    private List<HostResources> hostsResources;
    private long usedCpu, usedMemory;

    public ClusterResourcesUp(long clusterId, String clusterName, long cpuSpeed, long usedCpu, int cpus, long memory, long usedMemory, List<HostResources> hostsResources) {
        super(clusterId, clusterName, cpuSpeed, cpus, memory);
        this.hostsResources = hostsResources;
        this.usedCpu = usedCpu;
        this.usedMemory = usedMemory;
    }

    public void setHostsResources(List<HostResources> hostsList) {
        this.hostsResources = hostsList;
    }

    public List<HostResources> getHostsResources() {
        return hostsResources;
    }

    /**
     * @return sum of each server used Memory (found in capacityDao)
     */
    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    /**
     * @return sum of each server used CPU (found in capacityDao)
     */
    public long getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(long usedCPU) {
        this.usedCpu = usedCPU;
    }

    @Override
    public String toString() {
        return super.toString() + ", usedMemory= " + Long.toString(this.usedMemory) + ", usedCpu= " + Long.toString(this.usedCpu);
    }

}
