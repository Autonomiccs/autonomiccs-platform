
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

/**
 * Contains the VM resources (vm id, cpus, cpu speed and memory).
 */
public class VmResources implements Cloneable {
    private long vmId;
    private int cpus;
    private long cpuSpeed;
    private long memory;

    public VmResources(long id, int cpus, long cpuSpeed, long memory) {
        this.vmId = id;
        this.cpus = cpus;
        this.cpuSpeed = cpuSpeed;
        this.memory = memory;
    }

    public int getNumberOfCpus() {
        return cpus;
    }

    public long getVmId() {
        return vmId;
    }

    public long getCpuSpeed() {
        return cpuSpeed;
    }

    public long getMemoryInMegaBytes() {
        return memory;
    }

    public void setVmId(long vmId) {
        this.vmId = vmId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
