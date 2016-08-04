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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceState;

import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.enums.StartType;

@RunWith(MockitoJUnitRunner.class)
public class HostJdbcDaoTest {

    @Spy
    private HostJdbcDao spy;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private String sqlGetAdministrationStatus = "SELECT administration_status FROM host WHERE id=?;";
    private String sqlSetAdministrationStatus = "UPDATE host SET administration_status=? WHERE id=?;";
    private String sqlGetStartType = "SELECT start_type FROM host WHERE id=?;";
    private String sqlGetStatus = "SELECT status FROM host WHERE id=?;";
    private String sqlGetResourceState = "SELECT resource_state FROM host WHERE id=?;";
    private String sqlSetAllHypervisorsTypeInCloud = "select hypervisor_type from host where removed is null and hypervisor_type is not null group by hypervisor_type";
    private String sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager = "select id from host where removed is null and administration_status = 'ShutDownToConsolidate'";
    private String sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager = "select id from host where removed is null and administration_status = 'ShutDownToConsolidate' and pod_id = ?";

    @Before
    public void setup() {
        spy.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void getAdministrationStatusTest() {
        Mockito.doReturn("Up").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetAdministrationStatus), Mockito.eq(String.class), Mockito.eq(0l));

        HostAdministrationStatus result = spy.getAdministrationStatus(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetAdministrationStatus), Mockito.eq(String.class), Mockito.eq(0l));
        Assert.assertEquals(HostAdministrationStatus.Up, result);
    }

    @Test
    public void getAdministrationStatusTestIsBlank() {
        Mockito.doReturn("").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetAdministrationStatus), Mockito.eq(String.class), Mockito.eq(0l));

        HostAdministrationStatus result = spy.getAdministrationStatus(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetAdministrationStatus), Mockito.eq(String.class), Mockito.eq(0l));
        Assert.assertEquals(null, result);
    }

    @Test
    public void setAdministrationStatusTest() {
        Object[] args = { "Up", 0l };
        Mockito.doReturn(0).when(jdbcTemplate).update(sqlSetAdministrationStatus, args);

        spy.setAdministrationStatus(HostAdministrationStatus.Up, 0l);

        Mockito.verify(jdbcTemplate).update(sqlSetAdministrationStatus, args);
    }

    @Test
    public void getStartTypeTest() {
        Mockito.doReturn("WakeOnLan").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStartType), Mockito.eq(String.class), Mockito.eq(0l));

        StartType result = spy.getStartType(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStartType), Mockito.eq(String.class), Mockito.eq(0l));
        Assert.assertEquals(StartType.WakeOnLan, result);
    }

    @Test
    public void getStatusTest() {
        Mockito.doReturn("Up").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStatus), Mockito.eq(String.class), Mockito.eq(0l));

        Status result = spy.getStatus(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetStatus), Mockito.eq(String.class), Mockito.eq(0l));
        Assert.assertEquals(Status.Up, result);
    }

    @Test
    public void getResourceStateTest() {
        Mockito.doReturn("Enabled").when(jdbcTemplate).queryForObject(Mockito.eq(sqlGetResourceState), Mockito.eq(String.class), Mockito.eq(0l));

        ResourceState result = spy.getResourceState(0l);

        Mockito.verify(jdbcTemplate).queryForObject(Mockito.eq(sqlGetResourceState), Mockito.eq(String.class), Mockito.eq(0l));
        Assert.assertEquals(ResourceState.Enabled, result);
    }

    @Test
    public void getAllHypervisorsTypeInCloudTest() {
        List<String> hypervisorTypesAsString = new ArrayList<>();
        hypervisorTypesAsString.add("XenServer");
        hypervisorTypesAsString.add("KVM");
        Mockito.doReturn(hypervisorTypesAsString).when(jdbcTemplate).queryForList(Mockito.eq(sqlSetAllHypervisorsTypeInCloud), Mockito.eq(String.class));

        List<HypervisorType> result = spy.getAllHypervisorsTypeInCloud();

        Mockito.verify(jdbcTemplate).queryForList(Mockito.eq(sqlSetAllHypervisorsTypeInCloud), Mockito.eq(String.class));
        Assert.assertEquals(HypervisorType.XenServer, result.get(0));
        Assert.assertEquals(HypervisorType.KVM, result.get(1));
    }

    @Test
    public void isThereAnyHostOnCloudDeactivatedByOurManagerTest() {
        List<Long> hostsIds = new ArrayList<>();
        hostsIds.add(0l);
        configureIsThereAnyHostOnClusterDeactivatedByOurManager(hostsIds);

        boolean result = spy.isThereAnyHostOnCloudDeactivatedByOurManager();

        Mockito.verify(jdbcTemplate).queryForList(Mockito.eq(sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager), Mockito.eq(Long.class));
        Assert.assertEquals(true, result);
    }

    @Test
    public void isThereAnyHostOnCloudDeactivatedByOurManagerTestEmptyList() {
        configureIsThereAnyHostOnClusterDeactivatedByOurManager(new ArrayList<Long>());

        boolean result = spy.isThereAnyHostOnCloudDeactivatedByOurManager();

        Mockito.verify(jdbcTemplate).queryForList(Mockito.eq(sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager), Mockito.eq(Long.class));
        Assert.assertEquals(false, result);
    }

    @Test
    public void isThereAnyHostOnPodDeactivatedByOurManagerTest() {
        List<Long> hostsIds = new ArrayList<>();
        hostsIds.add(0l);
        configureIsThereAnyHostOnPodDeactivatedByOurManager(hostsIds);

        boolean result = spy.isThereAnyHostOnPodDeactivatedByOurManager(0l);

        verifyIsThereAnyHostOnPodDeactivatedByOurManager(true, result);
    }

    @Test
    public void isThereAnyHostOnPodDeactivatedByOurManagerTestEmptyList() {
        configureIsThereAnyHostOnClusterDeactivatedByOurManager(new ArrayList<Long>());

        boolean result = spy.isThereAnyHostOnPodDeactivatedByOurManager(0l);

        verifyIsThereAnyHostOnPodDeactivatedByOurManager(false, result);
    }

    private void configureIsThereAnyHostOnClusterDeactivatedByOurManager(List<Long> hostsIds) {
        Mockito.doReturn(hostsIds).when(jdbcTemplate).queryForList(Mockito.eq(sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager), Mockito.eq(Long.class));
    }

    private void configureIsThereAnyHostOnPodDeactivatedByOurManager(List<Long> hostsIds) {
        Mockito.doReturn(hostsIds).when(jdbcTemplate).queryForList(Mockito.eq(sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager), Mockito.eq(Long.class), Mockito.eq(0l));
    }

    private void verifyIsThereAnyHostOnPodDeactivatedByOurManager(boolean expected, boolean result) {
        Mockito.verify(jdbcTemplate).queryForList(Mockito.eq(sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager), Mockito.eq(Long.class), Mockito.eq(0l));
        Assert.assertEquals(expected, result);
    }

}
