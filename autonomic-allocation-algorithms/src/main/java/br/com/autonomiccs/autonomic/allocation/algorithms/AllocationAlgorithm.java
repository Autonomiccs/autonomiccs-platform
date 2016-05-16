
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
package br.com.autonomiccs.autonomic.allocation.algorithms;

import java.util.List;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;

/**
 * This interface provides the basic methods that are used by the allocation
 * agent of the CloudStack autonomous consolidation plugin.
 *
 * The implemented algorithm should have some kind of "intelligence" to define
 * which servers to power on; first it has to choose a cluster to start the
 * process; then it chooses servers to be activated.
 */
public interface AllocationAlgorithm {

    /**
     * The lower index of the lists the higher priority to receive a VM that is
     * being deployed.
     *
     * @param List
     *            of clusters
     * @return Ordered list of Clusters
     */
    public List<ClusterResourcesAvailableToStart> rankClustersToAllocation(List<ClusterResourcesAvailableToStart> clusters);

    /**
     * Checks if it is needed to activate hosts of the cluster on after the
     * deployment of a VM.
     *
     * @param cloudCapacity
     * @return True if the available (idle) resources are not sufficient.
     */
    public boolean needsToActivateHost(CloudResources cloudCapacity);

    /**
     * Order the deactivated hosts list, the first ones of the lists will be
     * activated first.
     *
     * @param deactivated
     *            hosts to be enabled
     * @return deactivated hosts ordered to be started
     */
    public List<HostResources> rankHostsToStart(List<HostResources> hostsResources);

}
