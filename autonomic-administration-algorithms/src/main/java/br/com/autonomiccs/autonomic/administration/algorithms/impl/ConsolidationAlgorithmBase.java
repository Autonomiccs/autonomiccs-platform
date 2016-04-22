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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

/**
 * Provides basic heuristics to be extended by specialized consolidation algorithms.
 */
public class ConsolidationAlgorithmBase extends ClusterManagementDummyAlgorithm {

    /**
     * This constant was created to divide a number (in Bytes) by 1.000.000 (resulting in a number
     * of Mega Bytes metrics).
     */
    protected final static int BYTES_TO_MEGA_BYTES = 1000000;

    /**
     * It indicates if the given heuristic is going to shutdown hosts.
     * This is used to control the deployment of Autonomiccs system VMs.
     */
    protected boolean canHeuristicShutdownHost = true;

    @Override
    public int getClusterIntervalBetweenConsolidation() {
        return 600;
    }

    /**
     * Maps VMs from hosts with lower score(interest in stay running) to hosts with higher score
     * (more interesting to keep running).
     *
     * @note For each {@link HostResources} in the given {@link List} (starts from the
     *       higher index to the lower), it gets a {@link List} of {@link VmResources}; then, for
     *       each host (starts from the higher index to the lower), maps a migration of the selected
     *       VM to the selected host.
     */
    @Override
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts) {
        Map<Long, HostResources> vmsToHost = new HashMap<>();
        for (int i = rankedHosts.size() - 1; i > 0; i--) {
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

    /**
     * Checks if the host can allocate the VM. If the given host has more resources available than
     * the resources needed by the VM, it returns true. The comparison of resources considers the
     * number of CPUs, total CPU (number of CPUs * CPU speed) and total memory; if any comparison
     * fails it returns false.
     *
     * @param vm
     * @param host
     * @return
     */
    public boolean canMigrateVmToHost(VmResources vm, HostResources host) {
        if (host.getCpus() < vm.getNumberOfCpus()) {
            return false;
        }
        long vmCpuNeeds = vm.getNumberOfCpus() * vm.getCpuSpeed();
        if (getHostAvailableCpu(host) < vmCpuNeeds) {
            return false;
        }
        long vmMemory = vm.getMemoryInMegaBytes();
        long hostAvailableMemory = getHostAvailableMemory(host);
        if (hostAvailableMemory < vmMemory) {
            return false;
        }
        return true;
    }

    /**
     * Returns the amount of memory in the given {@link HostResources}. It already considers the
     * Memory overprovisioning
     *
     * @param host
     * @return
     */
    protected long getHostAvailableMemory(HostResources host) {
        return (long) (host.getMemoryOverprovisioning() * getHostTotalMemoryInMegaBytes(host)) - host.getUsedMemoryInMegaBytes();
    }

    /**
     * Returns the host total memory. As the column 'ram' from the table 'host' stores the memory in
     * Bytes, it is necessary to divide the memory (from the {@link HostResources#getTotalMemoryInBytes()}
     * by {@value #BYTES_TO_MEGA_BYTES}, converting it to Mega Bytes.
     *
     * @param host
     * @return
     */
    protected long getHostTotalMemoryInMegaBytes(HostResources host) {
        return host.getTotalMemoryInBytes() / BYTES_TO_MEGA_BYTES;
    }

    /**
     * Returns the total CPU. It multiply the CPU over-provisioning, CPU speed and number of CPUs;
     * then subtracts from the total CPU the CPU usage.
     *
     * @param host
     * @return
     */
    protected float getHostAvailableCpu(HostResources host) {
        return (host.getCpuOverprovisioning() * host.getSpeed() * host.getCpus()) - host.getUsedCpu();
    }

    /**
     * Given a {@link VmResources} and a {@link HostResources}, it sums to the usage of the host
     * (used memory and used CPU) the memory and CPU allocated by the VM.
     *
     * @param vm
     * @param host
     */
    public void updateHostUsedResources(VmResources vm, HostResources host) {
        host.setUsedCpu(host.getUsedCpu() + vm.getNumberOfCpus() * vm.getCpuSpeed());
        host.setUsedMemoryInMegaBytes(host.getUsedMemoryInMegaBytes() + vm.getMemoryInMegaBytes());
    }

    /**
     * There are no heuristics developed for this method. The heuristic was developed in
     * {@link ConsolidationScoredPreferenceForSmallHosts}.
     */
    @Override
    public final boolean canPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources) {
        if(canHeuristicShutdownHost){
            return internalCanPowerOffHost(hostToPowerOff, cloudResources);
        }
        return false;
    }

    protected boolean internalCanPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources) {
        return true;
    }

    /**
     * There are no heuristics developed for this method. The heuristic was developed in
     * {@link ConsolidationScoredPreferenceForSmallHosts}.
     */
    @Override
    public final boolean canPowerOffAnotherHostInCloud(CloudResources cloudResources) {
        if(canHeuristicShutdownHost){
            return internalCanPowerOffAnotherHostInCloud(cloudResources);
        }
        return false;
    }

    protected boolean internalCanPowerOffAnotherHostInCloud(CloudResources cloudResources) {
        return true;
    }

    /**
     * It indicates if the given heuristic will be able to shutdown hosts.
     *
     * @return boolean indicating if the shutdown of hosts is enabled by the heuristics
     */
    @Override
    public boolean canHeuristicShutdownHosts() {
        return canHeuristicShutdownHost;
    }
}
