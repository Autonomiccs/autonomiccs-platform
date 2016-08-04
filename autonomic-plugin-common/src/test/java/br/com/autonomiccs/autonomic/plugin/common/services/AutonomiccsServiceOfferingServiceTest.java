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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.Storage;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachine;

@RunWith(MockitoJUnitRunner.class)
public class AutonomiccsServiceOfferingServiceTest {

    @Spy
    @InjectMocks
    private AutonomiccsServiceOfferingService spy;
    @Mock
    private ServiceOfferingDao serviceOfferingDao;

    private static final int AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE = 512;
    private static final int AUTONOMICCS_SYSTEM_VM_CPUMHZ = 1000;
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME = "Autonomiccs S.O.";
    private static final String AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME = "Autonomiccs system VMs service offering";

    @Test
    public void searchServiceOfferingByNameTest() {
        List<ServiceOfferingVO> allServiceOffering = createServiceOfferingVOList(1);
        Mockito.doReturn(allServiceOffering).when(serviceOfferingDao).listAll();

        ServiceOfferingVO result = spy.searchServiceOfferingByName("serviceOfferingVoName");

        Mockito.verify(serviceOfferingDao).listAll();
        Assert.assertEquals(allServiceOffering.get(0), result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void searchServiceOfferingByNameTestExpectCloudRuntimeException() {
        List<ServiceOfferingVO> allServiceOffering = createServiceOfferingVOList(1);
        Mockito.doReturn(allServiceOffering).when(serviceOfferingDao).listAll();
        spy.searchServiceOfferingByName("thisNameDoesNotExist");
    }

    @Test
    public void searchAutonomiccsServiceOfferingTest() {
        ServiceOfferingVO serviceOffering = createServiceOfferingVo("serviceOfferingVoName");
        Mockito.doReturn(serviceOffering).when(spy).searchServiceOfferingByName(Mockito.eq(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME));
        ServiceOfferingVO result = spy.searchAutonomiccsServiceOffering();

        Mockito.verify(spy).searchServiceOfferingByName(Mockito.eq(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME));
        Assert.assertEquals(serviceOffering, result);
    }

    @Test
    public void searchServiceOfferingByIdTest() {
        ServiceOfferingVO serviceOffering = createServiceOfferingVo("serviceOfferingVoName");
        Mockito.doReturn(serviceOffering).when(serviceOfferingDao).findById(Mockito.anyLong());

        ServiceOfferingVO result = spy.searchServiceOfferingById(11l);

        Mockito.verify(serviceOfferingDao).findById(Mockito.eq(11l));
        Assert.assertEquals(serviceOffering, result);
    }

    @Test
    public void createAutonomiccsServiceOfferingTest() {
        List<ServiceOfferingVO> allServiceOffering = createServiceOfferingVOList(2);
        configureAndExecuteCreateAutonomiccsServiceOfferingTest(allServiceOffering);
    }

    @Test(expected = CloudRuntimeException.class)
    public void createAutonomiccsServiceOfferingTestOfferingsSizeLessThanTwo() {
        List<ServiceOfferingVO> allServiceOffering = createServiceOfferingVOList(1);
        configureAndExecuteCreateAutonomiccsServiceOfferingTest(allServiceOffering);
    }

    @Test(expected = CloudRuntimeException.class)
    public void createAutonomiccsServiceOfferingTestOfferingsNull() {
        configureAndExecuteCreateAutonomiccsServiceOfferingTest(null);
    }

    private List<ServiceOfferingVO> createServiceOfferingVOList(int offerings) {
        List<ServiceOfferingVO> allServiceOffering = new ArrayList<>();
        ServiceOfferingVO serviceOfferingVo = createServiceOfferingVo("serviceOfferingVoName");
        allServiceOffering.add(serviceOfferingVo);
        for (int i = 1; i < offerings; i++) {
            allServiceOffering.add(createServiceOfferingVo("serviceOfferingVoName2"));
        }
        return allServiceOffering;
    }

    private ServiceOfferingVO createServiceOfferingVo(String name) {
        ServiceOfferingVO serviceOfferingVo = Mockito.mock(ServiceOfferingVO.class);
        Mockito.when(serviceOfferingVo.getUniqueName()).thenReturn(name);
        return serviceOfferingVo;
    }

    private void configureAndExecuteCreateAutonomiccsServiceOfferingTest(List<ServiceOfferingVO> allServiceOffering) {
        Mockito.doReturn(allServiceOffering).when(serviceOfferingDao).createSystemServiceOfferings(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME,
                AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME, 1, AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE, AUTONOMICCS_SYSTEM_VM_CPUMHZ, 0, 0, false, null,
                Storage.ProvisioningType.THIN, true, null, true, VirtualMachine.Type.Instance, true);

        spy.createAutonomiccsServiceOffering();

        Mockito.verify(serviceOfferingDao).createSystemServiceOfferings(AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_NAME, AUTONOMICCS_SYSTEM_VM_SERVICE_OFFERING_UNIQUE_NAME, 1,
                AUTONOMICCS_SYSTEM_VM_VM_RAMSIZE, AUTONOMICCS_SYSTEM_VM_CPUMHZ, 0, 0, false, null, Storage.ProvisioningType.THIN, true, null, true, VirtualMachine.Type.Instance,
                true);
    }

}
