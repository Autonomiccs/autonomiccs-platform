
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
package br.com.autonomiccs.autonomic.algorithms.commons.services;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.config.impl.ConfigurationVO;
import org.springframework.stereotype.Component;

import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

import com.cloud.host.HostVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.VMInstanceDao;

/**
 * This class provides services related with {@link HostResources} objects.
 */
@Component
public class HostResourcesService {

    private final static String CPU_OVERPROVISIONING_CONFIGURARTION_NAME = "cpu.overprovisioning.factor";
    private final static String MEMORY_OVERPROVISIONING_CONFIGURARTION_NAME = "mem.overprovisioning.factor";

    @Inject
    private ServiceOfferingDao serviceOfferingDao;

    @Inject
    private ConfigurationDao configurationDao;

    @Inject
    private VMInstanceDao vmInstanceDao;

    /**
     * Creates a {@link HostResources} object and configures it based on the
     * given {@link HostVO} (with host Id, hostname, cpu speed, number of cpus
     * and total memory.
     *
     * @param hostVo
     * @return
     */
    public HostResources createAndConfigureHostResources(HostVO hostVo) {
        HostResources hResources = new HostResources();
        hResources.setHostId(hostVo.getId());
        hResources.setHostName(hostVo.getName());
        hResources.setSpeed(hostVo.getSpeed());
        hResources.setCpus(hostVo.getCpus());
        hResources.setTotalMemoryInBytes(hostVo.getTotalMemory());
        return hResources;
    }

    /**
     * It creates a {@link HostResources} object using
     * {@link HostResourcesService#createAndConfigureHostResources(HostVO)}
     * method. It also sets VMs resources using
     * {@link HostResources#setVmsResources(List)} method, calculates the
     * allocated resources (using {@link #createHostResources(HostVO)} method)
     * and loads the over-provisioning factors (using
     * {@link #loadHostOverprovisioningFactors(HostResources)} method).
     *
     * @param hostVO
     * @param consolidationAlgorithm
     * @return
     */
    public HostResources createHostResources(HostVO hostVO) {
        HostResources host = createAndConfigureHostResources(hostVO);
        List<VmResources> vms = listVmsFromHost(host);
        host.setVmsResources(vms);
        calculateHostResourcesAllocated(host);
        loadHostOverprovisioningFactors(host);
        return host;
    }

    /**
     * @return lists system and user VMs (running, migrating and starting).
     */
    protected List<VmResources> listVmsFromHost(HostResources host) {
        List<VMInstanceVO> vmInstanceList = vmInstanceDao.listByHostId(host.getHostId());
        List<VmResources> vmResourcesList = new ArrayList<VmResources>();

        for (VMInstanceVO vmInstance : vmInstanceList) {
            if (!isVmCurrentlyUsingHostResource(vmInstance)) {
                continue;
            }
            ServiceOfferingVO serviceOffering = serviceOfferingDao.findById(vmInstance.getServiceOfferingId());
            vmResourcesList.add(new VmResources(vmInstance.getId(), serviceOffering.getCpu(), serviceOffering.getSpeed(), serviceOffering.getRamSize()));
        }
        return vmResourcesList;
    }

    /**
     * Checks if the VM is currently using the host resource. If the VM is in
     * {@link com.cloud.vm.VirtualMachine.State#Running}, {@link com.cloud.vm.VirtualMachine.State#Migrating} or {@link com.cloud.vm.VirtualMachine.State#Starting},
     * then returns true
     *
     * @param vmInstance
     * @return
     */
    protected boolean isVmCurrentlyUsingHostResource(VMInstanceVO vmInstance) {
        return VirtualMachine.State.Running == vmInstance.getState() || VirtualMachine.State.Migrating == vmInstance.getState()
                || VirtualMachine.State.Starting == vmInstance.getState();
    }

    /**
     * Calculates the amount of allocated (used memory and used cpu) resources
     * in the given {@link HostResources}.
     *
     * @note This method only works if the
     *       {@link HostResources#setVmsResources(List)} has already been
     *       executed, otherwise the {@link HostResources} object will not have
     *       a list of {@link VmResources}.
     * @param host
     */
    protected void calculateHostResourcesAllocated(HostResources host) {
        List<VmResources> vmsFromHost = host.getVmsResources();
        long usedCpu = 0;
        long usedMemory = 0;
        for (VmResources vm : vmsFromHost) {
            usedMemory += vm.getMemoryInMegaBytes();
            usedCpu += vm.getCpuSpeed() * vm.getNumberOfCpus();
        }
        host.setUsedCpu(usedCpu);
        host.setUsedMemoryInMegaBytes(usedMemory);
    }

    /**
     * Loads the over-provisioning factos of a given {@link HostResources}. To
     * load a overprovision factor it uses the
     * {@link ConfigurationDao#findByName(String)} using the
     * "CPU_OVERPROVISIONING_CONFIGURARTION_NAME" to get the cpu
     * over-provisioning and "MEMORY_OVERPROVISIONING_CONFIGURARTION_NAME" get
     * the memory over-provisioning.
     *
     * @param host
     */
    protected void loadHostOverprovisioningFactors(HostResources host) {
        ConfigurationVO configVO = configurationDao.findByName(CPU_OVERPROVISIONING_CONFIGURARTION_NAME);
        host.setCpuOverprovisioning(Float.parseFloat(configVO.getValue()));

        configVO = configurationDao.findByName(MEMORY_OVERPROVISIONING_CONFIGURARTION_NAME);
        host.setMemoryOverprovisioning(Float.parseFloat(configVO.getValue()));
    }

}
