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
package br.com.autonomiccs.autonomic.administration.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.host.HostVO;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.UserVmService;
import com.cloud.vm.VMInstanceVO;

import br.com.autonomiccs.autonomic.administration.algorithms.ClusterAdministrationHeuristicAlgorithm;
import br.com.autonomiccs.autonomic.administration.plugin.services.AutonomicClusterManagementService;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.CloudResources;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.ClusterResourcesUp;
import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.services.CloudResourcesService;
import br.com.autonomiccs.autonomic.algorithms.commons.services.ClusterResourcesService;
import br.com.autonomiccs.autonomic.algorithms.commons.services.HostResourcesService;
import br.com.autonomiccs.autonomic.plugin.common.enums.ClusterAdministrationStatus;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.ClusterService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.services.VirtualMachineService;
import br.com.autonomiccs.autonomic.plugin.common.services.ZoneService;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

/**
 * This class implements the administration agent. The agent keeps constantly searching for clusters
 * to act on; when it finds a cluster available to be managed, it executes all of
 * the administration task upon the cluster. Be aware that those tasks are controlled by heuristics
 * loaded into the agent.
 */
@Component("administrationAgent")
public class AdministrationAgent implements InitializingBean {

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private UserVmService userVmService;
    @Autowired
    private HypervisorManager hypervisorManager;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private HostService hostService;
    @Autowired
    private AutonomicClusterManagementService autonomicClusterManagementService;
    @Autowired
    private ClusterResourcesService clusterResourcesService;
    @Autowired
    private CloudResourcesService cloudResourcesService;
    @Autowired
    private ThreadUtils threadUtils;
    @Autowired
    private ZoneService zoneService;
    @Autowired
    private HostResourcesService hostResourcesService;
    @Autowired
    private AutonomicClusterManagementHeuristicService autonomicManagementHeuristicService;
    @Autowired
    private VirtualMachineService virtualMachineService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * It receives the cluster id from channel "outputChannel" using spring
     * integration. With the given cluster, it loads the
     * {@link ClusterAdministrationHeuristicAlgorithm} to be used and executes the
     * {@link #workOnCluster(ClusterVO, ClusterAdministrationHeuristicAlgorithm)} method.
     *
     * @note spring integration configuration is in
     *       '/autonomic-administration-plugin/resources/META-INF/cloudstack/
     *       administration/spring-administration-context.xml' file.
     */
    public void receiveClusterToBeManaged(Long clusterId) {
        if (clusterId == null) {
            logger.debug("Received a null as clusterId to be processed");
            return;
        }
        ClusterVO clusterToManage = clusterService.findById(clusterId);

        if (clusterToManage == null) {
            logger.debug(String.format("Received a cluster [id=%d] to be managed that was not found on database.", clusterId));
            return;
        }
        ClusterAdministrationHeuristicAlgorithm administrationAlgorithm = autonomicManagementHeuristicService.getAdministrationAlgorithm();
        workOnCluster(clusterToManage, administrationAlgorithm);
    }

    /**
     * It searches a cluster to be managed and sends it to {@link #receiveClusterToBeManaged(Long)}
     * method using spring-integration work flow.
     *
     * @note This method is used by a spring integration channel configured in
     *       '/autonomic-administration-plugin/resources/META-INF/cloudstack/
     *       administration/spring-administration-context.xml' file.
     * @return Cluster Id.
     */
    public Long searchClusterToBeManaged() {
        ClusterAdministrationHeuristicAlgorithm administrationAlgorithm = autonomicManagementHeuristicService.getAdministrationAlgorithm();
        ClusterVO clusterToManage = getClusterToManage(administrationAlgorithm);
        if (clusterToManage != null) {
            return clusterToManage.getId();
        }
        return null;
    }

    /**
     * It return a cluster to be managed. If the last management of a cluster has not happened in a
     * given threshold that is configured by the {@link ClusterAdministrationHeuristicAlgorithm}),
     * it returns null.
     */
    protected ClusterVO getClusterToManage(ClusterAdministrationHeuristicAlgorithm algorithm) {
        List<DataCenterVO> enabledZones = zoneService.listAllZonesEnabled();
        for (DataCenterVO zone : enabledZones) {
            Long zoneId = zone.getId();

            ClusterVO c = getClusterToManageFromZone(algorithm, zoneId);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    /**
     * It returns a cluster to be managed from a given zone.
     */
    protected ClusterVO getClusterToManageFromZone(ClusterAdministrationHeuristicAlgorithm algorithm, Long zoneId) {
        List<ClusterVO> clusters = clusterService.listAllClustersOnZone(zoneId);
        for (ClusterVO cluster : clusters) {
            if (cluster.getRemoved() != null) {
                continue;
            }
            if (autonomicClusterManagementService.isClusterBeingAdministrated(cluster.getId())) {
                continue;
            }
            if (autonomicClusterManagementService.canProcessCluster(cluster.getId(), algorithm)) {
                autonomicClusterManagementService.setClusterWorkInProgress(cluster.getId());
                return cluster;
            }
        }
        return null;
    }

    /**
     * It applies a given administration algorithm onto a cluster. First it
     * executes the {@link #processCluster(ClusterVO, ClusterAdministrationHeuristicAlgorithm)}
     * method;
     * then, it sets the cluster 'administration_status' to {@link ClusterAdministrationStatus#Done}
     * and
     * 'last_administration' to the current date at the 'cloud.cluster' table.
     */
    protected void workOnCluster(ClusterVO cluster, ClusterAdministrationHeuristicAlgorithm administrationAlgorithm) {
        long clusterId = cluster.getId();
        logger.debug(String.format("Managing cluster [id=%d].", clusterId));
        try {
            processCluster(cluster, administrationAlgorithm);
        } catch (Exception e) {
            logger.info(String.format("Exception occurred while managing cluster [id = %d]", clusterId), e);
        }
        autonomicClusterManagementService.markAdministrationStatusInClusterAsDone(clusterId);

        logger.debug(String.format("Management operation completed on cluster[id=%d].", clusterId));
    }

    /**
     * If the cluster can be managed (condition determined by the
     * {@link #canProcessCluster(ClusterVO, ClusterAdministrationHeuristicAlgorithm)} method)
     * it calls the
     * {@link #mapAndExecuteVMsMigrations(ClusterVO, ClusterAdministrationHeuristicAlgorithm)}
     * method; then it shutdown idle hosts with (if possible)
     * {@link #shutdownIdleHosts(ClusterVO, ClusterAdministrationHeuristicAlgorithm)} method.
     */
    protected void processCluster(ClusterVO cluster, ClusterAdministrationHeuristicAlgorithm administrationAlgorithm) {
        long clusterId = cluster.getId();
        if (!autonomicClusterManagementService.canProcessCluster(clusterId, administrationAlgorithm)) {
            logger.debug(String.format("Method canProcessCluster returned false for cluster [id=%d]", clusterId));
            return;
        }
        mapAndExecuteVMsMigrations(cluster, administrationAlgorithm);

        if (administrationAlgorithm.canHeuristicShutdownHosts()) {
            logger.info(String.format("Shutting down idle hosts for cluster [id=%d] consolidation", clusterId));
            shutdownIdleHosts(cluster, administrationAlgorithm);
        }
    }

    /**
     * It shuts idle hosts down in the given cluster. It will check if a host can
     * be shutdown using
     * {@link ClusterAdministrationHeuristicAlgorithm#canPowerOffHost(HostResources, CloudResources)}
     * method. To check if another host in the cloud can be powered off, it
     * uses
     * {@link ClusterAdministrationHeuristicAlgorithm#canPowerOffAnotherHostInCloud(CloudResources)}
     * . To shut down a host it calls the
     * {@link #shutDownHost(long)} method.
     */
    protected void shutdownIdleHosts(ClusterVO cluster, ClusterAdministrationHeuristicAlgorithm administrationAlgorithm) {
        if (!administrationAlgorithm.canHeuristicShutdownHosts()) {
            return;
        }
        List<HostResources> idleHosts = getClusterIdleHosts(cluster);
        long clusterId = cluster.getId();
        if (CollectionUtils.isEmpty(idleHosts)) {
            logger.info(String.format("There are no idle hosts in cluster[%d].", clusterId));
            return;
        }

        logger.info(String.format("Ranking hosts to shutdown on cluster[%d]", clusterId));
        List<HostResources> orderedHostsToPowerOff = administrationAlgorithm.rankHostToPowerOff(idleHosts);
        for (HostResources host : orderedHostsToPowerOff) {
            CloudResources cloudResources = cloudResourcesService.createCloudResources(createAllClustersResourcesUp());
            if (!administrationAlgorithm.canPowerOffAnotherHostInCloud(cloudResources)) {
                logger.info("The agent cannot shut down more hosts given the result of canPowerOffAnotherHostInCloud[false].");
                break;
            }
            long hostId = host.getHostId();
            if (!administrationAlgorithm.canPowerOffHost(host, cloudResources)) {
                logger.info(String.format("Could not shut down host[id=%d] for cluster[id=%d]. Result of canPowerOffHost[false]", hostId, clusterId));
                continue;
            }
            logger.info(String.format("Shutting down host[id=%d] from cluster[id=%d]. Result of canPowerOffHost[true]", hostId, clusterId));
            shutdownHost(hostId);
        }
    }

    /**
     * Returns a list of clusters resources for the whole environment.
     */
    public List<ClusterResourcesUp> createAllClustersResourcesUp() {
        List<ClusterResourcesUp> clusters = new ArrayList<>();
        List<ClusterVO> clustersVO = clusterService.listAllClusters();
        for (ClusterVO clusterVO : clustersVO) {
            if (clusterVO.getRemoved() != null) {
                continue;
            }
            clusters.add(clusterResourcesService.createClusterResourcesUp(clusterVO.getId(), clusterVO.getName(), getClusterUpHosts(clusterVO)));
        }
        return clusters;
    }

    /**
     * Maps and executes VMs migrations. It starts by ranking hosts that are
     * 'Up' with {@link ClusterAdministrationHeuristicAlgorithm#rankHosts(List)}; then it maps
     * migrations with {@link ClusterAdministrationHeuristicAlgorithm#mapVMsToHost(List)} method,
     * finally it tries every mapped migration with
     * {@link #migrateVM(VMInstanceVO, HostVO)} method.
     */
    protected void mapAndExecuteVMsMigrations(ClusterVO cluster, ClusterAdministrationHeuristicAlgorithm administrationAlgorithm) {
        List<HostResources> hosts = getClusterUpHosts(cluster);
        long clusterId = cluster.getId();

        if (CollectionUtils.isEmpty(hosts) || hosts.size() == 1) {
            logger.debug(String.format("No hosts or single host on cluster[id=%d], no need to migrate virtual machines.", clusterId));
            return;
        }

        logger.info(String.format("Starting ranking hosts for cluster[id=%d] administration.", clusterId));
        List<HostResources> rankedHosts = administrationAlgorithm.rankHosts(hosts);
        logger.info(String.format("Starting mapping VMs for cluster[id=%d] administration.", clusterId));
        Map<Long, HostResources> migrations = administrationAlgorithm.mapVMsToHost(rankedHosts);
        logger.info(String.format("Starting migrating VMs for cluster[id=%d] administration.", clusterId));

        for (Entry<Long, HostResources> mappedMig : migrations.entrySet()) {
            Long vmId = mappedMig.getKey();
            HostVO targetHost = hostService.findHostById(mappedMig.getValue().getHostId());
            migrateVM(virtualMachineService.searchVmInstanceById(vmId), targetHost);
        }
        logger.info(String.format("Migration of VMs for cluster[id=%d] administration was finished.", clusterId));
    }

    /**
     * Tries to migrate the given virtual machine ({@link VMInstanceVO}) to a target host (
     * {@link HostVO}). It calls the method
     * {@link UserVmService#migrateVirtualMachine(Long, com.cloud.host.Host)}; in case of
     * exception, it catches and logs the issue.
     */
    protected void migrateVM(VMInstanceVO vm, HostVO host) {
        long vmId = vm.getId();
        long targetHostId = host.getId();
        Long sourceHostId = vm.getHostId();
        if (sourceHostId == targetHostId) {
            logger.debug(String.format("VM [id=%d] is not being migrated hence it was mapped to the host it is running.", vmId));
            return;
        }
        try {
            logger.info(String.format("VM [id=%d] is migrating from host[Id=%d] to host[Id=%d].", vmId, sourceHostId, targetHostId));
            userVmService.migrateVirtualMachine(vmId, host);
            logger.info(String.format("VM [id=%d] was migrated from host[Id=%d] to host[Id=%d].", vmId, sourceHostId, targetHostId));
        } catch (Exception e) {
            logger.debug(String.format("Could not migrate VM [id=%d] to host [id=%d, hostName=%s], from host [id=%d]", vmId, targetHostId, host.getName(), sourceHostId), e);
        }
    }

    /**
     * Returns a list of {@link HostResources} that are Up in the given cluster.
     */
    protected List<HostResources> getClusterUpHosts(ClusterVO cluster) {
        List<HostVO> hostsVo = hostService.listAllHostsInCluster(cluster);
        List<HostResources> hostsResources = new ArrayList<>();
        for (HostVO host : hostsVo) {
            hostsResources.add(hostResourcesService.createHostResources(host));
        }
        return hostsResources;
    }

    /**
     * Returns a list of hosts ({@link HostResources} {@link List}) that are
     * idle (the host has no VM allocated).
     */
    protected List<HostResources> getClusterIdleHosts(ClusterVO cluster) {
        List<HostResources> hosts = getClusterUpHosts(cluster);
        List<HostResources> hostsIdle = new ArrayList<>();
        for (HostResources currentHost : hosts) {
            if (CollectionUtils.isEmpty(currentHost.getVmsResources())) {
                hostsIdle.add(currentHost);
            }
        }
        return hostsIdle;
    }

    /**
     * Logs when the Administration agent starts.
     */
    @Override
    public void afterPropertiesSet() {
        logger.debug("Administration Administration initialized");
    }

    /**
     * If the host is Up and enabled, it puts host in maintenance with
     * {@link #putHostInMaintenance(long)} and requests a host shutdown with
     * {@link HypervisorManager#shutdownHost(HostVO)}.
     */
    protected void shutdownHost(long hostId) {
        try {
            checkIfHostIsUpAndEnabled(hostId);

            List<VMInstanceVO> vms = hostService.listAllVmsFromHost(hostId);
            if (CollectionUtils.isNotEmpty(vms)) {
                logger.debug(String.format("Could not shut dow host [id=%d], there are %d VMs running in this host.", hostId, vms.size()));
                return;
            }
            putHostInMaintenance(hostId);

            hypervisorManager.shutdownHost(hostService.findHostById(hostId));
        } catch (Exception e) {
            logger.info(String.format("Error while shutting down host [id=%d]", hostId), e);
        }
    }

    /**
     * Checks if the host is Up and Enabled; if not it throws a
     * {@link CloudRuntimeException}.
     */
    protected void checkIfHostIsUpAndEnabled(long hostId) {
        if (!hostService.isHostUpAndEnabled(hostId)) {
            throw new CloudRuntimeException(String.format("Host [id=%d] is not Up and/or Enabled", hostId));
        }
    }

    /**
     * Sends the maintenance request with {@link ResourceManager#maintain(long)}
     * to update the host state to {@link com.cloud.resource.ResourceState#Maintenance}. If the
     * host state be {@link com.cloud.resource.ResourceState#ErrorInMaintenance} and not
     * {@link com.cloud.resource.ResourceState#PrepareForMaintenance}, it throws a
     * {@link CloudRuntimeException}.
     */
    protected void putHostInMaintenance(long hostId) {
        try {
            resourceManager.maintain(hostId);
        } catch (Exception e) {
            throw new CloudRuntimeException(String.format("Problems while putting host [id=%d] on maintenance", hostId), e);
        }
        do {
            threadUtils.sleepThread(3);
            if (hostService.isHostInMaintenanceError(hostId)) {
                throw new CloudRuntimeException(String.format("Error while sending the maintenance command to host [id=%d]", hostId));
            }

        } while (hostService.isHostInPreparedForMaintenance(hostId));
    }

}