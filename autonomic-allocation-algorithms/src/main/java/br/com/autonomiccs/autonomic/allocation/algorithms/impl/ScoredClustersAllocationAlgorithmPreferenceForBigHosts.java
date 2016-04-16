package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This algorithm was designed for the LRG cloud environment. It gives priority to start hosts with
 * bigger amount of resources.
 */
public class ScoredClustersAllocationAlgorithmPreferenceForBigHosts extends ScoredClustersAllocationAlgorithmPreferenceForSmallHosts {

    /**
     * It calls the {@link #sortHostsDownwardScore(List)}
     */
    @Override
    protected void sortHosts(List<HostResources> sortedHosts) {
        sortHostsDownwardScore(sortedHosts);
    }

    /**
     * It sorts hosts by downward score. Hosts with higher score positioned on
     * lower indexes of the list.
     *
     * @param
     */
    protected void sortHostsDownwardScore(List<HostResources> hosts) {
        Collections.sort(hosts, hostReversedComparator);
    }

    /**
     * This method allows to revert the {@link HostUpwardComparator} logic; thus the logic allows to
     * sort hosts with lower score in lower indexes.
     */
    @SuppressWarnings("unchecked")
    protected Comparator<HostResources> hostReversedComparator = ComparatorUtils.reversedComparator(hostUpwardScoreComparator);

}
