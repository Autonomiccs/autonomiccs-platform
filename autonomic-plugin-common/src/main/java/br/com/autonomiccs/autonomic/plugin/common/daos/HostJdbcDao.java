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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.enums.StartType;

import com.cloud.host.Status;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.resource.ResourceState;

/**
 * Offers support for some 'select' and 'update' sql statements in table 'host'.
 */
public class HostJdbcDao extends JdbcDaoSupport {

    private String sqlGetAdministrationStatus = "SELECT administration_status FROM host WHERE id=?;";
    private String sqlSetAdministrationStatus = "UPDATE host SET administration_status=? WHERE id=?;";
    private String sqlGetStartType = "SELECT start_type FROM host WHERE id=?;";
    private String sqlGetStatus = "SELECT status FROM host WHERE id=?;";
    private String sqlGetResourceState = "SELECT resource_state FROM host WHERE id=?;";

    public HostAdministrationStatus getAdministrationStatus(long hostId) {
        String hostAdministrationStatusAsAtring = getJdbcTemplate().queryForObject(sqlGetAdministrationStatus, String.class, hostId);
        if (StringUtils.isBlank(hostAdministrationStatusAsAtring)) {
            return null;
        }
        return HostAdministrationStatus.valueOf(hostAdministrationStatusAsAtring);
    }

    /**
     * Updates the 'administration_status' column from the 'host' table.
     *
     * @param hostConsolidationStatus
     * @param hostId
     */
    public void setAdministrationStatus(HostAdministrationStatus hostConsolidationStatus, long hostId) {
        Object[] args = {ObjectUtils.toString(hostConsolidationStatus), hostId};
        getJdbcTemplate().update(sqlSetAdministrationStatus, args);
    }

    /**
     * Selects the 'start_type' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public StartType getStartType(long hostId) {
        return StartType.valueOf(getJdbcTemplate().queryForObject(sqlGetStartType, String.class, hostId));
    }

    /**
     * Selects the 'status' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public Status getStatus(long hostId) {
        return Status.valueOf(getJdbcTemplate().queryForObject(sqlGetStatus, String.class, hostId));
    }

    /**
     * Selects the 'resource_state' column from the 'host' table.
     *
     * @param hostId
     * @return
     */
    public ResourceState getResourceState(long hostId) {
        return ResourceState.valueOf(getJdbcTemplate().queryForObject(sqlGetResourceState, String.class, hostId));
    }

    private String sqlSetAllHypervisorsTypeInCloud = "select hypervisor_type from host where removed is null and hypervisor_type is not null group by hypervisor_type";

    /**
     * It loads all of the hypervisors types in use in the whole cloud environment
     * @return List<HypervisorType>
     */
    public List<HypervisorType> getAllHypervisorsTypeInCloud() {
        List<String> hypervisorTypesAsString = getJdbcTemplate().queryForList(sqlSetAllHypervisorsTypeInCloud, String.class);
        List<HypervisorType> hypervisorTypes = new ArrayList<Hypervisor.HypervisorType>();
        for (String s : hypervisorTypesAsString) {
            hypervisorTypes.add(HypervisorType.valueOf(s));
        }
        return hypervisorTypes;
    }

    private String sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager = "select id from host where removed is null and consolidation_status = 'ShutDownToConsolidate'";
    public boolean isThereAnyHostOnCloudDeactivatedByOurManager() {
        List<Long> hostsIds = getJdbcTemplate().queryForList(sqlCheckIsThereAnyHostOnCloudDeactivatedByOurManager, Long.class);
        return CollectionUtils.isNotEmpty(hostsIds);
    }

    private String sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager = "select id from host where removed is null and consolidation_status = 'ShutDownToConsolidate' and pod_id = ?";
    public boolean isThereAnyHostOnPodDeactivatedByOurManager(long id) {
        List<Long> hostsIds = getJdbcTemplate().queryForList(sqlCheckIsThereAnyHostOnPodDeactivatedByOurManager, Long.class, id);
        return CollectionUtils.isNotEmpty(hostsIds);
    }
}
