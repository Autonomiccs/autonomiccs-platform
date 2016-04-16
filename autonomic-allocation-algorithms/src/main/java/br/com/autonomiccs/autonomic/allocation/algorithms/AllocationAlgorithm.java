package br.com.autonomiccs.autonomic.allocation.algorithms;

import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This interface provides the basic methods that are used by the allocation
 * agent of the CloudStack autonomous consolidation plugin.
 *
 * The implemented algorithm should have some kind of "intelligence" to define
 * which servers to power on; first it has to choose a cluster to start the
 * process; then it chooses servers to be activated.
 */
public interface AllocationAlgorithm {

    /**
     * The lower index of the lists the higher priority to receive a VM that is
     * being deployed.
     *
     * @param List
     *            of clusters
     * @return Ordered list of Clusters
     */
    public List<ClusterResourcesAvailableToStart> rankClustersToAllocation(List<ClusterResourcesAvailableToStart> clusters);

    /**
     * Checks if it is needed to activate hosts of the cluster on after the
     * deployment of a VM.
     *
     * @param cloudCapacity
     * @return True if the available (idle) resources are not sufficient.
     */
    public boolean needsToActivateHost(CloudResources cloudCapacity);

    /**
     * Order the deactivated hosts list, the first ones of the lists will be
     * activated first.
     *
     * @param deactivated
     *            hosts to be enabled
     * @return deactivated hosts ordered to be started
     */
    public List<HostResources> rankHostsToStart(List<HostResources> hostsResources);

}
