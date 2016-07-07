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

import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.HostPodDao;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;

@RunWith(MockitoJUnitRunner.class)
public class PodServiceTest {

    @Spy
    @InjectMocks
    private PodService spy;
    @Mock
    private HostPodDao hostPodDao;
    @Mock
    private HostJdbcDao hostJdbcDao;

    @Test
    public void getAllPodsEnabledFromZoneTest() {
        List<HostPodVO> hosts = new ArrayList<>();
        Mockito.doReturn(hosts).when(hostPodDao).listByDataCenterId(Mockito.anyLong());

        List<HostPodVO> result = spy.getAllPodsEnabledFromZone(0l);

        Mockito.verify(hostPodDao).listByDataCenterId(Mockito.anyLong());
        Assert.assertEquals(hosts, result);
    }

    @Test
    public void findPodByIdTest() {
        HostPodVO host = Mockito.mock(HostPodVO.class);
        Mockito.doReturn(host).when(hostPodDao).findById(Mockito.anyLong());
        HostPodVO result = spy.findPodById(0l);
        Mockito.verify(hostPodDao).findById(Mockito.anyLong());
        Assert.assertEquals(host, result);
    }

    @Test
    public void isThereAnyHostOnPodDeactivatedByOurManagerTestTrue() {
        executeIsThereAnyHostOnPodDeactivatedByOurManagerTest(true);
    }

    @Test
    public void isThereAnyHostOnPodDeactivatedByOurManagerTestFalse() {
        executeIsThereAnyHostOnPodDeactivatedByOurManagerTest(false);
    }

    private void executeIsThereAnyHostOnPodDeactivatedByOurManagerTest(boolean expected) {
        Mockito.doReturn(expected).when(hostJdbcDao).isThereAnyHostOnPodDeactivatedByOurManager(Mockito.anyLong());
        boolean result = spy.isThereAnyHostOnPodDeactivatedByOurManager(0l);
        Mockito.verify(hostJdbcDao).isThereAnyHostOnPodDeactivatedByOurManager(Mockito.anyLong());
        Assert.assertEquals(expected, result);
    }

}
