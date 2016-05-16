
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
package br.com.autonomiccs.autonomic.algorithms.commons.beans;

import java.util.List;

/**
 * Represents all the resources in the cloud (number of cpus, cpu speed, cpu
 * usage, memory, used memory and all the {@link ClusterResources} in this
 * cloud.
 */
public class CloudResources {

    private int cpus;
    private long usedCpu, cpuSpeed, usedMemory, memory;
    private List<ClusterResourcesUp> clusters;

    public CloudResources(List<ClusterResourcesUp> clusters, long uMemory, long mem, long uCpu, long cpuFreq, int cpuN) {
        this.clusters = clusters;
        this.usedMemory = uMemory;
        this.memory = mem;
        this.usedCpu = uCpu;
        this.cpuSpeed = cpuFreq;
        this.cpus = cpuN;
    }

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public long getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(long usedCpu) {
        this.usedCpu = usedCpu;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public void setCpuSpeed(long cpuSpeed) {
        this.cpuSpeed = cpuSpeed;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getMemoryInBytes() {
        return memory;
    }

    public void setMemoryInBytes(long memory) {
        this.memory = memory;
    }

    public List<ClusterResourcesUp> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterResourcesUp> clusters) {
        this.clusters = clusters;
    }

}
