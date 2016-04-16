package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public abstract class ScoredClustersAllocationAlgorithm {

    protected List<HostResources> createHostsWithScore() {
        List<HostResources> hosts = new ArrayList<HostResources>();

        HostResources host1 = new HostResources();
        host1.setScore(4.0);
        hosts.add(host1);

        HostResources host2 = new HostResources();
        host2.setScore(2.0);
        hosts.add(host2);

        HostResources host3 = new HostResources();
        host3.setScore(1.0);
        hosts.add(host3);

        HostResources host4 = new HostResources();
        host4.setScore(3.0);
        hosts.add(host4);

        return hosts;
    }

    protected List<ClusterResourcesAvailableToStart> createClustersWithScore() {
        List<ClusterResourcesAvailableToStart> clusters = new ArrayList<ClusterResourcesAvailableToStart>();

        ClusterResourcesAvailableToStart cluster1 = new ClusterResourcesAvailableToStart(1l, "cluster1", 1l, 1, 1l, null);
        cluster1.setScore(4.0);
        clusters.add(cluster1);

        ClusterResourcesAvailableToStart cluster2 = new ClusterResourcesAvailableToStart(2l, "cluster2", 1l, 1, 1l, null);
        cluster2.setScore(1.0);
        clusters.add(cluster2);

        ClusterResourcesAvailableToStart cluster3 = new ClusterResourcesAvailableToStart(3l, "cluster3", 1l, 1, 1l, null);
        cluster3.setScore(3.0);
        clusters.add(cluster3);

        ClusterResourcesAvailableToStart cluster4 = new ClusterResourcesAvailableToStart(4l, "cluster4", 1l, 1, 1l, null);
        cluster4.setScore(2.0);
        clusters.add(cluster4);

        return clusters;
    }

}
