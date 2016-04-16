package br.com.autonomiccs.autonomic.algorithms.commons.resources;

import java.util.List;

/**
 * Represents the {@link HostResources} that were deactivated by the {@link ConsolidationManager}; thus they are inactive and available to be started.
 */
public class ClusterResourcesAvailableToStart extends ClusterResources {

    private List<HostResources> hostsToStart;

    public ClusterResourcesAvailableToStart(long clusterId, String clusterName, long cpuSpeed, int cpus, long memory, List<HostResources> hostsToStart) {
        super(clusterId, clusterName, cpuSpeed, cpus, memory);
        this.hostsToStart = hostsToStart;
    }

    public List<HostResources> getHostsToStart() {
        return hostsToStart;
    }

    public void setHostsToStart(List<HostResources> hostsToStart) {
        this.hostsToStart = hostsToStart;
    }

}
