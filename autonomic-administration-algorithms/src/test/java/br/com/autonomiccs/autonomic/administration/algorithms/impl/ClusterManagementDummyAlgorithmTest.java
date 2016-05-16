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
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.VmResources;

@RunWith(MockitoJUnitRunner.class)
public class ClusterManagementDummyAlgorithmTest {

    private ClusterManagementDummyAlgorithm spyAlgorithm;

    @Before
    public void setup() {
        spyAlgorithm = Mockito.spy(new ClusterManagementDummyAlgorithm());
    }

    @Test
    public void getClusterIntervalBetweenConsolidationTest() {
        int result = spyAlgorithm.getClusterIntervalBetweenConsolidation();
        Assert.assertEquals(Integer.MAX_VALUE, result);
    }

    @Test
    public void rankHostsTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        spyAlgorithm.rankHosts(hosts);
        Mockito.verify(spyAlgorithm).cloneListOfHosts(hosts);
    }

    @Test
    public void mapVMsToHost() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        Map<Long, HostResources> result = spyAlgorithm.mapVMsToHost(hosts);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void rankHostToPowerOffTest() {
        List<HostResources> hosts = new ArrayList<HostResources>();
        spyAlgorithm.rankHostToPowerOff(hosts);
        Mockito.verify(spyAlgorithm).cloneListOfHosts(hosts);
    }

    @Test
    public void canPowerOffHostTest() {
        HostResources hostToPowerOff = Mockito.mock(HostResources.class);
        CloudResources cloudResources = Mockito.mock(CloudResources.class);
        boolean result = spyAlgorithm.canPowerOffHost(hostToPowerOff, cloudResources);
        Assert.assertFalse(result);
    }

    @Test
    public void canPowerOffAnotherHostInCloudTest() {
        CloudResources cloudResources = Mockito.mock(CloudResources.class);
        boolean result = spyAlgorithm.canPowerOffAnotherHostInCloud(cloudResources);
        Assert.assertFalse(result);
    }

    @Test
    public void canHeuristicShutdownHostsTest() {
        boolean result = spyAlgorithm.canHeuristicShutdownHosts();
        Assert.assertFalse(result);
    }

    @Test
    public void cloneListOfHostsTest() {
        VmResources vm1Host1 = new VmResources(0, 0, 0, 0);
        List<VmResources> vmsHost = new ArrayList<VmResources>();
        vmsHost.add(vm1Host1);
        HostResources host = new HostResources();
        host.setHostId(1l);
        host.setVmsResources(vmsHost);
        List<HostResources> hosts = new ArrayList<HostResources>();
        hosts.add(host);

        List<HostResources> clonedHosts = spyAlgorithm.cloneListOfHosts(hosts);

        Assert.assertNotNull(clonedHosts);
        Assert.assertEquals(hosts.size(), clonedHosts.size());
        Assert.assertNotEquals(hosts, clonedHosts);

        HostResources clonedHost = clonedHosts.get(0);
        Assert.assertNotNull(clonedHost);
        Assert.assertEquals(host.getHostId(), clonedHost.getHostId());
        Assert.assertNotEquals(host, clonedHost);

        Assert.assertNotNull(clonedHost.getVmsResources());
        Assert.assertEquals(vmsHost.size(), clonedHost.getVmsResources().size());
        Assert.assertEquals(vmsHost.get(0), clonedHost.getVmsResources().get(0));

        Mockito.verify(spyAlgorithm).cloneHostVmsList(host);
    }

    @Test
    public void cloneHostVmsListTest() {
        HostResources host = Mockito.mock(HostResources.class);
        List<VmResources> vms = new ArrayList<VmResources>();
        VmResources vm = Mockito.mock(VmResources.class);
        vms.add(vm);
        Mockito.when(host.getVmsResources()).thenReturn(vms);

        List<VmResources> result = spyAlgorithm.cloneHostVmsList(host);

        Assert.assertNotNull(result);
        Assert.assertEquals(vms.size(), result.size());
        Assert.assertEquals(vms, result);
        Assert.assertEquals(vm, result.get(0));
        Assert.assertFalse(vms == result);
    }

}
