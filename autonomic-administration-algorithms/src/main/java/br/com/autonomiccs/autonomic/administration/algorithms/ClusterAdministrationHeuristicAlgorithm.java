package br.com.autonomiccs.autonomic.administration.algorithms;

import java.util.List;
import java.util.Map;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This interface provides the basic methods that are used by the consolidation
 * agent of the CloudStack autonomous consolidation plugin. It provides the
 * "intelligence" for the framework, deciding which hosts to be deactivated,
 * clusters to be consolidated and creating the mapping of VMs. All of that is
 * done according to the knowledge that the implementation of this class will
 * provide for the environment.
 */
public interface ClusterAdministrationHeuristicAlgorithm {

    /**
     * @return minimum time interval (seconds) between consolidation of a cluster.
     * */
    public int getClusterIntervalBetweenConsolidation();

    /**
     * Rank a list of hosts based in some score function.
     * The hosts at the top of the lists will have priority to be kept active,
     * @param list of host
     * @return list of host
     * */
    public List<HostResources> rankHosts(List<HostResources> hosts);

    /**
     * Map VMs and hosts to be migrate in order to optimize the environment. The
     * returned {@link Map} should have a key as the VM 'id' and the object as
     * the {@link HostResources}.
     *
     * @param list
     *            of host (an ordered list of hosts) the top hosts have priority
     *            to be kept active
     * @return a list of migration, maps a set of VMs and their target host
     */
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts);

    /**
     * Receive a list of active idle hosts that should be ordered. The top hosts
     * have priority to be deactivated.
     *
     * @param list
     *            of hosts candidates to power off
     * @return list of ordered hosts ordered, by priority
     */
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts);

    /**
     * This method checks if the agent can power off the host.
     *
     * @param resources
     *            of host to be powerOff.
     * @param sum
     *            of cluster resources (idle and total).
     * @return true if can power the host.
     */
    public boolean canPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources);

    /**
     * Checks if the consolidation agent can disable others idle host in the
     * given cluster.
     *
     * @param cluster
     *            load (idle resource, total).
     * @return true if can power off a host.
     */
    boolean canPowerOffAnotherHostInCloud(CloudResources cloudResources);

    /**
     * It returns true if the heuristic can shutdown hosts; it returns false if the heuristic cannot
     * power off hosts.
     */
    boolean canHeuristicShutdownHosts();

}
