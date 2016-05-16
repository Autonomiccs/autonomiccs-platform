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
package br.com.autonomiccs.autonomic.administration.algorithms;

import java.util.List;
import java.util.Map;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;

/**
 * This interface provides the basic methods that are used by the consolidation
 * agent of the CloudStack autonomous consolidation plugin. It provides the
 * "intelligence" for the framework, deciding which hosts to be deactivated,
 * clusters to be consolidated and creating the mapping of VMs. All of that is
 * done according to the knowledge that the implementation of this class will
 * provide for the environment.
 */
public interface ClusterAdministrationHeuristicAlgorithm {

    /**
     * @return minimum time interval (seconds) between consolidation of a cluster.
     * */
    public int getClusterIntervalBetweenConsolidation();

    /**
     * Rank a list of hosts based in some score function.
     * The hosts at the top of the lists will have priority to be kept active,
     * @param list of host
     * @return list of host
     * */
    public List<HostResources> rankHosts(List<HostResources> hosts);

    /**
     * Map VMs and hosts to be migrate in order to optimize the environment. The
     * returned {@link Map} should have a key as the VM 'id' and the object as
     * the {@link HostResources}.
     *
     * @param list
     *            of host (an ordered list of hosts) the top hosts have priority
     *            to be kept active
     * @return a list of migration, maps a set of VMs and their target host
     */
    public Map<Long, HostResources> mapVMsToHost(List<HostResources> rankedHosts);

    /**
     * Receive a list of active idle hosts that should be ordered. The top hosts
     * have priority to be deactivated.
     *
     * @param list
     *            of hosts candidates to power off
     * @return list of ordered hosts ordered, by priority
     */
    public List<HostResources> rankHostToPowerOff(List<HostResources> idleHosts);

    /**
     * This method checks if the agent can power off the host.
     *
     * @param resources
     *            of host to be powerOff.
     * @param sum
     *            of cluster resources (idle and total).
     * @return true if can power the host.
     */
    public boolean canPowerOffHost(HostResources hostToPowerOff, CloudResources cloudResources);

    /**
     * This method checks if the administration agent can disable idle hosts given the whole cloud
     * environment current state.
     */
    boolean canPowerOffAnotherHostInCloud(CloudResources cloudResources);

    /**
     * It returns true if the heuristic can shutdown hosts; it returns false if the heuristic cannot
     * power off hosts.
     */
    boolean canHeuristicShutdownHosts();

}
