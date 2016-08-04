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

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.administration.algorithms.impl.ClusterManagementDummyAlgorithm;
import br.com.autonomiccs.autonomic.administration.algorithms.impl.VmsDispersionAlgorithmForHomogeneousEnvironment;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;

/**
 * Configures the CloudStack database, inserting (if needed)
 * 'administration_status' and 'start_type' columns into 'host' table; It also
 * inserts 'administration_status' and 'last_consolidated' columns into 'cluster'
 * table.
 */
public class ConfigureDatabaseDao extends JdbcDaoSupport {

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

    /**
     * It configures the database, adding missing tables, columns, and rows, that are necessary for
     * the execution of the Autonomiccs platform. Everytime that the Autonomiccs platform is
     * started, it verifies the need of Database configurations.
     */
    @Override
    protected void initDao() throws Exception {
        super.initDao();
        if (!hasHostConsolidationStatusColumn()) {
            createHostConsolidationStatusColumn();
        }
        if (!hasHostStartTypeColumn()) {
            createHostStartTypeColumn();
        }
        if (!hasClusterConsolidationStatusColumn()) {
            createClusterConsolidationStatusColumn();
        }
        if (!hasClusterLastConsolidatedColumn()) {
            createClusterLastConsolidatedColumn();
        }
        if (!hasClusterManagerAlgorithmsInConfiguration()) {
            insertClusterManagerAlgorithmsInConfiguration();
        }
        if (!hasAutonomiccsSystemVmTable()) {
            createAutonomiccsSystemVmTable();
        }
    }

    /**
     * It returns true if there is the 'administration_status' column at the 'host' table
     */
    protected boolean hasHostConsolidationStatusColumn() {
        return !getJdbcTemplate().queryForList(sqlHasHostAdministrationStatusColumn).isEmpty();
    }

    /**
     * It creates the 'administration_status' column at the 'host' table..
     */
    protected void createHostConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateHostAdministrationStatusColumn);
    }

    /**
     * It returns true if there is a 'start_type' column at the 'host' table.
     */
    protected boolean hasHostStartTypeColumn() {
        return !getJdbcTemplate().queryForList(sqlHasHostStartTypeColumn).isEmpty();
    }

    /**
     * It creates the 'start_type' column at the 'host' table.
     */
    protected void createHostStartTypeColumn() {
        getJdbcTemplate().execute(sqlCreateHostStartTypeColumn);
    }

    /**
     * It returns true if there is a column called 'administration_status' at the 'cluster' table.
     */
    protected boolean hasClusterConsolidationStatusColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterAdministrationStatusColumn).isEmpty();
    }

    /**
     * It creates the 'administration_status' column at the 'cluster' table.
     */
    protected void createClusterConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateClusterAdministrationStatusColumn);
    }

    /**
     * It returns true if there is a 'last_administration' column at the 'cluster' table.
     */
    protected boolean hasClusterLastConsolidatedColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterLastAdministrationColumn).isEmpty();
    }

    /**
     * It creates the 'last_administration' column at the 'cluster' table.
     */
    protected void createClusterLastConsolidatedColumn() {
        getJdbcTemplate().execute(sqlCreateClusterLastAdministrationColumn);
    }

    /**
     * It returns true if it has a row with the 'autonomiccs.clustermanager.algorithm' at the table
     * 'configuration'.
     */
    protected boolean hasClusterManagerAlgorithmsInConfiguration() {
        return !getJdbcTemplate().queryForList(sqlHasClusterAdministrationAlgorithmsInConfiguration).isEmpty();
    }

    /**
     * It inserts a row with the Autonomiccs configuration ('autonomiccs.clustermanager.algorithm')
     * at the table 'configuration'.
     */
    protected void insertClusterManagerAlgorithmsInConfiguration() {
        getJdbcTemplate().execute(sqlInsertIntoConfigurationClusterAlgorithms);
    }

    /**
     * It returns true if there is a table called 'AutonomiccsSystemVm'.
     */
    protected boolean hasAutonomiccsSystemVmTable() {
        return !getJdbcTemplate().queryForList(sqlHasAutonomiccsSystemVmTable).isEmpty();
    }

    /**
     * It creates the table 'AutonomiccsSystemVm'.
     */
    protected void createAutonomiccsSystemVmTable() {
        getJdbcTemplate().execute(sqlCreateAutonomiccsSystemVmTable);
    }

}
