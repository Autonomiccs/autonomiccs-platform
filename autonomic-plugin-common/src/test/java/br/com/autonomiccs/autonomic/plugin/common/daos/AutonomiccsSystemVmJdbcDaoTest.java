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
package br.com.autonomiccs.autonomic.plugin.common.daos;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;

@RunWith(MockitoJUnitRunner.class)
public class AutonomiccsSystemVmJdbcDaoTest {

    private String sqlGetStartHostServiceVmIdFromPod = "select id from vm_instance where removed is null and pod_id  = ? and account_id = 1 and instance_name like ? ";
    private AutonomiccsSystemVmJdbcDao spy;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setup() throws Exception {
        spy = PowerMockito.spy(new AutonomiccsSystemVmJdbcDao());
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        spy.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void getStartHostServiceVmIdFromPodTest() {
        Mockito.doReturn(0l).when(jdbcTemplate).queryForObject(Mockito.anyString(), Mockito.eq(Long.class), Mockito.anyLong(), Mockito.anyString());

        long result = spy.getStartHostServiceVmIdFromPod(0l, SystemVmType.ClusterManagerAgent);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStartHostServiceVmIdFromPod), Mockito.eq(Long.class), Mockito.anyLong(), Mockito.anyString());
        Assert.assertEquals(0l, result);
    }

    @Test
    public void getStartHostServiceVmIdFromPodTestWithException() throws Exception {
        Mockito.doThrow(EmptyResultDataAccessException.class).when(jdbcTemplate).queryForObject(Mockito.anyString(), Mockito.eq(Long.class), Mockito.anyLong(),
                Mockito.anyString());

        Assert.assertEquals(null, spy.getStartHostServiceVmIdFromPod(0l, SystemVmType.ClusterManagerAgent));
        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStartHostServiceVmIdFromPod), Mockito.eq(Long.class), Mockito.anyLong(), Mockito.anyString());
    }

}
