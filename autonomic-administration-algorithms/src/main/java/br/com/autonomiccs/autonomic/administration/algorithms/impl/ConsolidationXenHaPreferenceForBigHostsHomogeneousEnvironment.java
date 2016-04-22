/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

/**
 * Extends the {@link ConsolidationScoredPreferenceForBigHosts} overriding
 * {@link #mapVMsToHost(List)}. This Class allows to consolidate over clusters with Xen Server High
 * Availability feature enabled, avoiding fence problems that happens in clusters with less than 3
 * hosts. If the amount of hosts in the cluster is above 3 it chooses hosts with higher amount of
 * resources to stay running and consolidates the environment (at the best scenario 3 hosts stay
 * running); otherwise it uses the
 * {@link VmsDispersionAlgorithmForHomogeneousEnvironment#mapVMsToHost(List)} method;
 */
public class ConsolidationXenHaPreferenceForBigHostsHomogeneousEnvironment extends ConsolidationScoredPreferenceForBigHosts {

    protected VmsDispersionAlgorithmForHomogeneousEnvironment vmsDispersionHomogeneousHosts = new VmsDispersionAlgorithmForHomogeneousEnvironment();

    protected long totalNumbersOfHostsInCluster = 0;

    @Override
    public List<HostResources> rankHosts(List<HostResources> hostsList) {
        totalNumbersOfHostsInCluster = hostsList.size();
        return super.rankHosts(hostsList);
    }

    /**
     * Maps migrations among hosts if the cluster size is bigger than 3 hosts, avoinding fence
     * problems in a HA Xen cluster. If the cluster has more than 3 hosts, then it will allow to
     * power off hosts until it has only 3.
     */
    @Override
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts) {
        Map<Long, HostResources> vmsToHost = new HashMap<Long, HostResources>();
        if (rankedHosts.size() > 3) {
            for (int i = rankedHosts.size() - 1; i > 2; i--) {
                for (VmResources vmResources : rankedHosts.get(i).getVmsResources()) {
                    for (HostResources hostCandidateToStayRunning : rankedHosts) {
                        if (canMigrateVmToHost(vmResources, hostCandidateToStayRunning)) {
                            vmsToHost.put(vmResources.getVmId(), hostCandidateToStayRunning);
                            updateHostUsedResources(vmResources, hostCandidateToStayRunning);
                            break;
                        }
                    }
                }
            }
            return vmsToHost;
        }
        vmsDispersionHomogeneousHosts.rankHosts(rankedHosts);
        return vmsDispersionHomogeneousHosts.mapVMsToHost(rankedHosts);
    }

    /**
     * If the cluster size is of 4 hosts or bigger, it ranks the host according the super class (
     * {@link ConsolidationScoredPreferenceForBigHosts#rankHostToPowerOff(List)})but removes 3 hosts
     * that are more interesting to keep running.
     */
    @Override
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts) {
        if(totalNumbersOfHostsInCluster < 4){
            return new ArrayList<HostResources>();
        }
        List<HostResources> rankedHostToPowerOff = super.rankHostToPowerOff(idleHosts);
        long hostsThatAreBeingKeptUp = totalNumbersOfHostsInCluster - idleHosts.size();
        for (int i = 0; i < 3 - hostsThatAreBeingKeptUp; i++) {
            removeOnIfPossible(rankedHostToPowerOff);
        }
        return rankedHostToPowerOff;
    }

    protected void removeOnIfPossible(List<HostResources> rankedHostToPowerOff) {
        if (rankedHostToPowerOff.size() > 0) {
            rankedHostToPowerOff.remove(rankedHostToPowerOff.size() - 1);
        }
    }
}
