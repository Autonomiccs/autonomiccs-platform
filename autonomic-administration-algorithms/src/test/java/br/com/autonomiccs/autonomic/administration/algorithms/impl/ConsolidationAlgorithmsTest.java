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
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ObjectUtils;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

public class ConsolidationAlgorithmsTest {

    protected int BYTES_TO_MEGA_BYTES = 1000000;
    protected Random random = new Random();

    protected HostResources createHost(int cpus, long cpuSpeed, long usedCpu, long totalMemoryInBytes, long usedMemoryInMegaBytes) {
        HostResources host = new HostResources();
        host.setCpus(cpus);
        host.setSpeed(cpuSpeed);
        host.setUsedCpu(usedCpu);
        host.setTotalMemoryInBytes(totalMemoryInBytes * BYTES_TO_MEGA_BYTES);
        host.setUsedMemoryInMegaBytes(usedMemoryInMegaBytes);
        host.setCpuOverprovisioning(1);
        host.setMemoryOverprovisioning(1);
        return host;
    }

    protected List<HostResources> createHosts() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        HostResources host1 = createHost(5, 5000l, 2000l, 5000l, 1000l);
        host1.setHostId(2);
        host1.setVmsResources(createVms(2));
        hosts.add(host1);

        HostResources host2 = createHost(5, 5000l, 1000l, 5000l, 500l);
        host2.setHostId(2);
        host2.setVmsResources(createVms(1));
        hosts.add(host2);

        return hosts;
    }

    protected List<VmResources> createVms(int n) {
        List<VmResources> vms = new ArrayList<VmResources>();
        for (int i = 0; i < n; i++) {
            VmResources vm = new VmResources(random.nextLong(), 1, 1000l, 500l);
            vms.add(vm);
        }
        return vms;
    }

    protected List<HostResources> createClusterHomogeneousHosts(int numberOfHosts) {
        List<HostResources> hosts = new ArrayList<HostResources>();
        for (int i = 0; i < numberOfHosts; i++) {
            hosts.add(createHostWithSmallVms(1));
        }
        return hosts;
    }

    private HostResources createHostResources(long hostId, long totalMemory, int cpus, long cpuSpeed) {
        HostResources h = new HostResources();
        h.setHostId(hostId);
        h.setMemoryOverprovisioning(1);
        h.setCpuOverprovisioning(1);
        h.setUsedCpu(0l);
        h.setTotalMemoryInBytes(totalMemory * BYTES_TO_MEGA_BYTES);
        h.setUsedMemoryInMegaBytes(0l);
        h.setCpus(cpus);
        h.setSpeed(cpuSpeed);

        return h;
    }

    protected HostResources createHostWithSmallVms(int amount) {
        return createHostConfiguringVms(createSmallVms(amount));
    }

    protected HostResources createHostConfiguringVms(List<VmResources> vms) {
        long usedMemory = 0;
        long usedCpuSpeed = 0;
        for (VmResources vm : vms) {
            usedMemory += vm.getMemoryInMegaBytes();
            usedCpuSpeed += vm.getCpuSpeed() * vm.getNumberOfCpus();
        }

        HostResources host = createHostConfiguringUsedMemory(usedMemory);
        host.setVmsResources(vms);
        host.setUsedCpu(usedCpuSpeed);

        return host;
    }

    protected List<VmResources> createSmallVms(int amount) {
        return replicateVm(amount, createVmResourcesSmall());
    }

    private VmResources createVmResourcesSmall() {
        return new VmResources(random.nextLong(), 1, 1000, 512);
    }

    private List<VmResources> replicateVm(int amount, VmResources vm) {
        List<VmResources> vms = new ArrayList<VmResources>();
        for (int i = 0; i < amount; i++) {
            VmResources clonedVm = ObjectUtils.clone(vm);
            clonedVm.setVmId(random.nextLong());
            vms.add(clonedVm);
        }
        return vms;
    }

    protected HostResources createHostConfiguringUsedMemory(long usedMemory) {
        HostResources host = createHostResources(random.nextLong(), 8192, 4, 2000l);
        host.setUsedMemoryInMegaBytes(usedMemory);
        return host;
    }

    protected HostResources createEmptyHost() {
        return createHostConfiguringUsedMemory(0);
    }

    protected List<VmResources> createMediumVms(int amount) {
        return replicateVm(amount, createVmResourcesMedium());
    }

    protected HostResources createHostWithMediumVms(int amount) {
        return createHostConfiguringVms(createMediumVms(amount));
    }

    protected VmResources createVmResourcesMedium() {
        return new VmResources(random.nextLong(), 1, 1000, 1024);
    }

    protected List<VmResources> createHugeVms(int amount) {
        return replicateVm(amount, createVmResourcesHuge());
    }

    private VmResources createVmResourcesHuge() {
        return new VmResources(random.nextLong(), 1, 1000, 4096);
    }

    protected VmResources createVmResourcesBig() {
        return new VmResources(random.nextLong(), 1, 1000, 2048);
    }

}
