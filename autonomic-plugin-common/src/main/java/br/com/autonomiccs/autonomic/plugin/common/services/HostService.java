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
package br.com.autonomiccs.autonomic.plugin.common.services;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;

import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceState;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

/**
 * Provides operations over the host table.
 */
@Service
public class HostService {

    @Inject
    private HostJdbcDao hostDaoJdbc;
    @Inject
    private HostDao hostDao;
    @Inject
    private VMInstanceDao vmInstanceDao;

    /**
     * Returns true if the host (with the given id) is 'Up' and 'Enabled'
     */
    public boolean isHostUpAndEnabled(long hostId) {
        return Status.Up.equals(hostDaoJdbc.getStatus(hostId)) && ResourceState.Enabled.equals(hostDaoJdbc.getResourceState(hostId));
    }

    /**
     * Returns true if the host (with the given id) is in the Maintenance Error
     * state ({@link ResourceState#ErrorInMaintenance}).
     */
    public boolean isHostInMaintenanceError(long hostId) {
        return ResourceState.ErrorInMaintenance.equals(hostDaoJdbc.getResourceState(hostId));
    }

    /**
     * Returns true if the host (with the given id) is preparing for maintenance
     * state (({@link ResourceState#PrepareForMaintenance}).
     */
    public boolean isHostInPreparedForMaintenance(long hostId) {
        return ResourceState.PrepareForMaintenance.equals(hostDaoJdbc.getResourceState(hostId));
    }

    /**
     * Marks the host as shutdown to consolidated (
     * {@link HostAdministrationStatus#ShutDownToConsolidate}).
     */
    @Transactional(readOnly = false)
    public void markHostAsShutdownByAdministrationAgent(long id) {
        hostDaoJdbc.setAdministrationStatus(HostAdministrationStatus.ShutDownToConsolidate, id);
    }

    /**
     * Loads the host details. This method must be executed first than any other
     * that might need the host informations, such as host ip, username,
     * password or guest uuid; thus this method is the first method to be
     * executed in {@link #getConnection(HostVO)}, getConnection is the first
     * method executed by {@link #shutdownHost(HostVO)}.
     */
    public void loadHostDetails(HostVO host) {
        hostDao.loadDetails(host);
    }

    /**
     * It returns all of the hypervisors types that are in use by the whole environment.
     * @return List<HypervisorType>
     */
    public List<HypervisorType> getAllHypervisorsTypeInCloud() {
        return this.hostDaoJdbc.getAllHypervisorsTypeInCloud();
    }

    /**
     * Searches a host with the given id
     * @return {@link HostVO} that represents a host with the given id.
     */
    public HostVO findHostById(Long hostId) {
        return hostDao.findById(hostId);
    }

    /**
     * It returns a list of {@link HostVO} containing all hosts in the given {@link Cluster}.
     */
    public List<HostVO> listAllHostsInCluster(Cluster cluster) {
        return hostDao.listAllUpAndEnabledNonHAHosts(Type.Routing, cluster.getId(), cluster.getPodId(), cluster.getDataCenterId(), null);
    }

    /**
     * It returns a list of {@link VMInstanceVO} containing all VMs of the host with the given id.
     */
    public List<VMInstanceVO> listAllVmsFromHost(long hostId) {
        return vmInstanceDao.listByHostId(hostId);
    }

    /**
     * It updates the host private mac address.
     */
    @Transactional(readOnly = false)
    public void updateHostPrivaceMacAddress(HostVO hostVo, String privateMacAddress) {
        hostVo.setPrivateMacAddress(privateMacAddress);
        hostDao.update(hostVo.getId(), hostVo);
    }

    /**
     * It returns true if the host administration status is not Up (
     * {@link HostAdministrationStatus#Up}).
     */
    public boolean isHostDown(long id) {
        HostAdministrationStatus hostConsolidationStatus = hostDaoJdbc.getAdministrationStatus(id);
        return hostConsolidationStatus != null && !HostAdministrationStatus.Up.equals(hostConsolidationStatus);
    }

    /**
     * It returns true if there is at least one host deactivated by the Autonomiccs platform in the
     * whole environment.
     */
    public boolean isThereAnyHostOnCloudDeactivatedByOurManager() {
        return hostDaoJdbc.isThereAnyHostOnCloudDeactivatedByOurManager();
    }

}
