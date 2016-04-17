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
package br.com.autonomiccs.autonomic.administration.algorithms.impl;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

@RunWith(MockitoJUnitRunner.class)
public class ConsolidationAlgorithmBaseTest extends ConsolidationAlgorithmsTest {

    private ConsolidationAlgorithmBase spyAlgorithm;

    @Before
    public void setup() {
        spyAlgorithm = Mockito.spy(new ConsolidationAlgorithmBase());
    }

    @Test
    public void testBytesToMegaBytesVariable() {
        Assert.assertEquals(1000000, ConsolidationAlgorithmBase.BYTES_TO_MEGA_BYTES);
    }

    @Test
    public void getClusterIntervalBetweenConsolidationTest() {
        Assert.assertEquals(600, spyAlgorithm.getClusterIntervalBetweenConsolidation());
    }

    @Test
    public void mapVMsToHost() {
        List<HostResources> hosts = createHosts();
        Map<Long, HostResources> migrations = spyAlgorithm.mapVMsToHost(hosts);

        long id = hosts.get(1).getVmsResources().get(0).getVmId();

        Assert.assertEquals(1, migrations.size());
        Assert.assertEquals(3000l, hosts.get(0).getUsedCpu());
        Assert.assertEquals(1500l, hosts.get(0).getUsedMemoryInMegaBytes());
        Assert.assertEquals(hosts.get(0).getHostId(), migrations.get(id).getHostId());

        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm).canMigrateVmToHost(hosts.get(1).getVmsResources().get(0), hosts.get(0));
        inOrder.verify(spyAlgorithm).updateHostUsedResources(hosts.get(1).getVmsResources().get(0), hosts.get(0));
    }

    @Test
    public void canMigrateVmToHostTestHostLessAmountOfCpusThanVm() {
        HostResources host = createHost(1, 1000l, 0l, 1000l, 500l);
        VmResources vm = new VmResources(0, 2, 1000l, 10l);
        Assert.assertFalse(spyAlgorithm.canMigrateVmToHost(vm, host));
        checkGetHostAvailableCpuAndMemoryExecution(host, 0, 0);
    }

    @Test
    public void canMigrateVmToHostTestHostLessCpusAndCpuSpeedThanVm() {
        HostResources host = createHost(2, 1l, 0l, 1000l, 500l);
        VmResources vm = new VmResources(0, 2, 1000l, 10l);
        Assert.assertFalse(spyAlgorithm.canMigrateVmToHost(vm, host));
        checkGetHostAvailableCpuAndMemoryExecution(host, 1, 0);
    }

    @Test
    public void canMigrateVmToHostTestHostLessMemoryThanVm() {
        HostResources host = createHost(2, 1000l, 0l, 1000l, 500l);
        VmResources vm = new VmResources(0, 1, 1000l, 1000l);
        Assert.assertFalse(spyAlgorithm.canMigrateVmToHost(vm, host));
        checkGetHostAvailableCpuAndMemoryExecution(host, 1, 1);
    }

    @Test
    public void canMigrateVmToHostTestHostHasResourcesToAllocateVm() {
        HostResources host = createHost(2, 1000l, 0l, 1000l, 0l);
        VmResources vm = new VmResources(0, 1, 1000l, 500l);
        Assert.assertTrue(spyAlgorithm.canMigrateVmToHost(vm, host));
        checkGetHostAvailableCpuAndMemoryExecution(host, 1, 1);
    }

    @Test
    public void getHostAvailableMemoryTest() {
        HostResources host = createHost(0, 0l, 0l, 1000l, 500l);
        Assert.assertEquals(500l, spyAlgorithm.getHostAvailableMemory(host));
    }

    @Test
    public void getHostTotalMemoryInMegaBytesTest() {
        HostResources host = createHost(0, 0l, 0l, 1000l, 500l);
        Assert.assertEquals(1000l, spyAlgorithm.getHostTotalMemoryInMegaBytes(host));
    }

    @Test
    public void getHostAvailableCpuTest() {
        HostResources host = createHost(1, 1000l, 500l, 0l, 0l);
        Assert.assertEquals(500l, spyAlgorithm.getHostAvailableCpu(host), 0.0);
    }

    @Test
    public void getHostAvailableCpu2CoresTest() {
        HostResources host = createHost(2, 1000l, 500l, 0l, 0l);
        Assert.assertEquals(1500l, spyAlgorithm.getHostAvailableCpu(host), 0.0);
    }

    @Test
    public void updateHostUsedResourcesTest() {
        HostResources host = createHost(2, 1000l, 0l, 1000l, 0l);
        VmResources vm = new VmResources(0, 1, 1000l, 500l);

        spyAlgorithm.updateHostUsedResources(vm, host);

        Assert.assertEquals(1000l, host.getUsedCpu());
        Assert.assertEquals(500l, host.getUsedMemoryInMegaBytes());
    }

    @Test
    public void canPowerOffHostTest() {
        HostResources host = createHost(2, 1000l, 0l, 1000l, 0l);
        CloudResources cloud = new CloudResources(null, 0, 0, 0, 0, 0);
        spyAlgorithm.canPowerOffHost(host, cloud);
        Mockito.verify(spyAlgorithm).internalCanPowerOffHost(host, cloud);
    }

    @Test
    public void internalCanPowerOffHostTest() {
        HostResources host = createHost(2, 1000l, 0l, 1000l, 0l);
        CloudResources cloud = new CloudResources(null, 0, 0, 0, 0, 0);
        Assert.assertTrue(spyAlgorithm.internalCanPowerOffHost(host, cloud));
    }

    @Test
    public void canPowerOffAnotherHostInCloudTest() {
        CloudResources cloud = new CloudResources(null, 0, 0, 0, 0, 0);
        spyAlgorithm.canPowerOffAnotherHostInCloud(cloud);
        Mockito.verify(spyAlgorithm).internalCanPowerOffAnotherHostInCloud(cloud);
    }

    @Test
    public void internalCanPowerOffAnotherHostInCloudTest() {
        CloudResources cloud = new CloudResources(null, 0, 0, 0, 0, 0);
        Assert.assertTrue(spyAlgorithm.internalCanPowerOffAnotherHostInCloud(cloud));
    }

    @Test
    public void canHeuristicShutdownHostsTest() {
        Assert.assertTrue(spyAlgorithm.canHeuristicShutdownHosts());
    }

    private void checkGetHostAvailableCpuAndMemoryExecution(HostResources host, int timesAvailableCpu, int timesAvailableMemory) {
        InOrder inOrder = Mockito.inOrder(spyAlgorithm);
        inOrder.verify(spyAlgorithm, Mockito.times(timesAvailableCpu)).getHostAvailableCpu(host);
        inOrder.verify(spyAlgorithm, Mockito.times(timesAvailableMemory)).getHostAvailableMemory(host);
    }

}
