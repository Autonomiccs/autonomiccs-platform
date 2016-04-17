/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;

import br.com.autonomiccs.autonomic.administration.algorithms.pojos.HostProfile;
import br.com.autonomiccs.autonomic.administration.algorithms.profilers.HostProfiler;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This class provides heuristics for sorting priority in hosts to be powered off and decide if can
 * power off a
 * given host in the cluster.
 */
public class ConsolidationScoredPreferenceForSmallHosts extends ConsolidationAlgorithmBase {

    protected HostProfiler profiler;

    /**
     * It ranks hosts to be kept running. It clones the given list (using
     * {@link #cloneListOfHosts(List)}),
     * sets each host score using {@link #setEachHostScore(List)} and sorts the list using
     * {@link #sortHosts(List)} method.
     */
    @Override
    public List<HostResources> rankHosts(List<HostResources> hostsList) {
        List<HostResources> sortedHosts = cloneListOfHosts(hostsList);
        this.profiler = new HostProfiler(sortedHosts);
        setEachHostScore(sortedHosts);
        sortHosts(sortedHosts);
        return sortedHosts;
    }

    /**
     * It sorts hosts upward using the {@link #sortHostsUpwardScore(List)} method.
     *
     * @param hosts
     */
    protected void sortHosts(List<HostResources> hosts) {
        sortHostsUpwardScore(hosts);
    }

    private HostUpwardComparator hostUpwardScoreComparator = new HostUpwardComparator();

    /**
     * It sorts hosts by upward score. Hosts with lower score positioned on lower indexes of the
     * list.
     * It uses the {@link Collections#sort(List, Comparator)}, where the comparator is the
     * {@link HostUpwardComparator}.
     *
     * @param hosts
     */
    protected void sortHostsUpwardScore(List<HostResources> hosts) {
        Collections.sort(hosts, hostUpwardScoreComparator);
    }

    /**
     * This class allows to compare Hosts upward based on each host score using the
     * {@link #compare(HostResources, HostResources)} method to return a integer, this class
     * implements {@link Comparator}.
     */
    public class HostUpwardComparator implements Comparator<HostResources> {
        /**
         * Given 2 (two) {@link HostResources}, it returns the result from the first host score
         * subtracted to the second host score, allowing to compare both hosts. If the result is
         * greater than zero, then the first host score is bigger than the second host score; thus
         * the first host will be sorted in a lower index than the second host.
         *
         * @note Overrides the {@link Comparator#compare(Object, Object)} method.
         * @param h1
         * @param h2
         * @return
         */
        @Override
        public int compare(HostResources h1, HostResources h2) {
            return (int) (h1.getScore() - h2.getScore());
        }
    }

    @SuppressWarnings("unchecked")
    private Comparator<HostResources> reversedComparator = ComparatorUtils.reversedComparator(hostUpwardScoreComparator);

    /**
     * It sorts hosts by downward score. Hosts with higher score are positioned on
     * lower indexes of the list.
     *
     * @param
     * @return sorted list of HostResources
     */
    protected void sortHostsDownwardScore(List<HostResources> hosts) {
        Collections.sort(hosts, reversedComparator);
    }

    /**
     * It calculates each host score using the {@link #calculateHostScore(HostResources)} method.
     *
     * @param hosts
     * @param vmsProfile
     */
    protected List<HostResources> setEachHostScore(List<HostResources> hosts) {
        for (HostResources host : hosts) {
            host.setScore(calculateHostScore(host));
        }
        return hosts;
    }

    /**
     * This method calculates the host score based on its resources amount considering the
     * {@link HostProfiler}.
     * It calculates the profile using {@link HostProfiler#hostVMsResourceProportion(HostResources)}
     * method; then returns the result of (cpu speed profile * number of cpus profile * memory
     * profile)
     *
     * @param host
     */
    protected double calculateHostScore(HostResources host) {
        HostProfile hostProfile = profiler.hostVMsResourceProportion(host);
        return hostProfile.getCpuSpeedProfile() * hostProfile.getCpusProfile() * hostProfile.getMemoryProfile();
    }

    /**
     * It clones the given list using the {@link #cloneListOfHosts(List)} method; sets each host
     * score with {@link #setEachHostScore(List)} method; returns the resulting list from
     * {@link #sortHostsDownwardScore(List)} method.
     */
    @Override
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts) {
        List<HostResources> idleHostSorted = cloneListOfHosts(idleHosts);
        sortHostsDownwardScore(setEachHostScore(idleHostSorted));
        return idleHostSorted;
    }

    /**
     * It calculates the expected CPU usage percentage (using
     * {@link #getCloudExpetedCpuUsageAfterHostShutdown(HostResources, CloudResources)}) in case of
     * power off the host; it calculates the expected memory usage percentage (using
     * {@link #getCloudExpetedMemoryUsagePercentageAfterHostShutdown(HostResources, CloudResources)}
     * method)
     * in case of power off the host. If some of both expected usage percentage result is higher
     * than 70%, then it returns false;
     *
     * @note it first tests if expectedCpuUsagePercentage > 70%, if returns true then does not
     *       waists time testing if the expectedMemoryUsagePercentage < 70%
     */
    @Override
    protected boolean internalCanPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources) {
        double expectedCpuUsagePercentage = getCloudExpetedCpuUsageAfterHostShutdown(hostToPowerOff, cloudResources);
        if (expectedCpuUsagePercentage > 0.7) {
            return false;
        }
        double expectedMemoryUsagePercentage = getCloudExpetedMemoryUsagePercentageAfterHostShutdown(hostToPowerOff, cloudResources);
        return expectedMemoryUsagePercentage < 0.7;
    }

    /**
     * It calculates the cloud expected memory usage percentage after shutting down the given host
     * (by
     * dividing the used memory by the expected after shutdown the given host).
     *
     * @param hostToPowerOff
     * @param cloudResources
     * @return
     */
    protected double getCloudExpetedMemoryUsagePercentageAfterHostShutdown(HostResources hostToPowerOff, CloudResources cloudResources) {
        double hostMemory = (hostToPowerOff.getTotalMemoryInBytes() / BYTES_TO_MEGA_BYTES) * hostToPowerOff.getMemoryOverprovisioning();
        double expectedCloudMemoryAfterShutDown = (cloudResources.getMemoryInBytes() / BYTES_TO_MEGA_BYTES) - hostMemory;
        return (cloudResources.getUsedMemory() / BYTES_TO_MEGA_BYTES) / expectedCloudMemoryAfterShutDown;
    }

    /**
     * It calculates the cloud expected cpu usage percentage after shutting down the given host
     * (dividing the used cpu by the expected after shutdown the given host).
     *
     * @param hostToPowerOff
     * @param cloudResources
     * @return
     */
    protected double getCloudExpetedCpuUsageAfterHostShutdown(HostResources hostToPowerOff, CloudResources cloudResources) {
        double hostCpu = hostToPowerOff.getSpeed() * hostToPowerOff.getCpus() * hostToPowerOff.getCpuOverprovisioning();
        double expectedcloudCpuAfterShutDown = getCloudCpuCapacity(cloudResources) - hostCpu;
        return cloudResources.getUsedCpu() / expectedcloudCpuAfterShutDown;
    }

    /**
     * It returns the cloud cpu capacity ({@link CloudResources#getCpuSpeed()} *
     * {@link CloudResources#getCpus()}).
     *
     * @param cloudResources
     * @return
     */
    protected float getCloudCpuCapacity(CloudResources cloudResources) {
        return cloudResources.getCpuSpeed() * cloudResources.getCpus();
    }

    /**
     * It calculates the current CPU usage percentage (using
     * {@link #cloudCpuUsagePercentage(CloudResources)} method); it calculates the current memory
     * usage percentage (using {@link #cloudMemoryUsagePercentage(CloudResources)} method). If some
     * of both usage percentage is higher than 70%, then it returns false.
     *
     * @note it first tests if expectedCpuUsagePercentage > 70%, if returns true then does not
     *       waists time testing if the expectedMemoryUsagePercentage < 70%
     */
    @Override
    protected boolean internalCanPowerOffAnotherHostInCloud(CloudResources cloudResources) {
        double cpuUsagePercentage = cloudCpuUsagePercentage(cloudResources);
        if (cpuUsagePercentage > 0.7) {
            return false;
        }
        double memoryUsagePercentage = cloudMemoryUsagePercentage(cloudResources);
        return memoryUsagePercentage < 0.7;
    }

    /**
     * This method returns the cloud memory usage percentage ({@link CloudResources#getUsedMemory()}
     * /
     * {@link CloudResources#getMemoryInBytes()}).
     *
     * @param cloudResources
     * @return
     */
    protected double cloudMemoryUsagePercentage(CloudResources cloudResources) {
        double cloudMemory = cloudResources.getMemoryInBytes() / BYTES_TO_MEGA_BYTES;
        double cloudUsedMemory = cloudResources.getUsedMemory() / BYTES_TO_MEGA_BYTES;
        return (cloudUsedMemory / cloudMemory);
    }

    /**
     * It returns the cloud cpu usage percentage ({@link CloudResources#getUsedCpu()} /
     * {@link CloudResources#getCpuCapacity()}).
     *
     * @param cloudResources
     * @return
     */
    protected double cloudCpuUsagePercentage(CloudResources cloudResources) {
        double cloudCpu = getCloudCpuCapacity(cloudResources);
        double cloudUsedCpu = cloudResources.getUsedCpu();
        return (cloudUsedCpu / cloudCpu);
    }

}
