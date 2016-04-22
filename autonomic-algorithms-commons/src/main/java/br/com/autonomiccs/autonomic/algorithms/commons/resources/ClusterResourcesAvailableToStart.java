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
package br.com.autonomiccs.autonomic.algorithms.commons.resources;

import java.util.List;

/**
 * Represents the {@link HostResources} that were deactivated by the {@link ConsolidationManager}; thus they are inactive and available to be started.
 */
public class ClusterResourcesAvailableToStart extends ClusterResources {

    private List<HostResources> hostsToStart;

    public ClusterResourcesAvailableToStart(long clusterId, String clusterName, long cpuSpeed, int cpus, long memory, List<HostResources> hostsToStart) {
        super(clusterId, clusterName, cpuSpeed, cpus, memory);
        this.hostsToStart = hostsToStart;
    }

    public List<HostResources> getHostsToStart() {
        return hostsToStart;
    }

    public void setHostsToStart(List<HostResources> hostsToStart) {
        this.hostsToStart = hostsToStart;
    }

}
