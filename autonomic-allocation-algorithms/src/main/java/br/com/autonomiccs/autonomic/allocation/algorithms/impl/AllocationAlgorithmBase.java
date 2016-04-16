package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.ArrayList;
import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.AllocationAlgorithm;

/**
 * This class provides basic heuristics to be extended by specialized allocation algorithms.
 */
public class AllocationAlgorithmBase implements AllocationAlgorithm {

    /**
     * It clones the given {@link List} of {@link ClusterResourcesAvailableToStart} to a new list.
     * The
     * new list allows to operate into same objects (from the cloned list) without alter the given
     * list.
     *
     * @param clusters
     * @return {@link List} of {@link ClusterResourcesAvailableToStart}
     */
    @SuppressWarnings("unchecked")
    protected List<ClusterResourcesAvailableToStart> cloneListOfClusters(List<ClusterResourcesAvailableToStart> clusters) {
        return (List<ClusterResourcesAvailableToStart>) ((ArrayList<ClusterResourcesAvailableToStart>) clusters).clone();
    }

    /**
     * It clones the given {@link List} of {@link HostResources} to a new list. The new list allows
     * to operate into same objects (from the cloned list) without alter the given list.
     *
     * @param hosts
     * @return {@link List} of {@link HostResources}
     */
    @SuppressWarnings("unchecked")
    protected List<HostResources> cloneListOfHosts(List<HostResources> hosts) {
        return (List<HostResources>) ((ArrayList<HostResources>) hosts).clone();
    }

    /**
     * It uses the {@link #cloneListOfClusters(List)} method. It does not alter sequence from the
     * original list; thus, the ranking method just returns the original list.
     *
     * @return {@link List} of {@link ClusterResourcesAvailableToStart}
     */
    @Override
    public List<ClusterResourcesAvailableToStart> rankClustersToAllocation(List<ClusterResourcesAvailableToStart> clusters) {
        return cloneListOfClusters(clusters);
    }

    /**
     * This method always returns false.
     */
    @Override
    public boolean needsToActivateHost(CloudResources cloudCapacity) {
        return false;
    }

    /**
     * It uses the {@link #cloneListOfHosts(List)} method. It does not alter sequence from the
     * original
     * list; thus, the ranking method just returns the original list.
     *
     * @return {@link List} of {@link HostResources}
     */
    @Override
    public List<HostResources> rankHostsToStart(List<HostResources> hostsResources) {
        return cloneListOfHosts(hostsResources);
    }

}
