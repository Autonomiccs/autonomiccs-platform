
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
package br.com.autonomiccs.autonomic.allocation.algorithms.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This class provides heuristics for calculating priority in hosts and clusters VM allocation.
 */
public class ScoredClustersAllocationAlgorithmPreferenceForSmallHosts extends AllocationAlgorithmBase {

    /**
     * It clones the given list of clusters (with {@link #cloneListOfClusters(List)}); after that, it  sets each
     * cluster score using {@link #setClustersScore(List)}, and then it returns the sorted list (using
     * {@link #sortClustersDownwardScore(List)}.
     */
    @Override
    public List<ClusterResourcesAvailableToStart> rankClustersToAllocation(List<ClusterResourcesAvailableToStart> clustersAvailableToStart) {
        List<ClusterResourcesAvailableToStart> sortedClustersAvailableToStart = cloneListOfClusters(clustersAvailableToStart);
        setClustersScore(sortedClustersAvailableToStart);
        sortClustersDownwardScore(sortedClustersAvailableToStart);
        return sortedClustersAvailableToStart;
    }

    /**
     * For each {@link ClusterResourcesAvailableToStart}, it calculates the given cluster score
     * using the {@link #calculateClusterScore(ClusterResources)} method.
     *
     * @param clusters
     * @return
     */
    protected void setClustersScore(List<ClusterResourcesAvailableToStart> clusters) {
        for (ClusterResourcesAvailableToStart cluster : clusters) {
            long clusterScore = calculateClusterScore(cluster);
            cluster.setScore(clusterScore);
        }
    }

    /**
     * It calculates the cluster score according its total resource amount (number of CPUs * CPU
     * speed
     * * memory).
     *
     * @param cluster
     * @return the score of the cluster
     */
    protected long calculateClusterScore(ClusterResources cluster) {
        long clusterCpu = cluster.getCpus() * cluster.getCpuSpeed();
        long clusterMemory = cluster.getMemoryInBytes();
        return clusterCpu * clusterMemory;
    }

    private ClusterUpwardComparator clusterUpwardScoreComparator = new ClusterUpwardComparator();

    /**
     * This class allows to compare clusters ({@link ClusterResources}) upward based on each cluster
     * score using the
     * {@link #compare(ClusterResources, ClusterResources)} method to return an integer, this class
     * implements {@link Comparator}.
     */
    protected static class ClusterUpwardComparator implements Comparator<ClusterResources> {
        /**
         * Given 2 (two) {@link ClusterResources}, it returns the result from the first cluster score
         * subtracted to the second cluster score, allowing to compare both clusters. If the result is
         * greater than zero, then the first cluster score is bigger than the second cluster score; thus,
         * the first cluster will be sorted in a lower index than the second one.
         *
         * @note Overrides the {@link Comparator#compare(Object, Object)} method.
         * @param c1
         * @param c2
         */
        @Override
        public int compare(ClusterResources c1, ClusterResources c2) {
            return (int) (c1.getScore() - c2.getScore());
        }
    }

    @SuppressWarnings("unchecked")
    private Comparator<ClusterResources> reversedComparator = ComparatorUtils.reversedComparator(clusterUpwardScoreComparator);

    /**
     * it sorts clusters by downward score. Clusters with higher score are positioned on lower
     * indexes
     * of the list.
     *
     * @param clusters
     */
    protected void sortClustersDownwardScore(List<ClusterResourcesAvailableToStart> clusters) {
        Collections.sort(clusters, reversedComparator);
    }

    /**
     * it ranks hosts to be started. It clones the given list (using {@link #cloneListOfHosts(List)}
     * ),
     * sets each host score using {@link #setEachHostScore(List)} and sorts the list using
     * {@link #sortHosts(List)} method.
     */
    @Override
    public List<HostResources> rankHostsToStart(List<HostResources> hostsResources) {
        List<HostResources> sortedHosts = cloneListOfHosts(hostsResources);
        setEachHostScore(sortedHosts);
        sortHosts(sortedHosts);
        return sortedHosts;
    }

    /**
     * it sorts hosts upward (using the {@link #sortHostsUpwardScore(List)} method) based on their
     * score.
     *
     * @param hosts
     */
    protected void sortHosts(List<HostResources> hosts) {
        sortHostsUpwardScore(hosts);
    }

    /**
     * It set each host score using the {@link #calculateHostScore(HostResources)} method.
     *
     * @param hosts
     * @return {@link List} of {@link HostResources}
     */
    protected List<HostResources> setEachHostScore(List<HostResources> hosts) {
        for (HostResources hostToStart : hosts) {
            double hostScore = calculateHostScore(hostToStart);
            hostToStart.setScore(hostScore);
        }
        return hosts;
    }

    /**
     * It calculates the host score based on its resource amount (cpu speed * number of cpus * cpu
     * over-provisioning * memory * memory over-provisioning).
     *
     * @param hostToStart
     */
    protected double calculateHostScore(HostResources hostToStart) {
        double hostCpu = hostToStart.getSpeed() * hostToStart.getCpus() * hostToStart.getCpuOverprovisioning();
        double hostMemory = hostToStart.getTotalMemoryInBytes() * hostToStart.getMemoryOverprovisioning();
        return hostCpu * hostMemory;
    }

    protected HostUpwardComparator hostUpwardScoreComparator = new HostUpwardComparator();

    /**
     * It sorts hosts upward based on their score. It uses the
     * {@link Collections#sort(List, Comparator)}, where the comparator is the
     * {@link HostUpwardComparator}.
     *
     * @param hosts
     */
    protected void sortHostsUpwardScore(List<HostResources> hosts) {
        Collections.sort(hosts, hostUpwardScoreComparator);
    }

    /**
     * This class allows to compare Hosts upward based on each host score using the
     * {@link #compare(HostResources, HostResources)} method to return an integer, this class
     * implements {@link Comparator}.
     */
    public static class HostUpwardComparator implements Comparator<HostResources> {
        /**
         * Given 2 (two) {@link HostResources}, it returns the result from the first host score
         * subtracted to the second host score, allowing to compare both hosts. If the result is
         * greater than zero, then the first host score is bigger than the second host score; thus
         * the first host will be sorted in a lower index than the second host.
         *
         * @note Overrides the {@link Comparator#compare(Object, Object)} method.
         * @param h1
         * @param h2
         */
        @Override
        public int compare(HostResources h1, HostResources h2) {
            return Double.compare(h1.getScore(), h2.getScore());
        }
    }

}
