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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.vm.NicVO;
import com.cloud.vm.dao.NicDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;

@RunWith(MockitoJUnitRunner.class)
public class StartHostSystemVmServiceTest {

    private static final String RESERVER_NAME = "PodBasedNetworkGuru";

    @Spy
    @InjectMocks
    private StartHostSystemVmService spy;
    @Mock
    private AutonomiccsSystemVmJdbcDao autonomiccsSystemVmJdbcDao;
    @Mock
    private NicDao nicDao;
    @Mock
    private HostUtils hostUtils;

    @Test
    public void isStartHostSystemVmDeployedOnPodTest() {
        executeIsStartHostSystemVmDeployedOnPodTest(0l, true);
    }

    @Test
    public void isStartHostSystemVmDeployedOnPodTestNull() {
        executeIsStartHostSystemVmDeployedOnPodTest(null, false);
    }

    @Test
    public void getStartHostServiceVmIdFromPodTest() {
        Mockito.doReturn(0l).when(autonomiccsSystemVmJdbcDao).getStartHostServiceVmIdFromPod(Mockito.anyLong(), Mockito.eq(SystemVmType.ClusterManagerStartHostService));

        Long result = spy.getStartHostServiceVmIdFromPod(0l);

        Mockito.verify(autonomiccsSystemVmJdbcDao).getStartHostServiceVmIdFromPod(Mockito.anyLong(), Mockito.eq(SystemVmType.ClusterManagerStartHostService));
        Assert.assertEquals(new Long(0l), result);
    }

    @Test
    public void isStartHostServiceVmRunningOnPodTest() {
        NicVO nic = Mockito.mock(NicVO.class);
        executeIsStartHostServiceVmRunningOnPodTest(nic, true, 1);
    }

    @Test
    public void isStartHostServiceVmRunningOnPodTestNullNic() {
        executeIsStartHostServiceVmRunningOnPodTest(null, false, 0);
    }

    @Test
    public void getInternalCommunicationNicFromVmTest() {
        List<NicVO> nicsOfVm = new ArrayList<>();
        NicVO nic = Mockito.mock(NicVO.class);
        Mockito.doReturn(RESERVER_NAME).when(nic).getReserver();
        nicsOfVm.add(nic);

        executeGetInternalCommunicationNicFromVmTest(nicsOfVm, nic);

        Mockito.verify(nic).getReserver();
    }

    @Test
    public void getInternalCommunicationNicFromVmTestReserverNameNull() {
        List<NicVO> nicsOfVm = new ArrayList<>();
        NicVO nic = Mockito.mock(NicVO.class);
        Mockito.doReturn(null).when(nic).getReserver();
        nicsOfVm.add(nic);

        executeGetInternalCommunicationNicFromVmTest(nicsOfVm, null);

        Mockito.verify(nic).getReserver();
    }

    @Test
    public void getInternalCommunicationNicFromVmTestNicsEmpty() {
        executeGetInternalCommunicationNicFromVmTest(new ArrayList<NicVO>(), null);
    }

    @Test
    public void getNicToPingTest() {
        NicVO nic = Mockito.mock(NicVO.class);
        executeGetNicToPingTest(0l, nic, nic, 1);
    }

    @Test
    public void getNicToPingTestNicNull() {
        executeGetNicToPingTest(0l, null, null, 1);
    }

    @Test
    public void getNicToPingTestIdNull() {
        NicVO nic = Mockito.mock(NicVO.class);
        executeGetNicToPingTest(null, nic, null, 0);
    }

    @Test
    public void isStartHostServiceVmReadyToStartHostOnPodTestNicIsNull() {
        Mockito.doReturn(null).when(spy).getNicToPing(Mockito.eq(0l));

        boolean result = spy.isStartHostServiceVmReadyToStartHostOnPod(0l);

        Assert.assertFalse(result);
        Mockito.verify(spy).getNicToPing(Mockito.eq(0l));
    }

    @Test
    public void isStartHostServiceVmReadyToStartHostOnPodTestNicNotNull() {
        NicVO nic = Mockito.mock(NicVO.class);
        Mockito.doReturn(nic).when(spy).getNicToPing(Mockito.eq(0l));
        Mockito.doReturn("IpAddress").when(nic).getIPv4Address();
        Mockito.doReturn(true).when(hostUtils).isHostReachableOnPort8080(Mockito.eq("IpAddress"));

        boolean result = spy.isStartHostServiceVmReadyToStartHostOnPod(0l);

        Assert.assertTrue(result);
        Mockito.verify(spy).getNicToPing(Mockito.eq(0l));
        Mockito.verify(nic).getIPv4Address();
        Mockito.verify(hostUtils).isHostReachableOnPort8080(Mockito.eq("IpAddress"));
    }

    private void executeGetNicToPingTest(Long vmId, NicVO nic, NicVO expected, int times) {
        Mockito.doReturn(vmId).when(spy).getStartHostServiceVmIdFromPod(Mockito.anyLong());
        Mockito.doReturn(nic).when(spy).getInternalCommunicationNicFromVm(Mockito.anyLong());

        NicVO result = spy.getNicToPing(0l);

        Mockito.verify(spy).getStartHostServiceVmIdFromPod(Mockito.anyLong());
        Mockito.verify(spy, Mockito.times(times)).getInternalCommunicationNicFromVm(Mockito.anyLong());
        Assert.assertEquals(expected, result);
    }

    private void executeIsStartHostSystemVmDeployedOnPodTest(Long id, boolean expected) {
        Mockito.doReturn(id).when(spy).getStartHostServiceVmIdFromPod(Mockito.anyLong());
        boolean result = spy.isStartHostSystemVmDeployedOnPod(0l);
        Mockito.verify(spy).getStartHostServiceVmIdFromPod(Mockito.anyLong());
        Assert.assertEquals(expected, result);
    }

    private void executeGetInternalCommunicationNicFromVmTest(List<NicVO> nicsOfVm, NicVO expectedNic) {
        Mockito.doReturn(nicsOfVm).when(nicDao).listByVmId(Mockito.anyLong());

        NicVO result = spy.getInternalCommunicationNicFromVm(Mockito.anyLong());

        Mockito.verify(nicDao).listByVmId(Mockito.anyLong());
        Assert.assertEquals(expectedNic, result);
    }

    private void executeIsStartHostServiceVmRunningOnPodTest(NicVO nic, boolean expected, int isHostReachableTimes) {
        Mockito.doReturn(nic).when(spy).getNicToPing(Mockito.anyLong());
        Mockito.doReturn(true).when(hostUtils).isHostReachable(Mockito.anyString());

        boolean result = spy.isStartHostServiceVmRunningOnPod(0l);

        Mockito.verify(spy).getNicToPing(Mockito.anyLong());
        Mockito.verify(hostUtils, Mockito.times(isHostReachableTimes)).isHostReachable(Mockito.anyString());
        Assert.assertEquals(expected, result);
    }

}
