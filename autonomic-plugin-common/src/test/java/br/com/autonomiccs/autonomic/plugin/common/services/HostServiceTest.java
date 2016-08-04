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
package br.com.autonomiccs.autonomic.plugin.common.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.Host.Type;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceState;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;

@RunWith(MockitoJUnitRunner.class)
public class HostServiceTest {

    @Spy
    @InjectMocks
    private HostService spy;
    @Mock
    private HostJdbcDao hostDaoJdbc;
    @Mock
    private HostDao hostDao;
    @Mock
    private VMInstanceDao vmInstanceDao;

    @Test
    public void isHostUpAndEnabledTest() {
        executeIsHostUpAndEnabledTest(Status.Up, ResourceState.Enabled, 1, true);
    }

    @Test
    public void isHostUpAndEnabledTestStatusNotUp() {
        executeIsHostUpAndEnabledTest(Status.Connecting, ResourceState.Enabled, 0, false);
    }

    @Test
    public void isHostUpAndEnabledTestResourceStateNotEnabled() {
        executeIsHostUpAndEnabledTest(Status.Up, ResourceState.Creating, 1, false);
    }

    @Test
    public void isHostInMaintenanceErrorTest() throws Exception {
        executeResourceStateCheck(true, ResourceState.ErrorInMaintenance, "isHostInMaintenanceError");
    }

    @Test
    public void isHostInMaintenanceErrorTestResourceStateNotErrorInMaintenance() throws Exception {
        executeResourceStateCheck(false, ResourceState.PrepareForMaintenance, "isHostInMaintenanceError");
    }

    @Test
    public void isHostInPreparedForMaintenanceTest() throws Exception {
        executeResourceStateCheck(true, ResourceState.PrepareForMaintenance, "isHostInPreparedForMaintenance");
    }

    @Test
    public void isHostInPreparedForMaintenanceTestResourceStateNotPrepareForMaintenance() throws Exception {
        executeResourceStateCheck(false, ResourceState.Disabled, "isHostInPreparedForMaintenance");
    }

    @Test
    public void markHostAsShutdownByAdministrationAgentTest() {
        Mockito.doNothing().when(hostDaoJdbc).setAdministrationStatus(Mockito.eq(HostAdministrationStatus.ShutDownToConsolidate), Mockito.anyLong());
        spy.markHostAsShutdownByAdministrationAgent(0l);
        Mockito.verify(hostDaoJdbc).setAdministrationStatus(Mockito.eq(HostAdministrationStatus.ShutDownToConsolidate), Mockito.anyLong());
    }

    @Test
    public void loadHostDetailsTest() {
        HostVO host = Mockito.mock(HostVO.class);
        Mockito.doNothing().when(hostDao).loadDetails(Mockito.any(HostVO.class));
        spy.loadHostDetails(host);
        Mockito.verify(hostDao).loadDetails(Mockito.any(HostVO.class));
    }

    @Test
    public void getAllHypervisorsTypeInCloudTest() {
        List<HypervisorType> hypervisors = new ArrayList<>();
        hypervisors.add(HypervisorType.XenServer);
        hypervisors.add(HypervisorType.KVM);
        Mockito.doReturn(hypervisors).when(hostDaoJdbc).getAllHypervisorsTypeInCloud();

        List<HypervisorType> result = spy.getAllHypervisorsTypeInCloud();

        Mockito.verify(hostDaoJdbc).getAllHypervisorsTypeInCloud();
        Assert.assertEquals(hypervisors.size(), result.size());
        Assert.assertEquals(hypervisors.get(0), result.get(0));
        Assert.assertEquals(hypervisors.get(1), result.get(1));
    }

    @Test
    public void findHostByIdTest() {
        HostVO host = new HostVO("guid");
        Mockito.doReturn(host).when(hostDao).findById(Mockito.anyLong());
        HostVO result = spy.findHostById(0l);
        Mockito.verify(hostDao).findById(Mockito.anyLong());
        Assert.assertEquals(host, result);
    }

    @Test
    public void listAllHostsInClusterTest() {
        String str = null;
        List<HostVO> hosts = new ArrayList<>();
        HostVO host1 = Mockito.mock(HostVO.class);
        hosts.add(host1);
        HostVO host2 = Mockito.mock(HostVO.class);
        hosts.add(host2);
        Cluster cluster = Mockito.mock(Cluster.class);
        Mockito.when(cluster.getId()).thenReturn(0l);
        Mockito.when(cluster.getPodId()).thenReturn(0l);
        Mockito.when(cluster.getDataCenterId()).thenReturn(0l);
        Mockito.doReturn(hosts).when(hostDao).listAllUpAndEnabledNonHAHosts(Mockito.eq(Type.Routing), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(),
                Mockito.eq(str));

        spy.listAllHostsInCluster(cluster);

        Mockito.verify(cluster).getId();
        Mockito.verify(cluster).getPodId();
        Mockito.verify(cluster).getDataCenterId();
        Mockito.verify(hostDao).listAllUpAndEnabledNonHAHosts(Mockito.eq(Type.Routing), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.eq(str));
    }

    @Test
    public void listAllVmsFromHostTest() {
        List<VMInstanceVO> vms = new ArrayList<>();
        VMInstanceVO vm = Mockito.mock(VMInstanceVO.class);
        vms.add(vm);
        Mockito.doReturn(vms).when(vmInstanceDao).listByHostId(Mockito.anyLong());

        List<VMInstanceVO> result = spy.listAllVmsFromHost(0l);

        Mockito.verify(vmInstanceDao).listByHostId(Mockito.anyLong());
        Assert.assertEquals(vms, result);
        Assert.assertEquals(vms.get(0), vms.get(0));
        Assert.assertEquals(vms.size(), vms.size());
    }

    @Test
    public void updateHostPrivaceMacAddressTest() {
        HostVO host = Mockito.mock(HostVO.class);
        Mockito.doNothing().when(host).setPrivateMacAddress(Mockito.anyString());
        Mockito.when(host.getId()).thenReturn(0l);
        Mockito.doReturn(true).when(hostDao).update(Mockito.anyLong(), Mockito.any(HostVO.class));

        spy.updateHostPrivaceMacAddress(host, "privateMacAddress");

        Mockito.verify(host).setPrivateMacAddress(Mockito.anyString());
        Mockito.verify(host).getId();
        Mockito.verify(hostDao).update(Mockito.anyLong(), Mockito.any(HostVO.class));
    }

    @Test
    public void isHostDownTest() {
        executeIsHostDownTest(HostAdministrationStatus.FailedToShutDown, true);
    }

    @Test
    public void isHostDownTestHostAdministrationStatusNull() {
        executeIsHostDownTest(null, false);
    }

    @Test
    public void isHostDownTestHostAdministrationStatusUp() {
        executeIsHostDownTest(HostAdministrationStatus.Up, false);
    }

    @Test
    public void isThereAnyHostOnCloudDeactivatedByOurManagerTest() {
        executeIsThereAnyHostOnCloudDeactivatedByOurManagerTest(true);
    }

    @Test
    public void isThereAnyHostOnCloudDeactivatedByOurManagerTestFalse() {
        executeIsThereAnyHostOnCloudDeactivatedByOurManagerTest(false);
    }

    private void executeIsThereAnyHostOnCloudDeactivatedByOurManagerTest(boolean expected) {
        Mockito.doReturn(expected).when(hostDaoJdbc).isThereAnyHostOnCloudDeactivatedByOurManager();
        boolean result = spy.isThereAnyHostOnCloudDeactivatedByOurManager();
        Mockito.verify(hostDaoJdbc).isThereAnyHostOnCloudDeactivatedByOurManager();
        Assert.assertEquals(expected, result);
    }

    private void executeIsHostUpAndEnabledTest(Status status, ResourceState resourceState, int timesResourceState, boolean expectedResult) {
        Mockito.doReturn(status).when(hostDaoJdbc).getStatus(Mockito.anyLong());
        Mockito.doReturn(resourceState).when(hostDaoJdbc).getResourceState(Mockito.anyLong());

        boolean result = spy.isHostUpAndEnabled(0l);

        Mockito.verify(hostDaoJdbc).getStatus(Mockito.anyLong());
        Mockito.verify(hostDaoJdbc, Mockito.times(timesResourceState)).getResourceState(Mockito.anyLong());
        Assert.assertEquals(expectedResult, result);
    }

    private void executeResourceStateCheck(boolean expected, ResourceState resourceState, String methodName) throws Exception {
        Mockito.doReturn(resourceState).when(hostDaoJdbc).getResourceState(Mockito.anyLong());

        Class<?> clazz = Class.forName("br.com.autonomiccs.autonomic.plugin.common.services.HostService");
        Method method = clazz.getMethod(methodName, long.class);
        boolean result = (boolean) method.invoke(spy, 0l);

        Mockito.verify(hostDaoJdbc).getResourceState(Mockito.anyLong());
        Assert.assertEquals(expected, result);
    }

    private void executeIsHostDownTest(HostAdministrationStatus hostAdmStatus, boolean expected) {
        Mockito.doReturn(hostAdmStatus).when(hostDaoJdbc).getAdministrationStatus(Mockito.anyLong());
        boolean result = spy.isHostDown(0l);
        Mockito.verify(hostDaoJdbc).getAdministrationStatus(Mockito.anyLong());
        Assert.assertEquals(expected, result);
    }

}
