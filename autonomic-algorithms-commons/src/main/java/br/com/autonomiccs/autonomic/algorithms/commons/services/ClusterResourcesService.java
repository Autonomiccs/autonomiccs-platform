
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
package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

/**
 * This class manages operations over {@link ClusterResources} objects.
 */
@Component
public class ClusterResourcesService {

    /**
     * It aggregates the cluster {@link HostResources} that are running and returns a
     * {@link ClusterResourcesUp} object.
     *
     * @return {@link ClusterResources}
     */
    public ClusterResourcesUp createClusterResourcesUp(long clusterId, String clusterName, List<HostResources> hostsResources) {
        long clusterUsedCpu = 0, clusterCpuSpeed = 0, clusterMemory = 0, clusterUsedMemory = 0;
        int clusterCpus = 0;
        for (HostResources currentHost : hostsResources) {
            clusterMemory += currentHost.getTotalMemoryInBytes() * currentHost.getMemoryOverprovisioning();
            clusterUsedMemory += currentHost.getUsedMemoryInMegaBytes();
            clusterUsedCpu += currentHost.getUsedCpu();
            clusterCpuSpeed += currentHost.getSpeed() * currentHost.getCpuOverprovisioning();
            clusterCpus += currentHost.getCpus();
        }

        ClusterResourcesUp clusterResourcesUp = new ClusterResourcesUp(clusterId, clusterName, clusterCpuSpeed, clusterUsedCpu, clusterCpus, clusterMemory, clusterUsedMemory,
                hostsResources);

        return clusterResourcesUp;
    }

    /**
     * It creates a {@link ClusterResourcesAvailableToStart} with the cluster's
     * with zero CPU and memory usage (the host list being a {@link List} of
     * deactivated {@link HostResources}).
     *
     * @param clusterVO
     * @param hostsToStart
     * @return {@link ClusterResourcesAvailableToStart}
     */
    public ClusterResourcesAvailableToStart createClusterResourcesAvailableToStart(long clusterId, String clusterName, List<HostResources> hostsToStart) {
        long clusterCpuSpeed = 0, clusterMemory = 0;
        int clusterCpus = 0;
        for (HostResources currentHost : hostsToStart) {
            clusterMemory += currentHost.getTotalMemoryInBytes();
            clusterCpuSpeed += currentHost.getSpeed();
            clusterCpus += currentHost.getCpus();
        }

        return new ClusterResourcesAvailableToStart(clusterId, clusterName, clusterCpuSpeed, clusterCpus, clusterMemory, hostsToStart);
    }

}
