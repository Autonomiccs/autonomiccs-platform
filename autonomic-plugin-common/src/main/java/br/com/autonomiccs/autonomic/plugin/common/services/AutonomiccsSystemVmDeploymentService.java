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

import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
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
import com.cloud.vm.VirtualMachineManager;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmDao;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.pojos.AutonomiccsSystemVm;
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
     * It deployed an Autonomiccs system VM into the provided host. We will
     * discover the template based on the hypervisor type of the host. The
     * prefix that has to be informed is used to mark the type of agent that is
     * being deployed.
     * This method will wait until the deployed VM is up. If the VM for some reason do not boot, a {@link CloudRuntimeException} exception will be thrown.
     *
     * @param hostId
     *            in which the VM is being deployed
     * @param systemVmType
     *            type of vm being deployed
     * @return {@link AutonomiccsSystemVm} that represents the deployed VM
     */
    public AutonomiccsSystemVm deploySystemVm(Long hostId, SystemVmType systemVmType) {
        HostVO host = hostService.findHostById(hostId);
        if (host == null) {
            throw new CloudRuntimeException(String.format("Could not find a host with the provieded id [%d]", hostId));
        }
        if (!autonomiccsSystemVmTemplateService.isTemplateRegisteredAndReadyForHypervisor(host.getHypervisorType())) {
            throw new CloudRuntimeException(
                    String.format("There is no Autonomiccs system VM for hypervisor [%s], so we do not deploy a system Vm for it.", host.getHypervisorType()));
        }
        VMTemplateVO systemVmTemplate = autonomiccsSystemVmTemplateService.findAutonomiccsSystemVmTemplate(host.getHypervisorType());
        if (systemVmTemplate == null) {
            throw new CloudRuntimeException(String.format("Could not find a System VM template for the host hypervisors [%s]", host.getHypervisorType()));
        }

        Account systemAcct = accountManager.getSystemAccount();

        long id = getNextSystemVmId();
        String name = createAutonomiccsSystemVmNameForType(id, systemVmType, getVirtualMachineInstanceSuffix());

        long dataCenterId = host.getDataCenterId();
        DataCenterDeployment plan = new DataCenterDeployment(dataCenterId, host.getPodId(), host.getClusterId(), host.getId(), null, null);

        NetworkVO defaultNetwork = getDefaultNetwork(dataCenterId);
        List<? extends NetworkOffering> offerings = networkModel.getSystemAccountNetworkOfferings(NetworkOffering.SystemControlNetwork, NetworkOffering.SystemManagementNetwork);
        LinkedHashMap<Network, List<? extends NicProfile>> networks = new LinkedHashMap<>(offerings.size() + 1);
        NicProfile defaultNic = new NicProfile();
        defaultNic.setDefaultNic(true);
        defaultNic.setDeviceId(2);

        networks.put(networkManager.setupNetwork(systemAcct, networkOfferingDao.findById(defaultNetwork.getNetworkOfferingId()), plan, null, null, false).get(0),
                new ArrayList<NicProfile>(Arrays.asList(defaultNic)));

        for (NetworkOffering offering : offerings) {
            networks.put(networkManager.setupNetwork(systemAcct, offering, plan, null, null, false).get(0), new ArrayList<NicProfile>());
        }

        AutonomiccsSystemVm autonomiccsSystemVm = new AutonomiccsSystemVm(id, autonomiccsSystemVmServiceOffering.getId(), name, systemVmTemplate.getId(),
                systemVmTemplate.getHypervisorType(), systemVmTemplate.getGuestOSId(), systemAcct.getDomainId(), systemAcct.getId(),
                accountManager.getSystemUser().getId(), autonomiccsSystemVmServiceOffering.getOfferHA());
        autonomiccsSystemVm.setDynamicallyScalable(systemVmTemplate.isDynamicallyScalable());
        autonomiccsSystemVm = autonomiccsSystemVmDao.persist(autonomiccsSystemVm);

        try {
            virtualMachineManager.allocate(name, systemVmTemplate, autonomiccsSystemVmServiceOffering, networks, plan, null);

            autonomiccsSystemVm = autonomiccsSystemVmDao.findById(id);
            virtualMachineManager.advanceStart(autonomiccsSystemVm.getUuid(), null, null);
            autonomiccsSystemVm = autonomiccsSystemVmDao.findById(id);
        } catch (ConcurrentOperationException | ResourceUnavailableException | OperationTimedoutException | InsufficientCapacityException e) {
            throw new CloudRuntimeException("Insufficient capacity exception when deploying a Autonomiccs system VM.", e);
        }

        for (int i = 0; i < 100; i++) {
            logger.debug(String.format("Checking for the %d time(s) if the system VM [name=%s], [id=%d] is reachable ", i, autonomiccsSystemVm.getInstanceName(),
                    autonomiccsSystemVm.getId()));
            if (hostUtils.isHostReachable(autonomiccsSystemVm.getManagementIpAddress())) {
                logger.info(String.format("We noticed out that the system VM [name=%s], [id=%d] is reachable after %d tries.", autonomiccsSystemVm.getInstanceName(),
                        autonomiccsSystemVm.getId(), i));
                break;
            }
            threadUtils.sleepThread(5);
        }
        if (!hostUtils.isHostReachable(autonomiccsSystemVm.getManagementIpAddress())) {
            throw new CloudRuntimeException(String.format("The system VM [name=%s], [id=%d] is not reachable, maybe a problem has happened while starting it.",
                    autonomiccsSystemVm.getInstanceName(),
                    autonomiccsSystemVm.getId()));
        }
        return autonomiccsSystemVm;
    }

    private long getNextSystemVmId() {
        Long id = autonomiccsSystemVmDao.getNextInSequence(Long.class, "id");
        if(id == null){
            throw new CloudRuntimeException("It was not possible to find an id to create a system VM.");
        }
        return id;
    }

    private NetworkVO getDefaultNetwork(long dataCenterId) {
        DataCenterVO dc = dataCenterDao.findById(dataCenterId);
        if (dc.getNetworkType() == NetworkType.Advanced && dc.isSecurityGroupEnabled()) {
            List<NetworkVO> networks = networkDao.listByZoneSecurityGroup(dataCenterId);
            if (CollectionUtils.isEmpty(networks)) {
                throw new CloudRuntimeException("Can not found security enabled network in SG Zone " + dc);
            }
            return networks.get(0);
        } else {
            TrafficType defaultTrafficType = TrafficType.Public;
            if (dc.getNetworkType() == NetworkType.Basic || dc.isSecurityGroupEnabled()) {
                defaultTrafficType = TrafficType.Guest;
            }
            List<NetworkVO> defaultNetworks = networkDao.listByZoneAndTrafficType(dataCenterId, defaultTrafficType);

            // api should never allow this situation to happen
            if (defaultNetworks.size() != 1) {
                throw new CloudRuntimeException("Found " + defaultNetworks.size() + " networks of type " + defaultTrafficType + " when expect to find 1");
            }
            return defaultNetworks.get(0);
        }
    }

    /**
     * The name created follows the convention: system VM type suffix-id of
     * VM-instanceSuffix
     *
     * @return the instance name for Autonomiccs system VMs
     */
    private String createAutonomiccsSystemVmNameForType(long id, SystemVmType systemVmType, String instanceSuffix) {
        return String.format("%s-%d-%s", systemVmType.getNamePrefix(), id, instanceSuffix);
    }

    /**
     * Retrieves the virtual machine name suffix from the database. The suffix
     * is defined by the "instance.name" parameter.
     *
     * @return virtual machine instance suffix
     */
    private String getVirtualMachineInstanceSuffix() {
        Map<String, String> configs = getConfigurationsFromDatabase();
        return configs.get("instance.name");
    }

    private Map<String, String> getConfigurationsFromDatabase() {
        return this.configurationDao.getConfiguration("management-server", new HashMap<String, Object>());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadAutonomiccsSystemVmServiceOffering();
        if (autonomiccsSystemVmServiceOffering == null) {
            throw new CloudRuntimeException("Could not register the Autonomiccs system VMs service offering.");
        }
    }

    private void loadAutonomiccsSystemVmServiceOffering() {
        try{
            autonomiccsSystemVmServiceOffering = autonomiccsServiceOfferingService.searchAutonomiccsServiceOffering();
        } catch (CloudRuntimeException ex) {
            logger.debug("Autonomiccs system VMs service offerring not in database; we will create one.", ex);
            autonomiccsServiceOfferingService.createAutonomiccsServiceOffering();
            autonomiccsSystemVmServiceOffering = autonomiccsServiceOfferingService.searchAutonomiccsServiceOffering();
        }
    }
}
