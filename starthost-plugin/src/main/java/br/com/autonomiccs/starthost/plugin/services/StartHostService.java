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
package br.com.autonomiccs.starthost.plugin.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.api.command.admin.host.CancelMaintenanceCmd;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud.agent.AgentManager;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.ClusterResourcesAvailableToStart;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.services.ClusterResourcesService;
import br.com.autonomiccs.autonomic.algorithms.commons.services.HostResourcesService;
import br.com.autonomiccs.autonomic.allocation.algorithms.AllocationAlgorithm;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForBigHosts;
import br.com.autonomiccs.autonomic.allocation.algorithms.impl.ScoredClustersAllocationAlgorithmPreferenceForSmallHosts;
import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmDao;
import br.com.autonomiccs.autonomic.plugin.common.daos.HostJdbcDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.HostAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.enums.StartType;
import br.com.autonomiccs.autonomic.plugin.common.pojos.AutonomiccsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.services.StartHostSystemVmService;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.HttpUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ReflectionUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

/**
 * Manages the host starting process.
 */
@Service("startHostService")
public class StartHostService {

    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private StartHostSystemVmService startHostSystemVmService;
    @Autowired
    private ClusterDao clusterDao;
    @Autowired
    private HostDao hostDao;
    @Autowired
    private HostJdbcDao hostDaoJdbc;
    @Autowired
    private HostResourcesService hostResourcesManager;
    @Autowired
    private AgentManager agentMgr;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private ClusterResourcesService clusterResourcesManager;
    @Autowired
    private ThreadUtils threadUtils;
    @Autowired
    private HostUtils hostUtils;
    @Autowired
    private AutonomiccsSystemVmDao AutonomiccsSystemVmDao;
    @Autowired
    private ReflectionUtils reflectionUtils;

    private Logger logger = Logger.getLogger(StartHostService.class);

    @Transactional(readOnly = false)
    public HostVO startHost(VMEntityVO vmEntity, Exception e) throws Exception {
        List<HostResources> hostsToStart = getHostsToStart(vmEntity);

        if (hostsToStart.isEmpty()) {
            throw e;
        }
        for (HostResources hostResources : hostsToStart) {
            HostVO hostVO = hostDao.findById(hostResources.getHostId());
            startHost(hostVO);
            if (isHostAlive(hostVO)) {
                changeConsolidationStatusToUp(hostVO);
                return hostVO;
            }
        }
        return null;
    }

    /**
     * If the host status in database is Up then returns true.
     */
    public boolean isHostStatusUpInDataBase(HostVO hostVO) {
        return hostDaoJdbc.getStatus(hostVO.getId()) == Status.Up;
    }

    /**
     * Returns a ranked list of hosts to start. If there is no host available to
     * start, it returns an empty list.
     */
    private List<HostResources> getHostsToStart(VMEntityVO vmEntity) {
        List<ClusterVO> clusters = getClustersByHypervisorType(vmEntity);
        List<ClusterResourcesAvailableToStart> clustersResourcesAvailableToStart = getClusterResourcesWithServersToBeStarted(clusters);
        if (clustersResourcesAvailableToStart.isEmpty()) {
            return new ArrayList<>();
        }

        AllocationAlgorithm allocationAlgorithm = getAllocationAlgorithm();
        List<ClusterResourcesAvailableToStart> clustersRanked = allocationAlgorithm.rankClustersToAllocation(clustersResourcesAvailableToStart);

        return allocationAlgorithm.rankHostsToStart(clustersRanked.get(0).getHostsToStart());
    }

    /**
     * Lists clusters compatible with the VM hypervisor
     *
     * @param vmEntity
     * @return
     */
    private List<ClusterVO> getClustersByHypervisorType(VMEntityVO vmEntity) {
        String hypervisorType = vmEntity.getHypervisorType().name();
        return clusterDao.listByDcHyType(vmEntity.getDataCenterId(), hypervisorType);
    }


    /**
     * Calculates clusters resources available to start.
     */
    private List<ClusterResourcesAvailableToStart> getClusterResourcesWithServersToBeStarted(List<ClusterVO> clusters) {
        List<ClusterResourcesAvailableToStart> clustersResourcesAvailableToStart = new ArrayList<>();
        for (ClusterVO cluster : clusters) {
            List<HostVO> hosts = hostDao.findByClusterId(cluster.getId());
            List<HostResources> hostsToStart = new ArrayList<>();
            for (HostVO hostVO : hosts) {
                if (!startHostSystemVmService.isStartHostServiceVmRunningOnPod(hostVO.getPodId())) {
                    continue;
                }
                if (!startHostSystemVmService.isStartHostServiceVmReadyToStartHostOnPod(hostVO.getPodId())) {
                    continue;
                }
                if (hostDaoJdbc.getAdministrationStatus(hostVO.getId()) == HostAdministrationStatus.ShutDownToConsolidate) {
                    HostResources hResources = hostResourcesManager.createAndConfigureHostResources(hostVO);
                    hostsToStart.add(hResources);
                }
            }
            if (!hostsToStart.isEmpty()) {
                clustersResourcesAvailableToStart.add(clusterResourcesManager.createClusterResourcesAvailableToStart(cluster.getId(), cluster.getName(), hostsToStart));
            }
        }
        return clustersResourcesAvailableToStart;
    }

    /**
     * Calls {@link #sendStartHostCommand(HostVO)} to send a wake on LAN command
     * to start the host. If the host status is updated to Up, then logs the
     * success of this operation and finishes. If the waiting time elapsed or an
     * exception happens, it logs the issue.
     */
    private void startHost(HostVO hostVO) {
        try {
            sendStartHostCommand(hostVO);
        } catch (Exception failToStartHostException) {
            logger.warn(String.format("Failed to start host [hostId=%d],[hostName=%s], [hostIp=%s]", hostVO.getId(), hostVO.getName(), hostVO.getPublicIpAddress()),
                    failToStartHostException);
        }
        markHostAsFailedToStart(hostVO);
    }

    /**
     * Returns a Allocation algorithm to be used, currently is hard-coded to
     * select {@link ScoredClustersAllocationAlgorithmPreferenceForBigHosts}
     */
    public ScoredClustersAllocationAlgorithmPreferenceForSmallHosts getAllocationAlgorithm() {
        // TODO Este metodo deverá ser implementado usando um parametro de
        // configurações globais do CS específico para o algoritmo de alocação
        // usado.
        return new ScoredClustersAllocationAlgorithmPreferenceForBigHosts();
    }

    /**
     * Executes the start host command; if it cannot get a ping response using
     * {@link #isHostAlive(HostVO)}, it assumes the host will not wake up; as a
     * consequence of that the host will be marked as failed to start.
     */
    public void sendStartHostCommand(HostVO host) {
        executeCommandToStartHost(host);

        if (!isHostAlive(host)) {
            markHostAsFailedToStart(host);
            return;
        }
        threadUtils.sleepThread(10);
    }

    /**
     * If the host supports wake on LAN command, it executes the 'wake-on-lan'
     * command from the Operating System, if the host does not support
     * 'wake-on-lan', it will be executed a script
     * (/etc/cloudstack/management/startHost.sh) that has to be configured by
     * the environment administrator. This Script will receive two (2)
     * parameters, the host private IP address and name.
     *
     * It is used a database column called 'start_type' at table host.
     */
    private void executeCommandToStartHost(HostVO host) {
        if (hostDaoJdbc.getStartType(host.getId()) == StartType.Script) {
            executeProgram(String.format("/etc/cloudstack/management/startHost.sh %s %s", host.getManagementServerId(), host.getName()));
            return;
        }
        Long startHostServiceVmIdFromPod = startHostSystemVmService.getStartHostServiceVmIdFromPod(host.getPodId());

        AutonomiccsSystemVm systemVmInstance = AutonomiccsSystemVmDao.findById(startHostServiceVmIdFromPod);
        String systemVmIp = systemVmInstance.getManagementIpAddress();
        String httpResponse = httpUtils.wakeHaltedHostUsingHttpGet(systemVmIp, host.getPrivateMacAddress());
        logger.info(httpResponse);
    }

    /**
     * Updates the host consolidation status to Up. It will use the
     * {@link HostJdbcDao#setConsolidationStatus(HostConsolidationStatus, long)}
     * to update the host Consolidation status to 'Up'
     */
    private void changeConsolidationStatusToUp(HostVO host) {
        hostDaoJdbc.setAdministrationStatus(HostAdministrationStatus.Up, host.getId());
    }

    /**
     * Updates the host consolidation status as FailedToStart. It is used
     * {@link HostJdbcDao#setConsolidationStatus(HostConsolidationStatus, long)}
     * to update the host consolidation status.
     */
    @Transactional(readOnly = false)
    public void markHostAsFailedToStart(HostVO host) {
        hostDaoJdbc.setAdministrationStatus(HostAdministrationStatus.FailedToStart, host.getId());
    }

    /**
     * Returns true if the host is responding. It tries 50 times to get a
     * response with {@link #isHostReachable(HostVO)} method.
     */
    private boolean isHostAlive(HostVO host) {
        logger.debug(String.format("Checking if host[%d] is reachable.", host.getId()));
        for (int i = 0; i < 500; i++) {
            if (hostUtils.isHostReachable(host.getPrivateIpAddress())) {
                logger.debug(String.format("Host[%d] is reachable.", host.getId()));
                return true;
            }
            threadUtils.sleepThread(2);
        }
        logger.debug(String.format("Host[%d] is not reachable.", host.getId()));
        return false;
    }

    /**
     * It forces CloudStack to update the host.
     * To do so, CloudStack has the
     * {@link com.cloud.agent.manager.AgentManagerImpl#loadDirectlyConnectedHost( HostVO, boolean)}
     * method, which is protected. The
     * {@link com.cloud.agent.manager.AgentManagerImpl#loadDirectlyConnectedHost( HostVO, boolean)} is
     * invoked using reflection.
     */
    private void forceAgentManagerToUpdateHostStatus(HostVO host) {
        Method declaredMethod = org.springframework.util.ReflectionUtils.findMethod(agentMgr.getClass(), "loadDirectlyConnectedHost", HostVO.class, boolean.class);
        if (declaredMethod == null) {
            throw new CloudRuntimeException("Could not find method 'loadDirectlyConnectedHost' at " + agentMgr.getClass());
        }
        declaredMethod.setAccessible(true);
        try {
            declaredMethod.invoke(agentMgr, host, true);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CloudRuntimeException(e);
        }
    }

    /**
     * Executes an Operating System program. It will wait until the program
     * finishes.
     */
    private void executeProgram(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (Exception e) {
            throw new CloudRuntimeException(e);
        }
    }

    @Transactional(readOnly = false)
    public void prepareHostToReceiveVms(HostVO startedHost) {
        logger.debug(String.format("Starting to check host[%d] status up in database.", startedHost.getId()));
        if (isHostStatusUpInDataBase(startedHost)) {
            CancelMaintenanceCmd cancelMaintenanceCmd = new CancelMaintenanceCmd();
            reflectionUtils.setFieldIntoObject(cancelMaintenanceCmd, "id", startedHost.getId());
            resourceManager.cancelMaintenance(cancelMaintenanceCmd);
            forceAgentManagerToUpdateHostStatus(startedHost);
            return;
        }
        logger.warn(String.format("Failed to start host [hostId=%d],[hostName=%s], [hostIp=%s]. It seems that the host status is not up in database.", startedHost.getId(),
                startedHost.getName(), startedHost.getPublicIpAddress()));
        markHostAsFailedToStart(startedHost);
    }

}
