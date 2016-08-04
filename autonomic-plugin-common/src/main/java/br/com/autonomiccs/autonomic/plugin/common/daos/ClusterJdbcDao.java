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

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;

/**
 * This class deals with operations regarding the 'cluster' table.
 */
public class ClusterJdbcDao extends JdbcDaoSupport {

    private String sqlGetClusterAdministrationStatus = "SELECT administration_status FROM cluster WHERE id=?;";
    private String sqlSetClusterAdministrationStatus = "UPDATE cluster SET administration_status=? WHERE id=?;";

    private String sqlGetClusterLastAdministration = "SELECT last_administration FROM cluster WHERE id=?;";
    private String sqlSetClusterLastAdministration = "UPDATE cluster SET last_administration=? WHERE id=?;";

    /**
     * It returns the 'last_administration' column of the 'cluster' table.
     */
    public ClusterAdministrationStatus getClusterAdministrationStatus(long clusterId) {
        String statusAsString = getJdbcTemplate().queryForObject(sqlGetClusterAdministrationStatus, String.class, clusterId);
        if (statusAsString == null) {
            return null;
        }
        return ClusterAdministrationStatus.valueOf(statusAsString);
    }

    /**
     * Updates the 'last_administration' column of the 'cluster' table.
     */
    public void setClusterAdministrationStatus(ClusterAdministrationStatus clusterConsolidationStatus, long clusterId) {
        Object[] args = {clusterConsolidationStatus.toString(), clusterId};
        getJdbcTemplate().update(sqlSetClusterAdministrationStatus, args);
    }

    /**
     * It returns the 'last_administration' column of the 'cluster' table.
     */
    public Date getClusterLastAdminstration(long clusterId) {
        return getJdbcTemplate().queryForObject(sqlGetClusterLastAdministration, Date.class, clusterId);
    }

    /**
     * Updates the 'last_administration' column of the 'cluster' table.
     */
    public void setClusterLastAdministration(Date date, long clusterId) {
        Object[] args = { date, clusterId };
        getJdbcTemplate().update(sqlSetClusterLastAdministration, args);
    }

}
