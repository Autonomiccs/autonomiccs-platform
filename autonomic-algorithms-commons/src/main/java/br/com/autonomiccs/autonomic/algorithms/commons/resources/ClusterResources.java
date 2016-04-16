package br.com.autonomiccs.autonomic.algorithms.commons.resources;

/**
 * Representation of cluster resources. This class does not considers the hosts in the cluster
 * neither resources usage. For this, {@link ClusterResourcesAvailableToStart} and
 * {@link ClusterResourcesUp} extend it and specialize according their need.
 *
 * @param ClusterVO
 * @param List
 *            of HostResources, every HostResource in this list must be in the cluster.
 */
public abstract class ClusterResources {

    private int cpus;
    private long cpuSpeed, memory, clusterId;
    private String clusterName;
    private double score;

    public long getMemoryInBytes() {
        return memory;
    }
    public void setMemoryInBytes(long memory) {
        this.memory = memory;
    }

    public ClusterResources(long clusterId, String clusterName, long cpuSpeed, int cpus, long memory) {
        this.clusterId = clusterId;
        this.clusterName = clusterName;
        this.cpuSpeed = cpuSpeed;
        this.cpus = cpus;
        this.memory = memory;
    }

    /**
     * @return sum of cpu speed (frequency of each cpu core) of each host in this cluster
     * */
    public long getCpuSpeed() {
        return cpuSpeed;
    }
    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    /**
     * @return sum number of cores in each host in this cluster.
     * */
    public int getCpus() {
        return cpus;
    }
    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }


    @Override
    public String toString() {
        return "clusterId= " + Long.toString(this.clusterId) + ", usedMemory= " + ", memory= " + Long.toString(this.memory) + ", usedCpu= " + ", cpuSpeed= "
                + Long.toString(this.cpuSpeed) + ", cpus= " + Integer.toString(this.cpus);
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

}