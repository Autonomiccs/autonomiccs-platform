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
import org.springframework.jdbc.core.JdbcTemplate;

import com.cloud.utils.exception.CloudRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class AutonomiccsSystemVmTemplateJdbcDaoTest {

    private AutonomiccsSystemVmTemplateJdbcDao spy;
    private String sqlIsTemplateRegistered = "select count(id) from vm_template where removed is null and state <> 'Inactive' and name = ?";
    private String sqlIsTemplateRegisteredAndReady = "select count(id) from template_view where state = 'READY' and removed is null and name = ?";
    private String sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor = "select id from template_view where state = 'READY' and removed is null and name = ?";
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setup() {
        spy = Mockito.spy(new AutonomiccsSystemVmTemplateJdbcDao());
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        spy.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void isTemplateRegisteredTest() {
        Mockito.doReturn(true).when(spy).executeTemplateQueryAndRetrieveBoolean(Mockito.anyString(), Mockito.eq(sqlIsTemplateRegistered));
        spy.isTemplateRegistered("templateName");
        Mockito.verify(spy).executeTemplateQueryAndRetrieveBoolean(Mockito.anyString(), Mockito.eq(sqlIsTemplateRegistered));
    }

    @Test
    public void executeTemplateQueryAndRetrieveBooleanTest() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.eq(sqlIsTemplateRegistered), Mockito.any(Object[].class), Mockito.eq(Integer.class))).thenReturn(new Integer(1));

        boolean result = spy.executeTemplateQueryAndRetrieveBoolean("templateName", sqlIsTemplateRegistered);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlIsTemplateRegistered), Mockito.any(Object[].class), Mockito.eq(Integer.class));
        Assert.assertTrue(result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void executeTemplateQueryAndRetrieveBooleanTestWithCloudRuntimeException() {
        Mockito.when(jdbcTemplate.queryForObject(Mockito.eq(sqlIsTemplateRegistered), Mockito.any(Object[].class), Mockito.eq(Integer.class))).thenReturn(new Integer(2));

        spy.executeTemplateQueryAndRetrieveBoolean("templateName", sqlIsTemplateRegistered);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlIsTemplateRegistered), Mockito.any(Object[].class), Mockito.eq(Integer.class));
    }

    @Test
    public void isTemplateRegisteredAndReadyTest() {
        Mockito.doReturn(true).when(spy).executeTemplateQueryAndRetrieveBoolean(Mockito.eq("TemplateName"),
                Mockito.eq(sqlIsTemplateRegisteredAndReady));
        boolean result = spy.isTemplateRegisteredAndReady("TemplateName");
        Mockito.verify(spy).executeTemplateQueryAndRetrieveBoolean(Mockito.eq("TemplateName"), Mockito.eq(sqlIsTemplateRegisteredAndReady));
        Assert.assertTrue(result);
    }

    @Test
    public void searchAutonomiccsSystemVmTemplateIdForHypervisorTest() {
        Mockito.doReturn(123l).when(jdbcTemplate).queryForObject(Mockito.eq(sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor), Mockito.eq(Long.class), Mockito.anyLong());

        long result = spy.searchAutonomiccsSystemVmTemplateIdForHypervisor("TemplateName");

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlSearchAutonomiccsSystemVmTemplateIdForHypervisor), Mockito.eq(Long.class), Mockito.anyString());
        Assert.assertEquals(123l, result);
    }

}
