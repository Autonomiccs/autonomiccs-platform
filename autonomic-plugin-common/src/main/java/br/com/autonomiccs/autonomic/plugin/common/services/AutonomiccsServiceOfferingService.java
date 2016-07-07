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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.Storage;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;

/**
 * This class configures and manage the Autonomiccs system VM service offering.
 */
@Service
public class AutonomiccsServiceOfferingService {

    private static final int AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE = 512;
    private static final int AUTONOMICCS_SYSTEM_VM_CPUMHZ = 1000;
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME = "Autonomiccs S.O.";
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME = "Autonomiccs system VMs service offering";

    @Autowired
    private ServiceOfferingDao serviceOfferingDao;

    /**
     * It returns the service offering with the given name; if it fails to find a
     * {@link ServiceOfferingVO} it throws a {@link CloudRuntimeException}.
     */
    protected ServiceOfferingVO searchServiceOfferingByName(String name) {
        List<ServiceOfferingVO> allServiceOffering = serviceOfferingDao.listAll();
        for(ServiceOfferingVO so : allServiceOffering){
            if (name.equals(so.getUniqueName())) {
                return so;
            }
        }
        throw new CloudRuntimeException(String.format("Could not find Autonomiccs System VMs service offering using name [%s]", name));
    }

    /**
     * It returns the Autonomiccs VM service offering ({@link ServiceOfferingVO}).
     */
    public ServiceOfferingVO searchAutonomiccsServiceOffering() {
        return searchServiceOfferingByName(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME);
    }

    /**
     * It returns a {@link ServiceOfferingVO} with the given id.
     */
    public ServiceOfferingVO searchServiceOfferingById(long id) {
        return serviceOfferingDao.findById(id);
    }

    /**
     * It creates a service offering with the necessary atributes of the Autonomiccs system VM.
     */
    public void createAutonomiccsServiceOffering() {
        List<ServiceOfferingVO> offerings = serviceOfferingDao.createSystemServiceOfferings(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME,
                AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME, 1, AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE, AUTONOMICCS_SYSTEM_VM_CPUMHZ, 0, 0, false, null,
                Storage.ProvisioningType.THIN, true, null, true, VirtualMachine.Type.Instance, true);
        if (offerings == null || offerings.size() < 2) {
            throw new CloudRuntimeException("Data integrity problem : Autonomiccs System Offering For VMs has been removed?");
        }
    }
}
