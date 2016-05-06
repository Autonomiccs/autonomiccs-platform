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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.UserVmService;
import com.cloud.vm.VMInstanceVO;
import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.plugin.services.AutonomicClusterManagementService;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;
import br.com.autonomiccs.autonomic.algorithms.commons.services.CloudResourcesService;
import br.com.autonomiccs.autonomic.algorithms.commons.services.ClusterResourcesService;
import br.com.autonomiccs.autonomic.algorithms.commons.services.HostResourcesService;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.ClusterService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.services.VirtualMachineService;
import br.com.autonomiccs.autonomic.plugin.common.services.ZoneService;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

@RunWith(MockitoJUnitRunner.class)
public class AdministrationAgentTest {

    @Spy
    @InjectMocks
    private AdministrationAgent administrationAgent;
    @Mock
    private ClusterService clusterService;
    @Mock
    private AutonomicClusterManagementHeuristicService autonomicManagementHeuristicService;
    @Mock
    private ClusterAdministrationHeuristicAlgorithm clusterAdministrationHeuristicAlgorithm;
    @Mock
    private ZoneService zoneService;
    @Mock
    private AutonomicClusterManagementService autonomicClusterManagementService;
    @Mock
    private CloudResourcesService cloudResourcesService;
    @Mock
    private ClusterResourcesService clusterResourcesService;
    @Mock
    private HostService hostService;
    @Mock
    private VirtualMachineService virtualMachineService;
    @Mock
    private ThreadUtils threadUtils;
    @Mock
    private UserVmService userVmService;
    @Mock
    private HypervisorManager hypervisorManager;
    @Mock
    private HostResourcesService hostResourcesService;
    @Mock
    private ResourceManager resourceManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void receiveClusterToBeManagedTestClusterIdNull() {
        administrationAgent.receiveClusterToBeManaged(null);
        verifyReceiveClusterToBeManaged(0, 0);
    }

    @Test
    public void receiveClusterToBeManagedTestClusterIdNotNullButClusterWasNotFound() {
        administrationAgent.receiveClusterToBeManaged(1l);
        verifyReceiveClusterToBeManaged(1, 0);
    }

    @Test
    public void receiveClusterToBeManagedTestClusterIdAndClusterNotNull() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        Mockito.doReturn(cluster).when(clusterService).findById(1l);
        Mockito.doNothing().when(administrationAgent).workOnCluster(Mockito.any(ClusterVO.class), Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));

        administrationAgent.receiveClusterToBeManaged(1l);
        verifyReceiveClusterToBeManaged(1, 1);
    }

    @Test
    public void searchClusterToBeManagedTestReturnedClusterNull() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        Mockito.doReturn(null).when(administrationAgent).getClusterToManage(Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));

        Long result = administrationAgent.searchClusterToBeManaged();

        Assert.assertNull(result);
        verifySearchClusterToBeManagedCalledMethods(cluster, 0);
    }

    @Test
    public void searchClusterToBeManagedTestReturnedClusterNotNull() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        Mockito.doReturn(cluster).when(administrationAgent).getClusterToManage(Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));

        Long result = administrationAgent.searchClusterToBeManaged();

        Assert.assertNotNull(result);
        verifySearchClusterToBeManagedCalledMethods(cluster, 1);
    }

    @Test
    public void getClusterToManageTestEmptyEnabledZonesList() {
        Mockito.doReturn(new ArrayList<>()).when(zoneService).listAllZonesEnabled();

        ClusterVO result = administrationAgent.getClusterToManage(clusterAdministrationHeuristicAlgorithm);
        Assert.assertNull(result);
        Mockito.verify(zoneService).listAllZonesEnabled();
        Mockito.verify(administrationAgent, Mockito.times(0)).getClusterToManageFromZone(Mockito.any(ClusterAdministrationHeuristicAlgorithm.class), Mockito.anyLong());
    }

    @Test
    public void getClusterToManageTestNotEmptyEnabledZonesList() {
        List<DataCenterVO> zones = new ArrayList<>();
        zones.add(new DataCenterVO(0, "name", "description", "", "", "", "", "", "", 0l, NetworkType.Basic, "", ""));
        ClusterVO mockedCluster = Mockito.mock(ClusterVO.class);

        Mockito.doReturn(zones).when(zoneService).listAllZonesEnabled();
        Mockito.doReturn(mockedCluster).when(administrationAgent).getClusterToManageFromZone(Mockito.any(ClusterAdministrationHeuristicAlgorithm.class), Mockito.anyLong());

        ClusterVO result = administrationAgent.getClusterToManage(clusterAdministrationHeuristicAlgorithm);

        Assert.assertEquals(mockedCluster, result);
    }

    @Test
    public void getClusterToManageFromZoneTestListOfClustersEmpty() {
        List<ClusterVO> clusters = new ArrayList<>();
        Mockito.doReturn(clusters).when(clusterService).listAllClustersOnZone(Mockito.anyLong());

        ClusterVO result = administrationAgent.getClusterToManageFromZone(clusterAdministrationHeuristicAlgorithm, null);

        Assert.assertNull(result);
        verifyGetClusterToManageFromZoneCalledMethods(0, 0, 0);
    }

    @Test
    public void getClusterToManageFromZoneTestListOfClustersNotEmptyAndClusterIsNotAdministrated() {
        List<ClusterVO> clusters = createClusters(null);
        getClusterToManageFromZoneReturnedValuesSetup(clusters, false, true);

        ClusterVO result = administrationAgent.getClusterToManageFromZone(clusterAdministrationHeuristicAlgorithm, null);

        Assert.assertNotNull(result);
        verifyGetClusterToManageFromZoneCalledMethods(1, 1, 1);
    }

    @Test
    public void getClusterToManageFromZoneTestListOfClustersNotEmptyAndCannotProcess() {
        List<ClusterVO> clusters = createClusters(null);
        getClusterToManageFromZoneReturnedValuesSetup(clusters, false, false);

        ClusterVO result = administrationAgent.getClusterToManageFromZone(clusterAdministrationHeuristicAlgorithm, null);

        Assert.assertNull(result);
        verifyGetClusterToManageFromZoneCalledMethods(1, 1, 0);
    }

    @Test
    public void getClusterToManageFromZoneTestClusterIsRemoved() {
        List<ClusterVO> clusters = createClusters(new Date());
        getClusterToManageFromZoneReturnedValuesSetup(clusters, true, true);

        ClusterVO result = administrationAgent.getClusterToManageFromZone(clusterAdministrationHeuristicAlgorithm, null);

        Assert.assertNull(result);
        verifyGetClusterToManageFromZoneCalledMethods(0, 0, 0);
    }

    @Test
    public void workOnClusterTestWithoutExceptionToCatch() {
        administrationAgent.workOnCluster(createCluster(null), clusterAdministrationHeuristicAlgorithm);
        verifyWorkOnClusterCalledMethods();
    }

    @Test
    public void workOnClusterTestExceptionAtProcessCluster() {
        Mockito.doThrow(Exception.class).when(administrationAgent).processCluster(Mockito.any(ClusterVO.class), Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        administrationAgent.workOnCluster(createCluster(null), clusterAdministrationHeuristicAlgorithm);
        verifyWorkOnClusterCalledMethods();
    }

    @Test
    public void processClusterTestCannotProcessCluster() {
        ClusterVO cluster = createCluster(null);
        processClusterSetupConditionals(false, true);

        administrationAgent.processCluster(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyProcessClusterCalledMethods(1, 0, 0);
    }

    @Test
    public void processClusterTestCannotShutdownHosts() {
        ClusterVO cluster = createCluster(null);
        processClusterSetupConditionals(true, false);

        administrationAgent.processCluster(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyProcessClusterCalledMethods(1, 1, 0);
    }

    @Test
    public void processClusterTestFullExecution() {
        ClusterVO cluster = createCluster(null);
        processClusterSetupConditionals(true, true);

        administrationAgent.processCluster(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyProcessClusterCalledMethods(1, 1, 1);
    }

    @Test
    public void shutdownIdleHostsTestFullExecution() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        List<HostResources> idleHosts = createHosts(1);
        shutdownIdleHostsTestSetupMethodsExecution(idleHosts, true, true, true);

        administrationAgent.shutdownIdleHosts(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyShutdownIdleHostsCalledMethods(1, 1, 1, 1);
    }

    @Test
    public void shutdownIdleHostsTestHeuristicCannotShutdownHosts() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        List<HostResources> idleHosts = createHosts(1);
        shutdownIdleHostsTestSetupMethodsExecution(idleHosts, false, true, true);

        administrationAgent.shutdownIdleHosts(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyShutdownIdleHostsCalledMethods(0, 0, 0, 0);
    }

    private List<HostResources> createHosts(int size) {
        List<HostResources> idleHosts = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            HostResources host = new HostResources();
            host.setHostId(0);
            idleHosts.add(host);
        }
        return idleHosts;
    }

    @Test
    public void shutdownIdleHostsTestEmptyIdleHostsList() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        List<HostResources> idleHosts = new ArrayList<>();
        shutdownIdleHostsTestSetupMethodsExecution(idleHosts, true, true, true);

        administrationAgent.shutdownIdleHosts(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyShutdownIdleHostsCalledMethods(1, 0, 0, 0);
    }

    @Test
    public void shutdownIdleHostsTestHeuristicCannotPowerOffAnotherHosts() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        List<HostResources> idleHosts = createHosts(1);
        shutdownIdleHostsTestSetupMethodsExecution(idleHosts, true, false, true);

        administrationAgent.shutdownIdleHosts(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyShutdownIdleHostsCalledMethods(1, 1, 0, 0);
    }

    @Test
    public void shutdownIdleHostsTestHeuristicCannotPowerOffHost() {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        List<HostResources> idleHosts = createHosts(1);
        shutdownIdleHostsTestSetupMethodsExecution(idleHosts, true, true, false);

        administrationAgent.shutdownIdleHosts(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyShutdownIdleHostsCalledMethods(1, 1, 1, 0);
    }

    @Test
    public void createAllClustersResourcesUpTestFullExecution() {
        List<ClusterVO> clustersVO = createClusters(null);
        Mockito.doReturn(clustersVO).when(clusterService).listAllClusters();
        Mockito.doReturn(new ArrayList<>()).when(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        List<ClusterResourcesUp> result = administrationAgent.createAllClustersResourcesUp();

        verifyCreateAllClustersResourcesUpCalledMethods(1);
        Assert.assertEquals(clustersVO.size(), result.size());
    }

    @Test
    public void createAllClustersResourcesUpTestRemovedCluster() {
        List<ClusterVO> clustersVO = createClusters(new Date());
        Mockito.doReturn(clustersVO).when(clusterService).listAllClusters();
        Mockito.doReturn(new ArrayList<>()).when(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        List<ClusterResourcesUp> result = administrationAgent.createAllClustersResourcesUp();

        verifyCreateAllClustersResourcesUpCalledMethods(0);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void createAllClustersResourcesUpTestEmptyClustersList() {
        Mockito.doReturn(new ArrayList<>()).when(clusterService).listAllClusters();
        Mockito.doReturn(new ArrayList<>()).when(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        List<ClusterResourcesUp> result = administrationAgent.createAllClustersResourcesUp();

        verifyCreateAllClustersResourcesUpCalledMethods(0);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void mapAndExecuteVMsMigrationsTestEmptyHostList() {
        ClusterVO cluster = createCluster(null);
        List<HostResources> hosts = new ArrayList<>();
        setupMapAndExecuteVMsMigrationsTest(hosts, true);

        administrationAgent.mapAndExecuteVMsMigrations(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyMapAndExecuteVMsMigrationsCalledMethods(0, 0);
    }

    @Test
    public void mapAndExecuteVMsMigrationsTestOneHost() {
        ClusterVO cluster = createCluster(null);
        List<HostResources> hosts = createHosts(1);
        setupMapAndExecuteVMsMigrationsTest(hosts, true);

        administrationAgent.mapAndExecuteVMsMigrations(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyMapAndExecuteVMsMigrationsCalledMethods(0, 0);
    }

    @Test
    public void mapAndExecuteVMsMigrationsTestFullExecution() {
        ClusterVO cluster = createCluster(null);
        List<HostResources> hosts = createHosts(2);
        setupMapAndExecuteVMsMigrationsTest(hosts, true);

        administrationAgent.mapAndExecuteVMsMigrations(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyMapAndExecuteVMsMigrationsCalledMethods(1, 1);
    }

    @Test
    public void mapAndExecuteVMsMigrationsTestEmptyMigrationsMap() {
        ClusterVO cluster = createCluster(null);
        List<HostResources> hosts = createHosts(2);
        setupMapAndExecuteVMsMigrationsTest(hosts, false);

        administrationAgent.mapAndExecuteVMsMigrations(cluster, clusterAdministrationHeuristicAlgorithm);

        verifyMapAndExecuteVMsMigrationsCalledMethods(1, 0);
    }

    @Test
    public void migrateVMTestTargetHostEqualsVmHost() throws Exception {
        VMInstanceVO vmInstanceVo = migrateVMSetupVmInstanceVo(1l);
        HostVO hostVo = migrateVMSetupHostVo(1l);
        Mockito.doReturn(vmInstanceVo).when(userVmService).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));
        Mockito.doReturn(new HostVO("")).when(hostService).findHostById(Mockito.anyLong());

        administrationAgent.migrateVM(vmInstanceVo, hostVo);

        Mockito.verify(userVmService, Mockito.times(0)).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));
    }

    @Test
    public void migrateVMTestWithException() throws Exception {
        VMInstanceVO vmInstanceVo = migrateVMSetupVmInstanceVo(0l);
        HostVO hostVo = migrateVMSetupHostVo(1l);
        Mockito.doThrow(Exception.class).when(userVmService).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));

        administrationAgent.migrateVM(vmInstanceVo, hostVo);

        Mockito.verify(userVmService, Mockito.times(1)).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));
    }

    @Test
    public void migrateVMTestTargetHostDifferentFromVmHost() throws Exception {
        VMInstanceVO vmInstanceVo = migrateVMSetupVmInstanceVo(0l);
        HostVO hostVo = migrateVMSetupHostVo(1l);
        Mockito.doReturn(vmInstanceVo).when(userVmService).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));

        administrationAgent.migrateVM(vmInstanceVo, hostVo);

        Mockito.verify(userVmService, Mockito.times(1)).migrateVirtualMachine(Mockito.anyLong(), Mockito.any(HostVO.class));
    }

    @Test
    public void shutdownHostTestHostWithoutVms() {
        setupShutdownHostTest(new ArrayList<VMInstanceVO>());
        administrationAgent.shutdownHost(0l);
        verifyShutdownHostCalledMethods(1, 1, 1);
    }

    @Test
    public void shutdownHostTestExceptionThrownByShutdownHost() {
        setupShutdownHostTest(new ArrayList<VMInstanceVO>());
        Mockito.doThrow(CloudRuntimeException.class).when(hypervisorManager).shutdownHost(Mockito.any(HostVO.class));
        administrationAgent.shutdownHost(0l);
        verifyShutdownHostCalledMethods(1, 1, 1);
    }

    @Test
    public void shutdownHostTestExceptionThrownByPutHostInMaintenance() {
        setupShutdownHostTest(new ArrayList<VMInstanceVO>());
        Mockito.doThrow(CloudRuntimeException.class).when(administrationAgent).putHostInMaintenance(Mockito.anyLong());
        administrationAgent.shutdownHost(0l);
        verifyShutdownHostCalledMethods(1, 1, 0);
    }

    @Test
    public void shutdownHostTestExceptionThrownByCheckIfHostIsUpAndEnabled() {
        setupShutdownHostTest(new ArrayList<VMInstanceVO>());
        Mockito.doThrow(CloudRuntimeException.class).when(administrationAgent).checkIfHostIsUpAndEnabled(Mockito.anyLong());
        administrationAgent.shutdownHost(0l);
        verifyShutdownHostCalledMethods(0, 0, 0);
    }

    @Test
    public void shutdownHostTestHostWithVms() {
        setupShutdownHostTest(createVms());

        administrationAgent.shutdownHost(0l);

        verifyShutdownHostCalledMethods(1, 0, 0);
    }

    @Test
    public void shutdownHostTestHostWithVmsExceptionThrownByCheckIfHostIsUpAndEnabled() {
        setupShutdownHostTest(createVms());
        Mockito.doThrow(CloudRuntimeException.class).when(administrationAgent).checkIfHostIsUpAndEnabled(Mockito.anyLong());

        administrationAgent.shutdownHost(0l);

        verifyShutdownHostCalledMethods(0, 0, 0);
    }

    @Test
    public void getClusterUpHostsTest() {
        ClusterVO cluster = createCluster(null);
        List<HostVO> hosts = setupGetClusterUpHostsTest(2);

        List<HostResources> result = administrationAgent.getClusterUpHosts(cluster);

        Assert.assertEquals(hosts.size(), result.size());
        verifyGetClusterUpHostsCalledMethods(2);
    }

    @Test
    public void getClusterUpHostsTestNoHostRunning() {
        ClusterVO cluster = createCluster(null);
        List<HostVO> hosts = setupGetClusterUpHostsTest(0);

        List<HostResources> result = administrationAgent.getClusterUpHosts(cluster);

        Assert.assertEquals(hosts.size(), result.size());
        verifyGetClusterUpHostsCalledMethods(0);
    }

    @Test
    public void getClusterIdleHostsTest() {
        ClusterVO cluster = createCluster(null);
        List<HostResources> hosts = createHosts(2);
        List<VmResources> vmsResources = new ArrayList<>();
        vmsResources.add(new VmResources(0l, 1, 1000, 1000));
        hosts.get(0).setVmsResources(vmsResources);

        Mockito.doReturn(hosts).when(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));

        List<HostResources> result = administrationAgent.getClusterIdleHosts(cluster);

        Mockito.verify(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(hosts.get(1), result.get(0));
    }

    @Test(expected = CloudRuntimeException.class)
    public void checkIfHostIsUpAndEnabledTestExpectCloudRuntimeException() {
        Mockito.doReturn(false).when(hostService).isHostUpAndEnabled(Mockito.anyLong());
        administrationAgent.checkIfHostIsUpAndEnabled(Mockito.anyLong());
        Mockito.verify(hostService).isHostUpAndEnabled(Mockito.anyLong());
    }

    @Test
    public void checkIfHostIsUpAndEnabledTestDoesNotExpectCloudRuntimeException() {
        Mockito.doReturn(true).when(hostService).isHostUpAndEnabled(Mockito.anyLong());
        administrationAgent.checkIfHostIsUpAndEnabled(Mockito.anyLong());
        Mockito.verify(hostService).isHostUpAndEnabled(Mockito.anyLong());
    }

    @Test
    public void putHostInMaintenanceTestSmoothExecution() throws AgentUnavailableException {
        setupPutHostInMaintenance(false);

        administrationAgent.putHostInMaintenance(0l);

        verifyPutHostInMaintenanceCalledMethods(1, 1);
    }

    @Test(expected = CloudRuntimeException.class)
    public void putHostInMaintenanceTestExceptionThrownAtMaintain() throws AgentUnavailableException {
        setupPutHostInMaintenance(false);
        Mockito.doThrow(AgentUnavailableException.class).when(resourceManager).maintain(Mockito.anyLong());

        administrationAgent.putHostInMaintenance(0l);

        verifyPutHostInMaintenanceCalledMethods(0, 0);
    }

    @Test(expected = CloudRuntimeException.class)
    public void putHostInMaintenanceTestHostInMaintenanceError() throws AgentUnavailableException {
        setupPutHostInMaintenance(true);

        administrationAgent.putHostInMaintenance(0l);

        verifyPutHostInMaintenanceCalledMethods(1, 0);
    }

    private void verifyPutHostInMaintenanceCalledMethods(int times, int isHostInPreparedForMaintenanceTimes) throws AgentUnavailableException {
        Mockito.verify(resourceManager).maintain(Mockito.anyLong());
        Mockito.verify(threadUtils, Mockito.times(times)).sleepThread(Mockito.anyInt());
        InOrder hostServiceInOrder = Mockito.inOrder(hostService);
        hostServiceInOrder.verify(hostService, Mockito.times(times)).isHostInMaintenanceError(Mockito.anyLong());
        hostServiceInOrder.verify(hostService, Mockito.times(isHostInPreparedForMaintenanceTimes)).isHostInPreparedForMaintenance(Mockito.anyLong());
    }

    private void setupPutHostInMaintenance(boolean isHostInMaintenanceError) throws AgentUnavailableException {
        Mockito.doReturn(true).when(resourceManager).maintain(Mockito.anyLong());
        Mockito.doNothing().when(threadUtils).sleepThread(Mockito.anyInt());
        Mockito.doReturn(isHostInMaintenanceError).when(hostService).isHostInMaintenanceError(Mockito.anyLong());
        Mockito.doReturn(false).when(hostService).isHostInPreparedForMaintenance(Mockito.anyLong());
    }

    private void verifyReceiveClusterToBeManaged(int executionTimes, int executionTimesAfterClusterNullCheck) {
        Mockito.verify(clusterService, Mockito.times(executionTimes)).findById(Mockito.anyLong());
        Mockito.verify(autonomicManagementHeuristicService, Mockito.times(executionTimesAfterClusterNullCheck)).getAdministrationAlgorithm();
        Mockito.verify(administrationAgent, Mockito.times(executionTimesAfterClusterNullCheck)).workOnCluster(Mockito.any(ClusterVO.class),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
    }

    private void verifySearchClusterToBeManagedCalledMethods(ClusterVO cluster, int times) {
        Mockito.verify(autonomicManagementHeuristicService).getAdministrationAlgorithm();
        Mockito.verify(administrationAgent).getClusterToManage(Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.verify(cluster, Mockito.times(times)).getId();
    }

    private void verifyGetClusterToManageFromZoneCalledMethods(int timesIsClusterBeingAdministrated, int timesCanProcessCluster, int timesSetClusterWorkInProgress) {
        Mockito.verify(clusterService).listAllClustersOnZone(Mockito.anyLong());
        InOrder inOrder = Mockito.inOrder(autonomicClusterManagementService);
        inOrder.verify(autonomicClusterManagementService, Mockito.times(timesIsClusterBeingAdministrated)).isClusterBeingAdministrated(Mockito.anyLong());
        inOrder.verify(autonomicClusterManagementService, Mockito.times(timesCanProcessCluster)).canProcessCluster(Mockito.anyLong(),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        inOrder.verify(autonomicClusterManagementService, Mockito.times(timesSetClusterWorkInProgress)).setClusterWorkInProgress(Mockito.anyLong());
    }

    private List<ClusterVO> createClusters(Date date) {
        List<ClusterVO> clusters = new ArrayList<>();
        ClusterVO cluster = createCluster(date);
        clusters.add(cluster);
        return clusters;
    }

    private ClusterVO createCluster(Date date) {
        ClusterVO cluster = Mockito.mock(ClusterVO.class);
        Mockito.doReturn(date).when(cluster).getRemoved();
        Mockito.doReturn(0l).when(cluster).getId();
        Mockito.doReturn("name").when(cluster).getName();
        return cluster;
    }

    private void getClusterToManageFromZoneReturnedValuesSetup(List<ClusterVO> clusters, boolean isClusterBeingAdministrated, boolean canProcessCluster) {
        Mockito.doReturn(clusters).when(clusterService).listAllClustersOnZone(Mockito.anyLong());
        Mockito.doReturn(isClusterBeingAdministrated).when(autonomicClusterManagementService).isClusterBeingAdministrated(Mockito.anyLong());
        Mockito.doReturn(canProcessCluster).when(autonomicClusterManagementService).canProcessCluster(Mockito.anyLong(),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
    }

    private void verifyProcessClusterCalledMethods(int canProcessClusterTimes, int canHeuristicShutdownHostsTimes, int shutdownIdleHostsTimes) {
        Mockito.verify(autonomicClusterManagementService, Mockito.times(canProcessClusterTimes)).canProcessCluster(Mockito.anyLong(),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.verify(administrationAgent, Mockito.times(canHeuristicShutdownHostsTimes)).mapAndExecuteVMsMigrations(Mockito.any(ClusterVO.class),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(canHeuristicShutdownHostsTimes)).canHeuristicShutdownHosts();
        Mockito.verify(administrationAgent, Mockito.times(shutdownIdleHostsTimes)).shutdownIdleHosts(Mockito.any(ClusterVO.class),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
    }

    private void processClusterSetupConditionals(boolean canProcessCluster, boolean canHeuristicShutdownHosts) {
        Mockito.doReturn(canProcessCluster).when(autonomicClusterManagementService).canProcessCluster(Mockito.anyLong(),
                Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.doReturn(canHeuristicShutdownHosts).when(clusterAdministrationHeuristicAlgorithm).canHeuristicShutdownHosts();
        Mockito.doNothing().when(administrationAgent).mapAndExecuteVMsMigrations(Mockito.any(ClusterVO.class), Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.doNothing().when(administrationAgent).shutdownIdleHosts(Mockito.any(ClusterVO.class), Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
    }

    private void shutdownIdleHostsTestSetupMethodsExecution(List<HostResources> idleHosts, boolean canHeuristicShutdownHosts, boolean canPowerOffAnotherHostInCloud,
            boolean canPowerOffHost) {
        ClusterResourcesUp clusterResourcesUp = Mockito.mock(ClusterResourcesUp.class);
        List<ClusterResourcesUp> clusterResourcesUpList = new ArrayList<>();
        clusterResourcesUpList.add(clusterResourcesUp);
        CloudResources cloudResources = Mockito.mock(CloudResources.class);

        Mockito.doReturn(canHeuristicShutdownHosts).when(clusterAdministrationHeuristicAlgorithm).canHeuristicShutdownHosts();
        Mockito.doReturn(idleHosts).when(administrationAgent).getClusterIdleHosts(Mockito.any(ClusterVO.class));
        Mockito.doReturn(idleHosts).when(clusterAdministrationHeuristicAlgorithm).rankHostToPowerOff(Matchers.anyListOf(HostResources.class));
        Mockito.doReturn(cloudResources).when(cloudResourcesService).createCloudResources(Matchers.anyListOf(ClusterResourcesUp.class));
        Mockito.doReturn(canPowerOffAnotherHostInCloud).when(clusterAdministrationHeuristicAlgorithm).canPowerOffAnotherHostInCloud(Mockito.any(CloudResources.class));
        Mockito.doReturn(canPowerOffHost).when(clusterAdministrationHeuristicAlgorithm).canPowerOffHost(Mockito.any(HostResources.class), Mockito.any(CloudResources.class));
        Mockito.doNothing().when(administrationAgent).shutdownHost(Mockito.anyLong());
    }

    private void verifyShutdownIdleHostsCalledMethods(int getClusterIdleHostsTimes, int rankHostToPowerOffTimes, int canPowerOffHostTimes, int shutdownHostTimes) {
        Mockito.verify(clusterAdministrationHeuristicAlgorithm).canHeuristicShutdownHosts();
        Mockito.verify(administrationAgent, Mockito.times(getClusterIdleHostsTimes)).getClusterIdleHosts(Mockito.any(ClusterVO.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(rankHostToPowerOffTimes)).rankHostToPowerOff(Matchers.anyListOf(HostResources.class));
        Mockito.verify(administrationAgent, Mockito.times(rankHostToPowerOffTimes)).createAllClustersResourcesUp();
        Mockito.verify(cloudResourcesService, Mockito.times(rankHostToPowerOffTimes)).createCloudResources(Matchers.anyListOf(ClusterResourcesUp.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(rankHostToPowerOffTimes)).canPowerOffAnotherHostInCloud(Mockito.any(CloudResources.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(canPowerOffHostTimes)).canPowerOffHost(Mockito.any(HostResources.class),
                Mockito.any(CloudResources.class));
        Mockito.verify(administrationAgent, Mockito.times(shutdownHostTimes)).shutdownHost(Mockito.anyLong());
    }

    private void verifyCreateAllClustersResourcesUpCalledMethods(int createClusterResourcesUpTimes) {
        Mockito.verify(clusterService).listAllClusters();
        Mockito.verify(clusterResourcesService, Mockito.times(createClusterResourcesUpTimes)).createClusterResourcesUp(Mockito.anyLong(), Mockito.anyString(),
                Matchers.anyListOf(HostResources.class));
    }

    private void setupMapAndExecuteVMsMigrationsTest(List<HostResources> hosts, boolean hasMigration) {
        Mockito.doReturn(hosts).when(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        Mockito.doReturn(hosts).when(clusterAdministrationHeuristicAlgorithm).rankHosts(Matchers.anyListOf(HostResources.class));

        if (!hasMigration || CollectionUtils.isEmpty(hosts)) {
            Mockito.doReturn(new HashMap<>()).when(clusterAdministrationHeuristicAlgorithm).mapVMsToHost(Matchers.anyListOf(HostResources.class));
        } else {
            Map<Long, HostResources> migrations = new HashMap<>();
            migrations.put(0l, hosts.get(0));
            Mockito.doReturn(migrations).when(clusterAdministrationHeuristicAlgorithm).mapVMsToHost(Matchers.anyListOf(HostResources.class));
        }

        Mockito.doReturn(new HostVO("")).when(hostService).findHostById(Mockito.anyLong());

        VMInstanceVO vmInstanceVo = Mockito.mock(VMInstanceVO.class);
        Mockito.doReturn(vmInstanceVo).when(virtualMachineService).searchVmInstanceById(Mockito.anyLong());
    }

    private void verifyMapAndExecuteVMsMigrationsCalledMethods(int rankHostsTimes, int findHostByIdTimes) {
        Mockito.verify(administrationAgent).getClusterUpHosts(Mockito.any(ClusterVO.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(rankHostsTimes)).rankHosts(Matchers.anyListOf(HostResources.class));
        Mockito.verify(clusterAdministrationHeuristicAlgorithm, Mockito.times(rankHostsTimes)).mapVMsToHost(Matchers.anyListOf(HostResources.class));
        Mockito.verify(hostService, Mockito.times(findHostByIdTimes)).findHostById(Mockito.anyLong());
        Mockito.verify(virtualMachineService, Mockito.times(findHostByIdTimes)).searchVmInstanceById(Mockito.anyLong());
        Mockito.verify(administrationAgent, Mockito.times(findHostByIdTimes)).migrateVM(Mockito.any(VMInstanceVO.class), Mockito.any(HostVO.class));
    }

    private HostVO migrateVMSetupHostVo(long targetHostId) {
        HostVO hostVo = Mockito.mock(HostVO.class);
        Mockito.doReturn(targetHostId).when(hostVo).getId();
        return hostVo;
    }

    private VMInstanceVO migrateVMSetupVmInstanceVo(long hostId) {
        VMInstanceVO vmInstanceVo = Mockito.mock(VMInstanceVO.class);
        Mockito.doReturn(0l).when(vmInstanceVo).getId();
        Mockito.doReturn(hostId).when(vmInstanceVo).getHostId();
        return vmInstanceVo;
    }

    private void setupShutdownHostTest(List<VMInstanceVO> vms) {
        Mockito.doNothing().when(administrationAgent).checkIfHostIsUpAndEnabled(Mockito.anyLong());
        Mockito.doReturn(vms).when(hostService).listAllVmsFromHost(Mockito.anyLong());
        Mockito.doNothing().when(administrationAgent).putHostInMaintenance(Mockito.anyLong());
        Mockito.doReturn(new HostVO("")).when(hostService).findHostById(Mockito.anyLong());
        Mockito.doNothing().when(hypervisorManager).shutdownHost(Mockito.any(HostVO.class));
    }

    private void verifyShutdownHostCalledMethods(int listAllVmsFromHostTimes, int timesIfHostEmpty, int shutdownHostTimes) {
        Mockito.verify(administrationAgent).checkIfHostIsUpAndEnabled(Mockito.anyLong());
        Mockito.verify(hostService, Mockito.times(listAllVmsFromHostTimes)).listAllVmsFromHost(Mockito.anyLong());
        Mockito.verify(administrationAgent, Mockito.times(timesIfHostEmpty)).putHostInMaintenance(Mockito.anyLong());
        Mockito.verify(hostService, Mockito.times(shutdownHostTimes)).findHostById(Mockito.anyLong());
        Mockito.verify(hypervisorManager, Mockito.times(shutdownHostTimes)).shutdownHost(Mockito.any(HostVO.class));
    }

    private List<HostVO> setupGetClusterUpHostsTest(int numberOfHosts) {
        List<HostVO> hosts = new ArrayList<>();
        for (int i = 0; i < numberOfHosts; i++) {
            hosts.add(new HostVO(""));
        }
        Mockito.doReturn(hosts).when(hostService).listAllHostsInCluster(Mockito.any(ClusterVO.class));
        Mockito.doReturn(new HostResources()).when(hostResourcesService).createHostResources(Mockito.any(HostVO.class));
        return hosts;
    }

    private void verifyGetClusterUpHostsCalledMethods(int numberOfHosts) {
        Mockito.verify(hostService).listAllHostsInCluster(Mockito.any(ClusterVO.class));
        Mockito.verify(hostResourcesService, Mockito.times(numberOfHosts)).createHostResources(Mockito.any(HostVO.class));
    }

    private void verifyWorkOnClusterCalledMethods() {
        Mockito.verify(administrationAgent).processCluster(Mockito.any(ClusterVO.class), Mockito.any(ClusterAdministrationHeuristicAlgorithm.class));
        Mockito.verify(autonomicClusterManagementService).markAdministrationStatusInClusterAsDone(Mockito.anyLong());
    }

    private List<VMInstanceVO> createVms() {
        List<VMInstanceVO> vms = new ArrayList<>();
        VMInstanceVO vm = Mockito.mock(VMInstanceVO.class);
        vms.add(vm);
        return vms;
    }

}
