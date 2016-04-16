package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * Extends the {@link ConsolidationScoredPreferenceForSmallHosts} overriding {@link #sortHosts(List)}. This
 * Class allows to choose hosts with higher amount of resources to stay running;
 */
public class ConsolidationScoredPreferenceForBigHosts extends ConsolidationScoredPreferenceForSmallHosts {

    /**
     * Sorts the hosts with higher amount of resources as the most suitable to
     * stay running. It uses the {@link #sortHostsDownwardScore(List)} method.
     */
    @Override
    protected void sortHosts(List<HostResources> sortedHosts) {
        sortHostsDownwardScore(sortedHosts);
    }

}
