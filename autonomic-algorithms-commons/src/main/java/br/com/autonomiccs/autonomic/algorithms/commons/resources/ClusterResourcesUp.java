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
package br.com.autonomiccs.autonomic.algorithms.commons.resources;

import java.util.List;

/**
 * This object represents the {@link HostResources} that are running in the
 * cluster. TODO ver nome melhor
 */
public class ClusterResourcesUp extends ClusterResources {

    private List<HostResources> hostsResources;
    private long usedCpu, usedMemory;

    public ClusterResourcesUp(long clusterId, String clusterName, long cpuSpeed, long usedCpu, int cpus, long memory, long usedMemory, List<HostResources> hostsResources) {
        super(clusterId, clusterName, cpuSpeed, cpus, memory);
        this.hostsResources = hostsResources;
        this.usedCpu = usedCpu;
        this.usedMemory = usedMemory;
    }

    public void setHostsResources(List<HostResources> hostsList) {
        this.hostsResources = hostsList;
    }

    public List<HostResources> getHostsResources() {
        return hostsResources;
    }

    /**
     * @return sum of each server used Memory (found in capacityDao)
     */
    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    /**
     * @return sum of each server used CPU (found in capacityDao)
     */
    public long getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(long usedCPU) {
        this.usedCpu = usedCPU;
    }

    @Override
    public String toString() {
        return super.toString() + ", usedMemory= " + Long.toString(this.usedMemory) + ", usedCpu= " + Long.toString(this.usedCpu);
    }

}
