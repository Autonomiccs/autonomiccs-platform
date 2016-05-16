
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

import java.util.ArrayList;
import java.util.List;

public class HostResources implements Cloneable {

    private List<VmResources> vmsResources = new ArrayList<>();
    private float cpuOverprovisioning, memoryOverprovisioning;
    private long usedCpu, usedMemory, hostId, speed, totalMemory;
    private double score;
    private String hostName;
    private Integer cpus;

    public float getCpuOverprovisioning() {
        return cpuOverprovisioning;
    }
    public void setCpuOverprovisioning(float cpuOverprovisioning) {
        this.cpuOverprovisioning = cpuOverprovisioning;
    }
    public float getMemoryOverprovisioning() {
        return memoryOverprovisioning;
    }
    public void setMemoryOverprovisioning(float memoryOverprovisioning) {
        this.memoryOverprovisioning = memoryOverprovisioning;
    }

    public long getUsedMemoryInMegaBytes() {
        return this.usedMemory;
    }

    public void setUsedMemoryInMegaBytes(long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public long getUsedCpu() {
        return this.usedCpu;
    }
    public void setUsedCpu(long usedCPU) {
        this.usedCpu = usedCPU;
    }

    /**
     * @return score of a Host
     * */
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Returns a list of virtual machines that are allocated in this host (
     * {@link VmResources} {@link List}).
     *
     * @return
     */
    public List<VmResources> getVmsResources() {
        return vmsResources;
    }

    /**
     * Sets the virtual machines allocated in this host ({@link VmResources}
     * {@link List}).
     *
     * @param vmsResources
     */
    public void setVmsResources(List<VmResources> vmsResources) {
        this.vmsResources = vmsResources;
    }

    @Override
    public String toString() {
        return "hostId= " + Long.toString(this.hostId) + ", vmsInHost = " + Integer.toString(this.vmsResources.size()) + ", usedMemory= " + Double.toString(this.usedMemory)
                + ", memoryOverprovisioning= " + Float.toString(this.memoryOverprovisioning)
                + ", usedCpu= " + Double.toString(this.usedCpu) + ", cpuOverprovisioning= "+ Float.toString(this.cpuOverprovisioning);
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(long hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getCpus() {
        return cpus;
    }

    public void setCpus(Integer cpus) {
        this.cpus = cpus;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getTotalMemoryInBytes() {
        return totalMemory;
    }

    public void setTotalMemoryInBytes(long totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}