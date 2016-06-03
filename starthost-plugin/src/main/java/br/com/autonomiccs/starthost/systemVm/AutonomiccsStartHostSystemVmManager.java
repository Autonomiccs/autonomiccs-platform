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
package br.com.autonomiccs.starthost.systemVm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.host.HostVO;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachineManager;

import br.com.autonomiccs.autonomic.plugin.common.beans.AutonomiccsSystemVm;
import br.com.autonomiccs.autonomic.plugin.common.enums.SystemVmType;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomicClusterManagementHeuristicService;
import br.com.autonomiccs.autonomic.plugin.common.services.AutonomiccsSystemVmDeploymentService;
import br.com.autonomiccs.autonomic.plugin.common.services.HostService;
import br.com.autonomiccs.autonomic.plugin.common.services.PodService;
import br.com.autonomiccs.autonomic.plugin.common.services.StartHostSystemVmService;
import br.com.autonomiccs.autonomic.plugin.common.services.VirtualMachineService;
import br.com.autonomiccs.autonomic.plugin.common.services.ZoneService;
import br.com.autonomiccs.autonomic.plugin.common.utils.SshUtils;

@Component
public class AutonomiccsStartHostSystemVmManager implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final long ONE_MINUTE_IN_MILLISECONDS = 60000;
    private static final int NUMBER_OF_MINUTES_BETWEEN_CHECKS = 15;
    private static final int NUMBER_OF_MINUTES_BETWEEN_DESTROY_CHECKS = 60;

    private File startHostApplicationConfiguration;
    private File startHostApplicationExecutable;
    private File startHostApplicationInicializationScript;
    private File startHostApplicationLogConfiguration;

    @Autowired
    private AutonomiccsSystemVmDeploymentService autonomiccsSystemVmDeploymentService;

    @Autowired
    private SshUtils sshUtils;

    @Autowired
    private HostService hostService;

    @Autowired
    private PodService podService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private StartHostSystemVmService startHostSystemVmService;

    @Autowired
    private VirtualMachineManager virtualMachineManager;

    @Autowired
    private VirtualMachineService virtualMachineService;

    @Autowired
    private AutonomicClusterManagementHeuristicService autonomicManagementHeuristicService;

    @Autowired
    @Qualifier("wakeOnLanHostApplicationVersion")
    private String wakeOnLanHostApplicationVersion;

    @Scheduled(initialDelay = ONE_MINUTE_IN_MILLISECONDS, fixedDelay = ONE_MINUTE_IN_MILLISECONDS * NUMBER_OF_MINUTES_BETWEEN_DESTROY_CHECKS)
    public void destroyStartHostSystemVmsThatAreNotNeeded() {
        List<DataCenterVO> zones = zoneService.listAllZonesEnabled();
        if (CollectionUtils.isEmpty(zones)) {
            logger.info("No enabled zones to destroy start host system vms.");
            return;
        }
        for (DataCenterVO zone : zones) {
            List<HostPodVO> allPodsEnabledFromZone = podService.getAllPodsEnabledFromZone(zone.getId());
            if (CollectionUtils.isEmpty(allPodsEnabledFromZone)) {
                logger.info(String.format("Coul not find an enabled pod for zone id[%d], name[%s]", zone.getId(), zone.getName()));
                continue;
            }
            for (HostPodVO pod : allPodsEnabledFromZone) {
                if (!podService.isThereAnyHostOnPodDeactivatedByOurManager(pod.getId())) {
                    Long startHostServiceVmIdFromPod = startHostSystemVmService.getStartHostServiceVmIdFromPod(pod.getId());
                    if (startHostServiceVmIdFromPod == null) {
                        continue;
                    }
                    VMInstanceVO vmInstance = virtualMachineService.searchVmInstanceById(startHostServiceVmIdFromPod);
                    if (vmInstance != null) {
                        destroyCleverCloudSystemVm(vmInstance);
                    }
                }
            }
        }
    }

    /**
     * Loads all available pods and checks if they have a system VM running.
     * If the Pod does not have a system VM, it will create one for it.
     * The interval between the execution of the {@link #checkIfAllPodsHaveStartHostSystemVmRunning()}, that time is controlled by
     * {@link #NUMBER_OF_MINUTES_BETWEEN_CHECKS}
     */
    @Scheduled(initialDelay = ONE_MINUTE_IN_MILLISECONDS, fixedDelay = ONE_MINUTE_IN_MILLISECONDS * NUMBER_OF_MINUTES_BETWEEN_CHECKS)
    public void checkIfAllPodsHaveStartHostSystemVmRunning() {
        if (!autonomicManagementHeuristicService.getAdministrationAlgorithm().canHeuristicShutdownHosts() && !hostService.isThereAnyHostOnCloudDeactivatedByOurManager()) {
            return;
        }

        List<DataCenterVO> zones = zoneService.listAllZonesEnabled();
        if (CollectionUtils.isEmpty(zones)) {
            logger.info("No enabled zones to deploy start host system vms.");
            return;
        }
        for (DataCenterVO zone : zones) {
            List<HostPodVO> allPodsEnabledFromZone = podService.getAllPodsEnabledFromZone(zone.getId());
            if (CollectionUtils.isEmpty(allPodsEnabledFromZone)) {
                logger.info(String.format("Coul not find an enabled pod for zone id[%d], name[%s]", zone.getId(), zone.getName()));
                continue;
            }
            for (HostPodVO pod : allPodsEnabledFromZone) {
                if (!podService.isThereAnyHostOnPodDeactivatedByOurManager(pod.getId())) {
                    continue;
                }
                try {
                    checkAndDeploydStartHostServicesVmsIfNeeded(pod);
                } catch (Exception e) {
                    logger.info(String.format("A problem happened while checking/deploying a VM for the Pod [id=%d]", pod.getId()), e);
                }

            }
        }
    }

    private void checkAndDeploydStartHostServicesVmsIfNeeded(HostPodVO pod) {
        long podId = pod.getId();
        if (startHostSystemVmService.isStartHostSystemVmDeployedOnPod(podId)) {
            if (startHostSystemVmService.isStartHostServiceVmRunningOnPod(podId)) {
                if (!startHostSystemVmService.isStartHostServiceVmReadyToStartHostOnPod(podId)) {
                    Long startHostServiceVmIdFromPod = startHostSystemVmService.getStartHostServiceVmIdFromPod(podId);
                    VMInstanceVO vmInstance = virtualMachineService.searchVmInstanceById(startHostServiceVmIdFromPod);
                    destroyCleverCloudSystemVm(vmInstance);
                }
                return;
            }
            Long startHostServiceVmIdFromPod = startHostSystemVmService.getStartHostServiceVmIdFromPod(podId);
            VMInstanceVO vmInstance = virtualMachineService.searchVmInstanceById(startHostServiceVmIdFromPod);

            HostVO hostToStartVm = autonomiccsSystemVmDeploymentService.searchForAnotherRandomHostToStartSystemVm(vmInstance);
            if (hostToStartVm == null) {
                //did not find any suitable hosts in cluster, so remove it and hope for the best next time
                destroyCleverCloudSystemVm(vmInstance);
                return;
            }
            DataCenterDeployment plan = new DataCenterDeployment(hostToStartVm.getDataCenterId(), hostToStartVm.getPodId(), hostToStartVm.getClusterId(), hostToStartVm.getId(),
                    null, null);
            try {
                virtualMachineManager.advanceStart(vmInstance.getUuid(), null, plan, null);
            } catch (ConcurrentOperationException | InsufficientCapacityException | ResourceUnavailableException | OperationTimedoutException e) {
                logger.error(String.format("Problems while trying to start clever cloud system VM. Vmid[%d], vmName[%s]", vmInstance.getId(), vmInstance.getInstanceName()), e);
            }
            return;
        }
        HostVO hostToDeployVm = autonomiccsSystemVmDeploymentService.searchForRandomHostInPodToDeployAutonomiccsSystemVm(pod);
        if (hostToDeployVm != null) {
            AutonomiccsSystemVm vmInstace = autonomiccsSystemVmDeploymentService.deploySystemVmWithJAVA(hostToDeployVm.getId(), SystemVmType.ClusterManagerStartHostService);
            String vmIp = vmInstace.getManagementIpAddress();
            uploadFilesFromClassPathToVM(vmIp);
            configureStartUpServiceOnVM(vmIp);
            rebootVM(vmIp);
        }
    }

    /**
     * Reboots the virtual machine.
     *
     * @param vmIp
     *            The Management IP address of the virtual machine.
     */
    private void rebootVM(String vmIp) {
        sshUtils.executeCommandOnHostViaSsh(vmIp, "reboot");
    }

    /**
     * Define the shell script injected on VM with uploadFilesFromClassPathToVM as a service
     * in the virtual machine.
     *
     * @param vmIp
     *            The Management IP address of the virtual machine.
     */
    private void configureStartUpServiceOnVM(String vmIp) {
        sshUtils.executeCommandOnHostViaSsh(vmIp, "update-rc.d startup defaults");
    }

    /**
     * Upload the files from Classpath that are required to define
     * the wake on LAN application as a service in the virtual machine
     *
     * @param vmIp
     *            The Management IP address of the virtual machine.
     */
    private void uploadFilesFromClassPathToVM(String vmIp) {
        String remotePath = "/root/";
        sshUtils.sendFileToHost(startHostApplicationExecutable, "startupHost.jar", remotePath, vmIp);
        sshUtils.sendFileToHost(startHostApplicationConfiguration, "application.yml", remotePath, vmIp);
        sshUtils.sendFileToHost(startHostApplicationLogConfiguration, "log4j.properties", remotePath, vmIp);
        sshUtils.sendFileToHost(startHostApplicationInicializationScript, "startup", "/etc/init.d/", vmIp);
    }

    private void destroyCleverCloudSystemVm(VMInstanceVO vmInstance) {
        try {
            virtualMachineManager.expunge(vmInstance.getUuid());
            vmInstance.setPrivateMacAddress(null);
            vmInstance.setPrivateIpAddress(null);
            virtualMachineService.update(vmInstance.getId(), vmInstance);
            virtualMachineService.remove(vmInstance.getId());
        } catch (Exception e) {
            logger.warn("Unable to expunge VM." + vmInstance, e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        copyStartHostApplicationToTempFile();
        startHostApplicationConfiguration = createTempFileForResourceInJar("/application.yml");
        startHostApplicationInicializationScript = createTempFileForResourceInJar("/startup");
        startHostApplicationLogConfiguration = createTempFileForResourceInJar("/log4j.properties");
    }

    private void copyStartHostApplicationToTempFile() throws FileNotFoundException, IOException {
        String cloudStackWebappLibFolder = getCloudStackWebappLibFolder();
        String startHostApplicationFullQualifiedPath = cloudStackWebappLibFolder + "wakeonlan-service-" + wakeOnLanHostApplicationVersion;
        FileInputStream startHostApplicationStream = new FileInputStream(startHostApplicationFullQualifiedPath);
        startHostApplicationExecutable = createTempFileForStream(startHostApplicationStream);
        IOUtils.closeQuietly(startHostApplicationStream);
    }

    private String getCloudStackWebappLibFolder() {
        String regexFindCloudStackLibFolder = "jar:file:(.+)\\/.*\\.jar.*";
        Pattern patternFindCloudStackLibFolder = Pattern.compile(regexFindCloudStackLibFolder);

        String fileUsedToUncoverCloudStackLibFolder = "/application.yml";
        URL resource = getClass().getResource(fileUsedToUncoverCloudStackLibFolder);
        Matcher matcher = patternFindCloudStackLibFolder.matcher(ObjectUtils.toString(resource));
        if(matcher.find()){
            return matcher.group(1) + "/";
        }
        throw new CloudRuntimeException("Could not find cloudstack lib folder.");
    }

    private File createTempFileForResourceInJar(String resourceNameInClasspath) throws IOException {
        InputStream resourceAsStream = getClass().getResourceAsStream(resourceNameInClasspath);
        File tempFileForStream = createTempFileForStream(resourceAsStream);

        IOUtils.closeQuietly(resourceAsStream);
        return tempFileForStream;
    }

    private File createTempFileForStream(InputStream resourceAsStream) throws IOException, FileNotFoundException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        FileOutputStream tempFileStream = new FileOutputStream(file);

        IOUtils.copy(resourceAsStream, tempFileStream);
        IOUtils.closeQuietly(tempFileStream);
        return file;
    }
}
