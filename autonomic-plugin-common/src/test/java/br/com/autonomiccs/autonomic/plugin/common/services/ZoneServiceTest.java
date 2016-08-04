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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Assert;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;

@RunWith(MockitoJUnitRunner.class)
public class ZoneServiceTest {

    @Spy
    @InjectMocks
    private ZoneService spy;
    @Mock
    private DataCenterDao dataCenterDao;

    @Test
    public void listAllZonesEnabledTest() {
        List<DataCenterVO> dataCenters = new ArrayList<>();
        Mockito.doReturn(dataCenters).when(dataCenterDao).listEnabledZones();

        List<DataCenterVO> result = spy.listAllZonesEnabled();

        Mockito.verify(dataCenterDao).listEnabledZones();
        Assert.assertEquals(dataCenters, result);
    }

}
