
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

import java.util.ArrayList;
import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.allocation.algorithms.AllocationAlgorithm;

/**
 * This class provides basic heuristics to be extended by specialized allocation algorithms.
 */
public class AllocationAlgorithmBase implements AllocationAlgorithm {

    /**
     * It clones the given {@link List} of {@link ClusterResourcesAvailableToStart} to a new list.
     * The
     * new list allows to operate into same objects (from the cloned list) without alter the given
     * list.
     *
     * @param clusters
     * @return {@link List} of {@link ClusterResourcesAvailableToStart}
     */
    @SuppressWarnings("unchecked")
    protected List<ClusterResourcesAvailableToStart> cloneListOfClusters(List<ClusterResourcesAvailableToStart> clusters) {
        return (List<ClusterResourcesAvailableToStart>) ((ArrayList<ClusterResourcesAvailableToStart>) clusters).clone();
    }

    /**
     * It clones the given {@link List} of {@link HostResources} to a new list. The new list allows
     * to operate into same objects (from the cloned list) without alter the given list.
     *
     * @param hosts
     * @return {@link List} of {@link HostResources}
     */
    @SuppressWarnings("unchecked")
    protected List<HostResources> cloneListOfHosts(List<HostResources> hosts) {
        return (List<HostResources>) ((ArrayList<HostResources>) hosts).clone();
    }

    /**
     * It uses the {@link #cloneListOfClusters(List)} method. It does not alter sequence from the
     * original list; thus, the ranking method just returns the original list.
     *
     * @return {@link List} of {@link ClusterResourcesAvailableToStart}
     */
    @Override
    public List<ClusterResourcesAvailableToStart> rankClustersToAllocation(List<ClusterResourcesAvailableToStart> clusters) {
        return cloneListOfClusters(clusters);
    }

    /**
     * This method always returns false.
     */
    @Override
    public boolean needsToActivateHost(CloudResources cloudCapacity) {
        return false;
    }

    /**
     * It uses the {@link #cloneListOfHosts(List)} method. It does not alter sequence from the
     * original
     * list; thus, the ranking method just returns the original list.
     *
     * @return {@link List} of {@link HostResources}
     */
    @Override
    public List<HostResources> rankHostsToStart(List<HostResources> hostsResources) {
        return cloneListOfHosts(hostsResources);
    }

}
