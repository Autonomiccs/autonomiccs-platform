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
package br.com.autonomiccs.autonomic.administration.plugin.hypervisors.xenserver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.xmlrpc.XmlRpcException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.cloud.host.HostVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.xenserver.resource.XenServerConnectionPool;
import com.cloud.utils.exception.CloudRuntimeException;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;

import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

@PrepareForTest({ Host.class, Pool.class })
@RunWith(PowerMockRunner.class)
public class XenHypervisorTest {

    private XenHypervisor xenHypervisor;
    @Mock
    private ThreadUtils threadUtils;
    @Mock
    private HostUtils hostUtils;
    @Mock
    private ShellCommandUtils shellCommandUtils;
    @Mock
    private HostService hostService;

    private static final String EXPECTED_FIND_ARP_FOR_HOST_COMMAND = "arp -n %s";
    private static final String EXPECTED_REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT = ".+([A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}).+";
    private Pattern expectedCompileRegexToGetArp = Pattern.compile(EXPECTED_REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT);

    private Connection conn;
    private Host host;

    @Before
    public void setup() {
        xenHypervisor = Mockito.spy(new XenHypervisor());
        xenHypervisor.hostUtils = hostUtils;
        xenHypervisor.hostService = hostService;
        xenHypervisor.shellCommandUtils = shellCommandUtils;
        xenHypervisor.threadUtils = threadUtils;

        conn = Mockito.mock(Connection.class);
        host = Mockito.mock(Host.class);
    }

    @Test
    public void shutdownHostTest() throws Exception {
        HostVO hostVo = setupShutdownHostTest();
        xenHypervisor.shutdownHost(hostVo);
        verifyShutdownHostCalledMethods();
    }

    @Test
    public void shutdownHostTestExceptionThrownByChangeMasterIfNeeded() throws Exception {
        HostVO hostVo = setupShutdownHostTest();
        xenHypervisor.shutdownHost(hostVo);
        verifyShutdownHostCalledMethods();
    }

    @Test
    public void getHostPrivateMacAddressTest() {
        String expected = "00:00:00:00:00:00";
        String arpResult = "0.0.0.0 ether 00:00:00:00:00:00 C interface";
        HostVO hostVo = new HostVO("");
        hostVo.setPrivateIpAddress("");

        Mockito.doReturn(arpResult).when(shellCommandUtils).executeCommand(Mockito.anyString());

        String result = xenHypervisor.getHostPrivateMacAddress(hostVo);

        Assert.assertEquals(EXPECTED_REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT, XenHypervisor.REGEX_GET_ARP_FROM_ARP_COMMAND_OUTPUT);
        Assert.assertEquals(EXPECTED_FIND_ARP_FOR_HOST_COMMAND, XenHypervisor.FIND_ARP_FOR_HOST_COMMAND);
        Assert.assertEquals(expectedCompileRegexToGetArp.toString(), xenHypervisor.compileRegexToGetArp.toString());
        Assert.assertEquals(expected, result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getHostPrivateMacAddressTestExpectException() {
        String arpResult = "0.0.0.0 ether 00:00:00:00:00 C interface";
        HostVO hostVo = new HostVO("");
        hostVo.setPrivateIpAddress("");

        Mockito.doReturn(arpResult).when(shellCommandUtils).executeCommand(Mockito.anyString());

        xenHypervisor.getHostPrivateMacAddress(hostVo);
    }

    @Test
    public void disableAndShutdownHostTest() throws Exception {
        Mockito.doNothing().when(host).disable(conn);
        Mockito.doNothing().when(host).shutdown(conn);

        xenHypervisor.disableAndShutdownHost(conn, host);

        InOrder inOrder = Mockito.inOrder(host);
        inOrder.verify(host).disable(Mockito.any(Connection.class));
        inOrder.verify(host).shutdown(Mockito.any(Connection.class));
    }

    @Test
    public void changeMasterIfNeededTest() throws Exception {
        setupChangeMasterIfNeeded(false);
        xenHypervisor.changeMasterIfNeeded(conn, host, "hostUuid");
        verifyChangeMasterIfNeededCalledMethods(1);
    }

    @Test
    public void changeMasterIfNeededTestIsLastHostOnPool() throws Exception {
        setupChangeMasterIfNeeded(true);
        xenHypervisor.changeMasterIfNeeded(conn, host, "hostUuid");
        verifyChangeMasterIfNeededCalledMethods(0);
    }

    @Test
    public void isLastHostOnPoolTest() throws Exception {
        Set<Host> setHosts = new HashSet<Host>();
        setHosts.add(host);
        setIsLastHostOnPool(setHosts);

        boolean result = xenHypervisor.isLastHostOnPool(conn);
        Assert.assertTrue(result);
    }

    @Test
    public void isLastHostOnPoolTestEmptySet() throws Exception {
        Set<Host> setHosts = new HashSet<Host>();
        setIsLastHostOnPool(setHosts);

        boolean result = xenHypervisor.isLastHostOnPool(conn);
        Assert.assertFalse(result);
    }

    @Test
    public void getMasterHostTest() throws Exception {
        Pool pool = Mockito.mock(Pool.class);
        pool.getMaster(Mockito.any(Connection.class));
        Set<Pool> setOfPools = new HashSet<Pool>();
        setOfPools.add(pool);
        PowerMockito.mockStatic(Pool.class);
        PowerMockito.doReturn(setOfPools).when(Pool.class, "getAll", conn);

        xenHypervisor.getMasterHost(conn);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getMasterHostTestEmptyPool() throws Exception {
        Set<Pool> setOfPools = new HashSet<Pool>();
        PowerMockito.mockStatic(Pool.class);
        PowerMockito.doReturn(setOfPools).when(Pool.class, "getAll", conn);

        xenHypervisor.getMasterHost(conn);
    }

    @Test
    public void changePoolMasterHostTestSameHostAndMasterUuid() throws Exception {
        setChangePoolMasterHost("hostUuid", true);
        xenHypervisor.changePoolMasterHost(conn, "hostUuid");
        verifyChangePoolMasterHostCalledMethods(0, 0);
    }

    @Test
    public void changePoolMasterHostTestHostUnreachable() throws Exception {
        setChangePoolMasterHost("uuid", false);
        xenHypervisor.changePoolMasterHost(conn, "hostUuid");
        verifyChangePoolMasterHostCalledMethods(1, 0);
    }

    @Test
    public void changePoolMasterHostTestFulExecution() throws Exception {
        setChangePoolMasterHost("uuid", true);
        xenHypervisor.changePoolMasterHost(conn, "hostUuid");
        verifyChangePoolMasterHostCalledMethods(1, 1);
    }

    @Test
    public void waitChangePoolMasterHostTest() throws Exception {
        setupWaitChangePoolMasterHost("uuidDifferentFromHost");
        xenHypervisor.waitChangePoolMasterHost(host, conn, "uuid");
    }

    @Test(expected = CloudRuntimeException.class)
    public void waitChangePoolMasterHostTestExpectCloudRuntimeException() throws Exception {
        setupWaitChangePoolMasterHost("uuid");
        xenHypervisor.waitChangePoolMasterHost(host, conn, "uuid");
        Mockito.verify(threadUtils, Mockito.times(90)).sleepThread(Mockito.anyInt());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getConnectionTest() throws Exception {
        HostVO hostVo = setHostVoWithDetails();

        XenServerConnectionPool connPool = Mockito.mock(XenServerConnectionPool.class);
        Mockito.doReturn(conn).when(connPool).getConnect(Mockito.anyString(), Mockito.anyString(), Mockito.any(Queue.class));
        xenHypervisor.connPool = connPool;

        xenHypervisor.getConnection(hostVo);

        InOrder inOrder = Mockito.inOrder(xenHypervisor);
        inOrder.verify(xenHypervisor).getUsername(Mockito.anyMap());
        inOrder.verify(xenHypervisor).getPassword(Mockito.anyMap());
        Mockito.verify(connPool).getConnect(Mockito.anyString(), Mockito.anyString(), Mockito.any(Queue.class));
    }

    @Test
    public void getUsernameTest() {
        HostVO hostVo = setHostVoWithDetails();
        String result = xenHypervisor.getUsername(hostVo.getDetails());
        Assert.assertEquals("name".toString(), result.toString());
    }

    @Test
    public void getPassswordTest() {
        HostVO hostVo = setHostVoWithDetails();
        Queue<String> result = xenHypervisor.getPassword(hostVo.getDetails());
        Assert.assertEquals("pass".toString(), result.poll().toString());
    }

    @Test
    public void supportsHypervisorTest() {
        boolean result = xenHypervisor.supportsHypervisor(HypervisorType.XenServer);
        Assert.assertTrue(result);
    }

    private void verifyShutdownHostCalledMethods() throws Exception {
        InOrder inOrderXenHypervisor = Mockito.inOrder(xenHypervisor);
        inOrderXenHypervisor.verify(xenHypervisor).getHostPrivateMacAddress(Mockito.any(HostVO.class));
        Mockito.verify(hostService).updateHostPrivaceMacAddress(Mockito.any(HostVO.class), Mockito.anyString());
        inOrderXenHypervisor.verify(xenHypervisor).getConnection(Mockito.any(HostVO.class));
        inOrderXenHypervisor.verify(xenHypervisor).changeMasterIfNeeded(Mockito.any(Connection.class), Mockito.any(Host.class), Mockito.anyString());
        inOrderXenHypervisor.verify(xenHypervisor).disableAndShutdownHost(Mockito.any(Connection.class), Mockito.any(Host.class));
        Mockito.verify(threadUtils, Mockito.times(2)).sleepThread(Mockito.anyInt());
        Mockito.verify(hostUtils, Mockito.times(2)).isHostReachable(Mockito.anyString());

        PowerMockito.verifyStatic();
        Host.getByUuid(Mockito.any(Connection.class), Mockito.anyString());
    }

    private HostVO setupShutdownHostTest() throws Exception {
        HostVO hostVo = new HostVO("");
        Mockito.doReturn("").when(xenHypervisor).getHostPrivateMacAddress(Mockito.any(HostVO.class));
        Mockito.doNothing().when(hostService).updateHostPrivaceMacAddress(Mockito.any(HostVO.class), Mockito.anyString());
        Connection conn = Mockito.mock(Connection.class);
        Mockito.doReturn(conn).when(xenHypervisor).getConnection(Mockito.any(HostVO.class));

        PowerMockito.mockStatic(Host.class);
        PowerMockito.when(Host.getByUuid(Mockito.any(Connection.class), Mockito.anyString())).thenReturn(host);

        Mockito.doReturn(host).when(xenHypervisor).getMasterHost(Mockito.any(Connection.class));
        Mockito.doNothing().when(xenHypervisor).changeMasterIfNeeded(Mockito.any(Connection.class), Mockito.any(Host.class), Mockito.anyString());
        Mockito.doNothing().when(xenHypervisor).disableAndShutdownHost(Mockito.any(Connection.class), Mockito.any(Host.class));
        Mockito.doNothing().when(threadUtils).sleepThread(Mockito.anyInt());
        //        Mockito.doReturn(false).when(hostUtils).isHostReachable(Mockito.anyString());//TODO
        Mockito.when(hostUtils.isHostReachable(Mockito.anyString())).thenReturn(true, false);
        return hostVo;
    }

    private void verifyChangeMasterIfNeededCalledMethods(int times) throws BadServerResponse, XenAPIException, XmlRpcException {
        InOrder inOrder = Mockito.inOrder(xenHypervisor);
        inOrder.verify(xenHypervisor).isLastHostOnPool(Mockito.any(Connection.class));
        inOrder.verify(xenHypervisor, Mockito.times(times)).changePoolMasterHost(Mockito.any(Connection.class), Mockito.anyString());
        inOrder.verify(xenHypervisor, Mockito.times(times)).waitChangePoolMasterHost(Mockito.any(Host.class), Mockito.any(Connection.class), Mockito.anyString());
    }

    private void setupChangeMasterIfNeeded(boolean isLastHostOnPool) throws Exception {
        Mockito.doReturn("hostUuid").when(host).getUuid(Mockito.any(Connection.class));
        Mockito.doReturn(isLastHostOnPool).when(xenHypervisor).isLastHostOnPool(Mockito.any(Connection.class));
        Mockito.doNothing().when(xenHypervisor).changePoolMasterHost(Mockito.any(Connection.class), Mockito.anyString());
        Mockito.doNothing().when(xenHypervisor).waitChangePoolMasterHost(Mockito.any(Host.class), Mockito.any(Connection.class), Mockito.anyString());
    }

    private void setupWaitChangePoolMasterHost(String uuid) throws Exception {
        Mockito.doNothing().when(threadUtils).sleepThread(Mockito.anyInt());
        Mockito.doReturn(uuid).when(host).getUuid(Mockito.any(Connection.class));
    }

    private HostVO setHostVoWithDetails() {
        HostVO hostVo = new HostVO("");
        hostVo.setPrivateIpAddress("ipAddress");
        Map<String, String> map = new HashMap<String, String>();
        map.put("username", "name");
        map.put("password", "pass");
        hostVo.setDetails(map);
        return hostVo;
    }

    private void setIsLastHostOnPool(Set<Host> setHosts) throws Exception {
        PowerMockito.mockStatic(Host.class);
        PowerMockito.when(Host.getAll(Mockito.any(Connection.class))).thenReturn(setHosts);
    }

    private void setChangePoolMasterHost(String hostUuid, boolean isHostReachable) throws Exception {
        Mockito.doReturn(hostUuid).when(host).getUuid(Mockito.any(Connection.class));
        Mockito.doReturn("hostAddress").when(host).getAddress(Mockito.any(Connection.class));
        Mockito.doReturn(isHostReachable).when(hostUtils).isHostReachable(Mockito.anyString());
        PowerMockito.mockStatic(Host.class);
        PowerMockito.mockStatic(Pool.class);
        Set<Host> setHosts = new HashSet<Host>();
        setHosts.add(host);
        setIsLastHostOnPool(setHosts);
    }

    private void verifyChangePoolMasterHostCalledMethods(int times, int designateNewMasterTimes) throws Exception {
        Mockito.verify(host).getUuid(Mockito.any(Connection.class));
        Mockito.verify(host, Mockito.times(times)).getAddress(Mockito.any(Connection.class));
        Mockito.verify(hostUtils, Mockito.times(times)).isHostReachable(Mockito.anyString());

        PowerMockito.verifyStatic(Mockito.times(designateNewMasterTimes));
        Pool.designateNewMaster(Mockito.any(Connection.class), Mockito.any(Host.class));
    }

}
