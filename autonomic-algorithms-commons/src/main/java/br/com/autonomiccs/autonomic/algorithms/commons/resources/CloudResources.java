package br.com.autonomiccs.autonomic.algorithms.commons.resources;

import java.util.List;

/**
 * Represents all the resources in the cloud (number of cpus, cpu speed, cpu
 * usage, memory, used memory and all the {@link ClusterResources} in this
 * cloud.
 */
public class CloudResources {

    private int cpus;
    private long usedCpu, cpuSpeed, usedMemory, memory;
    private List<ClusterResourcesUp> clusters;

    public CloudResources(List<ClusterResourcesUp> clusters, long uMemory, long mem, long uCpu, long cpuFreq, int cpuN) {
        this.clusters = clusters;
        this.usedMemory = uMemory;
        this.memory = mem;
        this.usedCpu = uCpu;
        this.cpuSpeed = cpuFreq;
        this.cpus = cpuN;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public long getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(long usedCpu) {
        this.usedCpu = usedCpu;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getMemoryInBytes() {
        return memory;
    }

    public void setMemoryInBytes(long memory) {
        this.memory = memory;
    }

    public List<ClusterResourcesUp> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterResourcesUp> clusters) {
        this.clusters = clusters;
    }

}
