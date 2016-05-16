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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.MathUtils;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.VmResources;

/**
 * This class provides heuristics that allows the manager to balance the cluster workload among its
 * hosts. This heuristic does not power off hosts, its actions are related to VMs migrations.
 */
public class VmsDispersionAlgorithmForHomogeneousEnvironment extends ConsolidationScoredPreferenceForSmallHosts {

    /**
     * Represents the sum of hosts used memory divided by the sum of hosts total memory.
     */
    private double clusterMemoryUsageAverage;

    /**
     * Standard deviation of VMs memory configuration. This value considers scenarios where there
     * are a high variability in the VMs amount of resources, given the maximum and minimum load
     * allowed considering that some VMs might be big.
     */
    private double standardDeviationVmsConfiguration;

    /**
     * Standard deviation of hosts memory usage. This value considers the heterogeneous usage of
     * resources in the hosts, if the usage is very different, it will have a higher standard
     * deviation and allows the maximum and minimum load based on this evaluation.
     */
    private double standardDeviationHostsUsage;

    /**
     * It is the average between the {@link #standardDeviationVmsConfiguration} and
     * {@link #standardDeviationHostsUsage} standard deviation values.
     */
    private double standardDeviationAverage;

    private StandardDeviation std = new StandardDeviation(false);

    public VmsDispersionAlgorithmForHomogeneousEnvironment() {
        canHeuristicShutdownHost = false;
    }

    /**
     * It ranks hosts to receive VMs. It clones the given list (using
     * {@link #cloneListOfHosts(List)}),
     * sets each host score using {@link #setEachHostScore(List)} and sorts the list using
     * {@link #sortHosts(List)} method.
     */
    @Override
    public List<HostResources> rankHosts(List<HostResources> hostsList) {
        clusterMemoryUsagePercentage(hostsList);
        List<HostResources> sortedHosts = cloneListOfHosts(hostsList);
        setEachHostScore(sortedHosts);
        sortHosts(sortedHosts);
        return sortedHosts;
    }

    /**
     * It sorts hosts downward using the {@link #sortHostsUpwardScore(List)} method.
     */
    @Override
    protected void sortHosts(List<HostResources> hosts) {
        sortHostsDownwardScore(hosts);
    }

    /**
     * It clones the given list using the {@link #cloneListOfHosts(List)} method
     */
    @Override
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts) {
        return cloneListOfHosts(idleHosts);
    }

    /**
     * It calculates the host score based on its memory usage proportion (memory usage / total
     * memory)
     */
    @Override
    protected double calculateHostScore(HostResources host) {
        if (host.getUsedMemoryInMegaBytes() != 0) {
            return (host.getTotalMemoryInBytes() / BYTES_TO_MEGA_BYTES) / host.getUsedMemoryInMegaBytes();
        }
        return Double.POSITIVE_INFINITY;
    }

    /**
     * It returns false to ensure that this heuristic does not shut down any host.
     */
    @Override
    public boolean internalCanPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources) {
        return false;
    }

    /**
     * It updates the cluster memory usage average, and the standard deviations (
     * {@link #clusterMemoryUsageAverage}, {@link #standardDeviationAverage},
     * {@link #standardDeviationHostsUsage}, {@link #standardDeviationVmsConfiguration})
     */
    protected void clusterMemoryUsagePercentage(List<HostResources> hosts) {
        double clusterUsedMemory = 0;
        double vmsUsedMemory[] = null;
        double hostsUsedMemory[] = new double[hosts.size()];
        for (int hostIterator = 0; hostIterator < hosts.size(); hostIterator++) {
            List<VmResources> vmsResources = hosts.get(hostIterator).getVmsResources();
            double hostVmsUsedMemory[] = new double[vmsResources.size()];
            for (int vmIterator = 0; vmIterator < vmsResources.size(); vmIterator++) {
                hostVmsUsedMemory[vmIterator] = vmsResources.get(vmIterator).getMemoryInMegaBytes();
            }
            vmsUsedMemory = ArrayUtils.addAll(vmsUsedMemory, hostVmsUsedMemory);

            long hostUsedMemoryInMegaBytes = hosts.get(hostIterator).getUsedMemoryInMegaBytes();
            hostsUsedMemory[hostIterator] = hostUsedMemoryInMegaBytes;
            clusterUsedMemory += hostUsedMemoryInMegaBytes;
        }
        clusterMemoryUsageAverage = clusterUsedMemory / hosts.size();
        standardDeviationVmsConfiguration = std.evaluate(vmsUsedMemory);
        standardDeviationHostsUsage = std.evaluate(hostsUsedMemory);
        standardDeviationAverage = (standardDeviationVmsConfiguration + standardDeviationHostsUsage) / 2;
    }

    /**
     * It maps VMs from hosts with lower score (interest in distribute some of its VMs) to hosts
     * with
     * higher score (interesting to allocate more VMs). This methods simulates three different
     * mappings methods; the simulation that give the best standard deviation on the hosts workload
     * will be the chosen one. Each simulation is done by calling the {@link simulateVmsMigrations}
     * method with a different standard deviation ({@link #standardDeviationVmsConfiguration},
     * {@link #standardDeviationHostsUsage}, {@link #standardDeviationAverage}).
     * The standard deviation is used to give a maximum load in each host (host average load +
     * standard deviation) and the minimum load (host average load - standard deviation).
     */
    @Override
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts) {
        if (MathUtils.equals(standardDeviationHostsUsage, 0)) {
            return new HashMap<>();
        }
        List<HostResources> rankedHostsStdVms = cloneListOfHosts(rankedHosts);
        List<HostResources> rankedHostsStdHosts = cloneListOfHosts(rankedHosts);
        List<HostResources> rankedHostsStdAverage = cloneListOfHosts(rankedHosts);

        logger.debug("Simulating migration mapping with VMs resource standard deviation");
        Map<Long, HostResources> vmsToHostStdVms = simulateVmsMigrations(rankedHostsStdVms, standardDeviationVmsConfiguration);

        logger.debug("Simulating migration mapping with hosts resource standard deviation");
        Map<Long, HostResources> vmsToHostStdHosts = simulateVmsMigrations(rankedHostsStdHosts, standardDeviationHostsUsage);

        logger.debug("Simulating migration mapping with the average of hosts resource and VMs resource standard deviation");
        Map<Long, HostResources> vmsToHostStdAverage = simulateVmsMigrations(rankedHostsStdAverage, standardDeviationAverage);

        double usedMemoryHostsWithStdVms[] = new double[rankedHosts.size()];
        for (int i = 0; i < rankedHostsStdVms.size(); i++) {
            usedMemoryHostsWithStdVms[i] = rankedHostsStdVms.get(i).getUsedMemoryInMegaBytes();
        }
        double stdWithStdVms = std.evaluate(usedMemoryHostsWithStdVms);
        logger.debug(String.format("The Std. achieved using the VMs resource standard deviation as parameter for the simulation was [%f]", stdWithStdVms));
        double usedMemoryHostsWithStdHost[] = new double[rankedHosts.size()];
        for (int i = 0; i < rankedHostsStdHosts.size(); i++) {
            usedMemoryHostsWithStdHost[i] = rankedHostsStdHosts.get(i).getUsedMemoryInMegaBytes();
        }
        double stdWithStdHosts = std.evaluate(usedMemoryHostsWithStdHost);
        logger.debug(String.format("The Std. achieved using the hosts resource standard deviation as parameter for the simulation was [%f]", stdWithStdHosts));

        double usedMemoryHostsWithStdAverage[] = new double[rankedHosts.size()];
        for (int i = 0; i < rankedHostsStdAverage.size(); i++) {
            usedMemoryHostsWithStdAverage[i] = rankedHostsStdAverage.get(i).getUsedMemoryInMegaBytes();
        }
        double stdWithStdAverage = std.evaluate(usedMemoryHostsWithStdAverage);
        logger.debug(String.format("The Std. achieved using the average between hosts resource and VMs resource standard deviation as parameter for the simulation was [%f]",
                stdWithStdAverage));

        if (stdWithStdAverage <= stdWithStdHosts && stdWithStdAverage <= stdWithStdVms) {
            logger.debug("The simulation that won the competition was the one executed with alpha as the average of host resource and VMs resource standard deviation.");
            logger.debug(String.format("The number of migrations that will be executed is[%d].", vmsToHostStdAverage.size()));
            return vmsToHostStdAverage;
        }
        if (stdWithStdHosts <= stdWithStdVms) {
            logger.debug("The simulation that won the competition was the one executed with alpha as the host resource standard deviation.");
            logger.debug(String.format("The number of migrations that will be executed is[%d].", vmsToHostStdHosts.size()));
            return vmsToHostStdHosts;
        }
        logger.debug("The simulation that won the competition was the one executed with alpha as the VMs resource standard deviation.");
        logger.debug(String.format("The number of migrations that will be executed is[%d].", vmsToHostStdVms.size()));
        return vmsToHostStdVms;
    }


    /**
     * For a given a list of {@link HostResources} and a value that represents the standard
     * deviation it maps virtual machine migrations. The standar deviation is used as a
     */
    protected Map<Long, HostResources> simulateVmsMigrations(List<HostResources> rankedHosts, double standardDeviation) {
        Map<Long, HostResources> vmsToHost = new HashMap<>();

        double hostMemoryMinimumUsageAllowed = clusterMemoryUsageAverage - standardDeviation;
        for (int i = rankedHosts.size() - 1; i > 0; i--) {
            HostResources hostToBeOffLoaded = rankedHosts.get(i);
            if (hostToBeOffLoaded.getUsedMemoryInMegaBytes() > hostMemoryMinimumUsageAllowed) {

                for (HostResources hostCandidateToReceiveVMs : rankedHosts) {
                    if (hostCandidateToReceiveVMs.getUsedMemoryInMegaBytes() <= clusterMemoryUsageAverage) {

                        List<VmResources> clonedListOfVms = cloneHostVmsList(hostToBeOffLoaded);
                        for (VmResources vmResources : clonedListOfVms) {
                            if (isMemoryUsageOfHostsAfterVmMigration(vmResources, hostToBeOffLoaded, hostCandidateToReceiveVMs, standardDeviation)
                                    && canMigrateVmToHost(vmResources, hostCandidateToReceiveVMs)) {
                                updateHostUsedResources(vmResources, hostToBeOffLoaded, hostCandidateToReceiveVMs);
                                vmsToHost.put(vmResources.getVmId(), hostCandidateToReceiveVMs);
                            }
                        }
                    }
                }
            }
        }
        return vmsToHost;
    }


    /**
     * It updates the resources counting (used cpu and memory) from the host that the VM resids on
     * and the target host to be migrated.
     */
    private void updateHostUsedResources(VmResources vm, HostResources hostVmResidOn, HostResources targetHost) {
        super.updateHostUsedResources(vm, targetHost);
        targetHost.getVmsResources().add(vm);

        hostVmResidOn.getVmsResources().remove(vm);
        hostVmResidOn.setUsedCpu(hostVmResidOn.getUsedCpu() - (vm.getCpuSpeed() * vm.getNumberOfCpus()));
        hostVmResidOn.setUsedMemoryInMegaBytes(hostVmResidOn.getUsedMemoryInMegaBytes() - vm.getMemoryInMegaBytes());
    }

    /**
     * It returns true if a VM migration will maintain the VM's current host with a usage above the
     * {@link #hostMemoryMinimumUsageAllowedStdVms}; and if the target host usage will stay below
     * the {@link #hostMemoryMaximumUsageAllowedStdVms} after the migration.
     */
    private boolean isMemoryUsageOfHostsAfterVmMigration(VmResources vmResources, HostResources hostVmResidOn, HostResources targetHost, double standarDeviation) {
        if (hostVmResidOn.getUsedMemoryInMegaBytes() == targetHost.getUsedMemoryInMegaBytes()) {
            return false;
        }
        long vmMemoryInMegaBytes = vmResources.getMemoryInMegaBytes();
        long hostMemoryAfterMigrateVm = hostVmResidOn.getUsedMemoryInMegaBytes() - vmMemoryInMegaBytes;
        long targetHostMemoryUsageAfterReceiveVM = targetHost.getUsedMemoryInMegaBytes() + vmMemoryInMegaBytes;

        double hostMemoryMinimumUsageAllowed = clusterMemoryUsageAverage - standarDeviation;
        double hostMemoryMaximumUsageAllowed = clusterMemoryUsageAverage + standarDeviation;

        return hostMemoryMinimumUsageAllowed <= hostMemoryAfterMigrateVm && targetHostMemoryUsageAfterReceiveVM < hostMemoryMaximumUsageAllowed;
    }
}