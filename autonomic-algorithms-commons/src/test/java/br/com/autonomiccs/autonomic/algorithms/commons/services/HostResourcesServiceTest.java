
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

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.VMInstanceDao;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.VmResources;

@RunWith(MockitoJUnitRunner.class)
public class HostResourcesServiceTest {

    @InjectMocks
    private HostResourcesService hostResourcesService;
    @Mock
    private ServiceOfferingDao serviceOfferingDao;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private VMInstanceDao vmInstanceDao;

    private Random random = new Random();

    private HostResourcesTestUtils hostResourcesUtils = new HostResourcesTestUtils();

    private VMInstanceVO vmInstance;

    @Before
    public void beforeTest() {
        vmInstance = new VMInstanceVO(random.nextLong(), 1l, "name", "instanceName", com.cloud.vm.VirtualMachine.Type.User, 1l, HypervisorType.XenServer, 1l, 1l, 1l,
                1l, true, true, 1l);
    }

    @Test
    public void listVmsFromHostTest() {
        HostResources hostResources = Mockito.mock(HostResources.class);
        Mockito.when(hostResources.getHostId()).thenReturn(1l);

        ServiceOfferingVO serviceOfferingVO = new ServiceOfferingVO("name", 1, 512, 1000, 1000, 1000, true, "displayText", ProvisioningType.THIN, true, true, "tags", true,
                com.cloud.vm.VirtualMachine.Type.User, true);

        List<VMInstanceVO> vmsInstanceVO = createRunningVmIistanceVo(2);
        Mockito.when(vmInstanceDao.listByHostId(1l)).thenReturn(vmsInstanceVO);
        Mockito.when(serviceOfferingDao.findById(1l)).thenReturn(serviceOfferingVO);

        List<VmResources> vmResourcesList = hostResourcesService.listVmsFromHost(hostResources);

        Assert.assertEquals(2, vmResourcesList.size());
        Assert.assertEquals(1000l, vmResourcesList.get(0).getCpuSpeed());
        Assert.assertEquals(512l, vmResourcesList.get(1).getMemoryInMegaBytes());
        Assert.assertEquals(1, vmResourcesList.get(0).getNumberOfCpus());
    }

    @Test
    public void createHostResourcesTest() {
        HostVO hostVo = new HostVO("");
        HostResources host = new HostResources();

        HostResourcesService spy = Mockito.spy(new HostResourcesService());
        Mockito.doReturn(host).when(spy).createAndConfigureHostResources(hostVo);

        List<VmResources> vms = hostResourcesUtils.createSmallVms(1);
        Mockito.doReturn(vms).when(spy).listVmsFromHost(host);

        Mockito.doNothing().when(spy).calculateHostResourcesAllocated(host);
        Mockito.doNothing().when(spy).loadHostOverprovisioningFactors(host);

        HostResources hostResourcesReturned = spy.createHostResources(hostVo);
        Assert.assertEquals(vms, hostResourcesReturned.getVmsResources());

        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy).createAndConfigureHostResources(hostVo);
        inOrder.verify(spy).listVmsFromHost(host);
        inOrder.verify(spy).calculateHostResourcesAllocated(host);
        inOrder.verify(spy).loadHostOverprovisioningFactors(host);
    }

    @Test
    public void createAndConfigureHostResourcesTest() {
        HostVO hostVo = Mockito.mock(HostVO.class);
        Mockito.when(hostVo.getId()).thenReturn(1l);
        Mockito.when(hostVo.getName()).thenReturn("test");
        Mockito.when(hostVo.getSpeed()).thenReturn(2000l);
        Mockito.when(hostVo.getCpus()).thenReturn(4);
        Mockito.when(hostVo.getTotalMemory()).thenReturn(8000l);

        HostResources host = hostResourcesService.createAndConfigureHostResources(hostVo);

        Assert.assertEquals(1l, host.getHostId());
        Assert.assertEquals("test", host.getHostName());
        Assert.assertEquals(2000l, host.getSpeed());
        Assert.assertEquals(4, host.getCpus().intValue());
        Assert.assertEquals(8000l, host.getTotalMemoryInBytes());
    }

    @Test
    public void calculateHostResourcesAllocatedTest() {
        HostResources hostResources = new HostResources();
        hostResourcesUtils.createHostWithSmallVms(hostResources, 1);
        hostResourcesService.calculateHostResourcesAllocated(hostResources);

        Assert.assertEquals(512l, hostResources.getUsedMemoryInMegaBytes());
        Assert.assertEquals(1000l, hostResources.getUsedCpu());
    }

    @Test
    public void isVmStateRunningTest() {
        vmInstance.setState(VirtualMachine.State.Running);
        Assert.assertTrue(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateMigratingTest() {
        vmInstance.setState(VirtualMachine.State.Migrating);
        Assert.assertTrue(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateStartingTest() {
        vmInstance.setState(VirtualMachine.State.Starting);
        Assert.assertTrue(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateDestroyedTest() {
        vmInstance.setState(VirtualMachine.State.Destroyed);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateErrorTest() {
        vmInstance.setState(VirtualMachine.State.Error);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateExpungingTest() {
        vmInstance.setState(VirtualMachine.State.Expunging);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateShutdownedTest() {
        vmInstance.setState(VirtualMachine.State.Shutdowned);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateStoppingTest() {
        vmInstance.setState(VirtualMachine.State.Stopping);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void isVmStateUnknownTest() {
        vmInstance.setState(VirtualMachine.State.Unknown);
        Assert.assertFalse(hostResourcesService.isVmCurrentlyUsingHostResource(vmInstance));
    }

    @Test
    public void loadHostOverprovisioningFactorsTest() {
        HostResources hostResources = new HostResources();

        ConfigurationVO configurationVOCpu = Mockito.mock(ConfigurationVO.class);
        ConfigurationVO configurationVOMem = Mockito.mock(ConfigurationVO.class);

        Mockito.when(configurationDao.findByName("cpu.overprovisioning.factor")).thenReturn(configurationVOCpu);
        Mockito.when(configurationVOCpu.getValue()).thenReturn("1.5");

        Mockito.when(configurationDao.findByName("mem.overprovisioning.factor")).thenReturn(configurationVOMem);
        Mockito.when(configurationVOMem.getValue()).thenReturn("2.0");

        hostResourcesService.loadHostOverprovisioningFactors(hostResources);

        Assert.assertEquals(1.5, hostResources.getCpuOverprovisioning(), 0f);
        Assert.assertEquals(2.0, hostResources.getMemoryOverprovisioning(), 0f);
    }

    private List<VMInstanceVO> createRunningVmIistanceVo(int amount) {
        List<VMInstanceVO> vms = new ArrayList<VMInstanceVO>();
        for(int i = 0; i<amount; i++) {
            VMInstanceVO vm = new VMInstanceVO(random.nextLong(), 1l, "name", "instanceName", com.cloud.vm.VirtualMachine.Type.User, 1l, HypervisorType.XenServer, 1l, 1l, 1l, 1l,
                    true, true, 1l);
            vm.setState(VirtualMachine.State.Running);
            vms.add(vm);
        }
        return vms;
    }
}
