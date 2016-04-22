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
 * 'consolidation_status' and 'start_type' columns into 'host' table; It also
 * inserts 'consolidation_status' and 'last_consolidated' columns into 'cluster'
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
     * TODO Documentation
     *
     * @return
     */
    private boolean hasHostConsolidationStatusColumn() {
        return (!getJdbcTemplate().queryForList(sqlHasHostAdministrationStatusColumn).isEmpty());
    }

    /**
     * TODO Documentation
     */
    private void createHostConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateHostAdministrationStatusColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasHostStartTypeColumn() {
        return !getJdbcTemplate().queryForList(sqlHasHostStartTypeColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createHostStartTypeColumn() {
        getJdbcTemplate().execute(sqlCreateHostStartTypeColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterConsolidationStatusColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterAdministrationStatusColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createClusterConsolidationStatusColumn() {
        getJdbcTemplate().execute(sqlCreateClusterAdministrationStatusColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterLastConsolidatedColumn() {
        return !getJdbcTemplate().queryForList(sqlHasClusterLastAdministrationColumn).isEmpty();
    }

    /**
     * TODO Documentation
     */
    private void createClusterLastConsolidatedColumn() {
        getJdbcTemplate().execute(sqlCreateClusterLastAdministrationColumn);
    }

    /**
     * TODO Documentation
     *
     * @return
     */
    private boolean hasClusterManagerAlgorithmsInConfiguration() {
        return !getJdbcTemplate().queryForList(sqlHasClusterAdministrationAlgorithmsInConfiguration).isEmpty();
    }

    /**
     * TODO
     */
    private void insertClusterManagerAlgorithmsInConfiguration() {
        getJdbcTemplate().execute(sqlInsertIntoConfigurationClusterAlgorithms);
    }

    /**
     * TODO
     * @return
     */
    public boolean hasAutonomiccsSystemVmTable() {
        return !getJdbcTemplate().queryForList(sqlHasAutonomiccsSystemVmTable).isEmpty();
    }

    /**
     * TODO
     */
    private void createAutonomiccsSystemVmTable() {
        getJdbcTemplate().execute(sqlCreateAutonomiccsSystemVmTable);
    }

}
