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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.UserVO;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.services.HostResourcesService;
import br.com.autonomiccs.autonomic.plugin.common.beans.AutonomiccsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.SshUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

@RunWith(PowerMockRunner.class)
public class AutonomiccsSystemVmDeploymentServiceTest {
    private final String commandToInstallOpenJDK7 = "aptitude -y install openjdk-7-jdk";
    private final int ONE_MEGABYTE_IN_BYTES = 1048576;

    @Spy
    @InjectMocks
    private AutonomiccsSystemVmDeploymentService spy = new AutonomiccsSystemVmDeploymentService();
    @Mock
    private ServiceOfferingVO autonomiccsSystemVmServiceOffering;
    @Mock
    private SshUtils sshUtils;
    @Mock
    private AutonomiccsSystemVmTemplateService autonomiccsSystemVmTemplateService;
    @Mock
    private AutonomiccsSystemVmDao autonomiccsSystemVmDao;
    @Mock
    private HostService hostService;
    @Mock
    private ConfigurationDao configurationDao;
    @Mock
    private DataCenterDao dataCenterDao;
    @Mock
    private AccountManager accountManager;
    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkModel networkModel;
    @Mock
    private NetworkOfferingDao networkOfferingDao;
    @Mock
    private NetworkOrchestrationService networkManager;
    @Mock
    private VirtualMachineManager virtualMachineManager;
    @Mock
    private AutonomiccsServiceOfferingService autonomiccsServiceOfferingService;
    @Mock
    private HostUtils hostUtils;
    @Mock
    private ThreadUtils threadUtils;
    @Mock
    private ClusterService clusterService;
    @Mock
    private HostResourcesService hostResourcesService;
    @Mock
    private PodService podService;
    @Mock
    private ZoneService zoneService;

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInCloudToDeployAutonomiccsSystemVmTest() {
        List<DataCenterVO> allZonesEnabled = createDataCenterVOList();
        HostVO host = Mockito.mock(HostVO.class);
        executeSearchForRandomHostInCloudToDeployAutonomiccsSystemVmTest(allZonesEnabled, host, host, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInCloudToDeployAutonomiccsSystemVmTestEmptyList() {
        HostVO host = Mockito.mock(HostVO.class);
        executeSearchForRandomHostInCloudToDeployAutonomiccsSystemVmTest(new ArrayList<DataCenterVO>(), host, null, 0);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInCloudToDeployAutonomiccsSystemVmTestNullHost() {
        List<DataCenterVO> allZonesEnabled = createDataCenterVOList();
        executeSearchForRandomHostInCloudToDeployAutonomiccsSystemVmTest(allZonesEnabled, null, null, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInZoneToDeployAutonomiccsSystemVmTest() {
        List<HostPodVO> allPodsEnabledFromZone = configureAllPodsEnabledFromZone();
        HostVO host = Mockito.mock(HostVO.class);
        configureExecuteVerifySearchForRandomHostInZoneToDeployAutonomiccsSystemVmTest(allPodsEnabledFromZone, host, host, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInZoneToDeployAutonomiccsSystemVmTestHostNull() {
        List<HostPodVO> allPodsEnabledFromZone = configureAllPodsEnabledFromZone();

        configureExecuteVerifySearchForRandomHostInZoneToDeployAutonomiccsSystemVmTest(allPodsEnabledFromZone, null, null, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInZoneToDeployAutonomiccsSystemVmTestEmptyList() {
        HostVO host = Mockito.mock(HostVO.class);
        configureExecuteVerifySearchForRandomHostInZoneToDeployAutonomiccsSystemVmTest(new ArrayList<HostPodVO>(), host, null, 0);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInPodToDeployAutonomiccsSystemVmTest() {
        List<ClusterVO> allClustersFromPod = configureAllClustersFromPod();

        HostVO host = Mockito.mock(HostVO.class);
        configureExecuteVerifySearchForRandomHostInPodToDeployAutonomiccsSystemVmTest(allClustersFromPod, host, host, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInPodToDeployAutonomiccsSystemVmTestHostNull() {
        List<ClusterVO> allClustersFromPod = configureAllClustersFromPod();

        configureExecuteVerifySearchForRandomHostInPodToDeployAutonomiccsSystemVmTest(allClustersFromPod, null, null, 1);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInPodToDeployAutonomiccsSystemVmTestEmptyList() {
        HostVO host = Mockito.mock(HostVO.class);
        configureExecuteVerifySearchForRandomHostInPodToDeployAutonomiccsSystemVmTest(new ArrayList<ClusterVO>(), host, null, 0);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForRandomHostInClusterToDeployAutonomiccsSystemVmTest() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        HostVO host = Mockito.mock(HostVO.class);
        Mockito.doReturn(host).when(spy).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(Mockito.any(ClusterVO.class), Matchers.anyListOf(HostVO.class));

        HostVO result = spy.searchForRandomHostInClusterToDeployAutonomiccsSystemVm(cluster);

        Mockito.verify(spy).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(Mockito.any(ClusterVO.class), Matchers.anyListOf(HostVO.class));
        Assert.assertEquals(host, result);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTest() {
        HostVO host = Mockito.mock(HostVO.class);
        HostVO hostToExclude = Mockito.mock(HostVO.class);

        List<HostVO> allHostsInCluster = new ArrayList<>();
        allHostsInCluster.add(host);
        allHostsInCluster.add(hostToExclude);

        List<HostVO> excludeHosts = new ArrayList<>();
        excludeHosts.add(hostToExclude);

        configureExecuteVerifySearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTest(allHostsInCluster, excludeHosts, true, 1, host);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTestCantDeployAutonomiccsSystemVmOnHost() {
        HostVO host = Mockito.mock(HostVO.class);
        List<HostVO> allHostsInCluster = new ArrayList<>();
        allHostsInCluster.add(host);
        configureExecuteVerifySearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTest(allHostsInCluster, new ArrayList<HostVO>(), false, 1, null);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTestEmptyList() {
        configureExecuteVerifySearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTest(new ArrayList<HostVO>(), new ArrayList<HostVO>(), true, 0, null);
    }

    @Test
    public void canDeployAutonomiccsSystemVmOnHostTestCanHostSupportVmTrueIsTemplateRegisteredAndReadyForHypervisorTrueExpectedTrue() {
        configureExecuteVerifyCanDeployAutonomiccsSystemVmOnHostTest(true, true, true, 1);
    }

    @Test
    public void canDeployAutonomiccsSystemVmOnHostTestCanHostSupportVmTrueIsTemplateRegisteredAndReadyForHypervisorFalseExpectedFalse() {
        configureExecuteVerifyCanDeployAutonomiccsSystemVmOnHostTest(true, false, false, 1);
    }

    @Test
    public void canDeployAutonomiccsSystemVmOnHostTestCanHostSupportVmFalseIsTemplateRegisteredAndReadyForHypervisorTrueExpectedFalse() {
        configureExecuteVerifyCanDeployAutonomiccsSystemVmOnHostTest(false, true, false, 0);
    }

    @Test
    public void canDeployAutonomiccsSystemVmOnHostTestCanHostSupportVmFalseIsTemplateRegisteredAndReadyForHypervisorFalseExpectedFalse() {
        configureExecuteVerifyCanDeployAutonomiccsSystemVmOnHostTest(false, false, false, 0);
    }

    @Test
    public void canHostSupportVmTest() {
        ServiceOfferingVO vmServiceOffering = createVmServiceOffering(1, 1000, 1000);
        HostResources hostResources = createHostResources(2, 2.0f, 1000, 1000, 3.0f, 4000 * (long) ONE_MEGABYTE_IN_BYTES, 2000);

        boolean result = spy.canHostSupportVm(vmServiceOffering, hostResources);

        verifyCanHostSupportVmTest(vmServiceOffering, hostResources, 1, 1, true, result);
    }

    @Test
    public void canHostSupportVmTestNotEnoughtRam() {
        ServiceOfferingVO vmServiceOffering = createVmServiceOffering(1, 1000, 1000);
        HostResources hostResources = createHostResources(2, 2.0f, 1000, 1000, 2.0f, 1000 * (long) ONE_MEGABYTE_IN_BYTES, 1200);

        boolean result = spy.canHostSupportVm(vmServiceOffering, hostResources);

        verifyCanHostSupportVmTest(vmServiceOffering, hostResources, 1, 1, false, result);
    }

    @Test
    public void canHostSupportVmTestNotEnoughtCpu() {
        ServiceOfferingVO vmServiceOffering = createVmServiceOffering(2, 1000, 1000);
        HostResources hostResources = createHostResources(2, 1.0f, 1000, 1000, 2.0f, 2000 * (long) ONE_MEGABYTE_IN_BYTES, 1000);

        boolean result = spy.canHostSupportVm(vmServiceOffering, hostResources);

        verifyCanHostSupportVmTest(vmServiceOffering, hostResources, 1, 0, false, result);
    }

    @Test
    public void canHostSupportVmTestNotEnoughtNumberOfCpus() {
        ServiceOfferingVO vmServiceOffering = createVmServiceOffering(2, 1000, 1000);
        HostResources hostResources = createHostResources(1, 2.0f, 900, 1000, 2.0f, 2000 * (long) ONE_MEGABYTE_IN_BYTES, 1000);

        boolean result = spy.canHostSupportVm(vmServiceOffering, hostResources);

        verifyCanHostSupportVmTest(vmServiceOffering, hostResources, 0, 0, false, result);
    }

    @Test
    public void searchForAnotherRandomHostToStartSystemVmTest() {
        executeSearchForAnotherRandomHostToStartSystemVmTest(0l, 0, 1);
    }

    @Test
    public void searchForAnotherRandomHostToStartSystemVmTestHostIdNull() {
        executeSearchForAnotherRandomHostToStartSystemVmTest(null, 1, 0);
    }

    @Test
    public void searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostTest() {
        HostVO expected = Mockito.mock(HostVO.class);
        executeSearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostTest(expected, 1);
    }

    @Test
    public void searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostTestNullHost() {
        executeSearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostTest(null, 2);
    }

    @Test
    public void deploySystemVmWithJavaTest() {
        AutonomiccsSystemVm vmInstance = Mockito.mock(AutonomiccsSystemVm.class);
        Mockito.doReturn(vmInstance).when(spy).deploySystemVm(Mockito.anyLong(), Mockito.any(SystemVmType.class));
        Mockito.doReturn("managementIp").when(vmInstance).getManagementIpAddress();
        Mockito.doNothing().when(sshUtils).executeCommandOnHostViaSsh(Mockito.eq("managementIp"), Mockito.eq("aptitude update"));
        Mockito.doNothing().when(sshUtils).executeCommandOnHostViaSsh(Mockito.eq("managementIp"), Mockito.eq(commandToInstallOpenJDK7));

        AutonomiccsSystemVm result = spy.deploySystemVmWithJava(0l, SystemVmType.ClusterManagerStartHostService);

        Mockito.verify(spy).deploySystemVm(Mockito.anyLong(), Mockito.any(SystemVmType.class));
        Mockito.verify(vmInstance).getManagementIpAddress();
        Mockito.verify(sshUtils).executeCommandOnHostViaSsh(Mockito.eq("managementIp"), Mockito.eq("aptitude update"));
        Mockito.verify(sshUtils).executeCommandOnHostViaSsh(Mockito.eq("managementIp"), Mockito.eq(commandToInstallOpenJDK7));
        Assert.assertEquals(vmInstance, result);
    }

    @Test
    public void deploySystemVmTest() {
        HostVO host = Mockito.mock(HostVO.class);
        VMTemplateVO systemVmTemplate = Mockito.mock(VMTemplateVO.class);
        Account systemAcct = Mockito.mock(Account.class);
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);

        Mockito.when(host.getDataCenterId()).thenReturn(0l);
        Mockito.when(host.getPodId()).thenReturn(0l);
        Mockito.when(host.getClusterId()).thenReturn(0l);
        Mockito.when(host.getId()).thenReturn(0l);

        Mockito.doReturn(host).when(hostService).findHostById(Mockito.anyLong());
        Mockito.doNothing().when(spy).validateParametersToDeployTheSystemVm(Mockito.anyLong(), Mockito.any(HostVO.class));
        Mockito.doReturn(systemVmTemplate).when(spy).getSystemVmTemplate(Mockito.any(HostVO.class));
        Mockito.doReturn(systemAcct).when(accountManager).getSystemAccount();
        Mockito.doReturn(networks).when(spy).getSystemVmNetworks(Mockito.any(HostVO.class), Mockito.any(DataCenterDeployment.class), Mockito.any(Account.class));
        Mockito.doReturn(0l).when(spy).getNextSystemVmId();
        Mockito.doReturn("vmInstanceSuffix").when(spy).getVirtualMachineInstanceSuffix();
        Mockito.doReturn("name").when(spy).createAutonomiccsSystemVmNameForType(Mockito.anyLong(), Mockito.any(SystemVmType.class), Mockito.anyString());
        Mockito.doReturn(autonomiccsSystemVm).when(spy).createTheAutonomiccsSystemVm(Mockito.any(VMTemplateVO.class), Mockito.any(Account.class), Mockito.anyLong(),
                Mockito.anyString());
        Mockito.doReturn(autonomiccsSystemVm).when(spy).allocateAndStartTheSystemVm(Mockito.any(VMTemplateVO.class), Mockito.any(DataCenterDeployment.class), Mockito.eq(networks),
                Mockito.any(AutonomiccsSystemVm.class));
        Mockito.doNothing().when(spy).waitUntilTheAutonomiccsSystemVmIsUpAndRunning(Mockito.any(AutonomiccsSystemVm.class));

        AutonomiccsSystemVm result = spy.deploySystemVm(0l, SystemVmType.ClusterManagerStartHostService);

        InOrder inOrder = Mockito.inOrder(host, spy);
        Mockito.verify(hostService).findHostById(Mockito.anyLong());
        inOrder.verify(spy).validateParametersToDeployTheSystemVm(Mockito.anyLong(), Mockito.any(HostVO.class));
        inOrder.verify(spy).getSystemVmTemplate(Mockito.any(HostVO.class));
        inOrder.verify(host).getDataCenterId();
        inOrder.verify(host).getPodId();
        inOrder.verify(host).getClusterId();
        inOrder.verify(host).getId();
        Mockito.verify(accountManager).getSystemAccount();
        inOrder.verify(spy).getSystemVmNetworks(Mockito.any(HostVO.class), Mockito.any(DataCenterDeployment.class), Mockito.any(Account.class));
        inOrder.verify(spy).getNextSystemVmId();
        inOrder.verify(spy).getVirtualMachineInstanceSuffix();
        inOrder.verify(spy).createAutonomiccsSystemVmNameForType(Mockito.anyLong(), Mockito.any(SystemVmType.class), Mockito.anyString());
        inOrder.verify(spy).createTheAutonomiccsSystemVm(Mockito.any(VMTemplateVO.class), Mockito.any(Account.class), Mockito.anyLong(), Mockito.anyString());
        inOrder.verify(spy).allocateAndStartTheSystemVm(Mockito.any(VMTemplateVO.class), Mockito.any(DataCenterDeployment.class), Mockito.eq(networks),
                Mockito.any(AutonomiccsSystemVm.class));
        inOrder.verify(spy).waitUntilTheAutonomiccsSystemVmIsUpAndRunning(Mockito.any(AutonomiccsSystemVm.class));
        Assert.assertEquals(autonomiccsSystemVm, result);
    }

    @Test
    public void allocateAndStartTheSystemVmTest() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null);

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 1, 2, 2, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void allocateAndStartTheSystemVmTestInsufficientCapacityException() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, null, null, null);
        Mockito.doThrow(Mockito.mock(InsufficientCapacityException.class)).when(virtualMachineManager).allocate(Mockito.anyString(), Mockito.any(VMTemplateVO.class),
                Mockito.any(ServiceOfferingVO.class), Mockito.eq(networks), Mockito.any(DataCenterDeployment.class), Mockito.eq((HypervisorType) null));

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 0, 0, 0, 0);
    }

    @Test(expected = CloudRuntimeException.class)
    public void allocateAndStartTheSystemVmTestInsufficientCapacityExceptionAtAdvanceStart() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, null, null, null);
        configureAdvanceStartException(Mockito.mock(InsufficientCapacityException.class), null, null);

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 1, 1, 1, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void allocateAndStartTheSystemVmTestConcurrentOperationException() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, null, null, null);
        configureAdvanceStartException(Mockito.mock(ConcurrentOperationException.class), null, null);

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 1, 1, 1, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void allocateAndStartTheSystemVmTestResourceUnavailableException() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, null, null, null);
        configureAdvanceStartException(Mockito.mock(ResourceUnavailableException.class), null, null);

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 1, 1, 1, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void allocateAndStartTheSystemVmTestOperationTimedoutException() throws Exception {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        configureAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, null, null, null);
        configureAdvanceStartException(Mockito.mock(OperationTimedoutException.class), null, null);

        spy.allocateAndStartTheSystemVm(Mockito.mock(VMTemplateVO.class), Mockito.mock(DataCenterDeployment.class), networks, autonomiccsSystemVm);

        verifyAllocateAndStartTheSystemVmTest(networks, autonomiccsSystemVm, (HypervisorType) null, null, null, 1, 1, 1, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void waitUntilTheAutonomiccsSystemVmIsUpAndRunningTest() {
        AutonomiccsSystemVm autonomiccsSystemVm = configureWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(false);
        spy.waitUntilTheAutonomiccsSystemVmIsUpAndRunning(autonomiccsSystemVm);
        verifyWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(autonomiccsSystemVm, 100, 100);
    }

    @Test
    public void waitUntilTheAutonomiccsSystemVmIsUpAndRunningTestHostIsReachable() {
        AutonomiccsSystemVm autonomiccsSystemVm = configureWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(true);
        spy.waitUntilTheAutonomiccsSystemVmIsUpAndRunning(autonomiccsSystemVm);
        verifyWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(autonomiccsSystemVm, 1, 0);
    }

    @Test
    @PrepareForTest(AutonomiccsSystemVmDeploymentService.class)
    public void createTheAutonomiccsSystemVmTest() throws Exception {
        VMTemplateVO systemVmTemplate = Mockito.mock(VMTemplateVO.class);
        Account systemAcct = Mockito.mock(Account.class);
        UserVO user = Mockito.mock(UserVO.class);

        Mockito.doReturn(0l).when(autonomiccsSystemVmServiceOffering).getId();
        Mockito.doReturn(0l).when(systemVmTemplate).getId();
        Mockito.doReturn(HypervisorType.Any).when(systemVmTemplate).getHypervisorType();
        Mockito.doReturn(0l).when(systemVmTemplate).getGuestOSId();
        Mockito.doReturn(0l).when(systemAcct).getDomainId();
        Mockito.doReturn(0l).when(systemAcct).getId();
        Mockito.doReturn(user).when(accountManager).getSystemUser();
        Mockito.doReturn(0l).when(user).getId();
        Mockito.doReturn(true).when(autonomiccsSystemVmServiceOffering).getOfferHA();

        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        PowerMockito.whenNew(AutonomiccsSystemVm.class).withAnyArguments().thenReturn(autonomiccsSystemVm);

        Mockito.doReturn(true).when(systemVmTemplate).isDynamicallyScalable();

        Mockito.doNothing().when(autonomiccsSystemVm).setDynamicallyScalable(Mockito.anyBoolean());
        Mockito.doReturn(autonomiccsSystemVm).when(autonomiccsSystemVmDao).persist(Mockito.any(AutonomiccsSystemVm.class));

        AutonomiccsSystemVm result = spy.createTheAutonomiccsSystemVm(systemVmTemplate, systemAcct, 0l, "name");

        Mockito.verify(autonomiccsSystemVmServiceOffering).getId();
        Mockito.verify(systemVmTemplate).getId();
        Mockito.verify(systemVmTemplate).getHypervisorType();
        Mockito.verify(systemVmTemplate).getGuestOSId();
        Mockito.verify(systemAcct).getDomainId();
        Mockito.verify(systemAcct).getId();
        Mockito.verify(accountManager).getSystemUser();
        Mockito.verify(user).getId();
        Mockito.verify(autonomiccsSystemVmServiceOffering).getOfferHA();

        Mockito.verify(systemVmTemplate).isDynamicallyScalable();
        Mockito.verify(autonomiccsSystemVm).setDynamicallyScalable(Mockito.anyBoolean());
        Mockito.verify(autonomiccsSystemVmDao).persist(Mockito.any(AutonomiccsSystemVm.class));
        Assert.assertEquals(autonomiccsSystemVm, result);
    }

    @Test
    public void getSystemVmNetworksTest() {
        List<NetworkOffering> offerings = new ArrayList<>();
        NetworkOffering offering = Mockito.mock(NetworkOfferingVO.class);
        offerings.add(offering);

        Network net2 = Mockito.mock(Network.class);
        List<Network> nets2 = new ArrayList<>();
        nets2.add(net2);
        String stringNull = null;

        Mockito.doReturn(nets2).when(networkManager).setupNetwork(Mockito.any(Account.class), Mockito.eq(offering), Mockito.any(DataCenterDeployment.class),
                Mockito.eq(stringNull), Mockito.eq(stringNull), Mockito.eq(false));

        executeGetSystemVmNetworksTest(offerings, 2);

        Mockito.verify(networkManager).setupNetwork(Mockito.any(Account.class), Mockito.eq(offering), Mockito.any(DataCenterDeployment.class), Mockito.eq(stringNull),
                Mockito.eq(stringNull), Mockito.eq(false));
    }

    @Test
    public void getSystemVmNetworksTestEmptyOfferings() {
        List<NetworkOffering> offerings = new ArrayList<>();
        executeGetSystemVmNetworksTest(offerings, 1);
    }

    @Test
    public void createDefaultNicTest() {
        NicProfile result = spy.createDefaultNic();
        Assert.assertEquals(true, result.isDefaultNic());
        Assert.assertEquals(new Integer(2), result.getDeviceId());
    }

    @Test
    public void getSystemVmTemplateTest() {
        VMTemplateVO systemVmTemplate = Mockito.mock(VMTemplateVO.class);

        HostVO host = Mockito.mock(HostVO.class);
        Mockito.doReturn(systemVmTemplate).when(autonomiccsSystemVmTemplateService).findAutonomiccsSystemVmTemplate(Mockito.any(HypervisorType.class));

        VMTemplateVO result = spy.getSystemVmTemplate(host);

        Mockito.verify(autonomiccsSystemVmTemplateService).findAutonomiccsSystemVmTemplate(Mockito.any(HypervisorType.class));
        Assert.assertEquals(systemVmTemplate, result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getSystemVmTemplateTestNullVMTemplateVO() {
        HostVO host = Mockito.mock(HostVO.class);
        Mockito.doReturn(null).when(autonomiccsSystemVmTemplateService).findAutonomiccsSystemVmTemplate(Mockito.any(HypervisorType.class));
        spy.getSystemVmTemplate(host);
        Mockito.verify(autonomiccsSystemVmTemplateService).findAutonomiccsSystemVmTemplate(Mockito.any(HypervisorType.class));
    }

    @Test
    public void validateParametersToDeployTheSystemVmTestTemplateRegisteredAndReadyForHypervisor() {
        HostVO host = Mockito.mock(HostVO.class);
        configureValidateParametersToDeployTheSystemVmTest(host, true);
        spy.validateParametersToDeployTheSystemVm(0l, host);
        verifyValidateParametersToDeployTheSystemVmTest(host);
    }

    @Test(expected = CloudRuntimeException.class)
    public void validateParametersToDeployTheSystemVmTest() {
        HostVO host = Mockito.mock(HostVO.class);
        configureValidateParametersToDeployTheSystemVmTest(host, false);
        spy.validateParametersToDeployTheSystemVm(0l, host);
        verifyValidateParametersToDeployTheSystemVmTest(host);
    }

    @Test(expected = CloudRuntimeException.class)
    public void validateParametersToDeployTheSystemVmTestCloudRuntimeException() {
        spy.validateParametersToDeployTheSystemVm(0l, null);
    }

    @Test
    public void getNextSystemVmIdTest() {
        Mockito.doReturn(0l).when(autonomiccsSystemVmDao).getNextInSequence(Mockito.eq(Long.class), Mockito.eq("id"));
        spy.getNextSystemVmId();
        Mockito.verify(autonomiccsSystemVmDao).getNextInSequence(Mockito.eq(Long.class), Mockito.eq("id"));
    }

    @Test(expected = CloudRuntimeException.class)
    public void getNextSystemVmIdTestLongNull() {
        Mockito.doReturn(null).when(autonomiccsSystemVmDao).getNextInSequence(Mockito.eq(Long.class), Mockito.eq("id"));
        spy.getNextSystemVmId();
        Mockito.verify(autonomiccsSystemVmDao).getNextInSequence(Mockito.eq(Long.class), Mockito.eq("id"));
    }

    @Test
    public void getDefaultNetworkTest() {
        NetworkVO network = Mockito.mock(NetworkVO.class);
        NetworkVO network2 = Mockito.mock(NetworkVO.class);
        DataCenterVO dc = configureGetDefaultNetworkTest(NetworkType.Advanced, true, network, network2);

        NetworkVO result = spy.getDefaultNetwork(0l);

        verifyGetDefaultNetworkTest(dc, network, result, 1, 1, 0);
    }

    @Test
    public void getDefaultNetworkTestNetworkTypeNotAdvanced() {
        NetworkVO network = Mockito.mock(NetworkVO.class);
        NetworkVO network2 = Mockito.mock(NetworkVO.class);
        DataCenterVO dc = configureGetDefaultNetworkTest(NetworkType.Basic, true, network, network2);

        NetworkVO result = spy.getDefaultNetwork(0l);

        verifyGetDefaultNetworkTest(dc, network2, result, 0, 0, 1);
    }

    @Test
    public void getDefaultNetworkTestSecurityGroupDisabled() {
        NetworkVO network = Mockito.mock(NetworkVO.class);
        NetworkVO network2 = Mockito.mock(NetworkVO.class);
        DataCenterVO dc = configureGetDefaultNetworkTest(NetworkType.Advanced, false, network, network2);

        NetworkVO result = spy.getDefaultNetwork(0l);

        verifyGetDefaultNetworkTest(dc, network2, result, 1, 0, 1);
    }

    @Test
    public void getDefaultNetworkWithParamDataCenterVOTest() {
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        List<NetworkVO> defaultNetworks = new ArrayList<>();
        defaultNetworks.add(networkVo);
        executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType.Guest, networkVo, defaultNetworks, NetworkType.Basic, true, 0);
    }

    @Test
    public void getDefaultNetworkWithParamDataCenterVOTestNetworkTypeNotBasic() {
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        List<NetworkVO> defaultNetworks = new ArrayList<>();
        defaultNetworks.add(networkVo);
        executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType.Guest, networkVo, defaultNetworks, NetworkType.Advanced, true, 1);
    }

    @Test
    public void getDefaultNetworkWithParamDataCenterVOTestNetworkTypeNotBasicAndsecurityGroupDisabled() {
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        List<NetworkVO> defaultNetworks = new ArrayList<>();
        defaultNetworks.add(networkVo);
        executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType.Public, networkVo, defaultNetworks, NetworkType.Advanced, false, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkWithParamDataCenterVOTestListBiggerThanOne() {
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        List<NetworkVO> defaultNetworks = new ArrayList<>();
        defaultNetworks.add(networkVo);
        defaultNetworks.add(networkVo);
        executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType.Guest, networkVo, defaultNetworks, NetworkType.Basic, false, 0);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkWithParamDataCenterVOTestListEmpty() {
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        List<NetworkVO> defaultNetworks = new ArrayList<>();
        executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType.Guest, networkVo, defaultNetworks, NetworkType.Basic, false, 0);
    }

    @Test
    public void getDefaultNetworkForAdvancedNetworkingWithSecurityGroupdsTest() {
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        List<NetworkVO> networks = new ArrayList<>();
        NetworkVO networkVo = Mockito.mock(NetworkVO.class);
        networks.add(networkVo);
        Mockito.doReturn(networks).when(networkDao).listByZoneSecurityGroup(Mockito.anyLong());

        NetworkVO result = spy.getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(0l, dc);

        Mockito.verify(networkDao).listByZoneSecurityGroup(Mockito.anyLong());
        Assert.assertEquals(networkVo, result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getDefaultNetworkForAdvancedNetworkingWithSecurityGroupdsTestEmptyNetworkVoList() {
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        Mockito.doReturn(new ArrayList<NetworkVO>()).when(networkDao).listByZoneSecurityGroup(Mockito.anyLong());

        spy.getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(0l, dc);

        Mockito.verify(networkDao).listByZoneSecurityGroup(Mockito.anyLong());
    }

    @Test
    public void createAutonomiccsSystemVmNameForTypeTest() {
        String result = spy.createAutonomiccsSystemVmNameForType(123l, SystemVmType.ClusterManagerAgent, "instanceSuffix");
        Assert.assertEquals("CM-A-123-instanceSuffix", result);
    }

    @Test
    public void getVirtualMachineInstanceSuffixTest() {
        Map<String, String> configs = new HashMap<>();
        configs.put("instance.name", "value");
        Mockito.doReturn(configs).when(spy).getConfigurationsFromDatabase();

        String result = spy.getVirtualMachineInstanceSuffix();
        Assert.assertEquals("value", result);
    }

    @Test
    public void getConfigurationsFromDatabaseTest() {
        Map<String, String> map = new HashMap<>();
        Mockito.doReturn(map).when(configurationDao).getConfiguration(Mockito.eq("management-server"), Matchers.<Map<String, Object>> any());
        Map<String, String> result = spy.getConfigurationsFromDatabase();
        Assert.assertEquals(map, result);
    }

    @Test
    public void afterPropertiesSetTest() throws Exception {
        Mockito.doNothing().when(spy).loadAutonomiccsSystemVmServiceOffering();
        spy.afterPropertiesSet();
        Mockito.verify(spy).loadAutonomiccsSystemVmServiceOffering();
    }

    @Test(expected = CloudRuntimeException.class)
    public void afterPropertiesSetTestExpectCloudRuntimeException() throws Exception {
        spy.autonomiccsSystemVmServiceOffering = null;
        Mockito.doNothing().when(spy).loadAutonomiccsSystemVmServiceOffering();
        spy.afterPropertiesSet();
        Mockito.verify(spy).loadAutonomiccsSystemVmServiceOffering();
    }

    @Test
    public void loadAutonomiccsSystemVmServiceOfferingTest() {
        Mockito.doReturn(null).when(autonomiccsServiceOfferingService).searchAutonomiccsServiceOffering();

        spy.loadAutonomiccsSystemVmServiceOffering();

        Mockito.verify(autonomiccsServiceOfferingService).searchAutonomiccsServiceOffering();
    }

    @Test
    public void loadAutonomiccsSystemVmServiceOfferingTestCatchCloudRuntimeException() {
        Mockito.doThrow(new CloudRuntimeException("exception")).doReturn(null).when(autonomiccsServiceOfferingService).searchAutonomiccsServiceOffering();

        spy.loadAutonomiccsSystemVmServiceOffering();

        Mockito.verify(autonomiccsServiceOfferingService, Mockito.times(2)).searchAutonomiccsServiceOffering();
        Mockito.verify(autonomiccsServiceOfferingService).createAutonomiccsServiceOffering();
    }

    private List<DataCenterVO> createDataCenterVOList() {
        List<DataCenterVO> allZonesEnabled = new ArrayList<>();
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        allZonesEnabled.add(dc);
        return allZonesEnabled;
    }

    private void executeSearchForRandomHostInCloudToDeployAutonomiccsSystemVmTest(List<DataCenterVO> allZonesEnabled, HostVO host, HostVO expected, int times) {
        Mockito.doReturn(allZonesEnabled).when(zoneService).listAllZonesEnabled();
        PowerMockito.mockStatic(Collections.class);
        Mockito.doReturn(host).when(spy).searchForRandomHostInZoneToDeployAutonomiccsSystemVm(Mockito.any(DataCenterVO.class));

        HostVO result = spy.searchForRandomHostInCloudToDeployAutonomiccsSystemVm();

        PowerMockito.verifyStatic();
        Collections.shuffle(Mockito.eq(allZonesEnabled));
        Mockito.verify(zoneService).listAllZonesEnabled();
        Mockito.verify(spy, Mockito.times(times)).searchForRandomHostInZoneToDeployAutonomiccsSystemVm(Mockito.any(DataCenterVO.class));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteVerifySearchForRandomHostInZoneToDeployAutonomiccsSystemVmTest(List<HostPodVO> allPodsEnabledFromZone, HostVO host, HostVO expected, int times) {
        DataCenterVO dataCenterVO = Mockito.mock(DataCenterVO.class);
        Mockito.when(dataCenterVO.getId()).thenReturn(0l);

        Mockito.doReturn(allPodsEnabledFromZone).when(podService).getAllPodsEnabledFromZone(Mockito.anyLong());
        PowerMockito.mockStatic(Collections.class);
        Mockito.doReturn(host).when(spy).searchForRandomHostInPodToDeployAutonomiccsSystemVm(Mockito.any(HostPodVO.class));

        HostVO result = spy.searchForRandomHostInZoneToDeployAutonomiccsSystemVm(dataCenterVO);

        PowerMockito.verifyStatic();
        Collections.shuffle(Mockito.eq(allPodsEnabledFromZone));
        Mockito.verify(spy, Mockito.times(times)).searchForRandomHostInPodToDeployAutonomiccsSystemVm(Mockito.any(HostPodVO.class));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteVerifySearchForRandomHostInPodToDeployAutonomiccsSystemVmTest(List<ClusterVO> allClustersFromPod, HostVO host, HostVO expected, int times) {
        HostPodVO pod = Mockito.mock(HostPodVO.class);
        Mockito.when(pod.getId()).thenReturn(0l);
        PowerMockito.mockStatic(Collections.class);
        Mockito.doReturn(allClustersFromPod).when(clusterService).listAllClustersFromPod(Mockito.anyLong());
        Mockito.doReturn(host).when(spy).searchForRandomHostInClusterToDeployAutonomiccsSystemVm(Mockito.any(ClusterVO.class));

        HostVO result = spy.searchForRandomHostInPodToDeployAutonomiccsSystemVm(pod);

        PowerMockito.verifyStatic();
        Collections.shuffle(Mockito.eq(allClustersFromPod));
        Mockito.verify(clusterService).listAllClustersFromPod(Mockito.anyLong());
        Mockito.verify(spy, Mockito.times(times)).searchForRandomHostInClusterToDeployAutonomiccsSystemVm(Mockito.any(ClusterVO.class));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteVerifySearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostsTest(List<HostVO> allHostsInCluster, List<HostVO> excludeHosts,
            boolean canDeployAutonomiccsSystemVmOnHost, int times, HostVO expected) {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);

        PowerMockito.mockStatic(Collections.class);
        Mockito.doReturn(allHostsInCluster).when(hostService).listAllHostsInCluster(Mockito.any(ClusterVO.class));
        Mockito.doReturn(canDeployAutonomiccsSystemVmOnHost).when(spy).canDeployAutonomiccsSystemVmOnHost(Mockito.any(HostVO.class));

        HostVO result = spy.searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(cluster, excludeHosts);

        PowerMockito.verifyStatic();
        Collections.shuffle(Mockito.eq(allHostsInCluster));
        Mockito.verify(hostService).listAllHostsInCluster(Mockito.any(ClusterVO.class));
        Mockito.verify(spy, Mockito.times(times)).canDeployAutonomiccsSystemVmOnHost(Mockito.any(HostVO.class));
        Assert.assertEquals(expected, result);
    }

    private void configureExecuteVerifyCanDeployAutonomiccsSystemVmOnHostTest(boolean canHostSupportVm, boolean isTemplateRegisteredAndReadyForHypervisor, boolean expected,
            int isTemplateRegisteredAndReadyForHypervisorTimes) {
        HostVO host = Mockito.mock(HostVO.class);
        Mockito.when(host.getHypervisorType()).thenReturn(HypervisorType.Any);
        ServiceOfferingVO vmServiceOffering = Mockito.mock(ServiceOfferingVO.class);
        HostResources hostResources = Mockito.mock(HostResources.class);

        Mockito.doReturn(vmServiceOffering).when(autonomiccsServiceOfferingService).searchAutonomiccsServiceOffering();
        Mockito.doReturn(hostResources).when(hostResourcesService).createHostResources(Mockito.any(HostVO.class));
        Mockito.doReturn(canHostSupportVm).when(spy).canHostSupportVm(Mockito.any(ServiceOfferingVO.class), Mockito.any(HostResources.class));
        Mockito.doReturn(isTemplateRegisteredAndReadyForHypervisor).when(autonomiccsSystemVmTemplateService)
        .isTemplateRegisteredAndReadyForHypervisor(Mockito.any(HypervisorType.class));

        boolean result = spy.canDeployAutonomiccsSystemVmOnHost(host);

        Mockito.verify(autonomiccsServiceOfferingService).searchAutonomiccsServiceOffering();
        Mockito.verify(hostResourcesService).createHostResources(Mockito.any(HostVO.class));
        Mockito.verify(spy).canHostSupportVm(Mockito.any(ServiceOfferingVO.class), Mockito.any(HostResources.class));
        Mockito.verify(autonomiccsSystemVmTemplateService, Mockito.times(isTemplateRegisteredAndReadyForHypervisorTimes))
        .isTemplateRegisteredAndReadyForHypervisor(Mockito.any(HypervisorType.class));
        Assert.assertEquals(expected, result);
    }

    private ServiceOfferingVO createVmServiceOffering(int cpus, int cpuSpeed, int ramSize) {
        ServiceOfferingVO vmServiceOffering = Mockito.mock(ServiceOfferingVO.class);
        Mockito.when(vmServiceOffering.getCpu()).thenReturn(cpus);
        Mockito.when(vmServiceOffering.getSpeed()).thenReturn(cpuSpeed);
        Mockito.when(vmServiceOffering.getRamSize()).thenReturn(ramSize);
        return vmServiceOffering;
    }

    private HostResources createHostResources(int cpus, float cpuOverprovisioning, long cpuSpeed, long usedCpu, float memoryOverprovisioning, long totalMemoryInBytes,
            long usedMemoryInMegaBytes) {
        HostResources hostResources = Mockito.mock(HostResources.class);
        Mockito.when(hostResources.getCpus()).thenReturn(cpus);
        Mockito.when(hostResources.getCpuOverprovisioning()).thenReturn(cpuOverprovisioning);
        Mockito.when(hostResources.getSpeed()).thenReturn(cpuSpeed);
        Mockito.when(hostResources.getMemoryOverprovisioning()).thenReturn(memoryOverprovisioning);
        Mockito.when(hostResources.getTotalMemoryInBytes()).thenReturn(totalMemoryInBytes);
        Mockito.when(hostResources.getUsedMemoryInMegaBytes()).thenReturn(usedMemoryInMegaBytes);
        Mockito.when(hostResources.getUsedCpu()).thenReturn(usedCpu);

        return hostResources;
    }

    private void verifyCanHostSupportVmTest(ServiceOfferingVO vmServiceOffering, HostResources hostResources, int times1, int times2, boolean expected, boolean result) {
        Mockito.verify(vmServiceOffering, Mockito.times(1 + times1)).getCpu();
        Mockito.verify(hostResources, Mockito.times(1 + times1)).getCpus();

        Mockito.verify(hostResources, Mockito.times(times1)).getCpuOverprovisioning();
        Mockito.verify(hostResources, Mockito.times(times1)).getSpeed();
        Mockito.verify(hostResources, Mockito.times(times1)).getUsedCpu();
        Mockito.verify(vmServiceOffering, Mockito.times(times1)).getSpeed();

        Mockito.verify(hostResources, Mockito.times(times2)).getMemoryOverprovisioning();
        Mockito.verify(hostResources, Mockito.times(times2)).getTotalMemoryInBytes();
        Mockito.verify(hostResources, Mockito.times(times2)).getUsedMemoryInMegaBytes();
        Mockito.verify(vmServiceOffering, Mockito.times(times2)).getRamSize();
        Assert.assertEquals(expected, result);
    }

    private void executeSearchForAnotherRandomHostToStartSystemVmTest(Long hostId, int hostIdNullTimes, int hostIdNotNullTimes) {
        VMInstanceVO vmInstance = Mockito.mock(VMInstanceVO.class);
        HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        HostVO host = Mockito.mock(HostVO.class);

        Mockito.when(vmInstance.getHostId()).thenReturn(hostId);
        Mockito.when(vmInstance.getPodIdToDeployIn()).thenReturn(0l);
        Mockito.doReturn(hostPodVO).when(podService).findPodById(Mockito.anyLong());
        Mockito.doReturn(host).when(spy).searchForRandomHostInPodToDeployAutonomiccsSystemVm(Mockito.any(HostPodVO.class));
        Mockito.doReturn(host).when(hostService).findHostById(Mockito.anyLong());
        Mockito.doReturn(host).when(spy).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(Mockito.any(HostVO.class));

        HostVO result = spy.searchForAnotherRandomHostToStartSystemVm(vmInstance);

        Mockito.verify(vmInstance).getHostId();
        Mockito.verify(vmInstance, Mockito.times(hostIdNullTimes)).getPodIdToDeployIn();
        Mockito.verify(podService, Mockito.times(hostIdNullTimes)).findPodById(Mockito.anyLong());
        Mockito.verify(spy, Mockito.times(hostIdNullTimes)).searchForRandomHostInPodToDeployAutonomiccsSystemVm(Mockito.any(HostPodVO.class));
        Mockito.verify(hostService, Mockito.times(hostIdNotNullTimes)).findHostById(Mockito.anyLong());
        Mockito.verify(spy, Mockito.times(hostIdNotNullTimes)).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(Mockito.any(HostVO.class));
        Assert.assertEquals(host, result);
    }

    private void executeSearchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHostTest(HostVO expected, int times) {
        HostVO excludeHost = Mockito.mock(HostVO.class);
        ClusterVO cluster = Mockito.mock(ClusterVO.class);

        Mockito.doReturn(0l).when(excludeHost).getClusterId();
        Mockito.doReturn(cluster).when(clusterService).findById(Mockito.anyLong());
        Mockito.doReturn(expected).when(spy).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(Mockito.any(ClusterVO.class), Matchers.anyListOf(HostVO.class));

        HostVO result = spy.searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(excludeHost);

        Mockito.verify(excludeHost, Mockito.times(times)).getClusterId();
        Mockito.verify(clusterService).findById(Mockito.anyLong());
        Mockito.verify(spy).searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(Mockito.any(ClusterVO.class), Matchers.anyListOf(HostVO.class));
        Assert.assertEquals(expected, result);
    }

    private void verifyAllocateAndStartTheSystemVmTest(LinkedHashMap<Network, List<? extends NicProfile>> networks, AutonomiccsSystemVm autonomiccsSystemVm,
            HypervisorType hypervisorType, Map<VirtualMachineProfile.Param, Object> arg1, DeploymentPlanner arg2, int getters, int timesGetId, int timesFindById,
            int timesAdvanceStart) throws InsufficientCapacityException, ResourceUnavailableException, OperationTimedoutException {
        Mockito.verify(autonomiccsSystemVm, Mockito.times(getters)).getInstanceName();
        Mockito.verify(autonomiccsSystemVm, Mockito.times(timesGetId)).getId();
        Mockito.verify(autonomiccsSystemVm, Mockito.times(getters)).getUuid();
        InOrder inOrderVirtualMachineManager = Mockito.inOrder(virtualMachineManager);
        inOrderVirtualMachineManager.verify(virtualMachineManager).allocate(Mockito.anyString(), Mockito.any(VMTemplateVO.class), Mockito.any(ServiceOfferingVO.class),
                Mockito.eq(networks),
                Mockito.any(DataCenterDeployment.class), Mockito.eq(hypervisorType));
        inOrderVirtualMachineManager.verify(virtualMachineManager, Mockito.times(timesAdvanceStart)).advanceStart(Mockito.anyString(), Mockito.eq(arg1), Mockito.eq(arg2));
        Mockito.verify(autonomiccsSystemVmDao, Mockito.times(timesFindById)).findById(Mockito.anyLong());
    }

    private AutonomiccsSystemVm configureWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(boolean isHostReachable) {
        AutonomiccsSystemVm autonomiccsSystemVm = Mockito.mock(AutonomiccsSystemVm.class);
        Mockito.doReturn("ip").when(autonomiccsSystemVm).getManagementIpAddress();
        Mockito.doReturn("name").when(autonomiccsSystemVm).getInstanceName();
        Mockito.doReturn(0l).when(autonomiccsSystemVm).getId();

        Mockito.doReturn(isHostReachable).when(hostUtils).isHostReachable(Mockito.anyString());
        Mockito.doNothing().when(threadUtils).sleepThread(Mockito.anyInt());
        return autonomiccsSystemVm;
    }

    private void verifyWaitUntilTheAutonomiccsSystemVmIsUpAndRunningTest(AutonomiccsSystemVm autonomiccsSystemVm, int timesLoop, int timesSleep) {
        Mockito.verify(autonomiccsSystemVm, Mockito.times(timesLoop + 1)).getManagementIpAddress();
        Mockito.verify(autonomiccsSystemVm, Mockito.times(timesLoop + 1)).getInstanceName();
        Mockito.verify(autonomiccsSystemVm, Mockito.times(timesLoop + 1)).getId();

        Mockito.verify(hostUtils, Mockito.times(timesLoop + 1)).isHostReachable(Mockito.anyString());
        Mockito.verify(threadUtils, Mockito.times(timesSleep)).sleepThread(Mockito.anyInt());
    }

    private void executeGetSystemVmNetworksTest(List<NetworkOffering> offerings, int size) {
        HostVO host = Mockito.mock(HostVO.class);
        DataCenterDeployment plan = Mockito.mock(DataCenterDeployment.class);
        Account systemAcct = Mockito.mock(Account.class);
        NicProfile defaultNic = Mockito.mock(NicProfile.class);
        NetworkVO defaultNetwork = Mockito.mock(NetworkVO.class);
        Mockito.doReturn(0l).when(defaultNetwork).getNetworkOfferingId();
        String stringNull = null;

        Network net = Mockito.mock(Network.class);
        List<Network> nets = new ArrayList<>();
        nets.add(net);

        NetworkOfferingVO networkOfferingVO = Mockito.mock(NetworkOfferingVO.class);

        Mockito.doReturn(defaultNic).when(spy).createDefaultNic();
        Mockito.doReturn(defaultNetwork).when(spy).getDefaultNetwork(Mockito.anyLong());
        Mockito.doReturn(networkOfferingVO).when(networkOfferingDao).findById(Mockito.anyLong());

        Mockito.doReturn(nets).when(networkManager).setupNetwork(Mockito.any(Account.class), Mockito.eq(networkOfferingVO), Mockito.any(DataCenterDeployment.class),
                Mockito.eq(stringNull), Mockito.eq(stringNull), Mockito.eq(false));

        Mockito.doReturn(offerings).when(networkModel).getSystemAccountNetworkOfferings(Mockito.eq(NetworkOffering.SystemControlNetwork),
                Mockito.eq(NetworkOffering.SystemManagementNetwork));

        LinkedHashMap<Network, List<? extends NicProfile>> result = spy.getSystemVmNetworks(host, plan, systemAcct);

        Mockito.verify(spy).createDefaultNic();
        Mockito.verify(spy).getDefaultNetwork(Mockito.anyLong());
        Mockito.verify(networkOfferingDao).findById(Mockito.anyLong());

        Mockito.verify(networkManager).setupNetwork(Mockito.any(Account.class), Mockito.eq(networkOfferingVO), Mockito.any(DataCenterDeployment.class), Mockito.eq(stringNull),
                Mockito.eq(stringNull), Mockito.eq(false));

        Mockito.verify(networkModel).getSystemAccountNetworkOfferings(Mockito.eq(NetworkOffering.SystemControlNetwork), Mockito.eq(NetworkOffering.SystemManagementNetwork));
        Assert.assertEquals(size, result.size());
    }

    private void configureValidateParametersToDeployTheSystemVmTest(HostVO host, boolean isTemplateRegisteredAndReadyForHypervisor) {
        Mockito.doReturn(HypervisorType.Any).when(host).getHypervisorType();
        Mockito.doReturn(isTemplateRegisteredAndReadyForHypervisor).when(autonomiccsSystemVmTemplateService)
        .isTemplateRegisteredAndReadyForHypervisor(Mockito.any(HypervisorType.class));
    }

    private void verifyValidateParametersToDeployTheSystemVmTest(HostVO host) {
        Mockito.verify(host).getHypervisorType();
        Mockito.verify(autonomiccsSystemVmTemplateService).isTemplateRegisteredAndReadyForHypervisor(Mockito.any(HypervisorType.class));
    }

    private DataCenterVO configureGetDefaultNetworkTest(NetworkType networkType, boolean isSecurityGroupEnabled, NetworkVO network, NetworkVO network2) {
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        Mockito.doReturn(networkType).when(dc).getNetworkType();
        Mockito.doReturn(dc).when(dataCenterDao).findById(Mockito.anyLong());
        Mockito.doReturn(isSecurityGroupEnabled).when(dc).isSecurityGroupEnabled();
        Mockito.doReturn(network).when(spy).getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(Mockito.anyLong(), Mockito.any(DataCenterVO.class));
        Mockito.doReturn(network2).when(spy).getDefaultNetwork(Mockito.any(DataCenterVO.class));
        return dc;
    }

    private void verifyGetDefaultNetworkTest(DataCenterVO dc, NetworkVO expected, NetworkVO result, int isSecurityGroupEnabledTimes,
            int getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds, int getDefaultNetworkTimes) {
        Mockito.verify(dataCenterDao).findById(Mockito.anyLong());
        Mockito.verify(dc, Mockito.times(isSecurityGroupEnabledTimes)).isSecurityGroupEnabled();
        Mockito.verify(spy, Mockito.times(getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds)).getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(Mockito.anyLong(),
                Mockito.any(DataCenterVO.class));
        Mockito.verify(spy, Mockito.times(getDefaultNetworkTimes)).getDefaultNetwork(Mockito.any(DataCenterVO.class));
        Assert.assertEquals(expected, result);
    }

    private void executeGetDefaultNetworkWithParamDataCenterVOTest(TrafficType trafficType, NetworkVO networkVO, List<NetworkVO> defaultNetworks, NetworkType networkType,
            boolean isSecurityGroupEnabled, int isSecurityGroupEnabledTimes) {
        DataCenterVO dc = Mockito.mock(DataCenterVO.class);
        Mockito.doReturn(0l).when(dc).getId();
        Mockito.doReturn(networkType).when(dc).getNetworkType();
        Mockito.doReturn(isSecurityGroupEnabled).when(dc).isSecurityGroupEnabled();

        Mockito.doReturn(defaultNetworks).when(networkDao).listByZoneAndTrafficType(Mockito.anyLong(), Mockito.eq(trafficType));

        NetworkVO result = spy.getDefaultNetwork(dc);

        InOrder inOrder = Mockito.inOrder(dc);
        inOrder.verify(dc).getNetworkType();
        inOrder.verify(dc, Mockito.times(isSecurityGroupEnabledTimes)).isSecurityGroupEnabled();
        inOrder.verify(dc).getId();
        Mockito.verify(networkDao).listByZoneAndTrafficType(Mockito.anyLong(), Mockito.eq(trafficType));
        Assert.assertEquals(networkVO, result);
    }

    private void configureAllocateAndStartTheSystemVmTest(LinkedHashMap<Network, List<? extends NicProfile>> networks, AutonomiccsSystemVm autonomiccsSystemVm,
            HypervisorType hypervisorType, Map<VirtualMachineProfile.Param, Object> arg1, DeploymentPlanner arg2)
                    throws InsufficientCapacityException, ResourceUnavailableException, OperationTimedoutException {
        Mockito.doReturn(0l).when(autonomiccsSystemVm).getId();
        Mockito.doReturn("uuid").when(autonomiccsSystemVm).getUuid();
        Mockito.doReturn("instanceName").when(autonomiccsSystemVm).getInstanceName();
        Mockito.doNothing().when(virtualMachineManager).allocate(Mockito.anyString(), Mockito.any(VMTemplateVO.class), Mockito.any(ServiceOfferingVO.class), Mockito.eq(networks),
                Mockito.any(DataCenterDeployment.class), Mockito.eq(hypervisorType));
        Mockito.doReturn(autonomiccsSystemVm).when(autonomiccsSystemVmDao).findById(Mockito.anyLong());
        Mockito.doNothing().when(virtualMachineManager).advanceStart(Mockito.anyString(), Mockito.eq(arg1), Mockito.eq(arg2));
        Mockito.doReturn(autonomiccsSystemVm).when(autonomiccsSystemVmDao).findById(Mockito.anyLong());
    }

    private void configureAdvanceStartException(Exception exception, Map<VirtualMachineProfile.Param, Object> arg1, DeploymentPlanner arg2) throws Exception {
        Mockito.doThrow(exception).when(virtualMachineManager).advanceStart(Mockito.anyString(), Mockito.eq(arg1), Mockito.eq(arg2));
    }

    private List<HostPodVO> configureAllPodsEnabledFromZone() {
        List<HostPodVO> allPodsEnabledFromZone = new ArrayList<>();
        HostPodVO hostPodVO = Mockito.mock(HostPodVO.class);
        allPodsEnabledFromZone.add(hostPodVO);
        return allPodsEnabledFromZone;
    }

    private List<ClusterVO> configureAllClustersFromPod() {
        List<ClusterVO> allClustersFromPod = new ArrayList<>();
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        allClustersFromPod.add(cluster);
        return allClustersFromPod;
    }

}
