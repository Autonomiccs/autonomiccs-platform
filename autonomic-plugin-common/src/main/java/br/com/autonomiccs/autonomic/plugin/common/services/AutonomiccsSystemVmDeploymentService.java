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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;

import br.com.autonomiccs.autonomic.algorithms.commons.beans.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.services.HostResourcesService;
import br.com.autonomiccs.autonomic.plugin.common.beans.AutonomiccsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.utils.HostUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.SshUtils;
import br.com.autonomiccs.autonomic.plugin.common.utils.ThreadUtils;

/**
 * This class is used to deploy and manage Autonomiccs system VMs.
 */
@Service
public class AutonomiccsSystemVmDeploymentService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String commandToInstallOpenJDK7 = "aptitude -y install openjdk-7-jdk";
    private final int ONE_MEGABYTE_IN_BYTES = 1048576;

    private ServiceOfferingVO autonomiccsSystemVmServiceOffering;

    @Autowired
    private SshUtils sshUtils;

    @Autowired
    private AutonomiccsSystemVmTemplateService autonomiccsSystemVmTemplateService;

    @Autowired
    private AutonomiccsSystemVmDao autonomiccsSystemVmDao;

    @Autowired
    private HostService hostService;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private DataCenterDao dataCenterDao;

    @Autowired
    private AccountManager accountManager;

    @Autowired
    private NetworkDao networkDao;

    @Autowired
    private NetworkModel networkModel;

    @Autowired
    private NetworkOfferingDao networkOfferingDao;

    @Autowired
    private NetworkOrchestrationService networkManager;

    @Autowired
    private VirtualMachineManager virtualMachineManager;

    @Autowired
    private AutonomiccsServiceOfferingService autonomiccsServiceOfferingService;

    @Autowired
    private HostUtils hostUtils;

    @Autowired
    private ThreadUtils threadUtils;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private HostResourcesService hostResourcesService;

    @Autowired
    private PodService podService;

    @Autowired
    private ZoneService zoneService;

    /**
     * This method looks for a host in the whole cloud environment to deploy an Autonomiccs system VM.
     * It loads all zones of the environment, and then it randomizes the list and tries to look for suitable hosts using {@link #searchForRandomHostInZoneToDeployAutonomiccsSystemVm(DataCenterVO)}
     *
     * @return {@link HostVO} to deploy the system VM, it can also return null if no suitable hosts were found.
     */
    public HostVO searchForRandomHostInCloudToDeployAutonomiccsSystemVm() {
        List<DataCenterVO> allZonesEnabled = zoneService.listAllZonesEnabled();
        Collections.shuffle(allZonesEnabled);
        for (DataCenterVO dataCenterVO : allZonesEnabled) {
            HostVO host = searchForRandomHostInZoneToDeployAutonomiccsSystemVm(dataCenterVO);
            if (host != null) {
                return host;
            }
        }
        logger.info("Could not find any suitable hosts to deploy the system VM into Cloud environment");
        return null;
    }

    /**
     * This method looks for a random host in the given zone ( {@link DataCenterVO}) to deploy an Autonomiccs system VM.
     * It loads all pods ( {@link HostPodVO}) of the environment, and then it randomizes the list and tries to look for suitable hosts using {@link #searchForRandomHostInPodToDeployAutonomiccsSystemVm(HostPodVO)}
     *
     * @return {@link HostVO} to deploy the system VM, it can also return null if no suitable hosts were found.
     */
    private HostVO searchForRandomHostInZoneToDeployAutonomiccsSystemVm(DataCenterVO dataCenterVO) {
        List<HostPodVO> allPodsEnabledFromZone = podService.getAllPodsEnabledFromZone(dataCenterVO.getId());
        Collections.shuffle(allPodsEnabledFromZone);
        for (HostPodVO hostPodVO : allPodsEnabledFromZone) {
            HostVO host = searchForRandomHostInPodToDeployAutonomiccsSystemVm(hostPodVO);
            if (host != null) {
                return host;
            }
        }
        logger.info(String.format("Could not find any suitable hosts to deploy the system VM into zone [zoneId=%d, zoneName=%s]", dataCenterVO.getId(), dataCenterVO.getName()));
        return null;
    }

    /**
     * This method will try to find a host in the given {@link HostPodVO} to deploy an Autonomiccs system vm.
     * The search steps are the following:
     *  <ul>
     *      <li>We load all enabled clusters, then we randomize the list;
     *      <li>then, try to look for a host using method {@link #searchForRandomHostInClusterToDeployAutonomiccsSystemVm(ClusterVO)}
     *  </ul>
     * @param pod - the Pod in which we will try to look for a host to deploy a system VM
     * @return {@link HostVO} that is going to be used to deploy a system VM. It may be returned a null value, which indicates that we did not find a suitable host to deploy the system VM in the given pod
     */
    public HostVO searchForRandomHostInPodToDeployAutonomiccsSystemVm(HostPodVO pod) {
        List<ClusterVO> allClustersFromPod = clusterService.listAllClustersFromPod(pod.getId());
        Collections.shuffle(allClustersFromPod);
        for (ClusterVO c : allClustersFromPod) {
            HostVO host = searchForRandomHostInClusterToDeployAutonomiccsSystemVm(c);
            if (host != null) {
                return host;
            }
        }
        logger.info(String.format("Could not find any suitable hosts to deploy the system VM into pod [podId=%d, podName=%s]", pod.getId(), pod.getName()));
        return null;
    }

    /**
     * This method tries to find a suitable host to deploy an Autonomiccs system VM.
     * It will send an empty list as the exclude host list to {@link #searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(ClusterVO, List)
     * @see #searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(ClusterVO, List)
     *
     * @param c cluster to look for a host to deploy a system VM
     * @return {@link HostVO} to deploy a system VM, it may return null if no suitable hosts have been found
     */
    private HostVO searchForRandomHostInClusterToDeployAutonomiccsSystemVm(ClusterVO c) {
        return searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(c, new ArrayList<HostVO>());
    }

    /**
     * This method tries to find a suitable host to deploy an Autonomiccs system VM.
     * The process is the following:
     * <ul>
     *   <li>We load all hosts of the given cluster, we also shuffle the host list;
     *   <li>after that, we go over the host list and check if it can host the system VM {@link #canHostSupportVm(ServiceOfferingVO, HostResources)}
     *   <li>if the host has resources to support the system VM, we check if its hypervisor type has a system VM template registered for it, if so we use it as the host to deploy the system VM
     * </ul>
     * @param c cluster to look for a host to deploy a system VM
     * @param excludeHosts hosts to exclude from the search
     * @return {@link HostVO} to deploy a system VM, it may return null if no suitable hosts have been found
     */
    private HostVO searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(ClusterVO c, List<HostVO> excludeHosts) {
        List<HostVO> allHostsInCluster = hostService.listAllHostsInCluster(c);
        allHostsInCluster.removeAll(excludeHosts);
        Collections.shuffle(allHostsInCluster);
        for (HostVO h : allHostsInCluster) {
            if (canDeployAutonomiccsSystemVmOnHost(h)) {
                return h;
            }
        }
        logger.info(String.format("Could not find any suitable hosts to deploy the system VM into cluster [clusterId=%d, clusterName=%s]", c.getId(), c.getName()));
        return null;
    }

    /**
     *  <p>This method will load the default service offering used for Autonomiccs system VMs using {@link AutonomiccsServiceOfferingService#searchAutonomiccsServiceOffering()};
     *  then it will create a POJO using {@link HostResourcesService#createHostResources(HostVO)}.
     *  <p>With the data, it will check if the host has enough resources to support the VM using {@link #canHostSupportVm(ServiceOfferingVO, HostResources)};
     *  if the host has enough resources, it will also check if the hypervisor type of the given {@link HostVO} has an Autonomiccs system VM template installed.
     *
     * @param h host candidate to receive an Autonomiccs system VM
     * @return true if the host has enough resources to receive the VM and if its hypervisor type has an Autonomiccs system VM template installed
     */
    private boolean canDeployAutonomiccsSystemVmOnHost(HostVO h) {
        ServiceOfferingVO vmServiceOffering = autonomiccsServiceOfferingService.searchAutonomiccsServiceOffering();
        HostResources hostResources = hostResourcesService.createHostResources(h);
        return canHostSupportVm(vmServiceOffering, hostResources) && autonomiccsSystemVmTemplateService.isTemplateRegisteredAndReadyForHypervisor(h.getHypervisorType());
    }

    /**
     * <p>This method checks if the host has sufficient resources to execute a VM with the given {@link ServiceOfferingVO}. The checking process is the following:
     * <ul>
     *  <li>Check if the number of CPU socket of the given host is greater than or equals to the number of sockets of the given {@link ServiceOfferingVO};
     *  <li>check if the host free CPU frequency + CPU over provisioning factor is greater than or equals to the frequency of the given {@link ServiceOfferingVO}.
     *  <li>then, we check if the host total free memory + memory over provisioning is greater than or equals to the memory requested by the given {@link ServiceOfferingVO}.
     * <ul>
     *
     * @param vmServiceOffering VM service offering
     * @param hostResources POJO that facilitates our job while checking if the host can receive the VM with the given service offering
     * @return true if the host has enough resources to receive the VM
     */
    private boolean canHostSupportVm(ServiceOfferingVO vmServiceOffering, HostResources hostResources) {
        if (vmServiceOffering.getCpu() > hostResources.getCpus()) {
            return false;
        }
        float hostTotalCpu = hostResources.getCpuOverprovisioning() * hostResources.getCpus() * hostResources.getSpeed();
        float hostUsedCpu = hostResources.getUsedCpu();
        if ((vmServiceOffering.getSpeed() * vmServiceOffering.getCpu()) > hostTotalCpu - hostUsedCpu) {
            return false;
        }

        float hostTotalMemoryInMegaBytes = (hostResources.getMemoryOverprovisioning() * hostResources.getTotalMemoryInBytes()) / ONE_MEGABYTE_IN_BYTES;
        long usedMemoryInMegabytes = hostResources.getUsedMemoryInMegaBytes();
        if (vmServiceOffering.getRamSize() > hostTotalMemoryInMegaBytes - usedMemoryInMegabytes) {
            return false;
        }
        return true;
    }

    /**
     * <p>This method will look for another host at the same Pod or cluster to re-start the system VM. The process is the following:
     * <ul>
     *  <li>if the {@link VMInstanceVO#getHostId()} returns null, we use the method {@link #searchForRandomHostInPodToDeployAutonomiccsSystemVm(HostPodVO)} to look for another host in the pod to re-start the VM
     *  <li>otherwise, we load the host in which the VM was running on with {@link HostService#findHostById(Long)} and then we use the method {@link #searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(HostVO)} to find for another host in the same cluster
     * </ul>
     * @param vmInstance that we are looking for other hosts to re-start it
     * @return a host to re-started the given VM; it may return null if no suitable hosts were found.
     */
    public HostVO searchForAnotherRandomHostToStartSystemVm(VMInstanceVO vmInstance) {
        Long hostId = vmInstance.getHostId();
        if (hostId == null) {
            Long podId = vmInstance.getPodIdToDeployIn();
            return searchForRandomHostInPodToDeployAutonomiccsSystemVm(podService.findPodById(podId));
        }
        HostVO hostInWhichVmWasRunning = hostService.findHostById(hostId);
        return searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(hostInWhichVmWasRunning);
    }

    /**
     * This method tries to find a suitable host to re-start a system VM.
     * We create a list of hosts to ignore with the given {@link HostVO} and then, we use the {@link #searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(ClusterVO, List)} to look for another host in the cluster of the given cluster.
     *
     * @param excludeHost hosts to be excluded from the search
     * @return a host to re-start an Autonomiccs system VM; if no suitable hosts were found this method returns null
     */
    private HostVO searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHost(HostVO excludeHost) {
        List<HostVO> hostToExclude = new ArrayList<>(1);
        hostToExclude.add(excludeHost);

        ClusterVO cluster = clusterService.findById(excludeHost.getClusterId());
        HostVO host = searchForAnotherRandomHostInTheClusterToStartSystemVmExcludingHosts(cluster, hostToExclude);
        if (host != null) {
            return host;
        }
        logger.info(String.format("Could not find a suitable host to re-start the Autonomiccs system vms in cluster [id=%d]", excludeHost.getClusterId()));
        return null;
    }

    /**
     * Will call the deploySystemVM method and will install openJDK 1.7 using
     * SSH in the SystemVM that is created.
     *
     * @param hostId
     *            in which the VM is being deployed
     * @param systemVmType
     *            type of vm being deployed
     * @return {@link AutonomiccsSystemVm} that represents the deployed VM
     */
    public AutonomiccsSystemVm deploySystemVmWithJAVA(Long hostId, SystemVmType systemVmType) {
        AutonomiccsSystemVm vmInstance = deploySystemVm(hostId, systemVmType);
        String managementIp = vmInstance.getManagementIpAddress();
        sshUtils.executeCommandOnHostViaSsh(managementIp, "aptitude update");
        sshUtils.executeCommandOnHostViaSsh(managementIp, commandToInstallOpenJDK7);
        return vmInstance;
    }

    /**
     * <p>It deploys an Autonomiccs system VM into the provided host. We will discover the template based on the hypervisor type of the host.
     * The prefix that has to be informed is used to mark the type of agent that is being deployed. This method will wait until the deployed VM is up.
     * If the VM for some reason do not boot, a {@link CloudRuntimeException} exception will be thrown.
     *
     * @param hostId
     *            in which the VM is being deployed
     * @param systemVmType
     *            type of vm being deployed
     * @return {@link AutonomiccsSystemVm} that represents the deployed VM
     */
    private AutonomiccsSystemVm deploySystemVm(Long hostId, SystemVmType systemVmType) {
        HostVO host = hostService.findHostById(hostId);
        validateParametersToDeployTheSystemVm(hostId, host);
        VMTemplateVO systemVmTemplate = getSystemVmTemplate(host);

        DataCenterDeployment plan = new DataCenterDeployment(host.getDataCenterId(), host.getPodId(), host.getClusterId(), host.getId(), null, null);
        Account systemAcct = accountManager.getSystemAccount();
        LinkedHashMap<Network, List<? extends NicProfile>> networks = getSystemVmNetworks(host, plan, systemAcct);
        long id = getNextSystemVmId();
        String name = createAutonomiccsSystemVmNameForType(id, systemVmType, getVirtualMachineInstanceSuffix());

        AutonomiccsSystemVm autonomiccsSystemVm = createTheAutonomiccsSystemVm(systemVmTemplate, systemAcct, id, name);
        autonomiccsSystemVm = allocateAndStartTheSystemVm(systemVmTemplate, plan, networks, autonomiccsSystemVm);

        waitUntilTheAutonomiccsSystemVmIsUpAndRunning(autonomiccsSystemVm);
        return autonomiccsSystemVm;
    }

    /**
     *  <p>This method tried to allocate the given Autonomiccs system vm on the environment. The method used to allocate it is {@link VirtualMachineManager#allocate(String, com.cloud.template.VirtualMachineTemplate, com.cloud.offering.ServiceOffering, LinkedHashMap, com.cloud.deploy.DeploymentPlan, com.cloud.hypervisor.Hypervisor.HypervisorType)}.
     *  After the VM is allocated, we try to start it using {@link VirtualMachineManager#advanceStart(String, Map, com.cloud.deploy.DeploymentPlanner)}.
     */
    private AutonomiccsSystemVm allocateAndStartTheSystemVm(VMTemplateVO systemVmTemplate, DataCenterDeployment plan, LinkedHashMap<Network, List<? extends NicProfile>> networks,
            AutonomiccsSystemVm autonomiccsSystemVm) {
        try {
            virtualMachineManager.allocate(autonomiccsSystemVm.getInstanceName(), systemVmTemplate, autonomiccsSystemVmServiceOffering, networks, plan, null);

            autonomiccsSystemVm = autonomiccsSystemVmDao.findById(autonomiccsSystemVm.getId());
            virtualMachineManager.advanceStart(autonomiccsSystemVm.getUuid(), null, null);
            autonomiccsSystemVm = autonomiccsSystemVmDao.findById(autonomiccsSystemVm.getId());
        } catch (InsufficientCapacityException | ConcurrentOperationException | ResourceUnavailableException | OperationTimedoutException e) {
            throw new CloudRuntimeException("An exception occurred when deploying a Autonomiccs system VM.", e);
        }
        return autonomiccsSystemVm;
    }

    /**
     *  <p>This method will check if the given system VM is up and running.
     *  The check is executed by the {@link HostUtils#isHostReachable(String)} method. If the host is not reachable we use the {@link ThreadUtils#sleepThread(int)} to sleep the thread for 5 seconds.
     *  We will execute 100 tests to check if the system VM is up and running, after that an exception is going to be raised.
     */
    private void waitUntilTheAutonomiccsSystemVmIsUpAndRunning(AutonomiccsSystemVm autonomiccsSystemVm) {
        for (int i = 0; i < 100; i++) {
            logger.debug("Checking for the %d time(s) if the system VM [name=%s], [id=%d] is reachable ", i, autonomiccsSystemVm.getInstanceName(), autonomiccsSystemVm.getId());
            if (hostUtils.isHostReachable(autonomiccsSystemVm.getManagementIpAddress())) {
                logger.info("The system VM [name=%s], [id=%d] is reachable after %d tries.", autonomiccsSystemVm.getInstanceName(), autonomiccsSystemVm.getId(), i);
                break;
            }
            threadUtils.sleepThread(5);
        }
        if (!hostUtils.isHostReachable(autonomiccsSystemVm.getManagementIpAddress())) {
            throw new CloudRuntimeException(String.format("The system VM [name=%s], [id=%d] is not reachable, maybe a problem has happened while starting it.",
                    autonomiccsSystemVm.getInstanceName(),
                    autonomiccsSystemVm.getId()));
        }
    }

    /**
     *  <p>This method creates an instance for an autonomiccs system VM template.
     *  It also persists the register on the database using {@link AutonomiccsSystemVmDao#persist(AutonomiccsSystemVm)}
     *
     * @return an instance of an Autonomiccs system VM template
     */
    private AutonomiccsSystemVm createTheAutonomiccsSystemVm(VMTemplateVO systemVmTemplate, Account systemAcct, long id, String name) {
        AutonomiccsSystemVm autonomiccsSystemVm = new AutonomiccsSystemVm(id, autonomiccsSystemVmServiceOffering.getId(), name, systemVmTemplate.getId(),
                systemVmTemplate.getHypervisorType(), systemVmTemplate.getGuestOSId(), systemAcct.getDomainId(), systemAcct.getId(),
                accountManager.getSystemUser().getId(), autonomiccsSystemVmServiceOffering.getOfferHA());
        autonomiccsSystemVm.setDynamicallyScalable(systemVmTemplate.isDynamicallyScalable());
        autonomiccsSystemVm = autonomiccsSystemVmDao.persist(autonomiccsSystemVm);
        return autonomiccsSystemVm;
    }

    private LinkedHashMap<Network, List<? extends NicProfile>> getSystemVmNetworks(HostVO host, DataCenterDeployment plan, Account systemAcct) {
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>();
        NicProfile defaultNic = createDefaultNic();

        NetworkVO defaultNetwork = getDefaultNetwork(host.getDataCenterId());
        networks.put(networkManager.setupNetwork(systemAcct, networkOfferingDao.findById(defaultNetwork.getNetworkOfferingId()), plan, null, null, false).get(0),
                new ArrayList<NicProfile>(Arrays.asList(defaultNic)));

        List<? extends NetworkOffering> offerings = networkModel.getSystemAccountNetworkOfferings(NetworkOffering.SystemControlNetwork, NetworkOffering.SystemManagementNetwork);
        for (NetworkOffering offering : offerings) {
            networks.put(networkManager.setupNetwork(systemAcct, offering, plan, null, null, false).get(0), new ArrayList<NicProfile>());
        }
        return networks;
    }

    /**
     * This method creates and return a {@link NicProfile}, it also sets the default NIC as true using {@link NicProfile#setDefaultNic(boolean)} and the device if as 2 using {@link NicProfile#setDeviceId(int)}
     * @return a NIC profile {@link NicProfile}
     */
    private NicProfile createDefaultNic() {
        NicProfile defaultNic = new NicProfile();
        defaultNic.setDefaultNic(true);
        defaultNic.setDeviceId(2);
        return defaultNic;
    }

    /**
     * This method looks for an autonomiccs system vm template for the hypervisor of the given host; if the template is not found, an exception is thrown.
     * @return {@link VMTemplateVO} of the Autonomiccs system vm template for the hypervisor type of the given host
     */
    private VMTemplateVO getSystemVmTemplate(HostVO host) {
        VMTemplateVO systemVmTemplate = autonomiccsSystemVmTemplateService.findAutonomiccsSystemVmTemplate(host.getHypervisorType());
        if (systemVmTemplate == null) {
            throw new CloudRuntimeException(String.format("Could not find a System VM template for the host hypervisors [%s]", host.getHypervisorType()));
        }
        return systemVmTemplate;
    }

    /**
     * <p>This method will check if the host is not null and if there is an Autonomiccs system VM registered for the host's hyeprvisor type.
     * If these requirements are not met, an exception will be raised.
     *
     * @param hostId to deploy the system VM
     */
    private void validateParametersToDeployTheSystemVm(Long hostId, HostVO host) {
        if (host == null) {
            throw new CloudRuntimeException(String.format("Could not find a host with the provieded id [%d]", hostId));
        }
        if (!autonomiccsSystemVmTemplateService.isTemplateRegisteredAndReadyForHypervisor(host.getHypervisorType())) {
            throw new CloudRuntimeException(
                    String.format("There is no Autonomiccs system VM for hypervisor [%s], so we do not deploy a system Vm for it.", host.getHypervisorType()));
        }
    }

    /**
     * This method will generate the next system VM id using the method {@link AutonomiccsSystemVmDao#getNextInSequence(Class, String)}
     * @return the next system VM id
     */
    private long getNextSystemVmId() {
        Long id = autonomiccsSystemVmDao.getNextInSequence(Long.class, "id");
        if(id == null){
            throw new CloudRuntimeException("It was not possible to find an id to create a system VM.");
        }
        return id;
    }

    /**
     * <p>This method will look for the default network type for the given zone ({@link DataCenterVO}).
     * If the given zone networking configuration is {@link DataCenterVO#getNetworkType()} equals to {@link NetworkType#Advanced} and {@link DataCenterVO#isSecurityGroupEnabled()} equals to true, we use the method {@link #getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(long, DataCenterVO)}.
     * Otherwise, we use the method {@link #getDefaultNetwork(DataCenterVO)}.
     *
     * @param dataCenterId id of the zone
     * @return the default network of the given zone
     */
    private NetworkVO getDefaultNetwork(long dataCenterId) {
        DataCenterVO dc = dataCenterDao.findById(dataCenterId);
        if (dc.getNetworkType() == NetworkType.Advanced && dc.isSecurityGroupEnabled()) {
            return getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(dataCenterId, dc);
        }
        return getDefaultNetwork(dc);
    }

    /**
     * <p>This method will look for the default network type for the given zone ({@link DataCenterVO}).
     * The method should be called only when network type meets either one of the following criterias:
     * <ul>
     *  <li> {@link DataCenterVO#getNetworkType()} equals to {@link NetworkType#Basic}
     *  <li> {@link DataCenterVO#getNetworkType()} equals to {@link NetworkType#Advanced} and {@link DataCenterVO#isSecurityGroupEnabled()} equals to false
     *</ul>
     *
     * @param dc zone to look for the default network type
     */
    private NetworkVO getDefaultNetwork(DataCenterVO dc) {
        TrafficType defaultTrafficType = TrafficType.Public;
        if (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()) {
            defaultTrafficType = TrafficType.Guest;
        }
        List<NetworkVO> defaultNetworks = networkDao.listByZoneAndTrafficType(dc.getId(), defaultTrafficType);
        if (defaultNetworks.size() != 1) {
            throw new CloudRuntimeException(String.format("Found %d networks of type %s, when it is only expected to find 1.", defaultNetworks.size(), defaultTrafficType));
        }
        return defaultNetworks.get(0);
    }

    /**
     * <p>This method will look for the default network type for the given zone ({@link DataCenterVO}).
     * The method should be called only when network type meets either one of the following criteria:
     * <ul>
     *  <li> {@link DataCenterVO#getNetworkType()} equals to {@link NetworkType#Advanced} and {@link DataCenterVO#isSecurityGroupEnabled()} equals to true
     *</ul>
     *
     * @param dc zone to look for the default network type
     */
    private NetworkVO getDefaultNetworkForAdvancedNetworkingWithSecurityGroupds(long dataCenterId, DataCenterVO dc) {
        List<NetworkVO> networks = networkDao.listByZoneSecurityGroup(dataCenterId);
        if (CollectionUtils.isEmpty(networks)) {
            throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + dc);
        }
        return networks.get(0);
    }

    /**
     * The name created follows the convention: system VM type suffix-id of VM-instanceSuffix
     *
     * @return the instance name for Autonomiccs system VMs
     */
    private String createAutonomiccsSystemVmNameForType(long id, SystemVmType systemVmType, String instanceSuffix) {
        return String.format("%s-%d-%s", systemVmType.getNamePrefix(), id, instanceSuffix);
    }

    /**
     * Retrieves the virtual machine name suffix from the database. The suffix is defined by the "instance.name" parameter.
     *
     * @return virtual machine instance suffix
     */
    private String getVirtualMachineInstanceSuffix() {
        Map<String, String> configs = getConfigurationsFromDatabase();
        return configs.get("instance.name");
    }

    /**
     * @return all of the configurations at the "configuration" table in the cloud database
     */
    private Map<String, String> getConfigurationsFromDatabase() {
        return this.configurationDao.getConfiguration("management-server", new HashMap<String, Object>());
    }

    /**
     * This method is used to load the default service offering for the Autonomiccs system VM.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        loadAutonomiccsSystemVmServiceOffering();
        if (autonomiccsSystemVmServiceOffering == null) {
            throw new CloudRuntimeException("Could not register the Autonomiccs system VMs service offering.");
        }
    }

    /**
     * <p>This method tries to load the default Autonomiccs system VM service offering; it uses the {@link AutonomiccsServiceOfferingService#searchAutonomiccsServiceOffering()} to search of the offering.
     * If a service offering is not found, it will create the service offering on the data base; the method used to create is {@link AutonomiccsServiceOfferingService#createAutonomiccsServiceOffering()}.
     */
    private void loadAutonomiccsSystemVmServiceOffering() {
        try{
            autonomiccsSystemVmServiceOffering = autonomiccsServiceOfferingService.searchAutonomiccsServiceOffering();
        } catch (CloudRuntimeException ex) {
            logger.debug("Autonomiccs system VMs service offering not in database; we will create one.", ex);
            autonomiccsServiceOfferingService.createAutonomiccsServiceOffering();
            autonomiccsSystemVmServiceOffering = autonomiccsServiceOfferingService.searchAutonomiccsServiceOffering();
        }
    }
}
