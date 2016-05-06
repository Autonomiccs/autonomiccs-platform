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
package br.com.autonomiccs.autonomic.administration.plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.administration.plugin.hypervisors.HypervisorHost;
import br.com.autonomiccs.autonomic.administration.plugin.hypervisors.xenserver.XenHypervisor;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;

@RunWith(MockitoJUnitRunner.class)
public class HypervisorManagerTest {

    @Spy
    @InjectMocks
    private HypervisorManager hypervisorManager;
    @Mock
    private HostService hostService;
    @Mock
    private List<HypervisorHost> hypervisorHosts;

    private HypervisorHost hypervisor;

    @Before
    public void setup() {
        hypervisor = Mockito.mock(XenHypervisor.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shutdownHostTestFull() {
        HostVO host = setTestOfShutdownHost(Host.Type.Routing, true);
        hypervisorManager.shutdownHost(host);
        verifyShutdownHostCalledMethods(1, 1);
    }

    @Test
    public void shutdownHostTestNotRoutingHost() {
        HostVO host = setTestOfShutdownHost(Host.Type.ConsoleProxy, true);
        hypervisorManager.shutdownHost(host);
        verifyShutdownHostCalledMethods(0, 0);
    }

    @Test(expected = CloudRuntimeException.class)
    public void shutdownHostTestNotXenServer() {
        HostVO host = setTestOfShutdownHost(Host.Type.Routing, false);
        hypervisorManager.shutdownHost(host);
        verifyShutdownHostCalledMethods(1, 0);
    }

    @Test
    public void shutdownTest() {
        HostVO host = new HostVO("");
        Mockito.doNothing().when(hostService).loadHostDetails(Mockito.any(HostVO.class));
        Mockito.doNothing().when(hypervisor).shutdownHost(Mockito.any(HostVO.class));
        Mockito.doNothing().when(hostService).markHostAsShutdownByAdministrationAgent(Mockito.anyLong());

        hypervisorManager.shutdown(host, hypervisor);

        InOrder inOrder = Mockito.inOrder(hostService);
        inOrder.verify(hostService).loadHostDetails(Mockito.any(HostVO.class));
        Mockito.verify(hypervisor).shutdownHost(Mockito.any(HostVO.class));
        inOrder.verify(hostService).markHostAsShutdownByAdministrationAgent(Mockito.anyLong());
    }

    private void verifyShutdownHostCalledMethods(int supportsHypervisorTimes, int shutdownTimes) {
        Mockito.verify(hypervisor, Mockito.times(supportsHypervisorTimes)).supportsHypervisor(Mockito.any(HypervisorType.class));
        Mockito.verify(hypervisorManager, Mockito.times(shutdownTimes)).shutdown(Mockito.any(HostVO.class), Mockito.any(HypervisorHost.class));
    }

    private HostVO setTestOfShutdownHost(Host.Type hostType, boolean supportsHypervisor) {
        HostVO host = new HostVO("");
        host.setHypervisorType(HypervisorType.XenServer);
        host.setType(hostType);

        Mockito.doReturn(supportsHypervisor).when(hypervisor).supportsHypervisor(Mockito.any(HypervisorType.class));
        List<HypervisorHost> hypervisorList = new ArrayList<>();
        hypervisorList.add(hypervisor);
        hypervisorManager.hypervisorHosts = hypervisorList;
        Mockito.doNothing().when(hypervisorManager).shutdown(Mockito.any(HostVO.class), Mockito.any(HypervisorHost.class));
        return host;
    }

}
