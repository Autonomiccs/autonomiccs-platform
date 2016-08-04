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

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;

@RunWith(MockitoJUnitRunner.class)
public class ClusterJdbcDaoTest {

    private ClusterJdbcDao spy;
    private String sqlGetClusterAdministrationStatus = "SELECT administration_status FROM cluster WHERE id=?;";
    private String sqlSetClusterAdministrationStatus = "UPDATE cluster SET administration_status=? WHERE id=?;";
    private String sqlGetClusterLastAdministration = "SELECT last_administration FROM cluster WHERE id=?;";
    private String sqlSetClusterLastAdministration = "UPDATE cluster SET last_administration=? WHERE id=?;";
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setup() {
        spy = Mockito.spy(new ClusterJdbcDao());
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        spy.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void getClusterAdministrationStatusTest() {
        Mockito.doReturn("Done").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterAdministrationStatus), Mockito.eq(String.class), Mockito.anyString());

        ClusterAdministrationStatus result = spy.getClusterAdministrationStatus(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterAdministrationStatus), Mockito.eq(String.class), Mockito.anyString());
        Assert.assertNotNull(result);
    }

    @Test
    public void getClusterAdministrationStatusTestReturnNull() {
        Mockito.doReturn(null).when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterAdministrationStatus), Mockito.eq(String.class), Mockito.anyString());

        ClusterAdministrationStatus result = spy.getClusterAdministrationStatus(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterAdministrationStatus), Mockito.eq(String.class), Mockito.anyString());
        Assert.assertNull(result);
    }

    @Test
    public void setClusterAdministrationStatusTest() {
        Object[] args = { ClusterAdministrationStatus.Done.toString(), 0l };
        Mockito.doReturn(0).when(jdbcTemplate).update(sqlSetClusterAdministrationStatus, args);

        spy.setClusterAdministrationStatus(ClusterAdministrationStatus.Done, 0l);

        Mockito.verify(jdbcTemplate).update(sqlSetClusterAdministrationStatus, args);
    }

    @Test
    public void getClusterLastAdminstrationTest() {
        Date expectedDate = new Date();
        Mockito.doReturn(expectedDate).when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterLastAdministration), Mockito.eq(Date.class), Mockito.eq(0l));

        Date result = spy.getClusterLastAdminstration(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetClusterLastAdministration), Mockito.eq(Date.class), Mockito.eq(0l));
        Assert.assertEquals(expectedDate, result);
    }

    @Test
    public void setClusterLastAdministrationTest() {
        Date date = new Date();
        Object[] args = { date, 0l };
        Mockito.doReturn(0).when(jdbcTemplate).update(sqlSetClusterLastAdministration, args);

        spy.setClusterLastAdministration(date, 0l);

        Mockito.verify(jdbcTemplate).update(sqlSetClusterLastAdministration, args);
    }

}
