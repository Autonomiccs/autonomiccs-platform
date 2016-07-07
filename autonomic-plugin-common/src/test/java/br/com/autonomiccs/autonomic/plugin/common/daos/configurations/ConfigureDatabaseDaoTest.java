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
package br.com.autonomiccs.autonomic.plugin.common.daos.configurations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.VmsDispersionAlgorithmForHomogeneousEnvironment;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureDatabaseDaoTest {

    private ConfigureDatabaseDao spy;
    private JdbcTemplate jdbcTemplate;

    private String sqlHasHostAdministrationStatusColumn = "SHOW COLUMNS FROM host LIKE 'administration_status';";
    private String sqlCreateHostAdministrationStatusColumn = "ALTER TABLE host ADD administration_status VARCHAR(60) AFTER status;";
    private String sqlHasHostStartTypeColumn = "SHOW COLUMNS FROM host LIKE 'start_type';";
    private String sqlCreateHostStartTypeColumn = "ALTER TABLE host ADD start_type VARCHAR(20) NOT NULL DEFAULT 'WakeOnLan' AFTER administration_status;";
    private String sqlHasClusterAdministrationStatusColumn = "SHOW COLUMNS FROM cluster LIKE 'administration_status';";
    private String sqlCreateClusterAdministrationStatusColumn = "ALTER TABLE cluster ADD administration_status VARCHAR(60);";
    private String sqlHasClusterLastAdministrationColumn = "SHOW COLUMNS FROM cluster LIKE 'last_administration';";
    private String sqlCreateClusterLastAdministrationColumn = "ALTER TABLE cluster ADD last_administration DATETIME;";
    private String sqlHasClusterAdministrationAlgorithmsInConfiguration = String.format("SELECT * FROM configuration WHERE name='%s';",
            AutonomicClusterManagementHeuristicService.CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY);
    private String sqlInsertIntoConfigurationClusterAlgorithms = String.format(
            "INSERT INTO configuration (category,instance,component,name,value,description,default_value,updated,scope,is_dynamic) VALUES ('Advanced','DEFAULT','autonomicClusterManager','%s','%s','Full qualified heuristic class name to be used to guide the agent during the cluster management process.','%s',null,null,0);",
            AutonomicClusterManagementHeuristicService.CLUSTER_ADMINISTRATION_ALGORITHMS_IN_CONFIGURATION_KEY,
            VmsDispersionAlgorithmForHomogeneousEnvironment.class.getCanonicalName(), ClusterManagementDummyAlgorithm.class.getCanonicalName());
    private String sqlHasAutonomiccsSystemVmTable = "SHOW TABLES LIKE 'AutonomiccsSystemVm';";
    private String sqlCreateAutonomiccsSystemVmTable = "CREATE TABLE AutonomiccsSystemVm(id BIGINT(20) UNSIGNED, public_ip_address VARCHAR(40), management_ip_address VARCHAR(40));";

    @Before
    public void setup() {
        spy = PowerMockito.spy(new ConfigureDatabaseDao());
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        spy.setJdbcTemplate(jdbcTemplate);
    }

    @Test
    public void initDaoTestTableConfigured() throws Exception {
        configureDbChecks(true);

        spy.initDao();

        verifyDbChecks();
        verifyDbAlterations(0);
    }

    @Test
    public void initDaoTestTableNotConfigured() throws Exception {
        Mockito.doNothing().when(spy).createHostConsolidationStatusColumn();
        Mockito.doNothing().when(spy).createHostStartTypeColumn();
        Mockito.doNothing().when(spy).createClusterConsolidationStatusColumn();
        Mockito.doNothing().when(spy).createClusterLastConsolidatedColumn();
        Mockito.doNothing().when(spy).insertClusterManagerAlgorithmsInConfiguration();
        Mockito.doNothing().when(spy).createAutonomiccsSystemVmTable();
        configureDbChecks(false);

        spy.initDao();

        verifyDbChecks();
        verifyDbAlterations(1);
    }

    @Test
    public void hasHostConsolidationStatusColumnTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasHostAdministrationStatusColumn));
        boolean result = spy.hasHostConsolidationStatusColumn();
        verifyHasDbConfiguration(sqlHasHostAdministrationStatusColumn, true, result);
    }

    @Test
    public void hasHostConsolidationStatusColumnTestIsEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasHostAdministrationStatusColumn));
        boolean result = spy.hasHostConsolidationStatusColumn();
        verifyHasDbConfiguration(sqlHasHostAdministrationStatusColumn, false, result);
    }

    @Test
    public void createHostConsolidationStatusColumnTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlCreateHostAdministrationStatusColumn));
        spy.createHostConsolidationStatusColumn();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlCreateHostAdministrationStatusColumn));
    }

    @Test
    public void hasHostStartTypeColumnTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasHostStartTypeColumn));
        boolean result = spy.hasHostStartTypeColumn();
        verifyHasDbConfiguration(sqlHasHostStartTypeColumn, true, result);
    }

    @Test
    public void hasHostStartTypeColumnTestIsEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasHostStartTypeColumn));
        boolean result = spy.hasHostStartTypeColumn();
        verifyHasDbConfiguration(sqlHasHostStartTypeColumn, false, result);
    }

    @Test
    public void createHostStartTypeColumnTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlCreateHostStartTypeColumn));
        spy.createHostStartTypeColumn();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlCreateHostStartTypeColumn));
    }

    @Test
    public void createClusterConsolidationStatusColumnTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlCreateClusterAdministrationStatusColumn));
        spy.createClusterConsolidationStatusColumn();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlCreateClusterAdministrationStatusColumn));
    }

    @Test
    public void hasClusterConsolidationStatusColumnTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterAdministrationStatusColumn));
        boolean result = spy.hasClusterConsolidationStatusColumn();
        verifyHasDbConfiguration(sqlHasClusterAdministrationStatusColumn, true, result);
    }

    @Test
    public void hasClusterConsolidationStatusColumnTestEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterAdministrationStatusColumn));
        boolean result = spy.hasClusterConsolidationStatusColumn();
        verifyHasDbConfiguration(sqlHasClusterAdministrationStatusColumn, false, result);
    }

    @Test
    public void hasClusterLastConsolidatedColumnTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterLastAdministrationColumn));
        boolean result = spy.hasClusterLastConsolidatedColumn();
        verifyHasDbConfiguration(sqlHasClusterLastAdministrationColumn, true, result);
    }

    @Test
    public void hasClusterLastConsolidatedColumnTestEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterLastAdministrationColumn));
        boolean result = spy.hasClusterLastConsolidatedColumn();
        verifyHasDbConfiguration(sqlHasClusterLastAdministrationColumn, false, result);
    }

    @Test
    public void createClusterLastConsolidatedColumnTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlCreateClusterLastAdministrationColumn));
        spy.createClusterLastConsolidatedColumn();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlCreateClusterLastAdministrationColumn));
    }

    @Test
    public void hasClusterManagerAlgorithmsInConfigurationTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterAdministrationAlgorithmsInConfiguration));
        boolean result = spy.hasClusterManagerAlgorithmsInConfiguration();
        verifyHasDbConfiguration(sqlHasClusterAdministrationAlgorithmsInConfiguration, true, result);
    }

    @Test
    public void hasClusterManagerAlgorithmsInConfigurationTestEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasClusterAdministrationAlgorithmsInConfiguration));
        boolean result = spy.hasClusterManagerAlgorithmsInConfiguration();
        verifyHasDbConfiguration(sqlHasClusterAdministrationAlgorithmsInConfiguration, false, result);
    }

    @Test
    public void insertClusterManagerAlgorithmsInConfigurationTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlInsertIntoConfigurationClusterAlgorithms));
        spy.insertClusterManagerAlgorithmsInConfiguration();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlInsertIntoConfigurationClusterAlgorithms));
    }

    @Test
    public void hasAutonomiccsSystemVmTableTest() {
        Mockito.doReturn(createListOfMaps()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasAutonomiccsSystemVmTable));
        boolean result = spy.hasAutonomiccsSystemVmTable();
        verifyHasDbConfiguration(sqlHasAutonomiccsSystemVmTable, true, result);
    }

    @Test
    public void hasAutonomiccsSystemVmTableTestEmpty() {
        Mockito.doReturn(new ArrayList<Map<String, Long>>()).when(jdbcTemplate).queryForList(Mockito.eq(sqlHasAutonomiccsSystemVmTable));
        boolean result = spy.hasAutonomiccsSystemVmTable();
        verifyHasDbConfiguration(sqlHasAutonomiccsSystemVmTable, false, result);
    }

    @Test
    public void createAutonomiccsSystemVmTableTest() {
        Mockito.doNothing().when(jdbcTemplate).execute(Mockito.eq(sqlCreateAutonomiccsSystemVmTable));
        spy.createAutonomiccsSystemVmTable();
        Mockito.verify(jdbcTemplate).execute(Mockito.eq(sqlCreateAutonomiccsSystemVmTable));
    }

    private void configureDbChecks(boolean hasTablesAlreadyConfigured) {
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasHostConsolidationStatusColumn();
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasHostStartTypeColumn();
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasClusterConsolidationStatusColumn();
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasClusterLastConsolidatedColumn();
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasClusterManagerAlgorithmsInConfiguration();
        Mockito.doReturn(hasTablesAlreadyConfigured).when(spy).hasAutonomiccsSystemVmTable();
    }

    private void verifyDbChecks() {
        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy).hasHostConsolidationStatusColumn();
        inOrder.verify(spy).hasHostStartTypeColumn();
        inOrder.verify(spy).hasClusterConsolidationStatusColumn();
        inOrder.verify(spy).hasClusterLastConsolidatedColumn();
        inOrder.verify(spy).hasClusterManagerAlgorithmsInConfiguration();
        inOrder.verify(spy).hasAutonomiccsSystemVmTable();
    }

    private void verifyDbAlterations(int times) {
        InOrder inOrder = Mockito.inOrder(spy);
        inOrder.verify(spy, Mockito.times(times)).createHostConsolidationStatusColumn();
        inOrder.verify(spy, Mockito.times(times)).createHostStartTypeColumn();
        inOrder.verify(spy, Mockito.times(times)).createClusterConsolidationStatusColumn();
        inOrder.verify(spy, Mockito.times(times)).createClusterLastConsolidatedColumn();
        inOrder.verify(spy, Mockito.times(times)).insertClusterManagerAlgorithmsInConfiguration();
        inOrder.verify(spy, Mockito.times(times)).createAutonomiccsSystemVmTable();
    }

    private List<Map<String, Long>> createListOfMaps() {
        List<Map<String, Long>> list = new ArrayList<Map<String, Long>>();
        Map<String, Long> map = new HashMap<>();
        map.put("teste", new Long(0l));
        list.add(map);
        return list;
    }

    private void verifyHasDbConfiguration(String sql, boolean expected, boolean result) {
        Mockito.verify(jdbcTemplate).queryForList(Mockito.eq(sql));
        Assert.assertEquals(expected, result);
    }

}
