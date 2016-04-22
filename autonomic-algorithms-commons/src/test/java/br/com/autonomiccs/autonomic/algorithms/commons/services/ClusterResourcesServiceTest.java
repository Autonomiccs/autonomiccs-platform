
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;

public class ClusterResourcesServiceTest {

    protected final static int MEGA_BYTES_TO_BYTES = 1000000;
    protected ClusterResourcesService clusterResourcesService = new ClusterResourcesService();
    protected Random random = new Random();

    private HostResourcesTestUtils hostResourcesUtils = new HostResourcesTestUtils();

    @Test
    public void createClusterResourcesUpOneHostTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(1);
        ClusterResourcesUp cluster = clusterResourcesService.createClusterResourcesUp(random.nextLong(), "cluster", hosts);

        Assert.assertEquals(512, cluster.getUsedMemory());
        Assert.assertEquals(1000l, cluster.getUsedCpu());

        checkCpuAndMemory(cluster);
    }

    @Test
    public void clusterResourcesAvailableToStartTest() {
        List<HostResources> hosts = createClusterHomogeneousHosts(1);
        ClusterResourcesAvailableToStart cluster = clusterResourcesService.createClusterResourcesAvailableToStart(random.nextLong(), "cluster", hosts);

        checkCpuAndMemory(cluster);

        Assert.assertEquals(hosts, cluster.getHostsToStart());
    }

    private void checkCpuAndMemory(ClusterResources cluster) {
        Assert.assertEquals(2000l, cluster.getCpuSpeed());
        Assert.assertEquals(8192l * MEGA_BYTES_TO_BYTES, cluster.getMemoryInBytes());
        Assert.assertEquals(4, cluster.getCpus());
    }

    protected List<HostResources> createClusterHomogeneousHosts(int numberOfHosts) {
        List<HostResources> hosts = new ArrayList<HostResources>();
        for (int i = 0; i < numberOfHosts; i++) {
            HostResources host = createHostResources(random.nextLong(), 8192, 4, 2000l);
            hostResourcesUtils.createHostWithSmallVms(host, 1);
            hosts.add(host);
        }
        return hosts;
    }

    private HostResources createHostResources(long hostId, long totalMemory, int cpus, long cpuSpeed) {
        HostResources h = new HostResources();
        h.setHostId(hostId);
        h.setMemoryOverprovisioning(1);
        h.setCpuOverprovisioning(1);
        h.setUsedCpu(0l);
        h.setTotalMemoryInBytes(totalMemory * MEGA_BYTES_TO_BYTES);
        h.setUsedMemoryInMegaBytes(0l);
        h.setCpus(cpus);
        h.setSpeed(cpuSpeed);

        return h;
    }
}
