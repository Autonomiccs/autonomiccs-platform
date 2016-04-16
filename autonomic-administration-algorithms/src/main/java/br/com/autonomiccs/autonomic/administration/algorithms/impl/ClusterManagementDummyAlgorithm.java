package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterManagerHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

public class ClusterManagementDummyAlgorithm implements ClusterManagerHeuristicAlgorithm {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int getClusterIntervalBetweenConsolidation() {
        return Integer.MAX_VALUE;
    }

    /**
     * Uses the {@link #cloneListOfHosts(List)} method. It does not alter sequence from the original
     * list; it will return a clone of the original list.
     */
    @Override
    public List<HostResources> rankHosts(List<HostResources> hosts) {
        return cloneListOfHosts(hosts);
    }

    @Override
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts) {
        return new HashMap<Long, HostResources>();
    }

    /**
     * Uses the {@link #cloneListOfHosts(List)} method. It does not alter the sequence from the
     * original list; it returns a cloned list.
     */
    @Override
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts) {
        return cloneListOfHosts(idleHosts);
    }

    @Override
    public boolean canPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources) {
        return false;
    }

    @Override
    public boolean canPowerOffAnotherHostInCloud(CloudResources cloudResources) {
        return false;
    }

    @Override
    public boolean canHeuristicShutdownHosts() {
        return false;
    }

    /**
     * Clones the given {@link List} of {@link HostResources} to a new list. The new list allows
     * to operate into same objects (from the cloned list) without alter the given list.
     *
     * @param hosts
     * @return
     */
    protected List<HostResources> cloneListOfHosts(List<HostResources> hosts) {
        List<HostResources> clonedHostList = new ArrayList<HostResources>();
        for (HostResources host : hosts) {
            try {
                HostResources clonedHost = (HostResources) host.clone();
                clonedHost.setVmsResources(cloneHostVmsList(host));
                clonedHostList.add(clonedHost);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return clonedHostList;
    }

    /**
     * It clones a list of {@link VmResources}.
     *
     * @param host
     */
    protected List<VmResources> cloneHostVmsList(HostResources host) {
        return new ArrayList<VmResources>(host.getVmsResources());
    }

}
