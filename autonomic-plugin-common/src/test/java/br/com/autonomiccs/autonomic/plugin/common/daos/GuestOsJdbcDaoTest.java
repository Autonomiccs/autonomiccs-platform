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
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(MockitoJUnitRunner.class)
public class GuestOsJdbcDaoTest {

    private String sqlGetGuestOsId = "select id from guest_os where display_name = ?";
    private GuestOsJdbcDao spy;

    @Before
    public void setup() {
        spy = PowerMockito.spy(new GuestOsJdbcDao());
    }

    @Test
    public void getGuestOsUuidTest() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        spy.setJdbcTemplate(jdbcTemplate);

        Mockito.doReturn(321l).when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetGuestOsId), Mockito.eq(Long.class), Mockito.eq("guesOsName"));

        long result = spy.getGuestOsUuid("guesOsName");

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetGuestOsId), Mockito.eq(Long.class), Mockito.eq("guesOsName"));
        Assert.assertEquals(321l, result);
    }

}
