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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

@RunWith(MockitoJUnitRunner.class)
public class VirtualMachineServiceTest {

    @Spy
    @InjectMocks
    private VirtualMachineService spy;
    @Mock
    private VMInstanceDao vmInstanceDao;

    @Test
    public void searchVmInstanceByIdTest() {
        VMInstanceVO vm = Mockito.mock(VMInstanceVO.class);
        Mockito.doReturn(vm).when(vmInstanceDao).findById(Mockito.anyLong());

        VMInstanceVO result = spy.searchVmInstanceById(0l);

        Mockito.verify(vmInstanceDao).findById(Mockito.anyLong());
        Assert.assertEquals(vm, result);
    }

    @Test
    public void updateTest() {
        VMInstanceVO vm = Mockito.mock(VMInstanceVO.class);
        Mockito.doReturn(true).when(vmInstanceDao).update(Mockito.anyLong(), Mockito.any(VMInstanceVO.class));

        spy.update(0l, vm);

        Mockito.verify(vmInstanceDao).update(Mockito.anyLong(), Mockito.any(VMInstanceVO.class));
    }

    @Test
    public void removeTest() {
        Mockito.doReturn(true).when(vmInstanceDao).remove(Mockito.anyLong());
        spy.remove(0l);
        Mockito.verify(vmInstanceDao).remove(Mockito.anyLong());
    }

}
