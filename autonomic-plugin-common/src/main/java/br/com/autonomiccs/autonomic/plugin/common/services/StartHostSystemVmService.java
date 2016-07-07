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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;

import com.cloud.vm.NicVO;
import com.cloud.vm.dao.NicDao;

/**
 * This class deals with interactions with the Start host service system VMs
 */
@Service
public class StartHostSystemVmService {

    private Logger logger = Logger.getLogger(getClass());

    private static final String RESERVER_NAME = "PodBasedNetworkGuru";

    @Autowired
    private AutonomiccsSystemVmJdbcDao autonomiccsSystemVmJdbcDao;

    @Autowired
    private NicDao nicDao;

    @Autowired
    private HostUtils hostUtils;

    /**
     * Checks if there is a start host system Vm on the given pod.
     * It uses the {@link #getStartHostServiceVmIdFromPod(Long)} method.
     *
     * @param podId
     * @return true if there a startHostSystemVm on the given Pod
     */
    public boolean isStartHostSystemVmDeployedOnPod(Long podId) {
        return getStartHostServiceVmIdFromPod(podId) != null;
    }

    /**
     * Checks if there is a start host system Vm on the given pod.
     * It uses the {@link AutonomiccsSystemVmJdbcDao#getStartHostServiceVmIdFromPod(Long, SystemVmType)} method.
     *
     * @param podId
     * @return the id of the start host service system vm.
     */
    public Long getStartHostServiceVmIdFromPod(Long podId) {
        return autonomiccsSystemVmJdbcDao.getStartHostServiceVmIdFromPod(podId, SystemVmType.ClusterManagerStartHostService);
    }

    /**
     * It returns true if the start host system VM is running on the given Pod
     */
    public boolean isStartHostServiceVmRunningOnPod(Long podId) {
        NicVO nic = getNicToPing(podId);
        if (nic == null) {
            return false;
        }
        return hostUtils.isHostReachable(nic.getIPv4Address());
    }

    /**
     * It looks for a NIC that is connected to the management network.
     */
    protected NicVO getInternalCommunicationNicFromVm(Long startHostServiceVmId) {
        List<NicVO> nicsOfVm = nicDao.listByVmId(startHostServiceVmId);
        for (NicVO nic : nicsOfVm) {
            if (RESERVER_NAME.equals(nic.getReserver())) {
                return nic;
            }
        }
        logger.info(String.format("Could not find a NIC created by PodBasedNetworkGuru, vmId[%d]", startHostServiceVmId));
        return null;
    }

    /**
     * This method checks if the start host system VM is ready to start deactivated hosts in the
     * given Pod.
     */
    public boolean isStartHostServiceVmReadyToStartHostOnPod(Long podId) {
        NicVO nic = getNicToPing(podId);
        if (nic == null) {
            return false;
        }
        return hostUtils.isHostReachableOnPort8080(nic.getIPv4Address());
    }

    /**
     * This method will try to find a management NIC to be used in our checks.
     */
    protected NicVO getNicToPing(Long podId) {
        Long startHostServiceVmId = getStartHostServiceVmIdFromPod(podId);
        if (startHostServiceVmId == null) {
            return null;
        }
        return getInternalCommunicationNicFromVm(startHostServiceVmId);
    }

}
