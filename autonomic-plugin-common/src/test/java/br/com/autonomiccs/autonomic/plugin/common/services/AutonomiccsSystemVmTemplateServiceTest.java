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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.utils.exception.CloudRuntimeException;

import br.com.autonomiccs.autonomic.plugin.common.daos.AutonomiccsSystemVmTemplateJdbcDao;

@RunWith(MockitoJUnitRunner.class)
public class AutonomiccsSystemVmTemplateServiceTest {

    @Spy
    @InjectMocks
    private AutonomiccsSystemVmTemplateService spy;
    @Mock
    private AutonomiccsSystemVmTemplateJdbcDao autonomiccsSystemVmTemplateJdbcDao;
    @Mock
    private VMTemplateDao templateDao;

    @Before
    public void setup() {
        spy.autonomiccsSystemVmTemplateDisplayText = "autonomiccsSystemVmTemplateDisplayText";
        spy.autonomiccsSystemVmTemplateName = "autonomiccsSystemVmTemplateName";
        spy.autonomiccsSystemVmsTemplateUrlBase = "autonomiccsSystemVmsTemplateUrlBase";
        spy.systemVmTemplateVersion = "systemVmTemplateVersion";
    }

    @Test
    public void isTemplateRegisteredTest() {
        boolean result = executeIsTemplateRegisteredTest(true);
        Assert.assertEquals(true, result);
    }

    @Test
    public void isTemplateRegisteredTestFalse() {
        boolean result = executeIsTemplateRegisteredTest(false);
        Assert.assertEquals(false, result);
    }

    @Test
    public void isTemplateRegisteredForHypervisorTest() {
        executeIsTemplateRegisteredForHypervisorTest(true);
    }

    @Test
    public void isTemplateRegisteredForHypervisorTestFalse() {
        executeIsTemplateRegisteredForHypervisorTest(false);
    }

    @Test
    public void getAutonomiccsSystemVmTemplateDisplayTextTest() {
        String result = spy.getAutonomiccsSystemVmTemplateDisplayText(HypervisorType.Any);
        Assert.assertEquals("autonomiccsSystemVmTemplateDisplayText - any", result);
    }

    @Test
    public void getAutonomiccsSystemVmTemplateNameTest() {
        String result = spy.getAutonomiccsSystemVmTemplateName(HypervisorType.Any);
        Assert.assertEquals("autonomiccsSystemVmTemplateName-any", result);
    }

    @Test
    public void constructSystemVmTemplateUrlTest() {
        Mockito.doReturn(null).when(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.doReturn("autonomiccsSystemVmTemplateName-xenserver").when(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));

        String result = spy.constructSystemVmTemplateUrl(HypervisorType.XenServer);

        Mockito.verify(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Assert.assertEquals("autonomiccsSystemVmsTemplateUrlBase/autonomiccsSystemVmTemplateName-xenserver.vhd", result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void constructSystemVmTemplateUrlTestExpectCloudRuntimeException() {
        spy.constructSystemVmTemplateUrl(HypervisorType.Any);
    }

    @Test
    public void getSystemVmTemplateUrlTestXenServer() {
        executeGetSystemVmTemplateUrlTest(HypervisorType.XenServer);
    }

    @Test
    public void getSystemVmTemplateUrlTestKvm() {
        executeGetSystemVmTemplateUrlTest(HypervisorType.KVM);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getSystemVmTemplateUrlTestExpectCloudRuntimeException() {
        spy.getSystemVmTemplateUrl(HypervisorType.Any);
    }

    @Test
    public void findAutonomiccsSystemVmTemplateTest() {
        VMTemplateVO vmTemplate = Mockito.mock(VMTemplateVO.class);
        Mockito.doReturn(StringUtils.EMPTY).when(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.doReturn(0l).when(autonomiccsSystemVmTemplateJdbcDao).searchAutonomiccsSystemVmTemplateIdForHypervisor(Mockito.anyString());
        Mockito.doReturn(vmTemplate).when(templateDao).findById(Mockito.anyLong());

        VMTemplateVO result = spy.findAutonomiccsSystemVmTemplate(HypervisorType.Any);

        Mockito.verify(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.verify(autonomiccsSystemVmTemplateJdbcDao).searchAutonomiccsSystemVmTemplateIdForHypervisor(Mockito.anyString());
        Mockito.verify(templateDao).findById(Mockito.anyLong());
        Assert.assertEquals(vmTemplate, result);
    }

    @Test
    public void afterPropertiesSetTest() throws Exception {
        spy.afterPropertiesSet();
        Assert.assertEquals("Autonomiccs System VM template (systemVmTemplateVersion)", spy.autonomiccsSystemVmTemplateDisplayText);
        Assert.assertEquals("autonomiccs-systemVm-systemVmTemplateVersion", spy.autonomiccsSystemVmTemplateName);
    }

    @Test
    public void isTemplateRegisteredAndReadyForHypervisorTestTrue() {
        executeIsTemplateRegisteredAndReadyForHypervisor(true);
    }

    @Test
    public void isTemplateRegisteredAndReadyForHypervisorTestFalse() {
        executeIsTemplateRegisteredAndReadyForHypervisor(false);
    }

    private void executeIsTemplateRegisteredAndReadyForHypervisor(boolean testWithBoolean) {
        Mockito.doReturn("").when(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.doReturn(testWithBoolean).when(autonomiccsSystemVmTemplateJdbcDao).isTemplateRegisteredAndReady(Mockito.anyString());

        boolean result = spy.isTemplateRegisteredAndReadyForHypervisor(HypervisorType.Any);

        Mockito.verify(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.verify(autonomiccsSystemVmTemplateJdbcDao).isTemplateRegisteredAndReady(Mockito.anyString());
        Assert.assertEquals(testWithBoolean, result);
    }

    private boolean executeIsTemplateRegisteredTest(boolean isRegistered) {
        Mockito.doReturn(isRegistered).when(autonomiccsSystemVmTemplateJdbcDao).isTemplateRegistered(Mockito.anyString());
        boolean result = spy.isTemplateRegistered("templateName");
        Mockito.verify(autonomiccsSystemVmTemplateJdbcDao).isTemplateRegistered(Mockito.anyString());
        return result;
    }

    private void executeIsTemplateRegisteredForHypervisorTest(boolean isTemplateRegistered) {
        Mockito.doReturn("String").when(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.doReturn(isTemplateRegistered).when(spy).isTemplateRegistered(Mockito.anyString());

        boolean result = spy.isTemplateRegisteredForHypervisor(HypervisorType.Any);

        Mockito.verify(spy).getAutonomiccsSystemVmTemplateName(Mockito.any(HypervisorType.class));
        Mockito.verify(spy).isTemplateRegistered(Mockito.anyString());
        Assert.assertEquals(isTemplateRegistered, result);
    }

    private void executeGetSystemVmTemplateUrlTest(HypervisorType hypervisorType) {
        Mockito.doReturn("expected").when(spy).constructSystemVmTemplateUrl(Mockito.any(HypervisorType.class));

        String result = spy.getSystemVmTemplateUrl(hypervisorType);

        Mockito.verify(spy).constructSystemVmTemplateUrl(Mockito.any(HypervisorType.class));
        Assert.assertEquals("expected", result);
    }

}
